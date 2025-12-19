package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.menu.LargePackagingTableMenu;
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
 * BlockEntity für Large Packaging Table (20g)
 * Kein Verpackungsmaterial benötigt!
 * Inventar:
 * - Slot 0: Input (fermentierter Tabak)
 * - Slots 1-9: Output (20g Pakete)
 */
public class LargePackagingTableBlockEntity extends BlockEntity implements MenuProvider, IUtilityConsumer {

    // Inventar: 1 Input + 9 Output = 10 Slots
    private final ItemStackHandler itemHandler = new ItemStackHandler(10) {
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
            // Slots 1-9: Output (nur verpackte 20g Pakete)
            if (slot >= 1 && slot <= 9) {
                return stack.getItem() instanceof PackagedDrugItem;
            }
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public LargePackagingTableBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.LARGE_PACKAGING_TABLE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Großer Packtisch (20g)");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new LargePackagingTableMenu(containerId, playerInventory, this);
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
     */
    private PackagingData extractPackagingData(ItemStack input) {
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

    // ═══════════════════════════════════════════════════════════
    // PACK-LOGIK (20g ohne Verpackungsmaterial)
    // ═══════════════════════════════════════════════════════════

    public int packageTobacco20g() {
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
        int packagesCount = totalWeight / 20; // Für 20g Pakete

        if (packagesCount == 0) {
            return 0;
        }

        long currentDay = level != null ? level.getDayTime() / 24000L : 0;
        int created = 0;

        for (int i = 0; i < packagesCount; i++) {
            int slot = findFreeSlot();
            if (slot == -1) {
                break; // Kein Platz mehr
            }

            ItemStack packagedDrug = PackagedDrugItem.create(data.drugType, 20, data.quality, data.variant, currentDay, data.itemType);
            itemHandler.setStackInSlot(slot, packagedDrug);
            created++;
        }

        // Verbrauche Input (1 item = 1g)
        int itemsUsed = created * 20; // Jedes 20g Paket braucht 20 items
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

        for (int i = 1; i <= 9; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagedDrugItem) {
                int weight = PackagedDrugItem.getWeight(stack);
                totalWeight += weight;

                // Entferne das Paket (kein Material zurückgeben)
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

    private int findFreeSlot() {
        for (int i = 1; i <= 9; i++) {
            if (itemHandler.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
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
