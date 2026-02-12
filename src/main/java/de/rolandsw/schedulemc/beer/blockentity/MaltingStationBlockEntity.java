package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.BeerQuality;
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.beer.menu.MaltingStationMenu;
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
 * Malting Station - Converts grain to malted grain
 *
 * Input: Raw grain (Barley, Wheat, Rye)
 * Output: Malted grain (same type, preserves quality)
 * Processing Time: 600 Ticks (30 seconds)
 */
public class MaltingStationBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int maltingProgress = 0;
    private BeerQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public MaltingStationBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.MALTING_STATION.get(), pos, state);
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
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == BeerItems.BARLEY.get() ||
                           stack.getItem() == BeerItems.WHEAT_GRAIN.get() ||
                           stack.getItem() == BeerItems.RYE.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && maltingProgress == 0) return super.extractItem(slot, amount, simulate);
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

            // Extract quality from NBT if present
            CompoundTag tag = handlerInput.getTag();
            if (tag != null && tag.contains("Quality")) {
                try { quality = BeerQuality.valueOf(tag.getString("Quality")); }
                catch (IllegalArgumentException ignored) {}
            } else {
                quality = BeerQuality.SCHLECHT;
            }

            maltingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            quality = null;
            maltingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getMaltingProgressValue() {
        return maltingProgress;
    }

    public int getTotalMaltingTime() {
        return 600; // 30 seconds
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            maltingProgress++;

            int totalTime = getTotalMaltingTime();
            if (maltingProgress >= totalTime) {
                // Malting complete: Grain → Malted Grain
                ItemStack maltedGrain = ItemStack.EMPTY;

                if (inputStack.getItem() == BeerItems.BARLEY.get()) {
                    maltedGrain = new ItemStack(BeerItems.MALTED_BARLEY.get(), inputStack.getCount());
                } else if (inputStack.getItem() == BeerItems.WHEAT_GRAIN.get()) {
                    maltedGrain = new ItemStack(BeerItems.MALTED_WHEAT.get(), inputStack.getCount());
                } else if (inputStack.getItem() == BeerItems.RYE.get()) {
                    maltedGrain = new ItemStack(BeerItems.MALTED_RYE.get(), inputStack.getCount());
                }

                // Preserve quality in NBT
                if (!maltedGrain.isEmpty() && quality != null) {
                    CompoundTag tag = maltedGrain.getOrCreateTag();
                    tag.putString("Quality", quality.name());
                }

                outputStack = maltedGrain;
                inputStack = ItemStack.EMPTY;
                maltingProgress = 0;
                changed = true;
            }

            if (maltingProgress % 20 == 0) changed = true;
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
        tag.putInt("Progress", maltingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        maltingProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) {
            try { quality = BeerQuality.valueOf(tag.getString("Quality")); }
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
        return Component.translatable("block.schedulemc.malting_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new MaltingStationMenu(id, inv, this);
    }
}
