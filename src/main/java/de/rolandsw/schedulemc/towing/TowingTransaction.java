package de.rolandsw.schedulemc.towing;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Einzelner Abschleppvorgang mit Einnahmen-Tracking
 */
public class TowingTransaction {
    private final long timestamp;  // Game time in ticks
    private final UUID playerId;
    private final UUID vehicleId;
    private final String towingYardPlotId;
    private final double totalCost;
    private final double playerPaid;
    private final double yardRevenue;
    private final MembershipTier membershipTier;

    public TowingTransaction(long timestamp, UUID playerId, UUID vehicleId, String towingYardPlotId,
                              double totalCost, double playerPaid, double yardRevenue, MembershipTier membershipTier) {
        this.timestamp = timestamp;
        this.playerId = playerId;
        this.vehicleId = vehicleId;
        this.towingYardPlotId = towingYardPlotId;
        this.totalCost = totalCost;
        this.playerPaid = playerPaid;
        this.yardRevenue = yardRevenue;
        this.membershipTier = membershipTier;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public String getTowingYardPlotId() {
        return towingYardPlotId;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getPlayerPaid() {
        return playerPaid;
    }

    public double getYardRevenue() {
        return yardRevenue;
    }

    public MembershipTier getMembershipTier() {
        return membershipTier;
    }

    /**
     * Prüft ob dieser Eintrag älter als X Tage ist
     */
    public boolean isOlderThan(long currentTime, int days) {
        long ageTicks = currentTime - timestamp;
        long daysTicks = days * 24000L; // 1 Tag = 24000 ticks
        return ageTicks > daysTicks;
    }

    /**
     * Gibt das Alter in Tagen zurück
     */
    public int getAgeDays(long currentTime) {
        long ageTicks = currentTime - timestamp;
        return (int) (ageTicks / 24000L);
    }

    // === NBT SERIALISIERUNG ===

    public CompoundTag save(CompoundTag tag) {
        tag.putLong("Timestamp", timestamp);
        tag.putUUID("PlayerId", playerId);
        tag.putUUID("VehicleId", vehicleId);
        tag.putString("TowingYardPlotId", towingYardPlotId);
        tag.putDouble("TotalCost", totalCost);
        tag.putDouble("PlayerPaid", playerPaid);
        tag.putDouble("YardRevenue", yardRevenue);
        tag.putInt("MembershipTier", membershipTier.ordinal());
        return tag;
    }

    public static TowingTransaction load(CompoundTag tag) {
        return new TowingTransaction(
            tag.getLong("Timestamp"),
            tag.getUUID("PlayerId"),
            tag.getUUID("VehicleId"),
            tag.getString("TowingYardPlotId"),
            tag.getDouble("TotalCost"),
            tag.getDouble("PlayerPaid"),
            tag.getDouble("YardRevenue"),
            MembershipTier.fromOrdinal(tag.getInt("MembershipTier"))
        );
    }

    @Override
    public String toString() {
        return String.format("TowingTransaction{timestamp=%d, player=%s, yard=%s, cost=%.2f€, revenue=%.2f€, tier=%s}",
            timestamp, playerId, towingYardPlotId, totalCost, yardRevenue, membershipTier);
    }
}
