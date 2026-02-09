package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.HoneyAgeLevel;
import de.rolandsw.schedulemc.honey.HoneyQuality;
import de.rolandsw.schedulemc.honey.HoneyType;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.honey.menu.ProcessingStationMenu;
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
 * Processing Station - Filtered Honey → Processed Honey
 *
 * Input: 2 slots (honey + additives like sugar/pollen)
 * Output: 1 slot (processed honey)
 * Processing time: 800 ticks (40 seconds)
 * Can upgrade quality by 1 level (max PREMIUM)
 */
public class ProcessingStationBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack honeyInput = ItemStack.EMPTY;
    private ItemStack additiveInput = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int processingProgress = 0;
    private HoneyType honeyType;
    private HoneyQuality quality;
    private HoneyAgeLevel ageLevel;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 800; // 40 seconds

    public ProcessingStationBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.PROCESSING_STATION.get(), pos, state);
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
                return slot == 2 ? 1 : 16;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == HoneyItems.FILTERED_HONEY_BUCKET.get();
                }
                if (slot == 1) {
                    return stack.getItem() == Items.SUGAR
                            || stack.getItem() == HoneyItems.POLLEN.get()
                            || stack.getItem() == HoneyItems.ROYAL_JELLY.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1) && processingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerHoney = itemHandler.getStackInSlot(0);
        ItemStack handlerAdditive = itemHandler.getStackInSlot(1);

        if (!handlerHoney.isEmpty() && honeyInput.isEmpty()) {
            honeyInput = handlerHoney.copy();
            CompoundTag tag = handlerHoney.getTag();
            if (tag != null) {
                if (tag.contains("HoneyType")) {
                    try { honeyType = HoneyType.valueOf(tag.getString("HoneyType")); }
                    catch (IllegalArgumentException ignored) {}
                }
                if (tag.contains("Quality")) {
                    try { quality = HoneyQuality.valueOf(tag.getString("Quality")); }
                    catch (IllegalArgumentException ignored) {}
                }
                if (tag.contains("AgeLevel")) {
                    try { ageLevel = HoneyAgeLevel.valueOf(tag.getString("AgeLevel")); }
                    catch (IllegalArgumentException ignored) {}
                } else ageLevel = HoneyAgeLevel.FRESH;
            }
            processingProgress = 0;
        } else if (handlerHoney.isEmpty()) {
            honeyInput = ItemStack.EMPTY;
            honeyType = null;
            quality = null;
            ageLevel = null;
            processingProgress = 0;
        }

        if (!handlerAdditive.isEmpty() && additiveInput.isEmpty()) {
            additiveInput = handlerAdditive.copy();
        } else if (handlerAdditive.isEmpty()) {
            additiveInput = ItemStack.EMPTY;
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, honeyInput.copy());
        itemHandler.setStackInSlot(1, additiveInput.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
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

        if (!honeyInput.isEmpty() && !additiveInput.isEmpty() && outputStack.isEmpty()) {
            processingProgress++;

            if (processingProgress >= PROCESSING_TIME) {
                // Processing complete
                ItemStack processedHoney = new ItemStack(HoneyItems.LIQUID_HONEY_BOTTLE.get(), 1);
                CompoundTag tag = processedHoney.getOrCreateTag();
                if (honeyType != null) tag.putString("HoneyType", honeyType.name());

                // Upgrade quality by 1 level (max PREMIUM)
                HoneyQuality upgradedQuality = upgradeQuality(quality);
                if (upgradedQuality != null) tag.putString("Quality", upgradedQuality.name());

                if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());

                outputStack = processedHoney;

                honeyInput.shrink(1);
                additiveInput.shrink(1);

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

    private HoneyQuality upgradeQuality(HoneyQuality current) {
        if (current == null) return HoneyQuality.GOOD;
        return switch (current) {
            case RAW -> HoneyQuality.BASIC;
            case BASIC -> HoneyQuality.GOOD;
            case GOOD -> HoneyQuality.PREMIUM;
            case PREMIUM -> HoneyQuality.PREMIUM; // Already max
            case EXCEPTIONAL -> HoneyQuality.EXCEPTIONAL;
        };
    }

    @Override
    public boolean isActivelyConsuming() {
        return !honeyInput.isEmpty() && !additiveInput.isEmpty() && outputStack.isEmpty();
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
        if (!additiveInput.isEmpty()) tag.put("AdditiveInput", additiveInput.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", processingProgress);
        if (honeyType != null) tag.putString("HoneyType", honeyType.name());
        if (quality != null) tag.putString("Quality", quality.name());
        if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        honeyInput = tag.contains("HoneyInput") ? ItemStack.of(tag.getCompound("HoneyInput")) : ItemStack.EMPTY;
        additiveInput = tag.contains("AdditiveInput") ? ItemStack.of(tag.getCompound("AdditiveInput")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        processingProgress = tag.getInt("Progress");
        if (tag.contains("HoneyType")) {
            try { honeyType = HoneyType.valueOf(tag.getString("HoneyType")); }
            catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("Quality")) {
            try { quality = HoneyQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("AgeLevel")) {
            try { ageLevel = HoneyAgeLevel.valueOf(tag.getString("AgeLevel")); }
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
        return Component.translatable("block.schedulemc.processing_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new ProcessingStationMenu(id, inv, this);
    }
}
