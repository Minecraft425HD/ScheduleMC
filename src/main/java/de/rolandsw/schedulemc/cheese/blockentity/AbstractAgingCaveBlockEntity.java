package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.cheese.CheeseAgeLevel;
import de.rolandsw.schedulemc.cheese.CheeseQuality;
import de.rolandsw.schedulemc.cheese.CheeseType;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.cheese.items.CheeseWheelItem;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
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

/**
 * Abstrakte Basis für Aging Caves (Reifekeller)
 * Lagert Käselaibe und lässt sie passiv reifen
 */
public abstract class AbstractAgingCaveBlockEntity extends BlockEntity implements IUtilityConsumer {
    private ItemStack storedCheese = ItemStack.EMPTY;
    private int agingTicks = 0;
    private CheeseType cheeseType;
    private CheeseQuality quality;
    private double weightKg;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractAgingCaveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    /**
     * Kapazität: Anzahl der Käselaibe
     */
    protected abstract int getCapacity();

    /**
     * Alterungs-Geschwindigkeits-Multiplikator
     * 1.0 = normal, 1.2 = 20% schneller, etc.
     */
    protected abstract double getAgingSpeedMultiplier();

    private void createItemHandler() {
        int maxItems = getCapacity();
        itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) syncInputFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 0 ? maxItems : 1;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0 && stack.getItem() == CheeseItems.CHEESE_WHEEL.get();
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && storedCheese.isEmpty()) {
            storedCheese = handlerInput.copy();
            CompoundTag tag = handlerInput.getTag();
            if (tag != null) {
                if (tag.contains("CheeseType")) cheeseType = CheeseType.valueOf(tag.getString("CheeseType"));
                if (tag.contains("Quality")) quality = CheeseQuality.valueOf(tag.getString("Quality"));
                if (tag.contains("WeightKg")) weightKg = tag.getDouble("WeightKg");
                if (tag.contains("AgingTicks")) agingTicks = tag.getInt("AgingTicks");
                else agingTicks = 0;
            }
        } else if (handlerInput.isEmpty()) {
            storedCheese = ItemStack.EMPTY;
            cheeseType = null;
            quality = null;
            weightKg = 0;
            agingTicks = 0;
        }
    }

    public int getAgingTicks() {
        return agingTicks;
    }

    public CheeseAgeLevel getCurrentAgeLevel() {
        return CheeseAgeLevel.determineAgeLevel(agingTicks);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!storedCheese.isEmpty()) {
            // Apply aging speed multiplier
            double tickIncrement = getAgingSpeedMultiplier();
            agingTicks += (int) Math.ceil(tickIncrement);

            // Update the stored cheese wheel's aging ticks
            CheeseWheelItem.setAgingTicks(storedCheese, agingTicks);
            itemHandler.setStackInSlot(0, storedCheese.copy());

            if (agingTicks % 100 == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        return !storedCheese.isEmpty();
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
        if (!storedCheese.isEmpty()) tag.put("StoredCheese", storedCheese.save(new CompoundTag()));
        tag.putInt("AgingTicks", agingTicks);
        if (cheeseType != null) tag.putString("CheeseType", cheeseType.name());
        if (quality != null) tag.putString("Quality", quality.name());
        tag.putDouble("WeightKg", weightKg);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        storedCheese = tag.contains("StoredCheese") ? ItemStack.of(tag.getCompound("StoredCheese")) : ItemStack.EMPTY;
        agingTicks = tag.getInt("AgingTicks");
        if (tag.contains("CheeseType")) cheeseType = CheeseType.valueOf(tag.getString("CheeseType"));
        if (tag.contains("Quality")) quality = CheeseQuality.valueOf(tag.getString("Quality"));
        weightKg = tag.getDouble("WeightKg");
        if (!storedCheese.isEmpty()) itemHandler.setStackInSlot(0, storedCheese.copy());
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
