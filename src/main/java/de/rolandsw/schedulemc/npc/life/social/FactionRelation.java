package de.rolandsw.schedulemc.npc.life.social;

import net.minecraft.nbt.CompoundTag;

/**
 * FactionRelation - Beziehung eines Spielers zu einer Fraktion
 *
 * Verwaltet Reputation, Rang und besondere Status.
 */
public class FactionRelation {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final int MIN_REPUTATION = -100;
    public static final int MAX_REPUTATION = 100;

    // Reputations-Schwellen
    public static final int HOSTILE_THRESHOLD = -50;
    public static final int UNFRIENDLY_THRESHOLD = -20;
    public static final int NEUTRAL_THRESHOLD = 20;
    public static final int FRIENDLY_THRESHOLD = 50;
    public static final int HONORED_THRESHOLD = 80;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final Faction faction;
    private int reputation;
    private FactionStanding standing;

    // Optionale spezielle Rollen
    private boolean isMember = false;
    private String memberTitle = "";
    private boolean isKnown = false; // Hat die Fraktion diesen Spieler bemerkt?

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public FactionRelation(Faction faction) {
        this.faction = faction;
        this.reputation = faction.getBaseReputation();
        this.standing = calculateStanding();
    }

    public FactionRelation(Faction faction, int reputation) {
        this.faction = faction;
        this.reputation = clamp(reputation);
        this.standing = calculateStanding();
    }

    // ═══════════════════════════════════════════════════════════
    // REPUTATION MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Ändert die Reputation
     */
    public void modifyReputation(int amount) {
        this.reputation = clamp(this.reputation + amount);
        this.standing = calculateStanding();

        // Wenn signifikante Änderung, wird Spieler bekannt
        if (Math.abs(amount) >= 10) {
            this.isKnown = true;
        }
    }

    /**
     * Setzt die Reputation direkt
     */
    public void setReputation(int reputation) {
        this.reputation = clamp(reputation);
        this.standing = calculateStanding();
    }

    /**
     * Berechnet den aktuellen Standing basierend auf Reputation
     */
    private FactionStanding calculateStanding() {
        if (reputation <= HOSTILE_THRESHOLD) return FactionStanding.HOSTILE;
        if (reputation <= UNFRIENDLY_THRESHOLD) return FactionStanding.UNFRIENDLY;
        if (reputation <= NEUTRAL_THRESHOLD) return FactionStanding.NEUTRAL;
        if (reputation <= FRIENDLY_THRESHOLD) return FactionStanding.FRIENDLY;
        if (reputation <= HONORED_THRESHOLD) return FactionStanding.HONORED;
        return FactionStanding.REVERED;
    }

    // ═══════════════════════════════════════════════════════════
    // MEMBERSHIP
    // ═══════════════════════════════════════════════════════════

    /**
     * Macht den Spieler zum Mitglied dieser Fraktion
     */
    public void joinFaction(String title) {
        this.isMember = true;
        this.memberTitle = title;
        this.isKnown = true;

        // Bonus-Reputation beim Beitritt
        modifyReputation(20);
    }

    /**
     * Entfernt den Spieler aus der Fraktion
     */
    public void leaveFaction() {
        this.isMember = false;
        this.memberTitle = "";

        // Reputation-Verlust beim Verlassen
        modifyReputation(-30);
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob der Spieler mit dieser Fraktion handeln kann
     */
    public boolean canTrade() {
        return standing.canTrade();
    }

    /**
     * Prüft ob der Spieler Quests von dieser Fraktion annehmen kann
     */
    public boolean canAcceptQuests() {
        return standing.canAcceptQuests();
    }

    /**
     * Prüft ob die Fraktion dem Spieler helfen würde
     */
    public boolean wouldHelp() {
        return standing.wouldHelp() || isMember;
    }

    /**
     * Prüft ob die Fraktion den Spieler angreifen würde
     */
    public boolean wouldAttack() {
        return standing == FactionStanding.HOSTILE;
    }

    /**
     * Berechnet den Preis-Modifikator basierend auf Standing
     */
    public float getPriceModifier() {
        return standing.getPriceModifier();
    }

    /**
     * Berechnet wie viel Reputation für das nächste Level benötigt wird
     */
    public int getReputationToNextLevel() {
        return switch (standing) {
            case HOSTILE -> UNFRIENDLY_THRESHOLD - reputation;
            case UNFRIENDLY -> NEUTRAL_THRESHOLD - reputation;
            case NEUTRAL -> FRIENDLY_THRESHOLD - reputation;
            case FRIENDLY -> HONORED_THRESHOLD - reputation;
            case HONORED -> MAX_REPUTATION - reputation;
            case REVERED -> 0; // Bereits max
        };
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public Faction getFaction() {
        return faction;
    }

    public int getReputation() {
        return reputation;
    }

    public FactionStanding getStanding() {
        return standing;
    }

    public boolean isMember() {
        return isMember;
    }

    public String getMemberTitle() {
        return memberTitle;
    }

    public boolean isKnown() {
        return isKnown;
    }

    public void setKnown(boolean known) {
        this.isKnown = known;
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    private static int clamp(int value) {
        return Math.max(MIN_REPUTATION, Math.min(MAX_REPUTATION, value));
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Faction", faction.name());
        tag.putInt("Reputation", reputation);
        tag.putBoolean("IsMember", isMember);
        tag.putString("MemberTitle", memberTitle);
        tag.putBoolean("IsKnown", isKnown);
        return tag;
    }

    public static FactionRelation load(CompoundTag tag) {
        Faction faction = Faction.fromName(tag.getString("Faction"));
        int reputation = tag.getInt("Reputation");

        FactionRelation relation = new FactionRelation(faction, reputation);
        relation.isMember = tag.getBoolean("IsMember");
        relation.memberTitle = tag.getString("MemberTitle");
        relation.isKnown = tag.getBoolean("IsKnown");

        return relation;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("FactionRelation{%s: %d (%s)%s}",
            faction.getDisplayName(),
            reputation,
            standing.getDisplayName(),
            isMember ? " [Mitglied]" : ""
        );
    }

    // ═══════════════════════════════════════════════════════════
    // STANDING ENUM
    // ═══════════════════════════════════════════════════════════

    /**
     * FactionStanding - Die verschiedenen Stufen der Beziehung
     */
    public enum FactionStanding {
        HOSTILE("Feindlich", false, false, false, 1.5f),
        UNFRIENDLY("Unfreundlich", true, false, false, 1.2f),
        NEUTRAL("Neutral", true, false, false, 1.0f),
        FRIENDLY("Freundlich", true, true, true, 0.95f),
        HONORED("Geehrt", true, true, true, 0.9f),
        REVERED("Verehrt", true, true, true, 0.85f);

        private final String displayName;
        private final boolean canTrade;
        private final boolean canAcceptQuests;
        private final boolean wouldHelp;
        private final float priceModifier;

        FactionStanding(String displayName, boolean canTrade, boolean canAcceptQuests,
                       boolean wouldHelp, float priceModifier) {
            this.displayName = displayName;
            this.canTrade = canTrade;
            this.canAcceptQuests = canAcceptQuests;
            this.wouldHelp = wouldHelp;
            this.priceModifier = priceModifier;
        }

        public String getDisplayName() { return displayName; }
        public boolean canTrade() { return canTrade; }
        public boolean canAcceptQuests() { return canAcceptQuests; }
        public boolean wouldHelp() { return wouldHelp; }
        public float getPriceModifier() { return priceModifier; }
    }
}
