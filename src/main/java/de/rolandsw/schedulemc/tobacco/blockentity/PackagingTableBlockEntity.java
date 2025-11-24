package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.PackagedTobaccoItem;
import de.rolandsw.schedulemc.tobacco.menu.PackagingTableMenu;
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

/**
 * BlockEntity für Packtisch
 * Speichert Input (fermentierter Tabak) und Output (abgepackte Pakete)
 */
public class PackagingTableBlockEntity extends BlockEntity implements MenuProvider {

    // Inventar: 1 Input Slot + 9 Output Slots
    private final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public PackagingTableBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.PACKAGING_TABLE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Tabak Packtisch");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PackagingTableMenu(containerId, playerInventory, this);
    }

    // ═══════════════════════════════════════════════════════════
    // INVENTAR ZUGRIFF
    // ═══════════════════════════════════════════════════════════

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    /**
     * Input Slot (Slot 0)
     */
    public ItemStack getInputStack() {
        return itemHandler.getStackInSlot(0);
    }

    public void setInputStack(ItemStack stack) {
        itemHandler.setStackInSlot(0, stack);
    }

    /**
     * Output Slots (Slots 1-9)
     */
    public ItemStack getOutputStack(int index) {
        if (index < 0 || index >= 9) return ItemStack.EMPTY;
        return itemHandler.getStackInSlot(1 + index);
    }

    public void setOutputStack(int index, ItemStack stack) {
        if (index >= 0 && index < 9) {
            itemHandler.setStackInSlot(1 + index, stack);
        }
    }

    /**
     * Leert alle Output-Slots in einen freien Slot
     */
    public int findFreeOutputSlot() {
        for (int i = 0; i < 9; i++) {
            if (getOutputStack(i).isEmpty()) {
                return i;
            }
        }
        return -1; // Kein freier Slot
    }

    // ═══════════════════════════════════════════════════════════
    // ABPACK-LOGIK
    // ═══════════════════════════════════════════════════════════

    /**
     * Packt Tabak ab
     *
     * @param weight Gewicht pro Paket (50, 100, 250, 500)
     * @return Anzahl erstellter Pakete
     */
    public int packageTobacco(int weight) {
        ItemStack input = getInputStack();

        // Check: Ist fermentierten Tabak?
        if (!(input.getItem() instanceof FermentedTobaccoLeafItem)) {
            return 0;
        }

        // Berechne verfügbares Gewicht (1 Blatt = 1g)
        int totalWeight = input.getCount();
        int packagesCount = totalWeight / weight;

        if (packagesCount == 0) {
            return 0; // Nicht genug
        }

        // Hole Typ und Qualität
        var type = FermentedTobaccoLeafItem.getType(input);
        var quality = FermentedTobaccoLeafItem.getQuality(input);
        long currentDay = level != null ? level.getDayTime() / 24000L : 0;

        int created = 0;

        // Erstelle Pakete
        for (int i = 0; i < packagesCount; i++) {
            int slot = findFreeOutputSlot();
            if (slot == -1) {
                break; // Kein Platz mehr
            }

            // Erstelle abgepackten Tabak
            ItemStack packagedTobacco = PackagedTobaccoItem.create(type, quality, weight, currentDay);
            setOutputStack(slot, packagedTobacco);
            created++;
        }

        // Verbrauche Input (1 Blatt = 1g)
        int itemsUsed = created * weight; // weight in Gramm
        input.shrink(itemsUsed);
        setInputStack(input);

        setChanged();
        return created;
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
}
