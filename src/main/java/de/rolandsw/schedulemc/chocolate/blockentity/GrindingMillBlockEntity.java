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
 * Grinding Mill - Mahlt Kakao-Nibs zu Kakaomasse
 *
 * Input: Cocoa Nibs
 * Output: Cocoa Mass
 * Processing Time: 600 Ticks (30 Sekunden)
 * Quality: Kann Quality um 1 Stufe verbessern (maximal GOOD)
 */
public class GrindingMillBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int grindingProgress = 0;
    private ChocolateQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 600; // 30 seconds

    public GrindingMillBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.GRINDING_MILL.get(), pos, state);
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
                    return stack.getItem() == ChocolateItems.COCOA_NIBS.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && grindingProgress == 0) return super.extractItem(slot, amount, simulate);
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
                try { quality = ChocolateQuality.valueOf(tag.getString("Quality")); }
                catch (IllegalArgumentException ignored) {}
            } else {
                quality = ChocolateQuality.GUT;
            }
            grindingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            quality = null;
            grindingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getGrindingProgressValue() {
        return grindingProgress;
    }

    public int getTotalGrindingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            grindingProgress++;

            if (grindingProgress >= PROCESSING_TIME) {
                // Grinding complete: Cocoa Nibs → Cocoa Mass
                ItemStack cocoaMass = new ItemStack(ChocolateItems.COCOA_MASS.get(), inputStack.getCount());

                // Upgrade quality by 1 level (max GOOD)
                ChocolateQuality upgradedQuality = quality != null ? quality : ChocolateQuality.SCHLECHT;
                upgradedQuality = (ChocolateQuality) upgradedQuality.upgrade();

                // Cap at GOOD quality for grinding mill
                if (upgradedQuality.getLevel() > ChocolateQuality.SEHR_GUT.getLevel()) {
                    upgradedQuality = ChocolateQuality.SEHR_GUT;
                }

                CompoundTag tag = cocoaMass.getOrCreateTag();
                tag.putString("Quality", upgradedQuality.name());

                outputStack = cocoaMass;
                grindingProgress = 0;
                changed = true;
            }

            if (grindingProgress % 20 == 0) changed = true;
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
        tag.putInt("Progress", grindingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        grindingProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) {
            try { quality = ChocolateQuality.valueOf(tag.getString("Quality")); }
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
        return Component.translatable("block.schedulemc.grinding_mill");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.GrindingMillMenu(id, inv, this);
    }
}
