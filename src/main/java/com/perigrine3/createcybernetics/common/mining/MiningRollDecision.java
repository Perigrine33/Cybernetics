package com.perigrine3.createcybernetics.common.mining;

public record MiningRollDecision(
        int baseRolls,
        int resolvedRolls,
        double multiplier,
        OreBlockCategory category,
        MiningRollMode mode
) {
    public static MiningRollDecision bypass(int baseRolls) {
        return new MiningRollDecision(
                baseRolls,
                baseRolls,
                1.0D,
                OreBlockCategory.NON_ORE,
                MiningRollMode.BYPASS
        );
    }

    public boolean isScaled() {
        return mode.scalesRolls() && resolvedRolls != baseRolls;
    }
}
