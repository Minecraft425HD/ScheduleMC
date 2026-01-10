package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.npc.data.NPCPersonality;
import de.rolandsw.schedulemc.npc.personality.NPCPersonalityTrait;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Verhandlungs-State - Verwaltet den Zustand einer laufenden Verhandlung
 *
 * Features:
 * - Rundenbasiertes System (max. Runden basierend auf Persönlichkeit)
 * - Heatmap: Misst die "Hitze" der Verhandlung (0-100)
 * - Abbruch-Risiko: Berechnet Wahrscheinlichkeit für NPC-Abbruch
 * - Preis-Historie: Speichert alle Angebote
 */
public class NegotiationState {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS (aus Config geladen in Produktion)
    // ═══════════════════════════════════════════════════════════

    // Basis-Runden pro Persönlichkeit
    public static final int BASE_ROUNDS_SUSPICIOUS = 4;
    public static final int BASE_ROUNDS_GREEDY = 5;
    public static final int BASE_ROUNDS_NEUTRAL = 6;
    public static final int BASE_ROUNDS_FRIENDLY = 7;
    public static final int BASE_ROUNDS_GENEROUS = 8;

    // Heat-Anstieg pro Zone
    public static final float HEAT_RED_ZONE = 25.0f;    // Rote Zone: +25% Heat
    public static final float HEAT_YELLOW_ZONE = 10.0f; // Gelbe Zone: +10% Heat
    public static final float HEAT_GREEN_ZONE = -5.0f;  // Grüne Zone: -5% Heat (Cooling)

    // Heat-Multiplikatoren pro Persönlichkeit
    public static final float HEAT_MULT_SUSPICIOUS = 1.5f;
    public static final float HEAT_MULT_GREEDY = 1.3f;
    public static final float HEAT_MULT_NEUTRAL = 1.0f;
    public static final float HEAT_MULT_FRIENDLY = 0.8f;
    public static final float HEAT_MULT_GENEROUS = 0.6f;

    // Abbruch-Schwellenwert
    public static final float HEAT_ABORT_THRESHOLD = 100.0f;

    // ═══════════════════════════════════════════════════════════
    // STATE DATA
    // ═══════════════════════════════════════════════════════════

    private int currentRound;
    private int maxRounds;
    private float heat;                    // 0-100+
    private float abortRisk;               // 0-100%
    private boolean aborted;               // NPC hat abgebrochen
    private boolean dealMade;              // Deal abgeschlossen

    private final NPCPersonality personality;
    private final NPCPersonalityTrait trait;

    private final List<PriceOffer> priceHistory;
    private double lastCounterOffer;
    private String lastNPCResponse;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public NegotiationState(NPCPersonality personality, NPCPersonalityTrait trait) {
        this.personality = personality;
        this.trait = trait;
        this.currentRound = 0;
        this.maxRounds = calculateMaxRounds(trait);
        this.heat = 0.0f;
        this.abortRisk = 0.0f;
        this.aborted = false;
        this.dealMade = false;
        this.priceHistory = new ArrayList<>();
        this.lastCounterOffer = 0.0;
        this.lastNPCResponse = "";
    }

    // ═══════════════════════════════════════════════════════════
    // ROUND MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet max. Runden basierend auf Trait
     */
    private int calculateMaxRounds(NPCPersonalityTrait trait) {
        return switch (trait) {
            case SUSPICIOUS -> BASE_ROUNDS_SUSPICIOUS;
            case GREEDY -> BASE_ROUNDS_GREEDY;
            case NEUTRAL -> BASE_ROUNDS_NEUTRAL;
            case FRIENDLY -> BASE_ROUNDS_FRIENDLY;
            case GENEROUS -> BASE_ROUNDS_GENEROUS;
        };
    }

    /**
     * Startet eine neue Runde mit einem Spieler-Angebot
     */
    public void nextRound(double playerOffer, PriceZone zone) {
        currentRound++;
        priceHistory.add(new PriceOffer(playerOffer, zone, currentRound));

        // Heat basierend auf Zone aktualisieren
        float heatChange = switch (zone) {
            case RED -> HEAT_RED_ZONE;
            case YELLOW -> HEAT_YELLOW_ZONE;
            case GREEN -> HEAT_GREEN_ZONE;
        };

        // Persönlichkeits-Multiplikator anwenden
        float heatMultiplier = getHeatMultiplier();
        heat = Math.max(0, heat + (heatChange * heatMultiplier));

        // Rundenbasierter Heat-Anstieg (jede Runde erhöht leicht die Spannung)
        heat += currentRound * 2.0f;

        // Abbruch-Risiko berechnen
        calculateAbortRisk();

        // Prüfe ob NPC abbricht
        if (heat >= HEAT_ABORT_THRESHOLD) {
            aborted = true;
        }
    }

    /**
     * Holt den Heat-Multiplikator für den Trait
     */
    private float getHeatMultiplier() {
        return switch (trait) {
            case SUSPICIOUS -> HEAT_MULT_SUSPICIOUS;
            case GREEDY -> HEAT_MULT_GREEDY;
            case NEUTRAL -> HEAT_MULT_NEUTRAL;
            case FRIENDLY -> HEAT_MULT_FRIENDLY;
            case GENEROUS -> HEAT_MULT_GENEROUS;
        };
    }

