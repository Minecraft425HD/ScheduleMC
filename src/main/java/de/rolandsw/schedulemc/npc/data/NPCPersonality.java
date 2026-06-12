package de.rolandsw.schedulemc.npc.data;

import net.minecraft.util.RandomSource;

/**
 * Defines three personality types for NPCs that affect their purchasing behavior
 */
public enum NPCPersonality {
    /**
     * FRUGAL (Cautious Buyer)
     * - Very price-sensitive and cautious
     * - High mood weight (40%), low demand weight (20%)
     * - Uses max 30% of wallet per purchase
     * - Needs 50+ score to buy
     */
    FRUGAL(0.4f, 0.2f, 0.3f, 50),

    /**
     * BALANCED (Standard Buyer)
     * - Balanced purchasing behavior
     * - Equal mood and demand weight (30% each)
     * - Uses max 50% of wallet per purchase
     * - Needs 40+ score to buy
     */
    BALANCED(0.3f, 0.3f, 0.5f, 40),

    /**
     * IMPULSIVE (Impulsive Buyer)
     * - Spontaneous and generous buying
     * - Low mood weight (20%), high demand weight (40%)
     * - Uses max 70% of wallet per purchase
     * - Needs only 30+ score to buy
     */
    IMPULSIVE(0.2f, 0.4f, 0.7f, 30);

    private final float moodWeight;
    private final float demandWeight;
    private final float maxBudgetPercent;
    private final int purchaseThreshold;

    NPCPersonality(float moodWeight, float demandWeight, float maxBudgetPercent, int purchaseThreshold) {
        this.moodWeight = moodWeight;
        this.demandWeight = demandWeight;
        this.maxBudgetPercent = maxBudgetPercent;
        this.purchaseThreshold = purchaseThreshold;
    }

    /**
     * Weight for mood score in purchase decision (0.0 - 1.0)
     */
    public float getMoodWeight() {
        return moodWeight;
    }

    /**
     * Weight for demand score in purchase decision (0.0 - 1.0)
     */
    public float getDemandWeight() {
        return demandWeight;
    }

    /**
     * Maximum percentage of wallet balance to spend per purchase (0.0 - 1.0)
     */
    public float getMaxBudgetPercent() {
        return maxBudgetPercent;
    }

    /**
     * Minimum score required to make a purchase (0-100)
     */
    public int getPurchaseThreshold() {
        return purchaseThreshold;
    }

    /**
     * Derives the purchasing profile from the NPC's trait axes.
     * Greedy NPCs buy cautiously, generous ones impulsively.
     */
    public static NPCPersonality fromTraits(de.rolandsw.schedulemc.npc.life.core.NPCTraits traits) {
        if (traits.getGreed() >= 30) return FRUGAL;
        if (traits.getGreed() <= -30) return IMPULSIVE;
        return BALANCED;
    }

    /**
     * Gets a random personality for new NPCs
     */
    public static NPCPersonality getRandom(RandomSource random) {
        NPCPersonality[] values = values();
        return values[random.nextInt(values.length)];
    }
}
