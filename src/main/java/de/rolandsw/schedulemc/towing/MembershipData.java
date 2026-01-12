package de.rolandsw.schedulemc.towing;

import java.util.UUID;

/**
 * Player membership data for towing service
 */
public class MembershipData {
    private final UUID playerId;
    private MembershipTier tier;
    private long subscriptionStartDate;
    private long nextPaymentDate;
    private boolean active;
    private int towsThisPeriod;

    public MembershipData(UUID playerId, MembershipTier tier) {
        this.playerId = playerId;
        this.tier = tier;
        this.subscriptionStartDate = System.currentTimeMillis();
        this.nextPaymentDate = calculateNextPaymentDate();
        this.active = tier != MembershipTier.NONE;
        this.towsThisPeriod = 0;
    }

    private long calculateNextPaymentDate() {
        if (tier == MembershipTier.NONE) {
            return 0;
        }
        // Convert Minecraft days to milliseconds
        // 1 Minecraft day = 20 minutes = 1200 seconds = 1200000 ms
        long intervalMs = de.rolandsw.schedulemc.config.ModConfigHandler.SERVER.membershipPaymentIntervalDays.get() * 1200000L;
        return subscriptionStartDate + intervalMs;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public MembershipTier getTier() {
        return tier;
    }

    public void setTier(MembershipTier tier) {
        this.tier = tier;
        this.subscriptionStartDate = System.currentTimeMillis();
        this.nextPaymentDate = calculateNextPaymentDate();
        this.active = tier != MembershipTier.NONE;
        this.towsThisPeriod = 0;
    }

    public long getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    public long getNextPaymentDate() {
        return nextPaymentDate;
    }

    public void setNextPaymentDate(long nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTowsThisPeriod() {
        return towsThisPeriod;
    }

    public void incrementTows() {
        this.towsThisPeriod++;
    }

    public void resetTows() {
        this.towsThisPeriod = 0;
    }

    /**
     * Renews the membership for another payment period
     */
    public void renew() {
        this.subscriptionStartDate = System.currentTimeMillis();
        this.nextPaymentDate = calculateNextPaymentDate();
        this.towsThisPeriod = 0;
    }

    /**
     * Cancels the membership
     */
    public void cancel() {
        this.tier = MembershipTier.NONE;
        this.active = false;
        this.nextPaymentDate = 0;
    }

    /**
     * Gets days until next payment
     */
    public int getDaysUntilPayment() {
        if (!active || tier == MembershipTier.NONE) {
            return 0;
        }
        long timeLeft = nextPaymentDate - System.currentTimeMillis();
        if (timeLeft <= 0) {
            return 0;
        }
        // Convert ms to Minecraft days
        return (int) (timeLeft / 1200000L);
    }
}
