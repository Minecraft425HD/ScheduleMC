package de.rolandsw.schedulemc.level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Spieler-spezifische Level-Daten.
 *
 * Speichert pro Spieler:
 * - Aktuelles Level
 * - Aktuelle XP
 * - Freigeschaltete Inhalte
 * - Statistiken (verkaufte Mengen pro Kategorie)
 */
public class ProducerLevelData {

    private final UUID playerUUID;
    private int level;
    private int totalXP;
    private final Set<String> unlockedItems;

    // Statistiken
    private int totalItemsSold;
    private int totalIllegalSold;
    private int totalLegalSold;
    private double totalRevenue;

    /**
     * Erstellt neue Level-Daten für einen Spieler.
     */
    public ProducerLevelData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.level = 0;
        this.totalXP = 0;
        this.unlockedItems = new HashSet<>();
        this.totalItemsSold = 0;
        this.totalIllegalSold = 0;
        this.totalLegalSold = 0;
        this.totalRevenue = 0.0;
    }

    /**
     * Erstellt Level-Daten aus gespeicherten Werten (Deserialisierung).
     */
    public ProducerLevelData(UUID playerUUID, int level, int totalXP,
                              Set<String> unlockedItems, int totalItemsSold,
                              int totalIllegalSold, int totalLegalSold, double totalRevenue) {
        this.playerUUID = playerUUID;
        this.level = level;
        this.totalXP = totalXP;
        this.unlockedItems = new HashSet<>(unlockedItems);
        this.totalItemsSold = totalItemsSold;
        this.totalIllegalSold = totalIllegalSold;
        this.totalLegalSold = totalLegalSold;
        this.totalRevenue = totalRevenue;
    }

    // ═══════════════════════════════════════════════════════════
    // XP & LEVEL MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt XP hinzu und prüft auf Level-Up.
     *
     * @param xp Hinzuzufügende XP
     * @return true wenn ein Level-Up stattfand
     */
    public boolean addXP(int xp) {
        if (xp <= 0) return false;
        if (level >= LevelRequirements.MAX_LEVEL) return false;

        totalXP += xp;
        int newLevel = LevelRequirements.getLevelForXP(totalXP);

        if (newLevel > level) {
            int oldLevel = level;
            level = newLevel;

            // Neue Unlockables freischalten
            updateUnlocks();

            return true; // Level-Up!
        }

        return false;
    }

    /**
     * Aktualisiert die freigeschalteten Inhalte basierend auf dem Level.
     */
    private void updateUnlocks() {
        for (Unlockable unlock : Unlockable.values()) {
            if (unlock.isUnlockedAt(level)) {
                unlockedItems.add(unlock.name());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UNLOCK CHECKS
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob ein bestimmter Inhalt freigeschaltet ist.
     */
    public boolean isUnlocked(Unlockable unlockable) {
        return unlockable.isUnlockedAt(level);
    }

    /**
     * Prüft ob ein Inhalt per Name freigeschaltet ist.
     */
    public boolean isUnlocked(String unlockableName) {
        try {
            Unlockable unlock = Unlockable.valueOf(unlockableName);
            return isUnlocked(unlock);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIKEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Verkauf in den Statistiken.
     */
    public void recordSale(int amount, boolean illegal, double revenue) {
        totalItemsSold += amount;
        if (illegal) {
            totalIllegalSold += amount;
        } else {
            totalLegalSold += amount;
        }
        totalRevenue += revenue;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getLevel() {
        return level;
    }

    public int getTotalXP() {
        return totalXP;
    }

    public Set<String> getUnlockedItems() {
        return new HashSet<>(unlockedItems);
    }

    public int getTotalItemsSold() {
        return totalItemsSold;
    }

    public int getTotalIllegalSold() {
        return totalIllegalSold;
    }

    public int getTotalLegalSold() {
        return totalLegalSold;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getProgress() {
        return LevelRequirements.getProgress(level, totalXP);
    }

    public int getXPToNextLevel() {
        return LevelRequirements.getXPToNextLevel(level, totalXP);
    }

    /**
     * Gibt eine formatierte Level-Info für den Spieler zurück.
     */
    public String getFormattedInfo() {
        return String.format(
                "§6═══ Produzenten-Level ═══\n" +
                "%s\n" +
                "§7Verkauft: §f%d Items §7(§c%d illegal§7, §a%d legal§7)\n" +
                "§7Umsatz: §f%.0f€\n" +
                "§7Freigeschaltet: §f%d / %d",
                LevelRequirements.getLevelOverview(level, totalXP),
                totalItemsSold, totalIllegalSold, totalLegalSold,
                totalRevenue,
                unlockedItems.size(), Unlockable.values().length
        );
    }
}
