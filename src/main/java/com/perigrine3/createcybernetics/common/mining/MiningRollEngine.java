package com.perigrine3.createcybernetics.common.mining;

import com.perigrine3.createcybernetics.common.attributes.ModAttributes;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class MiningRollEngine {

    private static final ExecutorService SNAPSHOT_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "createcybernetics-mining-snapshot");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });

    private static final AsyncOreBlockClassifier CLASSIFIER = new AsyncOreBlockClassifier();
    private static final AtomicReference<MiningRollPolicy> POLICY =
            new AtomicReference<>(StandardMiningRollPolicy.INSTANCE);
    private static final ConcurrentHashMap<UUID, MiningRollSnapshot> PLAYER_SNAPSHOTS =
            new ConcurrentHashMap<>();
    private static final ThreadLocal<MiningBreakContext> ACTIVE_BREAK_CONTEXT =
            new ThreadLocal<>();
    private static final ThreadLocal<Integer> POOL_ENTRY_ROLL_DEPTH =
            ThreadLocal.withInitial(() -> 0);
    private static volatile SilkTouchCacheEntry silkTouchCache;

    private record SilkTouchCacheEntry(RegistryAccess registryAccess, Holder<Enchantment> holder) {
    }

    private MiningRollEngine() {
    }

    public static MiningRollPolicy getPolicy() {
        return POLICY.get();
    }

    public static void setPolicy(MiningRollPolicy policy) {
        if (policy != null) {
            POLICY.set(policy);
        }
    }

    public static AsyncOreBlockClassifier getClassifier() {
        return CLASSIFIER;
    }

    public static CompletableFuture<Map<net.minecraft.world.level.block.Block, OreBlockCategory>> rebuildOreIndexAsync() {
        return CLASSIFIER.rebuildIndexAsync();
    }

    public static void clearAllCaches() {
        CLASSIFIER.invalidateAll();
        PLAYER_SNAPSHOTS.clear();
        ACTIVE_BREAK_CONTEXT.remove();
        POOL_ENTRY_ROLL_DEPTH.remove();
        silkTouchCache = null;
    }

    public static void beginBreakContext(Player player, BlockPos pos, BlockState state, ItemStack tool, long gameTime) {
        if (player == null || state == null) {
            ACTIVE_BREAK_CONTEXT.remove();
            return;
        }
        ItemStack safeTool = tool == null ? ItemStack.EMPTY : tool;
        ACTIVE_BREAK_CONTEXT.set(new MiningBreakContext(player, pos, state, safeTool, gameTime));
    }

    public static void clearBreakContext() {
        ACTIVE_BREAK_CONTEXT.remove();
    }

    public static void enterPoolEntryRoll() {
        POOL_ENTRY_ROLL_DEPTH.set(POOL_ENTRY_ROLL_DEPTH.get() + 1);
    }

    public static void exitPoolEntryRoll() {
        int current = POOL_ENTRY_ROLL_DEPTH.get();
        if (current <= 1) {
            POOL_ENTRY_ROLL_DEPTH.set(0);
        } else {
            POOL_ENTRY_ROLL_DEPTH.set(current - 1);
        }
    }

    public static boolean isInsidePoolEntryRoll() {
        return POOL_ENTRY_ROLL_DEPTH.get() > 0;
    }

    public static void invalidatePlayerSnapshot(UUID playerId) {
        if (playerId != null) {
            PLAYER_SNAPSHOTS.remove(playerId);
        }
    }

    public static void syncOrangeShardAttributes(Player player, int orangeShardCount) {
        if (player == null) {
            return;
        }
        if (orangeShardCount >= 1) {
            CyberwareAttributeHelper.applyModifier(player, "orangeshard_ore");
            CyberwareAttributeHelper.applyModifier(player, "orangeshard_mining");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "orangeshard_ore");
            CyberwareAttributeHelper.removeModifier(player, "orangeshard_mining");
        }

        if (orangeShardCount >= 2) {
            CyberwareAttributeHelper.applyModifier(player, "orangeshard_ore_2");
            CyberwareAttributeHelper.applyModifier(player, "orangeshard_mining_2");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "orangeshard_ore_2");
            CyberwareAttributeHelper.removeModifier(player, "orangeshard_mining_2");
        }
    }

    public static CompletableFuture<MiningRollSnapshot> refreshPlayerSnapshotAsync(Player player) {
        if (player == null || !player.hasData(ModAttachments.CYBERWARE)) {
            return CompletableFuture.completedFuture(null);
        }
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        return refreshPlayerSnapshotAsync(player, data);
    }

    public static CompletableFuture<MiningRollSnapshot> refreshPlayerSnapshotAsync(Player player, PlayerCyberwareData data) {
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }
        final UUID playerId = player.getUUID();
        final long gameTime = player.level() != null ? player.level().getGameTime() : 0L;
        final int shardCount = extractOrangeShardCount(data);
        syncOrangeShardAttributes(player, shardCount);
        final double attributeMultiplier = readOreDropAttribute(player);
        final MiningRollPolicy activePolicy = POLICY.get();

        return CompletableFuture.supplyAsync(() -> {
            double effective = activePolicy.computeEffectiveMultiplier(shardCount, attributeMultiplier);
            MiningRollSnapshot snapshot = new MiningRollSnapshot(playerId, shardCount, effective, gameTime);
            PLAYER_SNAPSHOTS.put(playerId, snapshot);
            return snapshot;
        }, SNAPSHOT_EXECUTOR);
    }

    public static MiningRollSnapshot resolvePlayerSnapshot(Player player) {
        if (player == null) {
            return MiningRollSnapshot.inactive(new UUID(0L, 0L), 0L);
        }

        UUID playerId = player.getUUID();
        long gameTime = player.level() != null ? player.level().getGameTime() : 0L;
        int currentShardCount = extractOrangeShardCount(player);

        MiningRollSnapshot cached = PLAYER_SNAPSHOTS.get(playerId);
        if (cached != null && cached.isFreshForTick(gameTime) && cached.activeShardCount() == currentShardCount) {
            return cached;
        }

        syncOrangeShardAttributes(player, currentShardCount);
        double attributeMultiplier = readOreDropAttribute(player);
        double effective = POLICY.get().computeEffectiveMultiplier(currentShardCount, attributeMultiplier);
        MiningRollSnapshot updated = new MiningRollSnapshot(playerId, currentShardCount, effective, gameTime);
        PLAYER_SNAPSHOTS.put(playerId, updated);
        return updated;
    }

    public static int resolvePoolRolls(int baseRolls, LootContext context) {
        return evaluateDecision(baseRolls, context).resolvedRolls();
    }

    public static Consumer<ItemStack> decorateDropConsumer(Consumer<ItemStack> downstream, LootContext context) {
        if (downstream == null || context == null || isInsidePoolEntryRoll()) {
            return downstream;
        }
        MiningRollDecision decision = evaluateDecision(1, context);
        if (!decision.mode().convertsDebrisToScrap()) {
            return downstream;
        }
        return POLICY.get().adaptDropConsumer(downstream, decision.mode());
    }

    public static MiningRollDecision evaluateDecision(int baseRolls, LootContext context) {
        if (baseRolls <= 0 || context == null || isInsidePoolEntryRoll()) {
            return MiningRollDecision.bypass(baseRolls);
        }

        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || state.isAir()) {
            return MiningRollDecision.bypass(baseRolls);
        }

        OreBlockCategory category = CLASSIFIER.classify(state);
        if (!category.isEligibleOre()) {
            return MiningRollDecision.bypass(baseRolls);
        }

        ServerLevel level = context.getLevel();
        long gameTime = level != null ? level.getGameTime() : 0L;

        Player player = resolveMiningPlayer(context, state, gameTime);
        if (player == null || player.isCreative() || player.isSpectator()) {
            return MiningRollDecision.bypass(baseRolls);
        }

        ItemStack tool = resolveMiningTool(context, player, state, gameTime);
        if (!canToolHarvestOre(state, tool)) {
            return MiningRollDecision.bypass(baseRolls);
        }

        if (level != null && hasSilkTouch(level, tool)) {
            return MiningRollDecision.bypass(baseRolls);
        }

        MiningRollSnapshot snapshot = resolvePlayerSnapshot(player);
        if (!snapshot.isActive()) {
            return MiningRollDecision.bypass(baseRolls);
        }

        MiningRollMode mode = category.toRollMode(true);
        if (!mode.scalesRolls()) {
            return MiningRollDecision.bypass(baseRolls);
        }

        int resolvedRolls = POLICY.get().resolvePoolRolls(
                baseRolls,
                snapshot.effectiveMultiplier(),
                context.getRandom()
        );

        return new MiningRollDecision(
                baseRolls,
                resolvedRolls,
                snapshot.effectiveMultiplier(),
                category,
                mode
        );
    }

    private static Player resolveMiningPlayer(LootContext context, BlockState state, long gameTime) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof Player directPlayer) {
            return directPlayer;
        }
        MiningBreakContext fallback = ACTIVE_BREAK_CONTEXT.get();
        if (fallback != null && fallback.matches(state, gameTime)) {
            return fallback.player();
        }
        return null;
    }

    private static ItemStack resolveMiningTool(LootContext context, Player player, BlockState state, long gameTime) {
        ItemStack contextTool = context.getParamOrNull(LootContextParams.TOOL);
        if (contextTool != null && !contextTool.isEmpty()) {
            return contextTool;
        }
        MiningBreakContext fallback = ACTIVE_BREAK_CONTEXT.get();
        if (fallback != null && fallback.matches(state, gameTime) && !fallback.tool().isEmpty()) {
            return fallback.tool();
        }
        return player.getMainHandItem();
    }

    public static boolean canToolHarvestOre(BlockState state, ItemStack tool) {
        if (state == null) {
            return false;
        }
        if (!state.requiresCorrectToolForDrops()) {
            return true;
        }
        return tool != null && !tool.isEmpty() && tool.isCorrectToolForDrops(state);
    }

    public static boolean hasSilkTouch(LevelAccessor level, ItemStack tool) {
        if (level == null || tool == null || tool.isEmpty() || !tool.isEnchanted()) {
            return false;
        }
        Holder<Enchantment> silkTouchHolder = resolveSilkTouchHolder(level.registryAccess());
        if (silkTouchHolder == null) {
            return false;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder, tool) > 0;
    }

    private static Holder<Enchantment> resolveSilkTouchHolder(RegistryAccess registryAccess) {
        if (registryAccess == null) {
            return null;
        }
        SilkTouchCacheEntry cached = silkTouchCache;
        if (cached != null && cached.registryAccess() == registryAccess) {
            return cached.holder();
        }
        try {
            HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            Optional<Holder.Reference<Enchantment>> holderOpt = lookup.get(Enchantments.SILK_TOUCH);
            if (holderOpt.isEmpty()) {
                return null;
            }
            Holder<Enchantment> holder = holderOpt.get();
            silkTouchCache = new SilkTouchCacheEntry(registryAccess, holder);
            return holder;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static int extractOrangeShardCount(Player player) {
        if (player == null || !player.hasData(ModAttachments.CYBERWARE)) {
            return 0;
        }
        return extractOrangeShardCount(player.getData(ModAttachments.CYBERWARE));
    }

    private static int extractOrangeShardCount(PlayerCyberwareData data) {
        if (data == null) {
            return 0;
        }
        return data.getChipwareShardExactCount(ModItems.DATA_SHARD_ORANGE.get());
    }

    private static double readOreDropAttribute(Player player) {
        if (player == null) {
            return 1.0D;
        }
        AttributeInstance instance = player.getAttribute(ModAttributes.ORE_DROP_MULTIPLIER);
        if (instance == null) {
            return 1.0D;
        }
        return instance.getValue();
    }
}
