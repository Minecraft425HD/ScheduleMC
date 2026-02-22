package de.rolandsw.schedulemc.gang.mission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.gang.Gang;
import de.rolandsw.schedulemc.gang.GangManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verwaltet alle Gang-Missionen (Auftraege).
 *
 * Features:
 * - Automatische Generierung bei Reset (stuendlich/taeglich/woechentlich)
 * - Fortschritts-Tracking via trackingKey
 * - Zustandsbasierte Pruefungen (Territorien, Online-Mitglieder, Kasse)
 * - Belohnungs-Einloesung
 * - JSON-Persistenz
 */
public class GangMissionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GangMissionManager.class);
    private static volatile GangMissionManager instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // gangId -> Liste aktiver Missionen
    private final Map<UUID, List<GangMission>> gangMissions = new ConcurrentHashMap<>();

    // gangId -> MissionType -> Letzter Reset-Timestamp
    private final Map<UUID, Map<MissionType, Long>> lastResets = new ConcurrentHashMap<>();

    // gangId -> Woechentliche Statistik
    private final Map<UUID, WeeklyStats> weeklyStats = new ConcurrentHashMap<>();

    private final Path saveFile;
    private final AtomicInteger missionIdCounter = new AtomicInteger(0);

    private GangMissionManager(Path saveDir) {
        this.saveFile = saveDir.resolve("schedulemc_gang_missions.json");
        load();
    }

    public static GangMissionManager getInstance() {
        return instance;
    }

    public static GangMissionManager getInstance(Path saveDir) {
        if (instance == null) {
            synchronized (GangMissionManager.class) {
                if (instance == null) {
                    instance = new GangMissionManager(saveDir);
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
    // MISSION-GENERIERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Prueft und generiert neue Missionen fuer alle Gangs.
     * Wird im Tick-Handler aufgerufen.
     */
    public void checkAndGenerateMissions(MinecraftServer server) {
        GangManager gm = GangManager.getInstance();
        if (gm == null) return;

        long now = System.currentTimeMillis();

        for (Gang gang : gm.getAllGangs()) {
            UUID gangId = gang.getGangId();

            // Sicherstellen dass die Gang Eintraege hat
            gangMissions.computeIfAbsent(gangId, k -> new ArrayList<>());
            lastResets.computeIfAbsent(gangId, k -> new EnumMap<>(MissionType.class));

            for (MissionType type : MissionType.values()) {
                Map<MissionType, Long> resets = lastResets.get(gangId);
                long lastReset = resets.getOrDefault(type, 0L);
                long nextReset = type.getNextResetAfter(lastReset);

                if (now >= nextReset || lastReset == 0) {
                    generateMissions(gangId, type);
                    resets.put(type, now);
                }
            }

            // Zustandsbasierte Missionen pruefen
            checkThresholdMissions(gang, server);
        }
    }

    /**
     * Generiert sofort alle Missionen fuer eine neu gegruendete Gang.
     * Wird direkt nach Gang-Erstellung aufgerufen.
     */
    public void generateInitialMissions(UUID gangId) {
        gangMissions.computeIfAbsent(gangId, k -> new ArrayList<>());
        Map<MissionType, Long> resets = lastResets.computeIfAbsent(gangId, k -> new EnumMap<>(MissionType.class));

        long now = System.currentTimeMillis();
        for (MissionType type : MissionType.values()) {
            generateMissions(gangId, type);
            resets.put(type, now);
        }
        LOGGER.info("Generated initial missions for new gang {}", gangId);
    }

    private void generateMissions(UUID gangId, MissionType type) {
        List<GangMission> missions = gangMissions.get(gangId);
        if (missions == null) return;

        // Alte Missionen dieses Typs entfernen
        missions.removeIf(m -> m.getType() == type);

        // Zufaellige Templates waehlen
        MissionTemplate[] pool = MissionTemplate.getByType(type);
        List<MissionTemplate> shuffled = new ArrayList<>(Arrays.asList(pool));
        Collections.shuffle(shuffled);

        int count = Math.min(type.getMissionCount(), shuffled.size());
        for (int i = 0; i < count; i++) {
            String id = "m_" + missionIdCounter.incrementAndGet();
            missions.add(shuffled.get(i).generate(id));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FORTSCHRITTS-TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Trackt Fortschritt fuer eine Gang (INCREMENTAL Missionen).
     * Wird aufgerufen wenn ein Event stattfindet (z.B. Verkauf).
     *
     * @param gangId     Gang-UUID
     * @param trackingKey Tracking-Schluessel (z.B. "SELL", "EARN")
     * @param amount     Menge (z.B. 1 fuer 1 Verkauf, 500 fuer 500€)
     */
    public void trackProgress(UUID gangId, String trackingKey, int amount) {
        List<GangMission> missions = gangMissions.get(gangId);
        if (missions == null) return;

        for (GangMission mission : missions) {
            if (mission.getTrackingKey().equals(trackingKey)
                    && mission.getTrackingMode() == MissionTemplate.TrackingMode.INCREMENTAL
                    && !mission.isCompleted()) {
                boolean justCompleted = mission.addProgress(amount);
                if (justCompleted) {
                    onMissionCompleted(gangId, mission);
                }
            }
        }
    }

    /**
     * Prueft zustandsbasierte Missionen (THRESHOLD).
     */
    private void checkThresholdMissions(Gang gang, MinecraftServer server) {
        List<GangMission> missions = gangMissions.get(gang.getGangId());
        if (missions == null) return;

        for (GangMission mission : missions) {
            if (mission.getTrackingMode() != MissionTemplate.TrackingMode.THRESHOLD) continue;
            if (mission.isCompleted()) continue;

            int currentValue = getThresholdValue(gang, mission.getTrackingKey(), server);
            if (currentValue > 0) {
                boolean justCompleted = mission.setProgress(currentValue);
                if (justCompleted) {
                    onMissionCompleted(gang.getGangId(), mission);
                }
            }
        }
    }

    private int getThresholdValue(Gang gang, String key, MinecraftServer server) {
        return switch (key) {
            case "TERRITORY_COUNT" -> gang.getTerritoryCount();
            case "GANG_BALANCE" -> gang.getGangBalance();
            case "ONLINE_MEMBERS" -> {
                int online = 0;
                for (UUID member : gang.getMembers().keySet()) {
                    if (server.getPlayerList().getPlayer(member) != null) online++;
                }
                yield online;
            }
            default -> 0;
        };
    }

    private void onMissionCompleted(UUID gangId, GangMission mission) {
        // Meta-Mission-Tracking: "X Auftraege erledigen"
        trackProgress(gangId, "COMPLETE_MISSION", 1);

        // Woechentliche Stats updaten
        WeeklyStats stats = weeklyStats.computeIfAbsent(gangId, k -> new WeeklyStats());
        switch (mission.getType()) {
            case HOURLY -> stats.hourlyCompleted++;
            case DAILY -> stats.dailyCompleted++;
            case WEEKLY -> stats.weeklyCompleted++;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BELOHNUNGEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Loest eine Missions-Belohnung ein.
     *
     * @return int[]{xpReward, moneyReward} oder null wenn nicht einloesbar
     */
    public int[] claimReward(UUID gangId, String missionId) {
        List<GangMission> missions = gangMissions.get(gangId);
        if (missions == null) return null;

        for (GangMission mission : missions) {
            if (mission.getMissionId().equals(missionId) && mission.isClaimable()) {
                mission.claim();
                return new int[]{mission.getXpReward(), mission.getMoneyReward()};
            }
        }
        return null;
    }

    /**
     * Prueft ob alle Missionen eines Typs abgeschlossen sind (Bonus).
     */
    public boolean isBonusComplete(UUID gangId, MissionType type) {
        List<GangMission> missions = gangMissions.get(gangId);
        if (missions == null) return false;

        long completed = missions.stream()
                .filter(m -> m.getType() == type && m.isCompleted())
                .count();
        return completed >= type.getMissionCount();
    }

    /**
     * Prueft ob der Bonus bereits eingeloest wurde.
     */
    public boolean isBonusClaimed(UUID gangId, MissionType type) {
        List<GangMission> missions = gangMissions.get(gangId);
        if (missions == null) return false;

        // Bonus gilt als eingeloest wenn alle Missionen des Typs claimed sind
        return missions.stream()
                .filter(m -> m.getType() == type)
                .allMatch(GangMission::isClaimed);
    }

    // ═══════════════════════════════════════════════════════════
    // ABFRAGEN
    // ═══════════════════════════════════════════════════════════

    public List<GangMission> getMissions(UUID gangId) {
        return gangMissions.getOrDefault(gangId, Collections.emptyList());
    }

    public List<GangMission> getMissionsByType(UUID gangId, MissionType type) {
        return getMissions(gangId).stream()
                .filter(m -> m.getType() == type)
                .toList();
    }

    public int getCompletedCount(UUID gangId) {
        return (int) getMissions(gangId).stream().filter(GangMission::isCompleted).count();
    }

    public int getTotalCount(UUID gangId) {
        return getMissions(gangId).size();
    }

    /**
     * Berechnet verbleibende MS bis zum naechsten Reset fuer einen MissionType.
     */
    public long getResetRemainingMs(UUID gangId, MissionType type) {
        Map<MissionType, Long> resets = lastResets.get(gangId);
        if (resets == null) return 0;
        long lastReset = resets.getOrDefault(type, System.currentTimeMillis());
        long nextReset = type.getNextResetAfter(lastReset);
        return Math.max(0, nextReset - System.currentTimeMillis());
    }

    public WeeklyStats getWeeklyStats(UUID gangId) {
        return weeklyStats.computeIfAbsent(gangId, k -> new WeeklyStats());
    }

    /**
     * Setzt woechentliche Stats zurueck (wird bei Wochen-Reset aufgerufen).
     */
    public void resetWeeklyStats(UUID gangId) {
        WeeklyStats stats = weeklyStats.get(gangId);
        if (stats != null) {
            stats.previousWeeks.add(0, new WeekSnapshot(stats));
            if (stats.previousWeeks.size() > 3) {
                stats.previousWeeks.subList(3, stats.previousWeeks.size()).clear();
            }
            stats.reset();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TRACKING-HOOKS (fuer externe Systeme)
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird aufgerufen wenn Gang-XP vergeben wird.
     */
    public void onGangXPAwarded(UUID gangId, int amount) {
        trackProgress(gangId, "GANG_XP", amount);
        WeeklyStats stats = weeklyStats.computeIfAbsent(gangId, k -> new WeeklyStats());
        stats.xpGained += amount;
    }

    /**
     * Wird aufgerufen wenn Geld in die Gang-Kasse eingezahlt wird.
     */
    public void onGangDeposit(UUID gangId, int amount) {
        trackProgress(gangId, "DEPOSIT", amount);
    }

    /**
     * Wird aufgerufen wenn ein Mitglied geworben wird.
     */
    public void onMemberRecruited(UUID gangId) {
        trackProgress(gangId, "RECRUIT", 1);
    }

    /**
     * Wird aufgerufen wenn ein Mitglied befoerdert wird.
     */
    public void onMemberPromoted(UUID gangId) {
        trackProgress(gangId, "PROMOTE", 1);
    }

    /**
     * Wird aufgerufen wenn ein Perk freigeschaltet wird.
     */
    public void onPerkUnlocked(UUID gangId) {
        trackProgress(gangId, "UNLOCK_PERK", 1);
    }

    /**
     * Wird aufgerufen wenn Geld verdient wird (Umsatz).
     */
    public void onMoneyEarned(UUID gangId, int amount) {
        trackProgress(gangId, "EARN", amount);
        WeeklyStats stats = weeklyStats.computeIfAbsent(gangId, k -> new WeeklyStats());
        stats.moneyEarned += amount;
    }

    /**
     * Wird aufgerufen wenn Beitraege kassiert werden.
     */
    public void onFeesCollected(UUID gangId, int amount) {
        WeeklyStats stats = weeklyStats.computeIfAbsent(gangId, k -> new WeeklyStats());
        stats.feesCollected += amount;
    }

    // ═══════════════════════════════════════════════════════════
    // WOECHENTLICHE STATISTIK
    // ═══════════════════════════════════════════════════════════

    public static class WeeklyStats {
        public int xpGained;
        public int moneyEarned;
        public int feesCollected;
        public int hourlyCompleted;
        public int dailyCompleted;
        public int weeklyCompleted;
        public List<WeekSnapshot> previousWeeks = new ArrayList<>();

        void reset() {
            xpGained = 0;
            moneyEarned = 0;
            feesCollected = 0;
            hourlyCompleted = 0;
            dailyCompleted = 0;
            weeklyCompleted = 0;
        }

        public int getTotalCompleted() {
            return hourlyCompleted + dailyCompleted + weeklyCompleted;
        }
    }

    public static class WeekSnapshot {
        public int xpGained;
        public int moneyEarned;

        public WeekSnapshot() {}

        WeekSnapshot(WeeklyStats from) {
            this.xpGained = from.xpGained;
            this.moneyEarned = from.moneyEarned;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    public void save() {
        try {
            Map<String, SavedGangMissions> saveData = new HashMap<>();
            for (Map.Entry<UUID, List<GangMission>> entry : gangMissions.entrySet()) {
                SavedGangMissions saved = new SavedGangMissions();
                saved.missionIdCounter = missionIdCounter.get();

                // Resets
                Map<MissionType, Long> resets = lastResets.get(entry.getKey());
                if (resets != null) {
                    saved.lastHourlyReset = resets.getOrDefault(MissionType.HOURLY, 0L);
                    saved.lastDailyReset = resets.getOrDefault(MissionType.DAILY, 0L);
                    saved.lastWeeklyReset = resets.getOrDefault(MissionType.WEEKLY, 0L);
                }

                // Missionen
                for (GangMission m : entry.getValue()) {
                    SavedMission sm = new SavedMission();
                    sm.id = m.getMissionId();
                    sm.templateName = m.getTemplate().name();
                    sm.description = m.getDescription();
                    sm.target = m.getTargetAmount();
                    sm.xp = m.getXpReward();
                    sm.money = m.getMoneyReward();
                    sm.createdAt = m.getCreatedAt();
                    sm.progress = m.getCurrentProgress();
                    sm.completed = m.isCompleted();
                    sm.claimed = m.isClaimed();
                    saved.missions.add(sm);
                }

                // Weekly Stats
                WeeklyStats ws = weeklyStats.get(entry.getKey());
                if (ws != null) {
                    saved.weeklyXP = ws.xpGained;
                    saved.weeklyMoney = ws.moneyEarned;
                    saved.weeklyFees = ws.feesCollected;
                    saved.weeklyHourly = ws.hourlyCompleted;
                    saved.weeklyDaily = ws.dailyCompleted;
                    saved.weeklyWeekly = ws.weeklyCompleted;
                }

                saveData.put(entry.getKey().toString(), saved);
            }

            String json = GSON.toJson(saveData);
            Files.writeString(saveFile, json);
        } catch (Exception e) {
            LOGGER.error("Failed to save gang missions", e);
        }
    }

    private void load() {
        if (!Files.exists(saveFile)) return;
        try {
            String json = Files.readString(saveFile);
            Type type = new TypeToken<Map<String, SavedGangMissions>>() {}.getType();
            Map<String, SavedGangMissions> saveData = GSON.fromJson(json, type);
            if (saveData == null) return;

            for (Map.Entry<String, SavedGangMissions> entry : saveData.entrySet()) {
                UUID gangId = UUID.fromString(entry.getKey());
                SavedGangMissions saved = entry.getValue();

                missionIdCounter.accumulateAndGet(saved.missionIdCounter, Math::max);

                // Resets
                Map<MissionType, Long> resets = new EnumMap<>(MissionType.class);
                resets.put(MissionType.HOURLY, saved.lastHourlyReset);
                resets.put(MissionType.DAILY, saved.lastDailyReset);
                resets.put(MissionType.WEEKLY, saved.lastWeeklyReset);
                lastResets.put(gangId, resets);

                // Missionen
                List<GangMission> missions = new ArrayList<>();
                for (SavedMission sm : saved.missions) {
                    try {
                        MissionTemplate tpl = MissionTemplate.valueOf(sm.templateName);
                        missions.add(new GangMission(
                                sm.id, tpl, sm.description, sm.target,
                                sm.xp, sm.money, sm.createdAt,
                                sm.progress, sm.completed, sm.claimed
                        ));
                    } catch (IllegalArgumentException e) {
                        // Template wurde entfernt, Mission ueberspringen
                    }
                }
                gangMissions.put(gangId, missions);

                // Weekly Stats
                WeeklyStats ws = new WeeklyStats();
                ws.xpGained = saved.weeklyXP;
                ws.moneyEarned = saved.weeklyMoney;
                ws.feesCollected = saved.weeklyFees;
                ws.hourlyCompleted = saved.weeklyHourly;
                ws.dailyCompleted = saved.weeklyDaily;
                ws.weeklyCompleted = saved.weeklyWeekly;
                weeklyStats.put(gangId, ws);
            }

            LOGGER.info("Loaded missions for {} gangs", gangMissions.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load gang missions", e);
        }
    }

    // Serialisierungs-Klassen
    private static class SavedGangMissions {
        int missionIdCounter;
        long lastHourlyReset, lastDailyReset, lastWeeklyReset;
        List<SavedMission> missions = new ArrayList<>();
        int weeklyXP, weeklyMoney, weeklyFees;
        int weeklyHourly, weeklyDaily, weeklyWeekly;
    }

    private static class SavedMission {
        String id, templateName, description;
        int target, xp, money, progress;
        long createdAt;
        boolean completed, claimed;
    }
}
