package de.rolandsw.schedulemc.npc.life.economy;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.core.NPCTraits;
import net.minecraft.server.level.ServerPlayer;

/**
 * NegotiationSystem - Verhandlungssystem für Preise
 *
 * Ermöglicht Spielern, mit NPCs über Preise zu verhandeln.
 * Erfolg basiert auf NPC-Traits, Beziehung und Verhandlungsgeschick.
 */
public class NegotiationSystem {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximale Anzahl an Verhandlungsrunden */
    public static final int MAX_ROUNDS = 5;

    /** Maximaler Rabatt der erreicht werden kann */
    public static final float MAX_DISCOUNT = 0.30f; // 30%

    /** Minimaler Rabatt pro erfolgreicher Runde */
    public static final float MIN_DISCOUNT_PER_ROUND = 0.02f; // 2%

    /** Maximaler Rabatt pro erfolgreicher Runde */
    public static final float MAX_DISCOUNT_PER_ROUND = 0.08f; // 8%

    // ═══════════════════════════════════════════════════════════
    // NEGOTIATION SESSION
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet eine neue Verhandlungssitzung
     */
    public static NegotiationSession startNegotiation(CustomNPCEntity npc, ServerPlayer player,
                                                      int originalPrice, boolean isBuying) {

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null || !lifeData.isWillingToTrade()) {
            return null; // NPC verhandelt nicht
        }

        NPCTraits traits = lifeData.getTraits();
        int maxRounds = traits.getNegotiationPatience();

