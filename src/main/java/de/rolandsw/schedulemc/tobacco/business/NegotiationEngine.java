package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Verhandlungs-Algorithmus mit dynamischen Gegenangeboten
 *
 * Der NPC erhöht sein Gegenangebot mit jeder Runde, um sich
 * in der Mitte mit dem Spieler zu treffen.
 */
public class NegotiationEngine {

    private static final Random random = new Random();
    private static final String NEGOTIATION_STATE_KEY = "NegotiationState_";

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
     * NPC-Reaktion auf Spieler-Angebot - DYNAMISCH
     *
     * Der NPC erhöht sein Gegenangebot mit jeder Runde:
     * - Runde 1: 70% des fairPrice
     * - Runde 2: 78% des fairPrice
     * - Runde 3: 86% des fairPrice
     * - Runde 4: 94% des fairPrice
     * - Runde 5+: 100% des fairPrice (akzeptiert fair price)
     *
     * Zusätzlich: NPC bewegt sich auf das Spieler-Angebot zu
     */
    public NPCResponse calculateResponse(double playerOffer, int round, double lastNPCOffer) {

        // Basis-Prozentsatz des fairen Preises, den der NPC bereit ist zu zahlen
        // Steigt mit jeder Runde um 8%
        double basePercentage = 0.70 + (round - 1) * 0.08;
        basePercentage = Math.min(1.0, basePercentage); // Max 100%

        // NPC's Startangebot für diese Runde
        double npcBaseOffer = fairPrice * basePercentage;

        // Wenn der Spieler in vorherigen Runden angeboten hat, bewegt sich der NPC darauf zu
        double npcOffer;
        if (lastNPCOffer > 0 && playerOffer > lastNPCOffer) {
            // NPC erhöht sein Angebot um 30-50% der Differenz zum Spielerangebot
            double diff = playerOffer - lastNPCOffer;
            double increase = diff * (0.30 + random.nextDouble() * 0.20);
            npcOffer = lastNPCOffer + increase;

            // Aber nicht mehr als der faire Preis
            npcOffer = Math.min(npcOffer, fairPrice);
        } else {
            npcOffer = npcBaseOffer;
        }

        // Reputation beeinflusst die Großzügigkeit
        int reputation = metrics.getReputation(playerUUID);
        if (reputation > 50) {
            npcOffer *= 1.0 + (reputation - 50) / 200.0; // Bis zu 25% mehr
        }

        // ═══════════════════════════════════════════════════════════
        // ENTSCHEIDUNGSLOGIK
        // ═══════════════════════════════════════════════════════════

        // Check 1: Spieler-Angebot ist niedriger oder gleich NPC-Angebot -> AKZEPTIERT!
        if (playerOffer <= npcOffer) {
            return new NPCResponse(
                true,
                playerOffer,
                getAcceptMessage(round),
                +3 + (round > 3 ? 2 : 0) // Bonus-Reputation für lange Verhandlung
            );
        }

        // Check 2: Spieler ist nah dran (innerhalb 10%) -> Hohe Akzeptanz-Chance
        double difference = (playerOffer - npcOffer) / npcOffer;
        if (difference <= 0.10) {
            // Akzeptanz-Chance basierend auf Runde und Reputation
            double acceptChance = 0.3 + round * 0.15 + reputation / 200.0;
            if (random.nextDouble() < acceptChance) {
                return new NPCResponse(
                    true,
                    playerOffer,
                    getAcceptMessage(round),
                    +2
                );
            }
        }

        // Check 3: Absurd zu hoch (mehr als 50% über fairPrice)
        if (playerOffer > fairPrice * 1.5) {
            return new NPCResponse(
                false,
                npcOffer,
                getOutrageousMessage(npcOffer),
                -3
            );
        }

        // Check 4: Zu hoch, aber verhandelbar
        if (playerOffer > fairPrice) {
            // Bei späten Runden: NPC wird nachgiebiger
            if (round >= 4) {
                double compromiseOffer = (playerOffer + npcOffer) / 2; // Mitte anbieten
                return new NPCResponse(
                    false,
                    compromiseOffer,
                    getCompromiseMessage(compromiseOffer, round),
                    0
                );
            }
            return new NPCResponse(
                false,
                npcOffer,
                getCounterMessage(npcOffer, round),
                0
            );
        }

        // Check 5: Fair-Bereich - NPC macht Gegenangebot das näher am Spieler liegt
        if (playerOffer >= minAcceptable) {
            // Je höher die Runde, desto eher akzeptiert der NPC
            if (round >= 5) {
                return new NPCResponse(
                    true,
                    playerOffer,
                    "Na gut, einverstanden!",
                    +2
                );
            }

            // Gegenangebot: Mitte zwischen NPC-Angebot und Spieler-Angebot
            double meetInMiddle = (npcOffer + playerOffer) / 2;
            return new NPCResponse(
                false,
                meetInMiddle,
                getMeetMiddleMessage(meetInMiddle, round),
                0
            );
        }

        // Check 6: Zu niedrig - NPC bleibt beim Mindestpreis, aber erhöht leicht
        double lowOffer = Math.max(minAcceptable, npcOffer * 0.95);
        return new NPCResponse(
            false,
            lowOffer,
            getTooLowMessage(lowOffer),
            -1
        );
    }

    // ═══════════════════════════════════════════════════════════
    // MESSAGE GENERATORS
    // ═══════════════════════════════════════════════════════════

    private String getAcceptMessage(int round) {
        if (round == 1) {
            return "Sofort einverstanden! Das ist ein gutes Angebot!";
        } else if (round <= 3) {
            return "In Ordnung, das ist fair!";
        } else {
            return "Na gut, Sie haben mich überzeugt. Deal!";
        }
    }

