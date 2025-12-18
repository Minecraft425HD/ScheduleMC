package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Verwaltet Sparkonten aller Spieler
 */
public class SavingsAccountManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static SavingsAccountManager instance;

    private final Map<UUID, List<SavingsAccount>> accounts = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path savePath;
    private MinecraftServer server;

    private long currentDay = 0;

    private SavingsAccountManager(MinecraftServer server) {
        this.server = server;
        this.savePath = server.getServerDirectory().toPath().resolve("config").resolve("plotmod_savings.json");
        load();
    }

    public static SavingsAccountManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new SavingsAccountManager(server);
        }
        instance.server = server;
        return instance;
    }

    /**
     * Erstellt ein neues Sparkonto
     */
    public boolean createSavingsAccount(UUID playerUUID, double initialDeposit) {
        double minDeposit = ModConfigHandler.COMMON.SAVINGS_MIN_DEPOSIT.get();
        if (initialDeposit < minDeposit) {
            return false;
        }

        // Prüfe Gesamtsumme aller Sparkonten
        double totalSavings = getTotalSavings(playerUUID);
        double maxPerPlayer = ModConfigHandler.COMMON.SAVINGS_MAX_PER_PLAYER.get();
        if (totalSavings + initialDeposit > maxPerPlayer) {
            return false;
        }

        // Prüfe ob genug Geld auf Hauptkonto
        if (!EconomyManager.withdraw(playerUUID, initialDeposit, TransactionType.SAVINGS_DEPOSIT,
                "Sparkonto eröffnet")) {
            return false;
        }

        SavingsAccount account = new SavingsAccount(playerUUID, initialDeposit, currentDay);
        accounts.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(account);

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§a§l[SPARKONTO] Erfolgreich eröffnet!\n" +
                "§7Einlage: §e" + String.format("%.2f€", initialDeposit) + "\n" +
                "§7Zinssatz: §a5.0% §7pro Woche\n" +
                "§7Sperrfrist: §e4 Wochen\n" +
                "§7Konto-ID: §f" + account.getAccountId().substring(0, 8)
            ));
        }

        save();
        LOGGER.info("Savings account created for {}: {}€", playerUUID, initialDeposit);
        return true;
    }

    /**
     * Zahlt auf Sparkonto ein
     */
    public boolean depositToSavings(UUID playerUUID, String accountId, double amount) {
        SavingsAccount account = findAccount(playerUUID, accountId);
        if (account == null) {
            return false;
        }

        // Prüfe Limit
        double totalSavings = getTotalSavings(playerUUID);
        double maxPerPlayer = ModConfigHandler.COMMON.SAVINGS_MAX_PER_PLAYER.get();
        if (totalSavings + amount > maxPerPlayer) {
            return false;
        }

        // Ziehe von Hauptkonto ab
        if (!EconomyManager.withdraw(playerUUID, amount, TransactionType.SAVINGS_DEPOSIT,
                "Sparkonto-Einzahlung")) {
            return false;
        }

        account.deposit(amount);
        save();

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§a✓ Einzahlung erfolgreich!\n" +
                "§7Betrag: §e+" + String.format("%.2f€", amount) + "\n" +
                "§7Neuer Sparkonto-Stand: §6" + String.format("%.2f€", account.getBalance())
            ));
        }

        return true;
    }

    /**
     * Hebt von Sparkonto ab
     */
    public boolean withdrawFromSavings(UUID playerUUID, String accountId, double amount, boolean forced) {
        SavingsAccount account = findAccount(playerUUID, accountId);
        if (account == null) {
            return false;
        }

        if (!account.isUnlocked(currentDay) && !forced) {
            return false;
        }

        double penalty = 0;
        if (!account.isUnlocked(currentDay) && forced) {
            penalty = amount * 0.10;
        }

        if (account.withdraw(amount, currentDay, forced)) {
            double payout = amount - penalty;

            // Zahle auf Hauptkonto ein
            EconomyManager.deposit(playerUUID, payout, TransactionType.SAVINGS_WITHDRAW,
                "Sparkonto-Abhebung");

            // Strafe an Staatskasse
            if (penalty > 0) {
                StateAccount.getInstance(server).deposit((int) penalty, "Sparkonto-Strafe");
            }

            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                String message = "§a✓ Abhebung erfolgreich!\n" +
                    "§7Betrag: §e" + String.format("%.2f€", amount);

                if (penalty > 0) {
                    message += "\n§cVorzeitige Abhebung: -10% Strafe (" + String.format("%.2f€", penalty) + ")";
                    message += "\n§7Ausgezahlt: §e" + String.format("%.2f€", payout);
                }

                message += "\n§7Sparkonto-Stand: §6" + String.format("%.2f€", account.getBalance());
                player.sendSystemMessage(Component.literal(message));
            }

            save();
            return true;
        }

        return false;
    }

    /**
     * Schließt Sparkonto
     */
    public boolean closeSavingsAccount(UUID playerUUID, String accountId) {
        SavingsAccount account = findAccount(playerUUID, accountId);
        if (account == null) {
            return false;
        }

        boolean isUnlocked = account.isUnlocked(currentDay);
        double payout = account.close(currentDay);
        double penalty = isUnlocked ? 0 : account.getBalance() * 0.10;

        // Zahle auf Hauptkonto
        EconomyManager.deposit(playerUUID, payout, TransactionType.SAVINGS_WITHDRAW,
            "Sparkonto geschlossen");

        // Strafe an Staatskasse
        if (penalty > 0) {
            StateAccount.getInstance(server).deposit((int) penalty, "Sparkonto-Schließung (vorzeitig)");
        }

        // Entferne Konto
        List<SavingsAccount> playerAccounts = accounts.get(playerUUID);
        if (playerAccounts != null) {
            playerAccounts.remove(account);
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            String message = "§e[SPARKONTO] Geschlossen\n" +
                "§7Ausgezahlt: §a" + String.format("%.2f€", payout);

            if (penalty > 0) {
                message += "\n§cVorzeitige Schließung: -10% Strafe";
            }

            player.sendSystemMessage(Component.literal(message));
        }

        save();
        return true;
    }

    /**
     * Tick-Methode für Zinsen
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            processInterest();
        }
    }

    /**
     * Verarbeitet wöchentliche Zinsen
     */
    private void processInterest() {
        for (Map.Entry<UUID, List<SavingsAccount>> entry : accounts.entrySet()) {
            UUID playerUUID = entry.getKey();

            for (SavingsAccount account : entry.getValue()) {
                double interest = account.calculateAndPayInterest(currentDay);

                if (interest > 0) {
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "§a§l[SPARKONTO] Zinsen gutgeschrieben!\n" +
                            "§7Zinssatz: §a5.0%\n" +
                            "§7Betrag: §a+" + String.format("%.2f€", interest) + "\n" +
                            "§7Neuer Stand: §6" + String.format("%.2f€", account.getBalance())
                        ));
                    }

                    LOGGER.info("Savings interest paid: {}€ to {}", interest, playerUUID);
                }
            }
        }

        if (!accounts.isEmpty()) {
            save();
        }
    }

    /**
     * Gibt alle Sparkonten eines Spielers zurück
     */
    public List<SavingsAccount> getAccounts(UUID playerUUID) {
        return accounts.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Findet Sparkonto
     */
    @Nullable
    private SavingsAccount findAccount(UUID playerUUID, String accountId) {
        List<SavingsAccount> playerAccounts = accounts.get(playerUUID);
        if (playerAccounts == null) {
            return null;
        }

        return playerAccounts.stream()
            .filter(acc -> acc.getAccountId().startsWith(accountId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gesamtsumme aller Sparkonten
     */
    private double getTotalSavings(UUID playerUUID) {
        List<SavingsAccount> playerAccounts = accounts.get(playerUUID);
        if (playerAccounts == null) {
            return 0.0;
        }

        return playerAccounts.stream()
            .mapToDouble(SavingsAccount::getBalance)
            .sum();
    }

    // Persistence
    private void load() {
        if (!Files.exists(savePath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(savePath)) {
            Type type = new TypeToken<Map<UUID, List<SavingsAccount>>>(){}.getType();
            Map<UUID, List<SavingsAccount>> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                accounts.putAll(loaded);
                LOGGER.info("Loaded {} savings accounts", loaded.values().stream()
                    .mapToInt(List::size).sum());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load savings accounts", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                gson.toJson(accounts, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save savings accounts", e);
        }
    }

    public static double getMaxSavingsPerPlayer() {
        return ModConfigHandler.COMMON.SAVINGS_MAX_PER_PLAYER.get();
    }

    public static double getMinDeposit() {
        return ModConfigHandler.COMMON.SAVINGS_MIN_DEPOSIT.get();
    }
}
