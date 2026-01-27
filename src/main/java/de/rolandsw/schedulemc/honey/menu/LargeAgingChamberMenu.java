package de.rolandsw.schedulemc.honey.menu;

import de.rolandsw.schedulemc.honey.blockentity.LargeAgingChamberBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class LargeAgingChamberMenu extends AbstractContainerMenu {
    public final LargeAgingChamberBlockEntity blockEntity;
    private final ContainerData data;

    public LargeAgingChamberMenu(int id, Inventory inv, LargeAgingChamberBlockEntity be) {
        super(HoneyMenuTypes.LARGE_AGING_CHAMBER_MENU.get(), id);
        this.blockEntity = be;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    public LargeAgingChamberMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(HoneyMenuTypes.LARGE_AGING_CHAMBER_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof LargeAgingChamberBlockEntity e ? e : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        if (blockEntity == null) return;
        var h = blockEntity.getItemHandler();
        // 16 slots in 4x4 grid
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                addSlot(new SlotItemHandler(h, j + i * 4, 35 + j * 18, 8 + i * 18));
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }

    public int getAgingTicks(int slot) {
        return blockEntity == null ? 0 : blockEntity.getAgingTicks(slot);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int i) {
        Slot s = slots.get(i);
        if (!s.hasItem()) return ItemStack.EMPTY;
        ItemStack st = s.getItem(), c = st.copy();
        if (i < 16) {
            if (!moveItemStackTo(st, 16, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(st, 0, 16, false)) return ItemStack.EMPTY;
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
