package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem;
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
 * Großes Trocknungsgestell BlockEntity
 * Kapazität: Konfigurierbar (Standard: 10 Tabakblätter)
 */
public class BigDryingRackBlockEntity extends BlockEntity {

    private static int getCapacity() {
        return ModConfigHandler.TOBACCO.BIG_DRYING_RACK_CAPACITY.get();
    }

    private static int getDryingTime() {
        return ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME.get();
    }

    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private int[] dryingProgress;
    private TobaccoType[] tobaccoTypes;
    private TobaccoQuality[] qualities;

    public BigDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.BIG_DRYING_RACK.get(), pos, state);
        initArrays();
    }

    private void initArrays() {
        int capacity = getCapacity();
        inputs = new ItemStack[capacity];
        outputs = new ItemStack[capacity];
        dryingProgress = new int[capacity];
        tobaccoTypes = new TobaccoType[capacity];
        qualities = new TobaccoQuality[capacity];
        for (int i = 0; i < capacity; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            dryingProgress[i] = 0;
        }
    }

    public boolean addFreshLeaves(ItemStack stack) {
        if (!(stack.getItem() instanceof FreshTobaccoLeafItem)) {
            return false;
        }

        // Finde leeren Slot
        for (int i = 0; i < getCapacity(); i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                tobaccoTypes[i] = FreshTobaccoLeafItem.getType(stack);
                qualities[i] = FreshTobaccoLeafItem.getQuality(stack);
                dryingProgress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllDriedLeaves() {
        int totalCount = 0;
        TobaccoType type = null;
        TobaccoQuality quality = null;

        // Sammle alle fertigen Blätter
        for (int i = 0; i < getCapacity(); i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = DriedTobaccoLeafItem.getType(outputs[i]);
                    quality = DriedTobaccoLeafItem.getQuality(outputs[i]);
                }
                totalCount += outputs[i].getCount();
                outputs[i] = ItemStack.EMPTY;
                inputs[i] = ItemStack.EMPTY;
                dryingProgress[i] = 0;
                tobaccoTypes[i] = null;
                qualities[i] = null;
            }
        }

        setChanged();
        return totalCount > 0 ? DriedTobaccoLeafItem.create(type, quality, totalCount) : ItemStack.EMPTY;
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

    public float getAverageDryingPercentage() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) dryingProgress[i] / getDryingTime();
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                dryingProgress[i]++;

                if (dryingProgress[i] >= getDryingTime()) {
                    // Trocknung abgeschlossen
                    outputs[i] = DriedTobaccoLeafItem.create(tobaccoTypes[i], qualities[i], 1);
                    changed = true;
                }

                if (dryingProgress[i] % 20 == 0) {
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

            tag.putInt("Progress" + i, dryingProgress[i]);

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
            dryingProgress[i] = tag.getInt("Progress" + i);

            if (tag.contains("Type" + i)) {
                tobaccoTypes[i] = TobaccoType.valueOf(tag.getString("Type" + i));
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
