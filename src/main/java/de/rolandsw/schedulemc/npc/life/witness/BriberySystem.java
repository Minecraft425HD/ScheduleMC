package de.rolandsw.schedulemc.npc.life.witness;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.core.NPCTraits;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * BriberySystem - Verwaltet Bestechungs-Mechaniken
 *
 * Ermöglicht Spielern, Zeugen zu bestechen um Meldungen zu verhindern.
 * Erfolg basiert auf NPC-Traits, Schwere des Verbrechens und Betrag.
 */
public class BriberySystem {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Minimum Bestechungsbetrag */
    public static final int MIN_BRIBE = 50;

    /** Maximum Multiplikator für Bestechung */
    public static final float MAX_MULTIPLIER = 5.0f;

    /** Basis-Erfolgschance bei fairem Angebot */
    public static final float BASE_SUCCESS_CHANCE = 0.5f;

    // ═══════════════════════════════════════════════════════════
    // BRIBERY ATTEMPT
    // ═══════════════════════════════════════════════════════════

    /**
     * Versucht einen NPC zu bestechen
     *
     * @param player Der Spieler der besticht
     * @param npc Der NPC der bestochen werden soll
     * @param report Der Zeugenbericht der unterdrückt werden soll
     * @param offeredAmount Der angebotene Betrag
     * @return Ergebnis des Bestechungsversuchs
     */
    public static BriberyResult attemptBribe(ServerPlayer player, CustomNPCEntity npc,
                                             WitnessReport report, int offeredAmount) {

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) {
            return BriberyResult.REFUSED;
        }

        NPCTraits traits = lifeData.getTraits();
        CrimeType crimeType = report.getCrimeType();

        // 1. Prüfen ob NPC überhaupt bestechlich ist
        if (!isNPCBribeable(npc, crimeType)) {
            return BriberyResult.UNBRIBEABLE;
        }

        // 2. Minimum-Betrag berechnen
        int minimumAmount = calculateMinimumBribe(npc, report);

        // 3. Erfolgswahrscheinlichkeit berechnen
        float successChance = calculateSuccessChance(traits, crimeType, offeredAmount, minimumAmount);

        // 4. Würfeln
        boolean success = Math.random() < successChance;

        // 5. Ergebnis verarbeiten
        if (success) {
            // Bestechung erfolgreich
            report.markAsBribed(offeredAmount);

            // NPC-Erinnerung
            lifeData.getMemory().addMemory(
                player.getUUID(),
                MemoryType.TRANSACTION,
                "Bestechung angenommen: " + offeredAmount,
                6 // Wichtig
            );
            lifeData.getMemory().addPlayerTag(player.getUUID(), "Bestechend");

            // NPC-Emotion: Zufrieden aber schuldig
            if (traits.getHonesty() > 0) {
                lifeData.getEmotions().trigger(EmotionState.SAD, 30.0f, 1200);
            } else {
                lifeData.getEmotions().trigger(EmotionState.HAPPY, 20.0f, 600);
            }

            // NPCLifeSystemIntegration: onBribeOffered aufrufen
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                var integration = de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration.get(serverLevel);
                integration.onBribeOffered(player, npc, true);
            }

