package de.rolandsw.schedulemc.tobacco.blockentity;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;

import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.items.PackagingBoxItem;
import de.rolandsw.schedulemc.tobacco.menu.MediumPackagingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockEntity für Medium Packaging Table (10g)
 *
 * Erweitert AbstractPackagingTableBlockEntity für geteilte Funktionalität
 *
 * Inventar:
 * - Slot 0: Input (fermentierter Tabak)
 * - Slots 1-10: Schachteln (leer und voll gemischt, für 10g)
 */
public class MediumPackagingTableBlockEntity extends AbstractPackagingTableBlockEntity {

    public MediumPackagingTableBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.MEDIUM_PACKAGING_TABLE.get(), pos, state, 11);
    }

    /**
     * Custom ItemStackHandler mit Slot-Validierung für Medium Packaging Table
     */
    @Override
    protected ItemStackHandler createItemHandler(int slots) {
        return new ItemStackHandler(slots) {
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
                // Slots 1-10: Schachteln (leer oder voll)
                if (slot >= 1 && slot <= 10) {
                    return stack.getItem() instanceof PackagingBoxItem ||
                           (stack.getItem() instanceof PackagedDrugItem && PackagedDrugItem.getWeight(stack) == 10);
                }
                return false;
            }
        };
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

    // ═══════════════════════════════════════════════════════════
    // PACK-LOGIK (10g mit Schachteln)
    // ═══════════════════════════════════════════════════════════

    /**
     * Packt Drug-Items in 10g Schachteln ab
     * @return Anzahl erstellter Pakete
     */
    public int packageTobacco10g() {
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
        int packagesCount = totalWeight / 10; // Für 10g Pakete

        // Zähle verfügbare leere Schachteln (Slots 1-10)
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

            ItemStack packagedDrug = PackagedDrugItem.create(data.drugType, 10, data.quality, data.variant, currentDay, data.itemType);
            itemHandler.setStackInSlot(slot, packagedDrug);
            created++;
        }

        // Verbrauche Input (1 item = 1g)
        int itemsUsed = created * 10; // Jedes 10g Paket braucht 10 items
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
        for (int i = 1; i <= 10; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagedDrugItem) {
                int weight = PackagedDrugItem.getWeight(stack);
                totalWeight += weight;

                // Gib Schachtel zurück
                addItemToSlots(new ItemStack(TobaccoItems.PACKAGING_BOX.get(), 1), 1, 10);

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
    // HELPER METHODEN (Table-specific)
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

    // ═══════════════════════════════════════════════════════════
    // IUtilityConsumer Implementation
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isActivelyConsuming() {
        // Packtisch verbraucht keinen Strom (manuelle Arbeit)
        return false;
    }
}
