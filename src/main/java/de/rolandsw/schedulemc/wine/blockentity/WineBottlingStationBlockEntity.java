package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.wine.WineAgeLevel;
import de.rolandsw.schedulemc.wine.WineProcessingMethod;
import de.rolandsw.schedulemc.wine.WineQuality;
import de.rolandsw.schedulemc.wine.WineType;
import de.rolandsw.schedulemc.wine.items.WineBottleItem;
import de.rolandsw.schedulemc.wine.items.WineItems;
import de.rolandsw.schedulemc.wine.menu.WineBottlingStationMenu;
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

public class WineBottlingStationBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer, MenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WineBottlingStationBlockEntity.class);
    private ItemStack wineInput = ItemStack.EMPTY;
    private ItemStack bottleInput = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int bottlingProgress = 0;

    private long lastGameTime = -1L;
    private WineType wineType;
    private WineQuality quality;
    private WineAgeLevel ageLevel;
    private WineProcessingMethod processingMethod = WineProcessingMethod.DRY;
    private double bottleSize = 0.75; // Default 750ml

    public WineBottlingStationBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.WINE_BOTTLING_STATION.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0 || slot == 1) syncInputsFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 2 ? 64 : 16;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) return stack.getItem() == WineItems.YOUNG_WINE.get();
                if (slot == 1) return stack.getItem() == WineItems.EMPTY_WINE_BOTTLE.get();
                return false; // output slot not insertable
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if (bottlingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputsFromHandler() {
        ItemStack handlerWineInput = itemHandler.getStackInSlot(0);
        ItemStack handlerBottleInput = itemHandler.getStackInSlot(1);

        if (!handlerWineInput.isEmpty() && wineInput.isEmpty()) {
            wineInput = handlerWineInput.copy();
            CompoundTag tag = handlerWineInput.getTag();
            if (tag != null) {
                if (tag.contains("WineType")) {
                    try { wineType = WineType.valueOf(tag.getString("WineType")); }
                    catch (IllegalArgumentException exception) {
                        LOGGER.warn("Invalid WineType '{}' while syncing bottling input at {}", tag.getString("WineType"), getBlockPos(), exception);
                    }
                }
                if (tag.contains("Quality")) {
                    try { quality = WineQuality.valueOf(tag.getString("Quality")); }
                    catch (IllegalArgumentException e) { quality = WineQuality.SCHLECHT; }
                }
                if (tag.contains("AgeLevel")) {
                    try { ageLevel = WineAgeLevel.valueOf(tag.getString("AgeLevel")); }
                    catch (IllegalArgumentException e) { ageLevel = WineAgeLevel.YOUNG; }
                } else ageLevel = WineAgeLevel.YOUNG;
            }
            bottlingProgress = 0;
        } else if (handlerWineInput.isEmpty()) {
            wineInput = ItemStack.EMPTY;
            wineType = null;
            quality = null;
            ageLevel = null;
            bottlingProgress = 0;
        }

        if (!handlerBottleInput.isEmpty() && bottleInput.isEmpty()) {
            bottleInput = handlerBottleInput.copy();
            if (bottleInput.getItem() == WineItems.EMPTY_WINE_BOTTLE.get()) {
                bottleSize = 0.75;
            }
        } else if (handlerBottleInput.isEmpty()) {
            bottleInput = ItemStack.EMPTY;
            bottleSize = 0.75;
        }
    }

    public int getBottlingProgress() {
        return bottlingProgress;
    }

    public WineProcessingMethod getProcessingMethod() {
        return processingMethod;
    }

    public void setProcessingMethod(WineProcessingMethod method) {
        this.processingMethod = method;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;
        if (ticksPassed == 0) return;

        if (!wineInput.isEmpty() && !bottleInput.isEmpty() && output.isEmpty()) {
            int bottlingTime = 200; // 10 seconds per bottle
            int prevProgress = bottlingProgress;
            bottlingProgress = Math.min(bottlingProgress + (int) ticksPassed, bottlingTime);

            if (bottlingProgress >= bottlingTime) {
                ItemStack filledBottle = WineBottleItem.create(
                        wineType,
                        quality,
                        ageLevel != null ? ageLevel : WineAgeLevel.YOUNG,
                        processingMethod,
                        bottleSize,
                        1
                );

                output = filledBottle;
                itemHandler.setStackInSlot(2, output.copy());

                wineInput.shrink(1);
                bottleInput.shrink(1);

                if (wineInput.isEmpty()) {
                    itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                } else {
                    itemHandler.setStackInSlot(0, wineInput.copy());
                }

                if (bottleInput.isEmpty()) {
                    itemHandler.setStackInSlot(1, ItemStack.EMPTY);
                } else {
                    itemHandler.setStackInSlot(1, bottleInput.copy());
                }

                bottlingProgress = 0;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }

            if (bottlingProgress / 20 > prevProgress / 20) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        } else {
            if (bottlingProgress > 0) {
                bottlingProgress = 0;
                setChanged();
            }
        }

        ItemStack handlerOutput = itemHandler.getStackInSlot(2);
        if (handlerOutput.isEmpty() && !output.isEmpty()) {
            output = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        return !wineInput.isEmpty() && !bottleInput.isEmpty() && output.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        if (!wineInput.isEmpty()) tag.put("WineInput", wineInput.save(new CompoundTag()));
        if (!bottleInput.isEmpty()) tag.put("BottleInput", bottleInput.save(new CompoundTag()));
        if (!output.isEmpty()) tag.put("Output", output.save(new CompoundTag()));
        tag.putInt("BottlingProgress", bottlingProgress);
        tag.putLong("LastGameTime", lastGameTime);
        if (wineType != null) tag.putString("WineType", wineType.name());
        if (quality != null) tag.putString("Quality", quality.name());
        if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", processingMethod.name());
        tag.putDouble("BottleSize", bottleSize);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        wineInput = tag.contains("WineInput") ? ItemStack.of(tag.getCompound("WineInput")) : ItemStack.EMPTY;
        bottleInput = tag.contains("BottleInput") ? ItemStack.of(tag.getCompound("BottleInput")) : ItemStack.EMPTY;
        output = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        bottlingProgress = tag.getInt("BottlingProgress");
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;
        if (tag.contains("WineType")) {
            try { wineType = WineType.valueOf(tag.getString("WineType")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid WineType '{}' in bottling station at {}", tag.getString("WineType"), getBlockPos(), exception);
            }
        }
        if (tag.contains("Quality")) {
            try { quality = WineQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException e) { quality = WineQuality.SCHLECHT; }
        }
        if (tag.contains("AgeLevel")) {
            try { ageLevel = WineAgeLevel.valueOf(tag.getString("AgeLevel")); }
            catch (IllegalArgumentException e) { ageLevel = WineAgeLevel.YOUNG; }
        }
        if (tag.contains("ProcessingMethod")) {
            try { processingMethod = WineProcessingMethod.valueOf(tag.getString("ProcessingMethod")); }
            catch (IllegalArgumentException e) { processingMethod = WineProcessingMethod.DRY; }
        } else {
            processingMethod = WineProcessingMethod.DRY;
        }
        bottleSize = tag.contains("BottleSize") ? tag.getDouble("BottleSize") : 0.75;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.wine_bottling_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new WineBottlingStationMenu(id, inv, this);
    }
}
