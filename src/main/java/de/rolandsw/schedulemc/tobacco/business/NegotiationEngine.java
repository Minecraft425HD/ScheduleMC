package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.util.SecureRandomUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Verhandlungs-Algorithmus
 * SICHERHEIT: Verwendet SecureRandom für unvorhersagbare NPC-Entscheidungen
 */
public class NegotiationEngine {

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

            // SICHERHEIT: SecureRandom für NPC-Entscheidungen
            if (SecureRandomUtil.chance(acceptChance) || round >= 3) {
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
                                                ItemStack tobaccoItem, double offeredPrice) {
        NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);

        // Parse from PackagedDrugItem
        String variantStr = PackagedDrugItem.getVariant(tobaccoItem);
        TobaccoType type = variantStr != null ? TobaccoType.valueOf(variantStr.split("\\.")[1]) : TobaccoType.VIRGINIA;

        String qualityStr = PackagedDrugItem.getQuality(tobaccoItem);
        TobaccoQuality quality = qualityStr != null ? TobaccoQuality.valueOf(qualityStr.split("\\.")[1]) : TobaccoQuality.GUT;

        int weight = PackagedDrugItem.getWeight(tobaccoItem);

        NegotiationEngine engine = new NegotiationEngine(type, quality, weight, metrics, player.getStringUUID());
        return engine.calculateResponse(offeredPrice, 1);
    }
}
