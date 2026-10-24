package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public final class CyberwarePlayerMovementStateEvent extends Event {

    public record MovementSnapshot(
            boolean sprinting,
            boolean crouching,
            boolean swimming,
            boolean fallFlying,
            boolean onGround
    ) {
        public static MovementSnapshot capture(Player player) {
            return new MovementSnapshot(
                    player.isSprinting(),
                    player.isCrouching(),
                    player.isSwimming(),
                    player.isFallFlying(),
                    player.onGround()
            );
        }

        public int toBitmask() {
            int mask = 0;
            if (sprinting) mask |= 1;
            if (crouching) mask |= 2;
            if (swimming) mask |= 4;
            if (fallFlying) mask |= 8;
            if (onGround) mask |= 16;
            return mask;
        }
    }

    private final Player player;
    private final PlayerCyberwareData data;
    private final MovementSnapshot previousState;
    private final MovementSnapshot currentState;

    public CyberwarePlayerMovementStateEvent(
            Player player,
            PlayerCyberwareData data,
            MovementSnapshot previousState,
            MovementSnapshot currentState
    ) {
        this.player = player;
        this.data = data;
        this.previousState = previousState;
        this.currentState = currentState;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerCyberwareData getData() {
        return data;
    }

    public MovementSnapshot getPreviousState() {
        return previousState;
    }

    public MovementSnapshot getCurrentState() {
        return currentState;
    }
}
