package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blockentity.HashPressBlockEntity;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
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

public class HashPressMenu extends AbstractContainerMenu {

    public final HashPressBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer dummy = new SimpleContainer(2);

    private static final int DATA_TRIM_WEIGHT    = 0;
    private static final int DATA_STRAIN         = 1;
    private static final int DATA_PRESS_PROGRESS = 2;
    private static final int DATA_IS_PRESSING    = 3;
    private static final int DATA_HAS_OUTPUT     = 4;
    private static final int DATA_QUALITY        = 5;
    private static final int DATA_SIZE           = 6;

    public static final int BUTTON_START = 0;
    public static final int SLOT_INPUT   = 0;
    public static final int SLOT_OUTPUT  = 1;
    public static final int HOTBAR_START = 2;

    // Server-side constructor
    public HashPressMenu(int containerId, Inventory playerInventory, HashPressBlockEntity blockEntity) {  // NOPMD
        super(CannabisMenuTypes.HASH_PRESS_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case DATA_TRIM_WEIGHT    -> blockEntity.getTrimWeight();
                    case DATA_STRAIN         -> blockEntity.getStrain().ordinal();
                    case DATA_PRESS_PROGRESS -> (int)(blockEntity.getPressProgress() * HashPressBlockEntity.PRESS_TICKS);
                    case DATA_IS_PRESSING    -> blockEntity.isPressing() ? 1 : 0;
                    case DATA_HAS_OUTPUT     -> blockEntity.hasOutput() ? 1 : 0;
                    case DATA_QUALITY        -> blockEntity.getTrimQuality().ordinal();
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
    public HashPressMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.HASH_PRESS_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof HashPressBlockEntity presse ? presse : null;  // NOPMD
        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    private void addMachineSlots() {
        HashPressBlockEntity be = blockEntity;
        // Slot 0: trim input
        addSlot(new Slot(dummy, 0, 22, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getInputDisplayItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && be.getTrimWeight() > 0; }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
        // Slot 1: hash output
        addSlot(new Slot(dummy, 1, 134, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getOutputItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && be.hasOutput(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_START && blockEntity != null && !blockEntity.isRemoved()) {
            return blockEntity.startPressing();
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (blockEntity == null) return;
        if (type == ClickType.PICKUP) {
            ItemStack cursor = getCarried();
            if (slotId == SLOT_INPUT) {
                if (!cursor.isEmpty() && cursor.getItem() instanceof TrimItem && !blockEntity.isPressing() && !blockEntity.hasOutput()) {
                    int consumed = blockEntity.addTrim(cursor);
                    if (consumed > 0) {
                        if (!player.isCreative()) cursor.shrink(consumed);
                        setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                    }
                } else if (cursor.isEmpty() && blockEntity.getTrimWeight() > 0 && !blockEntity.isPressing()) {
                    setCarried(blockEntity.getInputDisplayItem().copy());
                    blockEntity.clearTrim();
                }
                return;
            }
            if (slotId == SLOT_OUTPUT) {
                if (cursor.isEmpty() && blockEntity.hasOutput()) {
                    setCarried(blockEntity.extractHash());
                }
                return;
            }
        }
        super.clicked(slotId, button, type, player);
    }

    // Getter
    public int getTrimWeight()       { return this.data.get(DATA_TRIM_WEIGHT); }
    public boolean isPressing()      { return this.data.get(DATA_IS_PRESSING) == 1; }
    public boolean hasOutput()       { return this.data.get(DATA_HAS_OUTPUT) == 1; }
    public int getPressProgress()    { return this.data.get(DATA_PRESS_PROGRESS); }
    public float getPressProgressF() { return (float)getPressProgress() / HashPressBlockEntity.PRESS_TICKS; }
    public boolean canStart()        { return getTrimWeight() >= HashPressBlockEntity.MIN_TRIM_WEIGHT && !isPressing() && !hasOutput(); }
    public int getExpectedHashWeight() { return (int)(getTrimWeight() * HashPressBlockEntity.CONVERSION_RATE); }

    public CannabisStrain getStrain() {
        int ordinal = this.data.get(DATA_STRAIN);
        CannabisStrain[] values = CannabisStrain.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisStrain.HYBRID;
    }

    public CannabisQuality getBaseQuality() {
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
            if (stack.getItem() instanceof TrimItem && !blockEntity.isPressing() && !blockEntity.hasOutput()) {
                ItemStack original = stack.copy();
                int consumed = blockEntity.addTrim(stack);
                if (consumed > 0) {
                    if (!player.isCreative()) stack.shrink(consumed);
                    slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                    return original;
                }
            }
        } else if (index == SLOT_OUTPUT) {
            if (blockEntity.hasOutput()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack extracted = blockEntity.extractHash();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        } else if (index == SLOT_INPUT) {
            if (blockEntity.getTrimWeight() > 0 && !blockEntity.isPressing()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack display = blockEntity.getInputDisplayItem().copy();
                blockEntity.clearTrim();
                moveItemStackTo(display, HOTBAR_START, HOTBAR_START + 9, false);
                return display;
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
        private final HashPressBlockEntity blockEntity;
        public Provider(HashPressBlockEntity blockEntity) { this.blockEntity = blockEntity; }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.hash_presse.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new HashPressMenu(containerId, playerInventory, blockEntity);
        }
    }
}
