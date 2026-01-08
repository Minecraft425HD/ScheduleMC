package de.rolandsw.schedulemc.production.blockentity;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
 * Abstrakte Basis-Klasse für alle Verarbeitungs-BlockEntities
 *
 * Features:
 * - Input/Output Inventar
 * - Progress-Tracking
 * - Produkttyp und Qualität
 * - Tick-basierte Verarbeitung
 * - NBT Save/Load
 * - Client-Server Synchronisation
 *
 * @param <T> Der spezifische ProductionType
 * @param <Q> Die spezifische ProductionQuality
 */
public abstract class AbstractProcessingBlockEntity<T extends ProductionType, Q extends ProductionQuality>
        extends BlockEntity {

    // Processing State
    protected ItemStack inputStack = ItemStack.EMPTY;
    protected ItemStack outputStack = ItemStack.EMPTY;
    protected int processingProgress = 0;
    protected T productionType;
    protected Q quality;

    // Performance-Optimierung: Tick-Throttling
    private int tickCounter = 0;
    private static final int TICK_INTERVAL = 5; // Alle 5 Ticks statt jeden Tick
    private int lastSyncedProgress = 0; // Für Progressive setChanged()

    // OPTIMIERUNG: Dirty-Flags für Slot-Synchronisation (verhindert unnötige ItemStack.copy())
    private boolean inputDirty = false;
    private boolean outputDirty = false;

    // Forge ItemHandler
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    /**
     * Erstellt den ItemHandler mit Input- und Output-Slots
     */
    protected void createItemHandler() {
        int slots = getSlotCount();
        itemHandler = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (isInputSlot(slot)) {
                    onInputChanged(slot);
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return getSlotCapacity(slot);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return AbstractProcessingBlockEntity.this.isItemValid(slot, stack);
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (canExtractFromSlot(slot)) {
                    return super.extractItem(slot, amount, simulate);
                }
                return ItemStack.EMPTY;
            }
        };

        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    /**
     * Tick-Methode - wird jedes Tick aufgerufen
     * Performance-Optimierung: Throttled processing
     */
    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        tickCounter++;

        // Nur alle TICK_INTERVAL Ticks verarbeiten (statt jeden Tick)
        if (tickCounter >= TICK_INTERVAL) {
            tickCounter = 0;

            // Wenn Input vorhanden und Output-Platz frei
            if (canProcess()) {
                processingProgress += TICK_INTERVAL; // Erhöhe um Intervall

                if (processingProgress >= getProcessingTime()) {
                    // Verarbeitung abschließen
                    finishProcessing();
                    processingProgress = 0;
                    lastSyncedProgress = 0;
                }

                // Nur alle 20 Ticks synchronisieren (nicht jeden Processing-Tick)
                if (Math.abs(processingProgress - lastSyncedProgress) >= 20) {
                    setChanged();
                    lastSyncedProgress = processingProgress;
                }
            } else {
                // Reset progress wenn Bedingungen nicht erfüllt
                if (processingProgress > 0) {
                    processingProgress = 0;
                    lastSyncedProgress = 0;
                    setChanged();
                }
            }
        }
    }

    /**
     * Überprüft ob verarbeitet werden kann
     */
    protected boolean canProcess() {
        if (inputStack.isEmpty()) {
            return false;
        }

        // Prüfe ob Output-Platz vorhanden
        ItemStack result = getProcessingResult();
        if (result.isEmpty()) {
            return false;
        }

        if (outputStack.isEmpty()) {
            return true;
        }

        // Kann zu existierendem Output hinzugefügt werden?
        return ItemStack.isSameItemSameTags(outputStack, result) &&
               outputStack.getCount() + result.getCount() <= outputStack.getMaxStackSize();
    }

    /**
     * Schließt die Verarbeitung ab
     */
    protected void finishProcessing() {
        ItemStack result = getProcessingResult();
        if (result.isEmpty()) {
            return;
        }

        // Reduziere Input
        inputStack.shrink(getInputConsumption());
        inputDirty = true; // OPTIMIERUNG: Markiere als dirty
        if (inputStack.getCount() <= 0) {
            inputStack = ItemStack.EMPTY;
            productionType = null;
            quality = null;
        }

        // Erhöhe Output
        if (outputStack.isEmpty()) {
            outputStack = result.copy();
        } else {
            outputStack.grow(result.getCount());
        }
        outputDirty = true; // OPTIMIERUNG: Markiere als dirty

        syncToHandler();
        setChanged();
    }

    /**
     * Synchronisiert interne Stacks mit ItemHandler
     * OPTIMIERT: Nur kopieren wenn Dirty-Flag gesetzt ist
     */
    protected void syncToHandler() {
        if (inputDirty) {
            itemHandler.setStackInSlot(0, inputStack.copy());
            inputDirty = false;
        }
        if (outputDirty) {
            itemHandler.setStackInSlot(1, outputStack.copy());
            outputDirty = false;
        }
    }

    /**
     * Wird aufgerufen wenn Input-Slot sich ändert
     */
    protected void onInputChanged(int slot) {
        ItemStack handlerInput = itemHandler.getStackInSlot(slot);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            extractProductInfoFromInput(inputStack);
            processingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            productionType = null;
            quality = null;
            processingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    // ========== ABSTRACT METHODS - MÜSSEN ÜBERSCHRIEBEN WERDEN ==========

    /**
     * @return Anzahl der Slots (meist 2: Input + Output)
     */
    protected abstract int getSlotCount();

    /**
     * @return Ob dieser Slot ein Input-Slot ist
     */
    protected abstract boolean isInputSlot(int slot);

    /**
     * @return Kapazität des Slots
     */
    protected abstract int getSlotCapacity(int slot);

    /**
     * @return Ob Item in diesem Slot gültig ist
     */
    protected abstract boolean isItemValid(int slot, ItemStack stack);

    /**
     * @return Ob aus diesem Slot extrahiert werden kann
     */
    protected abstract boolean canExtractFromSlot(int slot);

    /**
     * @return Verarbeitungszeit in Ticks
     */
    protected abstract int getProcessingTime();

    /**
     * @return Wie viel Input pro Verarbeitung verbraucht wird
     */
    protected int getInputConsumption() {
        return 1;
    }

    /**
     * @return Das Ergebnis der Verarbeitung
     */
    protected abstract ItemStack getProcessingResult();

    /**
     * Extrahiert Produkttyp und Qualität aus Input-Item
     */
    protected abstract void extractProductInfoFromInput(ItemStack stack);

    // ========== NBT SAVE/LOAD ==========

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("Input", inputStack.save(new CompoundTag()));
        tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", processingProgress);
        tag.put("Inventory", itemHandler.serializeNBT());
        saveExtraData(tag);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inputStack = ItemStack.of(tag.getCompound("Input"));
        outputStack = ItemStack.of(tag.getCompound("Output"));
        processingProgress = tag.getInt("Progress");
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        loadExtraData(tag);
    }

    /**
     * Override für zusätzliche NBT-Daten (Typ, Qualität, etc.)
     */
    protected void saveExtraData(CompoundTag tag) {}
    protected void loadExtraData(CompoundTag tag) {}

    // ========== CLIENT-SERVER SYNC ==========

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ========== CAPABILITIES ==========

    @Override
    public @NotNull <C> LazyOptional<C> getCapability(@NotNull Capability<C> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    // ========== GETTERS ==========

    public int getProcessingProgress() {
        return processingProgress;
    }

    public T getProductionType() {
        return productionType;
    }

    public Q getQuality() {
        return quality;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}
