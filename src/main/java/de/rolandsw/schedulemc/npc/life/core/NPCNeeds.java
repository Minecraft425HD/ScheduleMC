package de.rolandsw.schedulemc.npc.life.core;

import de.rolandsw.schedulemc.npc.life.NPCLifeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * NPCNeeds - Verwaltet die Bedürfnisse eines NPCs (Energie & Sicherheit)
 *
 * Werte: 0 = kritisch, 100 = voll befriedigt
 */
public class NPCNeeds {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final float MIN_VALUE = 0.0f;
    public static final float MAX_VALUE = 100.0f;

    /** Kritischer Schwellenwert - NPC muss handeln */
    public static final float CRITICAL_THRESHOLD = 20.0f;

    /** Niedriger Schwellenwert - NPC wird unruhig */
    public static final float LOW_THRESHOLD = 40.0f;

    /** Normaler Schwellenwert */
    public static final float NORMAL_THRESHOLD = 60.0f;

    // Energie-Regeneration beim Schlafen (pro Stunde = 1000 Ticks)
    private static final float ENERGY_REGEN_PER_HOUR = 20.0f;

    // Sicherheits-Modifikatoren
    private static final float SAFETY_HOME_BONUS = 40.0f;
    private static final float SAFETY_POLICE_NEARBY_BONUS = 20.0f;
    private static final float SAFETY_FRIEND_NEARBY_BONUS = 10.0f;
    private static final float SAFETY_NIGHT_OUTDOOR_PENALTY = -20.0f;
    private static final float SAFETY_CRIME_NEARBY_PENALTY = -30.0f;
    private static final float SAFETY_CRIMINAL_NEARBY_PENALTY = -50.0f;
    private static final float SAFETY_WEAPON_VISIBLE_PENALTY = -40.0f;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private float energy = MAX_VALUE;
    private float safety = MAX_VALUE;

    // Tracking für Regeneration
    private boolean isSleeping = false;
    private int ticksSinceLastUpdate = 0;
    private static final int UPDATE_INTERVAL = 20; // Alle 20 Ticks (1 Sekunde)

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Tick aufgerufen - aktualisiert Bedürfnisse
     */
    public void tick() {
        ticksSinceLastUpdate++;

        if (ticksSinceLastUpdate >= UPDATE_INTERVAL) {
            ticksSinceLastUpdate = 0;

            // Energie-Decay (wenn nicht schläft)
            if (!isSleeping) {
                float decay = NeedType.ENERGY.getDecayPerTick() * UPDATE_INTERVAL;
                energy = Math.max(MIN_VALUE, energy - decay);
            } else {
                // Energie-Regeneration beim Schlafen
                float regen = (ENERGY_REGEN_PER_HOUR / 1000.0f) * UPDATE_INTERVAL;
                energy = Math.min(MAX_VALUE, energy + regen);
            }
        }
    }

    /**
     * Berechnet die Sicherheit basierend auf der Umgebung
     * Sollte periodisch aufgerufen werden (nicht jeden Tick)
     */
    public void calculateSafety(Level level, BlockPos npcPos, @Nullable BlockPos homePos,
                                 List<? extends Player> nearbyPlayers, boolean isNight,
                                 boolean policeNearby, boolean friendNearby,
                                 boolean recentCrimeNearby, @Nullable Player knownCriminal) {

        float baseSafety = 50.0f; // Basis-Sicherheit

        // Zu Hause Bonus (5 Blöcke Radius, quadriert)
        if (homePos != null && npcPos.distSqr(homePos) < NPCLifeConstants.Needs.HOME_SAFETY_CHECK_RADIUS_SQR) {
            baseSafety += SAFETY_HOME_BONUS;
        }

        // Polizei in der Nähe
        if (policeNearby) {
            baseSafety += SAFETY_POLICE_NEARBY_BONUS;
        }

        // Freund in der Nähe
        if (friendNearby) {
            baseSafety += SAFETY_FRIEND_NEARBY_BONUS;
        }

        // Nacht und draußen (mehr als 5 Blöcke vom Heim entfernt)
        if (isNight && (homePos == null || npcPos.distSqr(homePos) > NPCLifeConstants.Needs.NIGHT_OUTDOOR_CHECK_RADIUS_SQR)) {
            baseSafety += SAFETY_NIGHT_OUTDOOR_PENALTY;
        }

        // Kürzliches Verbrechen in der Nähe
        if (recentCrimeNearby) {
            baseSafety += SAFETY_CRIME_NEARBY_PENALTY;
        }

        // Bekannter Verbrecher in der Nähe
        if (knownCriminal != null) {
            baseSafety += SAFETY_CRIMINAL_NEARBY_PENALTY;
        }

        // Spieler mit Waffe in der Hand prüfen
        for (Player player : nearbyPlayers) {
            if (isHoldingWeapon(player)) {
                baseSafety += SAFETY_WEAPON_VISIBLE_PENALTY;
                break; // Nur einmal anwenden
            }
        }

        // Clamp auf gültigen Bereich
        this.safety = Math.max(MIN_VALUE, Math.min(MAX_VALUE, baseSafety));
    }

