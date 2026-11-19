
package com.perigrine3.createcybernetics.common.energy;

import com.perigrine3.createcybernetics.ConfigValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class ConditionalBlockPower {
    private ConditionalBlockPower() {
    }

    public static boolean hasTechEnergyModLoaded() {
        return shouldUseEnergyInsteadOfRedstone();
    }

    public static boolean shouldUseEnergyInsteadOfRedstone() {
        return ConfigValues.USE_FORGE_ENERGY;
    }

    public static boolean hasRedstonePower(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos);
    }

    public static boolean hasRequiredPower(Level level, BlockPos pos, IEnergyStorage energyStorage, int requiredEnergy) {
        if (shouldUseEnergyInsteadOfRedstone()) {
            return energyStorage != null && energyStorage.getEnergyStored() >= requiredEnergy;
        }

        return hasRedstonePower(level, pos);
    }

    public static boolean consumeRequiredPower(Level level, BlockPos pos, IEnergyStorage energyStorage, int requiredEnergy) {
        if (shouldUseEnergyInsteadOfRedstone()) {
            return energyStorage != null && energyStorage.extractEnergy(requiredEnergy, false) == requiredEnergy;
        }

        return hasRedstonePower(level, pos);
    }
}
