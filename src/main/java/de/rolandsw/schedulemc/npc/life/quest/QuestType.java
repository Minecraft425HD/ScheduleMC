package de.rolandsw.schedulemc.npc.life.quest;

/**
 * QuestType - Die 6 Questtypen des Systems
 *
 * Jeder Typ hat unterschiedliche Mechaniken und Belohnungen.
 */
public enum QuestType {

    // ═══════════════════════════════════════════════════════════
    // QUEST TYPES
    // ═══════════════════════════════════════════════════════════

    /**
     * Lieferquest - Bringe Item von A nach B
     */
    DELIVERY("Delivery", "Deliver an item", 1.0f),

    /**
     * Sammelquest - Sammle bestimmte Items
     */
    COLLECTION("Collection", "Collect specific items", 1.2f),

    /**
     * Eskort - Begleite NPC sicher zu einem Ort
     */
    ESCORT("Escort", "Escort someone safely", 1.5f),

    /**
     * Eliminierung - Besiege bestimmte Feinde
     */
    ELIMINATION("Elimination", "Eliminate the threat", 1.3f),

    /**
     * Investigation - Finde Informationen heraus
     */
    INVESTIGATION("Investigation", "Investigate the case", 1.4f),

    /**
     * Negotiation - Verhandle mit anderen NPCs
     */
    NEGOTIATION("Negotiation", "Negotiate a deal", 1.6f),

    /**
     * Supply - Proaktive Warenanfrage eines NPCs (Treffpunkt + Frist)
     */
    SUPPLY("Errand", "Bring the requested goods to the meeting point", 1.1f);

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String displayName;
    private final String description;
    private final float rewardMultiplier;

    QuestType(String displayName, String description, float rewardMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.rewardMultiplier = rewardMultiplier;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public float getRewardMultiplier() {
        return rewardMultiplier;
    }

    /**
     * Gibt die erwartete Dauer in Minecraft-Tagen zurück
     */
    public int getExpectedDuration() {
        return switch (this) {
            case DELIVERY -> 1;
            case COLLECTION -> 2;
            case ESCORT -> 1;
            case ELIMINATION -> 2;
            case INVESTIGATION -> 3;
            case NEGOTIATION -> 2;
            case SUPPLY -> 2;
        };
    }

    /**
     * Gibt die minimale Faction-Reputation zurück, die benötigt wird
     */
    public int getMinFactionRep() {
        return switch (this) {
            case DELIVERY -> 0;
            case COLLECTION -> 0;
            case ESCORT -> 10;
            case ELIMINATION -> 20;
            case INVESTIGATION -> 15;
            case NEGOTIATION -> 25;
            case SUPPLY -> 0;
        };
    }

    /**
     * Kann diese Quest von einem bestimmten NPC-Typ gegeben werden?
     */
    public boolean canBeGivenBy(de.rolandsw.schedulemc.npc.data.NPCType npcType) {
        return switch (this) {
            case DELIVERY -> true; // Jeder kann Lieferquests geben
            case COLLECTION -> true;
            case ESCORT -> npcType != de.rolandsw.schedulemc.npc.data.NPCType.POLICE;
            case ELIMINATION -> npcType == de.rolandsw.schedulemc.npc.data.NPCType.POLICE ||
                               npcType == de.rolandsw.schedulemc.npc.data.NPCType.CITIZEN;
            case INVESTIGATION -> npcType == de.rolandsw.schedulemc.npc.data.NPCType.POLICE ||
                                 npcType == de.rolandsw.schedulemc.npc.data.NPCType.MERCHANT;
            case NEGOTIATION -> npcType == de.rolandsw.schedulemc.npc.data.NPCType.MERCHANT ||
                               npcType == de.rolandsw.schedulemc.npc.data.NPCType.BANK;
            case SUPPLY -> npcType == de.rolandsw.schedulemc.npc.data.NPCType.MERCHANT ||
                          npcType == de.rolandsw.schedulemc.npc.data.NPCType.CITIZEN;
        };
    }
}
