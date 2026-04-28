package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blockentity.OilExtractorBlockEntity;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
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

public class OilExtractorMenu extends AbstractContainerMenu {

    public final OilExtractorBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer dummy = new SimpleContainer(3);

    private static final int DATA_MATERIAL_WEIGHT     = 0;
    private static final int DATA_IS_FROM_BUDS        = 1;
    private static final int DATA_STRAIN              = 2;
    private static final int DATA_QUALITY             = 3;
    private static final int DATA_SOLVENT_COUNT       = 4;
    private static final int DATA_EXTRACTION_PROGRESS = 5;
    private static final int DATA_IS_EXTRACTING       = 6;
    private static final int DATA_HAS_OUTPUT          = 7;
    private static final int DATA_SIZE                = 8;

    public static final int BUTTON_START  = 0;
    public static final int SLOT_MATERIAL = 0;
    public static final int SLOT_SOLVENT  = 1;
    public static final int SLOT_OUTPUT   = 2;
    public static final int HOTBAR_START  = 3;

    // Server-side constructor
    public OilExtractorMenu(int containerId, Inventory playerInventory, OilExtractorBlockEntity blockEntity) {  // NOPMD
        super(CannabisMenuTypes.OIL_EXTRACTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case DATA_MATERIAL_WEIGHT     -> blockEntity.getMaterialWeight();
                    case DATA_IS_FROM_BUDS        -> blockEntity.isFromBuds() ? 1 : 0;
                    case DATA_STRAIN              -> blockEntity.getStrain().ordinal();
                    case DATA_QUALITY             -> blockEntity.getBaseQuality().ordinal();
                    case DATA_SOLVENT_COUNT       -> blockEntity.getSolventCount();
                    case DATA_EXTRACTION_PROGRESS -> (int)(blockEntity.getExtractionProgress() * OilExtractorBlockEntity.EXTRACTION_TICKS);
                    case DATA_IS_EXTRACTING       -> blockEntity.isExtracting() ? 1 : 0;
                    case DATA_HAS_OUTPUT          -> blockEntity.hasOutput() ? 1 : 0;
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
    public OilExtractorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.OIL_EXTRACTOR_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof OilExtractorBlockEntity extractor ? extractor : null;  // NOPMD
        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    private void addMachineSlots() {
        OilExtractorBlockEntity be = blockEntity;
        // Slot 0: material input (x=14, y=30)
        addSlot(new Slot(dummy, 0, 14, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getInputDisplayItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && be.getMaterialWeight() > 0; }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
        // Slot 1: solvent input (x=36, y=30)
        addSlot(new Slot(dummy, 1, 36, 30) {
            @Override public ItemStack getItem() { return be != null ? be.getSolventDisplayItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()   { return be != null && be.getSolventCount() > 0; }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count) { return ItemStack.EMPTY; }
        });
        // Slot 2: oil output (x=128, y=30)
        addSlot(new Slot(dummy, 2, 128, 30) {
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
            return blockEntity.startExtraction();
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (blockEntity == null) return;
        if (type == ClickType.PICKUP) {
            ItemStack cursor = getCarried();
            if (slotId == SLOT_MATERIAL) {
                if (!cursor.isEmpty() && !blockEntity.isExtracting() && !blockEntity.hasOutput()
                        && (cursor.getItem() instanceof TrimmedBudItem || cursor.getItem() instanceof TrimItem)) {
                    if (blockEntity.addMaterial(cursor)) {
                        if (!player.isCreative()) cursor.shrink(1);
                        setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                    }
                } else if (cursor.isEmpty() && blockEntity.getMaterialWeight() > 0 && !blockEntity.isExtracting()) {
                    setCarried(blockEntity.getInputDisplayItem().copy());
                    blockEntity.clearMaterial();
                }
                return;
            }
            if (slotId == SLOT_SOLVENT) {
                if (!cursor.isEmpty() && cursor.getItem() == CannabisItems.EXTRACTION_SOLVENT.get()
                        && !blockEntity.isExtracting() && !blockEntity.hasOutput()) {
                    if (blockEntity.addSolvent(cursor)) {
                        if (!player.isCreative()) cursor.setCount(0);
                        setCarried(ItemStack.EMPTY);
                    }
                } else if (cursor.isEmpty() && blockEntity.getSolventCount() > 0 && !blockEntity.isExtracting()) {
                    setCarried(blockEntity.getSolventDisplayItem().copy());
                    blockEntity.clearSolvent();
                }
                return;
            }
            if (slotId == SLOT_OUTPUT) {
                if (cursor.isEmpty() && blockEntity.hasOutput()) {
                    setCarried(blockEntity.extractOil());
                }
                return;
            }
        }
        super.clicked(slotId, button, type, player);
    }

    // Getter
    public int getMaterialWeight()        { return this.data.get(DATA_MATERIAL_WEIGHT); }
    public boolean isFromBuds()           { return this.data.get(DATA_IS_FROM_BUDS) == 1; }
    public int getSolventCount()          { return this.data.get(DATA_SOLVENT_COUNT); }
    public boolean isExtracting()         { return this.data.get(DATA_IS_EXTRACTING) == 1; }
    public boolean hasOutput()            { return this.data.get(DATA_HAS_OUTPUT) == 1; }
    public int getExtractionProgressRaw() { return this.data.get(DATA_EXTRACTION_PROGRESS); }
    public float getExtractionProgressF() { return (float)getExtractionProgressRaw() / OilExtractorBlockEntity.EXTRACTION_TICKS; }
    public boolean canStart() {
        return getMaterialWeight() >= OilExtractorBlockEntity.MIN_MATERIAL_WEIGHT
                && getSolventCount() >= 1 && !isExtracting() && !hasOutput();
    }
    public int getExpectedOilAmount() {
        float rate = isFromBuds() ? OilExtractorBlockEntity.BUD_CONVERSION_RATE : OilExtractorBlockEntity.TRIM_CONVERSION_RATE;
        return Math.max(1, (int)(getMaterialWeight() * rate));
    }

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
            ItemStack original = stack.copy();
            if ((stack.getItem() instanceof TrimmedBudItem || stack.getItem() instanceof TrimItem)
                    && !blockEntity.isExtracting() && !blockEntity.hasOutput()) {
                boolean moved = false;
                while (!stack.isEmpty() && blockEntity.addMaterial(stack)) {
                    if (!player.isCreative()) stack.shrink(1);
                    moved = true;
                }
                if (moved) {
                    slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                    return original;
                }
            } else if (stack.getItem() == CannabisItems.EXTRACTION_SOLVENT.get()
                    && !blockEntity.isExtracting() && !blockEntity.hasOutput()) {
                if (blockEntity.addSolvent(stack)) {
                    if (!player.isCreative()) stack.setCount(0);
                    slot.set(ItemStack.EMPTY);
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
                ItemStack extracted = blockEntity.extractOil();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        } else if (index == SLOT_MATERIAL) {
            if (blockEntity.getMaterialWeight() > 0 && !blockEntity.isExtracting()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack display = blockEntity.getInputDisplayItem().copy();
                blockEntity.clearMaterial();
                moveItemStackTo(display, HOTBAR_START, HOTBAR_START + 9, false);
                return display;
            }
        } else if (index == SLOT_SOLVENT) {
            if (blockEntity.getSolventCount() > 0 && !blockEntity.isExtracting()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack display = blockEntity.getSolventDisplayItem().copy();
                blockEntity.clearSolvent();
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
        private final OilExtractorBlockEntity blockEntity;
        public Provider(OilExtractorBlockEntity blockEntity) { this.blockEntity = blockEntity; }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.oel_extractor.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new OilExtractorMenu(containerId, playerInventory, blockEntity);
        }
    }
}
