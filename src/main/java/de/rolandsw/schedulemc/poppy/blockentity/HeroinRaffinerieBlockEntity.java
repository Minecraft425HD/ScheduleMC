package de.rolandsw.schedulemc.poppy.blockentity;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.items.HeroinItem;
import de.rolandsw.schedulemc.poppy.items.MorphineItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Heroin-Raffinerie - raffiniert Morphin zu Heroin
 * Benötigt Brennstoff und Chemikalien (simuliert durch Brennstoff)
 */
public class HeroinRaffinerieBlockEntity extends BlockEntity {

    private static final int CAPACITY = 8;
    private static final int REFINE_TIME = 300; // 15 Sekunden
    private static final int MAX_FUEL = 800;

    private ItemStack[] inputs = new ItemStack[CAPACITY];
    private ItemStack[] outputs = new ItemStack[CAPACITY];
    private int[] progress = new int[CAPACITY];
    private PoppyType[] types = new PoppyType[CAPACITY];
    private TobaccoQuality[] qualities = new TobaccoQuality[CAPACITY];
    private int fuelLevel = 0;

    public HeroinRaffinerieBlockEntity(BlockPos pos, BlockState state) {
        super(PoppyBlockEntities.HEROIN_RAFFINERIE.get(), pos, state);
        initArrays();
    }

    private void initArrays() {
        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            progress[i] = 0;
        }
    }

    public int getCapacity() {
        return CAPACITY;
    }

    public int getMaxFuel() {
        return MAX_FUEL;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public void addFuel(int amount) {
        fuelLevel = Math.min(fuelLevel + amount, MAX_FUEL);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean addMorphine(ItemStack stack) {
        if (!(stack.getItem() instanceof MorphineItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                types[i] = MorphineItem.getType(stack);
                qualities[i] = MorphineItem.getQuality(stack);
                progress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllHeroin() {
        int totalCount = 0;
        PoppyType type = null;
        TobaccoQuality quality = null;

        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = HeroinItem.getType(outputs[i]);
                    quality = HeroinItem.getQuality(outputs[i]);
                }
                totalCount += outputs[i].getCount();
                outputs[i] = ItemStack.EMPTY;
                inputs[i] = ItemStack.EMPTY;
                progress[i] = 0;
                types[i] = null;
                qualities[i] = null;
            }
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return totalCount > 0 ? HeroinItem.create(type, quality, totalCount) : ItemStack.EMPTY;
    }

    public boolean isFull() {
        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasInput() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOutput() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getInputCount() {
        int count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) count++;
        }
        return count;
    }

    public int getOutputCount() {
        int count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) count++;
        }
        return count;
    }

    public float getAverageProgress() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) progress[i] / REFINE_TIME;
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    /**
     * Chance auf Qualitätsverbesserung während der Raffinierung
     */
    private double getQualityUpgradeChance() {
        return 0.2; // 20%
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (fuelLevel < 1) {
            return; // Pausiere
        }

        boolean changed = false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                progress[i]++;

                // Verbrauche Brennstoff
                if (progress[i] % 20 == 0) {
                    fuelLevel = Math.max(0, fuelLevel - 1);
                }

                if (progress[i] >= REFINE_TIME) {
                    // Chance auf Qualitätsverbesserung
                    TobaccoQuality finalQuality = calculateFinalQuality(qualities[i]);
                    outputs[i] = HeroinItem.create(types[i], finalQuality, 1);
                    changed = true;
                }

                if (progress[i] % 20 == 0) {
                    changed = true;
                }
            }
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private TobaccoQuality calculateFinalQuality(TobaccoQuality quality) {
        if (quality == TobaccoQuality.LEGENDAER) {
            return quality;
        }

        if (level != null && level.random.nextFloat() < getQualityUpgradeChance()) {
            return quality.upgrade();
        }

        return quality;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("FuelLevel", fuelLevel);

        for (int i = 0; i < CAPACITY; i++) {
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
            tag.putInt("Progress" + i, progress[i]);
            if (types[i] != null) {
                tag.putString("Type" + i, types[i].name());
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

        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            progress[i] = tag.getInt("Progress" + i);

            if (tag.contains("Type" + i)) {
                types[i] = PoppyType.valueOf(tag.getString("Type" + i));
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
