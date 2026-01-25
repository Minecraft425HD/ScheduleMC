package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Verhandlungs-Algorithmus
 */
public class NegotiationEngine {

    private static final Random random = new Random();

    private final TobaccoType type;
    private final TobaccoQuality quality;
    private final int weight;
    private final NPCBusinessMetrics metrics;
    private final String playerUUID;

    private final double fairPrice;
    private final double minAcceptable;
    private final double maxAcceptable;

    public NegotiationEngine(TobaccoType type, TobaccoQuality quality, int weight,
                            NPCBusinessMetrics metrics, String playerUUID) {
        this.type = type;
        this.quality = quality;
        this.weight = weight;
        this.metrics = metrics;
        this.playerUUID = playerUUID;

        int reputation = metrics.getReputation(playerUUID);
        int satisfaction = metrics.getSatisfaction();
        DemandLevel demand = metrics.getDemand();

        this.fairPrice = PriceCalculator.calculateFairPrice(type, quality, weight, demand, reputation, satisfaction);
        this.minAcceptable = PriceCalculator.calculateMinPrice(fairPrice);
        this.maxAcceptable = PriceCalculator.calculateMaxPrice(fairPrice);
    }

    /**
     * NPC-Reaktion auf Spieler-Angebot
     */
    public NPCResponse calculateResponse(double playerOffer, int round) {

        // Check 1: Absurd zu hoch
        if (playerOffer > maxAcceptable * 1.3) {
            return new NPCResponse(
                false,
                maxAcceptable * 0.8,
                "Das ist absurd! Maximal " + String.format("%.2f€", maxAcceptable * 0.8) + "!",
                -5
            );
        }

        // Check 2: Zu hoch, aber verhandelbar
        if (playerOffer > fairPrice) {
            double counter = fairPrice * 0.9;
            return new NPCResponse(
                false,
                counter,
                "Das ist mir zu viel. Wie wäre es mit " + String.format("%.2f€", counter) + "?",
                0
            );
        }

        // Check 3: Fair - NPC entscheidet
        if (playerOffer >= minAcceptable) {
            // Akzeptanz-Chance steigt mit Reputation & Zufriedenheit
            int reputation = metrics.getReputation(playerUUID);
            int satisfaction = metrics.getSatisfaction();
            double acceptChance = 0.3 + (satisfaction / 100.0 * 0.4) + (reputation / 100.0 * 0.3);

            if (random.nextDouble() < acceptChance || round >= 3) {
                return new NPCResponse(
                    true,
                    playerOffer,
                    "In Ordnung, das ist ein fairer Preis!",
                    +3
                );
            } else {
                double counter = playerOffer * 0.95;
                return new NPCResponse(
                    false,
                    counter,
                    "Fast! Ich würde " + String.format("%.2f€", counter) + " zahlen.",
                    0
                );
            }
        }

        // Check 4: Zu niedrig
        return new NPCResponse(
            false,
            minAcceptable,
            "Das ist zu wenig. Mindestens " + String.format("%.2f€", minAcceptable) + "!",
            -2
        );
    }

    public double getFairPrice() {
        return fairPrice;
    }

    public double getMinAcceptable() {
        return minAcceptable;
    }

    public double getMaxAcceptable() {
        return maxAcceptable;
    }

    /**
     * Static Helper-Methode für einfache Verhandlung
     */
    public static NPCResponse handleNegotiation(CustomNPCEntity npc, ServerPlayer player,
                                                ItemStack drugItem, double offeredPrice) {
        NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);

        // Prüfe DrugType - nur für Tabak spezifische Typen parsen
        de.rolandsw.schedulemc.production.core.DrugType drugType = PackagedDrugItem.getDrugType(drugItem);

        TobaccoType type = TobaccoType.VIRGINIA;  // Default
        TobaccoQuality quality = TobaccoQuality.GUT;  // Default

        if (drugType == de.rolandsw.schedulemc.production.core.DrugType.TOBACCO) {
            // Nur für Tabak: Parse spezifische Typen
            String variantStr = PackagedDrugItem.getVariant(drugItem);
            if (variantStr != null && variantStr.contains(".")) {
                try {
                    type = TobaccoType.valueOf(variantStr.split("\\.")[1]);
                } catch (IllegalArgumentException e) {
                    type = TobaccoType.VIRGINIA;
                }
            }

            String qualityStr = PackagedDrugItem.getQuality(drugItem);
            if (qualityStr != null && qualityStr.contains(".")) {
                try {
                    quality = TobaccoQuality.valueOf(qualityStr.split("\\.")[1]);
                } catch (IllegalArgumentException e) {
                    quality = TobaccoQuality.GUT;
                }
            }
        }
        // Für andere Drogenarten: Verwende Default-Werte (VIRGINIA/GUT)
        // Dies ermöglicht generische Verhandlung mit demselben Engine

        int weight = PackagedDrugItem.getWeight(drugItem);

        NegotiationEngine engine = new NegotiationEngine(type, quality, weight, metrics, player.getStringUUID());
        return engine.calculateResponse(offeredPrice, 1);
    }
}
