package de.rolandsw.schedulemc.chocolate.blockentity;

import de.rolandsw.schedulemc.chocolate.ChocolateQuality;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
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
 * Pressing Station - Presst Kakaomasse zu Butter und Pulver
 *
 * Input: Cocoa Mass
 * Output 1: Cocoa Butter
 * Output 2: Cocoa Powder
 * Processing Time: 800 Ticks (40 Sekunden)
 * Quality: Erhält Quality aus Input
 */
public class PressingStationBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack butterOutput = ItemStack.EMPTY;
    private ItemStack powderOutput = ItemStack.EMPTY;
    private int pressingProgress = 0;
    private ChocolateQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 800; // 40 seconds

    public PressingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.PRESSING_STATION.get(), pos, state);
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
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == ChocolateItems.COCOA_MASS.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1 || slot == 2) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && pressingProgress == 0) return super.extractItem(slot, amount, simulate);
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
            // Extract quality from NBT
            CompoundTag tag = handlerInput.getTag();
            if (tag != null && tag.contains("Quality")) {
                quality = ChocolateQuality.valueOf(tag.getString("Quality"));
            } else {
                quality = ChocolateQuality.BASIC;
            }
            pressingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            quality = null;
            pressingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, butterOutput.copy());
        itemHandler.setStackInSlot(2, powderOutput.copy());
    }

    public int getPressingProgressValue() {
        return pressingProgress;
    }

    public int getTotalPressingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && butterOutput.isEmpty() && powderOutput.isEmpty()) {
            pressingProgress++;

            if (pressingProgress >= PROCESSING_TIME) {
                // Pressing complete: Cocoa Mass → Cocoa Butter + Cocoa Powder
                // 1 mass produces 1 butter and 2 powder
                ItemStack butter = new ItemStack(ChocolateItems.COCOA_BUTTER.get(), inputStack.getCount());
                ItemStack powder = new ItemStack(ChocolateItems.COCOA_POWDER.get(), inputStack.getCount() * 2);

                // Preserve quality in both outputs
                if (quality != null) {
                    CompoundTag butterTag = butter.getOrCreateTag();
                    butterTag.putString("Quality", quality.name());

                    CompoundTag powderTag = powder.getOrCreateTag();
                    powderTag.putString("Quality", quality.name());
                }

                butterOutput = butter;
                powderOutput = powder;
                pressingProgress = 0;
                changed = true;
            }

            if (pressingProgress % 20 == 0) changed = true;
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
        return !inputStack.isEmpty() && butterOutput.isEmpty() && powderOutput.isEmpty();
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
        if (!butterOutput.isEmpty()) tag.put("ButterOutput", butterOutput.save(new CompoundTag()));
        if (!powderOutput.isEmpty()) tag.put("PowderOutput", powderOutput.save(new CompoundTag()));
        tag.putInt("Progress", pressingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        butterOutput = tag.contains("ButterOutput") ? ItemStack.of(tag.getCompound("ButterOutput")) : ItemStack.EMPTY;
        powderOutput = tag.contains("PowderOutput") ? ItemStack.of(tag.getCompound("PowderOutput")) : ItemStack.EMPTY;
        pressingProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) quality = ChocolateQuality.valueOf(tag.getString("Quality"));
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
        return Component.translatable("block.schedulemc.pressing_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return null; // TODO: Create menu in Part 3
    }
}
