package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.items.*;
import de.rolandsw.schedulemc.tobacco.menu.MediumPackagingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;

/**
 * BlockEntity für Medium Packaging Table (10g)
 * Inventar:
 * - Slot 0: Input (fermentierter Tabak)
 * - Slots 1-10: Schachteln (5 leer, 5 voll)
 */
public class MediumPackagingTableBlockEntity extends BlockEntity implements MenuProvider, IUtilityConsumer {

    // Inventar: 1 Input + 10 Schachteln = 11 Slots
    private final ItemStackHandler itemHandler = new ItemStackHandler(11) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return stack.getItem() instanceof FermentedTobaccoLeafItem;
            }
            if (slot >= 1 && slot <= 10) {
                return stack.getItem() instanceof PackagingBoxItem ||
                       (stack.getItem() instanceof PackagedTobaccoItem && PackagedTobaccoItem.getWeight(stack) == 10);
            }
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public MediumPackagingTableBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.MEDIUM_PACKAGING_TABLE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Mittlerer Packtisch (10g)");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MediumPackagingTableMenu(containerId, playerInventory, this);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ItemStack getInputStack() {
        return itemHandler.getStackInSlot(0);
    }

    public void setInputStack(ItemStack stack) {
        itemHandler.setStackInSlot(0, stack);
    }

    // ═══════════════════════════════════════════════════════════
    // PACK-LOGIK (10g mit Schachteln)
    // ═══════════════════════════════════════════════════════════

    public int packageTobacco10g() {
        ItemStack input = getInputStack();

        if (!(input.getItem() instanceof FermentedTobaccoLeafItem)) {
            return 0;
        }

        // Berechne verfügbares Gewicht (1 Blatt = 1g)
        int totalWeight = input.getCount();
        int packagesCount = totalWeight / 10; // Für 10g Pakete

        // Zähle leere Schachteln
        int emptyBoxes = 0;
        for (int i = 1; i <= 10; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagingBoxItem) {
                emptyBoxes += stack.getCount();
            }
        }

        packagesCount = Math.min(packagesCount, emptyBoxes);

        if (packagesCount == 0) {
            return 0;
        }

        var type = FermentedTobaccoLeafItem.getType(input);
        var quality = FermentedTobaccoLeafItem.getQuality(input);
        long currentDay = level != null ? level.getDayTime() / 24000L : 0;

        int created = 0;

        for (int i = 0; i < packagesCount; i++) {
            if (!consumeEmptyBox()) {
                break;
            }

            int slot = findFreeSlot(1, 10);
            if (slot == -1) {
                break;
            }

            ItemStack packagedTobacco = PackagedTobaccoItem.create(type, quality, 10, currentDay);
            itemHandler.setStackInSlot(slot, packagedTobacco);
            created++;
        }

        // Verbrauche Input (1 Blatt = 1g)
        int itemsUsed = created * 10; // Jedes 10g Paket braucht 10 Blätter
        input.shrink(itemsUsed);
        setInputStack(input);

        setChanged();
        return created;
    }

    // ═══════════════════════════════════════════════════════════
    // UNPACK-LOGIK
    // ═══════════════════════════════════════════════════════════

    public void unpackAll() {
        ItemStack input = getInputStack();
        int totalWeight = 0;

        for (int i = 1; i <= 10; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagedTobaccoItem) {
                int weight = PackagedTobaccoItem.getWeight(stack);
                totalWeight += weight;

                // Gib leere Schachtel zurück
                addItemToSlots(new ItemStack(TobaccoItems.PACKAGING_BOX.get(), 1), 1, 10);

                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        // Gib Tabak zurück (1g = 1 Blatt)
        if (totalWeight > 0 && !input.isEmpty() && input.getItem() instanceof FermentedTobaccoLeafItem) {
            int itemsToAdd = totalWeight; // 1g = 1 Blatt
            if (itemsToAdd > 0) {
                input.grow(itemsToAdd);
                setInputStack(input);
            }
        }

        setChanged();
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODEN
    // ═══════════════════════════════════════════════════════════

    private boolean consumeEmptyBox() {
        for (int i = 1; i <= 10; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagingBoxItem) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private int findFreeSlot(int start, int end) {
        for (int i = start; i <= end; i++) {
            if (itemHandler.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void addItemToSlots(ItemStack itemToAdd, int start, int end) {
        for (int i = start; i <= end; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) {
                itemHandler.setStackInSlot(i, itemToAdd.copy());
                return;
            } else if (ItemStack.isSameItemSameTags(stack, itemToAdd) && stack.getCount() < stack.getMaxStackSize()) {
                int space = stack.getMaxStackSize() - stack.getCount();
                int toAdd = Math.min(space, itemToAdd.getCount());
                stack.grow(toAdd);
                itemToAdd.shrink(toAdd);
                if (itemToAdd.isEmpty()) {
                    return;
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CAPABILITIES
    // ═══════════════════════════════════════════════════════════

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    // ═══════════════════════════════════════════════════════════
    // IUtilityConsumer Implementation
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isActivelyConsuming() {
        // Packtisch verbraucht keinen Strom (manuelle Arbeit)
        return false;
    }
}
