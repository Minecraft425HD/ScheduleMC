package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeRoastLevel;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.coffee.items.GreenCoffeeBeanItem;
import de.rolandsw.schedulemc.coffee.items.RoastedCoffeeBeanItem;
import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Abstrakte Basisklasse für Kaffee-Röster
 * Röstet grüne Kaffeebohnen zu verschiedenen Röstgraden
 */
public abstract class AbstractCoffeeRoasterBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Stacks
    private ItemStack inputStack = ItemStack.EMPTY;  // Green Coffee Beans
    private ItemStack outputStack = ItemStack.EMPTY; // Roasted Coffee Beans
    private int roastingProgress = 0;
    private CoffeeType coffeeType;
    private CoffeeQuality quality;
    private CoffeeRoastLevel selectedRoastLevel = CoffeeRoastLevel.MEDIUM; // Player-wählbar

    protected AbstractCoffeeRoasterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();  // NOPMD
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
                return slot == 0 && stack.getItem() instanceof GreenCoffeeBeanItem;
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

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            coffeeType = CoffeeItems.getTypeFromGreenBean(inputStack);
            quality = GreenCoffeeBeanItem.getQuality(inputStack);
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
            int totalTime = getRoastingTimePerBean() * inputStack.getCount();
            roastingProgress = Math.min(roastingProgress + 1, totalTime);

            if (roastingProgress >= totalTime) {
                // Röstung abgeschlossen
                outputStack = RoastedCoffeeBeanItem.create(
                    coffeeType,
                    quality,
                    selectedRoastLevel,
                    inputStack.getCount()
                );
                inputStack = ItemStack.EMPTY;
                roastingProgress = 0;
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

}
