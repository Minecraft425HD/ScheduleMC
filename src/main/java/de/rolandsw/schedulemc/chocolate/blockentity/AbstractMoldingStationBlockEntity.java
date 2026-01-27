package de.rolandsw.schedulemc.chocolate.blockentity;

import de.rolandsw.schedulemc.chocolate.ChocolateQuality;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
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
 * Abstrakte Basis für Molding Stations
 * Formt temperierte Schokolade in Tafeln
 *
 * Input 1: Tempered Chocolate
 * Input 2: Chocolate Mold
 * Output: Chocolate Bar (mit vollständigen NBT-Daten)
 * Processing Time: 600 Ticks (30 Sekunden)
 *
 * Verschiedene Größen:
 * - Small: 100g Tafeln, 1.0x Geschwindigkeit
 * - Medium: 200g Tafeln, 1.5x Geschwindigkeit
 * - Large: 500g Tafeln, 2.0x Geschwindigkeit
 */
public abstract class AbstractMoldingStationBlockEntity extends BlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;

    private ItemStack chocolateInput = ItemStack.EMPTY;
    private ItemStack moldInput = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int moldingProgress = 0;
    private ChocolateQuality quality;
    private boolean hasMilk;
    private boolean hasVanilla;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int BASE_PROCESSING_TIME = 600; // 30 seconds

    protected AbstractMoldingStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();
    }

    /**
     * Größe der Schokoladentafel in kg (0.1 = 100g, 0.2 = 200g, 0.5 = 500g)
     */
    protected abstract double getBarSize();

    /**
     * Geschwindigkeits-Multiplikator (1.0, 1.5, 2.0)
     */
    protected abstract double getSpeedMultiplier();

    /**
     * Gibt den entsprechenden Chocolate Bar Item zurück
     */
    protected abstract net.minecraft.world.item.Item getChocolateBarItem();

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0 || slot == 1) syncInputsFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 1 ? 16 : 64; // Molds stack to 16
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == ChocolateItems.TEMPERED_CHOCOLATE.get();
                }
                if (slot == 1) {
                    return stack.getItem() == ChocolateItems.CHOCOLATE_MOLD_BAR.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1) && moldingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerChocolate = itemHandler.getStackInSlot(0);
        ItemStack handlerMold = itemHandler.getStackInSlot(1);

        if (!handlerChocolate.isEmpty() && chocolateInput.isEmpty()) {
            chocolateInput = handlerChocolate.copy();
            // Extract quality and metadata from NBT
            CompoundTag tag = handlerChocolate.getTag();
            if (tag != null) {
                if (tag.contains("Quality")) {
                    quality = ChocolateQuality.valueOf(tag.getString("Quality"));
                } else {
                    quality = ChocolateQuality.BASIC;
                }
                hasMilk = tag.getBoolean("HasMilk");
                hasVanilla = tag.getBoolean("HasVanilla");
            } else {
                quality = ChocolateQuality.BASIC;
                hasMilk = false;
                hasVanilla = false;
            }
            moldingProgress = 0;
        } else if (handlerChocolate.isEmpty()) {
            chocolateInput = ItemStack.EMPTY;
            quality = null;
            hasMilk = false;
            hasVanilla = false;
            moldingProgress = 0;
        } else {
            chocolateInput = handlerChocolate.copy();
        }

        if (!handlerMold.isEmpty() && moldInput.isEmpty()) {
            moldInput = handlerMold.copy();
        } else if (handlerMold.isEmpty()) {
            moldInput = ItemStack.EMPTY;
        } else {
            moldInput = handlerMold.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, chocolateInput.copy());
        itemHandler.setStackInSlot(1, moldInput.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
    }

    public int getMoldingProgressValue() {
        return moldingProgress;
    }

    public int getTotalMoldingTime() {
        return (int) (BASE_PROCESSING_TIME / getSpeedMultiplier());
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!chocolateInput.isEmpty() && !moldInput.isEmpty() && outputStack.isEmpty()) {
            moldingProgress++;

            if (moldingProgress >= getTotalMoldingTime()) {
                // Molding complete: Tempered Chocolate + Mold → Chocolate Bar
                ItemStack chocolateBar = new ItemStack(getChocolateBarItem(), 1);

                // Create full NBT data for chocolate bar
                CompoundTag tag = chocolateBar.getOrCreateTag();

                // Quality
                if (quality != null) {
                    tag.putString("Quality", quality.name());
                }

                // Chocolate Type
                String chocolateType;
                if (hasMilk) {
                    chocolateType = "MILK";
                } else {
                    chocolateType = "DARK";
                }
                tag.putString("ChocolateType", chocolateType);

                // Additional flavor info
                tag.putBoolean("HasVanilla", hasVanilla);

                // Size
                tag.putDouble("Size", getBarSize());

                // Production timestamp
                if (level != null) {
                    tag.putLong("ProductionTime", level.getGameTime());
                }

                outputStack = chocolateBar;

                // Consume 1 chocolate and 1 mold
                chocolateInput.shrink(1);
                moldInput.shrink(1);

                if (chocolateInput.isEmpty()) {
                    quality = null;
                    hasMilk = false;
                    hasVanilla = false;
                }

                moldingProgress = 0;
                changed = true;
            }

            if (moldingProgress % 20 == 0) changed = true;
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
        return !chocolateInput.isEmpty() && !moldInput.isEmpty() && outputStack.isEmpty();
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
        if (!chocolateInput.isEmpty()) tag.put("ChocolateInput", chocolateInput.save(new CompoundTag()));
        if (!moldInput.isEmpty()) tag.put("MoldInput", moldInput.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", moldingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
        tag.putBoolean("HasMilk", hasMilk);
        tag.putBoolean("HasVanilla", hasVanilla);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        chocolateInput = tag.contains("ChocolateInput") ? ItemStack.of(tag.getCompound("ChocolateInput")) : ItemStack.EMPTY;
        moldInput = tag.contains("MoldInput") ? ItemStack.of(tag.getCompound("MoldInput")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        moldingProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) quality = ChocolateQuality.valueOf(tag.getString("Quality"));
        hasMilk = tag.getBoolean("HasMilk");
        hasVanilla = tag.getBoolean("HasVanilla");
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
