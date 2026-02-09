package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.BeerQuality;
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
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
 * Abstract base class for Beer Fermentation Tanks
 * Converts unfermented beer + yeast to young beer
 *
 * Input: Unfermented beer (fermenting_beer) + Yeast
 * Output: Young beer (green_beer)
 * Processing Time: 2400 Ticks (120 seconds = 2 minutes) base
 * Quality: Preserved through fermentation
 * Different sizes: Small (1.0x), Medium (1.5x), Large (2.0x)
 */
public abstract class AbstractBeerFermentationTankBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack unfermentedBeerStack = ItemStack.EMPTY;
    private ItemStack yeastStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int fermentationProgress = 0;
    private BeerQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractBeerFermentationTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    /**
     * Capacity in items
     */
    protected abstract int getCapacity();

    /**
     * Speed multiplier (1.0x, 1.5x, 2.0x)
     */
    protected abstract double getSpeedMultiplier();

    private void createItemHandler() {
        int maxItems = getCapacity();
        itemHandler = new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0 || slot == 1) syncInputsFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 2 ? 64 : maxItems;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == BeerItems.FERMENTING_BEER.get();
                }
                if (slot == 1) {
                    return stack.getItem() == BeerItems.YEAST.get() ||
                           stack.getItem() == BeerItems.BREWING_YEAST.get() ||
                           stack.getItem() == BeerItems.LAGER_YEAST.get() ||
                           stack.getItem() == BeerItems.ALE_YEAST.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1) && fermentationProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerBeer = itemHandler.getStackInSlot(0);
        ItemStack handlerYeast = itemHandler.getStackInSlot(1);

        if (!handlerBeer.isEmpty() && unfermentedBeerStack.isEmpty()) {
            unfermentedBeerStack = handlerBeer.copy();

            // Extract quality from NBT
            CompoundTag tag = handlerBeer.getTag();
            if (tag != null && tag.contains("Quality")) {
                quality = BeerQuality.valueOf(tag.getString("Quality"));
            } else {
                quality = BeerQuality.BASIC;
            }

            fermentationProgress = 0;
        } else if (handlerBeer.isEmpty()) {
            unfermentedBeerStack = ItemStack.EMPTY;
            quality = null;
            fermentationProgress = 0;
        } else {
            unfermentedBeerStack = handlerBeer.copy();
        }

        if (!handlerYeast.isEmpty() && yeastStack.isEmpty()) {
            yeastStack = handlerYeast.copy();
        } else if (handlerYeast.isEmpty()) {
            yeastStack = ItemStack.EMPTY;
        } else {
            yeastStack = handlerYeast.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, unfermentedBeerStack.copy());
        itemHandler.setStackInSlot(1, yeastStack.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
    }

    public int getFermentationProgressValue() {
        return fermentationProgress;
    }

    public int getTotalFermentationTime() {
        double speed = getSpeedMultiplier();
        if (speed <= 0) speed = 1.0;
        return (int) (2400 / speed); // Base 120 seconds (2 minutes), adjusted by speed
    }

    public int getProgress() {
        return fermentationProgress;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!unfermentedBeerStack.isEmpty() && !yeastStack.isEmpty() && outputStack.isEmpty()) {
            fermentationProgress++;

            int totalTime = getTotalFermentationTime();
            if (fermentationProgress >= totalTime) {
                // Fermentation complete: Unfermented Beer + Yeast → Young Beer
                ItemStack youngBeer = new ItemStack(BeerItems.GREEN_BEER.get(), 1);

                // Preserve quality through fermentation
                if (quality != null) {
                    CompoundTag tag = youngBeer.getOrCreateTag();
                    tag.putString("Quality", quality.name());
                }

                outputStack = youngBeer;
                unfermentedBeerStack.shrink(1);
                yeastStack.shrink(1);

                fermentationProgress = 0;
                changed = true;
            }

            if (fermentationProgress % 20 == 0) changed = true;
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
        return !unfermentedBeerStack.isEmpty() && !yeastStack.isEmpty() && outputStack.isEmpty();
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
        if (!unfermentedBeerStack.isEmpty()) tag.put("UnfermentedBeer", unfermentedBeerStack.save(new CompoundTag()));
        if (!yeastStack.isEmpty()) tag.put("Yeast", yeastStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", fermentationProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        unfermentedBeerStack = tag.contains("UnfermentedBeer") ? ItemStack.of(tag.getCompound("UnfermentedBeer")) : ItemStack.EMPTY;
        yeastStack = tag.contains("Yeast") ? ItemStack.of(tag.getCompound("Yeast")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        fermentationProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) quality = BeerQuality.valueOf(tag.getString("Quality"));
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
