package de.rolandsw.schedulemc.cannabis.blockentity;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
import de.rolandsw.schedulemc.cannabis.items.CannabisOilItem;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Öl-Extraktor - extrahiert Cannabis-Öl aus Blüten oder Trim
 * Benötigt Lösungsmittel (Extraction Solvent)
 */
public class OelExtraktortBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    public static final int EXTRACTION_TICKS = 12000;  // 10 Minuten
    public static final int MIN_MATERIAL_WEIGHT = 10;
    public static final float BUD_CONVERSION_RATE = 0.15f;  // 10g Bud -> 1.5ml Öl
    public static final float TRIM_CONVERSION_RATE = 0.08f; // 10g Trim -> 0.8ml Öl

    private int materialWeight = 0;
    private boolean isFromBuds = false; // true = Buds, false = Trim
    private CannabisStrain strain = CannabisStrain.HYBRID;
    private CannabisQuality baseQuality = CannabisQuality.MIDS;
    private int solventCount = 0;

    private int extractionProgress = 0;
    private boolean isExtracting = false;
    private ItemStack outputItem = ItemStack.EMPTY;

    public OelExtraktortBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.OEL_EXTRAKTOR.get(), pos, state);
    }

    public boolean addMaterial(ItemStack stack) {
        if (isExtracting || !outputItem.isEmpty()) return false;

        if (stack.getItem() instanceof TrimmedBudItem) {
            CannabisStrain budStrain = TrimmedBudItem.getStrain(stack);
            if (materialWeight > 0 && (budStrain != strain || !isFromBuds)) {
                return false;
            }
            strain = budStrain;
            baseQuality = TrimmedBudItem.getQuality(stack);
            materialWeight += TrimmedBudItem.getWeight(stack);
            isFromBuds = true;
        } else if (stack.getItem() instanceof TrimItem) {
            CannabisStrain trimStrain = TrimItem.getStrain(stack);
            if (materialWeight > 0 && (trimStrain != strain || isFromBuds)) {
                return false;
            }
            strain = trimStrain;
            baseQuality = CannabisQuality.MIDS;
            materialWeight += TrimItem.getWeight(stack);
            isFromBuds = false;
        } else {
            return false;
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public boolean addSolvent(ItemStack stack) {
        if (isExtracting || !outputItem.isEmpty()) return false;
        if (!stack.is(CannabisItems.EXTRACTION_SOLVENT.get())) return false;

        solventCount += stack.getCount();
        setChanged();
        return true;
    }

    public boolean startExtraction() {
        if (materialWeight < MIN_MATERIAL_WEIGHT || solventCount < 1 || isExtracting || !outputItem.isEmpty()) {
            return false;
        }

        isExtracting = true;
        extractionProgress = 0;
        solventCount--;

        setChanged();
        return true;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isExtracting) {
            extractionProgress++;

            if (extractionProgress >= EXTRACTION_TICKS) {
                finishExtraction();
            }

            setChanged();
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
        return isExtracting;
    }

    private void finishExtraction() {
        // Berechne Öl-Menge
        float conversionRate = isFromBuds ? BUD_CONVERSION_RATE : TRIM_CONVERSION_RATE;
        int oilMilliliters = (int) (materialWeight * conversionRate);
        oilMilliliters = Math.max(1, oilMilliliters); // Mindestens 1ml

        // Qualität: Buds geben bessere Qualität
        CannabisQuality quality = isFromBuds ? baseQuality.upgrade() : baseQuality;

        outputItem = CannabisOilItem.create(strain, quality, oilMilliliters);

        materialWeight = 0;
        isExtracting = false;
        extractionProgress = 0;

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack extractOil() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = outputItem.copy();
        outputItem = ItemStack.EMPTY;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    // Getter
    public int getMaterialWeight() { return materialWeight; }
    public int getSolventCount() { return solventCount; }
    public boolean isFromBuds() { return isFromBuds; }
    public boolean canStart() { return materialWeight >= MIN_MATERIAL_WEIGHT && solventCount >= 1 && !isExtracting && outputItem.isEmpty(); }
    public boolean isExtracting() { return isExtracting; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public float getExtractionProgress() { return (float) extractionProgress / EXTRACTION_TICKS; }
    public int getExpectedOilAmount() {
        float rate = isFromBuds ? BUD_CONVERSION_RATE : TRIM_CONVERSION_RATE;
        return Math.max(1, (int) (materialWeight * rate));
    }
    public CannabisStrain getStrain() { return strain; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("MaterialWeight", materialWeight);
        tag.putBoolean("IsFromBuds", isFromBuds);
        tag.putString("Strain", strain.name());
        tag.putString("Quality", baseQuality.name());
        tag.putInt("SolventCount", solventCount);
        tag.putInt("ExtractionProgress", extractionProgress);
        tag.putBoolean("IsExtracting", isExtracting);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        materialWeight = tag.getInt("MaterialWeight");
        isFromBuds = tag.getBoolean("IsFromBuds");
        try { strain = CannabisStrain.valueOf(tag.getString("Strain")); }
        catch (IllegalArgumentException e) { strain = CannabisStrain.HYBRID; }
        try { baseQuality = CannabisQuality.valueOf(tag.getString("Quality")); }
        catch (IllegalArgumentException e) { baseQuality = CannabisQuality.MIDS; }
        solventCount = tag.getInt("SolventCount");
        extractionProgress = tag.getInt("ExtractionProgress");
        isExtracting = tag.getBoolean("IsExtracting");
        outputItem = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
}
