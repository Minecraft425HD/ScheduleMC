package de.rolandsw.schedulemc.chocolate.blockentity;

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
 * Wrapping Station - Verpackt Schokoladen-Produkte
 *
 * Input 1: Chocolate Bar oder anderes Schokoladen-Produkt
 * Input 2: Wrapper (Normal oder Gold)
 * Input 3: Box (Optional, für Premium-Verpackung)
 * Output: Verpacktes Schokoladen-Produkt
 * Processing Time: 100 Ticks (5 Sekunden)
 *
 * Funktion:
 * - Verlängert Haltbarkeit
 * - Fügt Marken-/Label-Information hinzu
 * - Gold-Wrapper für Premium-Produkte
 * - Box-Verpackung für Geschenksets
 */
public class WrappingStationBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack chocolateInput = ItemStack.EMPTY;
    private ItemStack wrapperInput = ItemStack.EMPTY;
    private ItemStack boxInput = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int wrappingProgress = 0;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 100; // 5 seconds

    public WrappingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.WRAPPING_STATION.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(4) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot < 3) syncInputsFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 0 ? 16 : 64; // Chocolate bars stack to 16
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    // Accept chocolate bars and other chocolate products
                    return stack.getItem() == ChocolateItems.CHOCOLATE_BAR.get() ||
                           stack.getItem() == ChocolateItems.CHOCOLATE_TRUFFLE.get() ||
                           stack.getItem() == ChocolateItems.CHOCOLATE_PRALINE.get();
                }
                if (slot == 1) {
                    // Wrapper slot
                    return stack.getItem() == ChocolateItems.WRAPPER.get() ||
                           stack.getItem() == ChocolateItems.WRAPPER_GOLD.get();
                }
                if (slot == 2) {
                    // Box slot (optional)
                    return stack.getItem() == ChocolateItems.BOX.get() ||
                           stack.getItem() == ChocolateItems.BOX_PREMIUM.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 3) return super.extractItem(slot, amount, simulate);
                if (slot < 3 && wrappingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerChocolate = itemHandler.getStackInSlot(0);
        ItemStack handlerWrapper = itemHandler.getStackInSlot(1);
        ItemStack handlerBox = itemHandler.getStackInSlot(2);

        if (!handlerChocolate.isEmpty() && chocolateInput.isEmpty()) {
            chocolateInput = handlerChocolate.copy();
            wrappingProgress = 0;
        } else if (handlerChocolate.isEmpty()) {
            chocolateInput = ItemStack.EMPTY;
            wrappingProgress = 0;
        } else {
            chocolateInput = handlerChocolate.copy();
        }

        if (!handlerWrapper.isEmpty() && wrapperInput.isEmpty()) {
            wrapperInput = handlerWrapper.copy();
        } else if (handlerWrapper.isEmpty()) {
            wrapperInput = ItemStack.EMPTY;
        } else {
            wrapperInput = handlerWrapper.copy();
        }

        if (!handlerBox.isEmpty() && boxInput.isEmpty()) {
            boxInput = handlerBox.copy();
        } else if (handlerBox.isEmpty()) {
            boxInput = ItemStack.EMPTY;
        } else {
            boxInput = handlerBox.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, chocolateInput.copy());
        itemHandler.setStackInSlot(1, wrapperInput.copy());
        itemHandler.setStackInSlot(2, boxInput.copy());
        itemHandler.setStackInSlot(3, outputStack.copy());
    }

    public int getWrappingProgressValue() {
        return wrappingProgress;
    }

    public int getTotalWrappingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        // Need chocolate and wrapper (box is optional)
        if (!chocolateInput.isEmpty() && !wrapperInput.isEmpty() && outputStack.isEmpty()) {
            wrappingProgress++;

            if (wrappingProgress >= PROCESSING_TIME) {
                // Wrapping complete: Create wrapped chocolate
                ItemStack wrappedChocolate = chocolateInput.copy();
                wrappedChocolate.setCount(1);

                CompoundTag tag = wrappedChocolate.getOrCreateTag();

                // Mark as wrapped
                tag.putBoolean("Wrapped", true);

                // Add wrapper type
                boolean isGoldWrapper = wrapperInput.getItem() == ChocolateItems.WRAPPER_GOLD.get();
                tag.putBoolean("GoldWrapper", isGoldWrapper);

                // Add box info if present
                if (!boxInput.isEmpty()) {
                    tag.putBoolean("Boxed", true);
                    boolean isPremiumBox = boxInput.getItem() == ChocolateItems.BOX_PREMIUM.get();
                    tag.putBoolean("PremiumBox", isPremiumBox);
                }

                // Extended shelf life
                if (level != null) {
                    tag.putLong("WrappingTime", level.getGameTime());
                    tag.putInt("ShelfLifeDays", isGoldWrapper ? 365 : 180); // Gold wrapper = 1 year, normal = 6 months
                }

                // Update display name
                String wrapperType = isGoldWrapper ? "Premium-Wrapped" : "Wrapped";
                String packaging = !boxInput.isEmpty() ? " (Boxed)" : "";
                wrappedChocolate.setHoverName(Component.literal(wrapperType + " Chocolate" + packaging));

                outputStack = wrappedChocolate;

                // Consume resources
                chocolateInput.shrink(1);
                wrapperInput.shrink(1);
                if (!boxInput.isEmpty()) {
                    boxInput.shrink(1);
                }

                wrappingProgress = 0;
                changed = true;
            }

            if (wrappingProgress % 20 == 0) changed = true;
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
        return !chocolateInput.isEmpty() && !wrapperInput.isEmpty() && outputStack.isEmpty();
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
        if (!wrapperInput.isEmpty()) tag.put("WrapperInput", wrapperInput.save(new CompoundTag()));
        if (!boxInput.isEmpty()) tag.put("BoxInput", boxInput.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", wrappingProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        chocolateInput = tag.contains("ChocolateInput") ? ItemStack.of(tag.getCompound("ChocolateInput")) : ItemStack.EMPTY;
        wrapperInput = tag.contains("WrapperInput") ? ItemStack.of(tag.getCompound("WrapperInput")) : ItemStack.EMPTY;
        boxInput = tag.contains("BoxInput") ? ItemStack.of(tag.getCompound("BoxInput")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        wrappingProgress = tag.getInt("Progress");
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
        return Component.translatable("block.schedulemc.wrapping_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.WrappingStationMenu(id, inv, this);
    }
}
