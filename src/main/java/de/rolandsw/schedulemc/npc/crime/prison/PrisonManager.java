package de.rolandsw.schedulemc.npc.crime.prison;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.crime.prison.network.ClosePrisonScreenPacket;
import de.rolandsw.schedulemc.npc.crime.prison.network.OpenPrisonScreenPacket;
import de.rolandsw.schedulemc.npc.crime.prison.network.PrisonNetworkHandler;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Zentrale Verwaltung des Gefängnissystems
 * SICHERHEIT: Thread-safe Collections für parallele Zugriffe
 */
public class PrisonManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile PrisonManager instance;

    public static final int JAIL_SECONDS_PER_WANTED_LEVEL = 60;
    public static final double BAIL_MULTIPLIER = 1000.0;
    public static final double BAIL_AVAILABLE_AFTER = 0.33;

    private final Map<UUID, PrisonerData> prisoners = new ConcurrentHashMap<>();
    private final Map<UUID, Long> offlineRemainingTime = new ConcurrentHashMap<>();
    // SICHERHEIT: CopyOnWriteArrayList für Thread-Sicherheit
    private final List<String> prisonPlotIds = new CopyOnWriteArrayList<>();

    private static final String SAVE_FILE = "config/schedulemc/prisoners.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class PrisonerData {
        public UUID playerId;
        public String playerName;
        public String prisonPlotId;
        public int cellNumber;
        public int cellSpawnX;
        public int cellSpawnY;
        public int cellSpawnZ;
        public int originalWantedLevel;
        public long arrestTime;
        public long releaseTime;
        public long totalSentenceTicks;
        public double bailAmount;
        public boolean bailPaid = false;
        public long lastOnlineTime;

        public BlockPos getCellSpawn() {
            return new BlockPos(cellSpawnX, cellSpawnY, cellSpawnZ);
        }

        public void setCellSpawn(BlockPos pos) {
            this.cellSpawnX = pos.getX();
            this.cellSpawnY = pos.getY();
            this.cellSpawnZ = pos.getZ();
        }
    }

    public enum ReleaseReason {
        TIME_SERVED("manager.prison.release_reason_time_served"),
        BAIL_PAID("manager.prison.release_reason_bail_paid"),
        ADMIN_RELEASE("manager.prison.release_reason_admin_release");

        public final String displayName;
        ReleaseReason(String name) { this.displayName = name; }
    }

    private PrisonManager() {
        loadPrisonPlots();
        loadPrisonerData();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static PrisonManager getInstance() {
        PrisonManager localRef = instance;
        if (localRef == null) {
            synchronized (PrisonManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new PrisonManager();
                }
            }
        }
        return localRef;
    }

    public static void init() {
        getInstance();
    }

    private void loadPrisonPlots() {
        prisonPlotIds.clear();
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.getType() == PlotType.PRISON) {
                prisonPlotIds.add(plot.getPlotId());
            }
        }
        LOGGER.info("Prisons loaded: {}", prisonPlotIds.size());
    }

    public void registerPrison(String plotId) {
        PlotRegion plot = PlotManager.getPlot(plotId);
        if (plot != null && plot.getType() == PlotType.PRISON) {
            if (!prisonPlotIds.contains(plotId)) {
                prisonPlotIds.add(plotId);
                LOGGER.info("Prison registered: {}", plotId);
            }
        }
    }

    public PlotRegion getDefaultPrison() {
        if (prisonPlotIds.isEmpty()) {
            loadPrisonPlots();
        }
        if (prisonPlotIds.isEmpty()) return null;
        return PlotManager.getPlot(prisonPlotIds.get(0));
    }

    public PrisonCell findAvailableCell(int wantedLevel) {
        PlotRegion prison = getDefaultPrison();
        if (prison == null) {
            LOGGER.warn("No prison registered!");
            return null;
        }

        List<PrisonCell> cells = new ArrayList<>();
        for (var area : prison.getSubAreas()) {
            if (area instanceof PrisonCell cell) {
                cells.add(cell);
            }
        }

        cells.sort(Comparator.comparingInt(PrisonCell::getCellNumber));

        for (PrisonCell cell : cells) {
            if (cell.isFree()) {
                return cell;
            }
        }

        if (!cells.isEmpty()) {
            LOGGER.warn("All cells occupied! Using cell 1 for overcrowding.");
            return cells.get(0);
        }

        return null;
    }

    public boolean imprisonPlayer(ServerPlayer player, int wantedLevel) {
        UUID playerId = player.getUUID();

        if (isPrisoner(playerId)) {
            LOGGER.warn("Player {} is already imprisoned!", player.getName().getString());
            return false;
        }

        PrisonCell cell = findAvailableCell(wantedLevel);
        if (cell == null) {
            player.sendSystemMessage(Component.translatable(
                "manager.prison.no_prison_available"));
            return false;
        }

        int jailSeconds = wantedLevel * JAIL_SECONDS_PER_WANTED_LEVEL;
        long jailTicks = jailSeconds * 20L;
        long currentTick = player.level().getGameTime();
        long releaseTime = currentTick + jailTicks;
        long bailAvailableAtTick = currentTick + (long)(jailTicks * BAIL_AVAILABLE_AFTER);

        double bail = wantedLevel * BAIL_MULTIPLIER;
        double playerBalance = EconomyManager.getBalance(playerId);

        PrisonerData data = new PrisonerData();
        data.playerId = playerId;
        data.playerName = player.getName().getString();
        data.prisonPlotId = cell.getParentPlotId();
        data.cellNumber = cell.getCellNumber();
        data.setCellSpawn(cell.getSpawnPosition());
        data.originalWantedLevel = wantedLevel;
        data.arrestTime = System.currentTimeMillis();
        data.releaseTime = releaseTime;
        data.totalSentenceTicks = jailTicks;
        data.bailAmount = bail;
        data.lastOnlineTime = System.currentTimeMillis();

        cell.assignInmate(playerId, releaseTime);
        prisoners.put(playerId, data);

        BlockPos spawn = cell.getSpawnPosition();
        player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);

        applyPrisonEffects(player, jailSeconds);

        player.getPersistentData().putBoolean("IsInPrison", true);
        player.getPersistentData().putLong("JailReleaseTime", releaseTime);
        player.getPersistentData().putInt("JailCellNumber", cell.getCellNumber());

        PrisonNetworkHandler.sendToPlayer(new OpenPrisonScreenPacket(
            cell.getCellNumber(),
            jailTicks,
            releaseTime,
            bail,
            playerBalance,
            bailAvailableAtTick
        ), player);

        LOGGER.info("Player {} imprisoned in cell {} for {} seconds",
            data.playerName, cell.getCellNumber(), jailSeconds);

        savePrisonerData();
        return true;
    }

    /**
     * Sperrt Spieler wegen Schulden ein
     * @param player Der Spieler
     * @param minutes Gefängniszeit in Minuten
     * @param debtAmount Die Schuldenhöhe (für Logging)
     * @return true wenn erfolgreich
     */
    public static boolean imprisonPlayerForDebt(ServerPlayer player, int minutes, double debtAmount) {
        UUID playerId = player.getUUID();

        PrisonManager instance = getInstance();

        if (instance.isPrisoner(playerId)) {
            LOGGER.warn("Player {} is already imprisoned!", player.getName().getString());
            return false;
        }

        PrisonCell cell = instance.findAvailableCell(1); // Wanted level 1 für Schulden-Zelle
        if (cell == null) {
            player.sendSystemMessage(Component.translatable(
                "manager.prison.no_prison_available"));
            return false;
        }

        int jailSeconds = minutes * 60;
        long jailTicks = jailSeconds * 20L;
        long currentTick = player.level().getGameTime();
        long releaseTime = currentTick + jailTicks;

        // KEIN Bail für Schulden-Gefängnis!
        double bail = 0.0;
        double playerBalance = 0.0;
        long bailAvailableAtTick = Long.MAX_VALUE; // Nie verfügbar

        PrisonerData data = new PrisonerData();
        data.playerId = playerId;
        data.playerName = player.getName().getString();
        data.prisonPlotId = cell.getParentPlotId();
        data.cellNumber = cell.getCellNumber();
        data.setCellSpawn(cell.getSpawnPosition());
        data.originalWantedLevel = 0; // Kein Crime, nur Schulden
        data.arrestTime = System.currentTimeMillis();
        data.releaseTime = releaseTime;
        data.totalSentenceTicks = jailTicks;
        data.bailAmount = 0.0; // Kein Bail möglich
        data.lastOnlineTime = System.currentTimeMillis();

        cell.assignInmate(playerId, releaseTime);
        instance.prisoners.put(playerId, data);

        BlockPos spawn = cell.getSpawnPosition();
        player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);

        instance.applyPrisonEffects(player, jailSeconds);

        player.getPersistentData().putBoolean("IsInPrison", true);
        player.getPersistentData().putLong("JailReleaseTime", releaseTime);
        player.getPersistentData().putInt("JailCellNumber", cell.getCellNumber());

        PrisonNetworkHandler.sendToPlayer(new OpenPrisonScreenPacket(
            cell.getCellNumber(),
            jailTicks,
            releaseTime,
            bail,
            playerBalance,
            bailAvailableAtTick
        ), player);

        LOGGER.warn("Player {} imprisoned for DEBT in cell {} for {} minutes (debt: {}€)",
            data.playerName, cell.getCellNumber(), minutes, debtAmount);

        instance.savePrisonerData();
        return true;
    }

    public void releasePlayer(ServerPlayer player, ReleaseReason reason) {
        UUID playerId = player.getUUID();
        PrisonerData data = prisoners.remove(playerId);

        if (data == null) return;

        PlotRegion prison = PlotManager.getPlot(data.prisonPlotId);
        if (prison != null) {
            for (var area : prison.getSubAreas()) {
                if (area instanceof PrisonCell cell && cell.getCellNumber() == data.cellNumber) {
                    cell.releaseInmate();
                    break;
                }
            }
        }

        removePrisonEffects(player);

        player.getPersistentData().remove("IsInPrison");
        player.getPersistentData().remove("JailReleaseTime");
        player.getPersistentData().remove("JailCellNumber");

        switch (reason) {
            case TIME_SERVED, ADMIN_RELEASE -> CrimeManager.clearWantedLevel(playerId);
            case BAIL_PAID -> CrimeManager.setWantedLevel(playerId,
                Math.max(0, data.originalWantedLevel - 2));
        }

        if (prison != null) {
            BlockPos exit = prison.getSpawnPosition();
            player.teleportTo(exit.getX() + 0.5, exit.getY(), exit.getZ() + 0.5);
        }

        PrisonNetworkHandler.sendToPlayer(new ClosePrisonScreenPacket(reason.displayName), player);

        LOGGER.info("Player {} released: {}", data.playerName, reason.displayName);

        offlineRemainingTime.remove(playerId);
        savePrisonerData();
    }

    public boolean isBailAvailable(UUID playerId, long currentTick) {
        PrisonerData data = prisoners.get(playerId);
        if (data == null) return false;

        long startTick = data.releaseTime - data.totalSentenceTicks;
        long servedTicks = currentTick - startTick;
        long requiredTicks = (long)(data.totalSentenceTicks * BAIL_AVAILABLE_AFTER);

        return servedTicks >= requiredTicks;
    }

    public boolean payBail(ServerPlayer player) {
        UUID playerId = player.getUUID();
        PrisonerData data = prisoners.get(playerId);

        if (data == null) {
            player.sendSystemMessage(Component.translatable("message.prison.not_in_prison"));
            return false;
        }

        if (!isBailAvailable(playerId, player.level().getGameTime())) {
            player.sendSystemMessage(Component.translatable("message.prison.bail_not_available"));
            return false;
        }

        double balance = EconomyManager.getBalance(playerId);
        if (balance < data.bailAmount) {
            player.sendSystemMessage(Component.translatable(
                "manager.prison.insufficient_funds",
                (int)data.bailAmount, (int)balance));
            return false;
        }

        EconomyManager.withdraw(playerId, data.bailAmount);
        data.bailPaid = true;

        releasePlayer(player, ReleaseReason.BAIL_PAID);
        return true;
    }

    public void onPlayerLogout(UUID playerId, long currentTick) {
        PrisonerData data = prisoners.get(playerId);
        if (data == null) return;

        long remainingTicks = Math.max(0, data.releaseTime - currentTick);
        offlineRemainingTime.put(playerId, remainingTicks);
        data.lastOnlineTime = System.currentTimeMillis();

        LOGGER.info("Prisoner {} offline. Remaining: {} ticks", data.playerName, remainingTicks);
        savePrisonerData();
    }

    public void onPlayerLogin(ServerPlayer player) {
        UUID playerId = player.getUUID();
        PrisonerData data = prisoners.get(playerId);

        if (data == null) return;

        Long remainingTicks = offlineRemainingTime.remove(playerId);
        if (remainingTicks == null) {
            remainingTicks = data.totalSentenceTicks;
        }

        long currentTick = player.level().getGameTime();
        data.releaseTime = currentTick + remainingTicks;
        long bailAvailableAtTick = currentTick + (long)(remainingTicks * BAIL_AVAILABLE_AFTER);

        BlockPos cellSpawn = data.getCellSpawn();
        player.teleportTo(cellSpawn.getX() + 0.5, cellSpawn.getY(), cellSpawn.getZ() + 0.5);

        player.getPersistentData().putBoolean("IsInPrison", true);
        player.getPersistentData().putLong("JailReleaseTime", data.releaseTime);

        int remainingSeconds = (int)(remainingTicks / 20);
        applyPrisonEffects(player, remainingSeconds);

        double playerBalance = EconomyManager.getBalance(playerId);
        PrisonNetworkHandler.sendToPlayer(new OpenPrisonScreenPacket(
            data.cellNumber,
            data.totalSentenceTicks,
            data.releaseTime,
            data.bailAmount,
            playerBalance,
            bailAvailableAtTick
        ), player);

        LOGGER.info("Prisoner {} online. Remaining: {} seconds", data.playerName, remainingSeconds);
    }

    public void onServerTick(long currentTick, net.minecraft.server.level.ServerLevel level) {
        for (var entry : new HashMap<>(prisoners).entrySet()) {
            UUID playerId = entry.getKey();
            PrisonerData data = entry.getValue();

            if (offlineRemainingTime.containsKey(playerId)) continue;

            if (currentTick >= data.releaseTime) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
                if (player != null) {
                    releasePlayer(player, ReleaseReason.TIME_SERVED);
                }
            }
        }
    }

    private void applyPrisonEffects(ServerPlayer player, int seconds) {
        int ticks = seconds * 20;

        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN, ticks, 1, false, false, true));
        player.addEffect(new MobEffectInstance(
            MobEffects.JUMP, ticks, 128, false, false, true));
        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SLOWDOWN, ticks, 4, false, false, true));
        player.addEffect(new MobEffectInstance(
            MobEffects.WEAKNESS, ticks, 4, false, false, true));
    }

    private void removePrisonEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.JUMP);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
    }

    public boolean isPrisoner(UUID playerId) {
        return prisoners.containsKey(playerId);
    }

    public PrisonerData getPrisonerData(UUID playerId) {
        return prisoners.get(playerId);
    }

    public Collection<PrisonerData> getAllPrisoners() {
        return Collections.unmodifiableCollection(prisoners.values());
    }

    public int getPrisonerCount() {
        return prisoners.size();
    }

    public void addCellToPrison(String prisonPlotId, PrisonCell cell) {
        PlotRegion prison = PlotManager.getPlot(prisonPlotId);
        if (prison != null && prison.getType() == PlotType.PRISON) {
            prison.addSubArea(cell);
            PlotManager.savePlots();
            LOGGER.info("Cell {} added to prison {}", cell.getCellNumber(), prisonPlotId);
        }
    }

    private void savePrisonerData() {
        try {
            File file = new File(SAVE_FILE);
            file.getParentFile().mkdirs();

            Map<String, PrisonerData> saveData = new HashMap<>();
            for (var entry : prisoners.entrySet()) {
                saveData.put(entry.getKey().toString(), entry.getValue());
            }

            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(saveData, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Error saving prisoner data", e);
        }
    }

    private void loadPrisonerData() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, PrisonerData>>(){}.getType();
            Map<String, PrisonerData> loadedData = GSON.fromJson(reader, type);

            if (loadedData != null) {
                for (var entry : loadedData.entrySet()) {
                    UUID uuid = UUID.fromString(entry.getKey());
                    prisoners.put(uuid, entry.getValue());
                    offlineRemainingTime.put(uuid,
                        Math.max(0, entry.getValue().releaseTime - System.currentTimeMillis() / 50));
                }
                LOGGER.info("Prisoners loaded: {}", prisoners.size());
            }
        } catch (Exception e) {
            LOGGER.error("Error loading prisoner data", e);
        }
    }
}
