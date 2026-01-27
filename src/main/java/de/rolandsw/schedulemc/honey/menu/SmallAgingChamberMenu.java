package de.rolandsw.schedulemc.honey.menu;

import de.rolandsw.schedulemc.honey.blockentity.SmallAgingChamberBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SmallAgingChamberMenu extends AbstractContainerMenu {
    public final SmallAgingChamberBlockEntity blockEntity;
    private final ContainerData data;

    public SmallAgingChamberMenu(int id, Inventory inv, SmallAgingChamberBlockEntity be) {
        super(HoneyMenuTypes.SMALL_AGING_CHAMBER_MENU.get(), id);
        this.blockEntity = be;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    public SmallAgingChamberMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(HoneyMenuTypes.SMALL_AGING_CHAMBER_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof SmallAgingChamberBlockEntity e ? e : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        if (blockEntity == null) return;
        var h = blockEntity.getItemHandler();
        // 4 slots in 2x2 grid
        addSlot(new SlotItemHandler(h, 0, 62, 26));
        addSlot(new SlotItemHandler(h, 1, 80, 26));
        addSlot(new SlotItemHandler(h, 2, 62, 44));
        addSlot(new SlotItemHandler(h, 3, 80, 44));
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
        if (i < 4) {
            if (!moveItemStackTo(st, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(st, 0, 4, false)) return ItemStack.EMPTY;
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
