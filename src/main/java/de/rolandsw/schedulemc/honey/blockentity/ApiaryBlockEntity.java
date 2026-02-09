package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.HoneyQuality;
import de.rolandsw.schedulemc.honey.HoneyType;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.honey.menu.ApiaryMenu;
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
import net.minecraft.world.level.biome.Biome;
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
 * Apiary BlockEntity - Multi-hive system with highest production
 *
 * Production: 2 honeycombs every 600 ticks (30 seconds)
 * Quality: Best chance for premium quality
 * Output: 1 slot with increased capacity
 */
public class ApiaryBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private int tickCount = 0;
    private HoneyType honeyType = HoneyType.WILDFLOWER;
    private HoneyQuality quality = HoneyQuality.PREMIUM;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PRODUCTION_TIME = 600; // 30 seconds

    public ApiaryBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.APIARY.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return false; // Output only
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getTickCount() {
        return tickCount;
    }

    public int getProductionTime() {
        return PRODUCTION_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;
        ItemStack output = itemHandler.getStackInSlot(0);

        // Check if we can produce (output slot not full)
        if (output.isEmpty() || (output.getItem() == HoneyItems.RAW_HONEYCOMB.get() && output.getCount() < 62)) {
            tickCount++;

            // Determine quality based on biome
            if (tickCount == 1) {
                determineQualityFromBiome();
            }

            if (tickCount >= PRODUCTION_TIME) {
                // Produce 2 honeycombs (apiary is more productive)
                ItemStack honeycomb = new ItemStack(HoneyItems.RAW_HONEYCOMB.get(), 2);
                CompoundTag tag = honeycomb.getOrCreateTag();
                tag.putString("HoneyType", honeyType.name());
                tag.putString("Quality", quality.name());

                if (output.isEmpty()) {
                    itemHandler.setStackInSlot(0, honeycomb);
                } else {
                    output.grow(2);
                }

                tickCount = 0;
                changed = true;
            }

            if (tickCount % 100 == 0) changed = true;
        }

        if (changed) {
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

    private void determineQualityFromBiome() {
        if (level == null) return;

        Biome biome = level.getBiome(worldPosition).value();
        float temperature = biome.getBaseTemperature();

        // Apiary produces premium quality
        if (temperature > 1.5f) {
            honeyType = HoneyType.WILDFLOWER;
            quality = HoneyQuality.PREMIUM;
        } else if (temperature < 0.3f) {
            honeyType = HoneyType.FOREST;
            quality = HoneyQuality.PREMIUM;
        } else if (temperature >= 0.6f && temperature <= 0.95f) {
            honeyType = HoneyType.WILDFLOWER;
            quality = HoneyQuality.PREMIUM;
        } else {
            honeyType = HoneyType.WILDFLOWER;
            quality = HoneyQuality.PREMIUM;
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        ItemStack output = itemHandler.getStackInSlot(0);
        return output.isEmpty() || output.getCount() < 62;
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
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("TickCount", tickCount);
        tag.putString("HoneyType", honeyType.name());
        tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        tickCount = tag.getInt("TickCount");
        if (tag.contains("HoneyType")) {
            try { honeyType = HoneyType.valueOf(tag.getString("HoneyType")); }
            catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("Quality")) {
            try { quality = HoneyQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException ignored) {}
        }
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
        return Component.translatable("block.schedulemc.apiary");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new ApiaryMenu(id, inv, this);
    }
}
