package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCPersonality;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

/**
 * Zentrale Klasse für NPC-Kaufentscheidungen mit Weighted-Scoring
 *
 * Berechnet basierend auf verschiedenen Faktoren:
 * - Mood Score (0-30 Punkte): Kombination aus Reputation, Satisfaction, Player Reputation
 * - Demand Score (0-25 Punkte): Basierend auf aktueller Nachfrage
 * - Reputation Score (0-20 Punkte): Spieler-Reputation beim NPC
 * - Satisfaction Score (0-15 Punkte): NPC Zufriedenheit
 * - Deal History Score (0-10 Punkte): Erfolgreiche Deals in letzten 7 Tagen
 * - Time Score (±5 Punkte): Tageszeit und Wochentag-Faktoren
 * - Random Bonus (±10 Punkte): Zufälliger Faktor für Unvorhersehbarkeit
 *
 * Total: 0-115 Punkte (mit Bonus/Malus)
 */
public class NPCPurchaseDecision {

    private final CustomNPCEntity npc;
    private final Player player;
    private final NPCPersonality personality;
    private final NPCBusinessMetrics metrics;
    private final NPCData npcData;

    // Scoring-Komponenten
    private float moodScore = 0;
    private float demandScore = 0;
    private float reputationScore = 0;
    private float satisfactionScore = 0;
    private float dealHistoryScore = 0;
    private float timeScore = 0;
    private float randomBonus = 0;

    private float totalScore = 0;
    private boolean willingToBuy = false;
    private int desiredAmount = 0;

    public NPCPurchaseDecision(CustomNPCEntity npc, Player player) {
        this.npc = npc;
        this.player = player;
        this.npcData = npc.getNpcData();
        this.personality = getPersonalityFromNPC();
        this.metrics = new NPCBusinessMetrics(npc);
    }

    /**
     * Hauptmethode: Berechnet die Kaufentscheidung
     */
    public NPCPurchaseDecision calculate() {
        // Phase 1: Alle Scoring-Komponenten berechnen
        calculateMoodScore();
        calculateDemandScore();
        calculateReputationScore();
        calculateSatisfactionScore();
        calculateDealHistoryScore();
        calculateTimeScore();
        calculateRandomBonus();

        // Phase 2: Gewichtete Summierung
        totalScore = 0;
        totalScore += moodScore * personality.getMoodWeight() * 100;
        totalScore += demandScore * personality.getDemandWeight() * 100;
        totalScore += reputationScore;
        totalScore += satisfactionScore;
        totalScore += dealHistoryScore;
        totalScore += timeScore;
        totalScore += randomBonus;

        // Phase 3: Schwellenwert prüfen
        willingToBuy = totalScore >= personality.getPurchaseThreshold();

        // Phase 4: Menge berechnen
        if (willingToBuy) {
            desiredAmount = calculatePurchaseAmount();
        } else {
            desiredAmount = 0;
        }

        return this;
    }

    /**
     * Mood Score: Kombination aus Reputation, Satisfaction und Player Reputation
     * 0-30 Punkte (normalisiert auf 0-1 für Gewichtung)
     */
    private void calculateMoodScore() {
        String playerUUID = player.getStringUUID();

        float reputation = metrics.getReputation(playerUUID) / 100.0f; // 0-1
        float satisfaction = metrics.getSatisfaction() / 100.0f; // 0-1
        float playerRep = getGlobalPlayerReputation() / 100.0f; // 0-1

        // Durchschnitt der drei Faktoren
        moodScore = (reputation + satisfaction + playerRep) / 3.0f; // 0-1 für Gewichtung
    }

    /**
     * Demand Score: Basierend auf aktueller Nachfrage
     * 0-25 Punkte (normalisiert auf 0-1 für Gewichtung)
     */
    private void calculateDemandScore() {
        DemandLevel demand = metrics.getDemand();
        demandScore = switch(demand) {
            case VERY_LOW -> 0.1f;
            case LOW -> 0.3f;
            case MEDIUM -> 0.5f;
            case HIGH -> 0.7f;
            case VERY_HIGH -> 1.0f;
        };
    }

    /**
     * Reputation Score: Spieler-Reputation beim NPC
     * 0-20 Punkte
     */
    private void calculateReputationScore() {
        String playerUUID = player.getStringUUID();
        int reputation = metrics.getReputation(playerUUID);
        reputationScore = (reputation / 100.0f) * 20.0f;
    }

    /**
     * Satisfaction Score: NPC Zufriedenheit
     * 0-15 Punkte
     */
    private void calculateSatisfactionScore() {
        int satisfaction = metrics.getSatisfaction();
        satisfactionScore = (satisfaction / 100.0f) * 15.0f;
    }

    /**
     * Deal History Score: Anzahl erfolgreicher Deals in letzten 7 Tagen
     * 0-10 Punkte (2 Punkte pro Deal, max 5 Deals)
     */
    private void calculateDealHistoryScore() {
        String playerUUID = player.getStringUUID();
        long currentDay = npc.level().getDayTime() / 24000L;

        int recentDeals = 0;
        for (Purchase purchase : metrics.getPurchaseHistory()) {
            if (purchase.getPlayerUUID().equals(playerUUID)) {
                long daysSince = currentDay - purchase.getDay();
                if (daysSince <= 7) {
                    recentDeals++;
                }
            }
        }

        dealHistoryScore = Math.min(recentDeals * 2.0f, 10.0f);
    }

