package com.perigrine3.createcybernetics.screen.custom.computer;

import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.block.entity.ComputerTowerBlockEntity;
import com.perigrine3.createcybernetics.screen.ModMenuTypes;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ComputerTowerMenu extends AbstractContainerMenu {
    private static final int TOWER_SLOT_COUNT = 24;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 36;

    private static final int TOWER_COLUMNS = 4;
    private static final int TOWER_ROWS = 6;

    private static final int[] TOWER_SLOT_X = {
            33,
            64,
            95,
            126
    };

    private static final int[] TOWER_SLOT_Y = {
            10,
            28,
            46,
            70,
            88,
            106
    };

    private static final int PLAYER_INVENTORY_START_X = 8;
    private static final int PLAYER_INVENTORY_START_Y = 140;
    private static final int PLAYER_HOTBAR_Y = 198;

    private final ComputerTowerBlockEntity blockEntity;

    public ComputerTowerMenu(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                getBlockEntity(
                        playerInventory,
                        buffer
                )
        );
    }

    public ComputerTowerMenu(
            int containerId,
            Inventory playerInventory,
            ComputerTowerBlockEntity blockEntity
    ) {
        super(
                ModMenuTypes.COMPUTER_TOWER_MENU.get(),
                containerId
        );

        this.blockEntity = blockEntity;

        blockEntity.forceGenerateLootFromBlockNbt(
                playerInventory.player
        );

        addTowerSlots(
                playerInventory.player.level().isClientSide
                        ? new SimpleContainer(TOWER_SLOT_COUNT)
                        : blockEntity
        );

        addPlayerInventory(
                playerInventory
        );

        addPlayerHotbar(
                playerInventory
        );
    }

    private static ComputerTowerBlockEntity getBlockEntity(
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        BlockEntity blockEntity = playerInventory.player.level()
                .getBlockEntity(buffer.readBlockPos());

        if (blockEntity instanceof ComputerTowerBlockEntity computerTower) {
            return computerTower;
        }

        throw new IllegalStateException(
                "Computer tower block entity was missing"
        );
    }

    private void addTowerSlots(Container container) {
        for (int row = 0; row < TOWER_ROWS; row++) {
            for (int column = 0; column < TOWER_COLUMNS; column++) {
                int slotIndex =
                        column + row * TOWER_COLUMNS;

                addSlot(
                        new Slot(
                                container,
                                slotIndex,
                                TOWER_SLOT_X[column],
                                TOWER_SLOT_Y[row]
                        ) {
                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return stack.is(ModTags.Items.DATA_SHARDS);
                            }

                            @Override
                            public int getMaxStackSize() {
                                return 1;
                            }

                            @Override
                            public int getMaxStackSize(ItemStack stack) {
                                return 1;
                            }
                        }
                );
            }
        }
    }

    private void addPlayerInventory(
            Inventory playerInventory
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(
                        new Slot(
                                playerInventory,
                                column + row * 9 + 9,
                                PLAYER_INVENTORY_START_X + column * 18,
                                PLAYER_INVENTORY_START_Y + row * 18
                        )
                );
            }
        }
    }

    private void addPlayerHotbar(
            Inventory playerInventory
    ) {
        for (int column = 0; column < 9; column++) {
            addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            PLAYER_INVENTORY_START_X + column * 18,
                            PLAYER_HOTBAR_Y
                    )
            );
        }
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = slots.get(index);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack originalStack = sourceStack.copy();

        if (index < TOWER_SLOT_COUNT) {
            if (!moveItemStackTo(
                    sourceStack,
                    TOWER_SLOT_COUNT,
                    TOWER_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!sourceStack.is(ModTags.Items.DATA_SHARDS)) {
                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(
                    sourceStack,
                    0,
                    TOWER_SLOT_COUNT,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(
                player,
                sourceStack
        );

        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity.getLevel() == null) {
            return false;
        }

        return stillValid(
                ContainerLevelAccess.create(
                        blockEntity.getLevel(),
                        blockEntity.getBlockPos()
                ),
                player,
                ModBlocks.COMPUTER_TOWER.get()
        );
    }

    public ComputerTowerBlockEntity getBlockEntity() {
        return blockEntity;
    }
}