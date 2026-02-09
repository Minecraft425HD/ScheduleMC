package de.rolandsw.schedulemc.poppy.blockentity;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.items.MorphineItem;
import de.rolandsw.schedulemc.poppy.items.RawOpiumItem;
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
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Kochstation - kocht Rohopium zu Morphin-Base
 * Benötigt Wasser und Brennstoff
 */
public class KochstationBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;
    private static final int CAPACITY = 8;
    private static final int COOK_TIME = 200; // 10 Sekunden
    private static final int MAX_WATER = 1000;
    private static final int MAX_FUEL = 500;

    private ItemStack[] inputs = new ItemStack[CAPACITY];
    private ItemStack[] outputs = new ItemStack[CAPACITY];
    private int[] progress = new int[CAPACITY];
    private PoppyType[] types = new PoppyType[CAPACITY];
    private TobaccoQuality[] qualities = new TobaccoQuality[CAPACITY];
    private int waterLevel = 0;
    private int fuelLevel = 0;

    public KochstationBlockEntity(BlockPos pos, BlockState state) {
        super(PoppyBlockEntities.KOCHSTATION.get(), pos, state);
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

    public int getMaxWater() {
        return MAX_WATER;
    }

    public int getMaxFuel() {
        return MAX_FUEL;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public void addWater(int amount) {
        waterLevel = Math.min(waterLevel + amount, MAX_WATER);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void addFuel(int amount) {
        fuelLevel = Math.min(fuelLevel + amount, MAX_FUEL);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean addOpium(ItemStack stack) {
        if (!(stack.getItem() instanceof RawOpiumItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                types[i] = RawOpiumItem.getType(stack);
                qualities[i] = RawOpiumItem.getQuality(stack);
                progress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllMorphine() {
        int totalCount = 0;
        PoppyType type = null;
        TobaccoQuality quality = null;

        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = MorphineItem.getType(outputs[i]);
                    quality = MorphineItem.getQuality(outputs[i]);
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
        return totalCount > 0 ? MorphineItem.create(type, quality, totalCount) : ItemStack.EMPTY;
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
                totalProgress += (float) progress[i] / COOK_TIME;
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // Prüfe Wasser und Brennstoff
        if (waterLevel < 1 || fuelLevel < 1) {
            return; // Pausiere
        }

        boolean changed = false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                progress[i]++;

                // Verbrauche Ressourcen
                if (progress[i] % 20 == 0) {
                    waterLevel = Math.max(0, waterLevel - 1);
                    fuelLevel = Math.max(0, fuelLevel - 1);
                }

                if (progress[i] >= COOK_TIME) {
                    // 1 Rohopium = 1 Morphin (Qualität bleibt)
                    outputs[i] = MorphineItem.create(types[i], qualities[i], 1);
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

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn Wasser und Brennstoff vorhanden und Inputs zu verarbeiten sind
        if (waterLevel < 1 || fuelLevel < 1) return false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("WaterLevel", waterLevel);
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

        waterLevel = tag.getInt("WaterLevel");
        fuelLevel = tag.getInt("FuelLevel");

        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            progress[i] = tag.getInt("Progress" + i);

            if (tag.contains("Type" + i)) {
                try { types[i] = PoppyType.valueOf(tag.getString("Type" + i)); }
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
