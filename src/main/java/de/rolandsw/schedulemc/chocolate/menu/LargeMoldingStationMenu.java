package de.rolandsw.schedulemc.chocolate.menu;

import de.rolandsw.schedulemc.chocolate.blockentity.LargeMoldingStationBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class LargeMoldingStationMenu extends AbstractContainerMenu {
    public final LargeMoldingStationBlockEntity blockEntity;
    private final ContainerData data;

    public LargeMoldingStationMenu(int id, Inventory inv, LargeMoldingStationBlockEntity be) {
        super(ChocolateMenuTypes.LARGE_MOLDING_STATION_MENU.get(), id);
        this.blockEntity = be;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    public LargeMoldingStationMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(ChocolateMenuTypes.LARGE_MOLDING_STATION_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof LargeMoldingStationBlockEntity e ? e : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        if (blockEntity == null) return;
        var h = blockEntity.getItemHandler();
        // Chocolate input slot
        addSlot(new SlotItemHandler(h, 0, 44, 35));
        // Mold input slot
        addSlot(new SlotItemHandler(h, 1, 68, 35));
        // Output slot
        addSlot(new SlotItemHandler(h, 2, 116, 35));
        // Player inventory
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }

    public int getProgress() {
        return blockEntity == null ? 0 : blockEntity.getMoldingProgressValue();
    }

    public int getMaxProgress() {
        return blockEntity == null ? 1 : blockEntity.getTotalMoldingTime();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int i) {
        Slot s = slots.get(i);
        if (!s.hasItem()) return ItemStack.EMPTY;
        ItemStack st = s.getItem(), c = st.copy();
        int containerSlots = 3;
        if (i < containerSlots) {
            if (!moveItemStackTo(st, containerSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(st, 0, 2, false)) return ItemStack.EMPTY;
        }
        if (st.isEmpty()) s.set(ItemStack.EMPTY);
        else s.setChanged();
        return c;
    }

    @Override
    public boolean stillValid(@NotNull Player p) {
        return blockEntity != null && p.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5, blockEntity.getBlockPos().getZ() + 0.5) <= 64;
    }
}
