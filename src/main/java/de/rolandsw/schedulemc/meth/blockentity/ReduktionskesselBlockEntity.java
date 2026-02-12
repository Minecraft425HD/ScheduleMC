package de.rolandsw.schedulemc.meth.blockentity;

import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.meth.items.MethPasteItem;
import de.rolandsw.schedulemc.meth.items.RohMethItem;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Reduktionskessel - Zweiter Schritt der Meth-Herstellung
 * KRITISCH: Temperaturkontrolle mit Explosionsgefahr!
 *
 * Temperaturbereiche:
 * < 80°C = zu kalt, Prozess pausiert
 * 80-120°C = optimal, beste Qualität
 * 120-150°C = gefährlich, niedrigere Qualität
 * > 150°C = EXPLOSION!
 */
public class ReduktionskesselBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Temperatur-Konstanten
    public static final int TEMP_MIN = 20;          // Raumtemperatur
    public static final int TEMP_COLD_MAX = 79;     // Unter 80 = zu kalt
    public static final int TEMP_OPTIMAL_MIN = 80;  // Optimal Start
    public static final int TEMP_OPTIMAL_MAX = 120; // Optimal Ende
    public static final int TEMP_DANGER_MAX = 150;  // Über 150 = Explosion
    public static final int TEMP_EXPLOSION = 151;   // Explosions-Schwelle

    // Temperatur-Änderungsraten (pro Tick)
    private static final float TEMP_RISE_RATE = 1.5f;   // °C pro Tick wenn Heizung an
    private static final float TEMP_FALL_RATE = 0.8f;   // °C pro Tick Abkühlung
    private static final float TEMP_PROCESS_HEAT = 0.3f; // Zusätzliche Wärme durch chemischen Prozess

    // Prozess-Konstanten
    private static final int PROCESS_TIME = 400; // 20 Sekunden (400 Ticks bei 20 TPS)
    private static final int EXPLOSION_POWER = 4; // Stärke der Explosion

    // Zustandsvariablen
    private float currentTemperature = TEMP_MIN;
    private boolean isHeating = false;
    private boolean isProcessing = false;
    private int processProgress = 0;
    private int optimalTimeTicks = 0;  // Zeit im optimalen Bereich
    private int dangerTimeTicks = 0;   // Zeit im gefährlichen Bereich

    private ItemStack inputItem = ItemStack.EMPTY;
    private ItemStack outputItem = ItemStack.EMPTY;
    private MethQuality inputQuality = MethQuality.SCHLECHT;

    private UUID activePlayer = null; // Spieler der gerade die GUI bedient

    public ReduktionskesselBlockEntity(BlockPos pos, BlockState state) {
        super(MethBlockEntities.REDUKTIONSKESSEL.get(), pos, state);
    }

    /**
     * Fügt Meth-Paste hinzu
     */
    public boolean addMethPaste(ItemStack stack) {
        if (!(stack.getItem() instanceof MethPasteItem) || !inputItem.isEmpty() || !outputItem.isEmpty()) {
            return false;
        }

        inputItem = stack.copy();
        inputItem.setCount(1);
        inputQuality = MethPasteItem.getQuality(stack);
        processProgress = 0;
        optimalTimeTicks = 0;
        dangerTimeTicks = 0;
        isProcessing = false;

        setChanged();
        return true;
    }

    /**
     * Extrahiert das fertige Roh-Meth
     */
    public ItemStack extractOutput() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = outputItem.copy();
        outputItem = ItemStack.EMPTY;
        inputItem = ItemStack.EMPTY;
        processProgress = 0;
        optimalTimeTicks = 0;
        dangerTimeTicks = 0;
        currentTemperature = TEMP_MIN;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return result;
    }

    /**
     * Setzt Heizung an/aus (vom GUI-Button gesteuert)
     */
    public void setHeating(boolean heating) {
        this.isHeating = heating;
        setChanged();
    }

    /**
     * Setzt den aktiven Spieler (für GUI)
     */
    public void setActivePlayer(UUID playerUUID) {
        this.activePlayer = playerUUID;
    }

    /**
     * Prüft ob ein Spieler aktiv ist
     */
    public boolean hasActivePlayer() {
        return activePlayer != null;
    }

    /**
     * Entfernt aktiven Spieler (wenn GUI geschlossen wird)
     */
    public void clearActivePlayer() {
        this.activePlayer = null;
        this.isHeating = false;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        // Temperatur-Update
        if (isHeating && !outputItem.isEmpty()) {
            // Heizung abschalten wenn fertig
            isHeating = false;
        }

        if (isHeating) {
            currentTemperature += TEMP_RISE_RATE;

            // Extra Wärme wenn Prozess läuft
            if (isProcessing) {
                currentTemperature += TEMP_PROCESS_HEAT;
            }
        } else {
            // Abkühlung
            if (currentTemperature > TEMP_MIN) {
                currentTemperature -= TEMP_FALL_RATE;
                if (currentTemperature < TEMP_MIN) {
                    currentTemperature = TEMP_MIN;
                }
            }
        }

        // EXPLOSION CHECK!
        if (currentTemperature >= TEMP_EXPLOSION) {
            explode();
            return;
        }

        // Prozess-Update wenn Paste vorhanden
        if (!inputItem.isEmpty() && outputItem.isEmpty()) {

            // Prüfe ob Temperatur hoch genug ist
            if (currentTemperature >= TEMP_OPTIMAL_MIN) {
                isProcessing = true;
                processProgress++;

                // Zähle Zeit in verschiedenen Temperaturzonen
                if (currentTemperature <= TEMP_OPTIMAL_MAX) {
                    optimalTimeTicks++;
                } else if (currentTemperature <= TEMP_DANGER_MAX) {
                    dangerTimeTicks++;
                }

                // Prozess abgeschlossen?
                if (processProgress >= PROCESS_TIME) {
                    completeProcess();
                    changed = true;
                }
            } else {
                // Zu kalt - Prozess pausiert
                isProcessing = false;
            }
        } else {
            isProcessing = false;
        }

        // Update jede Sekunde für Client-Sync
        if (level.getGameTime() % 5 == 0) {
            changed = true;
        }

        if (changed) {
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

    // ═══════════════════════════════════════════════════════════════════════════
    // IUtilityConsumer Implementation
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn Heizung an oder Prozess läuft (hoher Stromverbrauch!)
        return isHeating || isProcessing;
    }

    /**
     * Berechnet finale Qualität und erstellt Output
     */
    private void completeProcess() {
        // Berechne Prozentsatz der Zeit im optimalen Bereich
        double optimalPercent = (double) optimalTimeTicks / PROCESS_TIME;
        double dangerPercent = (double) dangerTimeTicks / PROCESS_TIME;

        // Qualitätsberechnung
        MethQuality finalQuality;

        if (dangerPercent > 0.3) {
            // Zu viel Zeit im gefährlichen Bereich -> Qualitätsverlust
            finalQuality = inputQuality.downgrade();
        } else if (optimalPercent >= 0.9) {
            // 90%+ optimal -> Chance auf Blue Sky!
            if (level != null && level.random.nextFloat() < 0.3) { // 30% Chance
                finalQuality = MethQuality.LEGENDAER;
            } else {
                finalQuality = inputQuality.upgrade();
            }
        } else if (optimalPercent >= 0.7) {
            // 70-89% optimal -> Upgrade möglich
            finalQuality = level != null && level.random.nextFloat() < 0.5 ?
                    inputQuality.upgrade() : inputQuality;
        } else {
            // < 70% optimal -> Keine Verbesserung
            finalQuality = inputQuality;
        }

        outputItem = RohMethItem.create(finalQuality, 1);
        isProcessing = false;

        // Kühle ab nach Abschluss
        currentTemperature = Math.max(TEMP_MIN, currentTemperature - 30);
    }

    /**
     * EXPLOSION!
     */
    private void explode() {
        if (level == null) return;

        Level lvl = level;
        BlockPos explodePos = worldPosition;

        // Entferne Block
        lvl.removeBlock(worldPosition, false);

        // Erstelle Explosion
        lvl.explode(null, explodePos.getX() + 0.5, explodePos.getY() + 0.5, explodePos.getZ() + 0.5,
                EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);

        // Schaden an nahestehenden Spielern
        for (Player player : lvl.players()) {
            if (player.distanceToSqr(explodePos.getX(), explodePos.getY(), explodePos.getZ()) < 64) {
                player.hurt(player.damageSources().explosion(null, null), 10.0f);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER für GUI
    // ═══════════════════════════════════════════════════════════

    public float getTemperature() {
        return currentTemperature;
    }

    public int getTemperatureInt() {
        return (int) currentTemperature;
    }

    public boolean isHeating() {
        return isHeating;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public int getProcessProgress() {
        return processProgress;
    }

    public int getProcessTime() {
        return PROCESS_TIME;
    }

    public float getProgressPercent() {
        return (float) processProgress / PROCESS_TIME;
    }

    public boolean hasInput() {
        return !inputItem.isEmpty();
    }

    public boolean hasOutput() {
        return !outputItem.isEmpty();
    }

    public MethQuality getInputQuality() {
        return inputQuality;
    }

    /**
     * Gibt erwartete Qualität basierend auf aktuellem Temperaturverlauf zurück
     */
    public MethQuality getExpectedQuality() {
        if (processProgress == 0) return inputQuality;

        double optimalPercent = (double) optimalTimeTicks / Math.max(1, processProgress);
        double dangerPercent = (double) dangerTimeTicks / Math.max(1, processProgress);

        if (dangerPercent > 0.3) {
            return inputQuality.downgrade();
        } else if (optimalPercent >= 0.8) {
            return inputQuality.upgrade();
        }
        return inputQuality;
    }

    /**
     * Gibt Temperaturzone als String zurück
     */
    public String getTemperatureZone() {
        if (currentTemperature < TEMP_OPTIMAL_MIN) {
            return "§9ZU KALT";
        } else if (currentTemperature <= TEMP_OPTIMAL_MAX) {
            return "§aOPTIMAL";
        } else if (currentTemperature <= TEMP_DANGER_MAX) {
            return "§c⚠ GEFAHR";
        } else {
            return "§4§l☠ KRITISCH!";
        }
    }

    /**
     * Gibt Temperaturfarbe für GUI zurück
     */
    public int getTemperatureColor() {
        if (currentTemperature < TEMP_OPTIMAL_MIN) {
            return 0x5555FF; // Blau - zu kalt
        } else if (currentTemperature <= TEMP_OPTIMAL_MAX) {
            return 0x55FF55; // Grün - optimal
        } else if (currentTemperature <= TEMP_DANGER_MAX) {
            return 0xFFAA00; // Orange - gefährlich
        } else {
            return 0xFF5555; // Rot - kritisch
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NBT Speicherung
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putFloat("Temperature", currentTemperature);
        tag.putBoolean("Heating", isHeating);
        tag.putBoolean("Processing", isProcessing);
        tag.putInt("Progress", processProgress);
        tag.putInt("OptimalTime", optimalTimeTicks);
        tag.putInt("DangerTime", dangerTimeTicks);

        if (!inputItem.isEmpty()) {
            CompoundTag inputTag = new CompoundTag();
            inputItem.save(inputTag);
            tag.put("Input", inputTag);
            tag.putString("InputQuality", inputQuality.name());
        }

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

        currentTemperature = tag.getFloat("Temperature");
        isHeating = tag.getBoolean("Heating");
        isProcessing = tag.getBoolean("Processing");
        processProgress = tag.getInt("Progress");
        optimalTimeTicks = tag.getInt("OptimalTime");
        dangerTimeTicks = tag.getInt("DangerTime");

        inputItem = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputItem = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;

        if (tag.contains("InputQuality")) {
            try {
                inputQuality = MethQuality.valueOf(tag.getString("InputQuality"));
            } catch (IllegalArgumentException e) {
                inputQuality = MethQuality.SCHLECHT;
            }
        }

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
