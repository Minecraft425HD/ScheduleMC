package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.HoneyAgeLevel;
import de.rolandsw.schedulemc.honey.HoneyProcessingMethod;
import de.rolandsw.schedulemc.honey.HoneyQuality;
import de.rolandsw.schedulemc.honey.HoneyType;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
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
 * Bottling Station - Final bottling with processing method
 *
 * Input: 3 slots (honey source, glass jar, lid)
 * Output: 1 slot (honey jar)
 * Processing time: 300 ticks (15 seconds)
 * Allows processing method selection (LIQUID/CREAMED/CHUNK)
 * Final NBT data assembly
 */
public class BottlingStationBlockEntity extends BlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;

    private ItemStack honeyInput = ItemStack.EMPTY;
    private ItemStack jarInput = ItemStack.EMPTY;
    private ItemStack lidInput = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int processingProgress = 0;

    private HoneyType honeyType;
    private HoneyQuality quality;
    private HoneyAgeLevel ageLevel;
    private HoneyProcessingMethod processingMethod = HoneyProcessingMethod.LIQUID;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 300; // 15 seconds

    public BottlingStationBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.BOTTLING_STATION.get(), pos, state);
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
                return slot == 3 ? 16 : 16;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == HoneyItems.LIQUID_HONEY_BOTTLE.get()
                            || stack.getItem() == HoneyItems.CREAMED_HONEY.get()
                            || stack.getItem() == HoneyItems.FILTERED_HONEY_BUCKET.get();
                }
                if (slot == 1) {
                    return stack.getItem() == HoneyItems.GLASS_JAR.get()
                            || stack.getItem() == HoneyItems.GLASS_JAR_SMALL.get()
                            || stack.getItem() == HoneyItems.GLASS_JAR_LARGE.get();
                }
                if (slot == 2) {
                    return stack.getItem() == HoneyItems.JAR_LID.get()
                            || stack.getItem() == HoneyItems.JAR_LID_GOLD.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 3) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1 || slot == 2) && processingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerHoney = itemHandler.getStackInSlot(0);
        ItemStack handlerJar = itemHandler.getStackInSlot(1);
        ItemStack handlerLid = itemHandler.getStackInSlot(2);

        if (!handlerHoney.isEmpty() && honeyInput.isEmpty()) {
            honeyInput = handlerHoney.copy();
            CompoundTag tag = handlerHoney.getTag();
            if (tag != null) {
                if (tag.contains("HoneyType")) honeyType = HoneyType.valueOf(tag.getString("HoneyType"));
                if (tag.contains("Quality")) quality = HoneyQuality.valueOf(tag.getString("Quality"));
                if (tag.contains("AgeLevel")) ageLevel = HoneyAgeLevel.valueOf(tag.getString("AgeLevel"));
                else ageLevel = HoneyAgeLevel.FRESH;
            }

            // Determine processing method from input item
            if (handlerHoney.getItem() == HoneyItems.CREAMED_HONEY.get()) {
                processingMethod = HoneyProcessingMethod.CREAMED;
            } else {
                processingMethod = HoneyProcessingMethod.LIQUID;
            }

            processingProgress = 0;
        } else if (handlerHoney.isEmpty()) {
            honeyInput = ItemStack.EMPTY;
            honeyType = null;
            quality = null;
            ageLevel = null;
            processingProgress = 0;
        }

        if (!handlerJar.isEmpty() && jarInput.isEmpty()) {
            jarInput = handlerJar.copy();
        } else if (handlerJar.isEmpty()) {
            jarInput = ItemStack.EMPTY;
        }

        if (!handlerLid.isEmpty() && lidInput.isEmpty()) {
            lidInput = handlerLid.copy();
        } else if (handlerLid.isEmpty()) {
            lidInput = ItemStack.EMPTY;
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, honeyInput.copy());
        itemHandler.setStackInSlot(1, jarInput.copy());
        itemHandler.setStackInSlot(2, lidInput.copy());
        itemHandler.setStackInSlot(3, outputStack.copy());
    }

    public int getProcessingProgress() {
        return processingProgress;
    }

    public int getProcessingTime() {
        return PROCESSING_TIME;
    }

    public HoneyProcessingMethod getProcessingMethod() {
        return processingMethod;
    }

    public void setProcessingMethod(HoneyProcessingMethod method) {
        this.processingMethod = method;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!honeyInput.isEmpty() && !jarInput.isEmpty() && !lidInput.isEmpty() && outputStack.isEmpty()) {
            processingProgress++;

            if (processingProgress >= PROCESSING_TIME) {
                // Determine jar size based on input jar
                ItemStack honeyJar;
                if (jarInput.getItem() == HoneyItems.GLASS_JAR_SMALL.get()) {
                    honeyJar = new ItemStack(HoneyItems.HONEY_JAR_250G.get(), 1);
                } else if (jarInput.getItem() == HoneyItems.GLASS_JAR_LARGE.get()) {
                    honeyJar = new ItemStack(HoneyItems.HONEY_JAR_1KG.get(), 1);
                } else {
                    honeyJar = new ItemStack(HoneyItems.HONEY_JAR_500G.get(), 1);
                }

                // Assemble final NBT data
                CompoundTag tag = honeyJar.getOrCreateTag();
                if (honeyType != null) tag.putString("HoneyType", honeyType.name());
                if (quality != null) tag.putString("Quality", quality.name());
                if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());
                tag.putString("ProcessingMethod", processingMethod.name());

                // Add premium lid indicator
                if (lidInput.getItem() == HoneyItems.JAR_LID_GOLD.get()) {
                    tag.putBoolean("PremiumLid", true);
                }

                outputStack = honeyJar;

                honeyInput.shrink(1);
                jarInput.shrink(1);
                lidInput.shrink(1);

                if (honeyInput.isEmpty()) {
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
        return !honeyInput.isEmpty() && !jarInput.isEmpty() && !lidInput.isEmpty() && outputStack.isEmpty();
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
        if (!honeyInput.isEmpty()) tag.put("HoneyInput", honeyInput.save(new CompoundTag()));
        if (!jarInput.isEmpty()) tag.put("JarInput", jarInput.save(new CompoundTag()));
        if (!lidInput.isEmpty()) tag.put("LidInput", lidInput.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", processingProgress);
        if (honeyType != null) tag.putString("HoneyType", honeyType.name());
        if (quality != null) tag.putString("Quality", quality.name());
        if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", processingMethod.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        honeyInput = tag.contains("HoneyInput") ? ItemStack.of(tag.getCompound("HoneyInput")) : ItemStack.EMPTY;
        jarInput = tag.contains("JarInput") ? ItemStack.of(tag.getCompound("JarInput")) : ItemStack.EMPTY;
        lidInput = tag.contains("LidInput") ? ItemStack.of(tag.getCompound("LidInput")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        processingProgress = tag.getInt("Progress");
        if (tag.contains("HoneyType")) honeyType = HoneyType.valueOf(tag.getString("HoneyType"));
        if (tag.contains("Quality")) quality = HoneyQuality.valueOf(tag.getString("Quality"));
        if (tag.contains("AgeLevel")) ageLevel = HoneyAgeLevel.valueOf(tag.getString("AgeLevel"));
        if (tag.contains("ProcessingMethod")) {
            processingMethod = HoneyProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
        } else {
            processingMethod = HoneyProcessingMethod.LIQUID;
        }
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
