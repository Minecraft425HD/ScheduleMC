package de.rolandsw.schedulemc.npc.bank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet tägliche Überweisungslimits für Spieler
 */
public class TransferLimitTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static TransferLimitTracker instance;

    private final Map<UUID, DailyTransferData> dailyTransfers = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File saveFile;

    private long currentDay = 0;

    private TransferLimitTracker(MinecraftServer server) {
        this.saveFile = new File(server.getServerDirectory(), "config/plotmod_transfer_limits.json");
        load();
    }

    public static TransferLimitTracker getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new TransferLimitTracker(server);
        }
        return instance;
    }

    /**
     * Prüft ob Überweisung im Limit ist und aktualisiert bei Erfolg
     *
     * @param playerUUID UUID des Spielers
     * @param amount Überweisungsbetrag
     * @return true wenn im Limit, false wenn Limit überschritten
     */
    public boolean checkAndUpdateLimit(UUID playerUUID, double amount) {
        DailyTransferData data = dailyTransfers.computeIfAbsent(playerUUID,
            k -> new DailyTransferData(currentDay));

        // Reset wenn neuer Tag
        if (data.day != currentDay) {
            data = new DailyTransferData(currentDay);
            dailyTransfers.put(playerUUID, data);
        }

        double dailyLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();
        double newTotal = data.totalTransferred + amount;

        if (newTotal > dailyLimit) {
            return false;
        }

        data.totalTransferred = newTotal;
        save();
        return true;
    }

    /**
     * Gibt zurück wie viel vom Tageslimit noch verfügbar ist
     */
    public double getRemainingLimit(UUID playerUUID) {
        DailyTransferData data = dailyTransfers.get(playerUUID);

        if (data == null || data.day != currentDay) {
            return ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();
        }

        double dailyLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();
        return Math.max(0, dailyLimit - data.totalTransferred);
    }

    /**
     * Gibt zurück wie viel heute bereits überwiesen wurde
     */
    public double getTodayTransferred(UUID playerUUID) {
        DailyTransferData data = dailyTransfers.get(playerUUID);

        if (data == null || data.day != currentDay) {
            return 0.0;
        }

        return data.totalTransferred;
    }

    /**
     * Tick-Methode für Tag-Wechsel
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            // Alte Daten werden automatisch beim nächsten checkAndUpdateLimit resettet
            LOGGER.info("New day started: {}, transfer limits reset", currentDay);
        }
    }

    // ========== Persistence ==========

    private void load() {
        if (!saveFile.exists()) {
            LOGGER.info("No transfer limit data found, starting fresh");
            return;
        }

        try (FileReader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<Map<UUID, DailyTransferData>>(){}.getType();
            Map<UUID, DailyTransferData> loaded = gson.fromJson(reader, type);

            if (loaded != null) {
                dailyTransfers.putAll(loaded);
                LOGGER.info("Loaded transfer limits for {} players", dailyTransfers.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load transfer limits", e);
        }
    }

    private void save() {
        try {
            saveFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(dailyTransfers, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save transfer limits", e);
        }
    }

    /**
     * Daten-Klasse für tägliche Überweisungen
     */
    private static class DailyTransferData {
        long day;
        double totalTransferred;

        DailyTransferData(long day) {
            this.day = day;
            this.totalTransferred = 0.0;
        }
    }
}
