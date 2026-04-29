package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.HoneyAgeLevel;
import de.rolandsw.schedulemc.honey.HoneyQuality;
import de.rolandsw.schedulemc.honey.HoneyType;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

/**
 * Abstract Aging Chamber - Base class for aging honey
 *
 * Passive aging system that tracks ticks for each honey item
 * Updates HoneyAgeLevel in NBT
 * Different sizes: Small (4 slots), Medium (9 slots), Large (16 slots)
 * Speed multipliers: 1.0x, 1.5x, 2.0x
 */
public abstract class AbstractAgingChamberBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer {
    protected int[] agingTicks;

    protected AbstractAgingChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();  // NOPMD
        agingTicks = new int[getCapacity()];  // NOPMD
    }

    protected abstract int getCapacity();
    protected abstract float getSpeedMultiplier();

    private void createItemHandler() {
        int capacity = getCapacity();
        itemHandler = new ItemStackHandler(capacity) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1; // One bucket per slot
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.getItem() == HoneyItems.FILTERED_HONEY_BUCKET.get()
                        || stack.getItem() == HoneyItems.RAW_HONEY_BUCKET.get();
            }
        };
    }

    public int getAgingTicks(int slot) {
        if (slot >= 0 && slot < agingTicks.length) {
            return agingTicks[slot];
        }
        return 0;
    }

    public HoneyAgeLevel getCurrentAgeLevel(int slot) {
        if (slot < 0 || slot >= agingTicks.length) {
            return HoneyAgeLevel.determineAgeLevel(0);
        }
        return HoneyAgeLevel.determineAgeLevel(agingTicks[slot]);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        boolean changed = false;

        for (int i = 0; i < getCapacity(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                agingTicks[i] += Math.round(getSpeedMultiplier());

                // Update NBT every 100 ticks
                if (agingTicks[i] % 100 == 0) {
                    CompoundTag tag = stack.getOrCreateTag();
                    HoneyAgeLevel ageLevel = HoneyAgeLevel.determineAgeLevel(agingTicks[i]);
                    tag.putString("AgeLevel", ageLevel.name());
                    tag.putInt("AgingTicks", agingTicks[i]);
                    changed = true;
                }
            } else {
                if (agingTicks[i] > 0) {
                    agingTicks[i] = 0;
                    changed = true;
                }
            }
        }

        if (changed) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        for (int i = 0; i < getCapacity(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());

        ListTag ticksList = new ListTag();
        for (int tick : agingTicks) {
            CompoundTag tickTag = new CompoundTag();
            tickTag.putInt("Ticks", tick);
            ticksList.add(tickTag);
        }
        tag.put("AgingTicks", ticksList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        if (agingTicks == null) agingTicks = new int[getCapacity()];

        itemHandler.deserializeNBT(tag.getCompound("Inventory"));

        if (tag.contains("AgingTicks")) {
            ListTag ticksList = tag.getList("AgingTicks", Tag.TAG_COMPOUND);
            for (int i = 0; i < ticksList.size() && i < agingTicks.length; i++) {
                CompoundTag tickTag = ticksList.getCompound(i);
                agingTicks[i] = tickTag.getInt("Ticks");
            }
        }
    }

}
