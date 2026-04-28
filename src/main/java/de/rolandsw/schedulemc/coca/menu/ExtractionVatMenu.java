package de.rolandsw.schedulemc.coca.menu;

import de.rolandsw.schedulemc.coca.blockentity.AbstractExtractionVatBlockEntity;
import de.rolandsw.schedulemc.coca.items.FreshCocaLeafItem;
import de.rolandsw.schedulemc.vehicle.items.ItemDieselCanister;
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

public class ExtractionVatMenu extends AbstractContainerMenu {

    public final AbstractExtractionVatBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer dummy = new SimpleContainer(3);

    private static final int DATA_DIESEL_LEVEL    = 0;
    private static final int DATA_DIESEL_MAX      = 1;
    private static final int DATA_INPUT_COUNT     = 2;
    private static final int DATA_CAPACITY        = 3;
    private static final int DATA_OUTPUT_COUNT    = 4;
    private static final int DATA_PROGRESS_SCALED = 5; // 0-1000
    private static final int DATA_SIZE            = 6;

    public static final int SLOT_DIESEL   = 0;
    public static final int SLOT_INPUT    = 1;
    public static final int SLOT_OUTPUT   = 2;
    public static final int HOTBAR_START  = 3;

    // Server-side constructor
    public ExtractionVatMenu(int containerId, Inventory playerInventory, AbstractExtractionVatBlockEntity blockEntity) {  // NOPMD
        super(CocaMenuTypes.EXTRACTION_VAT_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case DATA_DIESEL_LEVEL    -> blockEntity.getDieselLevel();
                    case DATA_DIESEL_MAX      -> blockEntity.getMaxDiesel();
                    case DATA_INPUT_COUNT     -> blockEntity.getInputCount();
                    case DATA_CAPACITY        -> blockEntity.getCapacity();
                    case DATA_OUTPUT_COUNT    -> blockEntity.getOutputCount();
                    case DATA_PROGRESS_SCALED -> (int)(blockEntity.getAverageExtractionPercentage() * 1000);
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
    public ExtractionVatMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CocaMenuTypes.EXTRACTION_VAT_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof AbstractExtractionVatBlockEntity vatBE ? vatBE : null;  // NOPMD
        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    private void addMachineSlots() {
        AbstractExtractionVatBlockEntity be = blockEntity;
        // Slot 0: diesel action slot (display-only, interaction via clicked())
        addSlot(new Slot(dummy, 0, 22, 30) {
            @Override public @NotNull ItemStack getItem() { return ItemStack.EMPTY; }
            @Override public boolean hasItem()            { return false; }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
        });
        // Slot 1: input display (leaves)
        addSlot(new Slot(dummy, 1, 74, 30) {
            @Override public @NotNull ItemStack getItem() { return be != null ? be.getInputDisplayItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()            { return be != null && be.hasInput(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
        });
        // Slot 2: output display (paste)
        addSlot(new Slot(dummy, 2, 124, 30) {
            @Override public @NotNull ItemStack getItem() { return be != null ? be.getOutputDisplayItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()            { return be != null && be.hasOutput(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
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
            if (slotId == SLOT_DIESEL) {
                if (!cursor.isEmpty() && cursor.getItem() instanceof ItemDieselCanister) {
                    int inCanister = ItemDieselCanister.getFuel(cursor);
                    int space = blockEntity.getMaxDiesel() - blockEntity.getDieselLevel();
                    if (inCanister > 0 && space > 0) {
                        int toAdd = Math.min(inCanister, space);
                        blockEntity.addDiesel(toAdd);
                        if (!player.isCreative()) ItemDieselCanister.consumeFuel(cursor, toAdd);
                        setCarried(cursor);
                    }
                }
                return;
            }
            if (slotId == SLOT_INPUT) {
                if (!cursor.isEmpty() && cursor.getItem() instanceof FreshCocaLeafItem && !blockEntity.isFull()) {
                    while (!cursor.isEmpty() && !blockEntity.isFull()) {
                        if (blockEntity.addFreshLeaves(cursor)) {
                            if (!player.isCreative()) cursor.shrink(1);
                        } else {
                            break;
                        }
                    }
                    setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                }
                // cursor empty + hasInput: leaves can't be taken back
                return;
            }
            if (slotId == SLOT_OUTPUT) {
                if (cursor.isEmpty() && blockEntity.hasOutput()) {
                    setCarried(blockEntity.extractAllPaste());
                }
                return;
            }
        }
        super.clicked(slotId, button, type, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (blockEntity == null || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        if (index >= HOTBAR_START) {
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof FreshCocaLeafItem && !blockEntity.isFull()) {
                while (!stack.isEmpty() && !blockEntity.isFull()) {
                    if (blockEntity.addFreshLeaves(stack)) {
                        if (!player.isCreative()) stack.shrink(1);
                    } else {
                        break;
                    }
                }
                slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                return ItemStack.EMPTY;
            }
            if (stack.getItem() instanceof ItemDieselCanister) {
                int inCanister = ItemDieselCanister.getFuel(stack);
                int space = blockEntity.getMaxDiesel() - blockEntity.getDieselLevel();
                if (inCanister > 0 && space > 0) {
                    int toAdd = Math.min(inCanister, space);
                    blockEntity.addDiesel(toAdd);
                    if (!player.isCreative()) ItemDieselCanister.consumeFuel(stack, toAdd);
                    slot.set(stack);
                    return ItemStack.EMPTY;
                }
            }
        } else if (index == SLOT_OUTPUT) {
            if (blockEntity.hasOutput()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack extracted = blockEntity.extractAllPaste();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        }
        // SLOT_INPUT and SLOT_DIESEL: no extraction
        return ItemStack.EMPTY;
    }

    // Getters for screen
    public int getDieselLevel()       { return this.data.get(DATA_DIESEL_LEVEL); }
    public int getDieselMax()         { return this.data.get(DATA_DIESEL_MAX); }
    public int getInputCount()        { return this.data.get(DATA_INPUT_COUNT); }
    public int getCapacity()          { return this.data.get(DATA_CAPACITY); }
    public int getOutputCount()       { return this.data.get(DATA_OUTPUT_COUNT); }
    public float getProgressScaled()  { return this.data.get(DATA_PROGRESS_SCALED) / 1000.0f; }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
                player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    public static class Provider implements MenuProvider {
        private final AbstractExtractionVatBlockEntity blockEntity;
        public Provider(AbstractExtractionVatBlockEntity blockEntity) { this.blockEntity = blockEntity; }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.extraction_vat.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new ExtractionVatMenu(containerId, playerInventory, blockEntity);
        }
    }
}
