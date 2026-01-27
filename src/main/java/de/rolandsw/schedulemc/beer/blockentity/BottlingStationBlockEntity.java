package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.BeerAgeLevel;
import de.rolandsw.schedulemc.beer.BeerProcessingMethod;
import de.rolandsw.schedulemc.beer.BeerQuality;
import de.rolandsw.schedulemc.beer.BeerType;
import de.rolandsw.schedulemc.beer.items.BeerBottleItem;
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
 * Bottling Station - Final beer bottling with processing method selection
 *
 * Input Slots:
 * - Slot 0: Beer source (conditioned_beer)
 * - Slot 1: Container (beer_bottle_empty, beer_can_empty, or beer_keg)
 * - Slot 2: Cap (bottle_cap or crown_cap)
 * Output Slot:
 * - Slot 3: Finished beer bottle with complete NBT data
 *
 * Processing Time: 300 Ticks (15 seconds)
 * Processing Method: DRAFT/BOTTLED/CANNED
 * Final assembly: Combines all beer attributes into final product
 */
public class BottlingStationBlockEntity extends BlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;

    private ItemStack beerSource = ItemStack.EMPTY;
    private ItemStack containerStack = ItemStack.EMPTY;
    private ItemStack capStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int bottlingProgress = 0;

    private BeerType beerType;
    private BeerQuality quality;
    private BeerAgeLevel ageLevel;
    private BeerProcessingMethod processingMethod = BeerProcessingMethod.BOTTLED;
    private double bottleSize = 0.5; // Default 500ml

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public BottlingStationBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.BEER_BOTTLING_STATION.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(4) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0 || slot == 1 || slot == 2) syncInputsFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 3 ? 64 : 16;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == BeerItems.CONDITIONED_BEER.get();
                }
                if (slot == 1) {
                    return stack.getItem() == BeerItems.BEER_BOTTLE_EMPTY.get() ||
                           stack.getItem() == BeerItems.BEER_CAN_EMPTY.get() ||
                           stack.getItem() == BeerItems.BEER_KEG.get();
                }
                if (slot == 2) {
                    return stack.getItem() == BeerItems.BOTTLE_CAP.get() ||
                           stack.getItem() == BeerItems.CROWN_CAP.get();
                }
                return slot == 3;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 3) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1 || slot == 2) && bottlingProgress == 0) {
                    return super.extractItem(slot, amount, simulate);
                }
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerBeer = itemHandler.getStackInSlot(0);
        ItemStack handlerContainer = itemHandler.getStackInSlot(1);
        ItemStack handlerCap = itemHandler.getStackInSlot(2);

        if (!handlerBeer.isEmpty() && beerSource.isEmpty()) {
            beerSource = handlerBeer.copy();

            // Extract beer data from NBT
            CompoundTag tag = handlerBeer.getTag();
            if (tag != null) {
                if (tag.contains("BeerType")) {
                    beerType = BeerType.valueOf(tag.getString("BeerType"));
                } else {
                    // Default beer type if not specified
                    beerType = BeerType.PILSNER;
                }

                if (tag.contains("Quality")) {
                    quality = BeerQuality.valueOf(tag.getString("Quality"));
                } else {
                    quality = BeerQuality.BASIC;
                }

                if (tag.contains("AgeLevel")) {
                    ageLevel = BeerAgeLevel.valueOf(tag.getString("AgeLevel"));
                } else {
                    ageLevel = BeerAgeLevel.YOUNG;
                }
            } else {
                beerType = BeerType.PILSNER;
                quality = BeerQuality.BASIC;
                ageLevel = BeerAgeLevel.YOUNG;
            }

            bottlingProgress = 0;
        } else if (handlerBeer.isEmpty()) {
            beerSource = ItemStack.EMPTY;
            beerType = null;
            quality = null;
            ageLevel = null;
            bottlingProgress = 0;
        } else {
            beerSource = handlerBeer.copy();
        }

        if (!handlerContainer.isEmpty() && containerStack.isEmpty()) {
            containerStack = handlerContainer.copy();

            // Determine processing method and bottle size from container
            if (containerStack.getItem() == BeerItems.BEER_BOTTLE_EMPTY.get()) {
                processingMethod = BeerProcessingMethod.BOTTLED;
                bottleSize = 0.5; // 500ml
            } else if (containerStack.getItem() == BeerItems.BEER_CAN_EMPTY.get()) {
                processingMethod = BeerProcessingMethod.CANNED;
                bottleSize = 0.33; // 330ml
            } else if (containerStack.getItem() == BeerItems.BEER_KEG.get()) {
                processingMethod = BeerProcessingMethod.DRAFT;
                bottleSize = 5.0; // 5L keg
            }
        } else if (handlerContainer.isEmpty()) {
            containerStack = ItemStack.EMPTY;
            processingMethod = BeerProcessingMethod.BOTTLED;
            bottleSize = 0.5;
        } else {
            containerStack = handlerContainer.copy();
        }

        if (!handlerCap.isEmpty() && capStack.isEmpty()) {
            capStack = handlerCap.copy();
        } else if (handlerCap.isEmpty()) {
            capStack = ItemStack.EMPTY;
        } else {
            capStack = handlerCap.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, beerSource.copy());
        itemHandler.setStackInSlot(1, containerStack.copy());
        itemHandler.setStackInSlot(2, capStack.copy());
        itemHandler.setStackInSlot(3, outputStack.copy());
    }

    public int getBottlingProgress() {
        return bottlingProgress;
    }

    public int getTotalBottlingTime() {
        return 300; // 15 seconds
    }

    public BeerProcessingMethod getProcessingMethod() {
        return processingMethod;
    }

    public void setProcessingMethod(BeerProcessingMethod method) {
        this.processingMethod = method;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        // Check if we have all required inputs and empty output
        if (!beerSource.isEmpty() && !containerStack.isEmpty() && !capStack.isEmpty() && outputStack.isEmpty()) {
            bottlingProgress++;

            int totalTime = getTotalBottlingTime();
            if (bottlingProgress >= totalTime) {
                // Bottling complete: Create final beer bottle with all NBT data
                ItemStack filledBeer = BeerBottleItem.create(
                    beerType != null ? beerType : BeerType.PILSNER,
                    quality != null ? quality : BeerQuality.BASIC,
                    ageLevel != null ? ageLevel : BeerAgeLevel.YOUNG,
                    processingMethod,
                    bottleSize,
                    1
                );

                outputStack = filledBeer;
                beerSource.shrink(1);
                containerStack.shrink(1);
                capStack.shrink(1);

                bottlingProgress = 0;
                changed = true;
            }

            if (bottlingProgress % 20 == 0) changed = true;
        } else {
            if (bottlingProgress > 0) {
                bottlingProgress = 0;
                changed = true;
            }
        }

        if (changed) {
            syncToHandler();
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // Clear output if removed
        ItemStack handlerOutput = itemHandler.getStackInSlot(3);
        if (handlerOutput.isEmpty() && !outputStack.isEmpty()) {
            outputStack = ItemStack.EMPTY;
        }

        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        return !beerSource.isEmpty() && !containerStack.isEmpty() && !capStack.isEmpty();
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
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        if (!beerSource.isEmpty()) tag.put("BeerSource", beerSource.save(new CompoundTag()));
        if (!containerStack.isEmpty()) tag.put("Container", containerStack.save(new CompoundTag()));
        if (!capStack.isEmpty()) tag.put("Cap", capStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("BottlingProgress", bottlingProgress);
        if (beerType != null) tag.putString("BeerType", beerType.name());
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
        beerSource = tag.contains("BeerSource") ? ItemStack.of(tag.getCompound("BeerSource")) : ItemStack.EMPTY;
        containerStack = tag.contains("Container") ? ItemStack.of(tag.getCompound("Container")) : ItemStack.EMPTY;
        capStack = tag.contains("Cap") ? ItemStack.of(tag.getCompound("Cap")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        bottlingProgress = tag.getInt("BottlingProgress");
        if (tag.contains("BeerType")) beerType = BeerType.valueOf(tag.getString("BeerType"));
        if (tag.contains("Quality")) quality = BeerQuality.valueOf(tag.getString("Quality"));
        if (tag.contains("AgeLevel")) ageLevel = BeerAgeLevel.valueOf(tag.getString("AgeLevel"));
        if (tag.contains("ProcessingMethod")) {
            processingMethod = BeerProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
        } else {
            processingMethod = BeerProcessingMethod.BOTTLED;
        }
        bottleSize = tag.contains("BottleSize") ? tag.getDouble("BottleSize") : 0.5;
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
