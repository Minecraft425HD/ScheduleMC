package de.rolandsw.schedulemc.npc.personality;

import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.ThreadLocalRandom;

/**
 * NPC Personality Trait - Charaktereigenschaften für NPCs
 *
 * Beeinflusst:
 * - Preise beim Handel
 * - Verhalten bei Diebstahl
 * - Dialog-Optionen
 * - Polizei-Ruf-Wahrscheinlichkeit
 */
public enum NPCPersonalityTrait {

    /**
     * Freundlich - Bessere Preise, verzeiht kleine Vergehen
     */
    FRIENDLY(
        "Freundlich",
        "§a",
        0.80,    // 20% Rabatt
        0.05,    // 5% Chance Gratis-Item
        0.30,    // 30% Polizei-Ruf Chance (niedrig)
        "Dieser NPC ist freundlich und gibt bessere Preise."
    ),

    /**
     * Gierig - Höhere Preise, nie Rabatte
     */
    GREEDY(
        "Gierig",
        "§6",
        1.30,    // 30% Aufpreis
        0.0,     // Keine Gratis-Items
        0.90,    // 90% Polizei-Ruf Chance (sehr hoch)
        "Dieser NPC ist gierig und verlangt hohe Preise."
    ),

    /**
     * Großzügig - Gelegentlich Gratis-Items, faire Preise
     */
    GENEROUS(
        "Großzügig",
        "§d",
        0.90,    // 10% Rabatt
        0.15,    // 15% Chance Gratis-Item
        0.50,    // 50% Polizei-Ruf Chance (normal)
        "Dieser NPC ist großzügig und gibt manchmal Geschenke."
    ),

    /**
     * Misstrauisch - Leicht höhere Preise, ruft schnell Polizei
     */
    SUSPICIOUS(
        "Misstrauisch",
        "§c",
        1.10,    // 10% Aufpreis
        0.0,     // Keine Gratis-Items
        0.95,    // 95% Polizei-Ruf Chance (extrem hoch)
        "Dieser NPC ist misstrauisch und ruft schnell die Polizei."
    ),

    /**
     * Neutral - Standard-Verhalten, keine Besonderheiten
     */
    NEUTRAL(
        "Neutral",
        "§7",
        1.00,    // Normale Preise
        0.02,    // 2% Chance Gratis-Item
        0.70,    // 70% Polizei-Ruf Chance (normal-hoch)
        "Dieser NPC ist neutral und verhält sich durchschnittlich."
    );

    // ═══════════════════════════════════════════════════════════
    // PROPERTIES
    // ═══════════════════════════════════════════════════════════

    private final String displayName;
    private final String colorCode;
    private final double priceMultiplier;
    private final double freeItemChance;
    private final double policeCallChance;
    private final String description;

    NPCPersonalityTrait(String displayName, String colorCode, double priceMultiplier,
                       double freeItemChance, double policeCallChance, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priceMultiplier = priceMultiplier;
        this.freeItemChance = freeItemChance;
        this.policeCallChance = policeCallChance;
        this.description = description;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public double getFreeItemChance() {
        return freeItemChance;
    }

    public double getPoliceCallChance() {
        return policeCallChance;
    }

    public String getDescription() {
        return description;
    }

    // ═══════════════════════════════════════════════════════════
    // BEHAVIOR METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Sollte NPC Polizei rufen?
     */
    public boolean shouldCallPolice() {
        return ThreadLocalRandom.current().nextDouble() < policeCallChance;
    }

    /**
     * Sollte NPC Gratis-Item geben?
     */
    public boolean shouldGiveFreeItem() {
        return ThreadLocalRandom.current().nextDouble() < freeItemChance;
    }

    /**
     * Berechnet finalen Preis basierend auf Trait
     */
    public double calculatePrice(double basePrice) {
        return basePrice * priceMultiplier;
    }

    // ═══════════════════════════════════════════════════════════
    // RANDOM GENERATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Generiert zufälligen Personality Trait
     *
     * Wahrscheinlichkeiten:
     * - NEUTRAL: 40%
     * - FRIENDLY: 20%
     * - GENEROUS: 15%
     * - GREEDY: 15%
     * - SUSPICIOUS: 10%
     */
    public static NPCPersonalityTrait random() {
        double rand = ThreadLocalRandom.current().nextDouble();

        if (rand < 0.40) return NEUTRAL;        // 40%
        if (rand < 0.60) return FRIENDLY;       // 20%
        if (rand < 0.75) return GENEROUS;       // 15%
        if (rand < 0.90) return GREEDY;         // 15%
        return SUSPICIOUS;                       // 10%
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("PersonalityOrdinal", this.ordinal());
        return tag;
    }

    public static NPCPersonalityTrait load(CompoundTag tag) {
        if (tag.contains("PersonalityOrdinal")) {
            int ordinal = tag.getInt("PersonalityOrdinal");
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
        }
        return NEUTRAL;  // Default
    }

    // ═══════════════════════════════════════════════════════════
    // DISPLAY
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("%s%s§r (×%.2f, Free:%.1f%%, Police:%.1f%%)",
            colorCode, displayName, priceMultiplier, freeItemChance * 100, policeCallChance * 100);
    }

    public String getFormattedName() {
        return colorCode + displayName + "§r";
    }
}
