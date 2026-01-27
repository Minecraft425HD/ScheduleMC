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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mash Tun - Converts malted grain + water to wort
 *
 * Input: Malted grain (slot 0) + Water bucket (slot 1)
 * Output: Wort bucket
 * Processing Time: 800 Ticks (40 seconds)
 * Quality: Can upgrade by 1 level (max GOOD)
 */
public class MashTunBlockEntity extends BlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;

    private ItemStack maltedGrainStack = ItemStack.EMPTY;
    private ItemStack waterBucketStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int mashingProgress = 0;
    private BeerQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public MashTunBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.MASH_TUN.get(), pos, state);
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
                if (slot == 0) {
                    return stack.getItem() == BeerItems.MALTED_BARLEY.get() ||
                           stack.getItem() == BeerItems.MALTED_WHEAT.get() ||
                           stack.getItem() == BeerItems.MALTED_RYE.get();
                }
                if (slot == 1) {
                    return stack.getItem() == Items.WATER_BUCKET;
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1) && mashingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerGrain = itemHandler.getStackInSlot(0);
        ItemStack handlerWater = itemHandler.getStackInSlot(1);

        if (!handlerGrain.isEmpty() && maltedGrainStack.isEmpty()) {
            maltedGrainStack = handlerGrain.copy();

            // Extract quality from NBT
            CompoundTag tag = handlerGrain.getTag();
            if (tag != null && tag.contains("Quality")) {
                quality = BeerQuality.valueOf(tag.getString("Quality"));
            } else {
                quality = BeerQuality.BASIC;
            }

            mashingProgress = 0;
        } else if (handlerGrain.isEmpty()) {
            maltedGrainStack = ItemStack.EMPTY;
            quality = null;
            mashingProgress = 0;
        } else {
            maltedGrainStack = handlerGrain.copy();
        }

        if (!handlerWater.isEmpty() && waterBucketStack.isEmpty()) {
            waterBucketStack = handlerWater.copy();
        } else if (handlerWater.isEmpty()) {
            waterBucketStack = ItemStack.EMPTY;
        } else {
            waterBucketStack = handlerWater.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, maltedGrainStack.copy());
        itemHandler.setStackInSlot(1, waterBucketStack.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
    }

    public int getMashingProgressValue() {
        return mashingProgress;
    }

    public int getTotalMashingTime() {
        return 800; // 40 seconds
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!maltedGrainStack.isEmpty() && !waterBucketStack.isEmpty() && outputStack.isEmpty()) {
            mashingProgress++;

            int totalTime = getTotalMashingTime();
            if (mashingProgress >= totalTime) {
                // Mashing complete: Malted Grain + Water → Wort
                ItemStack wortBucket = new ItemStack(BeerItems.WORT_BUCKET.get(), 1);

                // Quality upgrade: can improve by 1 level (max GOOD)
                BeerQuality outputQuality = quality;
                if (outputQuality != null && outputQuality.getLevel() < BeerQuality.GOOD.getLevel()) {
                    outputQuality = (BeerQuality) outputQuality.upgrade();
                }

                // Store quality in NBT
                if (outputQuality != null) {
                    CompoundTag tag = wortBucket.getOrCreateTag();
                    tag.putString("Quality", outputQuality.name());
                }

                outputStack = wortBucket;
                maltedGrainStack.shrink(1);
                waterBucketStack.shrink(1);

                // Return empty bucket
                if (waterBucketStack.isEmpty()) {
                    waterBucketStack = new ItemStack(Items.BUCKET);
                }

                mashingProgress = 0;
                changed = true;
            }

            if (mashingProgress % 20 == 0) changed = true;
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
        return !maltedGrainStack.isEmpty() && !waterBucketStack.isEmpty() && outputStack.isEmpty();
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
        if (!maltedGrainStack.isEmpty()) tag.put("MaltedGrain", maltedGrainStack.save(new CompoundTag()));
        if (!waterBucketStack.isEmpty()) tag.put("WaterBucket", waterBucketStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", mashingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        maltedGrainStack = tag.contains("MaltedGrain") ? ItemStack.of(tag.getCompound("MaltedGrain")) : ItemStack.EMPTY;
        waterBucketStack = tag.contains("WaterBucket") ? ItemStack.of(tag.getCompound("WaterBucket")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        mashingProgress = tag.getInt("Progress");
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
