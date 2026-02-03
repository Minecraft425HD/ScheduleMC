package de.rolandsw.schedulemc.gang;

/**
 * Raenge innerhalb einer Gang.
 *
 * Jeder Rang hat bestimmte Berechtigungen:
 * - BOSS: Alles (Gang loeschen, Raenge vergeben, Perks, Territory)
 * - UNDERBOSS: Invite, Kick Recruits/Members, Territory claimen
 * - MEMBER: Grundfunktionen, Gang-Missionen
 * - RECRUIT: Nur Grundfunktionen, kein Kick/Invite
 */
public enum GangRank {

    BOSS("Boss", "\u00A7c", 4, true, true, true, true, true),
    UNDERBOSS("Underboss", "\u00A76", 3, true, true, true, false, false),
    MEMBER("Member", "\u00A7e", 2, false, false, true, false, false),
    RECRUIT("Rekrut", "\u00A77", 1, false, false, false, false, false);

    private final String displayName;
    private final String colorCode;
    private final int priority;
    private final boolean canInvite;
    private final boolean canKick;
    private final boolean canClaimTerritory;
    private final boolean canManagePerks;
    private final boolean canDisband;

    GangRank(String displayName, String colorCode, int priority,
             boolean canInvite, boolean canKick, boolean canClaimTerritory,
             boolean canManagePerks, boolean canDisband) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.priority = priority;
        this.canInvite = canInvite;
        this.canKick = canKick;
        this.canClaimTerritory = canClaimTerritory;
        this.canManagePerks = canManagePerks;
        this.canDisband = canDisband;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public int getPriority() { return priority; }
    public boolean canInvite() { return canInvite; }
    public boolean canKick() { return canKick; }
    public boolean canClaimTerritory() { return canClaimTerritory; }
    public boolean canManagePerks() { return canManagePerks; }
    public boolean canDisband() { return canDisband; }

    /**
     * Prueft ob dieser Rang einen anderen Rang kicken kann.
     * Nur hoeherrangige koennen niedrigerrangige kicken.
     */
    public boolean canKickRank(GangRank other) {
        return canKick && this.priority > other.priority;
    }

    /**
     * Prueft ob dieser Rang einen anderen Rang befoerdern kann.
     * Kann nur bis zu einem Rang unter dem eigenen befoerdern.
     */
    public boolean canPromoteTo(GangRank targetRank) {
        return this.priority > targetRank.priority && targetRank.priority > 1;
    }

    public String getFormattedName() {
        return colorCode + displayName;
    }
}
