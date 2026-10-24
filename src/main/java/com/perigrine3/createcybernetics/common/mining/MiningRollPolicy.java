package com.perigrine3.createcybernetics.common.mining;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface MiningRollPolicy {

    double computeEffectiveMultiplier(int shardCount, double attributeMultiplier);

    int resolvePoolRolls(int baseRolls, double effectiveMultiplier, RandomSource random);

    Consumer<ItemStack> adaptDropConsumer(Consumer<ItemStack> downstream, MiningRollMode mode);
}
