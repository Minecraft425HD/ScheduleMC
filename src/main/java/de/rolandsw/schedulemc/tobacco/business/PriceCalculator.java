package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;

/**
 * Berechnet dynamische Preise basierend auf NPC-Metriken
 */
public class PriceCalculator {

    /**
     * Berechnet Basis-Preis (ohne NPC-Modifier)
     */
    public static double calculateBasePrice(TobaccoType type, TobaccoQuality quality, int weight) {
        double pricePerGram = type.getBasePrice();
        double qualityMultiplier = quality.getPriceMultiplier();
        return pricePerGram * qualityMultiplier * weight;
    }

    /**
     * Berechnet fairen NPC-Preis (mit allen Modifikatoren)
     */
    public static double calculateFairPrice(TobaccoType type, TobaccoQuality quality, int weight,
                                           DemandLevel demand, int playerReputation, int satisfaction) {
        double basePrice = calculateBasePrice(type, quality, weight);

        // Nachfrage-Multiplikator
        double demandMultiplier = demand.getPriceMultiplier();

        // Reputation-Multiplikator (0-100 -> 0.85-1.20)
        double reputationMultiplier = TobaccoBusinessConstants.Price.REPUTATION_BASE_MULTIPLIER
            + (playerReputation / 100.0 * TobaccoBusinessConstants.Price.REPUTATION_RANGE_MULTIPLIER);

        // Zufriedenheits-Multiplikator (0-100 -> 0.80-1.10)
        double satisfactionMultiplier = TobaccoBusinessConstants.Price.SATISFACTION_BASE_MULTIPLIER
            + (satisfaction / 100.0 * TobaccoBusinessConstants.Price.SATISFACTION_RANGE_MULTIPLIER);

        return basePrice * demandMultiplier * reputationMultiplier * satisfactionMultiplier;
    }

    /**
     * Berechnet minimalen akzeptablen Preis
     */
    public static double calculateMinPrice(double fairPrice) {
        return fairPrice * TobaccoBusinessConstants.Price.MIN_PRICE_RATIO;
    }

    /**
     * Berechnet maximalen akzeptablen Preis
     */
    public static double calculateMaxPrice(double fairPrice) {
        return fairPrice * TobaccoBusinessConstants.Price.MAX_PRICE_RATIO;
    }

    /**
     * Berechnet idealen Preis für NPC
     */
    public static double calculateIdealPrice(double fairPrice) {
        return fairPrice * TobaccoBusinessConstants.Price.IDEAL_PRICE_RATIO;
    }
}
