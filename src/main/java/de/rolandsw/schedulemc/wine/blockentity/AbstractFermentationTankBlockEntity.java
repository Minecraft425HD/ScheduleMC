package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import de.rolandsw.schedulemc.wine.WineQuality;
import de.rolandsw.schedulemc.wine.WineType;
import de.rolandsw.schedulemc.wine.items.WineItems;
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
 * Abstrakte Basis für Fermentationstanks
 * Vergärt Traubensaft zu jungem Wein
 */
public abstract class AbstractFermentationTankBlockEntity extends BlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int fermentationProgress = 0;
    private WineType wineType;
    private WineQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractFermentationTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    protected abstract int getCapacity();
    protected abstract int getFermentationTimePerItem();

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
                if (slot == 0) {
                    return stack.getItem() == WineItems.RIESLING_JUICE.get() ||
                           stack.getItem() == WineItems.SPAETBURGUNDER_JUICE.get() ||
                           stack.getItem() == WineItems.CHARDONNAY_JUICE.get() ||
                           stack.getItem() == WineItems.MERLOT_JUICE.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && fermentationProgress == 0) return super.extractItem(slot, amount, simulate);
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
            // Determine wine type from juice
            if (handlerInput.getItem() == WineItems.RIESLING_JUICE.get()) wineType = WineType.RIESLING;
            else if (handlerInput.getItem() == WineItems.SPAETBURGUNDER_JUICE.get()) wineType = WineType.SPAETBURGUNDER;
            else if (handlerInput.getItem() == WineItems.CHARDONNAY_JUICE.get()) wineType = WineType.CHARDONNAY;
            else if (handlerInput.getItem() == WineItems.MERLOT_JUICE.get()) wineType = WineType.MERLOT;

            CompoundTag tag = handlerInput.getTag();
            if (tag != null && tag.contains("Quality")) {
                quality = WineQuality.valueOf(tag.getString("Quality"));
            } else {
                quality = WineQuality.LANDWEIN;
            }
            fermentationProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            wineType = null;
            quality = null;
            fermentationProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getFermentationProgressValue() {
        return fermentationProgress;
    }

    public int getTotalFermentationTime() {
        return getFermentationTimePerItem() * Math.max(1, inputStack.getCount());
    }

    public int getProgress() {
        return fermentationProgress;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            fermentationProgress++;

            int totalTime = getTotalFermentationTime();
            if (fermentationProgress >= totalTime) {
                // Fermentation complete: Juice → Young Wine
                ItemStack youngWine = new ItemStack(WineItems.YOUNG_WINE.get(), inputStack.getCount());

                // Store type and quality in NBT
                CompoundTag tag = youngWine.getOrCreateTag();
                if (wineType != null) tag.putString("WineType", wineType.name());
                if (quality != null) tag.putString("Quality", quality.name());

                outputStack = youngWine;
                fermentationProgress = 0;
                changed = true;
            }

            if (fermentationProgress % 20 == 0) changed = true;
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
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputStack.isEmpty()) tag.put("Input", inputStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", fermentationProgress);
        if (wineType != null) tag.putString("WineType", wineType.name());
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        fermentationProgress = tag.getInt("Progress");
        if (tag.contains("WineType")) wineType = WineType.valueOf(tag.getString("WineType"));
        if (tag.contains("Quality")) quality = WineQuality.valueOf(tag.getString("Quality"));
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
