package de.rolandsw.schedulemc.data;

import java.util.UUID;

/**
 * Speichert tägliche Belohnungs-Daten eines Spielers
 */
public class DailyReward {
    
    private final String playerUUID;
    private long lastClaimTime;
    private int currentStreak;
    private int longestStreak;
    private int totalClaims;
    
    public DailyReward(String playerUUID) {
        this.playerUUID = playerUUID;
        this.lastClaimTime = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.totalClaims = 0;
    }
    
    // Getter
    public String getPlayerUUID() { return playerUUID; }
    public long getLastClaimTime() { return lastClaimTime; }
    public int getCurrentStreak() { return currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public int getTotalClaims() { return totalClaims; }
    
    // Setter
    public void setLastClaimTime(long time) { this.lastClaimTime = time; }
    public void setCurrentStreak(int streak) { this.currentStreak = streak; }
    public void setLongestStreak(int streak) { this.longestStreak = streak; }
    public void setTotalClaims(int claims) { this.totalClaims = claims; }
    
    /**
     * Prüft ob heute bereits geclaimt wurde
     */
    public boolean hasClaimedToday() {
        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        return (now - lastClaimTime) < dayInMillis;
    }
    
    /**
     * Prüft ob Streak noch gültig ist (innerhalb 48h)
     */
    public boolean isStreakValid() {
        if (lastClaimTime == 0) return false;
        long now = System.currentTimeMillis();
        long maxGap = 48 * 60 * 60 * 1000; // 48 Stunden
        return (now - lastClaimTime) < maxGap;
    }
    
    /**
     * Claimen der täglichen Belohnung
     */
    public void claim() {
        long now = System.currentTimeMillis();
        
        // Streak aktualisieren
        if (isStreakValid()) {
            currentStreak++;
        } else {
            currentStreak = 1;
        }
        
        // Längsten Streak aktualisieren
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }
        
        lastClaimTime = now;
        totalClaims++;
    }
    
    /**
     * Gibt verbleibende Zeit bis zum nächsten Claim zurück (in Sekunden)
     */
    public long getTimeUntilNextClaim() {
        if (!hasClaimedToday()) return 0;
        
        long dayInMillis = 24 * 60 * 60 * 1000;
        long timeSinceClaim = System.currentTimeMillis() - lastClaimTime;
        long remaining = dayInMillis - timeSinceClaim;
        
        return Math.max(0, remaining / 1000);
    }
    
    /**
     * Formatiert Zeit bis zum nächsten Claim (HH:MM:SS)
     */
    public String getFormattedTimeUntilNext() {
        long seconds = getTimeUntilNextClaim();
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
