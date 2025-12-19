package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.items.*;
import de.rolandsw.schedulemc.tobacco.menu.SmallPackagingTableMenu;
import de.rolandsw.schedulemc.coca.items.CocaineItem;
import de.rolandsw.schedulemc.coca.items.CrackRockItem;
import de.rolandsw.schedulemc.poppy.items.HeroinItem;
import de.rolandsw.schedulemc.meth.items.MethItem;
import de.rolandsw.schedulemc.mushroom.items.DriedMushroomItem;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.items.CuredBudItem;
import net.minecraft.world.item.Item;
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
 * BlockEntity für Small Packaging Table (1g und 5g)
 * Inventar:
 * - Slot 0: Input (fermentierter Tabak)
 * - Slots 1-10: Tüten (leer und voll gemischt, für 1g)
 * - Slots 11-20: Gläser (leer und voll gemischt, für 5g)
 */
public class SmallPackagingTableBlockEntity extends BlockEntity implements MenuProvider, IUtilityConsumer {

    // Inventar: 1 Input + 10 Tüten + 10 Gläser = 21 Slots
    private final ItemStackHandler itemHandler = new ItemStackHandler(21) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // Slot 0: Verpackbare Drug-Items (fermentiert/verarbeitet)
            if (slot == 0) {
                return PackagedDrugItem.isPackageableItem(stack);
            }
            // Slots 1-10: Nur Tüten (leer oder voll)
            if (slot >= 1 && slot <= 10) {
                return stack.getItem() instanceof PackagingBagItem ||
                       (stack.getItem() instanceof PackagedDrugItem && PackagedDrugItem.getWeight(stack) == 1);
            }
            // Slots 11-20: Nur Gläser (leer oder voll)
            if (slot >= 11 && slot <= 20) {
                return stack.getItem() instanceof PackagingJarItem ||
                       (stack.getItem() instanceof PackagedDrugItem && PackagedDrugItem.getWeight(stack) == 5);
            }
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public SmallPackagingTableBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.SMALL_PACKAGING_TABLE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Kleiner Packtisch (1g, 5g)");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SmallPackagingTableMenu(containerId, playerInventory, this);
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
    // HELPER CLASSES & METHODS FOR GENERIC DRUG PACKAGING
    // ═══════════════════════════════════════════════════════════

    /**
     * Helper class to store drug packaging data
     */
    private static class PackagingData {
        final DrugType drugType;
        final ProductionQuality quality;
        final ProductionType variant;

        PackagingData(DrugType drugType, ProductionQuality quality, ProductionType variant) {
            this.drugType = drugType;
            this.quality = quality;
            this.variant = variant;
        }
    }

    /**
     * Extracts DrugType, Quality, and Variant from any packageable drug item
     */
    private PackagingData extractPackagingData(ItemStack input) {
        Item item = input.getItem();

        if (item instanceof FermentedTobaccoLeafItem) {
            return new PackagingData(
                DrugType.TOBACCO,
                FermentedTobaccoLeafItem.getQuality(input),
                FermentedTobaccoLeafItem.getType(input)
            );
        } else if (item instanceof CocaineItem) {
            return new PackagingData(
                DrugType.COCAINE,
                CocaineItem.getQuality(input),
                CocaineItem.getType(input)
            );
        } else if (item instanceof CrackRockItem) {
            return new PackagingData(
                DrugType.COCAINE, // Crack is COCAINE drug type
                CrackRockItem.getQuality(input),
                CrackRockItem.getType(input)
            );
        } else if (item instanceof HeroinItem) {
            return new PackagingData(
                DrugType.HEROIN,
                HeroinItem.getQuality(input),
                HeroinItem.getType(input)
            );
        } else if (item instanceof MethItem) {
            return new PackagingData(
                DrugType.METH,
                MethItem.getQuality(input),
                null // Meth has no variant/type
            );
        } else if (item instanceof DriedMushroomItem driedMushroom) {
            return new PackagingData(
                DrugType.MUSHROOM,
                DriedMushroomItem.getQuality(input),
                driedMushroom.getMushroomType()
            );
        } else if (item instanceof TrimmedBudItem) {
            return new PackagingData(
                DrugType.CANNABIS,
                TrimmedBudItem.getQuality(input),
                TrimmedBudItem.getStrain(input)
            );
        } else if (item instanceof CuredBudItem) {
            return new PackagingData(
                DrugType.CANNABIS,
                CuredBudItem.getQuality(input),
                CuredBudItem.getStrain(input)
            );
        }

        return null; // Item not packageable
    }

    // ═══════════════════════════════════════════════════════════
    // PACK-LOGIK (1g mit Tüten)
    // ═══════════════════════════════════════════════════════════