    /**
     * Time Score: Tageszeit-Faktoren
     * ±5 Punkte basierend auf Tageszeit
     */
    private void calculateTimeScore() {
        long dayTime = npc.level().getDayTime() % 24000;

        // Minecraft Tageszeiten:
        // 0 = 6:00 (Morgen)
        // 6000 = 12:00 (Mittag)
        // 12000 = 18:00 (Abend)
        // 18000 = 0:00 (Mitternacht)

        if (dayTime >= 0 && dayTime < 3000) {
            timeScore = 2.0f; // Früher Morgen: +2 (frisch und motiviert)
        } else if (dayTime >= 3000 && dayTime < 9000) {
            timeScore = 1.0f; // Vormittag bis Mittag: +1
        } else if (dayTime >= 9000 && dayTime < 15000) {
            timeScore = 0.0f; // Nachmittag: neutral
        } else if (dayTime >= 15000 && dayTime < 18000) {
            timeScore = -1.0f; // Später Abend: -1 (müde)
        } else {
            timeScore = -3.0f; // Nacht: -3 (schlafen)
        }
    }

    /**
     * Random Bonus: Zufälliger Faktor für Unvorhersehbarkeit
     * -10 bis +10 Punkte
     */
    private void calculateRandomBonus() {
        Random random = npc.getRandom();
        randomBonus = (random.nextFloat() * 20.0f) - 10.0f;
    }

    /**
     * Berechnet die gewünschte Kaufmenge basierend auf Score und Budget
     * 0-10g für Standard-NPCs
     */
    private int calculatePurchaseAmount() {
        // 1. Score-basierte Menge (0-10g)
        float scorePercent = Math.min(totalScore / 100.0f, 1.0f);
        int maxGrams = 10;
        int scoreBasedAmount = Math.round(scorePercent * maxGrams);

        // 2. Budget-Limit prüfen
        int walletBalance = npcData.getWallet();
        float estimatedPricePerGram = 5.0f; // Durchschnittspreis, wird später genauer berechnet
        float maxBudget = walletBalance * personality.getMaxBudgetPercent();
        int budgetBasedAmount = (int)(maxBudget / estimatedPricePerGram);

        // 3. Minimum nehmen und auf 0-10g limitieren
        int finalAmount = Math.min(Math.min(scoreBasedAmount, budgetBasedAmount), maxGrams);

        // 4. Mindestens 1g wenn überhaupt kaufen
        return Math.max(finalAmount, 1);
    }

    /**
     * Holt die Persönlichkeit des NPCs (oder weist eine zu, wenn nicht vorhanden)
     */
    private NPCPersonality getPersonalityFromNPC() {
        // Versuche Persönlichkeit aus customData zu laden
        String personalityStr = npcData.getCustomData().getString("personality");

        if (personalityStr.isEmpty()) {
            // Keine Persönlichkeit vorhanden -> zufällig zuweisen
            NPCPersonality newPersonality = NPCPersonality.getRandom(npc.getRandom());
            npcData.getCustomData().putString("personality", newPersonality.name());
            return newPersonality;
        }

        try {
            return NPCPersonality.valueOf(personalityStr);
        } catch (IllegalArgumentException e) {
            // Ungültige Persönlichkeit -> Standard
            return NPCPersonality.AUSGEWOGEN;
        }
    }

    /**
     * Holt die globale Spieler-Reputation (wenn implementiert)
     * Placeholder: 50 (neutral)
     */
    private float getGlobalPlayerReputation() {
        // TODO: Implement global player reputation system
        return 50.0f;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS für Debugging und GUI-Anzeige
    // ═══════════════════════════════════════════════════════════

    public float getTotalScore() {
        return totalScore;
    }

    public boolean isWillingToBuy() {
        return willingToBuy;
    }

    public int getDesiredAmount() {
        return desiredAmount;
    }

    public float getMoodScore() {
        return moodScore;
    }

    public float getDemandScore() {
        return demandScore;
    }

    public float getReputationScore() {
        return reputationScore;
    }

    public float getSatisfactionScore() {
        return satisfactionScore;
    }

    public float getDealHistoryScore() {
        return dealHistoryScore;
    }

    public float getTimeScore() {
        return timeScore;
    }

    public float getRandomBonus() {
        return randomBonus;
    }

    public NPCPersonality getPersonality() {
        return personality;
    }

    /**
     * Debug-String für Entwicklung
     */
    @Override
    public String toString() {
        return String.format(
            "NPCPurchaseDecision[personality=%s, totalScore=%.1f, willing=%s, amount=%dg]\n" +
            "  Mood: %.2f, Demand: %.2f, Reputation: %.1f, Satisfaction: %.1f\n" +
            "  History: %.1f, Time: %.1f, Random: %.1f",
            personality, totalScore, willingToBuy, desiredAmount,
            moodScore, demandScore, reputationScore, satisfactionScore,
            dealHistoryScore, timeScore, randomBonus
        );
    }
}
