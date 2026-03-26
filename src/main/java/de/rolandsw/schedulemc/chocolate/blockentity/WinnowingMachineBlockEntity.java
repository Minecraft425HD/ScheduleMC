package de.rolandsw.schedulemc.chocolate.blockentity;

import de.rolandsw.schedulemc.chocolate.ChocolateQuality;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Winnowing Machine - Trennt Schalen von Kakao-Nibs
 *
 * Input: Roasted Cocoa Beans
 * Output: Cocoa Nibs
 * Byproduct: Cocoa Shells (wird zu Kompost/Dünger)
 * Processing Time: 300 Ticks (15 Sekunden)
 * Quality: Erhält Quality aus Input
 */
public class WinnowingMachineBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private ItemStack byproductStack = ItemStack.EMPTY;
    private int winnowingProgress = 0;
    private ChocolateQuality quality;

    private static final int PROCESSING_TIME = 300; // 15 seconds

    public WinnowingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.WINNOWING_MACHINE.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(3) {
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
                return slot == 0 && stack.getItem() == ChocolateItems.ROASTED_COCOA_BEANS.get();
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1 || slot == 2) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && winnowingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            // Extract quality from NBT
            CompoundTag tag = handlerInput.getTag();
            if (tag != null && tag.contains("Quality")) {
                try { quality = ChocolateQuality.valueOf(tag.getString("Quality")); }
                catch (IllegalArgumentException e) { quality = ChocolateQuality.GUT; }
            } else {
                quality = ChocolateQuality.GUT;
            }
            winnowingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            quality = null;
            winnowingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
        itemHandler.setStackInSlot(2, byproductStack.copy());
    }

    public int getWinnowingProgressValue() {
        return winnowingProgress;
    }

    public int getTotalWinnowingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty() && byproductStack.isEmpty()) {
            winnowingProgress = Math.min(winnowingProgress + 1, PROCESSING_TIME);

            if (winnowingProgress >= PROCESSING_TIME) {
                // Winnowing complete: Roasted Beans → Cocoa Nibs + Shells
                ItemStack nibs = new ItemStack(ChocolateItems.COCOA_NIBS.get(), inputStack.getCount());

                // Preserve quality in NBT
                if (quality != null) {
                    CompoundTag tag = nibs.getOrCreateTag();
                    tag.putString("Quality", quality.name());
                }

                // Byproduct: Cocoa shells (can be used as fertilizer/compost)
                // Each roasted bean produces 1 nib and some shells
                ItemStack shells = new ItemStack(ChocolateItems.COCOA_SHELLS.get(), inputStack.getCount() / 2);

                outputStack = nibs;
                byproductStack = shells;
                winnowingProgress = 0;
                changed = true;
            }

            if (winnowingProgress % 20 == 0) changed = true;
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
        return !inputStack.isEmpty() && outputStack.isEmpty() && byproductStack.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputStack.isEmpty()) tag.put("Input", inputStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        if (!byproductStack.isEmpty()) tag.put("Byproduct", byproductStack.save(new CompoundTag()));
        tag.putInt("Progress", winnowingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        byproductStack = tag.contains("Byproduct") ? ItemStack.of(tag.getCompound("Byproduct")) : ItemStack.EMPTY;
        winnowingProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) {
            try { quality = ChocolateQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException e) { quality = ChocolateQuality.GUT; }
        }
        syncToHandler();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.winnowing_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.WinnowingMachineMenu(id, inv, this);
    }
}
