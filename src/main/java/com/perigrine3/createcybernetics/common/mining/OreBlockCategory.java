package com.perigrine3.createcybernetics.common.mining;

public enum OreBlockCategory {
    NON_ORE(false, false),
    STANDARD_ORE(true, false),
    ANCIENT_DEBRIS(true, true);

    private final boolean eligibleOre;
    private final boolean requiresScrapConversion;

    OreBlockCategory(boolean eligibleOre, boolean requiresScrapConversion) {
        this.eligibleOre = eligibleOre;
        this.requiresScrapConversion = requiresScrapConversion;
    }

    public boolean isEligibleOre() {
        return eligibleOre;
    }

    public boolean requiresScrapConversion() {
        return requiresScrapConversion;
    }

    public MiningRollMode toRollMode(boolean active) {
        if (!active || !eligibleOre) {
            return MiningRollMode.BYPASS;
        }
        return requiresScrapConversion
                ? MiningRollMode.SCRAP_CONVERSION_ROLL_SCALING
                : MiningRollMode.STANDARD_ROLL_SCALING;
    }
}
