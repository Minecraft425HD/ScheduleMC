package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.production.core.DrugType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

import java.util.EnumMap;
import java.util.Map;

/**
 * NPC Addiction Profile - Definiert Suchtlevel und Drogentyp-Präferenzen pro NPC
 *
 * Jeder NPC hat individuelle Präferenzen für verschiedene Drogentypen (0-100).
 * Das Suchtlevel beeinflusst:
 * - Welche Drogentypen der NPC kauft
 * - Wie dringend er kaufen möchte (höhere Akzeptanz bei schlechten Deals)
 * - Die maximale Menge die er kaufen will
 */
public class NPCAddictionProfile {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final int MIN_PREFERENCE = 0;
    public static final int MAX_PREFERENCE = 100;
    public static final int DEFAULT_PREFERENCE = 0;

    // Schwellenwerte für Kaufbereitschaft
    public static final int THRESHOLD_NO_INTEREST = 10;      // Unter 10: Kein Interesse
    public static final int THRESHOLD_LOW_INTEREST = 30;     // 10-30: Geringes Interesse
    public static final int THRESHOLD_MEDIUM_INTEREST = 60;  // 30-60: Mittleres Interesse
    public static final int THRESHOLD_HIGH_INTEREST = 80;    // 60-80: Hohes Interesse
    // 80-100: Sucht - kauft fast alles

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final Map<DrugType, Integer> preferences;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public NPCAddictionProfile() {
        this.preferences = new EnumMap<>(DrugType.class);
        for (DrugType type : DrugType.values()) {
            preferences.put(type, DEFAULT_PREFERENCE);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PREFERENCE MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt die Präferenz für einen Drogentyp
     */
    public void setPreference(DrugType type, int value) {
        preferences.put(type, Math.max(MIN_PREFERENCE, Math.min(MAX_PREFERENCE, value)));
    }

    /**
     * Holt die Präferenz für einen Drogentyp
     */
    public int getPreference(DrugType type) {
        return preferences.getOrDefault(type, DEFAULT_PREFERENCE);
    }

    /**
     * Erhöht die Präferenz (bei Konsum)
     */
    public void increasePreference(DrugType type, int amount) {
        int current = getPreference(type);
        setPreference(type, current + amount);
    }

    /**
     * Reduziert die Präferenz (bei Entzug/Zeit)
     */
    public void decreasePreference(DrugType type, int amount) {
        int current = getPreference(type);
        setPreference(type, current - amount);
    }

    // ═══════════════════════════════════════════════════════════
    // INTEREST LEVEL CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob der NPC an diesem Drogentyp interessiert ist
     */
    public boolean isInterestedIn(DrugType type) {
        return getPreference(type) >= THRESHOLD_NO_INTEREST;
    }

    /**
     * Gibt das Interesse-Level als String zurück
     */
    public InterestLevel getInterestLevel(DrugType type) {
        int pref = getPreference(type);
        if (pref < THRESHOLD_NO_INTEREST) return InterestLevel.NONE;
        if (pref < THRESHOLD_LOW_INTEREST) return InterestLevel.LOW;
        if (pref < THRESHOLD_MEDIUM_INTEREST) return InterestLevel.MEDIUM;
        if (pref < THRESHOLD_HIGH_INTEREST) return InterestLevel.HIGH;
        return InterestLevel.ADDICTED;
    }

    /**
     * Berechnet den Score-Bonus basierend auf Suchtlevel
     * Höhere Sucht = höherer Bonus (NPC will unbedingt kaufen)
     */
    public int getAddictionScoreBonus(DrugType type) {
        int pref = getPreference(type);
        if (pref < THRESHOLD_NO_INTEREST) return -20; // Kein Interesse = Malus
        if (pref < THRESHOLD_LOW_INTEREST) return 0;  // Geringes Interesse = neutral
        if (pref < THRESHOLD_MEDIUM_INTEREST) return 10; // Mittleres Interesse = kleiner Bonus
        if (pref < THRESHOLD_HIGH_INTEREST) return 20;   // Hohes Interesse = guter Bonus
        return 25; // Sucht = maximaler Bonus
    }

    /**
     * Berechnet den Preis-Toleranz-Multiplikator
     * Süchtige NPCs akzeptieren höhere Preise
     */
    public float getPriceToleranceMultiplier(DrugType type) {
        int pref = getPreference(type);
        if (pref < THRESHOLD_NO_INTEREST) return 0.7f;  // Will Schnäppchen
        if (pref < THRESHOLD_LOW_INTEREST) return 0.9f;
        if (pref < THRESHOLD_MEDIUM_INTEREST) return 1.0f;
        if (pref < THRESHOLD_HIGH_INTEREST) return 1.1f;
        return 1.3f; // Sucht = zahlt 30% mehr
    }

    /**
     * Gibt den primären Drogentyp zurück (höchste Präferenz)
     */
    public DrugType getPrimaryDrugType() {
        DrugType primary = DrugType.TOBACCO;
        int maxPref = 0;
        for (Map.Entry<DrugType, Integer> entry : preferences.entrySet()) {
            if (entry.getValue() > maxPref) {
                maxPref = entry.getValue();
                primary = entry.getKey();
            }
        }
        return primary;
    }

    // ═══════════════════════════════════════════════════════════
    // RANDOM GENERATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Generiert ein zufälliges Addiction Profile für einen neuen NPC
     *
     * Verteilung:
     * - 40% NPCs haben keine Sucht (alle Präferenzen 0-20)
     * - 30% NPCs haben leichte Tendenz (eine Präferenz 20-50)
     * - 20% NPCs haben moderate Sucht (eine Präferenz 50-80)
     * - 10% NPCs haben starke Sucht (eine Präferenz 80-100)
     */
    public static NPCAddictionProfile generateRandom(RandomSource random) {
        NPCAddictionProfile profile = new NPCAddictionProfile();

        float roll = random.nextFloat();

        if (roll < 0.40f) {
            // Keine Sucht - niedrige zufällige Werte
            for (DrugType type : DrugType.values()) {
                profile.setPreference(type, random.nextInt(21)); // 0-20
            }
        } else if (roll < 0.70f) {
            // Leichte Tendenz
            DrugType preferredType = DrugType.values()[random.nextInt(DrugType.values().length)];
            profile.setPreference(preferredType, 20 + random.nextInt(31)); // 20-50
        } else if (roll < 0.90f) {
            // Moderate Sucht
            DrugType preferredType = DrugType.values()[random.nextInt(DrugType.values().length)];
            profile.setPreference(preferredType, 50 + random.nextInt(31)); // 50-80
        } else {
            // Starke Sucht
            DrugType preferredType = DrugType.values()[random.nextInt(DrugType.values().length)];
            profile.setPreference(preferredType, 80 + random.nextInt(21)); // 80-100
        }

        return profile;
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    private static final String NBT_PREFIX = "addiction_";

    public CompoundTag save(CompoundTag tag) {
        for (Map.Entry<DrugType, Integer> entry : preferences.entrySet()) {
            tag.putInt(NBT_PREFIX + entry.getKey().name(), entry.getValue());
        }
        return tag;
    }

    public static NPCAddictionProfile load(CompoundTag tag) {
        NPCAddictionProfile profile = new NPCAddictionProfile();
        for (DrugType type : DrugType.values()) {
            String key = NBT_PREFIX + type.name();
            if (tag.contains(key)) {
                profile.setPreference(type, tag.getInt(key));
            }
        }
        return profile;
    }

    // ═══════════════════════════════════════════════════════════
    // INTEREST LEVEL ENUM
    // ═══════════════════════════════════════════════════════════

    public enum InterestLevel {
        NONE("gui.addiction.interest.none", "§7", 0),
        LOW("gui.addiction.interest.low", "§e", 1),
        MEDIUM("gui.addiction.interest.medium", "§6", 2),
        HIGH("gui.addiction.interest.high", "§c", 3),
        ADDICTED("gui.addiction.interest.addicted", "§4", 4);

        private final String translationKey;
        private final String colorCode;
        private final int level;

        InterestLevel(String translationKey, String colorCode, int level) {
            this.translationKey = translationKey;
            this.colorCode = colorCode;
            this.level = level;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public String getColorCode() {
            return colorCode;
        }

        public int getLevel() {
            return level;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NPCAddictionProfile{");
        for (Map.Entry<DrugType, Integer> entry : preferences.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(", ");
            }
        }
        sb.append("primary=").append(getPrimaryDrugType().name()).append("}");
        return sb.toString();
    }
}
