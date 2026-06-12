package de.rolandsw.schedulemc.npc.life.core;

import java.util.Locale;

/**
 * Anzeigbarer Persönlichkeits-Archetyp eines NPCs, abgeleitet aus den
 * kontinuierlichen {@link NPCTraits}-Achsen (Mut, Ehrlichkeit, Gier).
 *
 * Wird über dem NPC-Namen gerendert, sobald ein Spieler den NPC kennt
 * (erstes Gespräch), und prägt Dialogton sowie Preisverhalten.
 */
public enum PersonalityArchetype {
    GENEROUS,
    GREEDY,
    HONEST,
    DECEITFUL,
    BRAVE,
    TIMID,
    BALANCED;

    /** Schwelle, ab der eine Trait-Achse als dominant gilt. */
    public static final int DOMINANCE_THRESHOLD = 30;

    public String getTranslationKey() {
        return "personality.schedulemc." + name().toLowerCase(Locale.ROOT);
    }

    /**
     * Leitet den Archetyp aus der betragsmäßig stärksten Trait-Achse ab.
     * Tie-Break-Reihenfolge: Gier > Ehrlichkeit > Mut. Liegt keine Achse
     * über {@link #DOMINANCE_THRESHOLD}, ist der NPC BALANCED.
     */
    public static PersonalityArchetype fromTraits(NPCTraits traits) {
        int greed = traits.getGreed();
        int honesty = traits.getHonesty();
        int courage = traits.getCourage();

        int aGreed = Math.abs(greed);
        int aHonesty = Math.abs(honesty);
        int aCourage = Math.abs(courage);

        int max = Math.max(aGreed, Math.max(aHonesty, aCourage));
        if (max < DOMINANCE_THRESHOLD) {
            return BALANCED;
        }
        if (aGreed == max) {
            return greed > 0 ? GREEDY : GENEROUS;
        }
        if (aHonesty == max) {
            return honesty > 0 ? HONEST : DECEITFUL;
        }
        return courage > 0 ? BRAVE : TIMID;
    }
}
