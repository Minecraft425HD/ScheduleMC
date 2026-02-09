package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
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

/**
 * Abstrakte Basisklasse für Fermentierungsfässer
 * Eliminiert Code-Duplikation zwischen Small/Medium/Big Varianten
 */
public abstract class AbstractFermentationBarrelBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private int[] fermentationProgress;
    private TobaccoType[] tobaccoTypes;
    private TobaccoQuality[] qualities;

    // Performance-Optimierung: Tick-Throttling
    private int tickCounter = 0;
    private static final int TICK_INTERVAL = 5; // Alle 5 Ticks statt jeden Tick
    private int lastSyncTick = 0;

    protected AbstractFermentationBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        initArrays();
    }

    /**
     * Muss von Subklassen implementiert werden - gibt die Kapazität zurück
     */
    protected abstract int getCapacity();

    /**
     * Muss von Subklassen implementiert werden - gibt die Fermentierungszeit zurück
     */
    protected abstract int getFermentationTime();

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
                tobaccoTypes[i] = null;
                qualities[i] = null;
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

            // Nur aktive Slots verarbeiten
            for (int i = 0; i < getCapacity(); i++) {
                if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                    fermentationProgress[i] += TICK_INTERVAL; // Erhöhe um Intervall

                    if (fermentationProgress[i] >= getFermentationTime()) {
                        // Fermentierung abgeschlossen - mit 30% Chance auf Qualitätsverbesserung
                        TobaccoQuality finalQuality = calculateFinalQuality(qualities[i]);
                        outputs[i] = FermentedTobaccoLeafItem.create(tobaccoTypes[i], finalQuality, 1);
                        changed = true;
                        needsSync = true;
                    }

                    // Nur alle 40 Ticks (2 Sekunden) synchronisieren
                    if (fermentationProgress[i] - lastSyncTick >= 40) {
                        needsSync = true;
                    }
                }
            }

            if (changed) {
                setChanged();
            }

            if (needsSync) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                lastSyncTick = fermentationProgress[0]; // Track anhand erstem Slot
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
