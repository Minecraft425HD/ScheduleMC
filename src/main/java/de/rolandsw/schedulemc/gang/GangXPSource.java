package de.rolandsw.schedulemc.gang;

/**
 * XP-Quellen fuer das Gang-Level-System.
 *
 * Gang-XP wird kollektiv durch Aktionen aller Mitglieder gesammelt.
 */
public enum GangXPSource {

    // Mitglieder-Verkaeufe
    MEMBER_SALE_LEGAL("Legaler Verkauf", 3),
    MEMBER_SALE_ILLEGAL("Illegaler Verkauf", 6),

    // Territory
    TERRITORY_HELD("Territory gehalten (pro Tag/Chunk)", 2),
    TERRITORY_CAPTURED("Neues Territory beansprucht", 15),

    // Missionen
    MISSION_COMPLETED("Gang-Mission abgeschlossen", 50),
    MISSION_BONUS("Bonus-Mission", 100),

    // Mitglieder
    MEMBER_LEVEL_UP("Mitglied Level-Up", 20),
    MEMBER_JOINED("Neues Mitglied", 10),

    // Crime
    SURVIVE_POLICE_RAID("Razzia ueberlebt (Mitglied)", 30),
    SUCCESSFUL_BRIBE("Erfolgreiche Bestechung", 8),

    // Sonstiges
    DAILY_ACTIVITY("Taegliche Aktivitaet", 5);

    private final String displayName;
    private final int baseXP;

    GangXPSource(String displayName, int baseXP) {
        this.displayName = displayName;
        this.baseXP = baseXP;
    }

    public String getDisplayName() { return displayName; }
    public int getBaseXP() { return baseXP; }

    /**
     * Berechnet die XP mit optionalem Multiplikator.
     */
    public int calculateXP(int amount, double multiplier) {
        return (int) (baseXP * amount * multiplier);
    }

    public int calculateXP(int amount) {
        return calculateXP(amount, 1.0);
    }
}
