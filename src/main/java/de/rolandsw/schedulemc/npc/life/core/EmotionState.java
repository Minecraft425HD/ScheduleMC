package de.rolandsw.schedulemc.npc.life.core;

/**
 * Enum für die 6 Emotionszustände eines NPCs.
 * Jede Emotion hat Modifikatoren für Preise und Gesprächsbereitschaft.
 */
public enum EmotionState {

    /**
     * Neutral - Standardzustand
     */
    NEUTRAL("Neutral", 1.0f, 1.0f, "§7", 0),

    /**
     * Glücklich - Gute Interaktion, Geschenk, Quest abgeschlossen
     */
    HAPPY("Glücklich", 0.9f, 1.3f, "§a", 1),

    /**
     * Traurig - Ignoriert, Verlust, schlechte Nachricht
     */
    SAD("Traurig", 1.0f, 0.5f, "§9", 2),

    /**
     * Wütend - Beleidigt, bestohlen, betrogen
     */
    ANGRY("Wütend", 1.3f, 0.3f, "§c", 3),

    /**
     * Ängstlich - Bedrohung, Verbrechen, Waffe gesehen
     */
    FEARFUL("Ängstlich", 1.5f, 0.1f, "§5", 4),

    /**
     * Misstrauisch - Verdächtiges Verhalten, Gerücht gehört
     */
    SUSPICIOUS("Misstrauisch", 1.2f, 0.6f, "§6", 5);

    private final String displayName;
    private final float priceModifier;
    private final float socialModifier;
    private final String colorCode;
    private final int iconIndex;

    EmotionState(String displayName, float priceModifier, float socialModifier,
                 String colorCode, int iconIndex) {
        this.displayName = displayName;
        this.priceModifier = priceModifier;
        this.socialModifier = socialModifier;
        this.colorCode = colorCode;
        this.iconIndex = iconIndex;
    }

    /**
     * Anzeigename der Emotion
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Preis-Modifikator (0.9 = 10% günstiger, 1.5 = 50% teurer)
     */
    public float getPriceModifier() {
        return priceModifier;
    }

    /**
     * Gesprächsbereitschafts-Modifikator (0.1 = kaum gesprächig, 1.3 = sehr gesprächig)
     */
    public float getSocialModifier() {
        return socialModifier;
    }

    /**
     * Minecraft Farbcode für Chat-Anzeige
     */
    public String getColorCode() {
        return colorCode;
    }

    /**
     * Index für Icon-Textur (in GUI)
     */
    public int getIconIndex() {
        return iconIndex;
    }

    /**
     * Gibt formatierten Namen mit Farbe zurück
     */
    public String getFormattedName() {
        return colorCode + displayName + "§r";
    }

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "npc.emotion." + name().toLowerCase();
    }

    /**
     * Prüft ob NPC bei dieser Emotion fliehen würde
     */
    public boolean wouldFlee() {
        return this == FEARFUL;
    }

    /**
     * Prüft ob NPC bei dieser Emotion kämpfen würde
     */
    public boolean wouldFight() {
        return this == ANGRY;
    }

    /**
     * Prüft ob NPC bei dieser Emotion handeln würde
     */
    public boolean wouldTrade() {
        return this != FEARFUL && this != ANGRY;
    }

    /**
     * Prüft ob dies eine negative Emotion ist
     */
    public boolean isNegative() {
        return this == SAD || this == ANGRY || this == FEARFUL || this == SUSPICIOUS;
    }

    /**
     * Prüft ob dies eine positive Emotion ist
     */
    public boolean isPositive() {
        return this == HAPPY;
    }

    /**
     * Gibt EmotionState aus Ordinal zurück (mit Fallback auf NEUTRAL)
     */
    public static EmotionState fromOrdinal(int ordinal) {
        EmotionState[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return NEUTRAL;
    }
}
