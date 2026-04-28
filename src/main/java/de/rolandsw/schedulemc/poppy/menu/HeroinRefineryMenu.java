package de.rolandsw.schedulemc.poppy.menu;

import de.rolandsw.schedulemc.poppy.blockentity.HeroinRefineryBlockEntity;
import de.rolandsw.schedulemc.poppy.items.MorphineItem;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeroinRefineryMenu extends AbstractContainerMenu {

    public final HeroinRefineryBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer dummy = new SimpleContainer(3);

    private static final int DATA_FUEL_LEVEL      = 0;
    private static final int DATA_FUEL_MAX        = 1;
    private static final int DATA_INPUT_COUNT     = 2;
    private static final int DATA_CAPACITY        = 3;
    private static final int DATA_OUTPUT_COUNT    = 4;
    private static final int DATA_PROGRESS_SCALED = 5;
    private static final int DATA_SIZE            = 6;

    public static final int SLOT_FUEL     = 0;
    public static final int SLOT_INPUT    = 1;
    public static final int SLOT_OUTPUT   = 2;
    public static final int HOTBAR_START  = 3;

    public HeroinRefineryMenu(int containerId, Inventory playerInventory, HeroinRefineryBlockEntity blockEntity) {  // NOPMD
        super(PoppyMenuTypes.HEROIN_REFINERY_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case DATA_FUEL_LEVEL      -> blockEntity.getFuelLevel();
                    case DATA_FUEL_MAX        -> blockEntity.getMaxFuel();
                    case DATA_INPUT_COUNT     -> blockEntity.getInputCount();
                    case DATA_CAPACITY        -> blockEntity.getCapacity();
                    case DATA_OUTPUT_COUNT    -> blockEntity.getOutputCount();
                    case DATA_PROGRESS_SCALED -> (int)(blockEntity.getAverageProgress() * 1000);
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

    public HeroinRefineryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(PoppyMenuTypes.HEROIN_REFINERY_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof HeroinRefineryBlockEntity raffBE ? raffBE : null;  // NOPMD
        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    private void addMachineSlots() {
        HeroinRefineryBlockEntity be = blockEntity;
        addSlot(new Slot(dummy, 0, 22, 30) {
            @Override public @NotNull ItemStack getItem() { return ItemStack.EMPTY; }
            @Override public boolean hasItem()            { return false; }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
        });
        addSlot(new Slot(dummy, 1, 74, 30) {
            @Override public @NotNull ItemStack getItem() { return be != null ? be.getInputDisplayItem() : ItemStack.EMPTY; }
            @Override public boolean hasItem()            { return be != null && be.hasInput(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
        });
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
            if (slotId == SLOT_FUEL) {
                if (!cursor.isEmpty() && isFuel(cursor)) {
                    int fuelValue = getFuelValue(cursor) * cursor.getCount();
                    blockEntity.addFuel(fuelValue);
                    if (!player.isCreative()) cursor.shrink(cursor.getCount());
                    setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                }
                return;
            }
            if (slotId == SLOT_INPUT) {
                if (!cursor.isEmpty() && cursor.getItem() instanceof MorphineItem && !blockEntity.isFull()) {
                    while (!cursor.isEmpty() && !blockEntity.isFull()) {
                        if (blockEntity.addMorphine(cursor)) {
                            if (!player.isCreative()) cursor.shrink(1);
                        } else break;
                    }
                    setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                }
                return;
            }
            if (slotId == SLOT_OUTPUT) {
                if (cursor.isEmpty() && blockEntity.hasOutput()) {
                    setCarried(blockEntity.extractAllHeroin());
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
            if (stack.getItem() instanceof MorphineItem && !blockEntity.isFull()) {
                while (!stack.isEmpty() && !blockEntity.isFull()) {
                    if (blockEntity.addMorphine(stack)) {
                        if (!player.isCreative()) stack.shrink(1);
                    } else break;
                }
                slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                return ItemStack.EMPTY;
            }
            if (isFuel(stack)) {
                int fuelValue = getFuelValue(stack) * stack.getCount();
                blockEntity.addFuel(fuelValue);
                if (!player.isCreative()) stack.shrink(stack.getCount());
                slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                return ItemStack.EMPTY;
            }
        } else if (index == SLOT_OUTPUT) {
            if (blockEntity.hasOutput()) {
                boolean hasSpace = false;
                for (int i = HOTBAR_START; i < HOTBAR_START + 9; i++) {
                    if (!slots.get(i).hasItem()) { hasSpace = true; break; }
                }
                if (!hasSpace) return ItemStack.EMPTY;
                ItemStack extracted = blockEntity.extractAllHeroin();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isFuel(ItemStack s) {
        return s.is(Items.COAL) || s.is(Items.CHARCOAL) || s.is(Items.COAL_BLOCK)
                || s.getItem().toString().contains("log") || s.getItem().toString().contains("planks");
    }

    public static int getFuelValue(ItemStack s) {
        if (s.is(Items.COAL_BLOCK)) return 800;
        if (s.is(Items.COAL) || s.is(Items.CHARCOAL)) return 100;
        if (s.getItem().toString().contains("log")) return 50;
        return 25;
    }

    public int getFuelLevel()         { return this.data.get(DATA_FUEL_LEVEL); }
    public int getFuelMax()           { return this.data.get(DATA_FUEL_MAX); }
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
        private final HeroinRefineryBlockEntity blockEntity;
        public Provider(HeroinRefineryBlockEntity blockEntity) { this.blockEntity = blockEntity; }

        @Override public @NotNull Component getDisplayName() {
            return Component.translatable("gui.heroin_refinery.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new HeroinRefineryMenu(containerId, playerInventory, blockEntity);
        }
    }
}
