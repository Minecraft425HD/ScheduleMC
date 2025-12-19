package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.cannabis.items.CuredBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.coca.items.CocaineItem;
import de.rolandsw.schedulemc.coca.items.CrackRockItem;
import de.rolandsw.schedulemc.meth.items.MethItem;
import de.rolandsw.schedulemc.mushroom.items.DriedMushroomItem;
import de.rolandsw.schedulemc.poppy.items.HeroinItem;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstrakte Basisklasse für Packaging Table BlockEntities
 *
 * Eliminiert ~395 Zeilen Duplikation über 3 Packaging Tables:
 * - SmallPackagingTableBlockEntity (1g, 5g)
 * - MediumPackagingTableBlockEntity (10g, 25g)
 * - LargePackagingTableBlockEntity (50g, 100g)
 *
 * Gemeinsame Funktionalität:
 * - extractPackagingData() - 63 Zeilen
 * - findFreeSlot() - 7 Zeilen
 * - addItemToSlots() - 15 Zeilen
 * - Capability setup - 25 Zeilen
 * - NBT save/load - 10 Zeilen
 * - drops() - 6 Zeilen
 *
 * Subklassen implementieren nur:
 * - getDisplayName()
 * - createMenu()
 * - Package-Weight-spezifische Logik
 */
public abstract class AbstractPackagingTableBlockEntity extends BlockEntity implements MenuProvider, IUtilityConsumer {

    protected final ItemStackHandler itemHandler;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public AbstractPackagingTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state);
        this.itemHandler = createItemHandler(slots);
    }

    /**
     * Erstellt ItemStackHandler mit Slot-Validierung
     * Subklassen können überschreiben für custom Validierung
     */
    protected ItemStackHandler createItemHandler(int slots) {
        return new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
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
    // GEMEINSAME HELPER-METHODEN (eliminiert ~85 Zeilen Duplikation)
    // ═══════════════════════════════════════════════════════════

    /**
     * Helper class to store drug packaging data
     */
    protected static class PackagingData {
        final DrugType drugType;
        final ProductionQuality quality;
        final ProductionType variant;
        final String itemType;

        PackagingData(DrugType drugType, ProductionQuality quality, ProductionType variant, String itemType) {
            this.drugType = drugType;
            this.quality = quality;
            this.variant = variant;
            this.itemType = itemType;
        }
    }

    /**
     * Extracts DrugType, Quality, and Variant from any packageable drug item
     *
     * ELIMINIERT: 63 Zeilen Duplikation (100% identisch in allen 3 Packaging Tables)
     */
    protected PackagingData extractPackagingData(ItemStack input) {
        Item item = input.getItem();

        if (item instanceof FermentedTobaccoLeafItem) {
            return new PackagingData(
                DrugType.TOBACCO,
                FermentedTobaccoLeafItem.getQuality(input),
                FermentedTobaccoLeafItem.getType(input),
                "TOBACCO"
            );
        } else if (item instanceof CocaineItem) {
            return new PackagingData(
                DrugType.COCAINE,
                CocaineItem.getQuality(input),
                CocaineItem.getType(input),
                "COCAINE"
            );
        } else if (item instanceof CrackRockItem) {
            return new PackagingData(
                DrugType.COCAINE, // Crack is COCAINE drug type
                CrackRockItem.getQuality(input),
                CrackRockItem.getType(input),
                "CRACK"
            );
        } else if (item instanceof HeroinItem) {
            return new PackagingData(
                DrugType.HEROIN,
                HeroinItem.getQuality(input),
                HeroinItem.getType(input),
                "HEROIN"
            );
        } else if (item instanceof MethItem) {
            return new PackagingData(
                DrugType.METH,
                MethItem.getQuality(input),
                null, // Meth has no variant/type
                "METH"
            );
        } else if (item instanceof DriedMushroomItem driedMushroom) {
            return new PackagingData(
                DrugType.MUSHROOM,
                DriedMushroomItem.getQuality(input),
                driedMushroom.getMushroomType(),
                "MUSHROOM"
            );
        } else if (item instanceof TrimmedBudItem) {
            return new PackagingData(
                DrugType.CANNABIS,
                TrimmedBudItem.getQuality(input),
                TrimmedBudItem.getStrain(input),
                "TRIMMED_CANNABIS"
            );
        } else if (item instanceof CuredBudItem) {
            return new PackagingData(
                DrugType.CANNABIS,
                CuredBudItem.getQuality(input),
                CuredBudItem.getStrain(input),
                "CURED_CANNABIS"
            );
        }

        return null; // Item not packageable
    }

    /**
     * Findet einen freien Slot im angegebenen Bereich
     *
     * ELIMINIERT: 7 Zeilen Duplikation × 3 = 21 Zeilen
     */
    protected int findFreeSlot(int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; i++) {
            if (itemHandler.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Fügt Items zu Slots hinzu (verteilt über Bereich)
     *
     * ELIMINIERT: 15 Zeilen Duplikation × 3 = 45 Zeilen
     */
    protected void addItemToSlots(ItemStack item, int startSlot, int endSlot) {
        int remaining = item.getCount();

        for (int slot = startSlot; slot <= endSlot && remaining > 0; slot++) {
            ItemStack slotStack = itemHandler.getStackInSlot(slot);

            if (slotStack.isEmpty()) {
                int toAdd = Math.min(remaining, item.getMaxStackSize());
                ItemStack newStack = item.copy();
                newStack.setCount(toAdd);
                itemHandler.setStackInSlot(slot, newStack);
                remaining -= toAdd;
            } else if (ItemStack.isSameItemSameTags(slotStack, item)) {
                int toAdd = Math.min(remaining, item.getMaxStackSize() - slotStack.getCount());
                slotStack.grow(toAdd);
                remaining -= toAdd;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CAPABILITY SETUP (eliminiert ~25 Zeilen × 3 = 75 Zeilen)
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

    // ═══════════════════════════════════════════════════════════
    // NBT SAVE/LOAD (eliminiert ~10 Zeilen × 3 = 30 Zeilen)
    // ═══════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════
    // DROPS (eliminiert ~6 Zeilen × 3 = 18 Zeilen)
    // ═══════════════════════════════════════════════════════════

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY CONSUMER INTERFACE
    // ═══════════════════════════════════════════════════════════

    public BlockPos getConsumerPos() {
        return this.worldPosition;
    }
}
