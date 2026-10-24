package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public final class CyberwarePowerStateChangeEvent extends Event {

    private final Player player;
    private final PlayerCyberwareData data;
    private final CyberwareSlot slot;
    private final int index;
    private final InstalledCyberware cyberware;
    private final boolean powered;

    public CyberwarePowerStateChangeEvent(
            Player player,
            PlayerCyberwareData data,
            CyberwareSlot slot,
            int index,
            InstalledCyberware cyberware,
            boolean powered
    ) {
        this.player = player;
        this.data = data;
        this.slot = slot;
        this.index = index;
        this.cyberware = cyberware;
        this.powered = powered;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerCyberwareData getData() {
        return data;
    }

    public CyberwareSlot getSlot() {
        return slot;
    }

    public int getIndex() {
        return index;
    }

    public InstalledCyberware getCyberware() {
        return cyberware;
    }

    public boolean isPowered() {
        return powered;
    }
}
