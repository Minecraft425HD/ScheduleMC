package de.rolandsw.schedulemc.cheese.menu;

import de.rolandsw.schedulemc.cheese.blockentity.PackagingStationBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class PackagingStationMenu extends AbstractContainerMenu {
    public final PackagingStationBlockEntity blockEntity;
    private final ContainerData data;

    public PackagingStationMenu(int id, Inventory inv, PackagingStationBlockEntity be) {
        super(CheeseMenuTypes.PACKAGING_STATION_MENU.get(), id);
        this.blockEntity = be;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    public PackagingStationMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(CheeseMenuTypes.PACKAGING_STATION_MENU.get(), id);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        this.blockEntity = be instanceof PackagingStationBlockEntity e ? e : null;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        if (blockEntity == null) return;
        var h = blockEntity.getItemHandler();
        addSlot(new SlotItemHandler(h, 0, 38, 35)); // Cheese wheel input
        addSlot(new SlotItemHandler(h, 1, 62, 35)); // Packaging material input
        addSlot(new SlotItemHandler(h, 2, 116, 35)); // Output 1
        addSlot(new SlotItemHandler(h, 3, 134, 35)); // Output 2 (for future use)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }

    public int getProgress() {
        return blockEntity == null ? 0 : blockEntity.getPackagingProgress();
    }

    public int getMaxProgress() {
        return 300; // 15 seconds
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int i) {
        Slot s = slots.get(i);
        if (!s.hasItem()) return ItemStack.EMPTY;
        ItemStack st = s.getItem(), c = st.copy();
        if (i < 4) {
            if (!moveItemStackTo(st, 4, slots.size(), true)) return ItemStack.EMPTY;
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
