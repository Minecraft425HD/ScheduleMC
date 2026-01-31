package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.GsonHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        // PERFORMANCE: load() wird nicht im Konstruktor aufgerufen, sondern separat nach Konstruktion.
        // Dies vermeidet File-I/O während der Objekterstellung und ermöglicht paralleles Laden.
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     * PERFORMANCE: load() wird nach Konstruktion aufgerufen statt im Konstruktor
     */
    public static TransactionHistory getInstance(MinecraftServer server) {
        TransactionHistory localRef = instance;
        if (localRef == null) {
            synchronized (TransactionHistory.class) {
                localRef = instance;
                if (localRef == null) {
                    localRef = new TransactionHistory(server);
                    localRef.load();
                    instance = localRef;
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
     * OPTIMIERT: Loop statt Stream (vermeidet Stream + Collectors.toList() Allokation)
     */
    public List<Transaction> getTransactionsByType(UUID playerUUID, TransactionType type) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return Collections.emptyList();
        }

        List<Transaction> result = new ArrayList<>();
        for (Transaction t : playerTransactions) {
            if (t.getType() == type) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Holt Transaktionen in einem Zeitraum
     * OPTIMIERT: Loop statt Stream (vermeidet Stream + Collectors.toList() Allokation)
     */
    public List<Transaction> getTransactionsBetween(UUID playerUUID, long startTime, long endTime) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return Collections.emptyList();
        }

        List<Transaction> result = new ArrayList<>();
        for (Transaction t : playerTransactions) {
            long ts = t.getTimestamp();
            if (ts >= startTime && ts <= endTime) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Berechnet Gesamteinnahmen eines Spielers
     * OPTIMIERT: Loop statt Stream (vermeidet Stream-Pipeline-Allokation)
     */
    public double getTotalIncome(UUID playerUUID) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return 0.0;
        }

        double sum = 0.0;
        for (Transaction t : playerTransactions) {
            double amount = t.getAmount();
            if (amount > 0) {
                sum += amount;
            }
        }
        return sum;
    }

    /**
     * Berechnet Gesamtausgaben eines Spielers
     * OPTIMIERT: Loop statt Stream (vermeidet Stream-Pipeline-Allokation)
     */
    public double getTotalExpenses(UUID playerUUID) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null) {
            return 0.0;
        }

        double sum = 0.0;
        for (Transaction t : playerTransactions) {
            double amount = t.getAmount();
            if (amount < 0) {
                sum += -amount;
            }
        }
        return sum;
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
     * Löscht ALLE Transaktionen ALLER Spieler (täglicher Reset)
     */
    public void clearAllTransactions() {
        int totalPlayers = transactions.size();
        int totalTransactions = countTotalTransactions();

        transactions.clear();
        needsSave = true;

        LOGGER.info("Daily transaction history reset: Cleared {} transactions from {} players",
            totalTransactions, totalPlayers);
    }

    /**
     * Rotiert alte Transaktionen (löscht Transaktionen älter als RETENTION_DAYS)
     * OPTIMIERUNG: Verhindert unbegrenztes Wachstum der Historie
     */
    public void rotateOldTransactions() {
        long cutoffTime = System.currentTimeMillis() - (TRANSACTION_RETENTION_DAYS * 86400000L);
        int totalRemoved = 0;
        int playersAffected = 0;

        // OPTIMIERT: Sammle leere Keys separat, um ConcurrentModificationException zu vermeiden
        List<UUID> emptyKeys = null;

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

            // Sammle leere Listen für spätere Entfernung
            if (playerTransactions.isEmpty()) {
                if (emptyKeys == null) emptyKeys = new ArrayList<>();
                emptyKeys.add(entry.getKey());
            }
        }

        // Entferne leere Einträge nach der Iteration
        if (emptyKeys != null) {
            for (UUID key : emptyKeys) {
                transactions.remove(key);
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
                LOGGER.info("Loaded {} transactions for {} players",
                    countTotalTransactions(), transactions.size());
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

                LOGGER.debug("Saved {} transactions for {} players",
                    countTotalTransactions(), transactions.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save transaction history", e);
        }
    }

    /**
     * Berechnet die Gesamtanzahl aller Transaktionen.
     * OPTIMIERT: Extrahierte Hilfsmethode (war vorher 3x dupliziert)
     */
    private int countTotalTransactions() {
        int total = 0;
        for (List<Transaction> list : transactions.values()) {
            total += list.size();
        }
        return total;
    }

    /**
     * Gibt Statistiken zurück
     */
    public String getStatistics() {
        return String.format("Transaction History: %d players, %d total transactions",
            transactions.size(), countTotalTransactions());
    }
}
