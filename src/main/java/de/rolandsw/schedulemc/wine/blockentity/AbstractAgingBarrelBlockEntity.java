package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.wine.WineAgeLevel;
import de.rolandsw.schedulemc.wine.WineQuality;
import de.rolandsw.schedulemc.wine.WineType;
import de.rolandsw.schedulemc.wine.items.WineItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAgingBarrelBlockEntity extends BlockEntity implements IUtilityConsumer {
    private ItemStack storedWine = ItemStack.EMPTY;
    private int agingTicks = 0;
    private WineType wineType;
    private WineQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractAgingBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    protected abstract int getCapacity();

    private void createItemHandler() {
        int maxItems = getCapacity();
        itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) syncInputFromHandler();
            }
            @Override public int getSlotLimit(int slot) { return slot == 0 ? maxItems : 64; }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0 && stack.getItem() == WineItems.YOUNG_WINE.get();
            }
        };
    }

    public ItemStackHandler getItemHandler() { return itemHandler; }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && storedWine.isEmpty()) {
            storedWine = handlerInput.copy();
            CompoundTag tag = handlerInput.getTag();
            if (tag != null) {
                if (tag.contains("WineType")) {
                    try { wineType = WineType.valueOf(tag.getString("WineType")); }
                    catch (IllegalArgumentException ignored) {}
                }
                if (tag.contains("Quality")) {
                    try { quality = WineQuality.valueOf(tag.getString("Quality")); }
                    catch (IllegalArgumentException ignored) {}
                }
            }
            agingTicks = 0;
        } else if (handlerInput.isEmpty()) {
            storedWine = ItemStack.EMPTY;
            wineType = null;
            quality = null;
            agingTicks = 0;
        }
    }

    public int getAgingTicks() { return agingTicks; }
    public WineAgeLevel getCurrentAgeLevel() { return WineAgeLevel.determineAgeLevel(agingTicks); }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!storedWine.isEmpty()) {
            agingTicks++;
            if (agingTicks % 100 == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override public boolean isActivelyConsuming() { return !storedWine.isEmpty(); }
    @Override public void onLoad() { super.onLoad(); lazyItemHandler = LazyOptional.of(() -> itemHandler); }
    @Override public void invalidateCaps() { super.invalidateCaps(); lazyItemHandler.invalidate(); }
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedWine.isEmpty()) tag.put("StoredWine", storedWine.save(new CompoundTag()));
        tag.putInt("AgingTicks", agingTicks);
        if (wineType != null) tag.putString("WineType", wineType.name());
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        storedWine = tag.contains("StoredWine") ? ItemStack.of(tag.getCompound("StoredWine")) : ItemStack.EMPTY;
        agingTicks = tag.getInt("AgingTicks");
        if (tag.contains("WineType")) {
            try { wineType = WineType.valueOf(tag.getString("WineType")); }
            catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("Quality")) {
            try { quality = WineQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException ignored) {}
        }
        if (!storedWine.isEmpty()) itemHandler.setStackInSlot(0, storedWine.copy());
    }

    @Override public CompoundTag getUpdateTag() { CompoundTag tag = new CompoundTag(); saveAdditional(tag); return tag; }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
