package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeRoastLevel;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.items.GreenCoffeeBeanItem;
import de.rolandsw.schedulemc.coffee.items.RoastedCoffeeBeanItem;
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
 * Abstrakte Basisklasse für Kaffee-Röster
 * Röstet grüne Kaffeebohnen zu verschiedenen Röstgraden
 */
public abstract class AbstractCoffeeRoasterBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Stacks
    private ItemStack inputStack = ItemStack.EMPTY;  // Green Coffee Beans
    private ItemStack outputStack = ItemStack.EMPTY; // Roasted Coffee Beans
    private int roastingProgress = 0;
    private CoffeeType coffeeType;
    private CoffeeQuality quality;
    private CoffeeRoastLevel selectedRoastLevel = CoffeeRoastLevel.MEDIUM; // Player-wählbar

    // ItemHandler (Slot 0 = Input, Slot 1 = Output)
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractCoffeeRoasterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    /**
     * Maximale Kapazität (Bohnen)
     */
    protected abstract int getCapacity();

    /**
     * Röstzeit pro Bohne in Ticks
     */
    protected abstract int getRoastingTimePerBean();

    private void createItemHandler() {
        int maxBeans = getCapacity();
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
                return slot == 0 ? maxBeans : 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() instanceof GreenCoffeeBeanItem;
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) {
                    return super.extractItem(slot, amount, simulate);
                }
                if (slot == 0 && roastingProgress == 0) {
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
            // Note: GreenCoffeeBeans don't have Type/Quality in NBT yet
            // These are determined by the plant's growing conditions and set during roasting
            // For now, use default values - proper implementation would track from harvest
            coffeeType = CoffeeType.ARABICA;
            quality = CoffeeQuality.GOOD;
            roastingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            coffeeType = null;
            quality = null;
            roastingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public void setRoastLevel(CoffeeRoastLevel level) {
        this.selectedRoastLevel = level;
        setChanged();
    }

    public CoffeeRoastLevel getSelectedRoastLevel() {
        return selectedRoastLevel;
    }

    public float getRoastingPercentage() {
        if (inputStack.isEmpty()) return 0;
        int totalTime = getRoastingTimePerBean() * inputStack.getCount();
        return (float) roastingProgress / totalTime;
    }

    public int getRoastingProgressValue() {
        return roastingProgress;
    }

    public int getTotalRoastingTime() {
        return getRoastingTimePerBean() * Math.max(1, inputStack.getCount());
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            roastingProgress++;

            int totalTime = getRoastingTimePerBean() * inputStack.getCount();
            if (roastingProgress >= totalTime) {
                // Röstung abgeschlossen
                outputStack = RoastedCoffeeBeanItem.create(
                    coffeeType,
                    quality,
                    selectedRoastLevel,
                    inputStack.getCount()
                );
                changed = true;
            }

            if (roastingProgress % 20 == 0) {
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
            tag.put("Input", inputStack.save(new CompoundTag()));
        }
        if (!outputStack.isEmpty()) {
            tag.put("Output", outputStack.save(new CompoundTag()));
        }

        tag.putInt("Progress", roastingProgress);
        tag.putString("RoastLevel", selectedRoastLevel.name());

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
        roastingProgress = tag.getInt("Progress");
        if (tag.contains("RoastLevel")) {
            try { selectedRoastLevel = CoffeeRoastLevel.valueOf(tag.getString("RoastLevel")); }
            catch (IllegalArgumentException ignored) { selectedRoastLevel = CoffeeRoastLevel.MEDIUM; }
        } else { selectedRoastLevel = CoffeeRoastLevel.MEDIUM; }

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
