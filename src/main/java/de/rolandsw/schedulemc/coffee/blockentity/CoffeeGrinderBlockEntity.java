package de.rolandsw.schedulemc.coffee.blockentity;
import de.rolandsw.schedulemc.coffee.CoffeeGrindSize;
import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeRoastLevel;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.items.GroundCoffeeItem;
import de.rolandsw.schedulemc.coffee.items.RoastedCoffeeBeanItem;
import de.rolandsw.schedulemc.coffee.menu.CoffeeGrinderMenu;
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

public class CoffeeGrinderBlockEntity extends BlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int grindingProgress = 0;
    private CoffeeGrindSize selectedGrindSize = CoffeeGrindSize.MEDIUM;
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public CoffeeGrinderBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.COFFEE_GRINDER.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(2) {
            @Override protected void onContentsChanged(int slot) { setChanged(); if (slot == 0) syncInputFromHandler(); }
            @Override public int getSlotLimit(int slot) { return 64; }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0 && stack.getItem() instanceof RoastedCoffeeBeanItem;
            }
            @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && grindingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() { return itemHandler; }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            grindingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            grindingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public void setGrindSize(CoffeeGrindSize size) {
        this.selectedGrindSize = size;
        setChanged();
    }

    public CoffeeGrindSize getSelectedGrindSize() { return selectedGrindSize; }
    public float getGrindingPercentage() {
        if (inputStack.isEmpty()) return 0;
        return (float) grindingProgress / 100;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            grindingProgress++;

            if (grindingProgress >= 100) {
                CoffeeType type = RoastedCoffeeBeanItem.getType(inputStack);
                CoffeeQuality quality = RoastedCoffeeBeanItem.getQuality(inputStack);
                CoffeeRoastLevel roastLevel = RoastedCoffeeBeanItem.getRoastLevel(inputStack);

                outputStack = GroundCoffeeItem.create(type, quality, roastLevel, selectedGrindSize, inputStack.getCount());
                grindingProgress = 0;
                changed = true;
            }

            if (grindingProgress % 20 == 0) changed = true;
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

    @Override public boolean isActivelyConsuming() {
        return !inputStack.isEmpty() && outputStack.isEmpty();
    }

    @Override public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputStack.isEmpty()) tag.put("Input", inputStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", grindingProgress);
        tag.putString("GrindSize", selectedGrindSize.name());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        grindingProgress = tag.getInt("Progress");
        if (tag.contains("GrindSize")) {
            try { selectedGrindSize = CoffeeGrindSize.valueOf(tag.getString("GrindSize")); }
            catch (IllegalArgumentException ignored) { selectedGrindSize = CoffeeGrindSize.MEDIUM; }
        } else { selectedGrindSize = CoffeeGrindSize.MEDIUM; }
        syncToHandler();
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.coffee_grinder");
    }

    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new CoffeeGrinderMenu(id, inv, this);
    }
}
