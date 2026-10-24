package com.perigrine3.createcybernetics.cyberspace;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.block.entity.ServerTowerBlockEntity;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.cyberspace.IceDetectionManager;
import com.perigrine3.createcybernetics.common.multiblock.ServerTowerMultiblock;
import com.perigrine3.createcybernetics.common.toggle.CyberwareToggleController;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.entity.ModEntities;
import com.perigrine3.createcybernetics.entity.custom.JackedInPlayerBodyEntity;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.network.payload.CyberspaceProgressPayload;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CyberspaceTravelManager {
    private static final String NBT_RETURN = "CreateCyberneticsCyberspaceReturn";
    private static final String NBT_DIMENSION = "Dimension";
    private static final String NBT_X = "X";
    private static final String NBT_Y = "Y";
    private static final String NBT_Z = "Z";
    private static final String NBT_YAW = "Yaw";
    private static final String NBT_PITCH = "Pitch";
    private static final String NBT_BODY_UUID = "BodyUUID";

    private static final String NBT_SERVER_TOWER_ID = "ServerTowerId";
    private static final String NBT_SERVER_TOWER_DIMENSION = "ServerTowerDimension";
    private static final String NBT_SERVER_TOWER_X = "ServerTowerX";
    private static final String NBT_SERVER_TOWER_Y = "ServerTowerY";
    private static final String NBT_SERVER_TOWER_Z = "ServerTowerZ";

    private static final String CYBERDECK_CYBERSPACE_INVENTORY = "CreateCyberneticsCyberspaceInventory";
    private static final String NBT_INVENTORY_ITEMS = "Items";
    private static final String NBT_INVENTORY_SLOT = "Slot";
    private static final String NBT_INVENTORY_STACK = "Stack";
    private static final String NBT_SELECTED_SLOT = "SelectedSlot";

    private static final int JACK_IN_TICKS = 200;
    private static final int JACK_OUT_TICKS = 200;
    private static final int UNSAFE_JACK_OUT_TICKS = 200;
    private static final int PACKET_GAP_TOLERANCE = 2;

    public static final int SYNAPSE_BURNOUT_DURATION = 20 * 60 * 3;

    private static final Map<UUID, JackInProgress> JACK_IN_PROGRESS = new HashMap<>();
    private static final Map<UUID, JackOutProgress> UNSAFE_JACK_OUT_PROGRESS = new HashMap<>();
    private static final Map<UUID, JackOutProgress> JACK_OUT_PROGRESS = new HashMap<>();

    private CyberspaceTravelManager() {}

    public static void handleJackInHold(ServerPlayer player, boolean held) {
        if (player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            interruptJackIn(player);
            return;
        }

        if (!held) {
            interruptJackIn(player);
            return;
        }

        if (player.hasEffect(ModEffects.SYNAPSE_BURNOUT)) {
            interruptJackIn(player);
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.synapse_burnout"), true);
            return;
        }

        BlockPos towerPos = getLookedAtServerTower(player);

        if (towerPos == null) {
            interruptJackIn(player);
            return;
        }

        if (!(player.level().getBlockEntity(towerPos) instanceof ServerTowerBlockEntity tower)) {
            interruptJackIn(player);
            return;
        }

        if (!tower.hasProvisionedArchitecture()) {
            interruptJackIn(player);
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.unprovisioned_server"), true);
            return;
        }

        if (!tower.isOperational()) {
            interruptJackIn(player);
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.missing_power"), true);
            return;
        }

        if (!hasCoaxialPort(player)) {
            interruptJackIn(player);
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.requires_coaxial_port"), true);
            return;
        }

        if (!hasLinkCableInHand(player)) {
            interruptJackIn(player);
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.requires_link_cable"), true);
            return;
        }

        long gameTime = player.level().getGameTime();
        JackInProgress progress = JACK_IN_PROGRESS.get(player.getUUID());

        if (progress == null || gameTime - progress.lastTick > PACKET_GAP_TOLERANCE || !progress.towerPos.equals(towerPos)) {
            progress = new JackInProgress(towerPos);
            JACK_IN_PROGRESS.put(player.getUUID(), progress);
        }

        if (progress.lastTick == gameTime) {
            return;
        }

        progress.lastTick = gameTime;
        progress.ticks++;

        int percent = Math.min(100, Math.round(progress.ticks * 100.0F / JACK_IN_TICKS));
        sendProgress(player, CyberspaceProgressPayload.JACK_IN, percent);

        if (progress.ticks >= JACK_IN_TICKS) {
            JACK_IN_PROGRESS.remove(player.getUUID());
            clearProgressDisplay(player);
            enterCyberspace(player, tower);
        }
    }

    private static BlockPos getLookedAtServerTower(ServerPlayer player) {
        HitResult hitResult = player.pick(6.0D, 1.0F, false);

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return null;
        }

        BlockPos hitPos = blockHitResult.getBlockPos();
        BlockState hitState = player.level().getBlockState(hitPos);

        if (hitState.is(ModBlocks.SERVER_TOWER.get())) {
            return hitPos;
        }

        if (!hitState.is(ModBlocks.SERVER_TOWER_PART.get())) {
            return null;
        }

        BlockPos towerPos = ServerTowerMultiblock.getControllerPos(hitPos, hitState);

        if (towerPos == null || !player.level().getBlockState(towerPos).is(ModBlocks.SERVER_TOWER.get())) {
            return null;
        }

        return towerPos;
    }

    private static boolean hasCoaxialPort(ServerPlayer player) {
        if (!player.hasData(ModAttachments.CYBERWARE)) {
            return false;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        return data != null && data.hasSpecificItem(ModItems.BRAINUPGRADES_COAXIALPORT.get(), CyberwareSlot.BRAIN);
    }

    private static boolean hasLinkCableInHand(ServerPlayer player) {
        return player.getMainHandItem().is(ModItems.LINK_CABLE.get()) || player.getOffhandItem().is(ModItems.LINK_CABLE.get());
    }

    private static boolean enterCyberspace(ServerPlayer player, ServerTowerBlockEntity tower) {
        if (player.hasEffect(ModEffects.SYNAPSE_BURNOUT)) {
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.synapse_burnout"), true);
            return false;
        }

        if (!tower.hasProvisionedArchitecture()) {
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.unprovisioned_server"), true);
            return false;
        }

        if (!tower.isOperational()) {
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.missing_power"), true);
            return false;
        }

        ServerLevel cyberspace = player.server.getLevel(ModDimensions.CYBERSPACE_LEVEL);

        if (cyberspace == null) {
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.unavailable"), true);
            return false;
        }

        ResourceLocation towerDimension = player.level().dimension().location();
        BlockPos towerPos = tower.getBlockPos();
        UUID towerId = tower.getServerId();

        CyberspaceDungeonSavedData.DungeonRecord dungeon = CyberspaceDungeonManager.getOrGenerateProvisionedDungeon(player.server, towerId);

        if (dungeon == null) {
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.unavailable"), true);
            return false;
        }

        recordReturnPoint(player);

        JackedInPlayerBodyEntity body = getStandInBody(player);

        if (body == null) {
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.unavailable"), true);
            return false;
        }

        CyberwareToggleController.forceAllToggleablesInactiveForCyberspace(player);

        CompoundTag returnTag = player.getPersistentData().getCompound(NBT_RETURN);
        returnTag.putUUID(NBT_SERVER_TOWER_ID, towerId);
        returnTag.putString(NBT_SERVER_TOWER_DIMENSION, towerDimension.toString());
        returnTag.putInt(NBT_SERVER_TOWER_X, towerPos.getX());
        returnTag.putInt(NBT_SERVER_TOWER_Y, towerPos.getY());
        returnTag.putInt(NBT_SERVER_TOWER_Z, towerPos.getZ());
        player.getPersistentData().put(NBT_RETURN, returnTag);

        clearPlayerInventory(player);
        loadSavedCyberspaceInventory(player);

        BlockPos spawn = dungeon.entryPos();

        player.teleportTo(cyberspace, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.fallDistance = 0.0F;

        player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.jack_in_complete"), true);

        return true;
    }

    public static void recordReturnPoint(ServerPlayer player) {
        if (player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            return;
        }

        CompoundTag returnTag = new CompoundTag();
        returnTag.putString(NBT_DIMENSION, player.level().dimension().location().toString());
        returnTag.putDouble(NBT_X, player.getX());
        returnTag.putDouble(NBT_Y, player.getY());
        returnTag.putDouble(NBT_Z, player.getZ());
        returnTag.putFloat(NBT_YAW, player.getYRot());
        returnTag.putFloat(NBT_PITCH, player.getXRot());

        player.getPersistentData().put(NBT_RETURN, returnTag);

        createStandInBody(player);
    }

    public static boolean hasReturnPoint(ServerPlayer player) {
        return player.getPersistentData().contains(NBT_RETURN);
    }

    private static void sendProgress(ServerPlayer player, int mode, int percent) {
        PacketDistributor.sendToPlayer(player, new CyberspaceProgressPayload(mode, Math.max(0, Math.min(100, percent)), true));
    }

    private static void clearProgressDisplay(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new CyberspaceProgressPayload(0, 0, false));
    }

    private static void interruptJackIn(ServerPlayer player) {
        JACK_IN_PROGRESS.remove(player.getUUID());
        clearProgressDisplay(player);
    }

    private static void interruptJackOut(ServerPlayer player) {
        JACK_OUT_PROGRESS.remove(player.getUUID());
        clearProgressDisplay(player);
    }

    private static void interruptUnsafeJackOut(ServerPlayer player) {
        UNSAFE_JACK_OUT_PROGRESS.remove(player.getUUID());
        clearProgressDisplay(player);
    }

    public static void handleJackOutHold(ServerPlayer player, boolean held) {
        if (!player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            interruptJackOut(player);
            return;
        }

        if (!hasReturnPoint(player)) {
            interruptJackOut(player);
            return;
        }

        if (!held) {
            interruptJackOut(player);
            return;
        }

        if (!isLookingAtAccessNode(player)) {
            interruptJackOut(player);
            return;
        }

        long gameTime = player.level().getGameTime();
        JackOutProgress progress = JACK_OUT_PROGRESS.get(player.getUUID());

        if (progress == null || gameTime - progress.lastTick > PACKET_GAP_TOLERANCE) {
            progress = new JackOutProgress();
            JACK_OUT_PROGRESS.put(player.getUUID(), progress);
        }

        if (progress.lastTick == gameTime) {
            return;
        }

        progress.lastTick = gameTime;
        progress.ticks++;

        int percent = Math.min(100, Math.round(progress.ticks * 100.0F / JACK_OUT_TICKS));
        sendProgress(player, CyberspaceProgressPayload.SAFE_JACK_OUT, percent);

        if (progress.ticks >= JACK_OUT_TICKS) {
            JACK_OUT_PROGRESS.remove(player.getUUID());
            clearProgressDisplay(player);
            safeJackOut(player);
        }
    }

    public static boolean safeJackOut(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains(NBT_RETURN)) {
            return false;
        }

        CompoundTag returnTag = persistentData.getCompound(NBT_RETURN);

        ResourceLocation dimensionId = ResourceLocation.tryParse(returnTag.getString(NBT_DIMENSION));
        if (dimensionId == null) {
            return false;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel returnLevel = player.server.getLevel(dimension);

        if (returnLevel == null) {
            return false;
        }

        JackedInPlayerBodyEntity body = getStandInBody(player);

        double x = returnTag.getDouble(NBT_X);
        double y = returnTag.getDouble(NBT_Y);
        double z = returnTag.getDouble(NBT_Z);
        float yaw = returnTag.getFloat(NBT_YAW);
        float pitch = returnTag.getFloat(NBT_PITCH);
        float bodyHealth = player.getHealth();

        if (body != null && body.isAlive()) {
            x = body.getX();
            y = body.getY();
            z = body.getZ();
            yaw = body.getYRot();
            pitch = body.getXRot();
            bodyHealth = body.getHealth();
        }

        saveCurrentCyberspaceInventory(player);
        clearPlayerInventory(player);

        if (body != null) {
            body.restoreMeatspaceInventory(player);
        }

        clearProgress(player);

        player.teleportTo(returnLevel, x, y, z, yaw, pitch);
        player.fallDistance = 0.0F;
        player.setHealth(Math.min(player.getMaxHealth(), bodyHealth));

        if (body != null) {
            body.discardStoredInventory();
            body.discard();
        }

        persistentData.remove(NBT_RETURN);

        player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.safe_jack_out"), true);

        return true;
    }

    public static void handleUnsafeJackOutHold(ServerPlayer player, boolean held) {
        if (!player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            interruptUnsafeJackOut(player);
            return;
        }

        if (!hasReturnPoint(player)) {
            interruptUnsafeJackOut(player);
            return;
        }

        if (!held) {
            interruptUnsafeJackOut(player);
            return;
        }

        long gameTime = player.level().getGameTime();
        JackOutProgress progress = UNSAFE_JACK_OUT_PROGRESS.get(player.getUUID());

        if (progress == null || gameTime - progress.lastTick > PACKET_GAP_TOLERANCE) {
            progress = new JackOutProgress();
            UNSAFE_JACK_OUT_PROGRESS.put(player.getUUID(), progress);
        }

        if (progress.lastTick == gameTime) {
            return;
        }

        progress.lastTick = gameTime;
        progress.ticks++;

        int percent = Math.min(100, Math.round(progress.ticks * 100.0F / UNSAFE_JACK_OUT_TICKS));
        sendProgress(player, CyberspaceProgressPayload.UNSAFE_JACK_OUT, percent);

        if (progress.ticks >= UNSAFE_JACK_OUT_TICKS) {
            UNSAFE_JACK_OUT_PROGRESS.remove(player.getUUID());
            clearProgressDisplay(player);
            unsafeJackOut(player);
        }
    }

    public static boolean unsafeJackOut(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains(NBT_RETURN)) {
            return false;
        }

        CompoundTag returnTag = persistentData.getCompound(NBT_RETURN);

        ResourceLocation dimensionId = ResourceLocation.tryParse(returnTag.getString(NBT_DIMENSION));
        if (dimensionId == null) {
            return false;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel returnLevel = player.server.getLevel(dimension);

        if (returnLevel == null) {
            return false;
        }

        JackedInPlayerBodyEntity body = getStandInBody(player);

        double x = returnTag.getDouble(NBT_X);
        double y = returnTag.getDouble(NBT_Y);
        double z = returnTag.getDouble(NBT_Z);
        float yaw = returnTag.getFloat(NBT_YAW);
        float pitch = returnTag.getFloat(NBT_PITCH);
        float bodyHealth = player.getHealth();

        if (body != null && body.isAlive()) {
            x = body.getX();
            y = body.getY();
            z = body.getZ();
            yaw = body.getYRot();
            pitch = body.getXRot();
            bodyHealth = body.getHealth();
        }

        clearPlayerInventory(player);

        if (body != null) {
            body.restoreMeatspaceInventory(player);
        }

        clearProgress(player);

        player.teleportTo(returnLevel, x, y, z, yaw, pitch);
        player.fallDistance = 0.0F;
        player.setHealth(Math.min(player.getMaxHealth(), bodyHealth));

        if (body != null) {
            body.discardStoredInventory();
            body.discard();
        }

        persistentData.remove(NBT_RETURN);

        applySynapseBurnout(player);

        if (player.getRandom().nextFloat() < 0.15F) {
            player.igniteForSeconds(3.0F + player.getRandom().nextFloat() * 3.0F);
        }

        player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.unsafe_jack_out_complete"), true);

        return true;
    }

    public static boolean handleCyberspaceDeath(ServerPlayer player) {
        if (!player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            return false;
        }

        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains(NBT_RETURN)) {
            return false;
        }

        CompoundTag returnTag = persistentData.getCompound(NBT_RETURN);

        ResourceLocation dimensionId = ResourceLocation.tryParse(returnTag.getString(NBT_DIMENSION));
        if (dimensionId == null) {
            return false;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel returnLevel = player.server.getLevel(dimension);

        if (returnLevel == null) {
            return false;
        }

        JackedInPlayerBodyEntity body = getStandInBody(player);

        double x = returnTag.getDouble(NBT_X);
        double y = returnTag.getDouble(NBT_Y);
        double z = returnTag.getDouble(NBT_Z);
        float yaw = returnTag.getFloat(NBT_YAW);
        float pitch = returnTag.getFloat(NBT_PITCH);
        float bodyHealth = player.getMaxHealth();

        if (body != null && body.isAlive()) {
            x = body.getX();
            y = body.getY();
            z = body.getZ();
            yaw = body.getYRot();
            pitch = body.getXRot();
            bodyHealth = body.getHealth();
        }

        clearPlayerInventory(player);

        if (body != null) {
            body.restoreMeatspaceInventory(player);
        }

        clearProgress(player);

        player.teleportTo(returnLevel, x, y, z, yaw, pitch);
        player.fallDistance = 0.0F;
        player.setHealth(Math.max(1.0F, Math.min(player.getMaxHealth(), bodyHealth)));

        if (body != null) {
            body.discardStoredInventory();
            body.discard();
        }

        persistentData.remove(NBT_RETURN);

        applySynapseBurnout(player);

        player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.unsafe_jack_out_complete"), true);

        return true;
    }

    public static void handleStandInBodyDeath(ServerPlayer player, JackedInPlayerBodyEntity body) {
        if (!player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            return;
        }

        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains(NBT_RETURN)) {
            return;
        }

        CompoundTag returnTag = persistentData.getCompound(NBT_RETURN);

        ResourceLocation dimensionId = ResourceLocation.tryParse(returnTag.getString(NBT_DIMENSION));
        if (dimensionId == null) {
            return;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel returnLevel = player.server.getLevel(dimension);

        if (returnLevel == null) {
            return;
        }

        double x = body != null ? body.getX() : returnTag.getDouble(NBT_X);
        double y = body != null ? body.getY() : returnTag.getDouble(NBT_Y);
        double z = body != null ? body.getZ() : returnTag.getDouble(NBT_Z);
        float yaw = body != null ? body.getYRot() : returnTag.getFloat(NBT_YAW);
        float pitch = body != null ? body.getXRot() : returnTag.getFloat(NBT_PITCH);

        clearPlayerInventory(player);
        clearProgress(player);

        if (body != null) {
            body.discardStoredInventory();
        }

        player.teleportTo(returnLevel, x, y, z, yaw, pitch);
        player.fallDistance = 0.0F;

        persistentData.remove(NBT_RETURN);

        applySynapseBurnout(player);

        player.die(player.damageSources().generic());
    }

    private static boolean isLookingAtAccessNode(ServerPlayer player) {
        HitResult hitResult = player.pick(6.0D, 1.0F, false);

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return false;
        }

        return player.level().getBlockState(blockHitResult.getBlockPos()).is(ModBlocks.ACCESS_NODE.get());
    }

    public static void applySynapseBurnout(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(ModEffects.SYNAPSE_BURNOUT, SYNAPSE_BURNOUT_DURATION, 0, false, false, true));
    }

    private static JackedInPlayerBodyEntity createStandInBody(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return null;
        }

        JackedInPlayerBodyEntity existingBody = getStandInBody(player);

        if (existingBody != null) {
            existingBody.discard();
        }

        JackedInPlayerBodyEntity body = ModEntities.JACKED_IN_PLAYER_BODY.get().create(level);

        if (body == null) {
            return null;
        }

        body.initializeFromPlayer(player);

        if (!level.addFreshEntity(body)) {
            return null;
        }

        CompoundTag returnTag = player.getPersistentData().getCompound(NBT_RETURN);
        returnTag.putUUID(NBT_BODY_UUID, body.getUUID());
        player.getPersistentData().put(NBT_RETURN, returnTag);

        return body;
    }

    public static JackedInPlayerBodyEntity getStandInBody(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains(NBT_RETURN)) {
            return null;
        }

        CompoundTag returnTag = persistentData.getCompound(NBT_RETURN);

        if (!returnTag.hasUUID(NBT_BODY_UUID)) {
            return null;
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(returnTag.getString(NBT_DIMENSION));

        if (dimensionId == null) {
            return null;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel returnLevel = player.server.getLevel(dimension);

        if (returnLevel == null) {
            return null;
        }

        UUID bodyUUID = returnTag.getUUID(NBT_BODY_UUID);

        if (returnLevel.getEntity(bodyUUID) instanceof JackedInPlayerBodyEntity body) {
            return body;
        }

        return null;
    }

    private static void loadSavedCyberspaceInventory(ServerPlayer player) {
        ItemStack cyberdeck = findInstalledCyberdeck(player);

        if (cyberdeck.isEmpty()) {
            clearPlayerInventory(player);
            return;
        }

        CustomData customData = cyberdeck.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag all = customData.copyTag();

        if (!all.contains(CYBERDECK_CYBERSPACE_INVENTORY, Tag.TAG_COMPOUND)) {
            clearPlayerInventory(player);
            return;
        }

        deserializeInventory(player, all.getCompound(CYBERDECK_CYBERSPACE_INVENTORY));
    }

    private static void saveCurrentCyberspaceInventory(ServerPlayer player) {
        ItemStack cyberdeck = findInstalledCyberdeck(player);

        if (cyberdeck.isEmpty()) {
            return;
        }

        CompoundTag inventoryTag = serializeInventory(player);

        CustomData customData = cyberdeck.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag all = customData.copyTag();
        all.put(CYBERDECK_CYBERSPACE_INVENTORY, inventoryTag);

        cyberdeck.set(DataComponents.CUSTOM_DATA, CustomData.of(all));

        if (player.hasData(ModAttachments.CYBERWARE)) {
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

            if (data != null) {
                data.setDirty();
                player.syncData(ModAttachments.CYBERWARE);
            }
        }
    }

    private static ItemStack findInstalledCyberdeck(ServerPlayer player) {
        if (!player.hasData(ModAttachments.CYBERWARE)) {
            return ItemStack.EMPTY;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        if (data == null) {
            return ItemStack.EMPTY;
        }

        InstalledCyberware[] brain = data.getAll().get(CyberwareSlot.BRAIN);

        if (brain == null) {
            return ItemStack.EMPTY;
        }

        for (InstalledCyberware installed : brain) {
            if (installed == null) continue;

            ItemStack stack = installed.getItem();

            if (stack == null || stack.isEmpty()) continue;

            if (stack.is(ModItems.BRAINUPGRADES_CYBERDECK.get())) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static CompoundTag serializeInventory(ServerPlayer player) {
        CompoundTag root = new CompoundTag();
        ListTag items = new ListTag();
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;

            CompoundTag entry = new CompoundTag();
            entry.putInt(NBT_INVENTORY_SLOT, slot);
            entry.put(NBT_INVENTORY_STACK, stack.save(player.registryAccess()));
            items.add(entry);
        }

        root.put(NBT_INVENTORY_ITEMS, items);
        root.putInt(NBT_SELECTED_SLOT, inventory.selected);

        return root;
    }

    private static void deserializeInventory(ServerPlayer player, CompoundTag root) {
        Inventory inventory = player.getInventory();
        inventory.clearContent();

        if (root.contains(NBT_INVENTORY_ITEMS, Tag.TAG_LIST)) {
            ListTag items = root.getList(NBT_INVENTORY_ITEMS, Tag.TAG_COMPOUND);

            for (int i = 0; i < items.size(); i++) {
                CompoundTag entry = items.getCompound(i);
                int slot = entry.getInt(NBT_INVENTORY_SLOT);

                if (slot < 0 || slot >= inventory.getContainerSize()) continue;
                if (!entry.contains(NBT_INVENTORY_STACK, Tag.TAG_COMPOUND)) continue;

                ItemStack stack = ItemStack.parseOptional(player.registryAccess(), entry.getCompound(NBT_INVENTORY_STACK));
                inventory.setItem(slot, stack);
            }
        }

        if (root.contains(NBT_SELECTED_SLOT, Tag.TAG_INT)) {
            inventory.selected = Math.max(0, Math.min(8, root.getInt(NBT_SELECTED_SLOT)));
        }

        syncInventory(player);
    }

    private static void clearPlayerInventory(ServerPlayer player) {
        player.getInventory().clearContent();
        syncInventory(player);
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static void clearProgress(ServerPlayer player) {
        JACK_IN_PROGRESS.remove(player.getUUID());
        JACK_OUT_PROGRESS.remove(player.getUUID());
        UNSAFE_JACK_OUT_PROGRESS.remove(player.getUUID());
        clearProgressDisplay(player);
        IceDetectionManager.endSession(player);
    }

    private static final class JackInProgress {
        private final BlockPos towerPos;
        private int ticks;
        private long lastTick = Long.MIN_VALUE;

        private JackInProgress(BlockPos towerPos) {
            this.towerPos = towerPos.immutable();
        }
    }

    private static final class JackOutProgress {
        private int ticks;
        private long lastTick = Long.MIN_VALUE;
    }
}