    /**
     * Berechnet das aktuelle Abbruch-Risiko
     */
    private void calculateAbortRisk() {
        // Basis: Heat direkt als Risiko
        float baseRisk = heat;

        // Runden-Faktor: Je mehr Runden, desto höher das Risiko
        float roundFactor = (float) currentRound / maxRounds * 20.0f;

        // Persönlichkeits-Faktor
        float traitFactor = switch (trait) {
            case SUSPICIOUS -> 1.4f;
            case GREEDY -> 1.2f;
            case NEUTRAL -> 1.0f;
            case FRIENDLY -> 0.8f;
            case GENEROUS -> 0.6f;
        };

        abortRisk = Math.min(100.0f, (baseRisk + roundFactor) * traitFactor);
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE ZONE CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Bestimmt die Preis-Zone basierend auf Angebot und NPC-Zielpreis
     */
    public static PriceZone calculatePriceZone(double playerOffer, double npcTargetPrice, double fairPrice) {
        // Grüne Zone: 80-120% des NPC-Zielpreises
        double greenLow = npcTargetPrice * 0.80;
        double greenHigh = npcTargetPrice * 1.20;

        // Gelbe Zone: 60-80% oder 120-150%
        double yellowLow = npcTargetPrice * 0.60;
        double yellowHigh = npcTargetPrice * 1.50;

        if (playerOffer >= greenLow && playerOffer <= greenHigh) {
            return PriceZone.GREEN;
        } else if (playerOffer >= yellowLow && playerOffer <= yellowHigh) {
            return PriceZone.YELLOW;
        } else {
            return PriceZone.RED;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEAL METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Markiert den Deal als abgeschlossen
     */
    public void completeDeal() {
        this.dealMade = true;
    }

    /**
     * Setzt das NPC Gegenangebot und die Antwort
     */
    public void setNPCResponse(double counterOffer, String response) {
        this.lastCounterOffer = counterOffer;
        this.lastNPCResponse = response;
    }

    // ═══════════════════════════════════════════════════════════
    // STATE CHECKS
    // ═══════════════════════════════════════════════════════════

    public boolean canContinue() {
        return !aborted && !dealMade && currentRound < maxRounds;
    }

    public boolean isLastRound() {
        return currentRound >= maxRounds - 1;
    }

    public boolean hasEnded() {
        return aborted || dealMade || currentRound >= maxRounds;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public int getCurrentRound() {
        return currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public float getHeat() {
        return heat;
    }

    public float getAbortRisk() {
        return abortRisk;
    }

    public boolean isAborted() {
        return aborted;
    }

    public boolean isDealMade() {
        return dealMade;
    }

    public NPCPersonality getPersonality() {
        return personality;
    }

    public NPCPersonalityTrait getTrait() {
        return trait;
    }

    public List<PriceOffer> getPriceHistory() {
        return priceHistory;
    }

    public double getLastCounterOffer() {
        return lastCounterOffer;
    }

    public String getLastNPCResponse() {
        return lastNPCResponse;
    }

    /**
     * Berechnet die Stimmung als Prozentwert (100% = entspannt, 0% = kurz vor Abbruch)
     */
    public float getMoodPercent() {
        return Math.max(0, 100.0f - heat);
    }

    // ═══════════════════════════════════════════════════════════
    // NETWORK SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(currentRound);
        buf.writeInt(maxRounds);
        buf.writeFloat(heat);
        buf.writeFloat(abortRisk);
        buf.writeBoolean(aborted);
        buf.writeBoolean(dealMade);
        buf.writeEnum(personality);
        buf.writeEnum(trait);
        buf.writeDouble(lastCounterOffer);
        buf.writeUtf(lastNPCResponse);

        // Price History
        buf.writeInt(priceHistory.size());
        for (PriceOffer offer : priceHistory) {
            buf.writeDouble(offer.price);
            buf.writeEnum(offer.zone);
            buf.writeInt(offer.round);
        }
    }

    public static NegotiationState decode(FriendlyByteBuf buf) {
        int currentRound = buf.readInt();
        int maxRounds = buf.readInt();
        float heat = buf.readFloat();
        float abortRisk = buf.readFloat();
        boolean aborted = buf.readBoolean();
        boolean dealMade = buf.readBoolean();
        NPCPersonality personality = buf.readEnum(NPCPersonality.class);
        NPCPersonalityTrait trait = buf.readEnum(NPCPersonalityTrait.class);
        double lastCounterOffer = buf.readDouble();
        String lastNPCResponse = buf.readUtf();

        NegotiationState state = new NegotiationState(personality, trait);
        state.currentRound = currentRound;
        state.maxRounds = maxRounds;
        state.heat = heat;
        state.abortRisk = abortRisk;
        state.aborted = aborted;
        state.dealMade = dealMade;
        state.lastCounterOffer = lastCounterOffer;
        state.lastNPCResponse = lastNPCResponse;

        // Price History
        int historySize = buf.readInt();
        for (int i = 0; i < historySize; i++) {
            double price = buf.readDouble();
            PriceZone zone = buf.readEnum(PriceZone.class);
            int round = buf.readInt();
            state.priceHistory.add(new PriceOffer(price, zone, round));
        }

        return state;
    }

    // ═══════════════════════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════════════════════

    /**
     * Preis-Zonen für die Verhandlung
     */
    public enum PriceZone {
        GREEN("gui.negotiation.zone.green", 0xFF55FF55),   // Akzeptabler Bereich
        YELLOW("gui.negotiation.zone.yellow", 0xFFFFFF55), // Verhandelbar
        RED("gui.negotiation.zone.red", 0xFFFF5555);       // Inakzeptabel

        private final String translationKey;
        private final int color;

        PriceZone(String translationKey, int color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public int getColor() {
            return color;
        }
    }

    /**
     * Repräsentiert ein Preis-Angebot in der Historie
     */
    public record PriceOffer(double price, PriceZone zone, int round) {
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("NegotiationState[round=%d/%d, heat=%.1f%%, risk=%.1f%%, aborted=%s, deal=%s]",
            currentRound, maxRounds, heat, abortRisk, aborted, dealMade);
    }
}
