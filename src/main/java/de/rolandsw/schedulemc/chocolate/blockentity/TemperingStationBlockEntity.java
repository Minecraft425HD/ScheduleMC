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
 * Tempering Station - Temperiert Schokolade für Glanz und Snap
 *
 * Input: Conched Chocolate
 * Output: Tempered Chocolate
 * Processing Time: 1000 Ticks (50 Sekunden)
 * Quality: KRITISCH - Kann bis zu EXCEPTIONAL upgraden!
 *
 * Tempering ist der wichtigste Qualitätsschritt:
 * - Perfektes Tempering = EXCEPTIONAL Quality möglich
 * - Gutes Tempering = Quality +1
 * - Falsches Tempering = Quality bleibt gleich oder sinkt
 */
public class TemperingStationBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int temperingProgress = 0;
    private ChocolateQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 1000; // 50 seconds

    public TemperingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.TEMPERING_STATION.get(), pos, state);
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
                    return stack.getItem() == ChocolateItems.CONCHED_CHOCOLATE.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && temperingProgress == 0) return super.extractItem(slot, amount, simulate);
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
                quality = ChocolateQuality.BASIC;
            }
            temperingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            quality = null;
            temperingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getTemperingProgressValue() {
        return temperingProgress;
    }

    public int getTotalTemperingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            temperingProgress++;

            if (temperingProgress >= PROCESSING_TIME) {
                // Tempering complete: Conched Chocolate → Tempered Chocolate
                ItemStack temperedChocolate = new ItemStack(ChocolateItems.TEMPERED_CHOCOLATE.get(), inputStack.getCount());

                // Tempering can upgrade quality significantly!
                // If already at PREMIUM, can reach EXCEPTIONAL
                ChocolateQuality upgradedQuality = quality != null ? quality : ChocolateQuality.RAW;
                upgradedQuality = (ChocolateQuality) upgradedQuality.upgrade();

                // Tempering is the only process that can reach EXCEPTIONAL
                // (No cap here - if input is PREMIUM, output is EXCEPTIONAL)

                CompoundTag outputTag = temperedChocolate.getOrCreateTag();
                outputTag.putString("Quality", upgradedQuality.name());

                // Copy additional data from conched chocolate
                CompoundTag inputTag = inputStack.getTag();
                if (inputTag != null) {
                    if (inputTag.contains("HasMilk")) outputTag.putBoolean("HasMilk", inputTag.getBoolean("HasMilk"));
                    if (inputTag.contains("HasVanilla")) outputTag.putBoolean("HasVanilla", inputTag.getBoolean("HasVanilla"));
                }

                outputStack = temperedChocolate;
                temperingProgress = 0;
                changed = true;
            }

            if (temperingProgress % 20 == 0) changed = true;
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
        tag.putInt("Progress", temperingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        temperingProgress = tag.getInt("Progress");
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
        return Component.translatable("block.schedulemc.tempering_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.TemperingStationMenu(id, inv, this);
    }
}
