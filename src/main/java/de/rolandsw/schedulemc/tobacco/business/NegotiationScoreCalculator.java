package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.messaging.Conversation;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCPersonality;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.personality.NPCPersonalityTrait;
import de.rolandsw.schedulemc.npc.personality.NPCRelationship;
import de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager;
import de.rolandsw.schedulemc.production.core.DrugType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

/**
 * Zentrale Score-Berechnung für das Verhandlungssystem
 *
 * Score-Komponenten (0-100 Punkte gesamt):
 * - Global Reputation:     0-20 Punkte (20%)
 * - NPC Relationship:      0-25 Punkte (25%)
 * - Personality Bonus:     0-15 Punkte (15%)
 * - Addiction Match:       0-25 Punkte (25%)
 * - Wallet Capacity:       0-15 Punkte (15%)
 */
public class NegotiationScoreCalculator {

    // ═══════════════════════════════════════════════════════════
    // SCORE WEIGHTS (können über Config angepasst werden)
    // ═══════════════════════════════════════════════════════════

    public static final float WEIGHT_GLOBAL_REP = 0.20f;      // 20 Punkte max
    public static final float WEIGHT_NPC_RELATION = 0.25f;    // 25 Punkte max
    public static final float WEIGHT_PERSONALITY = 0.15f;     // 15 Punkte max
    public static final float WEIGHT_ADDICTION = 0.25f;       // 25 Punkte max
    public static final float WEIGHT_WALLET = 0.15f;          // 15 Punkte max

    public static final int MAX_SCORE = 100;

    // ═══════════════════════════════════════════════════════════
    // SCORE COMPONENTS
    // ═══════════════════════════════════════════════════════════

    private int globalRepScore;      // 0-20
    private int npcRelationScore;    // 0-25
    private int personalityScore;    // 0-15
    private int addictionScore;      // 0-25
    private int walletScore;         // 0-15

    private int totalScore;          // 0-100
    private int abortRisk;           // 0-100%

    // Cached values
    private float globalReputation;
    private int npcRelationLevel;
    private NPCPersonality personality;
    private NPCPersonalityTrait trait;
    private int addictionLevel;
    private int walletBalance;
    private double requiredBudget;

