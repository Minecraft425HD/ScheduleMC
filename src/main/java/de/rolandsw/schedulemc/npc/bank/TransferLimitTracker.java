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
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile TransferLimitTracker instance;

    private final Map<UUID, DailyTransferData> dailyTransfers = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File saveFile;

    private long currentDay = 0;

    private TransferLimitTracker(MinecraftServer server) {
        this.saveFile = new File(server.getServerDirectory(), "config/plotmod_transfer_limits.json");
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static TransferLimitTracker getInstance(MinecraftServer server) {
        TransferLimitTracker localRef = instance;
        if (localRef == null) {
            synchronized (TransferLimitTracker.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new TransferLimitTracker(server);
                }
            }
        }
        return localRef;
    }

    /**
     * Prüft ob Überweisung im Limit ist und aktualisiert bei Erfolg
     *
     * SICHERHEIT FIX: Atomare Check-And-Update Operation verhindert TOCTOU Race Condition
     * - Vorher: Thread 1 und 2 lesen beide totalTransferred=0, beide updaten auf 100 -> Limit umgangen!
     * - Jetzt: Synchronized block garantiert atomare Operation
     *
     * @param playerUUID UUID des Spielers
     * @param amount Überweisungsbetrag
     * @return true wenn im Limit, false wenn Limit überschritten
     */
    public boolean checkAndUpdateLimit(UUID playerUUID, double amount) {
        double dailyLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();

        // SICHERHEIT: Atomare Operation mit synchronized auf dem spezifischen Player-Eintrag
        // Nutzt ConcurrentHashMap.compute() für lock-freie Concurrency auf Map-Ebene
        // und synchronized auf Data-Objekt-Ebene für atomare Check-Update
        final boolean[] success = {false};

        dailyTransfers.compute(playerUUID, (key, data) -> {
            // Erstelle neue Data wenn nicht vorhanden oder Tag gewechselt
            if (data == null || data.day != currentDay) {
                data = new DailyTransferData(currentDay);
            }

            // ATOMARE Check-And-Update Operation
            synchronized (data) {
                double newTotal = data.totalTransferred + amount;

                if (newTotal <= dailyLimit) {
                    data.totalTransferred = newTotal;
                    success[0] = true;
                } else {
                    success[0] = false;
                }
            }

            return data;
        });

        if (success[0]) {
            save();
        }

        return success[0];
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
