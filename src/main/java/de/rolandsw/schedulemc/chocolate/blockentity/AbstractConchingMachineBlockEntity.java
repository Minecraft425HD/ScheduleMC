package de.rolandsw.schedulemc.chocolate.blockentity;

import de.rolandsw.schedulemc.chocolate.ChocolateQuality;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstrakte Basis für Conching Machines
 * Conchiert Kakaomasse mit Zutaten zu Schokolade
 *
 * Input: Cocoa Mass + Zutaten (Zucker, Milchpulver, Vanille, etc.)
 * Output: Conched Chocolate
 * Processing Time: 2400 Ticks (2 Minuten) - sehr lange!
 * Quality: Kann Quality um 1 Stufe verbessern (maximal PREMIUM)
 *
 * Verschiedene Größen:
 * - Small: 2 Input-Slots, 1.0x Geschwindigkeit
 * - Medium: 4 Input-Slots, 1.5x Geschwindigkeit
 * - Large: 6 Input-Slots, 2.0x Geschwindigkeit
 */
public abstract class AbstractConchingMachineBlockEntity extends BlockEntity implements IUtilityConsumer {
    private boolean lastActiveState = false;

    // Separate stacks for each input slot
    private ItemStack cocoaMassInput = ItemStack.EMPTY;
    private ItemStack[] ingredientInputs;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int conchingProgress = 0;
    private ChocolateQuality quality;

    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int BASE_PROCESSING_TIME = 2400; // 2 minutes

