package de.rolandsw.schedulemc.npc.life.core;

import net.minecraft.nbt.CompoundTag;

import java.util.Random;

/**
 * NPCTraits - Persönlichkeits-Eigenschaften eines NPCs (3 Achsen)
 *
 * Jede Achse geht von -100 bis +100:
 * - MUT: +mutig ↔ -ängstlich
 * - EHRLICHKEIT: +ehrlich ↔ -betrügerisch
 * - GIER: +gierig ↔ -großzügig
 */
public class NPCTraits {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final int MIN_VALUE = -100;
    public static final int MAX_VALUE = 100;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /**
     * MUT (-100 bis +100)
     * +100: Konfrontiert Gefahren, verteidigt andere, meldet sofort
     * -100: Flieht sofort, sehr ängstlich, versteckt sich
     */
    private int courage;

    /**
     * EHRLICHKEIT (-100 bis +100)
     * +100: Meldet alles, nicht bestechlich, hält Versprechen
     * -100: Lügt, bestechlich, behält Geheimnisse
     */
    private int honesty;

    /**
     * GIER (-100 bis +100)
     * +100: Will maximalen Profit, hohe Preise, schwer zu verhandeln
     * -100: Großzügig, faire Preise, gibt Rabatte
     */
    private int greed;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════

    public NPCTraits() {
        this.courage = 0;
        this.honesty = 0;
        this.greed = 0;
    }

    public NPCTraits(int courage, int honesty, int greed) {
        this.courage = clamp(courage);
        this.honesty = clamp(honesty);
        this.greed = clamp(greed);
    }

    /**
     * Erstellt zufällige Traits
     */
    public static NPCTraits randomize(Random random) {
        // Normalverteilung um 0 mit Standardabweichung von ~40
        int courage = (int) (random.nextGaussian() * 40);
        int honesty = (int) (random.nextGaussian() * 40);
        int greed = (int) (random.nextGaussian() * 40);

        return new NPCTraits(courage, honesty, greed);
    }

