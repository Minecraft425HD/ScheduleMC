package de.rolandsw.schedulemc.coffee.blockentity;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.items.CoffeeCherryItem;
import de.rolandsw.schedulemc.coffee.items.GreenCoffeeBeanItem;
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
 * Wet Processing Station für Kaffee
 * 3-Stufen-Prozess:
 * 1. Pulping (Schälen) - 200 Ticks
 * 2. Fermentation - 1200 Ticks (1 Minute)
 * 3. Washing - 400 Ticks
 * Total: 1800 Ticks (90 Sekunden)
 */
public class WetProcessingStationBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Slots
    private ItemStack inputStack = ItemStack.EMPTY;  // Coffee Cherries
    private ItemStack outputStack = ItemStack.EMPTY; // Green Coffee Beans
    private int processingProgress = 0;
    private ProcessingStage currentStage = ProcessingStage.IDLE;
    private CoffeeType coffeeType;
    private CoffeeQuality quality;

    // ItemHandler (Slot 0 = Input, Slot 1 = Output)
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // Processing stages
    public enum ProcessingStage {
        IDLE(0),
        PULPING(200),       // Schälen
        FERMENTATION(1200), // Fermentation
        WASHING(400);       // Waschen

        private final int duration;

        ProcessingStage(int duration) {
            this.duration = duration;
        }

        public int getDuration() {
            return duration;
        }

        public ProcessingStage next() {
            return switch (this) {
                case IDLE -> PULPING;
                case PULPING -> FERMENTATION;
                case FERMENTATION -> WASHING;
                case WASHING -> IDLE;
            };
        }
    }

    public WetProcessingStationBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.WET_PROCESSING_STATION.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) {
                    syncInputFromHandler();
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == 0 ? 32 : 64; // Max 32 Kirschen
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() instanceof CoffeeCherryItem;
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) {
                    return super.extractItem(slot, amount, simulate);
                }
                if (slot == 0 && currentStage == ProcessingStage.IDLE) {
                    return super.extractItem(slot, amount, simulate);
                }
                return ItemStack.EMPTY;
            }
        };
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            coffeeType = CoffeeType.ARABICA; // Default
            quality = CoffeeQuality.GOOD; // Default
            currentStage = ProcessingStage.IDLE;
            processingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            coffeeType = null;
            quality = null;
            currentStage = ProcessingStage.IDLE;
            processingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public float getProcessingPercentage() {
        if (currentStage == ProcessingStage.IDLE) return 0;
        return (float) processingProgress / currentStage.getDuration();
    }

    public ProcessingStage getCurrentStage() {
        return currentStage;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            if (currentStage == ProcessingStage.IDLE) {
                currentStage = ProcessingStage.PULPING;
                processingProgress = 0;
                changed = true;
            } else {
                processingProgress++;

                if (processingProgress >= currentStage.getDuration()) {
                    // Stufe abgeschlossen
                    ProcessingStage nextStage = currentStage.next();

                    if (nextStage == ProcessingStage.IDLE) {
                        // Alle Stufen abgeschlossen - erstelle Output
                        // Wet Process: +30% Quality Bonus!
                        CoffeeQuality finalQuality = quality.upgrade();
                        int beanCount = inputStack.getCount() * 2; // 2 Bohnen pro Kirsche

                        outputStack = new ItemStack(
                            de.rolandsw.schedulemc.coffee.items.CoffeeItems.GREEN_ARABICA_BEANS.get(),
                            beanCount
                        );

                        currentStage = ProcessingStage.IDLE;
                        processingProgress = 0;
                    } else {
                        currentStage = nextStage;
                        processingProgress = 0;
                    }

                    changed = true;
                }

                if (processingProgress % 20 == 0) {
                    changed = true;
                }
            }
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
        return !inputStack.isEmpty() && outputStack.isEmpty() && currentStage != ProcessingStage.IDLE;
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

        if (!inputStack.isEmpty()) {
            tag.put("Input", inputStack.save(new CompoundTag()));
        }
        if (!outputStack.isEmpty()) {
            tag.put("Output", outputStack.save(new CompoundTag()));
        }

        tag.putInt("Progress", processingProgress);
        tag.putString("Stage", currentStage.name());

        if (coffeeType != null) {
            tag.putString("CoffeeType", coffeeType.name());
        }
        if (quality != null) {
            tag.putString("Quality", quality.name());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (itemHandler == null) {
            createItemHandler();
        }

        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        processingProgress = tag.getInt("Progress");
        currentStage = tag.contains("Stage") ? ProcessingStage.valueOf(tag.getString("Stage")) : ProcessingStage.IDLE;

        if (tag.contains("CoffeeType")) {
            coffeeType = CoffeeType.valueOf(tag.getString("CoffeeType"));
        }
        if (tag.contains("Quality")) {
            quality = CoffeeQuality.valueOf(tag.getString("Quality"));
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
}
