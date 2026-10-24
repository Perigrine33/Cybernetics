package com.perigrine3.createcybernetics.block.entity;

import com.perigrine3.createcybernetics.block.ComputerBlock;
import com.perigrine3.createcybernetics.common.computer.ChatSpaceSavedData;
import com.perigrine3.createcybernetics.common.energy.ConditionalBlockPower;
import com.perigrine3.createcybernetics.common.energy.ExternalEnergyInputTracker;
import com.perigrine3.createcybernetics.common.energy.SyncingEnergyStorage;
import com.perigrine3.createcybernetics.common.multiblock.ServerTowerMultiblock;
import com.perigrine3.createcybernetics.screen.custom.computer.ComputerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class ComputerBlockEntity extends BlockEntity implements ExternalEnergyInputTracker, MenuProvider {
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_COMPUTER_CODE = "ComputerCode";

    public static final int ENERGY_CAPACITY = 100_000;
    public static final int MAX_RECEIVE = 2_000;
    public static final int ENERGY_USED_PER_TICK = 20;

    private final SyncingEnergyStorage energyStorage = new SyncingEnergyStorage(this, ENERGY_CAPACITY, MAX_RECEIVE, 0);

    private int externalEnergyInputTicks;
    private String computerCode = "";

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public SyncingEnergyStorage getMutableEnergyStorage() {
        return energyStorage;
    }

    public String getComputerCode() {
        return computerCode;
    }

    public void ensureComputerRegistration() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ChatSpaceSavedData savedData = ChatSpaceSavedData.get(serverLevel.getServer());
        String registeredCode = savedData.registerComputer(serverLevel, worldPosition, computerCode);

        if (registeredCode.equals(computerCode)) return;

        computerCode = registeredCode;

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void unregisterComputer() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (computerCode.isBlank()) return;

        ChatSpaceSavedData.get(serverLevel.getServer()).unregisterComputer(serverLevel.dimension(), worldPosition, computerCode);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel) {
            ensureComputerRegistration();
            ComputerBlock.updateConnectionState(level, worldPosition);
        }
    }

    @Override
    public void markExternalEnergyInput() {
        externalEnergyInputTicks = 2;
    }

    public boolean isReceivingExternalEnergy() {
        return externalEnergyInputTicks > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ComputerBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.ensureComputerRegistration();
        ComputerBlock.updateConnectionState(level, pos);

        if (blockEntity.externalEnergyInputTicks > 0) {
            blockEntity.externalEnergyInputTicks--;
        }

        blockEntity.updatePowerState();
    }

    public void updatePowerState() {
        if (level == null || level.isClientSide) return;

        BlockState currentState = level.getBlockState(worldPosition);

        if (!currentState.is(getBlockState().getBlock())) return;
        if (!currentState.hasProperty(ComputerBlock.POWERED)) return;

        boolean powered;

        BlockPos serverTowerPos = ServerTowerMultiblock.getComputerControllerPos(level, worldPosition);

        if (serverTowerPos != null) {
            BlockEntity blockEntity = level.getBlockEntity(serverTowerPos);
            powered = blockEntity instanceof ServerTowerBlockEntity tower && tower.isOperational();
        } else if (ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) {
            powered = energyStorage.consumeEnergyInternally(ENERGY_USED_PER_TICK);
        } else {
            powered = ConditionalBlockPower.hasRedstonePower(level, worldPosition);
        }

        if (currentState.getValue(ComputerBlock.POWERED) != powered) {
            level.setBlock(worldPosition, currentState.setValue(ComputerBlock.POWERED, powered), Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.createcybernetics.computer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComputerMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.putInt(TAG_ENERGY, energyStorage.getEnergyStored());
        tag.putString(TAG_COMPUTER_CODE, computerCode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains(TAG_ENERGY)) {
            energyStorage.setEnergyStoredSilently(tag.getInt(TAG_ENERGY));
        }

        if (tag.contains(TAG_COMPUTER_CODE)) {
            computerCode = tag.getString(TAG_COMPUTER_CODE);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }
}