package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.GsonHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Collections;
import java.util.UUID;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * Verwaltet die Transaktionshistorie aller Spieler
 * Thread-safe mit ConcurrentHashMap
 */
public class TransactionHistory {
    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile TransactionHistory instance;

    private static final String FILE_NAME = "plotmod_transactions.json";
    private static final int MAX_TRANSACTIONS_PER_PLAYER = 1000; // Verhindert unbegrenztes Wachstum
    private static final long TRANSACTION_RETENTION_DAYS = 90; // 90 Tage Aufbewahrung
    private static final long ROTATION_INTERVAL_TICKS = 72000; // Alle 60 Minuten (72000 ticks)

    private final Map<UUID, List<Transaction>> transactions = new ConcurrentHashMap<>();
    private final Gson gson = GsonHelper.get(); // Umgebungsabhängig: kompakt in Produktion
    private final Path savePath;

    private boolean needsSave = false;
    private long lastRotationTime = System.currentTimeMillis();

    private TransactionHistory(MinecraftServer server) {
        this.savePath = server.getServerDirectory().toPath().resolve("config").resolve(FILE_NAME);
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static TransactionHistory getInstance(MinecraftServer server) {
        TransactionHistory localRef = instance;
        if (localRef == null) {
            synchronized (TransactionHistory.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new TransactionHistory(server);
                }
            }
        }
        return localRef;
    }

    @Nullable
    public static TransactionHistory getInstance() {
        return instance;
    }

    /**
     * Fügt eine neue Transaktion hinzu
     */
    public void addTransaction(UUID playerUUID, Transaction transaction) {
        transactions.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(transaction);

        // Begrenze Anzahl pro Spieler
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions.size() > MAX_TRANSACTIONS_PER_PLAYER) {
            // Entferne älteste Transaktionen
            playerTransactions.subList(0, playerTransactions.size() - MAX_TRANSACTIONS_PER_PLAYER).clear();
        }

