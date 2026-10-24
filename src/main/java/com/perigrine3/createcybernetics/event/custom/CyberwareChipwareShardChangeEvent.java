package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.List;

public final class CyberwareChipwareShardChangeEvent extends Event {

    private final Player player;
    private final PlayerCyberwareData data;
    private final List<ItemStack> previousShards;
    private final List<ItemStack> currentShards;

    public CyberwareChipwareShardChangeEvent(
            Player player,
            PlayerCyberwareData data,
            List<ItemStack> previousShards,
            List<ItemStack> currentShards
    ) {
        this.player = player;
        this.data = data;
        this.previousShards = previousShards;
        this.currentShards = currentShards;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerCyberwareData getData() {
        return data;
    }

    public List<ItemStack> getPreviousShards() {
        return previousShards;
    }

    public List<ItemStack> getCurrentShards() {
        return currentShards;
    }
}
