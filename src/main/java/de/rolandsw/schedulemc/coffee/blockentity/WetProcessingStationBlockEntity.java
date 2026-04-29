package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.items.CoffeeCherryItem;
import de.rolandsw.schedulemc.coffee.menu.WetProcessingStationMenu;
import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

public class WetProcessingStationBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer, MenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WetProcessingStationBlockEntity.class);
    private boolean lastActiveState = false;
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int processingProgress = 0;
    private long lastGameTime = -1L;
    private ProcessingStage currentStage = ProcessingStage.IDLE;
    private CoffeeType coffeeType;
    private CoffeeQuality quality;
    public enum ProcessingStage {
        IDLE(0), PULPING(200), FERMENTATION(1200), WASHING(400);
        private final int duration;
        ProcessingStage(int duration) { this.duration = duration; }
        public int getDuration() { return duration; }
        public ProcessingStage next() {
            return switch (this) {
                case IDLE -> PULPING;
                case PULPING -> FERMENTATION;
                case FERMENTATION -> WASHING;
                case WASHING -> IDLE;
            };
        }
    }

    public WetProcessingStationBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.WET_PROCESSING_STATION.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(2) {
            @Override protected void onContentsChanged(int slot) { setChanged(); if (slot == 0) syncInputFromHandler(); }
            @Override public int getSlotLimit(int slot) { return slot == 0 ? 32 : 64; }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0 && stack.getItem() instanceof CoffeeCherryItem;
            }
            @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && currentStage == ProcessingStage.IDLE) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            coffeeType = CoffeeType.ARABICA;
            quality = CoffeeQuality.SEHR_GUT;
            currentStage = ProcessingStage.IDLE;
            processingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            coffeeType = null;
            quality = null;
            currentStage = ProcessingStage.IDLE;
            processingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public float getProcessingPercentage() {
        if (currentStage == ProcessingStage.IDLE) return 0;
        return (float) processingProgress / currentStage.getDuration();
    }

    public ProcessingStage getCurrentStage() { return currentStage; }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;
        boolean changed = false;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;
        if (ticksPassed == 0) return;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            if (currentStage == ProcessingStage.IDLE) {
                currentStage = ProcessingStage.PULPING;
                processingProgress = 0;
                changed = true;
            } else {
                int prevProgress = processingProgress;
                processingProgress = (int) Math.min((long) processingProgress + ticksPassed, currentStage.getDuration());

                if (processingProgress >= currentStage.getDuration()) {
                    ProcessingStage nextStage = currentStage.next();

                    if (nextStage == ProcessingStage.IDLE) {
                        if (quality == null) quality = CoffeeQuality.SEHR_GUT;
                        CoffeeQuality finalQuality = quality.upgrade();  // NOPMD
                        int beanCount = inputStack.getCount() * 2;
                        outputStack = new ItemStack(de.rolandsw.schedulemc.coffee.items.CoffeeItems.GREEN_ARABICA_BEANS.get(), beanCount);
                        inputStack = ItemStack.EMPTY;
                        currentStage = ProcessingStage.IDLE;
                        processingProgress = 0;
                    } else {
                        currentStage = nextStage;
                        processingProgress = 0;
                    }
                    changed = true;
                } else if (processingProgress / 20 > prevProgress / 20) {
                    changed = true;
                }
            }
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

    @Override public boolean isActivelyConsuming() {
        return !inputStack.isEmpty() && outputStack.isEmpty() && currentStage != ProcessingStage.IDLE;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputStack.isEmpty()) tag.put("Input", inputStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", processingProgress);
        tag.putLong("LastGameTime", lastGameTime);
        tag.putString("Stage", currentStage.name());
        if (coffeeType != null) tag.putString("CoffeeType", coffeeType.name());
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        processingProgress = tag.getInt("Progress");
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;
        if (tag.contains("Stage")) {
            try { currentStage = ProcessingStage.valueOf(tag.getString("Stage")); }
            catch (IllegalArgumentException e) { currentStage = ProcessingStage.IDLE; }
        } else { currentStage = ProcessingStage.IDLE; }
        if (tag.contains("CoffeeType")) {
            try { coffeeType = CoffeeType.valueOf(tag.getString("CoffeeType")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid CoffeeType '{}' in WetProcessingStationBlockEntity at {}", tag.getString("CoffeeType"), getBlockPos(), exception);
            }
        }
        if (tag.contains("Quality")) {
            try { quality = CoffeeQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid CoffeeQuality '{}' in WetProcessingStationBlockEntity at {}", tag.getString("Quality"), getBlockPos(), exception);
            }
        }
        syncToHandler();
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.wet_processing_station");
    }

    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new WetProcessingStationMenu(id, inv, this);
    }
}
