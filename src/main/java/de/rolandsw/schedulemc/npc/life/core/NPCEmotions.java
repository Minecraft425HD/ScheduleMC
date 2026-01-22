package de.rolandsw.schedulemc.npc.life.core;

import net.minecraft.nbt.CompoundTag;

/**
 * NPCEmotions - Verwaltet den emotionalen Zustand eines NPCs
 *
 * Features:
 * - 6 Emotionszustände mit Intensität und Dauer
 * - Automatischer Decay zurück zur Baseline-Emotion
 * - Modifikatoren für Preise und Gesprächsbereitschaft
 */
public class NPCEmotions {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final float MIN_INTENSITY = 0.0f;
    public static final float MAX_INTENSITY = 100.0f;

    /** Decay-Rate pro Tick (Intensität sinkt über Zeit) */
    private static final float DECAY_PER_TICK = 0.02f; // ~1 pro Sekunde

    /** Schwelle ab der Emotion "stark" ist */
    public static final float STRONG_EMOTION_THRESHOLD = 70.0f;

    /** Schwelle ab der Emotion "aktiv" ist */
    public static final float ACTIVE_EMOTION_THRESHOLD = 20.0f;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private EmotionState currentEmotion = EmotionState.NEUTRAL;
    private float intensity = 0.0f;
    private int remainingTicks = 0;
    private EmotionState baselineEmotion = EmotionState.NEUTRAL;

    // Für sanfte Übergänge
    private EmotionState previousEmotion = EmotionState.NEUTRAL;
    private float transitionProgress = 1.0f; // 1.0 = vollständig gewechselt

    // ═══════════════════════════════════════════════════════════
    // TRIGGER / SET
    // ═══════════════════════════════════════════════════════════

    /**
     * Löst eine Emotion aus
     *
     * @param emotion Die Emotion
     * @param intensity Intensität (0-100)
     * @param durationTicks Dauer in Ticks (20 Ticks = 1 Sekunde)
     */
    public void trigger(EmotionState emotion, float intensity, int durationTicks) {
        // Stärkere Emotion überschreibt schwächere
        if (emotion != this.currentEmotion || intensity > this.intensity) {
            this.previousEmotion = this.currentEmotion;
            this.currentEmotion = emotion;
            this.intensity = Math.min(MAX_INTENSITY, Math.max(MIN_INTENSITY, intensity));
            this.remainingTicks = durationTicks;
            this.transitionProgress = 0.0f;
        } else if (emotion == this.currentEmotion) {
            // Gleiche Emotion - verlängern und Intensität erhöhen
            this.remainingTicks = Math.max(this.remainingTicks, durationTicks);
            this.intensity = Math.min(MAX_INTENSITY, this.intensity + intensity * 0.3f);
        }
    }

    /**
     * Löst eine Emotion mit Standard-Dauer aus
     */
    public void trigger(EmotionState emotion, float intensity) {
        // Standard-Dauer basierend auf Emotion
        int duration = switch (emotion) {
            case HAPPY -> 6000;      // 5 Minuten
            case SAD -> 12000;       // 10 Minuten
            case ANGRY -> 9000;      // 7.5 Minuten
            case FEARFUL -> 4800;    // 4 Minuten
            case SUSPICIOUS -> 12000; // 10 Minuten
            case NEUTRAL -> 0;
        };
        trigger(emotion, intensity, duration);
    }

