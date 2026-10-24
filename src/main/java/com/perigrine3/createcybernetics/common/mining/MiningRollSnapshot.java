package com.perigrine3.createcybernetics.common.mining;

import java.util.UUID;

public record MiningRollSnapshot(
        UUID playerId,
        int activeShardCount,
        double effectiveMultiplier,
        long gameTime
) {
    public static MiningRollSnapshot inactive(UUID playerId, long gameTime) {
        return new MiningRollSnapshot(playerId, 0, 1.0D, gameTime);
    }

    public boolean isActive() {
        return activeShardCount > 0 || effectiveMultiplier > 1.0001D;
    }

    public boolean isFreshForTick(long currentGameTime) {
        return this.gameTime == currentGameTime;
    }
}