            return BriberyResult.ACCEPTED;

        } else {
            // Bestechung abgelehnt
            if (offeredAmount < minimumAmount) {
                // Zu wenig - NPC will mehr
                lifeData.getMemory().addMemory(
                    player.getUUID(),
                    MemoryType.TRANSACTION,
                    "Bestechungsversuch abgelehnt (zu wenig)",
                    4
                );
                return BriberyResult.WANTS_MORE;

            } else {
                // Prinzipielle Ablehnung
                lifeData.getMemory().addMemory(
                    player.getUUID(),
                    MemoryType.THREAT_RECEIVED,
                    "Bestechungsversuch",
                    7
                );
                lifeData.getMemory().addPlayerTag(player.getUUID(), "Korrupt");
                lifeData.getEmotions().trigger(EmotionState.ANGRY, 50.0f);

                // NPCLifeSystemIntegration: onBribeOffered aufrufen (abgelehnt)
                if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    var integration = de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration.get(serverLevel);
                    integration.onBribeOffered(player, npc, false);
                }

                // Bei Polizei: Zusätzliches Verbrechen
                if (npc.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI) {
                    return BriberyResult.REFUSED_AND_REPORTED;
                }

                return BriberyResult.REFUSED;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob ein NPC prinzipiell bestechlich ist
     */
    public static boolean isNPCBribeable(CustomNPCEntity npc, CrimeType crimeType) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return false;

        NPCTraits traits = lifeData.getTraits();

        // Sehr ehrliche NPCs sind nicht bestechlich
        if (traits.getHonesty() > 80) {
            return false;
        }

        // Polizei ist schwerer zu bestechen
        if (npc.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI) {
            if (traits.getHonesty() > 30) {
                return false;
            }
        }

        // Schwere Verbrechen: Nur sehr unehrliche NPCs
        if (crimeType.getSeverity() >= 8) {
            return traits.getHonesty() < 0;
        }

        return true;
    }

    /**
     * Berechnet den Minimum-Bestechungsbetrag
     */
    public static int calculateMinimumBribe(CustomNPCEntity npc, WitnessReport report) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return Integer.MAX_VALUE;

        NPCTraits traits = lifeData.getTraits();
        CrimeType crimeType = report.getCrimeType();

        // Basis: Kopfgeld des Verbrechens
        int base = crimeType.getBaseBounty();

        // Gier-Faktor: Gierige NPCs wollen mehr
        float greedFactor = 1.0f + (traits.getGreed() / 100.0f) * 0.5f; // 0.5 - 1.5

        // Ehrlichkeits-Faktor: Ehrliche NPCs wollen viel mehr
        float honestyFactor = 1.0f;
        if (traits.getHonesty() > 0) {
            honestyFactor = 1.0f + (traits.getHonesty() / 100.0f) * 2.0f; // 1.0 - 3.0
        } else {
            honestyFactor = 1.0f + (traits.getHonesty() / 100.0f) * 0.3f; // 0.7 - 1.0
        }

        // Schwere-Faktor
        float severityFactor = 1.0f + (crimeType.getSeverity() - 5) * 0.2f;

        // Polizei-Faktor
        float policeFactor = 1.0f;
        if (npc.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI) {
            policeFactor = 3.0f; // Polizei will 3x so viel
        }

        int minimum = (int) (base * greedFactor * honestyFactor * severityFactor * policeFactor);

        return Math.max(MIN_BRIBE, minimum);
    }

    /**
     * Berechnet die Erfolgswahrscheinlichkeit
     */
    public static float calculateSuccessChance(NPCTraits traits, CrimeType crimeType,
                                               int offered, int minimum) {

        // Basis-Chance vom Trait
        float baseChance = traits.getBriberyBaseChance();

        // Betrags-Faktor
        float amountRatio = (float) offered / minimum;
        float amountFactor;
        if (amountRatio < 1.0f) {
            // Unter Minimum: Stark reduzierte Chance
            amountFactor = amountRatio * 0.3f;
        } else if (amountRatio < 2.0f) {
            // 1-2x Minimum: Linear steigend
            amountFactor = 0.3f + (amountRatio - 1.0f) * 0.4f;
        } else {
            // Über 2x: Diminishing returns
            amountFactor = 0.7f + Math.min(0.25f, (amountRatio - 2.0f) * 0.05f);
        }

        // Schwere-Malus
        float severityPenalty = (crimeType.getSeverity() - 5) * 0.05f;

        // Gesamtchance
        float totalChance = baseChance * amountFactor - severityPenalty;

        // Clamp
        return Math.max(0.05f, Math.min(0.95f, totalChance));
    }

    /**
     * Gibt einen Hinweis zurück wie viel der NPC ungefähr möchte
     */
    public static String getBribeHint(CustomNPCEntity npc, WitnessReport report) {
        int minimum = calculateMinimumBribe(npc, report);

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return "Der NPC scheint nicht interessiert.";

        NPCTraits traits = lifeData.getTraits();

        if (traits.getHonesty() > 60) {
            return "Der NPC wirkt sehr rechtschaffen...";
        }

        if (minimum < 200) {
            return "Ein kleiner Betrag könnte helfen.";
        } else if (minimum < 500) {
            return "Das wird nicht billig...";
        } else if (minimum < 1000) {
            return "Das wird sehr teuer.";
        } else {
            return "Das wird ein Vermögen kosten!";
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RESULT ENUM
    // ═══════════════════════════════════════════════════════════

    /**
     * Mögliche Ergebnisse eines Bestechungsversuchs
     */
    public enum BriberyResult {
        /** Bestechung akzeptiert */
        ACCEPTED("Bestechung angenommen"),

        /** NPC will mehr Geld */
        WANTS_MORE("Zu wenig Geld"),

        /** Bestechung abgelehnt */
        REFUSED("Bestechung abgelehnt"),

        /** Bestechung abgelehnt und gemeldet (Polizei) */
        REFUSED_AND_REPORTED("Bestechung abgelehnt und gemeldet"),

        /** NPC ist prinzipiell nicht bestechlich */
        UNBRIBEABLE("NPC ist nicht bestechlich");

        private final String displayName;

        BriberyResult(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isSuccess() {
            return this == ACCEPTED;
        }

        public boolean shouldRetry() {
            return this == WANTS_MORE;
        }
    }
}
