package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.items.CoffeeCherryItem;
import de.rolandsw.schedulemc.coffee.items.GreenCoffeeBeanItem;
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
 * Abstrakte Basisklasse für Kaffee-Trocknungsschalen
 * Trocknet Kaffeekirschen in der Sonne (Dry Process)
 */
public abstract class AbstractCoffeeDryingTrayBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Stacks
    private ItemStack inputStack = ItemStack.EMPTY;  // Coffee Cherries
    private ItemStack outputStack = ItemStack.EMPTY; // Green Coffee Beans
    private int dryingProgress = 0;
    private CoffeeType coffeeType;
    private CoffeeQuality quality;

    // ItemHandler für GUI (Slot 0 = Input, Slot 1 = Output)
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractCoffeeDryingTrayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    /**
     * Maximale Kapazität (Kirschen)
     */
    protected abstract int getCapacity();

    /**
     * Trocknungszeit pro Kirsche in Ticks
     */
    protected abstract int getDryingTimePerCherry();

    private void createItemHandler() {
        int maxCherries = getCapacity();
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
                return slot == 0 ? maxCherries : 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() instanceof CoffeeCherryItem;
                }
                return false; // Output ist read-only
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
            // Note: CoffeeCherries would need NBT tracking from harvest to preserve variety/quality
            // For now using defaults - proper implementation needs CoffeePlantBlock harvest tracking
            coffeeType = CoffeeType.ARABICA;
            quality = CoffeeQuality.GOOD;
            dryingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            coffeeType = null;
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

    public boolean addCherries(ItemStack stack) {
        if (!(stack.getItem() instanceof CoffeeCherryItem)) {
            return false;
        }

        if (inputStack.isEmpty() && outputStack.isEmpty()) {
            inputStack = stack.copy();
            inputStack.setCount(Math.min(stack.getCount(), getCapacity()));
            // Note: Type & quality would be extracted from NBT if CoffeeCherries tracked harvest data
            coffeeType = CoffeeType.ARABICA; // Default
            quality = CoffeeQuality.GOOD; // Default
            dryingProgress = 0;
            syncToHandler();
            setChanged();
            return true;
        } else if (!inputStack.isEmpty() && inputStack.getCount() < getCapacity() && outputStack.isEmpty()) {
            int canAdd = Math.min(stack.getCount(), getCapacity() - inputStack.getCount());
            inputStack.grow(canAdd);
            syncToHandler();
            setChanged();
            return true;
        }
        return false;
    }

    public ItemStack extractAllBeans() {
        if (!outputStack.isEmpty()) {
            ItemStack result = outputStack.copy();
            outputStack = ItemStack.EMPTY;
            inputStack = ItemStack.EMPTY;
            dryingProgress = 0;
            coffeeType = null;
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

    public float getDryingPercentage() {
        if (inputStack.isEmpty()) return 0;
        int totalTime = getDryingTimePerCherry() * inputStack.getCount();
        return (float) dryingProgress / totalTime;
    }

    public int getDryingProgressValue() {
        return dryingProgress;
    }

    public int getTotalDryingTime() {
        return getDryingTimePerCherry() * Math.max(1, inputStack.getCount());
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            dryingProgress++;

            int totalTime = getDryingTimePerCherry() * inputStack.getCount();
            if (dryingProgress >= totalTime) {
                // Trocknung abgeschlossen
                // 2 Bohnen pro Kirsche
                int beanCount = inputStack.getCount() * 2;
                outputStack = new ItemStack(
                    de.rolandsw.schedulemc.coffee.items.CoffeeItems.GREEN_ARABICA_BEANS.get(),
                    beanCount
                );
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

        // Utility-Status
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
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

        if (coffeeType != null) {
            tag.putString("CoffeeType", coffeeType.name());
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

        if (tag.contains("CoffeeType")) {
            try { coffeeType = CoffeeType.valueOf(tag.getString("CoffeeType")); }
            catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("Quality")) {
            try { quality = CoffeeQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException ignored) {}
        }

        syncToHandler();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
