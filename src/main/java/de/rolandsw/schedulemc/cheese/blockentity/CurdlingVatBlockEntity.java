package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.cheese.CheeseQuality;
import de.rolandsw.schedulemc.cheese.items.CheeseCurdItem;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Curdling Vat - Dicklegt pasteurisierte Milch mit Lab zu Käsebruch
 *
 * Input: Pasteurized Milk Bucket + Rennet
 * Output: Cheese Curd (mit Quality)
 * Processing Time: 600 Ticks (30 Sekunden)
 */
public class CurdlingVatBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack milkInput = ItemStack.EMPTY;
    private ItemStack rennetInput = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int curdlingProgress = 0;
    private CheeseQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public CurdlingVatBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.CURDLING_VAT.get(), pos, state);
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
                return slot == 2 ? 64 : 16;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == Items.MILK_BUCKET;
                }
                if (slot == 1) {
                    return stack.getItem() == CheeseItems.RENNET.get();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) return super.extractItem(slot, amount, simulate);
                if ((slot == 0 || slot == 1) && curdlingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerMilk = itemHandler.getStackInSlot(0);
        ItemStack handlerRennet = itemHandler.getStackInSlot(1);

        if (!handlerMilk.isEmpty() && milkInput.isEmpty()) {
            milkInput = handlerMilk.copy();
            curdlingProgress = 0;
        } else if (handlerMilk.isEmpty()) {
            milkInput = ItemStack.EMPTY;
            curdlingProgress = 0;
        } else {
            milkInput = handlerMilk.copy();
        }

        if (!handlerRennet.isEmpty() && rennetInput.isEmpty()) {
            rennetInput = handlerRennet.copy();
        } else if (handlerRennet.isEmpty()) {
            rennetInput = ItemStack.EMPTY;
        } else {
            rennetInput = handlerRennet.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, milkInput.copy());
        itemHandler.setStackInSlot(1, rennetInput.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
    }

    public int getCurdlingProgressValue() {
        return curdlingProgress;
    }

    public int getTotalCurdlingTime() {
        return 600; // 30 seconds
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!milkInput.isEmpty() && !rennetInput.isEmpty() && outputStack.isEmpty()) {
            curdlingProgress++;

            if (curdlingProgress >= getTotalCurdlingTime()) {
                // Determine quality once at completion
                if (quality == null) {
                    quality = CheeseQuality.determineQuality(1.0, ThreadLocalRandom.current());
                }

                // Curdling complete: Milk + Rennet → Cheese Curd
                int curdCount = milkInput.getCount() * 4; // 1 bucket = 4 curds
                ItemStack curd = CheeseCurdItem.create(quality, curdCount);

                outputStack = curd;

                // Consume one rennet
                rennetInput.shrink(1);
                if (rennetInput.isEmpty()) {
                    itemHandler.setStackInSlot(1, ItemStack.EMPTY);
                } else {
                    itemHandler.setStackInSlot(1, rennetInput.copy());
                }

                curdlingProgress = 0;
                quality = null;
                changed = true;
            }

            if (curdlingProgress % 20 == 0) changed = true;
        } else {
            if (curdlingProgress > 0 && (milkInput.isEmpty() || rennetInput.isEmpty())) {
                curdlingProgress = 0;
                quality = null;
                changed = true;
            }
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
        return !milkInput.isEmpty() && !rennetInput.isEmpty() && outputStack.isEmpty();
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
        if (!milkInput.isEmpty()) tag.put("MilkInput", milkInput.save(new CompoundTag()));
        if (!rennetInput.isEmpty()) tag.put("RennetInput", rennetInput.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", curdlingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        milkInput = tag.contains("MilkInput") ? ItemStack.of(tag.getCompound("MilkInput")) : ItemStack.EMPTY;
        rennetInput = tag.contains("RennetInput") ? ItemStack.of(tag.getCompound("RennetInput")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        curdlingProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) {
            try { quality = CheeseQuality.valueOf(tag.getString("Quality")); }
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
        return Component.translatable("block.schedulemc.curdling_vat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.cheese.menu.CurdlingVatMenu(id, inv, this);
    }
}
