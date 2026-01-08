package de.rolandsw.schedulemc.production.blockentity;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.production.config.ProductionConfig;
import de.rolandsw.schedulemc.production.core.GenericQuality;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 * Unified Processing Block Entity
 *
 * Ersetzt:
 * - AbstractDryingRackBlockEntity (und 3 Subklassen)
 * - AbstractFermentationBarrelBlockEntity (und 3 Subklassen)
 * - AbstractExtractionVatBlockEntity (und 3 Subklassen)
 * - AbstractRefineryBlockEntity (und 3 Subklassen)
 * - ReaktionsKesselBlockEntity (MDMA)
 * - FermentationsTankBlockEntity (LSD)
 * - und viele mehr...
 *
 * Reduziert ~2000 Zeilen Code auf eine einzige konfigurierbare Klasse
 */
public class UnifiedProcessingBlockEntity extends BlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ═══════════════════════════════════════════════════════════
    // PROCESSING DATA
    // ═══════════════════════════════════════════════════════════

    protected final int capacity;              // Anzahl Slots
    protected final String processingStageId;  // Processing Stage (aus Config)
    protected final ProductionConfig config;   // Production Config

    // Slot Arrays
    protected ItemStack[] inputs;
    protected ItemStack[] outputs;
    protected int[] progress;
    protected String[] productionIds;          // Production ID (z.B. "tobacco_virginia")
    protected GenericQuality[] qualities;

    // Resource System (optional)
    @Nullable
    protected String requiredResource;         // z.B. "diesel", "water"
    protected int resourceLevel;               // Aktueller Resource-Stand
    protected int maxResourceLevel;

    protected boolean changed = false;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public UnifiedProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                       int capacity, String processingStageId, ProductionConfig config) {
        super(type, pos, state);
        this.capacity = capacity;
        this.processingStageId = processingStageId;
        this.config = config;

        // Initialize Arrays
        this.inputs = new ItemStack[capacity];
        this.outputs = new ItemStack[capacity];
        this.progress = new int[capacity];
        this.productionIds = new String[capacity];
        this.qualities = new GenericQuality[capacity];

        for (int i = 0; i < capacity; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            progress[i] = 0;
            productionIds[i] = "";
        }

        // Resource System
        ProductionConfig.ProcessingStageConfig stageConfig = config.getProcessingStages().get(processingStageId);
        if (stageConfig != null && stageConfig.requiresResource()) {
            this.requiredResource = stageConfig.getRequiredResource();
            this.maxResourceLevel = 10000;  // 10 Liter default
            this.resourceLevel = 0;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TICK LOGIC
    // ═══════════════════════════════════════════════════════════

    /**
     * Hauptlogik - Wird jeden Tick aufgerufen
     */
    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        ProductionConfig.ProcessingStageConfig stageConfig = config.getProcessingStages().get(processingStageId);
        if (stageConfig == null) {
            LOGGER.error("Processing stage '{}' not found in config!", processingStageId);
            return;
        }

        int processingTime = stageConfig.getProcessingTime();
        boolean anyProgress = false;

        // Process alle Slots
        for (int i = 0; i < capacity; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {

                // Check Resource Requirement
                if (stageConfig.requiresResource()) {
                    if (resourceLevel < stageConfig.getResourceAmount()) {
                        continue;  // Nicht genug Resource, skip diesen Slot
                    }
                }

                // Increment Progress
                progress[i]++;

                // Consume Resources (jede Sekunde)
                if (stageConfig.requiresResource() && progress[i] % 20 == 0) {
                    resourceLevel = Math.max(0, resourceLevel - stageConfig.getResourceAmount());
                }

                // Check if Processing Complete
                if (progress[i] >= processingTime) {
                    // Create Output
                    outputs[i] = createOutput(i, stageConfig);

                    // Clear Input
                    inputs[i] = ItemStack.EMPTY;
                    progress[i] = 0;

                    anyProgress = true;
                }
            }
        }

        if (anyProgress) {
            changed = true;
            setChanged();
            syncToClient();
        }
    }

    /**
     * Erstellt Output-Item basierend auf Config
     */
    protected ItemStack createOutput(int slotIndex, ProductionConfig.ProcessingStageConfig stageConfig) {
        // TODO: Item-Registry Integration
        // Aktuell Placeholder - muss mit ModItems verbunden werden

        String outputItemId = stageConfig.getOutputItem();
        GenericQuality quality = qualities[slotIndex];

        // Erstelle ItemStack basierend auf outputItemId
        // Dies wird in der finalen Version mit dem Item Registry verbunden
        ItemStack output = ItemStack.EMPTY;  // Placeholder

        LOGGER.debug("Created output: {} ({})", outputItemId, quality != null ? quality.getDisplayName() : "unknown");

        return output;
    }

    // ═══════════════════════════════════════════════════════════
    // SLOT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt Item in nächsten freien Slot ein
     */
    public boolean insertItem(ItemStack stack, String productionId, @Nullable GenericQuality quality) {
        for (int i = 0; i < capacity; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                productionIds[i] = productionId;
                qualities[i] = quality;
                progress[i] = 0;
                changed = true;
                setChanged();
                return true;
            }
        }
        return false;  // Alle Slots voll
    }

    /**
     * Entfernt Output aus Slot
     */
    public ItemStack extractOutput(int slot) {
        if (slot >= 0 && slot < capacity && !outputs[slot].isEmpty()) {
            ItemStack extracted = outputs[slot].copy();
            outputs[slot] = ItemStack.EMPTY;
            productionIds[slot] = "";
            qualities[slot] = null;
            changed = true;
            setChanged();
            return extracted;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Gibt freie Slots zurück
     */
    public int getFreeSlots() {
        int free = 0;
        for (int i = 0; i < capacity; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                free++;
            }
        }
        return free;
    }

    // ═══════════════════════════════════════════════════════════
    // RESOURCE MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    public boolean hasResource() {
        return requiredResource != null;
    }

    @Nullable
    public String getRequiredResource() {
        return requiredResource;
    }

    public int getResourceLevel() {
        return resourceLevel;
    }

    public int getMaxResourceLevel() {
        return maxResourceLevel;
    }

    public void addResource(int amount) {
        if (hasResource()) {
            resourceLevel = Math.min(maxResourceLevel, resourceLevel + amount);
            changed = true;
            setChanged();
        }
    }

    public float getResourcePercentage() {
        if (!hasResource() || maxResourceLevel == 0) {
            return 0.0f;
        }
        return (float) resourceLevel / maxResourceLevel;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public int getCapacity() {
        return capacity;
    }

    public ItemStack getInput(int slot) {
        return slot >= 0 && slot < capacity ? inputs[slot] : ItemStack.EMPTY;
    }

    public ItemStack getOutput(int slot) {
        return slot >= 0 && slot < capacity ? outputs[slot] : ItemStack.EMPTY;
    }

    public int getProgress(int slot) {
        return slot >= 0 && slot < capacity ? progress[slot] : 0;
    }

    public float getProgressPercentage(int slot) {
        ProductionConfig.ProcessingStageConfig stageConfig = config.getProcessingStages().get(processingStageId);
        if (stageConfig == null || slot < 0 || slot >= capacity) {
            return 0.0f;
        }
        return (float) progress[slot] / stageConfig.getProcessingTime();
    }

    public String getProductionId(int slot) {
        return slot >= 0 && slot < capacity ? productionIds[slot] : "";
    }

    @Nullable
    public GenericQuality getQuality(int slot) {
        return slot >= 0 && slot < capacity ? qualities[slot] : null;
    }

    public String getProcessingStageId() {
        return processingStageId;
    }

    public ProductionConfig getConfig() {
        return config;
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("Capacity", capacity);
        tag.putString("ProcessingStageId", processingStageId);

        // Save Slots
        for (int i = 0; i < capacity; i++) {
            CompoundTag slotTag = new CompoundTag();

            slotTag.put("Input", inputs[i].save(new CompoundTag()));
            slotTag.put("Output", outputs[i].save(new CompoundTag()));
            slotTag.putInt("Progress", progress[i]);
            slotTag.putString("ProductionId", productionIds[i]);

            if (qualities[i] != null) {
                slotTag.putInt("QualityLevel", qualities[i].getLevel());
            }

            tag.put("Slot" + i, slotTag);
        }

        // Save Resources
        if (hasResource()) {
            tag.putString("RequiredResource", requiredResource);
            tag.putInt("ResourceLevel", resourceLevel);
            tag.putInt("MaxResourceLevel", maxResourceLevel);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // Load Slots
        for (int i = 0; i < capacity; i++) {
            if (tag.contains("Slot" + i)) {
                CompoundTag slotTag = tag.getCompound("Slot" + i);

                inputs[i] = ItemStack.of(slotTag.getCompound("Input"));
                outputs[i] = ItemStack.of(slotTag.getCompound("Output"));
                progress[i] = slotTag.getInt("Progress");
                productionIds[i] = slotTag.getString("ProductionId");

                if (slotTag.contains("QualityLevel")) {
                    int qualityLevel = slotTag.getInt("QualityLevel");
                    // TODO: Quality Lookup basierend auf Production Config
                    // qualities[i] = config.getQualityTiers()[qualityLevel];
                }
            }
        }

        // Load Resources
        if (tag.contains("RequiredResource")) {
            requiredResource = tag.getString("RequiredResource");
            resourceLevel = tag.getInt("ResourceLevel");
            maxResourceLevel = tag.getInt("MaxResourceLevel");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CLIENT SYNC
    // ═══════════════════════════════════════════════════════════

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("UnifiedProcessingBlockEntity{capacity=%d, stage='%s'}\n", capacity, processingStageId));

        for (int i = 0; i < capacity; i++) {
            if (!inputs[i].isEmpty() || !outputs[i].isEmpty()) {
                sb.append(String.format("  Slot %d: Input=%s, Output=%s, Progress=%d%%, ProductionId=%s\n",
                    i,
                    inputs[i].isEmpty() ? "empty" : inputs[i].getCount() + "x",
                    outputs[i].isEmpty() ? "empty" : outputs[i].getCount() + "x",
                    (int)(getProgressPercentage(i) * 100),
                    productionIds[i]
                ));
            }
        }

        if (hasResource()) {
            sb.append(String.format("  Resource: %s (%d/%d, %.1f%%)\n",
                requiredResource, resourceLevel, maxResourceLevel, getResourcePercentage() * 100));
        }

        return sb.toString();
    }
}
