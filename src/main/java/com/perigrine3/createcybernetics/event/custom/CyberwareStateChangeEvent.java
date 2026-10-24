package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public final class CyberwareStateChangeEvent extends Event {

    public enum ChangeReason {
        LOADOUT_MUTATED,
        TOGGLE_MUTATED,
        DURABILITY_THRESHOLD_CROSSED,
        LIFECYCLE_INITIALIZED
    }

    private final Player player;
    private final PlayerCyberwareData data;
    private final CyberwareLoadoutProfile previousProfile;
    private final CyberwareLoadoutProfile currentProfile;
    private final ChangeReason reason;

    public CyberwareStateChangeEvent(
            Player player,
            PlayerCyberwareData data,
            CyberwareLoadoutProfile previousProfile,
            CyberwareLoadoutProfile currentProfile,
            ChangeReason reason
    ) {
        this.player = player;
        this.data = data;
        this.previousProfile = previousProfile;
        this.currentProfile = currentProfile;
        this.reason = reason;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerCyberwareData getData() {
        return data;
    }

    public CyberwareLoadoutProfile getPreviousProfile() {
        return previousProfile;
    }

    public CyberwareLoadoutProfile getCurrentProfile() {
        return currentProfile;
    }

    public ChangeReason getReason() {
        return reason;
    }
}
