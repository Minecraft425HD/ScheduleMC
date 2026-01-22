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
    DELIVERY("Lieferung", "Liefern Sie einen Gegenstand", 1.0f),

    /**
     * Sammelquest - Sammle bestimmte Items
     */
    COLLECTION("Sammlung", "Sammeln Sie bestimmte Gegenstände", 1.2f),

    /**
     * Eskort - Begleite NPC sicher zu einem Ort
     */
    ESCORT("Eskorte", "Begleiten Sie jemanden sicher", 1.5f),

    /**
     * Eliminierung - Besiege bestimmte Feinde
     */
    ELIMINATION("Eliminierung", "Beseitigen Sie die Bedrohung", 1.3f),

    /**
     * Investigation - Finde Informationen heraus
     */
    INVESTIGATION("Ermittlung", "Untersuchen Sie den Fall", 1.4f),

    /**
     * Negotiation - Verhandle mit anderen NPCs
     */
    NEGOTIATION("Verhandlung", "Verhandeln Sie einen Deal", 1.6f);

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
        };
    }

    /**
     * Kann diese Quest von einem bestimmten NPC-Typ gegeben werden?
     */
    public boolean canBeGivenBy(de.rolandsw.schedulemc.npc.NPCType npcType) {
        return switch (this) {
            case DELIVERY -> true; // Jeder kann Lieferquests geben
            case COLLECTION -> true;
            case ESCORT -> npcType != de.rolandsw.schedulemc.npc.NPCType.POLICE;
            case ELIMINATION -> npcType == de.rolandsw.schedulemc.npc.NPCType.POLICE ||
                               npcType == de.rolandsw.schedulemc.npc.NPCType.CITIZEN;
            case INVESTIGATION -> npcType == de.rolandsw.schedulemc.npc.NPCType.POLICE ||
                                 npcType == de.rolandsw.schedulemc.npc.NPCType.MERCHANT;
            case NEGOTIATION -> npcType == de.rolandsw.schedulemc.npc.NPCType.MERCHANT ||
                               npcType == de.rolandsw.schedulemc.npc.NPCType.DRUG_DEALER;
        };
    }
}
