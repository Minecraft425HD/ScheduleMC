package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import java.util.stream.Collectors;

/**
 * Verwaltet die Transaktionshistorie aller Spieler
 * Thread-safe mit ConcurrentHashMap
 */
public class TransactionHistory {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static TransactionHistory instance;

    private static final String FILE_NAME = "plotmod_transactions.json";
    private static final int MAX_TRANSACTIONS_PER_PLAYER = 1000; // Verhindert unbegrenztes Wachstum

    private final Map<UUID, List<Transaction>> transactions = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path savePath;

    private boolean needsSave = false;

    private TransactionHistory(MinecraftServer server) {
        this.savePath = server.getServerDirectory().toPath().resolve("config").resolve(FILE_NAME);
        load();
    }

    public static TransactionHistory getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new TransactionHistory(server);
        }
        return instance;
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
     */
    public List<Transaction> getRecentTransactions(UUID playerUUID, int limit) {
        List<Transaction> playerTransactions = transactions.get(playerUUID);
        if (playerTransactions == null || playerTransactions.isEmpty()) {
            return Collections.emptyList();
        }

        // Rückwärts sortieren (neueste zuerst)
        List<Transaction> sorted = new ArrayList<>(playerTransactions);
        sorted.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        return sorted.stream().limit(limit).collect(Collectors.toList());
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
