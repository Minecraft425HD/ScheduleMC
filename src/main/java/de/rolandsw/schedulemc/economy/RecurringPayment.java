package de.rolandsw.schedulemc.economy;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Dauerauftrag - Automatische wiederkehrende Zahlung
 */
public class RecurringPayment {
    @SerializedName("id")
    private final String paymentId;

    @SerializedName("fromPlayer")
    private final UUID fromPlayer;

    @SerializedName("toPlayer")
    private final UUID toPlayer;

    @SerializedName("amount")
    private final double amount;

    @SerializedName("intervalDays")
    private final int intervalDays;

    @SerializedName("description")
    private final String description;

    @SerializedName("nextExecutionDay")
    private long nextExecutionDay;

    @SerializedName("active")
    private boolean active;

    @SerializedName("failureCount")
    private int failureCount;

    public RecurringPayment(UUID fromPlayer, UUID toPlayer, double amount, int intervalDays,
                           String description, long currentDay) {
        this.paymentId = UUID.randomUUID().toString();
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.amount = amount;
        this.intervalDays = intervalDays;
        this.description = description;
        this.nextExecutionDay = currentDay + intervalDays;
        this.active = true;
        this.failureCount = 0;
    }

    public String getPaymentId() { return paymentId; }
    public UUID getFromPlayer() { return fromPlayer; }
    public UUID getToPlayer() { return toPlayer; }
    public double getAmount() { return amount; }
    public int getIntervalDays() { return intervalDays; }
    public String getDescription() { return description; }
    public long getNextExecutionDay() { return nextExecutionDay; }
    public boolean isActive() { return active; }
    public int getFailureCount() { return failureCount; }

    /**
     * Führt Zahlung aus
     */
    public boolean execute(long currentDay) {
        if (!active || currentDay < nextExecutionDay) {
            return false;
        }

        // Versuche Überweisung
        boolean success = EconomyManager.transfer(fromPlayer, toPlayer, amount,
            "Dauerauftrag: " + description);

        if (success) {
            // Nächste Ausführung planen
            nextExecutionDay = currentDay + intervalDays;
            failureCount = 0;
            return true;
        } else {
            // Fehlgeschlagen
            failureCount++;

            // Nach 3 Fehlversuchen: deaktivieren
            if (failureCount >= 3) {
                active = false;
            }

            // Versuche in 1 Tag nochmal
            nextExecutionDay = currentDay + 1;
            return false;
        }
    }

    /**
     * Pausiert Dauerauftrag
     */
    public void pause() {
        active = false;
    }

    /**
     * Aktiviert Dauerauftrag
     */
    public void resume(long currentDay) {
        active = true;
        failureCount = 0;
        nextExecutionDay = currentDay + intervalDays;
    }

    /**
     * Gibt Tage bis nächster Ausführung zurück
     */
    public int getDaysUntilNext(long currentDay) {
        if (!active) {
            return -1;
        }
        long diff = nextExecutionDay - currentDay;
        return (int) Math.max(0, diff);
    }
}
