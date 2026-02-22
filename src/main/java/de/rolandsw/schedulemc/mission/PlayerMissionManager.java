package de.rolandsw.schedulemc.mission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.mission.network.MissionNetworkHandler;
import de.rolandsw.schedulemc.mission.client.PlayerMissionDto;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Verwaltet spielerindividuelle Missionen.
 *
 * Pattern: Singleton mit double-checked locking, analog GangMissionManager.
 * Persistenz: JSON via Gson in configDir/schedulemc_missions.json.
 */
public class PlayerMissionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerMissionManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile PlayerMissionManager instance;

    /** playerUUID -> Liste aktiver/abgeschlossener Missionen */
    private final ConcurrentHashMap<UUID, List<PlayerMission>> playerMissions = new ConcurrentHashMap<>();

    private final Path saveFile;
    private final Path backupFile;

    private PlayerMissionManager(Path saveDir) {
        this.saveFile = saveDir.resolve("schedulemc_missions.json");
        this.backupFile = saveDir.resolve("schedulemc_missions.json.bak");
        load();
    }

    public static PlayerMissionManager getInstance() {
        return instance;
    }

    public static PlayerMissionManager getInstance(Path saveDir) {
        if (instance == null) {
            synchronized (PlayerMissionManager.class) {
                if (instance == null) {
                    instance = new PlayerMissionManager(saveDir);
                }
            }
        }
        return instance;
    }

    public static void resetInstance() {
        if (instance != null) {
            instance.save();
            instance = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MISSION-VERWALTUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt alle Missionen eines Spielers zurück.
     */
    public List<PlayerMission> getPlayerMissions(UUID playerUUID) {
        return playerMissions.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Nimmt eine Mission an.
     * @return true wenn erfolgreich
     */
    public boolean acceptMission(ServerPlayer player, String definitionId) {
        UUID uuid = player.getUUID();
        MissionDefinition def = MissionRegistry.getById(definitionId);
        if (def == null) {
            LOGGER.warn("acceptMission: unbekannte Mission '{}' für {}", definitionId, uuid);
            return false;
        }

        List<PlayerMission> missions = playerMissions.computeIfAbsent(uuid, k -> new ArrayList<>());

        // Bereits aktiv oder abgeschlossen (nicht claimed)?
        for (PlayerMission m : missions) {
            if (m.getDefinitionId().equals(definitionId) && m.getStatus() != MissionStatus.CLAIMED) {
                return false;
            }
        }

        // Voraussetzungen prüfen
        for (String prereqId : def.getPrerequisiteIds()) {
            boolean fulfilled = missions.stream().anyMatch(
                m -> m.getDefinitionId().equals(prereqId) && m.getStatus() == MissionStatus.CLAIMED
            );
            if (!fulfilled) {
                return false;
            }
        }

        String missionId = "pm_" + uuid.toString().substring(0, 8) + "_" + definitionId;
        PlayerMission mission = new PlayerMission(missionId, def, uuid);
        missions.add(mission);

        syncToPlayer(player);
        return true;
    }

    /**
     * Gibt eine Mission auf.
     * @return true wenn erfolgreich
     */
    public boolean abandonMission(ServerPlayer player, String missionId) {
        UUID uuid = player.getUUID();
        List<PlayerMission> missions = playerMissions.get(uuid);
        if (missions == null) return false;

        boolean removed = missions.removeIf(
            m -> m.getMissionId().equals(missionId) && m.getStatus() == MissionStatus.ACTIVE
        );
        if (removed) {
            syncToPlayer(player);
        }
        return removed;
    }

    /**
     * Holt die Belohnung einer abgeschlossenen Mission ab.
     * @return true wenn erfolgreich
     */
    public boolean claimMission(ServerPlayer player, String missionId) {
        UUID uuid = player.getUUID();
        List<PlayerMission> missions = playerMissions.get(uuid);
        if (missions == null) return false;

        for (PlayerMission mission : missions) {
            if (mission.getMissionId().equals(missionId) && mission.claim()) {
                // XP und Geld auszahlen
                MissionDefinition def = mission.getDefinition();
                player.giveExperiencePoints(def.getXpReward());
                // Geld via EconomyManager auszahlen (falls verfügbar)
                try {
                    de.rolandsw.schedulemc.economy.EconomyManager em = de.rolandsw.schedulemc.economy.EconomyManager.getInstance();
                    if (em != null) {
                        em.addMoney(uuid, def.getMoneyReward(), "Mission: " + def.getTitle());
                    }
                } catch (Exception e) {
                    LOGGER.error("Fehler beim Auszahlen der Mission-Belohnung für {}", uuid, e);
                }
                syncToPlayer(player);
                return true;
            }
        }
        return false;
    }

    /**
     * Verfolgt Missionsfortschritt für alle Spieler mit passendem trackingKey.
     * Inkrementiert den Fortschritt um 'amount'.
     */
    public void trackProgress(ServerPlayer player, String trackingKey, int amount) {
        UUID uuid = player.getUUID();
        List<PlayerMission> missions = playerMissions.get(uuid);
        if (missions == null) return;

        boolean changed = false;
        for (PlayerMission mission : missions) {
            if (mission.getStatus() == MissionStatus.ACTIVE
                    && mission.getDefinition().getTrackingKey().equals(trackingKey)) {
                boolean completed = mission.addProgress(amount);
                changed = true;
                if (completed) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a✓ Mission abgeschlossen: §f" + mission.getDefinition().getTitle()
                    ));
                }
            }
        }
        if (changed) {
            syncToPlayer(player);
        }
    }

    /**
     * Sendet aktuellen Missionsstatus an den Spieler.
     */
    public void syncToPlayer(ServerPlayer player) {
        List<PlayerMission> missions = getPlayerMissions(player.getUUID());
        List<PlayerMissionDto> dtos = missions.stream()
            .map(PlayerMission::toDto)
            .collect(Collectors.toList());
        MissionNetworkHandler.sendToPlayer(
            new de.rolandsw.schedulemc.mission.network.SyncMissionsPacket(dtos), player
        );
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    public void save() {
        try {
            Map<String, List<MissionSaveEntry>> data = new HashMap<>();
            for (Map.Entry<UUID, List<PlayerMission>> entry : playerMissions.entrySet()) {
                List<MissionSaveEntry> entries = new ArrayList<>();
                for (PlayerMission m : entry.getValue()) {
                    entries.add(MissionSaveEntry.from(m));
                }
                data.put(entry.getKey().toString(), entries);
            }

            String json = GSON.toJson(data);
            // Atomarer Write: temp → rename
            Path tmpFile = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
            Files.writeString(tmpFile, json);
            if (Files.exists(saveFile)) {
                Files.copy(saveFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(tmpFile, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Missions-Daten", e);
        }
    }

    private void load() {
        if (!Files.exists(saveFile)) return;
        try {
            String json = Files.readString(saveFile);
            Type type = new TypeToken<Map<String, List<MissionSaveEntry>>>() {}.getType();
            Map<String, List<MissionSaveEntry>> data = GSON.fromJson(json, type);
            if (data == null) return;

            for (Map.Entry<String, List<MissionSaveEntry>> entry : data.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    List<PlayerMission> missions = new ArrayList<>();
                    for (MissionSaveEntry saved : entry.getValue()) {
                        MissionDefinition def = MissionRegistry.getById(saved.definitionId);
                        if (def == null) {
                            LOGGER.warn("Mission '{}' nicht mehr in Registry, wird übersprungen", saved.definitionId);
                            continue;
                        }
                        MissionStatus status = MissionStatus.valueOf(saved.status);
                        missions.add(new PlayerMission(
                            saved.missionId, saved.definitionId, def, uuid,
                            saved.currentProgress, status,
                            saved.acceptedAt, saved.completedAt, saved.claimedAt
                        ));
                    }
                    playerMissions.put(uuid, missions);
                } catch (Exception e) {
                    LOGGER.error("Fehler beim Laden von Mission-Daten für Spieler '{}'", entry.getKey(), e);
                }
            }
            LOGGER.info("Missions-Daten geladen: {} Spieler", playerMissions.size());
        } catch (IOException e) {
            LOGGER.error("Fehler beim Lesen der Missions-Datei, versuche Backup", e);
            loadBackup();
        }
    }

    private void loadBackup() {
        if (!Files.exists(backupFile)) return;
        try {
            Files.copy(backupFile, saveFile, StandardCopyOption.REPLACE_EXISTING);
            load();
        } catch (IOException e) {
            LOGGER.error("Backup-Wiederherstellung fehlgeschlagen", e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SAVE-ENTRY (JSON-Serialisierung)
    // ═══════════════════════════════════════════════════════════

    private static class MissionSaveEntry {
        String missionId;
        String definitionId;
        int currentProgress;
        String status;
        long acceptedAt;
        long completedAt;
        long claimedAt;

        static MissionSaveEntry from(PlayerMission m) {
            MissionSaveEntry e = new MissionSaveEntry();
            e.missionId = m.getMissionId();
            e.definitionId = m.getDefinitionId();
            e.currentProgress = m.getCurrentProgress();
            e.status = m.getStatus().name();
            e.acceptedAt = m.getAcceptedAt();
            e.completedAt = m.getCompletedAt();
            e.claimedAt = m.getClaimedAt();
            return e;
        }
    }
}
