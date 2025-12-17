package de.rolandsw.schedulemc.mdma.blockentity;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import de.rolandsw.schedulemc.mdma.PillColor;
import de.rolandsw.schedulemc.mdma.PillDesign;
import de.rolandsw.schedulemc.mdma.items.BindemittelItem;
import de.rolandsw.schedulemc.mdma.items.EcstasyPillItem;
import de.rolandsw.schedulemc.mdma.items.FarbstoffItem;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.mdma.items.MDMAKristallItem;
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

import java.util.UUID;

/**
 * Pillen-Presse - Dritter und letzter Schritt der MDMA-Herstellung
 * HAT TIMING-MINIGAME!
 *
 * Spieler muss im richtigen Moment klicken:
 * - Zu früh = Pille zerbricht (schlechte Qualität)
 * - Perfekt = Beste Qualität
 * - Zu spät = Pille ungleichmäßig (mittlere Qualität)
 */
public class PillenPresseBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Timing-Konstanten
    public static final int PRESS_CYCLE_TICKS = 60; // 3 Sekunden pro Zyklus
    public static final int PERFECT_WINDOW_START = 25; // Perfektes Fenster Tick 25-35
    public static final int PERFECT_WINDOW_END = 35;
    public static final int GOOD_WINDOW_START = 20;
    public static final int GOOD_WINDOW_END = 40;

    // Zustände
    private int kristallCount = 0;
    private int bindemittelCount = 0;
    private MDMAQuality inputQuality = MDMAQuality.STANDARD;
    private PillDesign selectedDesign = PillDesign.TESLA;
    private PillColor selectedColor = PillColor.PINK;

    private boolean isMinigameActive = false;
    private int minigameTick = 0;
    private boolean waitingForPress = false;

    private ItemStack outputItem = ItemStack.EMPTY;
    private int outputCount = 0;

    private UUID activePlayer = null;
    private double lastTimingScore = 0;

    public PillenPresseBlockEntity(BlockPos pos, BlockState state) {
        super(MDMABlockEntities.PILLEN_PRESSE.get(), pos, state);
    }

    public boolean addMDMAKristall(ItemStack stack) {
        if (!(stack.getItem() instanceof MDMAKristallItem)) return false;
        if (kristallCount >= 16 || !outputItem.isEmpty()) return false;

        inputQuality = MDMAKristallItem.getQuality(stack);
        kristallCount++;
        setChanged();
        return true;
    }

    public boolean addBindemittel(ItemStack stack) {
        if (!(stack.getItem() instanceof BindemittelItem)) return false;
        if (bindemittelCount >= 16) return false;

        bindemittelCount++;
        setChanged();
        return true;
    }

    public boolean addFarbstoff(ItemStack stack) {
        if (!(stack.getItem() instanceof FarbstoffItem)) return false;

        selectedColor = FarbstoffItem.getColor(stack);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public void cycleDesign() {
        int next = (selectedDesign.ordinal() + 1) % PillDesign.values().length;
        selectedDesign = PillDesign.values()[next];
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void cycleColor() {
        int next = (selectedColor.ordinal() + 1) % PillColor.values().length;
        selectedColor = PillColor.values()[next];
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Startet das Minigame
     */
    public boolean startMinigame(UUID playerUUID) {
        if (kristallCount <= 0 || bindemittelCount <= 0 || !outputItem.isEmpty() || isMinigameActive) {
            return false;
        }

        activePlayer = playerUUID;
        isMinigameActive = true;
        waitingForPress = true;
        minigameTick = 0;
        lastTimingScore = 0;

        setChanged();
        return true;
    }

    /**
     * Spieler drückt den Press-Button
     * Gibt Timing-Score zurück (0.0 - 1.0)
     */
    public double pressButton() {
        if (!isMinigameActive || !waitingForPress) return 0;

        // Berechne Timing-Score
        double score;
        if (minigameTick >= PERFECT_WINDOW_START && minigameTick <= PERFECT_WINDOW_END) {
            // Perfekt!
            int perfectCenter = (PERFECT_WINDOW_START + PERFECT_WINDOW_END) / 2;
            double distanceFromPerfect = Math.abs(minigameTick - perfectCenter);
            score = 1.0 - (distanceFromPerfect / 10.0) * 0.1; // 0.9 - 1.0
        } else if (minigameTick >= GOOD_WINDOW_START && minigameTick <= GOOD_WINDOW_END) {
            // Gut
            score = 0.6 + (0.3 * (1.0 - Math.abs(minigameTick - 30) / 15.0));
        } else if (minigameTick < GOOD_WINDOW_START) {
            // Zu früh
            score = 0.2 + (minigameTick / (double) GOOD_WINDOW_START) * 0.3;
        } else {
            // Zu spät
            score = Math.max(0.1, 0.5 - ((minigameTick - GOOD_WINDOW_END) / 20.0) * 0.4);
        }

        lastTimingScore = score;
        waitingForPress = false;

        // Erstelle Pille basierend auf Score
        createPill(score);

        return score;
    }

    private void createPill(double timingScore) {
        // Kombiniere Timing-Score mit Input-Qualität
        MDMAQuality baseQuality = inputQuality;
        MDMAQuality finalQuality = MDMAQuality.fromTimingScore(timingScore);

        // Wenn Input-Qualität besser ist, nutze die bessere
        if (baseQuality.getLevel() > finalQuality.getLevel()) {
            finalQuality = baseQuality;
        }

        // Verbrauche Ressourcen
        int pillsToMake = Math.min(kristallCount, bindemittelCount);
        kristallCount -= pillsToMake;
        bindemittelCount -= pillsToMake;

        outputItem = EcstasyPillItem.create(finalQuality, selectedDesign, selectedColor, pillsToMake);
        outputCount = pillsToMake;

        isMinigameActive = false;
        activePlayer = null;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack extractOutput() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = outputItem.copy();
        outputItem = ItemStack.EMPTY;
        outputCount = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isMinigameActive && waitingForPress) {
            minigameTick++;

            // Timeout - automatisch schlechte Qualität
            if (minigameTick >= PRESS_CYCLE_TICKS) {
                pressButton(); // Erzwinge Press mit schlechtem Timing
            }

            // Update für GUI
            if (minigameTick % 2 == 0) {
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

    public void cancelMinigame() {
        isMinigameActive = false;
        waitingForPress = false;
        minigameTick = 0;
        activePlayer = null;
        setChanged();
    }

    // Getter
    public boolean isMinigameActive() { return isMinigameActive; }
    public boolean isWaitingForPress() { return waitingForPress; }
    public int getMinigameTick() { return minigameTick; }
    public int getPressCycleTicks() { return PRESS_CYCLE_TICKS; }
    public float getMinigameProgress() { return (float) minigameTick / PRESS_CYCLE_TICKS; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public boolean canStart() { return kristallCount > 0 && bindemittelCount > 0 && outputItem.isEmpty() && !isMinigameActive; }
    public int getKristallCount() { return kristallCount; }
    public int getBindemittelCount() { return bindemittelCount; }
    public PillDesign getSelectedDesign() { return selectedDesign; }
    public PillColor getSelectedColor() { return selectedColor; }
    public MDMAQuality getInputQuality() { return inputQuality; }
    public double getLastTimingScore() { return lastTimingScore; }
    public UUID getActivePlayer() { return activePlayer; }

    /**
     * Gibt aktuelle Zone im Timing-Fenster zurück
     * 0 = zu früh, 1 = gut, 2 = perfekt, 3 = zu spät
     */
    public int getCurrentZone() {
        if (minigameTick >= PERFECT_WINDOW_START && minigameTick <= PERFECT_WINDOW_END) return 2;
        if (minigameTick >= GOOD_WINDOW_START && minigameTick <= GOOD_WINDOW_END) return 1;
        if (minigameTick < GOOD_WINDOW_START) return 0;
        return 3;
    }

    @Override
    public boolean isActivelyConsuming() {
        return isMinigameActive && waitingForPress;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Kristall", kristallCount);
        tag.putInt("Bindemittel", bindemittelCount);
        tag.putString("Quality", inputQuality.name());
        tag.putString("Design", selectedDesign.name());
        tag.putString("Color", selectedColor.name());
        tag.putBoolean("MinigameActive", isMinigameActive);
        tag.putInt("MinigameTick", minigameTick);
        tag.putBoolean("WaitingForPress", waitingForPress);
        tag.putDouble("LastScore", lastTimingScore);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
        if (activePlayer != null) {
            tag.putUUID("ActivePlayer", activePlayer);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        kristallCount = tag.getInt("Kristall");
        bindemittelCount = tag.getInt("Bindemittel");
        if (tag.contains("Quality")) {
            try { inputQuality = MDMAQuality.valueOf(tag.getString("Quality")); }
            catch (IllegalArgumentException e) { inputQuality = MDMAQuality.STANDARD; }
        }
        if (tag.contains("Design")) {
            try { selectedDesign = PillDesign.valueOf(tag.getString("Design")); }
            catch (IllegalArgumentException e) { selectedDesign = PillDesign.TESLA; }
        }
        if (tag.contains("Color")) {
            try { selectedColor = PillColor.valueOf(tag.getString("Color")); }
            catch (IllegalArgumentException e) { selectedColor = PillColor.PINK; }
        }
        isMinigameActive = tag.getBoolean("MinigameActive");
        minigameTick = tag.getInt("MinigameTick");
        waitingForPress = tag.getBoolean("WaitingForPress");
        lastTimingScore = tag.getDouble("LastScore");
        outputItem = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        if (tag.hasUUID("ActivePlayer")) {
            activePlayer = tag.getUUID("ActivePlayer");
        }
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
