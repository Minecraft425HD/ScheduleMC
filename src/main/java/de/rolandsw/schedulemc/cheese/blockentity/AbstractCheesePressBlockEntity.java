package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.cheese.CheeseAgeLevel;
import de.rolandsw.schedulemc.cheese.CheeseQuality;
import de.rolandsw.schedulemc.cheese.CheeseType;
import de.rolandsw.schedulemc.cheese.items.CheeseCurdItem;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.cheese.items.CheeseWheelItem;
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
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

/**
 * Abstrakte Basis für Käsepressen
 * Presst Käsebruch zu frischem Käselaib
 */
public abstract class AbstractCheesePressBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int pressingProgress = 0;
    private long lastGameTime = -1L;
    private CheeseQuality quality;

    protected AbstractCheesePressBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();  // NOPMD
    }

    /**
     * Kapazität in Items
     */
    protected abstract int getCapacity();

    /**
     * Presszeit pro Item in Ticks
     */
    protected abstract int getPressingTimePerItem();

    private void createItemHandler() {
        int maxItems = getCapacity();
        itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) syncInputFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 0 ? maxItems : 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0 && stack.getItem() == CheeseItems.CHEESE_CURD.get();
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && pressingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();

            // Extract quality from curd NBT
            quality = CheeseCurdItem.getQuality(handlerInput);

            pressingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            quality = null;
            pressingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getPressingProgressValue() {
        return pressingProgress;
    }

    public int getTotalPressingTime() {
        return getPressingTimePerItem() * Math.max(1, inputStack.getCount());
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
            int totalTime = getTotalPressingTime();
            int prevProgress = pressingProgress;
            pressingProgress = Math.min(pressingProgress + (int) ticksPassed, totalTime);

            if (pressingProgress >= totalTime) {
                // Pressing complete: Curd → Fresh Cheese Wheel
                // Weight: 0.25kg per curd item
                double weightKg = inputStack.getCount() * 0.25;

                ItemStack cheeseWheel = CheeseWheelItem.create(
                    CheeseType.GOUDA, // Default type, can be specialized later
                    quality != null ? quality : CheeseQuality.SCHLECHT,
                    CheeseAgeLevel.FRESH,
                    weightKg,
                    1
                );

                outputStack = cheeseWheel;
                inputStack = ItemStack.EMPTY;
                pressingProgress = 0;
                changed = true;
            }

            if (pressingProgress / 20 > prevProgress / 20) changed = true;
        }

        if (changed) {
            syncToHandler();
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
        return !inputStack.isEmpty() && outputStack.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputStack.isEmpty()) tag.put("Input", inputStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", pressingProgress);
        tag.putLong("LastGameTime", lastGameTime);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        pressingProgress = tag.getInt("Progress");
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;
        if (tag.contains("Quality")) {
            try { quality = CheeseQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException e) { quality = CheeseQuality.SCHLECHT; }
        }
        syncToHandler();
    }

}
