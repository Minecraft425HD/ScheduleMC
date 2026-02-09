package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.HoneyQuality;
import de.rolandsw.schedulemc.honey.HoneyType;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.honey.menu.HoneyExtractorMenu;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
 * Honey Extractor - Honeycomb → Raw Honey
 *
 * Input: 1 slot (raw honeycomb)
 * Output: 1 slot (raw honey bucket), 1 byproduct slot (beeswax)
 * Processing time: 400 ticks (20 seconds)
 * Preserves quality
 */
public class HoneyExtractorBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private ItemStack byproductStack = ItemStack.EMPTY;
    private int processingProgress = 0;
    private HoneyType honeyType;
    private HoneyQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 400; // 20 seconds

    public HoneyExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.HONEY_EXTRACTOR.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) syncInputFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 0 ? 64 : (slot == 1 ? 1 : 64);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == HoneyItems.RAW_HONEYCOMB.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1 || slot == 2) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && processingProgress == 0) return super.extractItem(slot, amount, simulate);
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
            CompoundTag tag = handlerInput.getTag();
            if (tag != null) {
                if (tag.contains("HoneyType")) {
                    try { honeyType = HoneyType.valueOf(tag.getString("HoneyType")); }
                    catch (IllegalArgumentException ignored) {}
                }
                if (tag.contains("Quality")) {
                    try { quality = HoneyQuality.valueOf(tag.getString("Quality")); }
                    catch (IllegalArgumentException ignored) {}
                }
            }
            processingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            honeyType = null;
            quality = null;
            processingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
        itemHandler.setStackInSlot(2, byproductStack.copy());
    }

    public int getProcessingProgress() {
        return processingProgress;
    }

    public int getProcessingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            processingProgress++;

            if (processingProgress >= PROCESSING_TIME) {
                // Processing complete: Honeycomb → Raw Honey + Beeswax
                ItemStack rawHoney = new ItemStack(HoneyItems.RAW_HONEY_BUCKET.get(), 1);
                CompoundTag tag = rawHoney.getOrCreateTag();
                if (honeyType != null) tag.putString("HoneyType", honeyType.name());
                if (quality != null) tag.putString("Quality", quality.name());

                ItemStack beeswax = new ItemStack(HoneyItems.BEESWAX.get(), 2);

                outputStack = rawHoney;
                byproductStack = beeswax;

                inputStack.shrink(4); // 4 honeycombs = 1 bucket
                if (inputStack.isEmpty()) {
                    honeyType = null;
                    quality = null;
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
        if (!byproductStack.isEmpty()) tag.put("Byproduct", byproductStack.save(new CompoundTag()));
        tag.putInt("Progress", processingProgress);
        if (honeyType != null) tag.putString("HoneyType", honeyType.name());
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        byproductStack = tag.contains("Byproduct") ? ItemStack.of(tag.getCompound("Byproduct")) : ItemStack.EMPTY;
        processingProgress = tag.getInt("Progress");
        if (tag.contains("HoneyType")) {
            try { honeyType = HoneyType.valueOf(tag.getString("HoneyType")); }
            catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("Quality")) {
            try { quality = HoneyQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException ignored) {}
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

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.honey_extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new HoneyExtractorMenu(id, inv, this);
    }
}
