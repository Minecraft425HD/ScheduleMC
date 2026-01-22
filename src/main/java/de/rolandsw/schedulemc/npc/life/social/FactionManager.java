package de.rolandsw.schedulemc.npc.life.social;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * FactionManager - Verwaltet alle Fraktionsbeziehungen
 *
 * Globaler Manager für Spieler-Fraktion Beziehungen.
 */
public class FactionManager {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, FactionManager> MANAGERS = new HashMap<>();

    public static FactionManager getManager(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, l -> new FactionManager());
    }

    public static void removeManager(ServerLevel level) {
        MANAGERS.remove(level);
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Spieler -> Fraktionsbeziehungen */
    private final Map<UUID, Map<Faction, FactionRelation>> playerFactions = new HashMap<>();

    // ═══════════════════════════════════════════════════════════
    // PLAYER FACTION MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt die Beziehung eines Spielers zu einer Fraktion
     */
    public FactionRelation getRelation(UUID playerUUID, Faction faction) {
        Map<Faction, FactionRelation> relations = playerFactions.computeIfAbsent(
            playerUUID, k -> createDefaultRelations()
        );
        return relations.computeIfAbsent(faction, FactionRelation::new);
    }

    /**
     * Holt alle Fraktionsbeziehungen eines Spielers
     */
    public Map<Faction, FactionRelation> getAllRelations(UUID playerUUID) {
        return playerFactions.computeIfAbsent(playerUUID, k -> createDefaultRelations());
    }

    /**
     * Erstellt Standard-Beziehungen für alle Fraktionen
     */
    private Map<Faction, FactionRelation> createDefaultRelations() {
        Map<Faction, FactionRelation> relations = new EnumMap<>(Faction.class);
        for (Faction faction : Faction.values()) {
            relations.put(faction, new FactionRelation(faction));
        }
        return relations;
    }

    // ═══════════════════════════════════════════════════════════
    // REPUTATION MODIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Ändert die Reputation eines Spielers bei einer Fraktion
     */
    public void modifyReputation(UUID playerUUID, Faction faction, int amount) {
        getRelation(playerUUID, faction).modifyReputation(amount);

        // Gegenwirkung auf verbündete/feindliche Fraktionen
        for (Faction other : Faction.values()) {
            if (other == faction) continue;

            int baseRelation = faction.getBaseRelationWith(other);
            if (baseRelation > 50) {
                // Verbündete: Halbe positive Auswirkung
                if (amount > 0) {
                    getRelation(playerUUID, other).modifyReputation(amount / 2);
                }
            } else if (baseRelation < -50) {
                // Feinde: Negative Auswirkung bei positiven Aktionen
                if (amount > 0) {
                    getRelation(playerUUID, other).modifyReputation(-amount / 3);
                } else {
                    // Bei negativen Aktionen gegen Feinde: Positiv
                    getRelation(playerUUID, other).modifyReputation(-amount / 4);
                }
            }
        }
    }

    /**
     * Ändert die Reputation basierend auf einem Verbrechen
     */
    public void onCrimeCommitted(UUID playerUUID, String crimeType, int severity) {
        // Ordnungshüter verlieren Reputation
        modifyReputation(playerUUID, Faction.ORDNUNG, -severity * 5);

        // Bürger verlieren auch
        modifyReputation(playerUUID, Faction.BUERGER, -severity * 3);

        // Händler verlieren weniger
        modifyReputation(playerUUID, Faction.HAENDLER, -severity * 2);

        // Untergrund gewinnt (außer bei Verbrechen gegen sie)
        if (!crimeType.contains("untergrund")) {
            modifyReputation(playerUUID, Faction.UNTERGRUND, severity);
        }
    }

    /**
     * Ändert die Reputation basierend auf einer guten Tat
     */
    public void onGoodDeed(UUID playerUUID, Faction beneficiary, int amount) {
        modifyReputation(playerUUID, beneficiary, amount);

        // Generell positive Auswirkung auf alle außer Untergrund
        if (beneficiary != Faction.UNTERGRUND) {
            for (Faction faction : Faction.values()) {
                if (faction != beneficiary && faction != Faction.UNTERGRUND) {
                    modifyReputation(playerUUID, faction, amount / 4);
                }
            }
        }
    }

    /**
     * Ändert die Reputation basierend auf einer Transaktion
     */
    public void onTransaction(UUID playerUUID, Faction sellerFaction, int transactionValue, boolean wasFair) {
        if (wasFair) {
            modifyReputation(playerUUID, sellerFaction, Math.min(5, transactionValue / 100));
        } else {
            modifyReputation(playerUUID, sellerFaction, -3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MEMBERSHIP
    // ═══════════════════════════════════════════════════════════

    /**
     * Lässt einen Spieler einer Fraktion beitreten
     */
    public boolean joinFaction(UUID playerUUID, Faction faction, String title) {
        FactionRelation relation = getRelation(playerUUID, faction);

        // Mindest-Standing erforderlich
        if (relation.getStanding().ordinal() < FactionRelation.FactionStanding.FRIENDLY.ordinal()) {
            return false;
        }

        // Nicht bei feindlicher Fraktion beitreten können
        for (Faction other : Faction.values()) {
            if (other != faction && getRelation(playerUUID, other).isMember()) {
                if (faction.isHostileTo(other)) {
                    // Muss erst andere Fraktion verlassen
                    return false;
                }
            }
        }

        relation.joinFaction(title);
        return true;
    }

    /**
     * Entfernt einen Spieler aus einer Fraktion
     */
    public void leaveFaction(UUID playerUUID, Faction faction) {
        getRelation(playerUUID, faction).leaveFaction();
    }

    /**
     * Holt die Fraktion, bei der ein Spieler Mitglied ist
     */
    public Optional<Faction> getPlayerMemberFaction(UUID playerUUID) {
        Map<Faction, FactionRelation> relations = getAllRelations(playerUUID);
        return relations.entrySet().stream()
            .filter(e -> e.getValue().isMember())
            .map(Map.Entry::getKey)
            .findFirst();
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den effektiven Preis-Modifikator für einen Spieler bei einer Fraktion
     */
    public float getPriceModifier(UUID playerUUID, Faction faction) {
        return getRelation(playerUUID, faction).getPriceModifier();
    }

    /**
     * Prüft ob ein Spieler mit einer Fraktion handeln kann
     */
    public boolean canTrade(UUID playerUUID, Faction faction) {
        return getRelation(playerUUID, faction).canTrade();
    }

    /**
     * Prüft ob ein Spieler Quests von einer Fraktion annehmen kann
     */
    public boolean canAcceptQuests(UUID playerUUID, Faction faction) {
        return getRelation(playerUUID, faction).canAcceptQuests();
    }

    /**
     * Prüft ob eine Fraktion einem Spieler helfen würde
     */
    public boolean wouldHelp(UUID playerUUID, Faction faction) {
        return getRelation(playerUUID, faction).wouldHelp();
    }

    /**
     * Findet die Fraktion mit der besten Beziehung zum Spieler
     */
    public Faction getBestFaction(UUID playerUUID) {
        return getAllRelations(playerUUID).entrySet().stream()
            .max(Comparator.comparingInt(e -> e.getValue().getReputation()))
            .map(Map.Entry::getKey)
            .orElse(Faction.BUERGER);
    }

    /**
     * Findet die Fraktion mit der schlechtesten Beziehung zum Spieler
     */
    public Faction getWorstFaction(UUID playerUUID) {
        return getAllRelations(playerUUID).entrySet().stream()
            .min(Comparator.comparingInt(e -> e.getValue().getReputation()))
            .map(Map.Entry::getKey)
            .orElse(Faction.ORDNUNG);
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, Map<Faction, FactionRelation>> entry : playerFactions.entrySet()) {
            ListTag relationsList = new ListTag();
            for (FactionRelation relation : entry.getValue().values()) {
                relationsList.add(relation.save());
            }
            playersTag.put(entry.getKey().toString(), relationsList);
        }
        tag.put("PlayerFactions", playersTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        playerFactions.clear();

        CompoundTag playersTag = tag.getCompound("PlayerFactions");
        for (String uuidStr : playersTag.getAllKeys()) {
            UUID playerUUID = UUID.fromString(uuidStr);
            Map<Faction, FactionRelation> relations = new EnumMap<>(Faction.class);

            ListTag relationsList = playersTag.getList(uuidStr, Tag.TAG_COMPOUND);
            for (int i = 0; i < relationsList.size(); i++) {
                FactionRelation relation = FactionRelation.load(relationsList.getCompound(i));
                relations.put(relation.getFaction(), relation);
            }

            // Fehlende Fraktionen mit Standard füllen
            for (Faction faction : Faction.values()) {
                relations.computeIfAbsent(faction, FactionRelation::new);
            }

            playerFactions.put(playerUUID, relations);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("FactionManager{trackedPlayers=%d}", playerFactions.size());
    }

    /**
     * Gibt eine detaillierte Übersicht für einen Spieler zurück
     */
    public String getPlayerOverview(UUID playerUUID) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Fraktions-Übersicht ===\n");

        Map<Faction, FactionRelation> relations = getAllRelations(playerUUID);
        for (Faction faction : Faction.values()) {
            FactionRelation rel = relations.get(faction);
            sb.append(String.format("%s: %d (%s)%s\n",
                faction.getDisplayName(),
                rel.getReputation(),
                rel.getStanding().getDisplayName(),
                rel.isMember() ? " [" + rel.getMemberTitle() + "]" : ""
            ));
        }

        return sb.toString();
    }
}
