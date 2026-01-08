package de.rolandsw.schedulemc.economy;
nimport de.rolandsw.schedulemc.util.StringUtils;

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

    /**
     * Private constructor for Builder
     */
    private Transaction(Builder builder) {
        this.transactionId = builder.transactionId != null ? builder.transactionId : UUID.randomUUID().toString();
        this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
        this.type = builder.type;
        this.fromPlayer = builder.fromPlayer;
        this.toPlayer = builder.toPlayer;
        this.amount = builder.amount;
        this.description = builder.description;
        this.balanceAfter = builder.balanceAfter;
    }

    /**
     * Creates a new Builder for constructing Transaction objects
     * @param type The transaction type (required)
     * @return A new Builder instance
     */
    public static Builder builder(TransactionType type) {
        return new Builder(type);
    }

    /**
     * Builder for Transaction - provides fluent API for complex object construction
     */
    public static class Builder {
        private final TransactionType type;
        private String transactionId;
        private long timestamp;
        private UUID fromPlayer;
        private UUID toPlayer;
        private double amount;
        private String description;
        private double balanceAfter;

        private Builder(TransactionType type) {
            this.type = type;
        }

        public Builder id(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder from(@Nullable UUID fromPlayer) {
            this.fromPlayer = fromPlayer;
            return this;
        }

        public Builder to(@Nullable UUID toPlayer) {
            this.toPlayer = toPlayer;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        public Builder balanceAfter(double balanceAfter) {
            this.balanceAfter = balanceAfter;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
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

        sb.append(StringUtils.formatMoney(Math.abs(amount)));
        sb.append(" §7- §e").append(type.getDisplayName());

        if (description != null && !description.isEmpty()) {
            sb.append(" §7(").append(description).append(")");
        }

        sb.append("\n  §7Neuer Kontostand: §6").append(StringUtils.formatMoney(balanceAfter));

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Transaction[%s, %s, %.2f€, %s]",
            transactionId, type, amount, getFormattedDate());
    }
}
