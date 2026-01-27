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
 * Abstract base class for Brew Kettles
 * Converts wort + hops to unfermented beer
 *
 * Input: Wort bucket (slot 0) + Hops (slot 1)
 * Output: Unfermented beer (fermenting_beer)
 * Processing Time: 1200 Ticks (60 seconds) base
 * Quality: Can upgrade by 1 level (max PREMIUM)
 * Different sizes: Small (1.0x), Medium (1.5x), Large (2.0x)
 */
public abstract class AbstractBrewKettleBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack wortStack = ItemStack.EMPTY;
    private ItemStack hopsStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int brewingProgress = 0;
    private BeerQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractBrewKettleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
                    return stack.getItem() == BeerItems.WORT_BUCKET.get();
                }
                if (slot == 1) {
                    return stack.getItem() == BeerItems.HOPS_CONE.get() ||
                           stack.getItem() == BeerItems.DRIED_HOPS.get() ||
                           stack.getItem() == BeerItems.HOP_EXTRACT.get() ||
                           stack.getItem() == BeerItems.HOP_PELLETS.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1) && brewingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerWort = itemHandler.getStackInSlot(0);
        ItemStack handlerHops = itemHandler.getStackInSlot(1);

        if (!handlerWort.isEmpty() && wortStack.isEmpty()) {
            wortStack = handlerWort.copy();

            // Extract quality from NBT
            CompoundTag tag = handlerWort.getTag();
            if (tag != null && tag.contains("Quality")) {
                quality = BeerQuality.valueOf(tag.getString("Quality"));
            } else {
                quality = BeerQuality.BASIC;
            }

            brewingProgress = 0;
        } else if (handlerWort.isEmpty()) {
            wortStack = ItemStack.EMPTY;
            quality = null;
            brewingProgress = 0;
        } else {
            wortStack = handlerWort.copy();
        }

        if (!handlerHops.isEmpty() && hopsStack.isEmpty()) {
            hopsStack = handlerHops.copy();
        } else if (handlerHops.isEmpty()) {
            hopsStack = ItemStack.EMPTY;
        } else {
            hopsStack = handlerHops.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, wortStack.copy());
        itemHandler.setStackInSlot(1, hopsStack.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
    }

    public int getBrewingProgressValue() {
        return brewingProgress;
    }

    public int getTotalBrewingTime() {
        return (int) (1200 / getSpeedMultiplier()); // Base 60 seconds, adjusted by speed
    }

    public int getProgress() {
        return brewingProgress;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!wortStack.isEmpty() && !hopsStack.isEmpty() && outputStack.isEmpty()) {
            brewingProgress++;

            int totalTime = getTotalBrewingTime();
            if (brewingProgress >= totalTime) {
                // Brewing complete: Wort + Hops → Unfermented Beer
                ItemStack unfermentedBeer = new ItemStack(BeerItems.FERMENTING_BEER.get(), 1);

                // Quality upgrade: can improve by 1 level (max PREMIUM)
                BeerQuality outputQuality = quality;
                if (outputQuality != null && outputQuality.getLevel() < BeerQuality.PREMIUM.getLevel()) {
                    outputQuality = (BeerQuality) outputQuality.upgrade();
                }

                // Store quality in NBT
                if (outputQuality != null) {
                    CompoundTag tag = unfermentedBeer.getOrCreateTag();
                    tag.putString("Quality", outputQuality.name());
                }

                outputStack = unfermentedBeer;
                wortStack.shrink(1);
                hopsStack.shrink(1);

                brewingProgress = 0;
                changed = true;
            }

            if (brewingProgress % 20 == 0) changed = true;
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
        return !wortStack.isEmpty() && !hopsStack.isEmpty() && outputStack.isEmpty();
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
        if (!wortStack.isEmpty()) tag.put("Wort", wortStack.save(new CompoundTag()));
        if (!hopsStack.isEmpty()) tag.put("Hops", hopsStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", brewingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        wortStack = tag.contains("Wort") ? ItemStack.of(tag.getCompound("Wort")) : ItemStack.EMPTY;
        hopsStack = tag.contains("Hops") ? ItemStack.of(tag.getCompound("Hops")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        brewingProgress = tag.getInt("Progress");
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
