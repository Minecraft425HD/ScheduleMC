package de.rolandsw.schedulemc.wine.menu;

import de.rolandsw.schedulemc.wine.blockentity.SmallWinePressBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SmallWinePressMenu extends AbstractContainerMenu {
    public final SmallWinePressBlockEntity blockEntity;
    private final ContainerData data;

    public SmallWinePressMenu(int id, Inventory inv, SmallWinePressBlockEntity be) {
        super(WineMenuTypes.SMALL_WINE_PRESS_MENU.get(), id);
        this.blockEntity = be;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    public SmallWinePressMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(WineMenuTypes.SMALL_WINE_PRESS_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof SmallWinePressBlockEntity e ? e : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        if (blockEntity == null) return;
        var h = blockEntity.getItemHandler();
        addSlot(new SlotItemHandler(h, 0, 56, 35));
        addSlot(new SlotItemHandler(h, 1, 116, 35));
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 84));
    }

    public int getProgress() {
        return blockEntity == null ? 0 : blockEntity.getCrushingProgress();
    }

    public int getMaxProgress() {
        return blockEntity == null ? 1 : blockEntity.getTotalPressingTime();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int i) {
        Slot s = slots.get(i);
        if (!s.hasItem()) return ItemStack.EMPTY;
        ItemStack st = s.getItem(), c = st.copy();
        if (i < 2) {
            if (!moveItemStackTo(st, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(st, 0, 1, false)) return ItemStack.EMPTY;
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
