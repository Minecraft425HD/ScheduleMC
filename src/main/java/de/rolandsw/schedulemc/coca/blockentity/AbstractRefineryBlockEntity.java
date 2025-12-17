package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.items.CocaineItem;
import de.rolandsw.schedulemc.coca.items.CocaPasteItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
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
 * Abstrakte Basisklasse für Raffinerien
 * Verarbeitet Koka-Paste zu Kokain (weiß) mit Brennstoff
 */
public abstract class AbstractRefineryBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private int[] refineryProgress;
    private CocaType[] cocaTypes;
    private TobaccoQuality[] qualities;
    private int fuelLevel = 0;

    protected AbstractRefineryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        initArrays();
    }

    /**
     * Kapazität (Anzahl Pasten gleichzeitig)
     */
    public abstract int getCapacity();

    /**
     * Raffinierungszeit in Ticks
     */
    protected abstract int getRefineryTime();

    /**
     * Maximaler Brennstoff-Level
     */
    public abstract int getMaxFuel();

    /**
     * Brennstoff-Verbrauch pro Paste
     */
    protected int getFuelPerPaste() {
        return 50;
    }

    /**
     * Chance auf Qualitätsverbesserung (0.0 - 1.0)
     */
    protected double getQualityUpgradeChance() {
        return 0.2; // 20%
    }

    private void initArrays() {
        int capacity = getCapacity();
        inputs = new ItemStack[capacity];
        outputs = new ItemStack[capacity];
        refineryProgress = new int[capacity];
        cocaTypes = new CocaType[capacity];
        qualities = new TobaccoQuality[capacity];
        for (int i = 0; i < capacity; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            refineryProgress[i] = 0;
        }
    }

    public boolean addPaste(ItemStack stack) {
        if (!(stack.getItem() instanceof CocaPasteItem)) {
            return false;
        }

        // Finde leeren Slot
        for (int i = 0; i < getCapacity(); i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                cocaTypes[i] = CocaPasteItem.getType(stack);
                qualities[i] = CocaPasteItem.getQuality(stack);
                refineryProgress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllCocaine() {
        int totalCount = 0;
        CocaType type = null;
        TobaccoQuality quality = null;

        for (int i = 0; i < getCapacity(); i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = CocaineItem.getType(outputs[i]);
                    quality = CocaineItem.getQuality(outputs[i]);
                }
                totalCount += outputs[i].getCount();
                outputs[i] = ItemStack.EMPTY;
                inputs[i] = ItemStack.EMPTY;
                refineryProgress[i] = 0;
                cocaTypes[i] = null;
                qualities[i] = null;
            }
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return totalCount > 0 ? CocaineItem.create(type, quality, totalCount) : ItemStack.EMPTY;
    }

    public void addFuel(int amount) {
        fuelLevel = Math.min(fuelLevel + amount, getMaxFuel());
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getFuelLevel() {
        return fuelLevel;
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

    public float getAverageRefineryPercentage() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) refineryProgress[i] / getRefineryTime();
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                // Prüfe ob genug Brennstoff vorhanden ist
                if (fuelLevel < 1) {
                    continue; // Kein Brennstoff - pausiere
                }

                refineryProgress[i]++;

                // Verbrauche Brennstoff (1 pro 20 Ticks = 1 Sekunde)
                if (refineryProgress[i] % 20 == 0) {
                    fuelLevel = Math.max(0, fuelLevel - 1);
                }

                if (refineryProgress[i] >= getRefineryTime()) {
                    // Raffinierung abgeschlossen - mit Chance auf Qualitätsverbesserung
                    TobaccoQuality finalQuality = calculateFinalQuality(qualities[i]);
                    outputs[i] = CocaineItem.create(cocaTypes[i], finalQuality, 1);
                    changed = true;
                }

                if (refineryProgress[i] % 20 == 0) {
                    changed = true;
                }
            }
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn Paste raffiniert wird und Brennstoff vorhanden ist
        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                if (fuelLevel >= 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private TobaccoQuality calculateFinalQuality(TobaccoQuality quality) {
        if (quality == TobaccoQuality.LEGENDAER) {
            return quality;
        }

        // Chance auf Upgrade
        if (level != null && level.random.nextFloat() < getQualityUpgradeChance()) {
            return quality.upgrade();
        }

        return quality;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        int capacity = getCapacity();
        tag.putInt("Capacity", capacity);
        tag.putInt("FuelLevel", fuelLevel);

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

            tag.putInt("Progress" + i, refineryProgress[i]);

            if (cocaTypes[i] != null) {
                tag.putString("Type" + i, cocaTypes[i].name());
            }
            if (qualities[i] != null) {
                tag.putString("Quality" + i, qualities[i].name());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (inputs == null) {
            initArrays();
        }

        fuelLevel = tag.getInt("FuelLevel");
        int savedCapacity = tag.contains("Capacity") ? tag.getInt("Capacity") : getCapacity();

        for (int i = 0; i < Math.min(savedCapacity, getCapacity()); i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            refineryProgress[i] = tag.getInt("Progress" + i);

            if (tag.contains("Type" + i)) {
                cocaTypes[i] = CocaType.valueOf(tag.getString("Type" + i));
            }
            if (tag.contains("Quality" + i)) {
                qualities[i] = TobaccoQuality.valueOf(tag.getString("Quality" + i));
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
