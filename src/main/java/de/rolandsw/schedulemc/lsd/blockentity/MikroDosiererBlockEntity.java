package de.rolandsw.schedulemc.lsd.blockentity;

import de.rolandsw.schedulemc.lsd.LSDDosage;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.items.LSDLoesungItem;
import de.rolandsw.schedulemc.lsd.items.LysergsaeureItem;
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
 * Mikro-Dosierer - Dritter Schritt der LSD-Herstellung
 * Präzise Dosierung der Lysergsäure zu LSD-Lösung
 * Hat GUI mit Dosierungs-Slider (50-300 μg)
 */
public class MikroDosiererBlockEntity extends BlockEntity implements IUtilityConsumer {

    private static final int PROCESS_TIME = 200; // 10 Sekunden

    private boolean lastActiveState = false;
    private int lysergsaeureCount = 0;
    private int dosageSlider = 50; // 0-100, default 50% = 175μg
    private int processProgress = 0;
    private ItemStack outputItem = ItemStack.EMPTY;
    private boolean isProcessing = false;

    public MikroDosiererBlockEntity(BlockPos pos, BlockState state) {
        super(LSDBlockEntities.MIKRO_DOSIERER.get(), pos, state);
    }

    /**
     * Fügt Lysergsäure hinzu
     */
    public boolean addLysergsaeure(ItemStack stack) {
        if (!(stack.getItem() instanceof LysergsaeureItem)) return false;
        if (lysergsaeureCount >= 16) return false;
        if (!outputItem.isEmpty()) return false;

        lysergsaeureCount++;
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
        if (lysergsaeureCount <= 0 || !outputItem.isEmpty() || isProcessing) {
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

        if (isProcessing && lysergsaeureCount > 0) {
            processProgress++;

            if (processProgress >= PROCESS_TIME) {
                // Prozess abgeschlossen
                int micrograms = LSDDosage.getMicrogramsFromSlider(dosageSlider);
                LSDDosage dosage = LSDDosage.fromSliderValue(dosageSlider);

                // Mehr Lysergsäure = mehr Ladungen
                // Höhere Dosis = weniger Ladungen pro Lysergsäure
                int chargesPerLysergsaeure = Math.max(1, 10 - (micrograms / 50));
                int totalCharges = lysergsaeureCount * chargesPerLysergsaeure;

                outputItem = LSDLoesungItem.create(dosage, micrograms, totalCharges);
                lysergsaeureCount = 0;
                isProcessing = false;
                processProgress = 0;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (processProgress % 20 == 0) {
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
    public boolean hasInput() { return lysergsaeureCount > 0; }
    public int getLysergsaeureCount() { return lysergsaeureCount; }
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
        tag.putInt("Lysergsaeure", lysergsaeureCount);
        tag.putInt("DosageSlider", dosageSlider);
        tag.putInt("Progress", processProgress);
        tag.putBoolean("Processing", isProcessing);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lysergsaeureCount = tag.getInt("Lysergsaeure");
        dosageSlider = tag.getInt("DosageSlider");
        processProgress = tag.getInt("Progress");
        isProcessing = tag.getBoolean("Processing");
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
