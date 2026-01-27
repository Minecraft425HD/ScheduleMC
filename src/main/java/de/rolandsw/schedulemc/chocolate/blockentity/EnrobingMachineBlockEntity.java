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
 * Enrobing Machine - Überzieht Items mit Schokolade
 *
 * Input 1: Tempered Chocolate
 * Input 2: Item zum Überziehen (Cookies, Früchte, Nüsse)
 * Output: Schokoladen-überzogenes Item
 * Processing Time: 400 Ticks (20 Sekunden)
 *
 * Beispiele:
 * - Cookie + Chocolate → Chocolate-Covered Cookie
 * - Apple + Chocolate → Chocolate-Covered Apple
 * - Hazelnuts + Chocolate → Chocolate-Covered Hazelnuts
 */
public class EnrobingMachineBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack chocolateInput = ItemStack.EMPTY;
    private ItemStack itemInput = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int enrobingProgress = 0;
    private ChocolateQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int PROCESSING_TIME = 400; // 20 seconds

    public EnrobingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.ENROBING_MACHINE.get(), pos, state);
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
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == ChocolateItems.TEMPERED_CHOCOLATE.get();
                }
                if (slot == 1) {
                    return isEnrobable(stack);
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1) && enrobingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private boolean isEnrobable(ItemStack stack) {
        // Items that can be coated with chocolate
        return stack.getItem() == Items.COOKIE ||
               stack.getItem() == Items.APPLE ||
               stack.getItem() == Items.SWEET_BERRIES ||
               stack.getItem() == ChocolateItems.HAZELNUTS.get() ||
               stack.getItem() == ChocolateItems.ALMONDS.get() ||
               stack.getItem() == ChocolateItems.DRIED_FRUITS.get() ||
               stack.getItem() == ChocolateItems.CARAMEL.get();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerChocolate = itemHandler.getStackInSlot(0);
        ItemStack handlerItem = itemHandler.getStackInSlot(1);

        if (!handlerChocolate.isEmpty() && chocolateInput.isEmpty()) {
            chocolateInput = handlerChocolate.copy();
            // Extract quality from NBT
            CompoundTag tag = handlerChocolate.getTag();
            if (tag != null && tag.contains("Quality")) {
                quality = ChocolateQuality.valueOf(tag.getString("Quality"));
            } else {
                quality = ChocolateQuality.BASIC;
            }
            enrobingProgress = 0;
        } else if (handlerChocolate.isEmpty()) {
            chocolateInput = ItemStack.EMPTY;
            quality = null;
            enrobingProgress = 0;
        } else {
            chocolateInput = handlerChocolate.copy();
        }

        if (!handlerItem.isEmpty() && itemInput.isEmpty()) {
            itemInput = handlerItem.copy();
        } else if (handlerItem.isEmpty()) {
            itemInput = ItemStack.EMPTY;
        } else {
            itemInput = handlerItem.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, chocolateInput.copy());
        itemHandler.setStackInSlot(1, itemInput.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
    }

    public int getEnrobingProgressValue() {
        return enrobingProgress;
    }

    public int getTotalEnrobingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!chocolateInput.isEmpty() && !itemInput.isEmpty() && outputStack.isEmpty()) {
            enrobingProgress++;

            if (enrobingProgress >= PROCESSING_TIME) {
                // Enrobing complete: Create chocolate-covered item
                // For simplicity, we'll create a modified copy with NBT data
                ItemStack enrobedItem = itemInput.copy();
                enrobedItem.setCount(1);

                CompoundTag tag = enrobedItem.getOrCreateTag();
                tag.putBoolean("ChocolateCovered", true);
                if (quality != null) {
                    tag.putString("ChocolateQuality", quality.name());
                }

                // Add custom name
                enrobedItem.setHoverName(Component.literal("Chocolate-Covered " +
                    itemInput.getItem().getDescriptionId()));

                outputStack = enrobedItem;

                // Consume resources
                chocolateInput.shrink(1);
                itemInput.shrink(1);

                if (chocolateInput.isEmpty()) {
                    quality = null;
                }

                enrobingProgress = 0;
                changed = true;
            }

            if (enrobingProgress % 20 == 0) changed = true;
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
        return !chocolateInput.isEmpty() && !itemInput.isEmpty() && outputStack.isEmpty();
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
        if (!itemInput.isEmpty()) tag.put("ItemInput", itemInput.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", enrobingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        chocolateInput = tag.contains("ChocolateInput") ? ItemStack.of(tag.getCompound("ChocolateInput")) : ItemStack.EMPTY;
        itemInput = tag.contains("ItemInput") ? ItemStack.of(tag.getCompound("ItemInput")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        enrobingProgress = tag.getInt("Progress");
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
        return Component.translatable("block.schedulemc.enrobing_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.EnrobingMachineMenu(id, inv, this);
    }
}
