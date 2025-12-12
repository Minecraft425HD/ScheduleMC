package de.rolandsw.schedulemc.economy;

/**
 * Speichert Umsatz-Daten für einen Tag
 * Wird für 7-Tage-Tracking verwendet
 */
public class DailyRevenueRecord {
    private final long dayNumber;      // Minecraft-Tag
    private int revenue;               // Einnahmen
    private int expenses;              // Ausgaben

    public DailyRevenueRecord(long dayNumber, int revenue, int expenses) {
        this.dayNumber = dayNumber;
        this.revenue = revenue;
        this.expenses = expenses;
    }

    public long getDayNumber() {
        return dayNumber;
    }

    public int getRevenue() {
        return revenue;
    }

    public int getExpenses() {
        return expenses;
    }

    /**
     * Nettoumsatz = Einnahmen - Ausgaben
     */
    public int getNetRevenue() {
        return Math.max(0, revenue - expenses);
    }

    public void addRevenue(int amount) {
        this.revenue += amount;
    }

    public void addExpense(int amount) {
        this.expenses += amount;
    }

    @Override
    public String toString() {
        return String.format("Tag %d: +%d€ / -%d€ = %d€ netto",
            dayNumber, revenue, expenses, getNetRevenue());
    }
}
