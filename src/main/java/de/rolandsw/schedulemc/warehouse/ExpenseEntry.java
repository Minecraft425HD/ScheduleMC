package de.rolandsw.schedulemc.warehouse;

import net.minecraft.nbt.CompoundTag;

/**
 * Einzelner Ausgaben-Eintrag für Warehouse Expense Tracking
 */
public class ExpenseEntry {
    private final long timestamp;  // Game time in ticks
    private final int amount;       // Kosten in €
    private final String description; // z.B. "Auto-Delivery"

    public ExpenseEntry(long timestamp, int amount, String description) {
        this.timestamp = timestamp;
        this.amount = amount;
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
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
        tag.putInt("Amount", amount);
        tag.putString("Description", description);
        return tag;
    }

    public static ExpenseEntry load(CompoundTag tag) {
        return new ExpenseEntry(
            tag.getLong("Timestamp"),
            tag.getInt("Amount"),
            tag.getString("Description")
        );
    }

    @Override
    public String toString() {
        return String.format("ExpenseEntry{timestamp=%d, amount=%d€, desc='%s'}",
            timestamp, amount, description);
    }
}
