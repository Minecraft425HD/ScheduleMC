package de.rolandsw.schedulemc.tobacco.business;

/**
 * NPC-Antwort bei Verhandlung
 */
public class NPCResponse {
    private final boolean accepted;
    private final double counterOffer;
    private final String message;
    private final int reputationChange;

    public NPCResponse(boolean accepted, double counterOffer, String message, int reputationChange) {
        this.accepted = accepted;
        this.counterOffer = counterOffer;
        this.message = message;
        this.reputationChange = reputationChange;
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
}
