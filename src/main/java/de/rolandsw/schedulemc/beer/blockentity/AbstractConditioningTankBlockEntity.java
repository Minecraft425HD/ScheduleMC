package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.BeerAgeLevel;
import de.rolandsw.schedulemc.beer.BeerQuality;
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for Conditioning Tanks
 * Passively ages young beer in multiple slots
 *
 * Input: Young beer (green_beer) in multiple slots
 * Output: Conditioned beer with improved age level
 * Aging: Tracks aging ticks for each slot independently
 * Different sizes: Small (4 slots), Medium (9 slots), Large (16 slots)
 * Speed multipliers: 1.0x, 1.5x, 2.0x
 */
public abstract class AbstractConditioningTankBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;
    private long lastGameTime = -1L;

    // Track aging ticks for each slot
    private int[] agingTicks;

    protected AbstractConditioningTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        createItemHandler();  // NOPMD
    }

    /**
     * Number of slots (4, 9, or 16)
     */
    protected abstract int getSlotCount();

    /**
     * Speed multiplier for aging (1.0x, 1.5x, 2.0x)
     */
    protected abstract double getSpeedMultiplier();

    private void createItemHandler() {
        int slotCount = getSlotCount();
        agingTicks = new int[slotCount];

        itemHandler = new ItemStackHandler(slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                // Reset aging if slot is emptied
                ItemStack stack = getStackInSlot(slot);
                if (stack.isEmpty()) {
                    agingTicks[slot] = 0;
                } else if (!stack.hasTag() || !stack.getTag().contains("AgingTicks")) {
                    // New beer placed, reset aging
                    agingTicks[slot] = 0;
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return 16; // Can store multiple beers per slot
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.getItem() == BeerItems.GREEN_BEER.get() ||
                       stack.getItem() == BeerItems.CONDITIONED_BEER.get();
            }
        };
    }

    public int getAgingTicks(int slot) {
        if (slot >= 0 && slot < agingTicks.length) {
            return agingTicks[slot];
        }
        return 0;
    }

    public BeerAgeLevel getCurrentAgeLevel(int slot) {
        int ticks = getAgingTicks(slot);
        int days = ticks / 24000; // Convert ticks to Minecraft days
        return BeerAgeLevel.determineAgeLevel(days);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;
        if (ticksPassed == 0) return;

        boolean changed = false;
        boolean anyActive = false;

        // Process each slot independently
        for (int slot = 0; slot < getSlotCount(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);

            if (!stack.isEmpty() && (stack.getItem() == BeerItems.GREEN_BEER.get() ||
                                     stack.getItem() == BeerItems.CONDITIONED_BEER.get())) {
                anyActive = true;

                int prevTick = agingTicks[slot];
                agingTicks[slot] += (int) Math.round(ticksPassed * getSpeedMultiplier());

                // Update NBT with aging information every 100 ticks
                if (agingTicks[slot] / 100 > prevTick / 100) {
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("AgingTicks", agingTicks[slot]);

                    // Calculate age level
                    int days = agingTicks[slot] / 24000;
                    BeerAgeLevel ageLevel = BeerAgeLevel.determineAgeLevel(days);
                    tag.putString("AgeLevel", ageLevel.name());

                    // If aged enough, convert to conditioned beer
                    if (stack.getItem() == BeerItems.GREEN_BEER.get() && days >= 15) {
                        // Extract quality
                        BeerQuality quality = BeerQuality.SCHLECHT;
                        if (tag.contains("Quality")) {
                            try { quality = BeerQuality.valueOf(tag.getString("Quality")); }
                        catch (IllegalArgumentException e) { quality = BeerQuality.SCHLECHT; }
                        }

                        // Create conditioned beer
                        ItemStack conditionedBeer = new ItemStack(BeerItems.CONDITIONED_BEER.get(), stack.getCount());
                        CompoundTag newTag = conditionedBeer.getOrCreateTag();
                        newTag.putString("Quality", quality.name());
                        newTag.putString("AgeLevel", ageLevel.name());
                        newTag.putInt("AgingTicks", agingTicks[slot]);

                        itemHandler.setStackInSlot(slot, conditionedBeer);
                        changed = true;  // NOPMD
                    }

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

        boolean currentActive = anyActive;
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Check if any slot has beer aging
        for (int slot = 0; slot < getSlotCount(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && (stack.getItem() == BeerItems.GREEN_BEER.get() ||
                                     stack.getItem() == BeerItems.CONDITIONED_BEER.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());

        // Save aging ticks for each slot
        ListTag agingList = new ListTag();
        for (int i = 0; i < agingTicks.length; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            slotTag.putInt("AgingTicks", agingTicks[i]);
            agingList.add(slotTag);
        }
        tag.put("AgingTicks", agingList);
        tag.putLong("LastGameTime", lastGameTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;

        itemHandler.deserializeNBT(tag.getCompound("Inventory"));

        // Load aging ticks for each slot
        if (tag.contains("AgingTicks")) {
            ListTag agingList = tag.getList("AgingTicks", Tag.TAG_COMPOUND);
            for (int i = 0; i < agingList.size(); i++) {
                CompoundTag slotTag = agingList.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < agingTicks.length) {
                    agingTicks[slot] = slotTag.getInt("AgingTicks");
                }
            }
        }
    }

}
