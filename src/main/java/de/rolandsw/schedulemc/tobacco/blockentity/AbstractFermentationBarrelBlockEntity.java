package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.util.ModConstants;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Basisklasse für Fermentierungsfässer (Small/Medium/Big).
 * Kapazität und Fermentierungszeit werden per Supplier konfiguriert,
 * sodass keine gesonderten Subklassen pro Größe nötig sind.
 */
public class AbstractFermentationBarrelBlockEntity extends BlockEntity implements IUtilityConsumer {

    private final Supplier<Integer> capacitySupplier;
    private final Supplier<Integer> fermentationTimeSupplier;

    private boolean lastActiveState = false;

    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private int[] fermentationProgress;
    private TobaccoType[] tobaccoTypes;
    private TobaccoQuality[] qualities;

    // Performance-Optimierung: Tick-Throttling
    private int tickCounter = 0;
    private static final int TICK_INTERVAL = ModConstants.PROCESSING_TICK_INTERVAL;
    // Sync-Throttling: Netzwerk-Update nur alle PROCESSING_SYNC_CYCLE Verarbeitungszyklen (~40 Ticks)
    private int syncCycleCounter = 0;
    private static final int SYNC_EVERY_N_CYCLES = ModConstants.PROCESSING_SYNC_CYCLE;

    public AbstractFermentationBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                                  Supplier<Integer> capacitySupplier,
                                                  Supplier<Integer> fermentationTimeSupplier) {
        super(type, pos, state);
        this.capacitySupplier = capacitySupplier;
        this.fermentationTimeSupplier = fermentationTimeSupplier;
        initArrays();  // NOPMD
    }

    protected int getCapacity() {
        return capacitySupplier.get();
    }

    protected int getFermentationTime() {
        return fermentationTimeSupplier.get();
    }

    private void initArrays() {
        int capacity = getCapacity();
        inputs = new ItemStack[capacity];
        outputs = new ItemStack[capacity];
        fermentationProgress = new int[capacity];
        tobaccoTypes = new TobaccoType[capacity];
        qualities = new TobaccoQuality[capacity];
        for (int i = 0; i < capacity; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            fermentationProgress[i] = 0;
        }
    }

    public boolean addDriedLeaves(ItemStack stack) {
        if (!(stack.getItem() instanceof DriedTobaccoLeafItem)) {
            return false;
        }

        // Finde leeren Slot
        for (int i = 0; i < getCapacity(); i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                tobaccoTypes[i] = DriedTobaccoLeafItem.getType(stack);
                qualities[i] = DriedTobaccoLeafItem.getQuality(stack);
                fermentationProgress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllFermentedLeaves() {
        int totalCount = 0;
        TobaccoType type = null;
        TobaccoQuality quality = null;

        // Sammle alle fertigen Blätter
        for (int i = 0; i < getCapacity(); i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = FermentedTobaccoLeafItem.getType(outputs[i]);
                    quality = FermentedTobaccoLeafItem.getQuality(outputs[i]);
                }
                totalCount += outputs[i].getCount();
                outputs[i] = ItemStack.EMPTY;
                inputs[i] = ItemStack.EMPTY;
                fermentationProgress[i] = 0;
                tobaccoTypes[i] = null;  // NOPMD
                qualities[i] = null;  // NOPMD
            }
        }

        setChanged();
        return totalCount > 0 ? FermentedTobaccoLeafItem.create(type, quality, totalCount) : ItemStack.EMPTY;
    }

    public boolean isFull() {
        for (int i = 0; i < getCapacity(); i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasInput() {
        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOutput() {
        for (int i = 0; i < getCapacity(); i++) {
            if (!outputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getInputCount() {
        int count = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty()) count++;
        }
        return count;
    }

    public int getOutputCount() {
        int count = 0;
        for (int i = 0; i < getCapacity(); i++) {
            if (!outputs[i].isEmpty()) count++;
        }
        return count;
    }

    public float getAverageFermentationPercentage() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) fermentationProgress[i] / getFermentationTime();
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        // Performance-Optimierung: Nur alle TICK_INTERVAL Ticks verarbeiten
        if (tickCounter >= TICK_INTERVAL) {
            tickCounter = 0;

            boolean changed = false;
            boolean needsSync = false;

            // Periodischer Sync: alle SYNC_EVERY_N_CYCLES Verarbeitungszyklen (~40 Ticks)
            syncCycleCounter++;
            if (syncCycleCounter >= SYNC_EVERY_N_CYCLES) {
                syncCycleCounter = 0;
                needsSync = true;
            }

            // Nur aktive Slots verarbeiten
            for (int i = 0; i < getCapacity(); i++) {
                if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                    fermentationProgress[i] += TICK_INTERVAL; // Erhöhe um Intervall

                    if (fermentationProgress[i] >= getFermentationTime()) {
                        // Fermentierung abgeschlossen - mit 30% Chance auf Qualitätsverbesserung
                        TobaccoQuality finalQuality = calculateFinalQuality(qualities[i]);
                        outputs[i] = FermentedTobaccoLeafItem.create(tobaccoTypes[i], finalQuality, 1);
                        // BUG FIX: Input-Stack leeren, damit der Slot nicht im Mischzustand
                        // (inputs[i] != EMPTY && outputs[i] != EMPTY) verbleibt.
                        inputs[i] = ItemStack.EMPTY;
                        changed = true;
                        needsSync = true;
                    }
                }
            }

            if (changed) {
                setChanged();
            }

            if (needsSync) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }

            // Utility-Status nur bei Änderung melden
            boolean currentActive = isActivelyConsuming();
            if (currentActive != lastActiveState) {
                lastActiveState = currentActive;
                UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
            }
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn mindestens ein Slot Input hat und noch nicht fertig ist (Output leer)
        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private TobaccoQuality calculateFinalQuality(TobaccoQuality quality) {
        if (quality == TobaccoQuality.LEGENDAER) {
            return quality;
        }

        // 30% Chance auf Upgrade
        if (level != null && level.random.nextFloat() < 0.3f) {
            return quality.upgrade();
        }

        return quality;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        int capacity = getCapacity();
        tag.putInt("Capacity", capacity);

        for (int i = 0; i < capacity; i++) {
            if (!inputs[i].isEmpty()) {
                CompoundTag inputTag = new CompoundTag();
                inputs[i].save(inputTag);
                tag.put("Input" + i, inputTag);
            }

            if (!outputs[i].isEmpty()) {
                CompoundTag outputTag = new CompoundTag();
                outputs[i].save(outputTag);
                tag.put("Output" + i, outputTag);
            }

            tag.putInt("Progress" + i, fermentationProgress[i]);

            if (tobaccoTypes[i] != null) {
                tag.putString("Type" + i, tobaccoTypes[i].name());
            }
            if (qualities[i] != null) {
                tag.putString("Quality" + i, qualities[i].name());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // Initialisiere Arrays wenn nötig
        if (inputs == null) {
            initArrays();
        }

        int savedCapacity = tag.contains("Capacity") ? tag.getInt("Capacity") : getCapacity();

        for (int i = 0; i < Math.min(savedCapacity, getCapacity()); i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            fermentationProgress[i] = tag.getInt("Progress" + i);

            if (tag.contains("Type" + i)) {
                try { tobaccoTypes[i] = TobaccoType.valueOf(tag.getString("Type" + i)); }
                catch (IllegalArgumentException ignored) {}
            }
            if (tag.contains("Quality" + i)) {
                try { qualities[i] = TobaccoQuality.valueOf(tag.getString("Quality" + i)); }
                catch (IllegalArgumentException ignored) {}
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
}
