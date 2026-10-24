package com.perigrine3.createcybernetics.block;

import com.mojang.serialization.MapCodec;
import com.perigrine3.createcybernetics.block.entity.ComputerBlockEntity;
import com.perigrine3.createcybernetics.block.entity.ModBlockEntities;
import com.perigrine3.createcybernetics.block.entity.ServerTowerBlockEntity;
import com.perigrine3.createcybernetics.common.energy.ConditionalBlockPower;
import com.perigrine3.createcybernetics.common.multiblock.ServerTowerMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ComputerBlock extends BaseEntityBlock {
    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    public ComputerBlock(Properties properties) {
        super(properties);

        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(CONNECTED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockPos serverTowerPos = ServerTowerMultiblock.getComputerControllerPos(level, pos);

        boolean connected = serverTowerPos != null || hasComputerTowerBelow(level, pos);
        boolean powered = false;
        Direction facing = context.getHorizontalDirection().getOpposite();

        if (serverTowerPos != null) {
            BlockState towerState = level.getBlockState(serverTowerPos);

            if (towerState.is(ModBlocks.SERVER_TOWER.get())) {
                facing = towerState.getValue(ServerTowerBlock.FACING);
            }

            if (level.getBlockEntity(serverTowerPos) instanceof ServerTowerBlockEntity tower) {
                powered = tower.isOperational();
            }
        } else if (!ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) {
            powered = ConditionalBlockPower.hasRedstonePower(level, pos);
        }

        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, powered)
                .setValue(CONNECTED, connected);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide) return;
        if (oldState.is(state.getBlock())) return;

        updateConnectionState(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.isClientSide) return;

        if (neighborPos.equals(pos.below())) {
            updateConnectionState(level, pos);
        }
    }

    public static void updateConnectionState(Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);

        if (!currentState.is(ModBlocks.COMPUTER.get())) return;
        if (!currentState.hasProperty(CONNECTED)) return;
        if (!currentState.hasProperty(POWERED)) return;

        BlockPos serverTowerPos = ServerTowerMultiblock.getComputerControllerPos(level, pos);
        boolean connected = serverTowerPos != null || hasComputerTowerBelow(level, pos);
        boolean powered = currentState.getValue(POWERED);

        BlockState updatedState = currentState;

        if (updatedState.getValue(CONNECTED) != connected) {
            updatedState = updatedState.setValue(CONNECTED, connected);
        }

        if (serverTowerPos != null) {
            BlockState towerState = level.getBlockState(serverTowerPos);

            if (towerState.is(ModBlocks.SERVER_TOWER.get())) {
                Direction towerFacing = towerState.getValue(ServerTowerBlock.FACING);

                if (updatedState.getValue(FACING) != towerFacing) {
                    updatedState = updatedState.setValue(FACING, towerFacing);
                }
            }

            if (level.getBlockEntity(serverTowerPos) instanceof ServerTowerBlockEntity tower) {
                powered = tower.isOperational();
            } else {
                powered = false;
            }
        } else if (!ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) {
            powered = ConditionalBlockPower.hasRedstonePower(level, pos);
        }

        if (updatedState.getValue(POWERED) != powered) {
            updatedState = updatedState.setValue(POWERED, powered);
        }

        if (!updatedState.equals(currentState)) {
            level.setBlock(pos, updatedState, Block.UPDATE_ALL);
        }
    }

    private static boolean hasComputerTowerBelow(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModBlocks.COMPUTER_TOWER.get());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!state.getValue(POWERED)) {
            if (!level.isClientSide) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.createcybernetics.computer.unpowered"), true);
            }

            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof ComputerBlockEntity computer && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(computer, buffer -> buffer.writeBlockPos(pos));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, CONNECTED);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof ComputerBlockEntity computer) {
                computer.unregisterComputer();
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.COMPUTER.get(), ComputerBlockEntity::serverTick);
    }
}