    private String getOutrageousMessage(double counterOffer) {
        return String.format("Das ist absurd! Höchstens %.2f€!", counterOffer);
    }

    private String getCounterMessage(double counterOffer, int round) {
        if (round == 1) {
            return String.format("Hmm, das ist mir zu viel. Ich biete %.2f€.", counterOffer);
        } else if (round == 2) {
            return String.format("Immer noch zu hoch. Mein Angebot: %.2f€.", counterOffer);
        } else {
            return String.format("Ich erhöhe auf %.2f€. Das ist mein bestes Angebot!", counterOffer);
        }
    }

    private String getCompromiseMessage(double compromiseOffer, int round) {
        return String.format("Okay, treffen wir uns in der Mitte: %.2f€?", compromiseOffer);
    }

    private String getMeetMiddleMessage(double offer, int round) {
        if (round <= 2) {
            return String.format("Fast! Wie wäre es mit %.2f€?", offer);
        } else {
            return String.format("Ich komme Ihnen entgegen: %.2f€!", offer);
        }
    }

    private String getTooLowMessage(double minOffer) {
        return String.format("Das ist zu wenig. Mindestens %.2f€!", minOffer);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public double getFairPrice() {
        return fairPrice;
    }

    public double getMinAcceptable() {
        return minAcceptable;
    }

    public double getMaxAcceptable() {
        return maxAcceptable;
    }

    // ═══════════════════════════════════════════════════════════
    // STATIC HELPER - MIT STATE TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt oder erstellt den Verhandlungs-State für einen Spieler
     */
    public static CompoundTag getOrCreateNegotiationState(CustomNPCEntity npc, String playerUUID) {
        String key = NEGOTIATION_STATE_KEY + playerUUID;
        CompoundTag customData = npc.getNpcData().getCustomData();

        if (!customData.contains(key)) {
            CompoundTag state = new CompoundTag();
            state.putInt("round", 0);
            state.putDouble("lastNPCOffer", 0.0);
            state.putDouble("lastPlayerOffer", 0.0);
            state.putLong("startTime", System.currentTimeMillis());
            customData.put(key, state);
        }

        return customData.getCompound(key);
    }

    /**
     * Aktualisiert den Verhandlungs-State
     * WICHTIG: startTime muss beibehalten werden, sonst wird isNegotiationActive false!
     */
    public static void updateNegotiationState(CustomNPCEntity npc, String playerUUID,
                                               int round, double npcOffer, double playerOffer) {
        String key = NEGOTIATION_STATE_KEY + playerUUID;
        CompoundTag customData = npc.getNpcData().getCustomData();

        // Hole existierende startTime oder erstelle neue
        long startTime = System.currentTimeMillis();
        if (customData.contains(key)) {
            CompoundTag existingState = customData.getCompound(key);
            if (existingState.contains("startTime")) {
                startTime = existingState.getLong("startTime");
            }
        }

        CompoundTag state = new CompoundTag();
        state.putInt("round", round);
        state.putDouble("lastNPCOffer", npcOffer);
        state.putDouble("lastPlayerOffer", playerOffer);
        state.putLong("startTime", startTime);  // WICHTIG: startTime beibehalten!
        state.putLong("lastUpdate", System.currentTimeMillis());
        customData.put(key, state);
    }

    /**
     * Löscht den Verhandlungs-State (nach erfolgreichem Deal oder Abbruch)
     */
    public static void clearNegotiationState(CustomNPCEntity npc, String playerUUID) {
        String key = NEGOTIATION_STATE_KEY + playerUUID;
        npc.getNpcData().getCustomData().remove(key);
    }

    /**
     * Prüft ob die Verhandlung noch aktiv ist (nicht älter als 5 Minuten)
     */
    public static boolean isNegotiationActive(CompoundTag state) {
        if (!state.contains("startTime")) return false;
        long startTime = state.getLong("startTime");
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed < 5 * 60 * 1000; // 5 Minuten
    }

    /**
     * Static Helper-Methode für Verhandlung MIT State-Tracking
     */
    public static NPCResponse handleNegotiation(CustomNPCEntity npc, ServerPlayer player,
                                                ItemStack drugItem, double offeredPrice) {
        NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
        String playerUUID = player.getStringUUID();

        // Prüfe DrugType - nur für Tabak spezifische Typen parsen
        DrugType drugType = PackagedDrugItem.getDrugType(drugItem);

        TobaccoType type = TobaccoType.VIRGINIA;  // Default
        TobaccoQuality quality = TobaccoQuality.GUT;  // Default

        if (drugType == DrugType.TOBACCO) {
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

        int weight = PackagedDrugItem.getWeight(drugItem);

        // ═══════════════════════════════════════════════════════════
        // STATE TRACKING
        // ═══════════════════════════════════════════════════════════

        CompoundTag state = getOrCreateNegotiationState(npc, playerUUID);

        // Prüfe ob Verhandlung noch aktiv ist
        if (!isNegotiationActive(state)) {
            // Reset - neue Verhandlung starten
            clearNegotiationState(npc, playerUUID);
            state = getOrCreateNegotiationState(npc, playerUUID);
        }

        int round = state.getInt("round") + 1;
        double lastNPCOffer = state.getDouble("lastNPCOffer");

        // Engine erstellen und Response berechnen
        NegotiationEngine engine = new NegotiationEngine(type, quality, weight, metrics, playerUUID);
        NPCResponse response = engine.calculateResponse(offeredPrice, round, lastNPCOffer);

        // State aktualisieren
        if (response.isAccepted()) {
            // Deal abgeschlossen - State löschen
            clearNegotiationState(npc, playerUUID);
        } else {
            // State für nächste Runde speichern
            updateNegotiationState(npc, playerUUID, round, response.getCounterOffer(), offeredPrice);
        }

        return response;
    }
}
