package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstrakte Basisklasse für Trocknungsgestelle
 * Ein Input-Slot und ein Output-Slot mit variabler Kapazität
 */
public abstract class AbstractDryingRackBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Input und Output als einzelne Stacks
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int dryingProgress = 0;
    private TobaccoType tobaccoType;
    private TobaccoQuality quality;

    // ItemHandler für GUI-Zugriff (Slot 0 = Input, Slot 1 = Output)
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractDryingRackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    /**
     * Muss von Subklassen implementiert werden - gibt die maximale Blätteranzahl zurück
     */
    protected abstract int getCapacity();

    /**
     * Muss von Subklassen implementiert werden - gibt die Trocknungszeit pro Blatt zurück
     */
    protected abstract int getDryingTime();

    private void createItemHandler() {
        int maxLeaves = getCapacity();
        itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) {
                    syncInputFromHandler();
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 0 ? maxLeaves : 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() instanceof FreshTobaccoLeafItem;
                }
                return false; // Output slot ist read-only
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) {
                    return super.extractItem(slot, amount, simulate);
                }
                // Input kann nur extrahiert werden wenn noch nicht getrocknet
                if (slot == 0 && dryingProgress == 0) {
                    return super.extractItem(slot, amount, simulate);
                }
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            if (handlerInput.getItem() instanceof FreshTobaccoLeafItem) {
                tobaccoType = FreshTobaccoLeafItem.getType(handlerInput);
                quality = FreshTobaccoLeafItem.getQuality(handlerInput);
            }
            dryingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            tobaccoType = null;
            quality = null;
            dryingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public boolean addFreshLeaves(ItemStack stack) {
        if (!(stack.getItem() instanceof FreshTobaccoLeafItem)) {
            return false;
        }

        if (inputStack.isEmpty() && outputStack.isEmpty()) {
            inputStack = stack.copy();
            inputStack.setCount(Math.min(stack.getCount(), getCapacity()));
            tobaccoType = FreshTobaccoLeafItem.getType(stack);
            quality = FreshTobaccoLeafItem.getQuality(stack);
            dryingProgress = 0;
            syncToHandler();
            setChanged();
            return true;
        } else if (!inputStack.isEmpty() && inputStack.getCount() < getCapacity() && outputStack.isEmpty()) {
            // Blätter hinzufügen wenn gleicher Typ
            TobaccoType newType = FreshTobaccoLeafItem.getType(stack);
            TobaccoQuality newQuality = FreshTobaccoLeafItem.getQuality(stack);
            if (newType == tobaccoType && newQuality == quality) {
                int canAdd = Math.min(stack.getCount(), getCapacity() - inputStack.getCount());
                inputStack.grow(canAdd);
                syncToHandler();
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllDriedLeaves() {
        if (!outputStack.isEmpty()) {
            ItemStack result = outputStack.copy();
            outputStack = ItemStack.EMPTY;
            inputStack = ItemStack.EMPTY;
            dryingProgress = 0;
            tobaccoType = null;
            quality = null;
            syncToHandler();
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    public boolean isFull() {
        return inputStack.getCount() >= getCapacity() || !outputStack.isEmpty();
    }

    public boolean hasInput() {
        return !inputStack.isEmpty();
    }

    public boolean hasOutput() {
        return !outputStack.isEmpty();
    }

    public int getInputCount() {
        return inputStack.getCount();
    }

    public int getOutputCount() {
        return outputStack.getCount();
    }

    public float getAverageDryingPercentage() {
        if (inputStack.isEmpty()) return 0;
        int totalTime = getDryingTime() * inputStack.getCount();
        return (float) dryingProgress / totalTime;
    }

    public int getDryingProgressValue() {
        return dryingProgress;
    }

    public int getTotalDryingTime() {
        return getDryingTime() * Math.max(1, inputStack.getCount());
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            dryingProgress++;

            int totalTime = getDryingTime() * inputStack.getCount();
            if (dryingProgress >= totalTime) {
                // Trocknung abgeschlossen
                outputStack = DriedTobaccoLeafItem.create(tobaccoType, quality, inputStack.getCount());
                changed = true;
            }

            if (dryingProgress % 20 == 0) {
                changed = true;
            }
        }

        if (changed) {
            syncToHandler();
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
        // Aktiv wenn Input vorhanden ist und noch nicht fertig (Output leer)
        return !inputStack.isEmpty() && outputStack.isEmpty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!inputStack.isEmpty()) {
            CompoundTag inputTag = new CompoundTag();
            inputStack.save(inputTag);
            tag.put("Input", inputTag);
        }

        if (!outputStack.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputStack.save(outputTag);
            tag.put("Output", outputTag);
        }

        tag.putInt("Progress", dryingProgress);

        if (tobaccoType != null) {
            tag.putString("Type", tobaccoType.name());
        }
        if (quality != null) {
            tag.putString("Quality", quality.name());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (itemHandler == null) {
            createItemHandler();
        }

        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        dryingProgress = tag.getInt("Progress");

        if (tag.contains("Type")) {
            tobaccoType = TobaccoType.valueOf(tag.getString("Type"));
        }
        if (tag.contains("Quality")) {
            quality = TobaccoQuality.valueOf(tag.getString("Quality"));
        }

        syncToHandler();
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
