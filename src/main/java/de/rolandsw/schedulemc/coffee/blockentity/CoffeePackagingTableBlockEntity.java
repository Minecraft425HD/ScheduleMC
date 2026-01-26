package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.coffee.items.GroundCoffeeItem;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Coffee Packaging Table - Verpackt gemahlenen Kaffee
 * Slot 0: Ground Coffee Input
 * Slot 1: Packaging Material (Coffee Bags)
 * Slot 2: Output (Packaged Coffee)
 */
public class CoffeePackagingTableBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Stacks
    private ItemStack coffeeInput = ItemStack.EMPTY;    // Ground Coffee
    private ItemStack packageInput = ItemStack.EMPTY;   // Packaging Material
    private ItemStack outputStack = ItemStack.EMPTY;    // Packaged Coffee
    private int packagingProgress = 0;
    private PackageSize selectedSize = PackageSize.MEDIUM; // Player-wählbar

    // ItemHandler (3 Slots)
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public enum PackageSize {
        SMALL(250, 250),   // 250g = 250 items
        MEDIUM(500, 500),  // 500g = 500 items
        LARGE(1000, 1000); // 1kg = 1000 items

        private final int grams;
        private final int requiredItems;

        PackageSize(int grams, int requiredItems) {
            this.grams = grams;
            this.requiredItems = requiredItems;
        }

        public int getGrams() {
            return grams;
        }

        public int getRequiredItems() {
            return requiredItems;
        }

        public ItemStack getPackagingMaterial() {
            return switch (this) {
                case SMALL -> new ItemStack(CoffeeItems.COFFEE_BAG_SMALL.get());
                case MEDIUM -> new ItemStack(CoffeeItems.COFFEE_BAG_MEDIUM.get());
                case LARGE -> new ItemStack(CoffeeItems.COFFEE_BAG_LARGE.get());
            };
        }

        public ItemStack getOutputItem() {
            return switch (this) {
                case SMALL -> new ItemStack(CoffeeItems.COFFEE_PACKAGE_250G.get());
                case MEDIUM -> new ItemStack(CoffeeItems.COFFEE_PACKAGE_500G.get());
                case LARGE -> new ItemStack(CoffeeItems.COFFEE_PACKAGE_1KG.get());
            };
        }
    }

    public CoffeePackagingTableBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.COFFEE_PACKAGING_TABLE.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                syncFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() instanceof GroundCoffeeItem;
                }
                if (slot == 1) {
                    // Akzeptiere alle Coffee Bag Typen
                    return stack.is(CoffeeItems.COFFEE_BAG_SMALL.get()) ||
                           stack.is(CoffeeItems.COFFEE_BAG_MEDIUM.get()) ||
                           stack.is(CoffeeItems.COFFEE_BAG_LARGE.get());
                }
                return false; // Slot 2 ist Output
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 2) {
                    return super.extractItem(slot, amount, simulate);
                }
                if ((slot == 0 || slot == 1) && packagingProgress == 0) {
                    return super.extractItem(slot, amount, simulate);
                }
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncFromHandler() {
        coffeeInput = itemHandler.getStackInSlot(0).copy();
        packageInput = itemHandler.getStackInSlot(1).copy();
        outputStack = itemHandler.getStackInSlot(2).copy();

        if (coffeeInput.isEmpty() || packageInput.isEmpty()) {
            packagingProgress = 0;
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, coffeeInput.copy());
        itemHandler.setStackInSlot(1, packageInput.copy());
        itemHandler.setStackInSlot(2, outputStack.copy());
    }

    public void setPackageSize(PackageSize size) {
        this.selectedSize = size;
        setChanged();
    }

    public PackageSize getSelectedSize() {
        return selectedSize;
    }

    public float getPackagingPercentage() {
        if (coffeeInput.isEmpty() || packageInput.isEmpty()) return 0;
        return (float) packagingProgress / 200; // 200 Ticks = 10 Sekunden
    }

    public boolean canPackage() {
        if (coffeeInput.isEmpty() || packageInput.isEmpty()) return false;
        if (!outputStack.isEmpty()) return false;

        // Prüfe ob genug Kaffee vorhanden
        if (coffeeInput.getCount() < selectedSize.getRequiredItems()) return false;

        // Prüfe ob richtige Verpackung
        ItemStack requiredBag = selectedSize.getPackagingMaterial();
        return packageInput.is(requiredBag.getItem());
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (canPackage()) {
            packagingProgress++;

            if (packagingProgress >= 200) { // 10 Sekunden
                // Packaging abgeschlossen
                coffeeInput.shrink(selectedSize.getRequiredItems());
                packageInput.shrink(1);
                outputStack = selectedSize.getOutputItem();
                // TODO: Kopiere NBT-Daten vom Ground Coffee zum Package

                packagingProgress = 0;
                changed = true;
            }

            if (packagingProgress % 20 == 0) {
                changed = true;
            }
        } else if (packagingProgress > 0) {
            packagingProgress = 0;
            changed = true;
        }

        if (changed) {
            syncToHandler();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        // Utility-Status
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        return canPackage();
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

        if (!coffeeInput.isEmpty()) {
            tag.put("CoffeeInput", coffeeInput.save(new CompoundTag()));
        }
        if (!packageInput.isEmpty()) {
            tag.put("PackageInput", packageInput.save(new CompoundTag()));
        }
        if (!outputStack.isEmpty()) {
            tag.put("Output", outputStack.save(new CompoundTag()));
        }

        tag.putInt("Progress", packagingProgress);
        tag.putString("PackageSize", selectedSize.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (itemHandler == null) {
            createItemHandler();
        }

        coffeeInput = tag.contains("CoffeeInput") ? ItemStack.of(tag.getCompound("CoffeeInput")) : ItemStack.EMPTY;
        packageInput = tag.contains("PackageInput") ? ItemStack.of(tag.getCompound("PackageInput")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        packagingProgress = tag.getInt("Progress");
        selectedSize = tag.contains("PackageSize") ?
            PackageSize.valueOf(tag.getString("PackageSize")) : PackageSize.MEDIUM;

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
