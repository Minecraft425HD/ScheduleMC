package de.rolandsw.schedulemc.beer.menu;

import de.rolandsw.schedulemc.beer.blockentity.LargeConditioningTankBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class LargeConditioningTankMenu extends AbstractContainerMenu {
    public final LargeConditioningTankBlockEntity blockEntity;
    private final ContainerData data;

    public LargeConditioningTankMenu(int id, Inventory inv, LargeConditioningTankBlockEntity be) {
        super(BeerMenuTypes.LARGE_CONDITIONING_TANK_MENU.get(), id);
        this.blockEntity = be;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    public LargeConditioningTankMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(BeerMenuTypes.LARGE_CONDITIONING_TANK_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof LargeConditioningTankBlockEntity e ? e : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        if (blockEntity == null) return;
        var h = blockEntity.getItemHandler();

        // 4x4 grid for 16 slots
        int startX = 53;
        int startY = 8;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                addSlot(new SlotItemHandler(h, index, startX + col * 18, startY + row * 18));
            }
        }

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    private void addPlayerInventory(Inventory inv) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inv) {
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
