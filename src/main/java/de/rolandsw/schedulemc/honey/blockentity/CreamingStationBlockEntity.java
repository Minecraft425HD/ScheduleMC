package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.HoneyAgeLevel;
import de.rolandsw.schedulemc.honey.HoneyQuality;
import de.rolandsw.schedulemc.honey.HoneyType;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.honey.menu.CreamingStationMenu;
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

/**
 * Creaming Station - Creates creamed honey specifically
 *
 * Input: 1 slot (filtered honey bucket)
 * Output: 1 slot (creamed honey)
 * Processing time: 1000 ticks (50 seconds)
 * Maintains quality
 */
public class CreamingStationBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer, MenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreamingStationBlockEntity.class);
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int processingProgress = 0;
    private HoneyType honeyType;
    private HoneyQuality quality;
    private HoneyAgeLevel ageLevel;

    private static final int PROCESSING_TIME = 1000; // 50 seconds

    public CreamingStationBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.CREAMING_STATION.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) syncInputFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1; // Buckets
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0 && stack.getItem() == HoneyItems.FILTERED_HONEY_BUCKET.get();
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && processingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            CompoundTag tag = handlerInput.getTag();
            if (tag != null) {
                if (tag.contains("HoneyType")) {
                    try { honeyType = HoneyType.valueOf(tag.getString("HoneyType")); }
                    catch (IllegalArgumentException exception) {
                        LOGGER.warn("Invalid HoneyType '{}' in CreamingStationBlockEntity at {}", tag.getString("HoneyType"), getBlockPos(), exception);
                    }
                }
                if (tag.contains("Quality")) {
                    try { quality = HoneyQuality.valueOf(tag.getString("Quality")); }
                    catch (IllegalArgumentException e) { quality = HoneyQuality.SCHLECHT; }
                }
                if (tag.contains("AgeLevel")) {
                    try { ageLevel = HoneyAgeLevel.valueOf(tag.getString("AgeLevel")); }
                    catch (IllegalArgumentException e) { ageLevel = HoneyAgeLevel.FRESH; }
                } else ageLevel = HoneyAgeLevel.FRESH;
            }
            processingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            honeyType = null;
            quality = null;
            ageLevel = null;
            processingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getProcessingProgress() {
        return processingProgress;
    }

    public int getProcessingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            processingProgress = Math.min(processingProgress + 1, PROCESSING_TIME);

            if (processingProgress >= PROCESSING_TIME) {
                // Processing complete: Filtered Honey → Creamed Honey
                ItemStack creamedHoney = new ItemStack(HoneyItems.CREAMED_HONEY.get(), 1);
                CompoundTag tag = creamedHoney.getOrCreateTag();
                if (honeyType != null) tag.putString("HoneyType", honeyType.name());
                if (quality != null) tag.putString("Quality", quality.name());
                if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());

                outputStack = creamedHoney;

                inputStack.shrink(1);
                if (inputStack.isEmpty()) {
                    honeyType = null;
                    quality = null;
                    ageLevel = null;
                }

                processingProgress = 0;
                changed = true;
            }

            if (processingProgress % 20 == 0) changed = true;
        }

        if (changed) {
            syncToHandler();
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
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
        tag.putInt("Progress", processingProgress);
        if (honeyType != null) tag.putString("HoneyType", honeyType.name());
        if (quality != null) tag.putString("Quality", quality.name());
        if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        processingProgress = tag.getInt("Progress");
        if (tag.contains("HoneyType")) {
            try { honeyType = HoneyType.valueOf(tag.getString("HoneyType")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid HoneyType '{}' in CreamingStationBlockEntity at {}", tag.getString("HoneyType"), getBlockPos(), exception);
            }
        }
        if (tag.contains("Quality")) {
            try { quality = HoneyQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException e) { quality = HoneyQuality.SCHLECHT; }
        }
        if (tag.contains("AgeLevel")) {
            try { ageLevel = HoneyAgeLevel.valueOf(tag.getString("AgeLevel")); }
            catch (IllegalArgumentException e) { ageLevel = HoneyAgeLevel.FRESH; }
        }
        syncToHandler();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.creaming_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new CreamingStationMenu(id, inv, this);
    }
}
