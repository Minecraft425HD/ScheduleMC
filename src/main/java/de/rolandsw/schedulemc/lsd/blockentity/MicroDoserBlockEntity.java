package de.rolandsw.schedulemc.lsd.blockentity;

import de.rolandsw.schedulemc.lsd.LSDDosage;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.items.LSDSolutionItem;
import de.rolandsw.schedulemc.lsd.items.LysergicAcidItem;
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
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

/**
 * Mikro-Dosierer - Dritter Schritt der LSD-Herstellung
 * Präzise Dosierung der Lysergsäure zu LSD-Lösung
 * Hat GUI mit Dosierungs-Slider (50-300 μg)
 */
public class MicroDoserBlockEntity extends BlockEntity implements IUtilityConsumer {

    private static final int PROCESS_TIME = 200; // 10 Sekunden

    private boolean lastActiveState = false;
    private int lysergic_acidCount = 0;
    private int dosageSlider = 50; // 0-100, default 50% = 175μg
    private int processProgress = 0;
    private ItemStack outputItem = ItemStack.EMPTY;
    private boolean isProcessing = false;
    private long lastGameTime = -1L;

    public MicroDoserBlockEntity(BlockPos pos, BlockState state) {
        super(LSDBlockEntities.MICRO_DOSER.get(), pos, state);
    }

    /**
     * Fügt Lysergsäure hinzu
     */
    public boolean addLysergicAcid(ItemStack stack) {
        if (!(stack.getItem() instanceof LysergicAcidItem)) return false;
        if (lysergic_acidCount >= 16) return false;
        if (!outputItem.isEmpty()) return false;

        lysergic_acidCount = Math.min(lysergic_acidCount + 1, 16);
        setChanged();
        return true;
    }

    /**
     * Setzt den Dosierungs-Slider (0-100)
     */
    public void setDosageSlider(int value) {
        this.dosageSlider = Math.max(0, Math.min(100, value));
        setChanged();
    }

    /**
     * Startet den Dosierungsprozess
     */
    public boolean startProcess() {
        if (lysergic_acidCount <= 0 || !outputItem.isEmpty() || isProcessing) {
            return false;
        }
        isProcessing = true;
        processProgress = 0;
        setChanged();
        return true;
    }

    /**
     * Extrahiert LSD-Lösung
     */
    public ItemStack extractOutput() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = outputItem.copy();
        outputItem = ItemStack.EMPTY;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;

        if (isProcessing && lysergic_acidCount > 0) {
            int prevProgress = processProgress;
            processProgress = (int) Math.min((long) processProgress + ticksPassed, PROCESS_TIME);

            if (processProgress >= PROCESS_TIME) {
                // Prozess abgeschlossen
                int micrograms = LSDDosage.getMicrogramsFromSlider(dosageSlider);
                LSDDosage dosage = LSDDosage.fromSliderValue(dosageSlider);

                // Mehr Lysergsäure = mehr Ladungen
                // Höhere Dosis = weniger Ladungen pro Lysergsäure
                int chargesPerLysergicAcid = Math.max(1, 10 - (micrograms / 50));
                int totalCharges = lysergic_acidCount * chargesPerLysergicAcid;

                outputItem = LSDSolutionItem.create(dosage, micrograms, totalCharges);
                lysergic_acidCount = 0;
                isProcessing = false;
                processProgress = 0;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (processProgress / 20 > prevProgress / 20) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    // Getter
    public boolean isProcessing() { return isProcessing; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public boolean hasInput() { return lysergic_acidCount > 0; }
    public int getLysergicAcidCount() { return lysergic_acidCount; }
    public int getDosageSlider() { return dosageSlider; }
    public int getCurrentMicrograms() { return LSDDosage.getMicrogramsFromSlider(dosageSlider); }
    public LSDDosage getCurrentDosage() { return LSDDosage.fromSliderValue(dosageSlider); }
    public float getProgress() { return (float) processProgress / PROCESS_TIME; }

    @Override
    public boolean isActivelyConsuming() {
        return isProcessing;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Lysergsaeure", lysergic_acidCount);
        tag.putInt("DosageSlider", dosageSlider);
        tag.putInt("Progress", processProgress);
        tag.putBoolean("Processing", isProcessing);
        tag.putLong("LastGameTime", lastGameTime);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lysergic_acidCount = tag.getInt("Lysergsaeure");
        dosageSlider = tag.getInt("DosageSlider");
        processProgress = tag.getInt("Progress");
        isProcessing = tag.getBoolean("Processing");
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;
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
