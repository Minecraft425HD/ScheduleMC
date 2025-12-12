package de.rolandsw.schedulemc.economy;

import java.util.UUID;

/**
 * Aktionär - Spieler der Aktien an einem Shop besitzt
 */
public class ShareHolder {
    private final UUID playerUUID;
    private final String playerName;
    private int sharesOwned;           // Anzahl Aktien (1-99)
    private int purchasePrice;         // Was der Spieler insgesamt bezahlt hat
    private final long purchaseDate;   // Wann erstmals gekauft

    public ShareHolder(UUID playerUUID, String playerName, int shares, int totalCost) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.sharesOwned = shares;
        this.purchasePrice = totalCost;
        this.purchaseDate = System.currentTimeMillis();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getSharesOwned() {
        return sharesOwned;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public long getPurchaseDate() {
        return purchaseDate;
    }

    /**
     * Fügt mehr Aktien hinzu (Nachkauf)
     */
    public void addShares(int amount, int cost) {
        this.sharesOwned += amount;
        this.purchasePrice += cost;
    }

    /**
     * Entfernt Aktien (Verkauf)
     */
    public void removeShares(int amount) {
        this.sharesOwned -= amount;
        if (this.sharesOwned < 0) {
            this.sharesOwned = 0;
        }
    }

    /**
     * Berechnet Auszahlung basierend auf 7-Tage-Nettoumsatz
     */
    public int calculatePayout(int totalNetRevenue) {
        // Anteil = (Shares / 100) × Nettoumsatz
        return (int)((sharesOwned / 100.0) * totalNetRevenue);
    }

    /**
     * Besitz-Prozentsatz
     */
    public float getOwnershipPercentage() {
        return (sharesOwned / 100.0f) * 100.0f;
    }

    @Override
    public String toString() {
        return String.format("%s: %d Aktien (%.1f%%), bezahlt: %d€",
            playerName, sharesOwned, getOwnershipPercentage(), purchasePrice);
    }
}
