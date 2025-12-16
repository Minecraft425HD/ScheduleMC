package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.items.CocaPasteItem;
import de.rolandsw.schedulemc.coca.items.FreshCocaLeafItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
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
 * Abstrakte Basisklasse für Extraktionswannen
 * Verarbeitet Koka-Blätter + Diesel zu Koka-Paste
 */
public abstract class AbstractExtractionVatBlockEntity extends BlockEntity {

    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private int[] extractionProgress;
    private CocaType[] cocaTypes;
    private TobaccoQuality[] qualities;
    private int dieselLevel = 0;

    protected AbstractExtractionVatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        initArrays();
    }

    /**
     * Kapazität (Anzahl Blätter gleichzeitig)
     */
    protected abstract int getCapacity();

    /**
     * Extraktionszeit in Ticks
     */
    protected abstract int getExtractionTime();

    /**
     * Maximale Diesel-Menge in mB
     */
    public abstract int getMaxDiesel();

    /**
     * Diesel-Verbrauch pro Blatt in mB
     */
    protected int getDieselPerLeaf() {
        return 100; // 100 mB pro Blatt
    }

    private void initArrays() {
        int capacity = getCapacity();
        inputs = new ItemStack[capacity];
        outputs = new ItemStack[capacity];
        extractionProgress = new int[capacity];
        cocaTypes = new CocaType[capacity];
        qualities = new TobaccoQuality[capacity];
        for (int i = 0; i < capacity; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            extractionProgress[i] = 0;
        }
    }

    public boolean addFreshLeaves(ItemStack stack) {
        if (!(stack.getItem() instanceof FreshCocaLeafItem)) {
            return false;
        }

        // Finde leeren Slot
        for (int i = 0; i < getCapacity(); i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                cocaTypes[i] = FreshCocaLeafItem.getType(stack);
                qualities[i] = FreshCocaLeafItem.getQuality(stack);
                extractionProgress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllPaste() {
        int totalCount = 0;
        CocaType type = null;
        TobaccoQuality quality = null;

        for (int i = 0; i < getCapacity(); i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = CocaPasteItem.getType(outputs[i]);
                    quality = CocaPasteItem.getQuality(outputs[i]);
                }
                totalCount += outputs[i].getCount();
                outputs[i] = ItemStack.EMPTY;
                inputs[i] = ItemStack.EMPTY;
                extractionProgress[i] = 0;
                cocaTypes[i] = null;
                qualities[i] = null;
            }
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return totalCount > 0 ? CocaPasteItem.create(type, quality, totalCount) : ItemStack.EMPTY;
    }

    public void addDiesel(int amount) {
        dieselLevel = Math.min(dieselLevel + amount, getMaxDiesel());
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getDieselLevel() {
        return dieselLevel;
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

    public float getAverageExtractionPercentage() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) extractionProgress[i] / getExtractionTime();
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                // Prüfe ob genug Diesel vorhanden ist
                if (dieselLevel < getDieselPerLeaf()) {
                    continue; // Nicht genug Diesel - pausiere Extraktion
                }

                extractionProgress[i]++;

                if (extractionProgress[i] >= getExtractionTime()) {
                    // Extraktion abgeschlossen
                    outputs[i] = CocaPasteItem.create(cocaTypes[i], qualities[i], 1);
                    dieselLevel -= getDieselPerLeaf(); // Verbrauche Diesel
                    changed = true;
                }

                if (extractionProgress[i] % 20 == 0) {
                    changed = true;
                }
            }
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        int capacity = getCapacity();
        tag.putInt("Capacity", capacity);
        tag.putInt("DieselLevel", dieselLevel);

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

            tag.putInt("Progress" + i, extractionProgress[i]);

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

        dieselLevel = tag.getInt("DieselLevel");
        int savedCapacity = tag.contains("Capacity") ? tag.getInt("Capacity") : getCapacity();

        for (int i = 0; i < Math.min(savedCapacity, getCapacity()); i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            extractionProgress[i] = tag.getInt("Progress" + i);

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
