package com.perigrine3.createcybernetics.common.mining;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public enum StandardMiningRollPolicy implements MiningRollPolicy {
    INSTANCE;

    private static final double MIN_ACTIVE_MULTIPLIER = 1.0001D;
    private static final double MAX_MULTIPLIER = 64.0D;
    private static final double FRACTIONAL_EPSILON = 1.0E-6D;
    private static final int MAX_SHARD_EXPONENT = 6;

    @Override
    public double computeEffectiveMultiplier(int shardCount, double attributeMultiplier) {
        int clampedShards = Math.max(0, Math.min(shardCount, MAX_SHARD_EXPONENT));
        double shardMultiplier = clampedShards == 0 ? 1.0D : Math.scalb(1.0D, clampedShards);
        double sanitizedAttribute = Double.isFinite(attributeMultiplier)
                ? Mth.clamp(attributeMultiplier, 1.0D, MAX_MULTIPLIER)
                : 1.0D;
        return Math.min(MAX_MULTIPLIER, Math.max(shardMultiplier, sanitizedAttribute));
    }

    @Override
    public int resolvePoolRolls(int baseRolls, double effectiveMultiplier, RandomSource random) {
        if (baseRolls <= 0 || !Double.isFinite(effectiveMultiplier) || effectiveMultiplier <= MIN_ACTIVE_MULTIPLIER) {
            return baseRolls;
        }

        double clampedMultiplier = Math.min(effectiveMultiplier, MAX_MULTIPLIER);
        double scaledRolls = baseRolls * clampedMultiplier;
        int guaranteedRolls = Mth.floor(scaledRolls);
        double fractionalChance = scaledRolls - guaranteedRolls;

        if (fractionalChance > FRACTIONAL_EPSILON && random != null && random.nextDouble() < fractionalChance) {
            guaranteedRolls++;
        }

        return Math.max(baseRolls, guaranteedRolls);
    }

    @Override
    public Consumer<ItemStack> adaptDropConsumer(Consumer<ItemStack> downstream, MiningRollMode mode) {
        if (downstream == null || mode == null || !mode.convertsDebrisToScrap()) {
            return downstream;
        }
        return stack -> {
            if (stack == null || stack.isEmpty()) {
                return;
            }
            if (stack.is(Items.ANCIENT_DEBRIS)) {
                downstream.accept(new ItemStack(Items.NETHERITE_SCRAP, stack.getCount()));
                return;
            }
            downstream.accept(stack);
        };
    }
}
