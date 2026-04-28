package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.items.CocaPasteItem;
import de.rolandsw.schedulemc.coca.items.FreshCocaLeafItem;
import de.rolandsw.schedulemc.production.ProductionSize;
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

public abstract class AbstractExtractionVatBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    private ItemStack[] inputs;
    private ItemStack[] outputs;
    private long[] slotStartTime;
    private CocaType[] cocaTypes;
    private TobaccoQuality[] qualities;
    private int dieselLevel = 0;

    protected final ProductionSize size;

    protected AbstractExtractionVatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, null);
    }

    protected AbstractExtractionVatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ProductionSize size) {
        super(type, pos, state);
        this.size = size;
        initArrays();  // NOPMD
    }

    public int getCapacity() {
        return size != null ? size.getCapacity() : getDefaultCapacity();
    }

    protected int getDefaultCapacity() {
        return 6;
    }

    protected abstract int getExtractionTime();

    public int getMaxDiesel() {
        return size != null ? size.getMaxFuel() : getDefaultMaxDiesel();
    }

    protected int getDefaultMaxDiesel() {
        return 1000;
    }

    protected int getDieselPerLeaf() {
        return 80;
    }

    private void initArrays() {
        int capacity = getCapacity();
        inputs = new ItemStack[capacity];
        outputs = new ItemStack[capacity];
        slotStartTime = new long[capacity];
        cocaTypes = new CocaType[capacity];
        qualities = new TobaccoQuality[capacity];
        for (int i = 0; i < capacity; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            slotStartTime[i] = -1L;
        }
    }

    public boolean addFreshLeaves(ItemStack stack) {
        if (!(stack.getItem() instanceof FreshCocaLeafItem)) {
            return false;
        }

        for (int i = 0; i < getCapacity(); i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                cocaTypes[i] = FreshCocaLeafItem.getType(stack);
                qualities[i] = FreshCocaLeafItem.getQuality(stack);
                slotStartTime[i] = -1L;
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
                slotStartTime[i] = -1L;
                cocaTypes[i] = null;
                qualities[i] = null;
            }
        }

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        return totalCount > 0 ? CocaPasteItem.create(type, quality, totalCount) : ItemStack.EMPTY;
    }

    public void addDiesel(int amount) {
        dieselLevel = Math.min(dieselLevel + amount, getMaxDiesel());
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
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

    public ItemStack getInputDisplayItem() {
        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty()) {
                ItemStack display = inputs[i].copy();
                display.setCount(getInputCount());
                return display;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getOutputDisplayItem() {
        int count = 0;
        ItemStack template = null;
        for (int i = 0; i < getCapacity(); i++) {
            if (!outputs[i].isEmpty()) {
                if (template == null) template = outputs[i].copy();
                count += outputs[i].getCount();
            }
        }
        if (template == null) return ItemStack.EMPTY;
        template.setCount(count);
        return template;
    }

    public float getAverageExtractionPercentage() {
        if (level == null) return 0;
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                activeSlots++;
                if (slotStartTime[i] >= 0) {
                    long elapsed = level.getDayTime() - slotStartTime[i];
                    totalProgress += Math.min(1.0f, (float) elapsed / getExtractionTime());
                }
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                if (slotStartTime[i] < 0) {
                    if (dieselLevel < getDieselPerLeaf()) continue;
                    dieselLevel -= getDieselPerLeaf();
                    slotStartTime[i] = level.getDayTime();
                    changed = true;
                }
                long elapsed = level.getDayTime() - slotStartTime[i];
                if (elapsed >= getExtractionTime()) {
                    outputs[i] = CocaPasteItem.create(cocaTypes[i], qualities[i], 1);
                    inputs[i] = ItemStack.EMPTY;
                    slotStartTime[i] = -1L;
                    changed = true;
                } else if (elapsed % 20 == 0) {
                    changed = true;
                }
            }
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        for (int i = 0; i < getCapacity(); i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                if (slotStartTime[i] >= 0 || dieselLevel >= getDieselPerLeaf()) {
                    return true;
                }
            }
        }
        return false;
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

            tag.putLong("StartTime" + i, slotStartTime[i]);

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
            slotStartTime[i] = tag.contains("StartTime" + i) ? tag.getLong("StartTime" + i) : -1L;

            if (tag.contains("Type" + i)) {
                try { cocaTypes[i] = CocaType.valueOf(tag.getString("Type" + i)); }
                catch (IllegalArgumentException ex) { cocaTypes[i] = null; }
            }
            if (tag.contains("Quality" + i)) {
                try { qualities[i] = TobaccoQuality.valueOf(tag.getString("Quality" + i)); }
                catch (IllegalArgumentException ex) { qualities[i] = null; }
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