        needsSave = true;
    }

    /**
     * Holt die letzten N Transaktionen eines Spielers
     *
     * OPTIMIERT: Da Transaktionen chronologisch hinzugefügt werden,
     * nehmen wir einfach die letzten N Elemente (O(n) -> O(limit))
     * statt die gesamte Liste zu sortieren (O(n log n))
     */
    public List<Transaction> getRecentTransactions(UUID playerUUID, int limit) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null || playerTransactions.isEmpty()) {
            return Collections.emptyList();
        }

        int size = playerTransactions.size();
        int startIndex = Math.max(0, size - limit);

        // Erstelle Ergebnisliste mit umgekehrter Reihenfolge (neueste zuerst)
        List<Transaction> result = new ArrayList<>(Math.min(limit, size));
        for (int i = size - 1; i >= startIndex; i--) {
            result.add(playerTransactions.get(i));
        }

        return result;
    }

    /**
     * Holt alle Transaktionen eines Spielers
     */
    public List<Transaction> getAllTransactions(UUID playerUUID) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(playerTransactions);
    }

    /**
     * Holt Transaktionen eines Typs
     */
    public List<Transaction> getTransactionsByType(UUID playerUUID, TransactionType type) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return Collections.emptyList();
        }

        return playerTransactions.stream()
            .filter(t -> t.getType() == type)
            .collect(Collectors.toList());
    }

    /**
     * Holt Transaktionen in einem Zeitraum
     */
    public List<Transaction> getTransactionsBetween(UUID playerUUID, long startTime, long endTime) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return Collections.emptyList();
        }

        return playerTransactions.stream()
            .filter(t -> t.getTimestamp() >= startTime && t.getTimestamp() <= endTime)
            .collect(Collectors.toList());
    }

    /**
     * Berechnet Gesamteinnahmen eines Spielers
     */
    public double getTotalIncome(UUID playerUUID) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return 0.0;
        }

        return playerTransactions.stream()
            .filter(t -> t.getAmount() > 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    /**
     * Berechnet Gesamtausgaben eines Spielers
     */
    public double getTotalExpenses(UUID playerUUID) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return 0.0;
        }

        return playerTransactions.stream()
            .filter(t -> t.getAmount() < 0)
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
    }

    /**
     * Anzahl Transaktionen eines Spielers
     */
    public int getTransactionCount(UUID playerUUID) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        return playerTransactions == null ? 0 : playerTransactions.size();
    }

    /**
     * Löscht alle Transaktionen eines Spielers
     */
    public void clearTransactions(UUID playerUUID) {
        transactions.remove(playerUUID);
        needsSave = true;
        LOGGER.info("Cleared transaction history for player {}", playerUUID);
    }

    /**
     * Rotiert alte Transaktionen (löscht Transaktionen älter als RETENTION_DAYS)
     * OPTIMIERUNG: Verhindert unbegrenztes Wachstum der Historie
     */
    public void rotateOldTransactions() {
        long cutoffTime = System.currentTimeMillis() - (TRANSACTION_RETENTION_DAYS * 86400000L);
        int totalRemoved = 0;
        int playersAffected = 0;

        for (Map.Entry<UUID, List<Transaction>> entry : transactions.entrySet()) {
            List<Transaction> playerTransactions = entry.getValue();
            int sizeBefore = playerTransactions.size();

            // Entferne alte Transaktionen
            playerTransactions.removeIf(t -> t.getTimestamp() < cutoffTime);

            int removed = sizeBefore - playerTransactions.size();
            if (removed > 0) {
                totalRemoved += removed;
                playersAffected++;
            }

            // Entferne leere Listen
            if (playerTransactions.isEmpty()) {
                transactions.remove(entry.getKey());
            }
        }

        if (totalRemoved > 0) {
            needsSave = true;
            LOGGER.info("Rotation: {} alte Transaktionen von {} Spielern entfernt",
                totalRemoved, playersAffected);
        }

        lastRotationTime = System.currentTimeMillis();
    }

    /**
     * Prüft ob Rotation nötig ist und führt sie aus
     * Sollte regelmäßig aufgerufen werden (z.B. alle 60 Minuten)
     */
    public void checkAndRotate() {
        long timeSinceLastRotation = System.currentTimeMillis() - lastRotationTime;
        long rotationIntervalMs = ROTATION_INTERVAL_TICKS * 50L; // Ticks zu MS

        if (timeSinceLastRotation >= rotationIntervalMs) {
            rotateOldTransactions();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    private void load() {
        if (!Files.exists(savePath)) {
            LOGGER.info("No transaction history file found, starting fresh");
            return;
        }

        try (Reader reader = Files.newBufferedReader(savePath)) {
            Type type = new TypeToken<Map<UUID, List<Transaction>>>(){}.getType();
            Map<UUID, List<Transaction>> loaded = gson.fromJson(reader, type);

            if (loaded != null) {
                transactions.putAll(loaded);
                int totalTransactions = transactions.values().stream()
                    .mapToInt(List::size)
                    .sum();
                LOGGER.info("Loaded {} transactions for {} players",
                    totalTransactions, transactions.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load transaction history", e);
        }
    }

    public void save() {
        if (!needsSave) {
            return;
        }

        try {
            // Erstelle config Verzeichnis falls nicht vorhanden
            Files.createDirectories(savePath.getParent());

            try (Writer writer = Files.newBufferedWriter(savePath)) {
                gson.toJson(transactions, writer);
                needsSave = false;

                int totalTransactions = transactions.values().stream()
                    .mapToInt(List::size)
                    .sum();
                LOGGER.debug("Saved {} transactions for {} players",
                    totalTransactions, transactions.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save transaction history", e);
        }
    }

    /**
     * Gibt Statistiken zurück
     */
    public String getStatistics() {
        int totalPlayers = transactions.size();
        int totalTransactions = transactions.values().stream()
            .mapToInt(List::size)
            .sum();

        return String.format("Transaction History: %d players, %d total transactions",
            totalPlayers, totalTransactions);
    }
}
