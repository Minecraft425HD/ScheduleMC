package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.items.CoffeeCherryItem;
import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.coffee.items.GreenCoffeeBeanItem;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

/**
 * Abstrakte Basisklasse für Kaffee-Trocknungsschalen
 * Trocknet Kaffeekirschen in der Sonne (Dry Process)
 */
public abstract class AbstractCoffeeDryingTrayBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCoffeeDryingTrayBlockEntity.class);

    private boolean lastActiveState = false;

    // Stacks
    private ItemStack inputStack = ItemStack.EMPTY;  // Coffee Cherries
    private ItemStack outputStack = ItemStack.EMPTY; // Green Coffee Beans
    private int dryingProgress = 0;
    private long lastGameTime = -1L;
    private CoffeeType coffeeType;
    private CoffeeQuality quality;

    protected AbstractCoffeeDryingTrayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();  // NOPMD
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
                return slot == 0 && stack.getItem() instanceof CoffeeCherryItem;
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

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            coffeeType = CoffeeItems.getTypeFromCherry(inputStack);
            quality = CoffeeCherryItem.getQuality(inputStack);
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
            quality = CoffeeQuality.SEHR_GUT; // Default
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
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        boolean changed = false;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;
        if (ticksPassed == 0) return;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            int totalTime = getDryingTimePerCherry() * inputStack.getCount();
            int prevProgress = dryingProgress;
            dryingProgress = (int) Math.min((long) dryingProgress + ticksPassed, totalTime);

            if (dryingProgress >= totalTime) {
                // Trocknung abgeschlossen
                // 2 Bohnen pro Kirsche
                int beanCount = inputStack.getCount() * 2;
                outputStack = new ItemStack(CoffeeItems.getGreenBeanForType(coffeeType != null ? coffeeType : CoffeeType.ARABICA), beanCount);
                GreenCoffeeBeanItem.withQuality(outputStack, quality != null ? quality : CoffeeQuality.GUT);
                inputStack = ItemStack.EMPTY;
                dryingProgress = 0;
                changed = true;
            } else if (dryingProgress / 20 > prevProgress / 20) {
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
        tag.putLong("LastGameTime", lastGameTime);

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
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;

        if (tag.contains("CoffeeType")) {
            try { coffeeType = CoffeeType.valueOf(tag.getString("CoffeeType")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid CoffeeType '{}' in AbstractCoffeeDryingTrayBlockEntity at {}", tag.getString("CoffeeType"), getBlockPos(), exception);
            }
        }
        if (tag.contains("Quality")) {
            try { quality = CoffeeQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid CoffeeQuality '{}' in AbstractCoffeeDryingTrayBlockEntity at {}", tag.getString("Quality"), getBlockPos(), exception);
            }
        }

        syncToHandler();
    }

}
