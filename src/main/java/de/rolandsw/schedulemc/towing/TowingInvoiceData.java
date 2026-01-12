package de.rolandsw.schedulemc.towing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Data class for a towing invoice
 */
public class TowingInvoiceData {
    private final UUID invoiceId;
    private final UUID playerId;
    private final UUID vehicleId;
    private final String towingYardPlotId;
    private final double amount;
    private final long timestamp;
    private boolean paid;

    public TowingInvoiceData(UUID playerId, UUID vehicleId, String towingYardPlotId, double amount, long timestamp) {
        this.invoiceId = UUID.randomUUID();
        this.playerId = playerId;
        this.vehicleId = vehicleId;
        this.towingYardPlotId = towingYardPlotId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.paid = false;
    }

    private TowingInvoiceData(UUID invoiceId, UUID playerId, UUID vehicleId, String towingYardPlotId,
                               double amount, long timestamp, boolean paid) {
        this.invoiceId = invoiceId;
        this.playerId = playerId;
        this.vehicleId = vehicleId;
        this.towingYardPlotId = towingYardPlotId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.paid = paid;
    }

    public UUID getInvoiceId() {
        return invoiceId;
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

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPaid() {
        return paid;
    }

    public void markAsPaid() {
        this.paid = true;
    }

    // === NBT SERIALIZATION ===

    public CompoundTag save(CompoundTag tag) {
        tag.putUUID("InvoiceId", invoiceId);
        tag.putUUID("PlayerId", playerId);
        tag.putUUID("VehicleId", vehicleId);
        tag.putString("TowingYardPlotId", towingYardPlotId);
        tag.putDouble("Amount", amount);
        tag.putLong("Timestamp", timestamp);
        tag.putBoolean("Paid", paid);
        return tag;
    }

    public static TowingInvoiceData load(CompoundTag tag) {
        return new TowingInvoiceData(
            tag.getUUID("InvoiceId"),
            tag.getUUID("PlayerId"),
            tag.getUUID("VehicleId"),
            tag.getString("TowingYardPlotId"),
            tag.getDouble("Amount"),
            tag.getLong("Timestamp"),
            tag.getBoolean("Paid")
        );
    }

    // === NETWORK SERIALIZATION ===

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(invoiceId);
        buf.writeUUID(playerId);
        buf.writeUUID(vehicleId);
        buf.writeUtf(towingYardPlotId);
        buf.writeDouble(amount);
        buf.writeLong(timestamp);
        buf.writeBoolean(paid);
    }

    public static TowingInvoiceData decode(FriendlyByteBuf buf) {
        return new TowingInvoiceData(
            buf.readUUID(),
            buf.readUUID(),
            buf.readUUID(),
            buf.readUtf(256),
            buf.readDouble(),
            buf.readLong(),
            buf.readBoolean()
        );
    }

    @Override
    public String toString() {
        return String.format("TowingInvoice{id=%s, player=%s, vehicle=%s, amount=%.2f€, paid=%s}",
            invoiceId, playerId, vehicleId, amount, paid);
    }
}
