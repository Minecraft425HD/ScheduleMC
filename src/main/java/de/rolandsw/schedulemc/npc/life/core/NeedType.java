package de.rolandsw.schedulemc.npc.life.core;

/**
 * Enum für die verschiedenen Bedürfnistypen eines NPCs.
 * Das NPC Life System verwendet 2 Kernbedürfnisse.
 */
public enum NeedType {
    /**
     * Energie - Sinkt über Zeit, regeneriert beim Schlafen
     * Niedrig = NPC wird langsamer, schlechte Laune
     * Kritisch (<20) = NPC geht nach Hause
     */
    ENERGY("Energie", 2.0f, true),

    /**
     * Sicherheit - Wird aus Umgebung berechnet (kein natürlicher Decay)
     * Niedrig = NPC wird ängstlich, will nach Hause
     * Kritisch (<20) = NPC flieht oder versteckt sich
     */
    SAFETY("Sicherheit", 0.0f, false);

    private final String displayName;
    private final float decayPerHour;
    private final boolean hasNaturalDecay;

    NeedType(String displayName, float decayPerHour, boolean hasNaturalDecay) {
        this.displayName = displayName;
        this.decayPerHour = decayPerHour;
        this.hasNaturalDecay = hasNaturalDecay;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Decay-Rate pro Stunde (Minecraft: 1000 Ticks = 50 Sekunden Echtzeit)
     * 1 MC-Tag = 24000 Ticks = 20 Minuten Echtzeit
     * 1 MC-Stunde = 1000 Ticks
     */
    public float getDecayPerHour() {
        return decayPerHour;
    }

    /**
     * Gibt die Decay-Rate pro Tick zurück
     * @return Decay pro Tick (decayPerHour / 1000)
     */
    public float getDecayPerTick() {
        return decayPerHour / 1000.0f;
    }

    public boolean hasNaturalDecay() {
        return hasNaturalDecay;
    }

    public String getTranslationKey() {
        return "npc.need." + name().toLowerCase();
    }
}
