package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import de.rolandsw.schedulemc.wine.WineQuality;
import de.rolandsw.schedulemc.wine.WineType;
import de.rolandsw.schedulemc.wine.items.GrapeItem;
import de.rolandsw.schedulemc.wine.items.WineItems;
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
 * Crushing Station - Zerdrückt Weintrauben zu Maische
 * 
 * Input: Weintrauben (beliebige Sorte)
 * Output: Maische (gleiche Sorte, erhält Quality)
 * Processing Time: 100 Ticks (5 Sekunden) pro Traube
 */
public class CrushingStationBlockEntity extends BlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;
    
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int crushingProgress = 0;
    private WineType wineType;
    private WineQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public CrushingStationBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.CRUSHING_STATION.get(), pos, state);
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
                return slot == 0 ? 64 : 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() instanceof GrapeItem;
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && crushingProgress == 0) return super.extractItem(slot, amount, simulate);
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
            if (handlerInput.getItem() instanceof GrapeItem grapeItem) {
                wineType = grapeItem.getWineType();
                quality = GrapeItem.getQuality(handlerInput);
            }
            crushingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            wineType = null;
            quality = null;
            crushingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getCrushingProgressValue() {
        return crushingProgress;
    }

    public int getTotalCrushingTime() {
        return 100 * Math.max(1, inputStack.getCount());
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            crushingProgress++;

            int totalTime = getTotalCrushingTime();
            if (crushingProgress >= totalTime) {
                // Crushing complete: Grapes → Mash
                ItemStack mash = switch (wineType) {
                    case RIESLING -> new ItemStack(WineItems.RIESLING_MASH.get(), inputStack.getCount());
                    case SPAETBURGUNDER -> new ItemStack(WineItems.SPAETBURGUNDER_MASH.get(), inputStack.getCount());
                    case CHARDONNAY -> new ItemStack(WineItems.CHARDONNAY_MASH.get(), inputStack.getCount());
                    case MERLOT -> new ItemStack(WineItems.MERLOT_MASH.get(), inputStack.getCount());
                };

                // Store quality in NBT
                if (mash != null && quality != null) {
                    CompoundTag tag = mash.getOrCreateTag();
                    tag.putString("Quality", quality.name());
                }

                outputStack = mash;
                crushingProgress = 0;
                changed = true;
            }

            if (crushingProgress % 20 == 0) changed = true;
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
        tag.putInt("Progress", crushingProgress);
        if (wineType != null) tag.putString("WineType", wineType.name());
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        crushingProgress = tag.getInt("Progress");
        if (tag.contains("WineType")) wineType = WineType.valueOf(tag.getString("WineType"));
        if (tag.contains("Quality")) quality = WineQuality.valueOf(tag.getString("Quality"));
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
