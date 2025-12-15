package de.rolandsw.schedulemc.tobacco.menu;

import de.rolandsw.schedulemc.tobacco.blockentity.SmallDryingRackBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Container-Menu für Small Drying Rack (6 Blätter Kapazität)
 */
public class SmallDryingRackMenu extends AbstractContainerMenu {

    public final SmallDryingRackBlockEntity blockEntity;
    private final ContainerData data;

    // Server-side constructor
    public SmallDryingRackMenu(int containerId, Inventory playerInventory, SmallDryingRackBlockEntity blockEntity) {
        super(ModMenuTypes.SMALL_DRYING_RACK_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new SimpleContainerData(2);
        addDataSlots(this.data);

        addSlots(playerInventory);
    }

    // Client-side constructor
    public SmallDryingRackMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.SMALL_DRYING_RACK_MENU.get(), containerId);

        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof SmallDryingRackBlockEntity dryingRack) {
            this.blockEntity = dryingRack;
        } else {
            this.blockEntity = null;
        }

        this.data = new SimpleContainerData(2);
        addDataSlots(this.data);

        addSlots(playerInventory);
    }

    private void addSlots(Inventory playerInventory) {
        if (blockEntity == null) return;

        var handler = blockEntity.getItemHandler();

        // Input Slot (Slot 0) - Links
        this.addSlot(new SlotItemHandler(handler, 0, 56, 35));

        // Output Slot (Slot 1) - Rechts
        this.addSlot(new SlotItemHandler(handler, 1, 116, 35));

        // Player Hotbar (nur Schnellzugriffsleiste)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 84));
        }
    }

    public int getProgress() {
        if (blockEntity == null) return 0;
        return blockEntity.getDryingProgressValue();
    }

    public int getMaxProgress() {
        if (blockEntity == null) return 1;
        return blockEntity.getTotalDryingTime();
    }

    public int getCapacity() {
        return 6;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        // Von BlockEntity zu Player
        if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        }
        // Von Player zu BlockEntity Input
        else {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
               player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                                   blockEntity.getBlockPos().getY() + 0.5,
                                   blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
