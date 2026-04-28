package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.cheese.CheeseAgeLevel;
import de.rolandsw.schedulemc.cheese.CheeseQuality;
import de.rolandsw.schedulemc.cheese.CheeseType;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.cheese.items.CheeseWheelItem;
import de.rolandsw.schedulemc.cheese.menu.PackagingStationMenu;
import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packaging Station - Schneidet Käselaibe in Wedges und verpackt sie
 *
 * Input: Aged Cheese Wheel + Packaging Material
 * Output: Cheese Wedges (basierend auf Gewicht)
 * Processing Method: Natural/Smoked/Herb
 */
public class PackagingStationBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer, MenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackagingStationBlockEntity.class);
    private ItemStack wheelInput = ItemStack.EMPTY;
    private ItemStack packagingInput = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private ItemStack secondaryOutput = ItemStack.EMPTY;
    private int packagingProgress = 0;

    private long lastGameTime = -1L;
    private CheeseType cheeseType;
    private CheeseQuality quality;
    private CheeseAgeLevel ageLevel;
    private double wheelWeight = 0;

    public PackagingStationBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.PACKAGING_STATION.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(4) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0 || slot == 1) syncInputsFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot >= 2 ? 64 : 16;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) return stack.getItem() == CheeseItems.CHEESE_WHEEL.get();
                if (slot == 1) {
                    return stack.getItem() == CheeseItems.CHEESE_CLOTH.get()
                            || stack.getItem() == CheeseItems.WAX_COATING.get()
                            || stack.getItem() == CheeseItems.CHEESE_PAPER.get();
                }
                return false; // output slots not insertable
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot >= 2) return super.extractItem(slot, amount, simulate);
                if (packagingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputsFromHandler() {
        ItemStack handlerWheel = itemHandler.getStackInSlot(0);
        ItemStack handlerPackaging = itemHandler.getStackInSlot(1);

        if (!handlerWheel.isEmpty() && wheelInput.isEmpty()) {
            wheelInput = handlerWheel.copy();
            CompoundTag tag = handlerWheel.getTag();
            if (tag != null) {
                if (tag.contains("CheeseType")) {
                    try { cheeseType = CheeseType.valueOf(tag.getString("CheeseType")); }
                    catch (IllegalArgumentException exception) {
                        LOGGER.warn("Invalid CheeseType '{}' in PackagingStationBlockEntity at {}", tag.getString("CheeseType"), getBlockPos(), exception);
                    }
                }
                if (tag.contains("Quality")) {
                    try { quality = CheeseQuality.valueOf(tag.getString("Quality")); }
                    catch (IllegalArgumentException e) { quality = CheeseQuality.SCHLECHT; }
                }
                if (tag.contains("AgeLevel")) {
                    try { ageLevel = CheeseAgeLevel.valueOf(tag.getString("AgeLevel")); }
                    catch (IllegalArgumentException e) { ageLevel = CheeseAgeLevel.FRESH; }
                } else ageLevel = CheeseAgeLevel.FRESH;
                if (tag.contains("WeightKg")) wheelWeight = tag.getDouble("WeightKg");
            }
            packagingProgress = 0;
        } else if (handlerWheel.isEmpty()) {
            wheelInput = ItemStack.EMPTY;
            cheeseType = null;
            quality = null;
            ageLevel = null;
            wheelWeight = 0;
            packagingProgress = 0;
        }

        if (!handlerPackaging.isEmpty() && packagingInput.isEmpty()) {
            packagingInput = handlerPackaging.copy();
        } else if (handlerPackaging.isEmpty()) {
            packagingInput = ItemStack.EMPTY;
        }
    }

    public int getPackagingProgress() {
        return packagingProgress;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;
        if (ticksPassed == 0) return;

        if (!wheelInput.isEmpty() && !packagingInput.isEmpty() && output.isEmpty()) {
            int packagingTime = 300; // 15 seconds per wheel
            int prevProgress = packagingProgress;
            packagingProgress = Math.min(packagingProgress + (int) ticksPassed, packagingTime);

            if (packagingProgress >= packagingTime) {
                // Output the packaged cheese wheel
                output = wheelInput.copy();
                output.setCount(1);
                itemHandler.setStackInSlot(2, output.copy());

                wheelInput.shrink(1);
                packagingInput.shrink(1);

                if (wheelInput.isEmpty()) {
                    itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                } else {
                    itemHandler.setStackInSlot(0, wheelInput.copy());
                }

                if (packagingInput.isEmpty()) {
                    itemHandler.setStackInSlot(1, ItemStack.EMPTY);
                } else {
                    itemHandler.setStackInSlot(1, packagingInput.copy());
                }

                packagingProgress = 0;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }

            if (packagingProgress / 20 > prevProgress / 20) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        } else {
            if (packagingProgress > 0) {
                packagingProgress = 0;
                setChanged();
            }
        }

        ItemStack handlerOutput = itemHandler.getStackInSlot(2);
        if (handlerOutput.isEmpty() && !output.isEmpty()) {
            output = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        return !wheelInput.isEmpty() && !packagingInput.isEmpty() && output.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        if (!wheelInput.isEmpty()) tag.put("WheelInput", wheelInput.save(new CompoundTag()));
        if (!packagingInput.isEmpty()) tag.put("PackagingInput", packagingInput.save(new CompoundTag()));
        if (!output.isEmpty()) tag.put("Output", output.save(new CompoundTag()));
        if (!secondaryOutput.isEmpty()) tag.put("SecondaryOutput", secondaryOutput.save(new CompoundTag()));
        tag.putInt("PackagingProgress", packagingProgress);
        tag.putLong("LastGameTime", lastGameTime);
        if (cheeseType != null) tag.putString("CheeseType", cheeseType.name());
        if (quality != null) tag.putString("Quality", quality.name());
        if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());
        tag.putDouble("WheelWeight", wheelWeight);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        wheelInput = tag.contains("WheelInput") ? ItemStack.of(tag.getCompound("WheelInput")) : ItemStack.EMPTY;
        packagingInput = tag.contains("PackagingInput") ? ItemStack.of(tag.getCompound("PackagingInput")) : ItemStack.EMPTY;
        output = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        secondaryOutput = tag.contains("SecondaryOutput") ? ItemStack.of(tag.getCompound("SecondaryOutput")) : ItemStack.EMPTY;
        packagingProgress = tag.getInt("PackagingProgress");
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;
        if (tag.contains("CheeseType")) {
            try { cheeseType = CheeseType.valueOf(tag.getString("CheeseType")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid CheeseType '{}' in PackagingStationBlockEntity at {}", tag.getString("CheeseType"), getBlockPos(), exception);
            }
        }
        if (tag.contains("Quality")) {
            try { quality = CheeseQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException e) { quality = CheeseQuality.SCHLECHT; }
        }
        if (tag.contains("AgeLevel")) {
            try { ageLevel = CheeseAgeLevel.valueOf(tag.getString("AgeLevel")); }
            catch (IllegalArgumentException e) { ageLevel = CheeseAgeLevel.FRESH; }
        }
        wheelWeight = tag.contains("WheelWeight") ? tag.getDouble("WheelWeight") : 0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.packaging_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new PackagingStationMenu(id, inv, this);
    }
}
