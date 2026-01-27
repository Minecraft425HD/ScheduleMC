package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.cheese.items.CheeseItems;
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
 * Pasteurization Station - Erhitzt Rohmilch zur Keimtötung
 *
 * Input: Raw Milk Bucket
 * Output: Pasteurized Milk Bucket
 * Processing Time: 400 Ticks (20 Sekunden)
 */
public class PasteurizationStationBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int pasteurizationProgress = 0;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public PasteurizationStationBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.PASTEURIZATION_STATION.get(), pos, state);
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
                return 16;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == Items.MILK_BUCKET;
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && pasteurizationProgress == 0) return super.extractItem(slot, amount, simulate);
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
            pasteurizationProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            pasteurizationProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getPasteurizationProgressValue() {
        return pasteurizationProgress;
    }

    public int getTotalPasteurizationTime() {
        return 400; // 20 seconds per bucket
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            pasteurizationProgress++;

            if (pasteurizationProgress >= getTotalPasteurizationTime()) {
                // Pasteurization complete: Raw Milk → Pasteurized Milk
                ItemStack pasteurizedMilk = new ItemStack(CheeseItems.MILK_BUCKET.get(), inputStack.getCount());

                outputStack = pasteurizedMilk;
                pasteurizationProgress = 0;
                changed = true;
            }

            if (pasteurizationProgress % 20 == 0) changed = true;
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
        tag.putInt("Progress", pasteurizationProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        pasteurizationProgress = tag.getInt("Progress");
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
        return Component.translatable("block.schedulemc.pasteurization_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.cheese.menu.PasteurizationStationMenu(id, inv, this);
    }
}