    /**
     * Prüft ob ein Spieler eine Waffe hält
     */
    private boolean isHoldingWeapon(Player player) {
        var mainHand = player.getMainHandItem();
        var offHand = player.getOffhandItem();

        // Prüfe auf Schwert, Axt, oder Mod-Waffen
        return mainHand.getItem() instanceof net.minecraft.world.item.SwordItem ||
               mainHand.getItem() instanceof net.minecraft.world.item.AxeItem ||
               offHand.getItem() instanceof net.minecraft.world.item.SwordItem ||
               offHand.getItem() instanceof net.minecraft.world.item.AxeItem;
    }

    // ═══════════════════════════════════════════════════════════
    // SATISFACTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Befriedigt ein Bedürfnis
     * @param type Bedürfnis-Typ
     * @param amount Menge (positiv)
     */
    public void satisfy(NeedType type, float amount) {
        switch (type) {
            case ENERGY -> energy = Math.min(MAX_VALUE, energy + amount);
            case SAFETY -> safety = Math.min(MAX_VALUE, safety + amount);
        }
    }

    /**
     * Reduziert ein Bedürfnis (z.B. durch Stress)
     * @param type Bedürfnis-Typ
     * @param amount Menge (positiv, wird abgezogen)
     */
    public void reduce(NeedType type, float amount) {
        switch (type) {
            case ENERGY -> energy = Math.max(MIN_VALUE, energy - amount);
            case SAFETY -> safety = Math.max(MIN_VALUE, safety - amount);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den Wert eines Bedürfnisses zurück
     */
    public float getValue(NeedType type) {
        return switch (type) {
            case ENERGY -> energy;
            case SAFETY -> safety;
        };
    }

    /**
     * Prüft ob ein Bedürfnis kritisch ist
     */
    public boolean isCritical(NeedType type) {
        return getValue(type) < CRITICAL_THRESHOLD;
    }

    /**
     * Prüft ob ein Bedürfnis niedrig ist
     */
    public boolean isLow(NeedType type) {
        return getValue(type) < LOW_THRESHOLD;
    }

    /**
     * Prüft ob irgendein Bedürfnis kritisch ist
     */
    public boolean hasCriticalNeed() {
        return isCritical(NeedType.ENERGY) || isCritical(NeedType.SAFETY);
    }

    /**
     * Gibt das kritischste Bedürfnis zurück
     */
    @Nullable
    public NeedType getMostCritical() {
        NeedType mostCritical = null;
        float lowestValue = MAX_VALUE;

        for (NeedType type : NeedType.values()) {
            float value = getValue(type);
            if (value < lowestValue) {
                lowestValue = value;
                mostCritical = type;
            }
        }

        // Nur zurückgeben wenn tatsächlich niedrig
        return (lowestValue < LOW_THRESHOLD) ? mostCritical : null;
    }

    /**
     * Berechnet die durchschnittliche Zufriedenheit (0-100)
     */
    public float getOverallSatisfaction() {
        return (energy + safety) / 2.0f;
    }

    // ═══════════════════════════════════════════════════════════
    // SLEEPING
    // ═══════════════════════════════════════════════════════════

    public void startSleeping() {
        this.isSleeping = true;
    }

    public void stopSleeping() {
        this.isSleeping = false;
    }

    public boolean isSleeping() {
        return isSleeping;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = Math.max(MIN_VALUE, Math.min(MAX_VALUE, energy));
    }

    public float getSafety() {
        return safety;
    }

    public void setSafety(float safety) {
        this.safety = Math.max(MIN_VALUE, Math.min(MAX_VALUE, safety));
    }

    /**
     * Modifiziert den Sicherheitswert um einen Betrag
     * @param amount Positiv = erhöhen, Negativ = reduzieren
     */
    public void modifySafety(float amount) {
        this.safety = Math.max(MIN_VALUE, Math.min(MAX_VALUE, this.safety + amount));
    }

    /**
     * Modifiziert den Sicherheitswert um einen Betrag (int overload)
     */
    public void modifySafety(int amount) {
        modifySafety((float) amount);
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Energy", energy);
        tag.putFloat("Safety", safety);
        tag.putBoolean("IsSleeping", isSleeping);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.energy = tag.getFloat("Energy");
        this.safety = tag.getFloat("Safety");
        this.isSleeping = tag.getBoolean("IsSleeping");

        // Validierung
        this.energy = Math.max(MIN_VALUE, Math.min(MAX_VALUE, energy));
        this.safety = Math.max(MIN_VALUE, Math.min(MAX_VALUE, safety));
    }

    public static NPCNeeds fromTag(CompoundTag tag) {
        NPCNeeds needs = new NPCNeeds();
        needs.load(tag);
        return needs;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("NPCNeeds{energy=%.1f, safety=%.1f, sleeping=%s}",
            energy, safety, isSleeping);
    }
}