        return new NegotiationSession(npc, player, originalPrice, maxRounds, isBuying);
    }

    /**
     * Führt eine Verhandlungsrunde durch
     */
    public static NegotiationResult negotiate(NegotiationSession session, NegotiationTactic tactic) {
        if (session.isFinished()) {
            return new NegotiationResult(ResultType.SESSION_ENDED, session.getCurrentPrice(), 0);
        }

        CustomNPCEntity npc = session.getNpc();
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) {
            return new NegotiationResult(ResultType.FAILED, session.getCurrentPrice(), 0);
        }

        NPCTraits traits = lifeData.getTraits();
        session.incrementRound();

        // Erfolgswahrscheinlichkeit berechnen
        float successChance = calculateSuccessChance(traits, tactic, session);

        // Würfeln
        boolean success = Math.random() < successChance;

        if (success) {
            // Rabatt berechnen
            float discount = calculateDiscount(traits, tactic, session);
            int newPrice = session.applyDiscount(discount);

            // NPC-Reaktion
            if (tactic == NegotiationTactic.FLATTERY) {
                lifeData.getEmotions().trigger(EmotionState.HAPPY, 20.0f, 600);
            }

            // Prüfen ob NPC aufgibt (zu viel Rabatt gegeben)
            if (session.getTotalDiscount() >= MAX_DISCOUNT * 0.8f) {
                session.finish(FinishReason.NPC_CONCEDED);
            }

            return new NegotiationResult(ResultType.SUCCESS, newPrice, discount);

        } else {
            // Fehlgeschlagen
            if (tactic == NegotiationTactic.AGGRESSIVE) {
                // Aggressive Taktik verärgert NPC
                lifeData.getEmotions().trigger(EmotionState.ANGRY, 30.0f);
                session.decreasePatience(2);
            } else {
                session.decreasePatience(1);
            }

            // Prüfen ob NPC die Verhandlung abbricht
            if (session.getCurrentPatience() <= 0) {
                session.finish(FinishReason.NPC_ANGRY);
                return new NegotiationResult(ResultType.NPC_REFUSED, session.getCurrentPrice(), 0);
            }

            // Prüfen ob maximale Runden erreicht
            if (session.getCurrentRound() >= session.getMaxRounds()) {
                session.finish(FinishReason.MAX_ROUNDS);
            }

            return new NegotiationResult(ResultType.FAILED, session.getCurrentPrice(), 0);
        }
    }

    /**
     * Akzeptiert den aktuellen Preis und beendet die Verhandlung
     */
    public static NegotiationResult acceptPrice(NegotiationSession session) {
        if (session.isFinished()) {
            return new NegotiationResult(ResultType.SESSION_ENDED, session.getCurrentPrice(), 0);
        }

        session.finish(FinishReason.ACCEPTED);

        // Erinnerung speichern wenn guter Deal
        if (session.getTotalDiscount() > 0.1f) {
            NPCLifeData lifeData = session.getNpc().getLifeData();
            if (lifeData != null) {
                lifeData.getMemory().addMemory(
                    session.getPlayer().getUUID(),
                    MemoryType.TRANSACTION,
                    "Gute Verhandlung: " + String.format("%.0f%% Rabatt", session.getTotalDiscount() * 100),
                    3
                );
            }
        }

        return new NegotiationResult(ResultType.DEAL_MADE, session.getCurrentPrice(), session.getTotalDiscount());
    }

    /**
     * Bricht die Verhandlung ab
     */
    public static void cancelNegotiation(NegotiationSession session) {
        if (!session.isFinished()) {
            session.finish(FinishReason.PLAYER_CANCELLED);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die Erfolgswahrscheinlichkeit
     */
    private static float calculateSuccessChance(NPCTraits traits, NegotiationTactic tactic,
                                                NegotiationSession session) {

        // Basis-Chance
        float chance = 0.5f;

        // Taktik-Modifikator
        chance += tactic.getBaseChanceModifier();

        // Gier-Modifikator: Gierige NPCs sind schwerer zu überzeugen
        chance -= (traits.getGreed() / 100.0f) * 0.2f;

        // Ehrlichkeits-Modifikator: Ehrliche NPCs reagieren auf Schmeichelei weniger
        if (tactic == NegotiationTactic.FLATTERY && traits.getHonesty() > 50) {
            chance -= 0.1f;
        }

        // Runden-Malus: Spätere Runden sind schwieriger
        chance -= session.getCurrentRound() * 0.05f;

        // Bereits gewährter Rabatt macht weitere schwieriger
        chance -= session.getTotalDiscount() * 0.3f;

        // Clamp
        return Math.max(0.1f, Math.min(0.9f, chance));
    }

    /**
     * Berechnet den Rabatt bei erfolgreicher Verhandlung
     */
    private static float calculateDiscount(NPCTraits traits, NegotiationTactic tactic,
                                           NegotiationSession session) {

        // Basis-Rabatt
        float discount = MIN_DISCOUNT_PER_ROUND +
                        (float) Math.random() * (MAX_DISCOUNT_PER_ROUND - MIN_DISCOUNT_PER_ROUND);

        // Taktik-Modifikator
        discount *= tactic.getDiscountMultiplier();

        // Gier-Modifikator: Gierige NPCs geben weniger nach
        discount *= 1.0f - (traits.getGreed() / 100.0f) * 0.3f;

        // Maximum nicht überschreiten
        float remainingDiscount = MAX_DISCOUNT - session.getTotalDiscount();
        discount = Math.min(discount, remainingDiscount);

        return Math.max(0, discount);
    }

    // ═══════════════════════════════════════════════════════════
    // ENUMS AND CLASSES
    // ═══════════════════════════════════════════════════════════

    /**
     * Verhandlungstaktiken die der Spieler wählen kann
     */
    public enum NegotiationTactic {
        /** Standard-Verhandlung */
        NORMAL("Normal", 0.0f, 1.0f),

        /** Schmeichelei - Höhere Chance, aber weniger Rabatt */
        FLATTERY("Schmeichelei", 0.1f, 0.8f),

        /** Aggressiv - Niedriger Chance, aber mehr Rabatt wenn erfolgreich */
        AGGRESSIVE("Aggressiv", -0.15f, 1.3f),

        /** Logisch argumentieren - Ausgeglichen */
        LOGICAL("Logisch", 0.05f, 1.0f),

        /** Mitleid erregen - Funktioniert bei netten NPCs */
        SYMPATHY("Mitleid", 0.0f, 0.9f);

        private final String displayName;
        private final float baseChanceModifier;
        private final float discountMultiplier;

        NegotiationTactic(String displayName, float baseChanceModifier, float discountMultiplier) {
            this.displayName = displayName;
            this.baseChanceModifier = baseChanceModifier;
            this.discountMultiplier = discountMultiplier;
        }

        public String getDisplayName() { return displayName; }
        public float getBaseChanceModifier() { return baseChanceModifier; }
        public float getDiscountMultiplier() { return discountMultiplier; }
    }

    /**
     * Ergebnis einer Verhandlungsrunde
     */
    public enum ResultType {
        SUCCESS,        // Rabatt erhalten
        FAILED,         // Kein Rabatt
        NPC_REFUSED,    // NPC verhandelt nicht mehr
        DEAL_MADE,      // Preis akzeptiert
        SESSION_ENDED   // Sitzung bereits beendet
    }

    /**
     * Grund für Verhandlungsende
     */
    public enum FinishReason {
        ACCEPTED,          // Spieler hat akzeptiert
        PLAYER_CANCELLED,  // Spieler hat abgebrochen
        NPC_ANGRY,         // NPC ist verärgert
        NPC_CONCEDED,      // NPC hat aufgegeben
        MAX_ROUNDS         // Maximale Runden erreicht
    }

    /**
     * Ergebnis einer Verhandlungsrunde
     */
    public record NegotiationResult(ResultType type, int newPrice, float discountGained) {
        public boolean isSuccess() {
            return type == ResultType.SUCCESS || type == ResultType.DEAL_MADE;
        }
    }

    /**
     * Eine aktive Verhandlungssitzung
     */
    public static class NegotiationSession {
        private final CustomNPCEntity npc;
        private final ServerPlayer player;
        private final int originalPrice;
        private int currentPrice;
        private final int maxRounds;
        private int currentRound = 0;
        private int currentPatience;
        private float totalDiscount = 0.0f;
        private boolean finished = false;
        private FinishReason finishReason;
        private final boolean isBuying;

        public NegotiationSession(CustomNPCEntity npc, ServerPlayer player, int originalPrice,
                                 int maxRounds, boolean isBuying) {
            this.npc = npc;
            this.player = player;
            this.originalPrice = originalPrice;
            this.currentPrice = originalPrice;
            this.maxRounds = maxRounds;
            this.currentPatience = maxRounds;
            this.isBuying = isBuying;
        }

        public void incrementRound() {
            currentRound++;
        }

        public void decreasePatience(int amount) {
            currentPatience = Math.max(0, currentPatience - amount);
        }

        public int applyDiscount(float discount) {
            totalDiscount += discount;
            if (isBuying) {
                currentPrice = (int) (originalPrice * (1.0f - totalDiscount));
            } else {
                // Beim Verkaufen: Spieler bekommt mehr
                currentPrice = (int) (originalPrice * (1.0f + totalDiscount));
            }
            return currentPrice;
        }

        public void finish(FinishReason reason) {
            this.finished = true;
            this.finishReason = reason;
        }

        // Getters
        public CustomNPCEntity getNpc() { return npc; }
        public ServerPlayer getPlayer() { return player; }
        public int getOriginalPrice() { return originalPrice; }
        public int getCurrentPrice() { return currentPrice; }
        public int getMaxRounds() { return maxRounds; }
        public int getCurrentRound() { return currentRound; }
        public int getCurrentPatience() { return currentPatience; }
        public float getTotalDiscount() { return totalDiscount; }
        public boolean isFinished() { return finished; }
        public FinishReason getFinishReason() { return finishReason; }
        public boolean isBuying() { return isBuying; }
    }
}
