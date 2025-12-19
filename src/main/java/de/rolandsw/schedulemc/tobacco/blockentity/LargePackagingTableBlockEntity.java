package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.menu.LargePackagingTableMenu;
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
 * BlockEntity für Large Packaging Table (20g)
 *
 * Erweitert AbstractPackagingTableBlockEntity für geteilte Funktionalität
 *
 * Kein Verpackungsmaterial benötigt!
 * Inventar:
 * - Slot 0: Input (fermentierter Tabak)
 * - Slots 1-9: Output (20g Pakete)
 */
public class LargePackagingTableBlockEntity extends AbstractPackagingTableBlockEntity {

    public LargePackagingTableBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.LARGE_PACKAGING_TABLE.get(), pos, state, 10);
    }

    /**
     * Custom ItemStackHandler mit Slot-Validierung für Large Packaging Table
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
                // Slots 1-9: Output (nur verpackte 20g Pakete)
                if (slot >= 1 && slot <= 9) {
                    return stack.getItem() instanceof PackagedDrugItem;
                }
                return false;
            }
        };
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

    // ═══════════════════════════════════════════════════════════
    // PACK-LOGIK (20g, kein Material benötigt)
    // ═══════════════════════════════════════════════════════════

    /**
     * Packt Drug-Items in 20g Pakete ab (ohne Verpackungsmaterial)
     * @return Anzahl erstellter Pakete
     */
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
            int slot = findFreeSlot(1, 9);
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

    /**
     * Entpackt alle vollen Pakete zurück zu Input
     */
    public void unpackAll() {
        ItemStack input = getInputStack();
        int totalWeight = 0;

        // Durchsuche alle Slots nach vollen Paketen
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof PackagedDrugItem) {
                int weight = PackagedDrugItem.getWeight(stack);
                totalWeight += weight;

                // Entferne das volle Paket (kein Material zurück, da keins verbraucht wurde)
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        // Gib Material zurück (1g = 1 item)
        if (totalWeight > 0 && !input.isEmpty()) {
            int itemsToAdd = totalWeight; // 1g = 1 item
            if (itemsToAdd > 0) {
                input.grow(itemsToAdd);
                setInputStack(input);
            }
        }

        setChanged();
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