    /**
     * Packt Drug-Items in 1g Tüten ab
     * @return Anzahl erstellter Pakete
     */
    public int packageTobacco1g() {
        ItemStack input = getInputStack();

        // Check if item is packageable
        if (!PackagedDrugItem.isPackageableItem(input)) {
            return 0;
        }

        // Extract packaging data from input item
        PackagingData data = extractPackagingData(input);
        if (data == null) {
            return 0;
        }

        // Berechne verfügbares Gewicht (1 item = 1g)
        int totalWeight = input.getCount();
        int packagesCount = totalWeight / 1; // Für 1g Pakete (= totalWeight)

        // Zähle verfügbare leere Tüten (Slots 1-10)
        int emptyBags = 0;
        for (int i = 1; i <= 10; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagingBagItem) {
                emptyBags += stack.getCount();
            }
        }

        // Begrenze auf verfügbare Tüten
        packagesCount = Math.min(packagesCount, emptyBags);

        if (packagesCount == 0) {
            return 0;
        }

        long currentDay = level != null ? level.getDayTime() / 24000L : 0;
        int created = 0;

        // Erstelle Pakete
        for (int i = 0; i < packagesCount; i++) {
            // Finde leere Tüte und verbrauche sie
            if (!consumeEmptyBag()) {
                break;
            }

            // Finde freien Slot für volle Tüte (Slots 1-10)
            int slot = findFreeSlot(1, 10);
            if (slot == -1) {
                break; // Kein Platz mehr
            }

            // Erstelle 1g Paket mit universellem System
            ItemStack packagedDrug = PackagedDrugItem.create(data.drugType, 1, data.quality, data.variant, currentDay);
            itemHandler.setStackInSlot(slot, packagedDrug);
            created++;
        }

        // Verbrauche Input (1 item = 1g)
        int itemsUsed = created * 1; // Jedes 1g Paket braucht 1 item
        input.shrink(itemsUsed);
        setInputStack(input);

        setChanged();
        return created;
    }

    // ═══════════════════════════════════════════════════════════
    // PACK-LOGIK (5g mit Gläsern)
    // ═══════════════════════════════════════════════════════════

    /**
     * Packt Drug-Items in 5g Gläser ab
     * @return Anzahl erstellter Pakete
     */
    public int packageTobacco5g() {
        ItemStack input = getInputStack();

        // Check if item is packageable
        if (!PackagedDrugItem.isPackageableItem(input)) {
            return 0;
        }

        // Extract packaging data from input item
        PackagingData data = extractPackagingData(input);
        if (data == null) {
            return 0;
        }

        // Berechne verfügbares Gewicht (1 item = 1g)
        int totalWeight = input.getCount();
        int packagesCount = totalWeight / 5; // Für 5g Pakete

        // Zähle verfügbare leere Gläser (Slots 11-20)
        int emptyJars = 0;
        for (int i = 11; i <= 20; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagingJarItem) {
                emptyJars += stack.getCount();
            }
        }

        packagesCount = Math.min(packagesCount, emptyJars);

        if (packagesCount == 0) {
            return 0;
        }

        long currentDay = level != null ? level.getDayTime() / 24000L : 0;
        int created = 0;

        for (int i = 0; i < packagesCount; i++) {
            if (!consumeEmptyJar()) {
                break;
            }

            int slot = findFreeSlot(11, 20);
            if (slot == -1) {
                break;
            }

            ItemStack packagedDrug = PackagedDrugItem.create(data.drugType, 5, data.quality, data.variant, currentDay);
            itemHandler.setStackInSlot(slot, packagedDrug);
            created++;
        }

        // Verbrauche Input (1 item = 1g)
        int itemsUsed = created * 5; // Jedes 5g Paket braucht 5 items
        input.shrink(itemsUsed);
        setInputStack(input);

        setChanged();
        return created;
    }

    // ═══════════════════════════════════════════════════════════
    // UNPACK-LOGIK
    // ═══════════════════════════════════════════════════════════

    /**
     * Entpackt alle vollen Pakete zurück zu Tabak + leeres Material
     */
    public void unpackAll() {
        ItemStack input = getInputStack();
        int totalWeight = 0;

        // Durchsuche alle Slots nach vollen Paketen
        for (int i = 1; i <= 20; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagedDrugItem) {
                int weight = PackagedDrugItem.getWeight(stack);
                totalWeight += weight;

                // Gib leeres Material zurück
                if (weight == 1) {
                    // Gib Tüte zurück
                    addItemToSlots(new ItemStack(TobaccoItems.PACKAGING_BAG.get(), 1), 1, 10);
                } else if (weight == 5) {
                    // Gib Glas zurück
                    addItemToSlots(new ItemStack(TobaccoItems.PACKAGING_JAR.get(), 1), 11, 20);
                }

                // Entferne das volle Paket
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

    private boolean consumeEmptyBag() {
        for (int i = 1; i <= 10; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagingBagItem) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private boolean consumeEmptyJar() {
        for (int i = 11; i <= 20; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagingJarItem) {
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

    // ═══════════════════════════════════════════════════════════
    // NBT SPEICHERN/LADEN
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
    // DROPS
    // ═══════════════════════════════════════════════════════════

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