    /**
     * Setzt die Baseline-Emotion (Standard-Emotion wenn keine andere aktiv)
     */
    public void setBaseline(EmotionState baseline) {
        this.baselineEmotion = baseline;
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Tick aufgerufen - verwaltet Decay und Übergänge
     */
    public void tick() {
        // Übergangs-Fortschritt
        if (transitionProgress < 1.0f) {
            transitionProgress = Math.min(1.0f, transitionProgress + 0.05f);
        }

        // Wenn aktive Emotion vorhanden
        if (currentEmotion != EmotionState.NEUTRAL && remainingTicks > 0) {
            remainingTicks--;

            // Intensität natürlich abbauen
            if (remainingTicks < 1200) { // Letzte Minute: Intensität sinkt
                intensity = Math.max(MIN_INTENSITY, intensity - DECAY_PER_TICK);
            }

            // Wenn Zeit abgelaufen oder Intensität zu niedrig
            if (remainingTicks <= 0 || intensity < ACTIVE_EMOTION_THRESHOLD) {
                decayToBaseline();
            }
        }
    }

    /**
     * Wechselt zurück zur Baseline-Emotion
     */
    private void decayToBaseline() {
        this.previousEmotion = this.currentEmotion;
        this.currentEmotion = this.baselineEmotion;
        this.intensity = 0.0f;
        this.remainingTicks = 0;
        this.transitionProgress = 0.0f;
    }

    /**
     * Sofortiger Reset zur Baseline
     */
    public void reset() {
        decayToBaseline();
        this.transitionProgress = 1.0f;
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt die aktuelle Emotion zurück
     */
    public EmotionState getCurrentEmotion() {
        return currentEmotion;
    }

    /**
     * Gibt die Intensität zurück (0-100)
     */
    public float getIntensity() {
        return intensity;
    }

    /**
     * Gibt die verbleibende Zeit in Ticks zurück
     */
    public int getRemainingTicks() {
        return remainingTicks;
    }

    /**
     * Gibt die verbleibende Zeit in Sekunden zurück
     */
    public int getRemainingSeconds() {
        return remainingTicks / 20;
    }

    /**
     * Gibt die Baseline-Emotion zurück
     */
    public EmotionState getBaselineEmotion() {
        return baselineEmotion;
    }

    /**
     * Prüft ob eine starke Emotion aktiv ist (Intensität > 70)
     */
    public boolean hasStrongEmotion() {
        return intensity >= STRONG_EMOTION_THRESHOLD;
    }

    /**
     * Prüft ob überhaupt eine Emotion aktiv ist
     */
    public boolean hasActiveEmotion() {
        return currentEmotion != EmotionState.NEUTRAL && intensity >= ACTIVE_EMOTION_THRESHOLD;
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFIERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den Preis-Modifikator basierend auf Emotion und Intensität
     *
     * @return Modifikator (z.B. 0.9 = 10% günstiger, 1.3 = 30% teurer)
     */
    public float getPriceModifier() {
        if (!hasActiveEmotion()) {
            return 1.0f;
        }

        float baseModifier = currentEmotion.getPriceModifier();
        // Intensität beeinflusst den Modifikator
        // Bei 50% Intensität = halber Effekt, bei 100% = voller Effekt
        float intensityFactor = intensity / MAX_INTENSITY;

        // Interpoliere zwischen 1.0 und dem Emotions-Modifikator
        return 1.0f + (baseModifier - 1.0f) * intensityFactor;
    }

    /**
     * Berechnet den Gesprächsbereitschafts-Modifikator
     *
     * @return Modifikator (z.B. 0.1 = kaum gesprächig, 1.3 = sehr gesprächig)
     */
    public float getSocialModifier() {
        if (!hasActiveEmotion()) {
            return 1.0f;
        }

        float baseModifier = currentEmotion.getSocialModifier();
        float intensityFactor = intensity / MAX_INTENSITY;

        return 1.0f + (baseModifier - 1.0f) * intensityFactor;
    }

    // ═══════════════════════════════════════════════════════════
    // BEHAVIOR HELPERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob der NPC bei aktueller Emotion fliehen würde
     */
    public boolean wouldFlee() {
        return currentEmotion.wouldFlee() && intensity >= 50.0f;
    }

    /**
     * Prüft ob der NPC bei aktueller Emotion kämpfen würde
     */
    public boolean wouldFight() {
        return currentEmotion.wouldFight() && intensity >= 70.0f;
    }

    /**
     * Prüft ob der NPC bei aktueller Emotion handeln würde
     */
    public boolean wouldTrade() {
        return currentEmotion.wouldTrade() || intensity < 50.0f;
    }

    /**
     * Prüft ob der NPC die Polizei rufen würde
     */
    public boolean wouldCallPolice() {
        return (currentEmotion == EmotionState.FEARFUL || currentEmotion == EmotionState.ANGRY)
               && intensity >= 60.0f;
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CurrentEmotion", currentEmotion.ordinal());
        tag.putFloat("Intensity", intensity);
        tag.putInt("RemainingTicks", remainingTicks);
        tag.putInt("BaselineEmotion", baselineEmotion.ordinal());
        return tag;
    }

    public void load(CompoundTag tag) {
        this.currentEmotion = EmotionState.fromOrdinal(tag.getInt("CurrentEmotion"));
        this.intensity = tag.getFloat("Intensity");
        this.remainingTicks = tag.getInt("RemainingTicks");
        this.baselineEmotion = EmotionState.fromOrdinal(tag.getInt("BaselineEmotion"));
        this.transitionProgress = 1.0f;
    }

    public static NPCEmotions fromTag(CompoundTag tag) {
        NPCEmotions emotions = new NPCEmotions();
        emotions.load(tag);
        return emotions;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        if (hasActiveEmotion()) {
            return String.format("NPCEmotions{%s (%.0f%%, %ds remaining)}",
                currentEmotion.getDisplayName(), intensity, getRemainingSeconds());
        }
        return String.format("NPCEmotions{%s (baseline)}", baselineEmotion.getDisplayName());
    }
}
