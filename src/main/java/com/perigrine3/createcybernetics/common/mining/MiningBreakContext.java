package com.perigrine3.createcybernetics.common.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public record MiningBreakContext(
        Player player,
        BlockPos pos,
        BlockState state,
        ItemStack tool,
        long gameTime
) {
    public boolean matches(BlockState targetState, long currentGameTime) {
        if (player == null || state == null || targetState == null) {
            return false;
        }
        return this.gameTime == currentGameTime && this.state.is(targetState.getBlock());
    }
}
