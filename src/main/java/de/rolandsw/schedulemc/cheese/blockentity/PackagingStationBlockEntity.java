package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.cheese.CheeseAgeLevel;
import de.rolandsw.schedulemc.cheese.CheeseProcessingMethod;
import de.rolandsw.schedulemc.cheese.CheeseQuality;
import de.rolandsw.schedulemc.cheese.CheeseType;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.cheese.items.CheeseWheelItem;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
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
 * Packaging Station - Schneidet Käselaibe in Wedges und verpackt sie
 *
 * Input: Aged Cheese Wheel + Packaging Material
 * Output: Cheese Wedges (basierend auf Gewicht)
 * Processing Method: Natural/Smoked/Herb
 */
public class PackagingStationBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private ItemStack wheelInput = ItemStack.EMPTY;
    private ItemStack packagingInput = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private ItemStack secondaryOutput = ItemStack.EMPTY;
    private int packagingProgress = 0;

    private CheeseType cheeseType;
    private CheeseQuality quality;
    private CheeseAgeLevel ageLevel;
    private CheeseProcessingMethod processingMethod = CheeseProcessingMethod.NATURAL;
    private double wheelWeight = 0;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

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
                if (slot == 0) {
                    return stack.getItem() == CheeseItems.CHEESE_WHEEL.get();
                }
                if (slot == 1) {
                    return stack.getItem() == CheeseItems.CHEESE_CLOTH.get()
                            || stack.getItem() == CheeseItems.WAX_COATING.get()
                            || stack.getItem() == CheeseItems.CHEESE_PAPER.get();
                }
                return slot >= 2;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerWheel = itemHandler.getStackInSlot(0);
        ItemStack handlerPackaging = itemHandler.getStackInSlot(1);

        if (!handlerWheel.isEmpty() && wheelInput.isEmpty()) {
            wheelInput = handlerWheel.copy();
            CompoundTag tag = handlerWheel.getTag();
            if (tag != null) {
                if (tag.contains("CheeseType")) cheeseType = CheeseType.valueOf(tag.getString("CheeseType"));
                if (tag.contains("Quality")) quality = CheeseQuality.valueOf(tag.getString("Quality"));
                if (tag.contains("AgeLevel")) ageLevel = CheeseAgeLevel.valueOf(tag.getString("AgeLevel"));
                else ageLevel = CheeseAgeLevel.FRESH;
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

    public CheeseProcessingMethod getProcessingMethod() {
        return processingMethod;
    }

    public void setProcessingMethod(CheeseProcessingMethod method) {
        this.processingMethod = method;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!wheelInput.isEmpty() && !packagingInput.isEmpty() && output.isEmpty()) {
            packagingProgress++;
            int packagingTime = 300; // 15 seconds per wheel

            if (packagingProgress >= packagingTime) {
                // Calculate number of wedges based on weight
                // 1kg = 4 wedges, so we get wedgeCount = weight * 4
                int wedgeCount = (int) Math.ceil(wheelWeight * 4);
                wedgeCount = Math.max(1, wedgeCount);

                // Create wedges based on processing method and type
                ItemStack wedges;
                if (processingMethod == CheeseProcessingMethod.SMOKED) {
                    wedges = new ItemStack(CheeseItems.SMOKED_CHEESE.get(), wedgeCount);
                } else if (processingMethod == CheeseProcessingMethod.HERB) {
                    wedges = new ItemStack(CheeseItems.HERB_CHEESE.get(), wedgeCount);
                } else {
                    // Natural - use specific cheese type wedge
                    wedges = new ItemStack(CheeseItems.CHEESE_WEDGE.get(), wedgeCount);
                }

                // Store metadata in wedge NBT
                CompoundTag wedgeTag = wedges.getOrCreateTag();
                if (cheeseType != null) wedgeTag.putString("CheeseType", cheeseType.name());
                if (quality != null) wedgeTag.putString("Quality", quality.name());
                if (ageLevel != null) wedgeTag.putString("AgeLevel", ageLevel.name());
                wedgeTag.putString("ProcessingMethod", processingMethod.name());

                output = wedges;
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

            if (packagingProgress % 20 == 0) {
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
        return !wheelInput.isEmpty() && !packagingInput.isEmpty();
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
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
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
        if (cheeseType != null) tag.putString("CheeseType", cheeseType.name());
        if (quality != null) tag.putString("Quality", quality.name());
        if (ageLevel != null) tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", processingMethod.name());
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
        if (tag.contains("CheeseType")) cheeseType = CheeseType.valueOf(tag.getString("CheeseType"));
        if (tag.contains("Quality")) quality = CheeseQuality.valueOf(tag.getString("Quality"));
        if (tag.contains("AgeLevel")) ageLevel = CheeseAgeLevel.valueOf(tag.getString("AgeLevel"));
        if (tag.contains("ProcessingMethod")) {
            processingMethod = CheeseProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
        } else {
            processingMethod = CheeseProcessingMethod.NATURAL;
        }
        wheelWeight = tag.contains("WheelWeight") ? tag.getDouble("WheelWeight") : 0;
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
        return Component.translatable("block.schedulemc.packaging_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return null; // Menu will be created later
    }
}
