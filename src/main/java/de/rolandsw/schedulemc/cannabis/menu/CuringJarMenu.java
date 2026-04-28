package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blockentity.CuringJarBlockEntity;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
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

public class CuringJarMenu extends AbstractContainerMenu {

    public final CuringJarBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer dummy = new SimpleContainer(2);

    private static final int DATA_CURING_PROGRESS = 0; // 0–1000
    private static final int DATA_STRAIN          = 1;
    private static final int DATA_QUALITY         = 2;
    private static final int DATA_WEIGHT          = 3;
    private static final int DATA_HAS_CONTENT     = 4;
    private static final int DATA_HAS_OUTPUT      = 5;
    private static final int DATA_SIZE            = 6;

    public static final int SLOT_INPUT  = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int HOTBAR_START = 2;

    // Server-side constructor
    public CuringJarMenu(int containerId, Inventory playerInventory, CuringJarBlockEntity blockEntity) {  // NOPMD
        super(CannabisMenuTypes.CURING_JAR_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case DATA_CURING_PROGRESS -> blockEntity.getCuringProgressScaled();
                    case DATA_STRAIN      -> blockEntity.getStrain().ordinal();
                    case DATA_QUALITY     -> blockEntity.getBaseQuality().ordinal();
                    case DATA_WEIGHT      -> blockEntity.getWeight();
                    case DATA_HAS_CONTENT -> blockEntity.hasContent() ? 1 : 0;
                    case DATA_HAS_OUTPUT  -> blockEntity.hasOutput()  ? 1 : 0;
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
    public CuringJarMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.CURING_JAR_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof CuringJarBlockEntity glas ? glas : null;  // NOPMD
        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    private void addMachineSlots() {
        CuringJarBlockEntity be = blockEntity;
        // Slot 0: input (TrimmedBud)
        addSlot(new Slot(dummy, 0, 22, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getStoredItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && be.hasContent(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
        // Slot 1: output (CuredBud nach Ablauf des Timers)
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
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (blockEntity == null) return;
        if (type == ClickType.PICKUP) {
            ItemStack cursor = getCarried();
            if (slotId == SLOT_INPUT) {
                if (!cursor.isEmpty() && cursor.getItem() instanceof TrimmedBudItem) {
                    while (!cursor.isEmpty() && blockEntity.addTrimmedBud(cursor)) {
                        if (!player.isCreative()) cursor.shrink(1);
                    }
                    setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                } else if (cursor.isEmpty() && blockEntity.hasContent()) {
                    setCarried(blockEntity.getStoredItem().copy());
                    blockEntity.clearStoredItem();
                }
                return;
            }
            if (slotId == SLOT_OUTPUT) {
                if (cursor.isEmpty() && blockEntity.hasOutput()) {
                    setCarried(blockEntity.extractCuredBud());
                }
                return;
            }
        }
        super.clicked(slotId, button, type, player);
    }

    // Getter
    public float getCuringProgress() { return this.data.get(DATA_CURING_PROGRESS) / 1000.0f; }
    public boolean hasContent()      { return this.data.get(DATA_HAS_CONTENT) == 1; }
    public boolean hasOutput()       { return this.data.get(DATA_HAS_OUTPUT)  == 1; }
    public int getWeight()           { return this.data.get(DATA_WEIGHT); }

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

    public CannabisQuality getExpectedQuality() {
        return getBaseQuality().upgrade();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (blockEntity == null || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        if (index >= HOTBAR_START) {
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof TrimmedBudItem) {
                ItemStack original = stack.copy();
                boolean moved = false;
                while (!stack.isEmpty() && blockEntity.addTrimmedBud(stack)) {
                    if (!player.isCreative()) stack.shrink(1);
                    moved = true;
                }
                if (moved) {
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
                ItemStack extracted = blockEntity.extractCuredBud();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        } else if (index == SLOT_INPUT) {
            if (blockEntity.hasContent() && !blockEntity.hasOutput()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack stored = blockEntity.getStoredItem().copy();
                blockEntity.clearStoredItem();
                moveItemStackTo(stored, HOTBAR_START, HOTBAR_START + 9, false);
                return stored;
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
        private final CuringJarBlockEntity blockEntity;
        public Provider(CuringJarBlockEntity blockEntity) { this.blockEntity = blockEntity; }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.curing_glas.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new CuringJarMenu(containerId, playerInventory, blockEntity);
        }
    }
}
