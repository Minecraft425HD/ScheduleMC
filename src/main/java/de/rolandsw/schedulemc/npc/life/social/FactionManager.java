package de.rolandsw.schedulemc.npc.life.social;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FactionManager - Verwaltet alle Fraktionsbeziehungen
 *
 * Globaler Manager für Spieler-Fraktion Beziehungen mit JSON-Persistenz.
 */
public class FactionManager extends AbstractPersistenceManager<Map<String, Map<String, FactionRelation>>> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile FactionManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static FactionManager getInstance() {
        return instance;
    }

    public static FactionManager getInstance(MinecraftServer server) {
        FactionManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new FactionManager(server);
                }
            }
        }
        return result;
    }

    /**
     * Gets manager instance for a specific level (convenience method).
     * Note: Manager is server-wide, not per-level.
     */
    public static FactionManager getManager(ServerLevel level) {
        return getInstance(level.getServer());
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Spieler -> Fraktionsbeziehungen */
    private final Map<UUID, Map<Faction, FactionRelation>> playerFactions = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private FactionManager(MinecraftServer server) {
        super(
            new File(server.getServerDirectory(), "config/npc_life_factions.json"),
            GsonHelper.get()
        );
        load();
    }

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
     * Gibt die Reputation eines Spielers bei einer Fraktion zurück
     */
    public int getReputation(UUID playerUUID, Faction faction) {
        return getRelation(playerUUID, faction).getReputation();
    }

    /**
     * Ändert die Reputation eines Spielers bei einer Fraktion
     */
    public void modifyReputation(UUID playerUUID, Faction faction, int amount) {
        getRelation(playerUUID, faction).modifyReputation(amount);
        markDirty();

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
        markDirty();
        return true;
    }

    /**
     * Entfernt einen Spieler aus einer Fraktion
     */
    public void leaveFaction(UUID playerUUID, Faction faction) {
        getRelation(playerUUID, faction).leaveFaction();
        markDirty();
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
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<String, Map<String, FactionRelation>>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<String, Map<String, FactionRelation>> data) {
        playerFactions.clear();

        int invalidCount = 0;
        int correctedCount = 0;

        for (Map.Entry<String, Map<String, FactionRelation>> entry : data.entrySet()) {
            try {
                UUID playerUUID = UUID.fromString(entry.getKey());
                Map<Faction, FactionRelation> relations = new EnumMap<>(Faction.class);

                for (Map.Entry<String, FactionRelation> relEntry : entry.getValue().entrySet()) {
                    try {
                        Faction faction = Faction.valueOf(relEntry.getKey());
                        FactionRelation relation = relEntry.getValue();

                        // DATA VALIDATION: Validate loaded relation data
                        if (relation == null) {
                            LOGGER.warn("Null relation for player {} faction {}, creating default",
                                playerUUID, faction);
                            relation = new FactionRelation(faction);
                            invalidCount++;
                        } else {
                            // Validate reputation range
                            int rep = relation.getReputation();
                            if (rep < FactionRelation.MIN_REPUTATION || rep > FactionRelation.MAX_REPUTATION) {
                                LOGGER.warn("Invalid reputation {} for player {} faction {}, clamping to range",
                                    rep, playerUUID, faction);
                                relation.setReputation(Math.max(FactionRelation.MIN_REPUTATION,
                                    Math.min(FactionRelation.MAX_REPUTATION, rep)));
                                correctedCount++;
                            }

                            // Validate member title length (prevent oversized data)
                            if (relation.isMember() && relation.getMemberTitle() != null
                                    && relation.getMemberTitle().length() > 100) {
                                LOGGER.warn("Member title too long ({} chars) for player {} faction {}, keeping as-is (no setter available)",
                                    relation.getMemberTitle().length(), playerUUID, faction);
                                // Note: FactionRelation has no setMemberTitle(), would need to re-join faction to fix
                            }
                        }

                        relations.put(faction, relation);
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Unknown faction: {}", relEntry.getKey());
                        invalidCount++;
                    }
                }

                // Fehlende Fraktionen mit Standard füllen
                for (Faction faction : Faction.values()) {
                    relations.computeIfAbsent(faction, FactionRelation::new);
                }

                playerFactions.put(playerUUID, relations);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid player UUID in faction data: {}", entry.getKey(), e);
                invalidCount++;
            }
        }

        // Log validation summary
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
    }

    @Override
    protected Map<String, Map<String, FactionRelation>> getCurrentData() {
        Map<String, Map<String, FactionRelation>> data = new HashMap<>();

        for (Map.Entry<UUID, Map<Faction, FactionRelation>> entry : playerFactions.entrySet()) {
            Map<String, FactionRelation> relations = new HashMap<>();
            for (Map.Entry<Faction, FactionRelation> relEntry : entry.getValue().entrySet()) {
                relations.put(relEntry.getKey().name(), relEntry.getValue());
            }
            data.put(entry.getKey().toString(), relations);
        }

        return data;
    }

    @Override
    protected String getComponentName() {
        return "FactionManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d Spieler, %d Beziehungen",
            playerFactions.size(),
            playerFactions.values().stream().mapToInt(Map::size).sum());
    }

    @Override
    protected void onCriticalLoadFailure() {
        playerFactions.clear();
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