    // ═══════════════════════════════════════════════════════════
    // CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den kompletten Score für eine Verhandlung
     */
    public NegotiationScoreCalculator calculate(
            CustomNPCEntity npc,
            Player player,
            DrugType drugType,
            double estimatedPrice
    ) {
        NPCData npcData = npc.getNpcData();
        UUID playerUUID = player.getUUID();
        UUID npcUUID = npcData.getNpcUUID();

        // 1. Global Reputation (Durchschnitt aller NPC-Konversationen)
        globalReputation = calculateGlobalReputation(playerUUID);
        globalRepScore = (int) ((globalReputation / 100.0f) * (MAX_SCORE * WEIGHT_GLOBAL_REP));

        // 2. NPC Relationship (direkte Beziehung zum NPC)
        NPCRelationship relationship = NPCRelationshipManager.getInstance()
            .getOrCreateRelationship(npcUUID, playerUUID);
        npcRelationLevel = relationship.getRelationshipLevel();
        // Normalisieren von -100..+100 auf 0..100
        float normalizedRelation = (npcRelationLevel + 100) / 200.0f;
        npcRelationScore = (int) (normalizedRelation * (MAX_SCORE * WEIGHT_NPC_RELATION));

        // 3. Personality Bonus (NPC-Persönlichkeit)
        personality = getPersonalityFromNPC(npc);
        trait = getTraitFromNPC(npc);
        personalityScore = calculatePersonalityScore(personality, trait);

        // 4. Addiction Match (passt die Droge zum Suchtprofil?)
        NPCAddictionProfile addictionProfile = getAddictionProfile(npc);
        addictionLevel = addictionProfile.getPreference(drugType);
        addictionScore = addictionProfile.getAddictionScoreBonus(drugType);
        // Normalisieren auf 0-25 (kann negativ sein bei keinem Interesse)
        addictionScore = Math.max(0, (int) ((addictionScore + 20) / 45.0f * (MAX_SCORE * WEIGHT_ADDICTION)));

        // 5. Wallet Capacity (kann sich NPC das leisten?)
        walletBalance = npcData.getWallet();
        requiredBudget = estimatedPrice;
        walletScore = calculateWalletScore(walletBalance, estimatedPrice, personality);

        // Gesamt-Score berechnen
        totalScore = globalRepScore + npcRelationScore + personalityScore + addictionScore + walletScore;
        totalScore = Math.max(0, Math.min(MAX_SCORE, totalScore));

        // Abbruch-Risiko berechnen (invers zum Score, mit Persönlichkeits-Faktor)
        abortRisk = calculateAbortRisk(totalScore, trait);

        return this;
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die globale Spieler-Reputation als Durchschnitt aller NPC-Konversationen
     */
    private float calculateGlobalReputation(UUID playerUUID) {
        List<Conversation> conversations = MessageManager.getConversations(playerUUID);

        if (conversations == null || conversations.isEmpty()) {
            return 50.0f; // Neutral für neue Spieler
        }

        int totalReputation = 0;
        int npcConversationCount = 0;

        for (Conversation conv : conversations) {
            if (!conv.isPlayerParticipant()) {
                totalReputation += conv.getReputation();
                npcConversationCount++;
            }
        }

        if (npcConversationCount == 0) {
            return 50.0f;
        }

        return (float) totalReputation / npcConversationCount;
    }

    /**
     * Berechnet den Persönlichkeits-Score
     */
    private int calculatePersonalityScore(NPCPersonality personality, NPCPersonalityTrait trait) {
        int maxPoints = (int) (MAX_SCORE * WEIGHT_PERSONALITY);

        // NPCPersonality Bonus
        int personalityBonus = switch (personality) {
            case IMPULSIV -> 5;   // Impulsive NPCs kaufen leichter
            case AUSGEWOGEN -> 3;
            case SPARSAM -> 0;    // Sparsame NPCs sind kritischer
        };

        // NPCPersonalityTrait Bonus
        int traitBonus = switch (trait) {
            case GENEROUS -> 5;   // Großzügige sind offen
            case FRIENDLY -> 4;
            case NEUTRAL -> 2;
            case GREEDY -> 1;     // Gierige wollen gute Deals
            case SUSPICIOUS -> 0; // Misstrauische sind kritisch
        };

        return Math.min(maxPoints, personalityBonus + traitBonus);
    }

    /**
     * Berechnet den Wallet-Score
     */
    private int calculateWalletScore(int wallet, double price, NPCPersonality personality) {
        int maxPoints = (int) (MAX_SCORE * WEIGHT_WALLET);

        if (wallet <= 0 || price <= 0) {
            return 0;
        }

        // Max Budget basierend auf Persönlichkeit
        float maxBudgetPercent = personality.getMaxBudgetPercent();
        double maxBudget = wallet * maxBudgetPercent;

        if (price > wallet) {
            return 0; // Kann sich das nicht leisten
        }

        float budgetUsage = (float) (price / maxBudget);

        if (budgetUsage <= 0.3f) {
            return maxPoints; // Nur 30% des Budgets = super
        } else if (budgetUsage <= 0.5f) {
            return (int) (maxPoints * 0.8f);
        } else if (budgetUsage <= 0.7f) {
            return (int) (maxPoints * 0.5f);
        } else if (budgetUsage <= 1.0f) {
            return (int) (maxPoints * 0.3f);
        } else {
            return 0; // Über Budget
        }
    }

    /**
     * Berechnet das Abbruch-Risiko
     */
    private int calculateAbortRisk(int score, NPCPersonalityTrait trait) {
        // Basis-Risiko invers zum Score
        int baseRisk = MAX_SCORE - score;

        // Trait-Multiplikator
        float traitMult = switch (trait) {
            case SUSPICIOUS -> 1.4f;
            case GREEDY -> 1.2f;
            case NEUTRAL -> 1.0f;
            case FRIENDLY -> 0.8f;
            case GENEROUS -> 0.6f;
        };

        return Math.min(100, (int) (baseRisk * traitMult));
    }

    /**
     * Holt die Persönlichkeit des NPCs
     */
    private NPCPersonality getPersonalityFromNPC(CustomNPCEntity npc) {
        String personalityStr = npc.getNpcData().getCustomData().getString("personality");
        if (personalityStr.isEmpty()) {
            NPCPersonality newPersonality = NPCPersonality.getRandom(npc.getRandom());
            npc.getNpcData().getCustomData().putString("personality", newPersonality.name());
            return newPersonality;
        }
        try {
            return NPCPersonality.valueOf(personalityStr);
        } catch (IllegalArgumentException e) {
            return NPCPersonality.AUSGEWOGEN;
        }
    }

    /**
     * Holt den Trait des NPCs
     */
    private NPCPersonalityTrait getTraitFromNPC(CustomNPCEntity npc) {
        String traitStr = npc.getNpcData().getCustomData().getString("personalityTrait");
        if (traitStr.isEmpty()) {
            NPCPersonalityTrait newTrait = NPCPersonalityTrait.random();
            npc.getNpcData().getCustomData().putString("personalityTrait", newTrait.name());
            return newTrait;
        }
        try {
            return NPCPersonalityTrait.valueOf(traitStr);
        } catch (IllegalArgumentException e) {
            return NPCPersonalityTrait.NEUTRAL;
        }
    }

    /**
     * Holt oder erstellt das Addiction Profile des NPCs
     */
    private NPCAddictionProfile getAddictionProfile(CustomNPCEntity npc) {
        if (npc.getNpcData().getCustomData().contains("addiction_TOBACCO")) {
            // Profile existiert bereits
            return NPCAddictionProfile.load(npc.getNpcData().getCustomData());
        } else {
            // Neues Profile generieren
            NPCAddictionProfile profile = NPCAddictionProfile.generateRandom(npc.getRandom());
            profile.save(npc.getNpcData().getCustomData());
            return profile;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public int getGlobalRepScore() {
        return globalRepScore;
    }

    public int getNpcRelationScore() {
        return npcRelationScore;
    }

    public int getPersonalityScore() {
        return personalityScore;
    }

    public int getAddictionScore() {
        return addictionScore;
    }

    public int getWalletScore() {
        return walletScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getAbortRisk() {
        return abortRisk;
    }

    public float getGlobalReputation() {
        return globalReputation;
    }

    public int getNpcRelationLevel() {
        return npcRelationLevel;
    }

    public NPCPersonality getPersonality() {
        return personality;
    }

    public NPCPersonalityTrait getTrait() {
        return trait;
    }

    public int getAddictionLevel() {
        return addictionLevel;
    }

    public int getWalletBalance() {
        return walletBalance;
    }

    // ═══════════════════════════════════════════════════════════
    // NETWORK SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(globalRepScore);
        buf.writeInt(npcRelationScore);
        buf.writeInt(personalityScore);
        buf.writeInt(addictionScore);
        buf.writeInt(walletScore);
        buf.writeInt(totalScore);
        buf.writeInt(abortRisk);
        buf.writeFloat(globalReputation);
        buf.writeInt(npcRelationLevel);
        buf.writeEnum(personality);
        buf.writeEnum(trait);
        buf.writeInt(addictionLevel);
        buf.writeInt(walletBalance);
    }

    public static NegotiationScoreCalculator decode(FriendlyByteBuf buf) {
        NegotiationScoreCalculator calc = new NegotiationScoreCalculator();
        calc.globalRepScore = buf.readInt();
        calc.npcRelationScore = buf.readInt();
        calc.personalityScore = buf.readInt();
        calc.addictionScore = buf.readInt();
        calc.walletScore = buf.readInt();
        calc.totalScore = buf.readInt();
        calc.abortRisk = buf.readInt();
        calc.globalReputation = buf.readFloat();
        calc.npcRelationLevel = buf.readInt();
        calc.personality = buf.readEnum(NPCPersonality.class);
        calc.trait = buf.readEnum(NPCPersonalityTrait.class);
        calc.addictionLevel = buf.readInt();
        calc.walletBalance = buf.readInt();
        return calc;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format(
            "NegotiationScore[total=%d/100, risk=%d%%]\n" +
            "  GlobalRep: %d (%.0f%%)\n" +
            "  NPCRelation: %d (%+d level)\n" +
            "  Personality: %d (%s, %s)\n" +
            "  Addiction: %d (%d%% interest)\n" +
            "  Wallet: %d (€%d available)",
            totalScore, abortRisk,
            globalRepScore, globalReputation,
            npcRelationScore, npcRelationLevel,
            personalityScore, personality, trait,
            addictionScore, addictionLevel,
            walletScore, walletBalance
        );
    }
}
