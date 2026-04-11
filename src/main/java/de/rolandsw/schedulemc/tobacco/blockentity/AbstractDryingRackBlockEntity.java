package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
import de.rolandsw.schedulemc.cannabis.items.FreshBudItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem;
import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Basisklasse für Trocknungsgestelle (Small/Medium/Big).
 * Kapazität und Trocknungszeit werden per Supplier konfiguriert,
 * sodass keine gesonderten Subklassen pro Größe nötig sind.
 */
public class AbstractDryingRackBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDryingRackBlockEntity.class);

    private final Supplier<Integer> capacitySupplier;
    private final Supplier<Integer> dryingTimeSupplier;

    private boolean lastActiveState = false;

    // Input und Output als einzelne Stacks
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int dryingProgress = 0;

    // Produkttyp (tobacco oder cannabis)
    private ContentType contentType = ContentType.NONE;

    // Tobacco-spezifische Daten
    private TobaccoType tobaccoType;
    private TobaccoQuality tobaccoQuality;

    // Cannabis-spezifische Daten
    private CannabisStrain cannabisStrain;
    private CannabisQuality cannabisQuality;

    public AbstractDryingRackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                          Supplier<Integer> capacitySupplier,
                                          Supplier<Integer> dryingTimeSupplier) {
        super(type, pos, state);
        this.capacitySupplier = capacitySupplier;
        this.dryingTimeSupplier = dryingTimeSupplier;
        createItemHandler();  // NOPMD
    }

    public int getCapacity() {
        return capacitySupplier.get();
    }

    protected int getDryingTime() {
        return dryingTimeSupplier.get();
    }

    private void createItemHandler() {
        int maxLeaves = getCapacity();
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
                return slot == 0 ? maxLeaves : 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() instanceof FreshTobaccoLeafItem ||
                           stack.getItem() instanceof FreshBudItem;
                }
                return false; // Output slot ist read-only
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) {
                    return super.extractItem(slot, amount, simulate);
                }
                // Input kann nur extrahiert werden wenn noch nicht getrocknet
                if (slot == 0 && dryingProgress == 0) {
                    return super.extractItem(slot, amount, simulate);
                }
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            if (handlerInput.getItem() instanceof FreshTobaccoLeafItem) {
                contentType = ContentType.TOBACCO;
                tobaccoType = FreshTobaccoLeafItem.getType(handlerInput);
                tobaccoQuality = FreshTobaccoLeafItem.getQuality(handlerInput);
            } else if (handlerInput.getItem() instanceof FreshBudItem) {
                contentType = ContentType.CANNABIS;
                cannabisStrain = FreshBudItem.getStrain(handlerInput);
                cannabisQuality = FreshBudItem.getQuality(handlerInput);
            }
            dryingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            contentType = ContentType.NONE;
            tobaccoType = null;
            tobaccoQuality = null;
            cannabisStrain = null;
            cannabisQuality = null;
            dryingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public boolean addFreshLeaves(ItemStack stack) {
        boolean isTobacco = stack.getItem() instanceof FreshTobaccoLeafItem;
        boolean isCannabis = stack.getItem() instanceof FreshBudItem;

        if (!isTobacco && !isCannabis) {
            return false;
        }

        if (inputStack.isEmpty() && outputStack.isEmpty()) {
            inputStack = stack.copy();
            inputStack.setCount(Math.min(stack.getCount(), getCapacity()));

            if (isTobacco) {
                contentType = ContentType.TOBACCO;
                tobaccoType = FreshTobaccoLeafItem.getType(stack);
                tobaccoQuality = FreshTobaccoLeafItem.getQuality(stack);
            } else {
                contentType = ContentType.CANNABIS;
                cannabisStrain = FreshBudItem.getStrain(stack);
                cannabisQuality = FreshBudItem.getQuality(stack);
            }

            dryingProgress = 0;
            syncToHandler();
            setChanged();
            return true;
        } else if (!inputStack.isEmpty() && inputStack.getCount() < getCapacity() && outputStack.isEmpty()) {
            // Items hinzufügen wenn gleicher Typ und Qualität
            if (contentType == ContentType.TOBACCO && isTobacco) {
                TobaccoType newType = FreshTobaccoLeafItem.getType(stack);
                TobaccoQuality newQuality = FreshTobaccoLeafItem.getQuality(stack);
                if (newType == tobaccoType && newQuality == tobaccoQuality) {
                    int canAdd = Math.min(stack.getCount(), getCapacity() - inputStack.getCount());
                    inputStack.grow(canAdd);
                    syncToHandler();
                    setChanged();
                    return true;
                }
            } else if (contentType == ContentType.CANNABIS && isCannabis) {
                CannabisStrain newStrain = FreshBudItem.getStrain(stack);
                CannabisQuality newQuality = FreshBudItem.getQuality(stack);
                if (newStrain == cannabisStrain && newQuality == cannabisQuality) {
                    int canAdd = Math.min(stack.getCount(), getCapacity() - inputStack.getCount());
                    inputStack.grow(canAdd);
                    syncToHandler();
                    setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack extractAllDriedLeaves() {
        if (!outputStack.isEmpty()) {
            ItemStack result = outputStack.copy();
            outputStack = ItemStack.EMPTY;
            inputStack = ItemStack.EMPTY;
            dryingProgress = 0;
            contentType = ContentType.NONE;
            tobaccoType = null;
            tobaccoQuality = null;
            cannabisStrain = null;
            cannabisQuality = null;
            syncToHandler();
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    public boolean isFull() {
        return inputStack.getCount() >= getCapacity() || !outputStack.isEmpty();
    }

    public boolean hasInput() {
        return !inputStack.isEmpty();
    }

    public boolean hasOutput() {
        return !outputStack.isEmpty();
    }

    public int getInputCount() {
        return inputStack.getCount();
    }

    public int getOutputCount() {
        return outputStack.getCount();
    }

    public float getAverageDryingPercentage() {
        if (inputStack.isEmpty()) return 0;
        int totalTime = getDryingTime() * inputStack.getCount();
        if (totalTime <= 0) return 0;
        return (float) dryingProgress / totalTime;
    }

    public int getDryingProgressValue() {
        return dryingProgress;
    }

    public int getTotalDryingTime() {
        return getDryingTime() * Math.max(1, inputStack.getCount());
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            int totalTime = getDryingTime() * inputStack.getCount();
            dryingProgress = Math.min(dryingProgress + 1, totalTime);

            if (dryingProgress >= totalTime) {
                // Trocknung abgeschlossen - erstelle richtigen Output
                if (contentType == ContentType.TOBACCO) {
                    outputStack = DriedTobaccoLeafItem.create(tobaccoType, tobaccoQuality, inputStack.getCount());
                } else if (contentType == ContentType.CANNABIS) {
                    outputStack = DriedBudItem.create(cannabisStrain, cannabisQuality, inputStack.getCount());
                }
                changed = true;
            }

            if (dryingProgress % 20 == 0) {
                changed = true;
            }
        }

        if (changed) {
            syncToHandler();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn Input vorhanden ist und noch nicht fertig (Output leer)
        return !inputStack.isEmpty() && outputStack.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!inputStack.isEmpty()) {
            CompoundTag inputTag = new CompoundTag();
            inputStack.save(inputTag);
            tag.put("Input", inputTag);
        }

        if (!outputStack.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputStack.save(outputTag);
            tag.put("Output", outputTag);
        }

        tag.putInt("Progress", dryingProgress);
        tag.putString("ContentType", contentType.name());

        if (tobaccoType != null) {
            tag.putString("TobaccoType", tobaccoType.name());
        }
        if (tobaccoQuality != null) {
            tag.putString("TobaccoQuality", tobaccoQuality.name());
        }
        if (cannabisStrain != null) {
            tag.putString("CannabisStrain", cannabisStrain.name());
        }
        if (cannabisQuality != null) {
            tag.putString("CannabisQuality", cannabisQuality.name());
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
        dryingProgress = tag.getInt("Progress");

        if (tag.contains("ContentType")) {
            try { contentType = ContentType.valueOf(tag.getString("ContentType")); }
            catch (IllegalArgumentException ex) { contentType = ContentType.NONE; }
        }

        // Legacy-Support: "Type" und "Quality" für alte Saves
        if (tag.contains("Type")) {
            try { tobaccoType = TobaccoType.valueOf(tag.getString("Type")); }  // NOPMD
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid legacy TobaccoType '{}' in AbstractDryingRackBlockEntity at {}", tag.getString("Type"), getBlockPos(), exception);
            }
        }
        if (tag.contains("Quality")) {
            try { tobaccoQuality = TobaccoQuality.valueOf(tag.getString("Quality")); }  // NOPMD
            catch (IllegalArgumentException e) { tobaccoQuality = TobaccoQuality.SCHLECHT; }  // NOPMD - legacy path, may be overwritten by TobaccoQuality tag
        }

        // Neue Felder
        if (tag.contains("TobaccoType")) {
            try { tobaccoType = TobaccoType.valueOf(tag.getString("TobaccoType")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid TobaccoType '{}' in AbstractDryingRackBlockEntity at {}", tag.getString("TobaccoType"), getBlockPos(), exception);
            }
        }
        if (tag.contains("TobaccoQuality")) {
            try { tobaccoQuality = TobaccoQuality.valueOf(tag.getString("TobaccoQuality")); }
            catch (IllegalArgumentException e) { tobaccoQuality = TobaccoQuality.SCHLECHT; }
        }
        if (tag.contains("CannabisStrain")) {
            try { cannabisStrain = CannabisStrain.valueOf(tag.getString("CannabisStrain")); }
            catch (IllegalArgumentException exception) {
                LOGGER.warn("Invalid CannabisStrain '{}' in AbstractDryingRackBlockEntity at {}", tag.getString("CannabisStrain"), getBlockPos(), exception);
            }
        }
        if (tag.contains("CannabisQuality")) {
            try { cannabisQuality = CannabisQuality.valueOf(tag.getString("CannabisQuality")); }
            catch (IllegalArgumentException e) { cannabisQuality = CannabisQuality.GUT; }
        }

        syncToHandler();
    }

    /**
     * Enum für den Inhaltstyp des Drying Racks
     */
    private enum ContentType {
        NONE,       // Leer
        TOBACCO,    // Tabak
        CANNABIS    // Cannabis
    }
}
