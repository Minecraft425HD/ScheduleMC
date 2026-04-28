package de.rolandsw.schedulemc.coca.menu;

import de.rolandsw.schedulemc.coca.blockentity.CrackCookerBlockEntity;
import de.rolandsw.schedulemc.coca.items.BackpulverItem;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.coca.items.CocaineItem;
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

public class CrackCookerMenu extends AbstractContainerMenu {

    public final CrackCookerBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer dummy = new SimpleContainer(3);

    private static final int DATA_COCAINE_GRAMS  = 0;
    private static final int DATA_BACKPULVER      = 1;
    private static final int DATA_COOK_TICK       = 2; // 0..80
    private static final int DATA_IS_ACTIVE       = 3;
    private static final int DATA_HAS_OUTPUT      = 4;
    private static final int DATA_SIZE            = 5;

    public static final int BUTTON_START  = 0;
    public static final int BUTTON_REMOVE = 1;

    public static final int SLOT_COCAINE    = 0;
    public static final int SLOT_BACKPULVER = 1;
    public static final int SLOT_OUTPUT     = 2;
    public static final int HOTBAR_START    = 3;

    // Server-side constructor
    public CrackCookerMenu(int containerId, Inventory playerInventory, CrackCookerBlockEntity blockEntity) {  // NOPMD
        super(CocaMenuTypes.CRACK_COOKER_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case DATA_COCAINE_GRAMS -> blockEntity.getCocaineGrams();
                    case DATA_BACKPULVER    -> blockEntity.getBackpulverCount();
                    case DATA_COOK_TICK     -> blockEntity.getCookTick();
                    case DATA_IS_ACTIVE     -> blockEntity.isMinigameActive() ? 1 : 0;
                    case DATA_HAS_OUTPUT    -> blockEntity.hasOutput() ? 1 : 0;
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
    public CrackCookerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CocaMenuTypes.CRACK_COOKER_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof CrackCookerBlockEntity cookerBE ? cookerBE : null;  // NOPMD
        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
        addMachineSlots();
        addPlayerHotbar(playerInventory);
    }

    private void addMachineSlots() {
        CrackCookerBlockEntity be = blockEntity;
        // Slot 0: cocaine input/display
        addSlot(new Slot(dummy, 0, 22, 30) {
            @Override public @NotNull ItemStack getItem() {
                if (be == null || be.getCocaineGrams() <= 0) return ItemStack.EMPTY;
                return new ItemStack(CocaItems.COCAINE.get(), be.getCocaineGrams());
            }
            @Override public boolean hasItem()            { return be != null && be.getCocaineGrams() > 0; }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
        });
        // Slot 1: backpulver input/display
        addSlot(new Slot(dummy, 1, 44, 30) {
            @Override public @NotNull ItemStack getItem() {
                if (be == null || be.getBackpulverCount() <= 0) return ItemStack.EMPTY;
                return new ItemStack(CocaItems.BACKPULVER.get(), be.getBackpulverCount());
            }
            @Override public boolean hasItem()            { return be != null && be.getBackpulverCount() > 0; }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
        });
        // Slot 2: output display (crack)
        addSlot(new Slot(dummy, 2, 134, 30) {
            @Override public @NotNull ItemStack getItem() { return be != null && be.hasOutput() ? new ItemStack(CocaItems.CRACK_ROCK.get(), 1) : ItemStack.EMPTY; }
            @Override public boolean hasItem()            { return be != null && be.hasOutput(); }
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
            @Override public @NotNull ItemStack remove(int count)   { return ItemStack.EMPTY; }
        });
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) addSlot(new Slot(inv, i, 8 + i * 18, 142));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity == null || blockEntity.isRemoved()) return false;
        if (id == BUTTON_START) {
            return blockEntity.startCooking(player.getUUID());
        }
        if (id == BUTTON_REMOVE) {
            if (blockEntity.isMinigameActive()) {
                blockEntity.removeCrack();
                return true;
            }
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (blockEntity == null) return;
        if (type == ClickType.PICKUP) {
            ItemStack cursor = getCarried();
            if (slotId == SLOT_COCAINE) {
                if (!cursor.isEmpty() && cursor.getItem() instanceof CocaineItem) {
                    if (blockEntity.addCocaine(cursor)) {
                        if (!player.isCreative()) cursor.shrink(cursor.getCount());
                        setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                    }
                }
                // cursor empty: can't take cocaine back
                return;
            }
            if (slotId == SLOT_BACKPULVER) {
                if (!cursor.isEmpty() && cursor.is(CocaItems.BACKPULVER.get())
                        && !blockEntity.isMinigameActive() && !blockEntity.hasOutput()) {
                    if (blockEntity.addBackpulver(cursor)) {
                        if (!player.isCreative()) cursor.shrink(cursor.getCount());
                        setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                    }
                }
                // cursor empty: can't take backpulver back
                return;
            }
            if (slotId == SLOT_OUTPUT) {
                if (cursor.isEmpty() && blockEntity.hasOutput()) {
                    setCarried(blockEntity.extractCrack());
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
            if (stack.getItem() instanceof CocaineItem
                    && !blockEntity.isMinigameActive() && !blockEntity.hasOutput()) {
                if (blockEntity.addCocaine(stack)) {
                    if (!player.isCreative()) stack.shrink(stack.getCount());
                    slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                    return ItemStack.EMPTY;
                }
            }
            if (stack.is(CocaItems.BACKPULVER.get())
                    && !blockEntity.isMinigameActive() && !blockEntity.hasOutput()) {
                if (blockEntity.addBackpulver(stack)) {
                    if (!player.isCreative()) stack.shrink(stack.getCount());
                    slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
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
                ItemStack extracted = blockEntity.extractCrack();
                if (extracted.isEmpty()) return ItemStack.EMPTY;
                ItemStack original = extracted.copy();
                moveItemStackTo(extracted, HOTBAR_START, HOTBAR_START + 9, false);
                return original;
            }
        }
        return ItemStack.EMPTY;
    }

    // Getters for screen
    public int getCocaineGrams()     { return this.data.get(DATA_COCAINE_GRAMS); }
    public int getBackpulverCount()  { return this.data.get(DATA_BACKPULVER); }
    public int getCookTick()         { return this.data.get(DATA_COOK_TICK); }
    public boolean isMinigameActive(){ return this.data.get(DATA_IS_ACTIVE) == 1; }
    public boolean hasOutput()       { return this.data.get(DATA_HAS_OUTPUT) == 1; }
    public boolean canStart()        { return getCocaineGrams() >= 1 && getBackpulverCount() >= 1 && !isMinigameActive() && !hasOutput(); }

    /** Zone from cook tick: 0=too early, 1=good, 2=perfect, 3=too late */
    public int getCookZone() {
        int tick = getCookTick();
        if (tick >= CrackCookerBlockEntity.PERFECT_WINDOW_START && tick <= CrackCookerBlockEntity.PERFECT_WINDOW_END) return 2;
        if (tick >= CrackCookerBlockEntity.GOOD_WINDOW_START && tick <= CrackCookerBlockEntity.GOOD_WINDOW_END) return 1;
        if (tick < CrackCookerBlockEntity.GOOD_WINDOW_START) return 0;
        return 3;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
                player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    public static class Provider implements MenuProvider {
        private final CrackCookerBlockEntity blockEntity;
        public Provider(CrackCookerBlockEntity blockEntity) { this.blockEntity = blockEntity; }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.crack_cooker.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new CrackCookerMenu(containerId, playerInventory, blockEntity);
        }
    }
}