    protected AbstractConchingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        ingredientInputs = new ItemStack[getIngredientSlots()];
        for (int i = 0; i < ingredientInputs.length; i++) {
            ingredientInputs[i] = ItemStack.EMPTY;
        }
        createItemHandler();
    }

    /**
     * Anzahl der Zutaten-Slots (ohne Kakaomasse-Slot)
     */
    protected abstract int getIngredientSlots();

    /**
     * Geschwindigkeits-Multiplikator (1.0, 1.5, 2.0)
     */
    protected abstract double getSpeedMultiplier();

    private void createItemHandler() {
        int totalSlots = 1 + getIngredientSlots() + 1; // 1 cocoa mass + ingredients + 1 output
        itemHandler = new ItemStackHandler(totalSlots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot < totalSlots - 1) syncInputsFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                // Slot 0: Cocoa Mass only
                if (slot == 0) {
                    return stack.getItem() == ChocolateItems.COCOA_MASS.get();
                }
                // Slots 1 to n: Ingredients
                if (slot > 0 && slot < totalSlots - 1) {
                    return isValidIngredient(stack);
                }
                // Last slot: Output only
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                // Output slot always extractable
                if (slot == totalSlots - 1) return super.extractItem(slot, amount, simulate);
                // Input slots only extractable when not processing
                if (slot < totalSlots - 1 && conchingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private boolean isValidIngredient(ItemStack stack) {
        return stack.getItem() == ChocolateItems.SUGAR.get() ||
               stack.getItem() == ChocolateItems.MILK_POWDER.get() ||
               stack.getItem() == ChocolateItems.VANILLA_EXTRACT.get() ||
               stack.getItem() == ChocolateItems.LECITHIN.get() ||
               stack.getItem() == ChocolateItems.COCOA_BUTTER.get();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private void syncInputsFromHandler() {
        ItemStack handlerCocoaMass = itemHandler.getStackInSlot(0);

        if (!handlerCocoaMass.isEmpty() && cocoaMassInput.isEmpty()) {
            cocoaMassInput = handlerCocoaMass.copy();
            // Extract quality from NBT
            CompoundTag tag = handlerCocoaMass.getTag();
            if (tag != null && tag.contains("Quality")) {
                try { quality = ChocolateQuality.valueOf(tag.getString("Quality")); }
                catch (IllegalArgumentException ignored) {}
            } else {
                quality = ChocolateQuality.GUT;
            }
            conchingProgress = 0;
        } else if (handlerCocoaMass.isEmpty()) {
            cocoaMassInput = ItemStack.EMPTY;
            quality = null;
            conchingProgress = 0;
        } else {
            cocoaMassInput = handlerCocoaMass.copy();
        }

        // Sync ingredient slots
        for (int i = 0; i < ingredientInputs.length; i++) {
            ItemStack handlerIngredient = itemHandler.getStackInSlot(i + 1);
            if (!handlerIngredient.isEmpty() && ingredientInputs[i].isEmpty()) {
                ingredientInputs[i] = handlerIngredient.copy();
            } else if (handlerIngredient.isEmpty()) {
                ingredientInputs[i] = ItemStack.EMPTY;
            } else {
                ingredientInputs[i] = handlerIngredient.copy();
            }
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, cocoaMassInput.copy());
        for (int i = 0; i < ingredientInputs.length; i++) {
            itemHandler.setStackInSlot(i + 1, ingredientInputs[i].copy());
        }
        itemHandler.setStackInSlot(1 + ingredientInputs.length, outputStack.copy());
    }

    public int getConchingProgressValue() {
        return conchingProgress;
    }

    public int getTotalConchingTime() {
        double speed = getSpeedMultiplier();
        if (speed <= 0) speed = 1.0;
        return (int) (BASE_PROCESSING_TIME / speed);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        // Check if we have cocoa mass and at least one ingredient
        boolean hasIngredient = false;
        for (ItemStack ingredient : ingredientInputs) {
            if (!ingredient.isEmpty()) {
                hasIngredient = true;
                break;
            }
        }

        if (!cocoaMassInput.isEmpty() && hasIngredient && outputStack.isEmpty()) {
            conchingProgress++;

            if (conchingProgress >= getTotalConchingTime()) {
                // Conching complete: Cocoa Mass + Ingredients → Conched Chocolate
                ItemStack conchedChocolate = new ItemStack(ChocolateItems.CONCHED_CHOCOLATE.get(), cocoaMassInput.getCount());

                // Upgrade quality by 1 level (max PREMIUM)
                ChocolateQuality upgradedQuality = quality != null ? quality : ChocolateQuality.SCHLECHT;
                upgradedQuality = (ChocolateQuality) upgradedQuality.upgrade();

                // Cap at PREMIUM quality for conching
                if (upgradedQuality.getLevel() > ChocolateQuality.LEGENDAER.getLevel()) {
                    upgradedQuality = ChocolateQuality.LEGENDAER;
                }

                CompoundTag tag = conchedChocolate.getOrCreateTag();
                tag.putString("Quality", upgradedQuality.name());

                // Store ingredient info for chocolate type determination
                tag.putBoolean("HasMilk", hasIngredient(ChocolateItems.MILK_POWDER.get()));
                tag.putBoolean("HasVanilla", hasIngredient(ChocolateItems.VANILLA_EXTRACT.get()));

                outputStack = conchedChocolate;
                conchingProgress = 0;
                changed = true;
            }

            if (conchingProgress % 20 == 0) changed = true;
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

    private boolean hasIngredient(net.minecraft.world.item.Item ingredientItem) {
        for (ItemStack ingredient : ingredientInputs) {
            if (!ingredient.isEmpty() && ingredient.getItem() == ingredientItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActivelyConsuming() {
        boolean hasIngredient = false;
        for (ItemStack ingredient : ingredientInputs) {
            if (!ingredient.isEmpty()) {
                hasIngredient = true;
                break;
            }
        }
        return !cocoaMassInput.isEmpty() && hasIngredient && outputStack.isEmpty();
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
        if (!cocoaMassInput.isEmpty()) tag.put("CocoaMass", cocoaMassInput.save(new CompoundTag()));

        CompoundTag ingredientsTag = new CompoundTag();
        for (int i = 0; i < ingredientInputs.length; i++) {
            if (!ingredientInputs[i].isEmpty()) {
                ingredientsTag.put("Ingredient" + i, ingredientInputs[i].save(new CompoundTag()));
            }
        }
        tag.put("Ingredients", ingredientsTag);

        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", conchingProgress);
        if (quality != null) tag.putString("Quality", quality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) {
            ingredientInputs = new ItemStack[getIngredientSlots()];
            for (int i = 0; i < ingredientInputs.length; i++) {
                ingredientInputs[i] = ItemStack.EMPTY;
            }
            createItemHandler();
        }

        cocoaMassInput = tag.contains("CocoaMass") ? ItemStack.of(tag.getCompound("CocoaMass")) : ItemStack.EMPTY;

        CompoundTag ingredientsTag = tag.getCompound("Ingredients");
        for (int i = 0; i < ingredientInputs.length; i++) {
            ingredientInputs[i] = ingredientsTag.contains("Ingredient" + i) ?
                ItemStack.of(ingredientsTag.getCompound("Ingredient" + i)) : ItemStack.EMPTY;
        }

        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        conchingProgress = tag.getInt("Progress");
        if (tag.contains("Quality")) {
            try { quality = ChocolateQuality.valueOf(tag.getString("Quality")); }
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
}