    /**
     * Erstellt Traits basierend auf der alten NPCPersonality
     */
    public static NPCTraits fromPersonality(de.rolandsw.schedulemc.npc.data.NPCPersonality personality) {
        return switch (personality) {
            case SPARSAM -> new NPCTraits(0, 30, 60);      // Ehrlich, gierig
            case AUSGEWOGEN -> new NPCTraits(0, 0, 0);     // Neutral
            case IMPULSIV -> new NPCTraits(40, -20, -30);  // Mutig, etwas unehrlich, großzügig
        };
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════

    public int getCourage() {
        return courage;
    }

    public void setCourage(int courage) {
        this.courage = clamp(courage);
    }

    public int getHonesty() {
        return honesty;
    }

    public void setHonesty(int honesty) {
        this.honesty = clamp(honesty);
    }

    public int getGreed() {
        return greed;
    }

    public void setGreed(int greed) {
        this.greed = clamp(greed);
    }

    /**
     * Berechnet eine Sozialitäts-Metrik basierend auf den Traits
     * Ehrliche, nicht gierige und mutige NPCs sind geselliger
     */
    public int getSociability() {
        // Kombination: Ehrlichkeit fördert, Gier senkt, Mut hat leichten Einfluss
        int base = (honesty / 2) - (greed / 3) + (courage / 4);
        return clamp(base);
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULATED VALUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den Basis-Preis-Modifikator basierend auf Gier
     *
     * Gier +100 = 1.3 (30% teurer)
     * Gier 0 = 1.0 (normal)
     * Gier -100 = 0.8 (20% günstiger)
     *
     * @return Modifikator (0.8 - 1.3)
     */
    public float getTradeModifier() {
        // Linear: -100 → 0.8, 0 → 1.0, +100 → 1.3
        if (greed >= 0) {
            return 1.0f + (greed / 100.0f) * 0.3f;
        } else {
            return 1.0f + (greed / 100.0f) * 0.2f;
        }
    }

    /**
     * Berechnet die Angst-Schwelle (ab welcher Bedrohungsstufe NPC flieht)
     *
     * Mutig (+100): Hohe Schwelle (80) - flieht fast nie
     * Normal (0): Mittlere Schwelle (50)
     * Ängstlich (-100): Niedrige Schwelle (20) - flieht schnell
     *
     * @return Schwelle (20 - 80)
     */
    public float getFearThreshold() {
        // Linear: -100 → 20, 0 → 50, +100 → 80
        return 50.0f + (courage / 100.0f) * 30.0f;
    }

    /**
     * Berechnet die Wahrscheinlichkeit, dass NPC ein Verbrechen meldet
     *
     * Ehrlich (+100): 95%
     * Normal (0): 50%
     * Unehrlich (-100): 5%
     *
     * @return Wahrscheinlichkeit (0.05 - 0.95)
     */
    public float getReportChance() {
        // Linear: -100 → 0.05, 0 → 0.5, +100 → 0.95
        return 0.5f + (honesty / 100.0f) * 0.45f;
    }

    /**
     * Berechnet die Bestechungs-Basis-Chance
     * (Höhere Werte = leichter zu bestechen)
     *
     * Ehrlich + nicht gierig = schwer zu bestechen (10%)
     * Unehrlich + gierig = leicht zu bestechen (70%)
     *
     * @return Basis-Chance (0.1 - 0.7)
     */
    public float getBriberyBaseChance() {
        // Kombiniert Unehrlichkeit und Gier
        float dishonesty = (-honesty + 100) / 200.0f; // 0 bis 1
        float greedFactor = (greed + 100) / 200.0f;   // 0 bis 1

        return 0.1f + (dishonesty * 0.3f) + (greedFactor * 0.3f);
    }

    /**
     * Prüft ob NPC ein bestimmtes Verbrechen melden würde
     *
     * @param crimeSeverity Schwere des Verbrechens (1-10)
     * @return true wenn NPC melden würde
     */
    public boolean wouldReport(int crimeSeverity) {
        float chance = getReportChance();

        // Schwere Verbrechen werden eher gemeldet
        chance += crimeSeverity * 0.03f;

        // Mutige NPCs melden eher
        if (courage > 50) {
            chance += 0.1f;
        }

        return Math.random() < Math.min(0.95f, chance);
    }

    /**
     * Berechnet wie viel Geduld der NPC bei Verhandlungen hat
     *
     * @return Geduld-Wert (1-5 Runden)
     */
    public int getNegotiationPatience() {
        // Gierige NPCs haben weniger Geduld
        if (greed > 50) return 2;
        if (greed > 0) return 3;
        if (greed > -50) return 4;
        return 5;
    }

    /**
     * Prüft ob NPC verdächtiges Verhalten untersuchen würde
     */
    public boolean wouldInvestigate() {
        // Mutige und ehrliche NPCs untersuchen eher
        return courage > 20 && honesty > -30;
    }

    /**
     * Prüft ob NPC ein Geheimnis für sich behalten würde
     */
    public boolean wouldKeepSecret() {
        return honesty < 0;
    }

    // ═══════════════════════════════════════════════════════════
    // TRAIT DESCRIPTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt eine Beschreibung des Muts zurück
     */
    public String getCourageDescription() {
        if (courage >= 70) return "Sehr mutig";
        if (courage >= 30) return "Mutig";
        if (courage >= -30) return "Normal";
        if (courage >= -70) return "Ängstlich";
        return "Sehr ängstlich";
    }

    /**
     * Gibt eine Beschreibung der Ehrlichkeit zurück
     */
    public String getHonestyDescription() {
        if (honesty >= 70) return "Sehr ehrlich";
        if (honesty >= 30) return "Ehrlich";
        if (honesty >= -30) return "Normal";
        if (honesty >= -70) return "Unehrlich";
        return "Betrügerisch";
    }

    /**
     * Gibt eine Beschreibung der Gier zurück
     */
    public String getGreedDescription() {
        if (greed >= 70) return "Sehr gierig";
        if (greed >= 30) return "Gierig";
        if (greed >= -30) return "Normal";
        if (greed >= -70) return "Großzügig";
        return "Sehr großzügig";
    }

    /**
     * Gibt einen Persönlichkeits-Archetyp basierend auf den Traits zurück
     */
    public String getArchetype() {
        // Kombinationen
        if (courage > 50 && honesty > 50 && greed < 0) return "Held";
        if (courage > 50 && honesty < -50 && greed > 50) return "Gangster";
        if (courage < -50 && honesty > 50 && greed < 0) return "Bürger";
        if (courage < -50 && honesty < -50 && greed > 50) return "Feigling";
        if (courage > 30 && honesty > 30) return "Aufrechter";
        if (greed > 50 && honesty < 0) return "Händler";
        if (courage < -30 && honesty < -30) return "Schattenhaft";
        return "Durchschnittlich";
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    private static int clamp(int value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }

    public void modify(int courageDelta, int honestyDelta, int greedDelta) {
        this.courage = clamp(this.courage + courageDelta);
        this.honesty = clamp(this.honesty + honestyDelta);
        this.greed = clamp(this.greed + greedDelta);
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Courage", courage);
        tag.putInt("Honesty", honesty);
        tag.putInt("Greed", greed);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.courage = clamp(tag.getInt("Courage"));
        this.honesty = clamp(tag.getInt("Honesty"));
        this.greed = clamp(tag.getInt("Greed"));
    }

    public static NPCTraits fromTag(CompoundTag tag) {
        NPCTraits traits = new NPCTraits();
        traits.load(tag);
        return traits;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("NPCTraits{%s: courage=%d, honesty=%d, greed=%d}",
            getArchetype(), courage, honesty, greed);
    }

    public String getDetailedInfo() {
        return String.format(
            "Persönlichkeit: %s\n" +
            "Mut: %d (%s)\n" +
            "Ehrlichkeit: %d (%s)\n" +
            "Gier: %d (%s)\n" +
            "Preis-Mod: ×%.2f | Angst-Schwelle: %.0f | Melde-Chance: %.0f%%",
            getArchetype(),
            courage, getCourageDescription(),
            honesty, getHonestyDescription(),
            greed, getGreedDescription(),
            getTradeModifier(), getFearThreshold(), getReportChance() * 100
        );
    }
}
