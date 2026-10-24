package com.perigrine3.createcybernetics.item.sculked;

import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.api.CyberwareDurabilityCategory;
import com.perigrine3.createcybernetics.api.CyberwareRepairType;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class SculkedBrainItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final int TICKS_PER_DAY = 24000;
    private static final float SCULKED_CHANCE_PER_DAY = 0.25F;

    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final int NEAR_RADIUS_BLOCKS = 64;
    private static final int LOCAL_DETERMINISTIC_RADIUS = 6;

    private static final float HURT_CHANCE_PER_CHECK = 0.35F;
    private static final float HURT_AMOUNT = 1.0F;

    private static final Block[] SCULK_BLOCKS = new Block[] {
            net.minecraft.world.level.block.Blocks.SCULK,
            net.minecraft.world.level.block.Blocks.SCULK_VEIN,
            net.minecraft.world.level.block.Blocks.SCULK_CATALYST,
            net.minecraft.world.level.block.Blocks.SCULK_SENSOR,
            net.minecraft.world.level.block.Blocks.CALIBRATED_SCULK_SENSOR,
            net.minecraft.world.level.block.Blocks.SCULK_SHRIEKER
    };

    private static final Component[] CALL_PHRASES = new Component[] {
            Component.literal("RETURN"),
            Component.literal("MERGE"),
            Component.literal("BELOW"),
            Component.literal("CONSUME"),
            Component.literal("NEED"),
            Component.literal("FAR"),
            Component.literal("CLOSER"),
            Component.literal("INTEGRATE")
    };

    public SculkedBrainItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public CyberwareRepairType getRepairType(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareRepairType.BIOLOGICAL;
    }

    @Override
    public CyberwareDurabilityCategory getDurabilityCategory(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareDurabilityCategory.WETWARE;
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<TagKey<Item>> incompatibleCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModTags.Items.BRAIN_ITEMS);
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.BRAIN);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(CyberwareSlot.BRAIN);
    }

    @Override
    public void onTick(Player player) {
        Level level = player.level();

        if (level.isClientSide) return;

        if (!player.hasEffect(ModEffects.SCULKED_EFFECT)) {
            if ((player.tickCount % TICKS_PER_DAY) == 0) {
                if (player.getRandom().nextFloat() < SCULKED_CHANCE_PER_DAY) {
                    player.addEffect(new MobEffectInstance(ModEffects.SCULKED_EFFECT, Integer.MAX_VALUE, 0, false, false, true));
                }
            }
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!StaggeredTickScheduler.shouldRun(player, CHECK_INTERVAL_TICKS, StaggeredTickScheduler.TickPhase.SCULKED_CHECK)) {
            return;
        }
        if (!hasThisInstalled(serverPlayer)) return;
        if (player.getRandom().nextFloat() >= HURT_CHANCE_PER_CHECK) return;
        if (isNearSculk(serverPlayer, NEAR_RADIUS_BLOCKS)) return;

        serverPlayer.hurt(serverPlayer.damageSources().magic(), HURT_AMOUNT);
        flashHivemindMessage(serverPlayer);
    }

    private static boolean hasThisInstalled(ServerPlayer player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        if (data == null) return false;

        return data.hasSpecificItem(ModItems.BODYPART_SCULKBRAIN.get(), CyberwareSlot.BRAIN);
    }

    private static boolean isNearSculk(ServerPlayer player, int radius) {
        Level level = player.level();
        BlockPos origin = player.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int dy = -LOCAL_DETERMINISTIC_RADIUS; dy <= LOCAL_DETERMINISTIC_RADIUS; dy++) {
            for (int dx = -LOCAL_DETERMINISTIC_RADIUS; dx <= LOCAL_DETERMINISTIC_RADIUS; dx++) {
                for (int dz = -LOCAL_DETERMINISTIC_RADIUS; dz <= LOCAL_DETERMINISTIC_RADIUS; dz++) {
                    mutablePos.setWithOffset(origin, dx, dy, dz);

                    if (!level.hasChunkAt(mutablePos)) continue;

                    if (isSculkBlock(level.getBlockState(mutablePos).getBlock())) {
                        return true;
                    }
                }
            }
        }

        var rng = player.getRandom();
        int r2 = radius * radius;

        final int samples = 512;

        for (int i = 0; i < samples; i++) {
            int dx = rng.nextInt(radius * 2 + 1) - radius;
            int dy = rng.nextInt(radius * 2 + 1) - radius;
            int dz = rng.nextInt(radius * 2 + 1) - radius;

            if ((dx * dx + dy * dy + dz * dz) > r2) continue;

            mutablePos.setWithOffset(origin, dx, dy, dz);

            if (!level.hasChunkAt(mutablePos)) continue;

            if (isSculkBlock(level.getBlockState(mutablePos).getBlock())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSculkBlock(Block block) {
        for (Block sculkBlock : SCULK_BLOCKS) {
            if (block == sculkBlock) return true;
        }

        return false;
    }

    private static void flashHivemindMessage(ServerPlayer player) {
        int index = player.getRandom().nextInt(CALL_PHRASES.length);
        Component title = CALL_PHRASES[index];

        player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 10, 10));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }
}