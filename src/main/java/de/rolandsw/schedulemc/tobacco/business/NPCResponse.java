package de.rolandsw.schedulemc.tobacco.business;

/**
 * NPC-Antwort bei Verhandlung
 */
public class NPCResponse {
    private final boolean accepted;
    private final double counterOffer;
    private final String message;
    private final int reputationChange;
    private final float mood;       // NPC-Stimmung (0-100)
    private final int round;        // Aktuelle Runde

    // Legacy-Konstruktor (für Kompatibilität)
    public NPCResponse(boolean accepted, double counterOffer, String message, int reputationChange) {
        this(accepted, counterOffer, message, reputationChange, 100.0f, 0);
    }

    // Neuer Konstruktor mit Mood und Round
    public NPCResponse(boolean accepted, double counterOffer, String message, int reputationChange, float mood, int round) {
        this.accepted = accepted;
        this.counterOffer = counterOffer;
        this.message = message;
        this.reputationChange = reputationChange;
        this.mood = mood;
        this.round = round;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public double getCounterOffer() {
        return counterOffer;
    }

    public String getMessage() {
        return message;
    }

    public int getReputationChange() {
        return reputationChange;
    }

    public float getMood() {
        return mood;
    }

    public int getRound() {
        return round;
    }
}
