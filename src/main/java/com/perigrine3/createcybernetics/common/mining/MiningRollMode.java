package com.perigrine3.createcybernetics.common.mining;

public enum MiningRollMode {
    BYPASS(false, false),
    STANDARD_ROLL_SCALING(true, false),
    SCRAP_CONVERSION_ROLL_SCALING(true, true);

    private final boolean scalesRolls;
    private final boolean convertsDebrisToScrap;

    MiningRollMode(boolean scalesRolls, boolean convertsDebrisToScrap) {
        this.scalesRolls = scalesRolls;
        this.convertsDebrisToScrap = convertsDebrisToScrap;
    }

    public boolean scalesRolls() {
        return scalesRolls;
    }

    public boolean convertsDebrisToScrap() {
        return convertsDebrisToScrap;
    }
}
