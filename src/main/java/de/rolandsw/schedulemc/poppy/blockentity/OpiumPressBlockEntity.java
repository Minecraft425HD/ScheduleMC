package de.rolandsw.schedulemc.poppy.blockentity;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.items.PoppyPodItem;
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
 * Opium-Presse - presst Mohnkapseln automatisch zu Rohopium
 * Benötigt Diesel als Brennstoff, höherer Ertrag als ScoringMachine
 */
public class OpiumPressBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;
    private static final int CAPACITY = 16;
    private static final int PROCESS_TIME = 4800; // 4 Minuten
    private static final int MAX_DIESEL = 16000;

    private final ItemStack[] inputs = new ItemStack[CAPACITY];
    private final ItemStack[] outputs = new ItemStack[CAPACITY];
    private final int[] progress = new int[CAPACITY];
    private final PoppyType[] types = new PoppyType[CAPACITY];
    private final TobaccoQuality[] qualities = new TobaccoQuality[CAPACITY];
    private int dieselLevel = 0;
    private long lastGameTime = -1L;

    public OpiumPressBlockEntity(BlockPos pos, BlockState state) {
        super(PoppyBlockEntities.OPIUM_PRESS.get(), pos, state);
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
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
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
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
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

    public ItemStack getInputDisplayItem() {
        int count = 0;
        ItemStack template = null;
        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) {
                if (template == null) template = inputs[i].copy();
                count++;
            }
        }
        if (template == null) return ItemStack.EMPTY;
        template.setCount(count);
        return template;
    }

    public ItemStack getOutputDisplayItem() {
        int count = 0;
        ItemStack template = null;
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                if (template == null) template = outputs[i].copy();
                count += outputs[i].getCount();
            }
        }
        if (template == null) return ItemStack.EMPTY;
        template.setCount(count);
        return template;
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

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;

        if (ticksPassed == 0) return;

        boolean changed = false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                if (progress[i] == 0) {
                    if (dieselLevel < 1000) continue;
                    dieselLevel -= 1000;
                    changed = true;
                }
                progress[i] = (int) Math.min((long) progress[i] + ticksPassed, PROCESS_TIME);

                if (progress[i] >= PROCESS_TIME) {
                    TobaccoQuality quality = qualities[i] != null ? qualities[i] : TobaccoQuality.SCHLECHT;
                    int yield = switch (quality) {
                        case SCHLECHT -> 2;
                        case GUT -> 3;
                        case SEHR_GUT -> 4;
                        case LEGENDAER -> 5;
                    };
                    outputs[i] = RawOpiumItem.create(types[i], quality, yield);
                    inputs[i] = ItemStack.EMPTY;
                    progress[i] = 0;
                    changed = true;
                }
            }
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }

        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        if (dieselLevel < 1000) return false;

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

        tag.putInt("DieselLevel", dieselLevel);
        tag.putLong("LastGameTime", lastGameTime);

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
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;

        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            progress[i] = tag.getInt("Progress" + i);

            if (tag.contains("Type" + i)) {
                try { types[i] = PoppyType.valueOf(tag.getString("Type" + i)); }
                catch (IllegalArgumentException e) { types[i] = PoppyType.AFGHANISCH; }
            }
            if (tag.contains("Quality" + i)) {
                try { qualities[i] = TobaccoQuality.valueOf(tag.getString("Quality" + i)); }
                catch (IllegalArgumentException e) { qualities[i] = TobaccoQuality.SCHLECHT; }
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
