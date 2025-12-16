package de.rolandsw.schedulemc.poppy.blockentity;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.items.PoppyPodItem;
import de.rolandsw.schedulemc.poppy.items.RawOpiumItem;
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
 * Opium-Presse - presst Mohnkapseln automatisch zu Rohopium
 * Benötigt Diesel als Brennstoff, höherer Ertrag als Ritzmaschine
 */
public class OpiumPresseBlockEntity extends BlockEntity {

    private static final int CAPACITY = 16;
    private static final int PROCESS_TIME = 80; // 4 Sekunden (schneller)
    private static final int MAX_DIESEL = 1000;

    private ItemStack[] inputs = new ItemStack[CAPACITY];
    private ItemStack[] outputs = new ItemStack[CAPACITY];
    private int[] progress = new int[CAPACITY];
    private PoppyType[] types = new PoppyType[CAPACITY];
    private TobaccoQuality[] qualities = new TobaccoQuality[CAPACITY];
    private int dieselLevel = 0;

    public OpiumPresseBlockEntity(BlockPos pos, BlockState state) {
        super(PoppyBlockEntities.OPIUM_PRESSE.get(), pos, state);
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

    public int getMaxDiesel() {
        return MAX_DIESEL;
    }

    public int getDieselLevel() {
        return dieselLevel;
    }

    public void addDiesel(int amount) {
        dieselLevel = Math.min(dieselLevel + amount, MAX_DIESEL);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean addPod(ItemStack stack) {
        if (!(stack.getItem() instanceof PoppyPodItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                types[i] = PoppyPodItem.getType(stack);
                qualities[i] = PoppyPodItem.getQuality(stack);
                progress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllOpium() {
        int totalCount = 0;
        PoppyType type = null;
        TobaccoQuality quality = null;

        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = RawOpiumItem.getType(outputs[i]);
                    quality = RawOpiumItem.getQuality(outputs[i]);
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
        return totalCount > 0 ? RawOpiumItem.create(type, quality, totalCount) : ItemStack.EMPTY;
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
                totalProgress += (float) progress[i] / PROCESS_TIME;
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // Prüfe Diesel
        if (dieselLevel < 1) {
            return; // Kein Diesel - pausiere
        }

        boolean changed = false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                progress[i]++;

                // Verbrauche Diesel (1 pro 20 Ticks)
                if (progress[i] % 20 == 0) {
                    dieselLevel = Math.max(0, dieselLevel - 1);
                }

                if (progress[i] >= PROCESS_TIME) {
                    // Höherer Ertrag als Ritzmaschine!
                    // 1 Kapsel = 2-5 Rohopium (basierend auf Qualität)
                    int yield = switch (qualities[i]) {
                        case SCHLECHT -> 2;
                        case GUT -> 3;
                        case SEHR_GUT -> 4;
                        case LEGENDAER -> 5;
                    };
                    outputs[i] = RawOpiumItem.create(types[i], qualities[i], yield);
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("DieselLevel", dieselLevel);

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

        dieselLevel = tag.getInt("DieselLevel");

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
