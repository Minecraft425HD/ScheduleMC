package de.rolandsw.schedulemc.cheese.menu;

import de.rolandsw.schedulemc.cheese.blockentity.SmallAgingCaveBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SmallAgingCaveMenu extends AbstractContainerMenu {
    public final SmallAgingCaveBlockEntity blockEntity;
    private final ContainerData data;

    public SmallAgingCaveMenu(int id, Inventory inv, SmallAgingCaveBlockEntity be) {
        super(CheeseMenuTypes.SMALL_AGING_CAVE_MENU.get(), id);
        this.blockEntity = be;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    public SmallAgingCaveMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(CheeseMenuTypes.SMALL_AGING_CAVE_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof SmallAgingCaveBlockEntity e ? e : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        if (blockEntity == null) return;
        var h = blockEntity.getItemHandler();
        addSlot(new SlotItemHandler(h, 0, 80, 35)); // Cheese wheel storage
        addSlot(new SlotItemHandler(h, 1, 116, 35)); // Output (not used, for future)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }

    public int getAgingTicks() {
        return blockEntity == null ? 0 : blockEntity.getAgingTicks();
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
