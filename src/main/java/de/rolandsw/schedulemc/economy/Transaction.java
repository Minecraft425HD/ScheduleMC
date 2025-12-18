package de.rolandsw.schedulemc.economy;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Repräsentiert eine einzelne Transaktion
 */
public class Transaction {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    @SerializedName("id")
    private final String transactionId;

    @SerializedName("timestamp")
    private final long timestamp;

    @SerializedName("type")
    private final TransactionType type;

    @SerializedName("from")
    @Nullable
    private final UUID fromPlayer;

    @SerializedName("to")
    @Nullable
    private final UUID toPlayer;

    @SerializedName("amount")
    private final double amount;

    @SerializedName("description")
    private final String description;

    @SerializedName("balanceAfter")
    private final double balanceAfter;

    public Transaction(TransactionType type, @Nullable UUID fromPlayer, @Nullable UUID toPlayer,
                      double amount, String description, double balanceAfter) {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.amount = amount;
        this.description = description;
        this.balanceAfter = balanceAfter;
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public long getTimestamp() { return timestamp; }
    public TransactionType getType() { return type; }
    @Nullable public UUID getFromPlayer() { return fromPlayer; }
    @Nullable public UUID getToPlayer() { return toPlayer; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public double getBalanceAfter() { return balanceAfter; }

    /**
     * Formatiert Timestamp als lesbares Datum
     */
    public String getFormattedDate() {
        return FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Gibt formatierte Beschreibung zurück
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("§7[").append(getFormattedDate()).append("] ");

        // Vorzeichen basierend auf Transaktion
        if (amount > 0) {
            sb.append("§a+");
        } else {
            sb.append("§c-");
        }

        sb.append(String.format("%.2f€", Math.abs(amount)));
        sb.append(" §7- §e").append(type.getDisplayName());

        if (description != null && !description.isEmpty()) {
            sb.append(" §7(").append(description).append(")");
        }

        sb.append("\n  §7Neuer Kontostand: §6").append(String.format("%.2f€", balanceAfter));

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Transaction[%s, %s, %.2f€, %s]",
            transactionId, type, amount, getFormattedDate());
    }
}
