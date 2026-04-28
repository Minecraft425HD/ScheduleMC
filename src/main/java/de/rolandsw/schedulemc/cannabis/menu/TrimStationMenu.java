package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.blockentity.TrimStationBlockEntity;
import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrimStationMenu extends AbstractContainerMenu {

    public final TrimStationBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer dummy = new SimpleContainer(3);

    private static final int DATA_CLICK_COUNT = 0;
    private static final int DATA_STRAIN      = 1;
    private static final int DATA_QUALITY     = 2;
    private static final int DATA_SIZE        = 3;

    public static final int BUTTON_TRIM   = 0;
    public static final int SLOT_INPUT    = 0;
    public static final int SLOT_BUD_OUT  = 1;
    public static final int SLOT_TRIM_OUT = 2;
    public static final int HOTBAR_START  = 3;

    // Server-side constructor
    public TrimStationMenu(int containerId, Inventory playerInventory, TrimStationBlockEntity blockEntity) {  // NOPMD
        super(CannabisMenuTypes.TRIM_STATION_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case DATA_CLICK_COUNT -> blockEntity.getClickCount();
                    case DATA_STRAIN      -> blockEntity.getLastStrain().ordinal();
                    case DATA_QUALITY     -> blockEntity.getLastQuality().ordinal();
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return DATA_SIZE; }
        };

        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    // Client-side constructor
    public TrimStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.TRIM_STATION_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof TrimStationBlockEntity station ? station : null;  // NOPMD
        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    private void addMachineSlots() {
        TrimStationBlockEntity be = blockEntity;
        // Slot 0: dried bud input (x=22, y=30)
        addSlot(new Slot(dummy, 0, 22, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getInputItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && be.hasInput(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
        // Slot 1: trimmed bud output (x=118, y=30)
        addSlot(new Slot(dummy, 1, 118, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getOutputTrimmedBud() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && !be.getOutputTrimmedBud().isEmpty(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
        // Slot 2: trim output (x=140, y=30)
        addSlot(new Slot(dummy, 2, 140, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getOutputTrim() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && !be.getOutputTrim().isEmpty(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_TRIM && blockEntity != null && !blockEntity.isRemoved()) {
            return blockEntity.doTrimClick(player);
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (blockEntity == null) return;
        if (type == ClickType.PICKUP) {
            ItemStack cursor = getCarried();
            if (slotId == SLOT_INPUT) {
                if (!cursor.isEmpty() && cursor.getItem() instanceof DriedBudItem) {
                    while (!cursor.isEmpty() && blockEntity.addDriedBud(cursor)) {
                        if (!player.isCreative()) cursor.shrink(1);
                    }
                    setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                } else if (cursor.isEmpty() && blockEntity.hasInput()) {
                    setCarried(blockEntity.extractInputItem());
                }
                return;
            }
            if (slotId == SLOT_BUD_OUT) {
                if (cursor.isEmpty() && !blockEntity.getOutputTrimmedBud().isEmpty()) {
                    setCarried(blockEntity.extractOutputTrimmedBud());
                }
                return;
            }
            if (slotId == SLOT_TRIM_OUT) {
                if (cursor.isEmpty() && !blockEntity.getOutputTrim().isEmpty()) {
                    setCarried(blockEntity.extractOutputTrim());
                }
                return;
            }
        }
        super.clicked(slotId, button, type, player);
    }

    // Getter
    public int getClickCount() { return this.data.get(DATA_CLICK_COUNT); }

    public CannabisStrain getLastStrain() {
        int ordinal = this.data.get(DATA_STRAIN);
        CannabisStrain[] values = CannabisStrain.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisStrain.HYBRID;
    }

    public CannabisQuality getLastQuality() {
        int ordinal = this.data.get(DATA_QUALITY);
        CannabisQuality[] values = CannabisQuality.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisQuality.GUT;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (blockEntity == null || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        if (index >= HOTBAR_START) {
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof DriedBudItem) {
                ItemStack original = stack.copy();
                boolean moved = false;
                while (!stack.isEmpty() && blockEntity.addDriedBud(stack)) {
                    if (!player.isCreative()) stack.shrink(1);
                    moved = true;
                }
                if (moved) {
                    slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                    return original;
                }
            }
        } else if (index == SLOT_BUD_OUT) {
            if (!blockEntity.getOutputTrimmedBud().isEmpty()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack extracted = blockEntity.extractOutputTrimmedBud();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        } else if (index == SLOT_TRIM_OUT) {
            if (!blockEntity.getOutputTrim().isEmpty()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack extracted = blockEntity.extractOutputTrim();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        } else if (index == SLOT_INPUT) {
            if (blockEntity.hasInput()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack extracted = blockEntity.extractInputItem();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
                player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    public static class Provider implements MenuProvider {
        private final TrimStationBlockEntity blockEntity;
        public Provider(TrimStationBlockEntity blockEntity) { this.blockEntity = blockEntity; }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.trimm_station.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new TrimStationMenu(containerId, playerInventory, blockEntity);
        }
    }
}
