package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Sparkonten aller Spieler
 * Refactored mit AbstractPersistenceManager
 */
public class SavingsAccountManager extends AbstractPersistenceManager<Map<UUID, List<SavingsAccount>>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile SavingsAccountManager instance;

    private final Map<UUID, List<SavingsAccount>> accounts = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private long currentDay = 0;

    private SavingsAccountManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_savings.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static SavingsAccountManager getInstance(MinecraftServer server) {
        SavingsAccountManager localRef = instance;
        if (localRef == null) {
            synchronized (SavingsAccountManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new SavingsAccountManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
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
            player.sendSystemMessage(Component.translatable("manager.savings.create_success",
                String.format("%.2f€", initialDeposit),
                account.getAccountId().substring(0, 8)
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
            player.sendSystemMessage(Component.translatable("manager.savings.deposit_success",
                String.format("%.2f€", amount),
                String.format("%.2f€", account.getBalance())
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
                if (penalty > 0) {
                    player.sendSystemMessage(Component.translatable("manager.savings.withdraw_success_early",
                        String.format("%.2f€", amount),
                        String.format("%.2f€", penalty),
                        String.format("%.2f€", payout),
                        String.format("%.2f€", account.getBalance())
                    ));
                } else {
                    player.sendSystemMessage(Component.translatable("manager.savings.withdraw_success",
                        String.format("%.2f€", amount),
                        String.format("%.2f€", account.getBalance())
                    ));
                }
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
            if (penalty > 0) {
                player.sendSystemMessage(Component.translatable("manager.savings.close_success_early",
                    String.format("%.2f€", payout)
                ));
            } else {
                player.sendSystemMessage(Component.translatable("manager.savings.close_success",
                    String.format("%.2f€", payout)
                ));
            }
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
                        player.sendSystemMessage(Component.translatable("manager.savings.interest_paid",
                            String.format("%.2f€", interest),
                            String.format("%.2f€", account.getBalance())
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

    // ========== AbstractPersistenceManager Implementation ==========

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, List<SavingsAccount>>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, List<SavingsAccount>> data) {
        accounts.clear();

        int invalidCount = 0;
        int correctedCount = 0;

        // NULL CHECK
        if (data == null) {
            LOGGER.warn("Null data loaded for savings accounts");
            invalidCount++;
            return;
        }

        // Check collection size
        if (data.size() > 10000) {
            LOGGER.warn("Savings accounts map size ({}) exceeds limit, potential corruption",
                data.size());
            correctedCount++;
        }

        for (Map.Entry<UUID, List<SavingsAccount>> entry : data.entrySet()) {
            try {
                UUID playerUUID = entry.getKey();
                List<SavingsAccount> accountList = entry.getValue();

                // NULL CHECK
                if (playerUUID == null) {
                    LOGGER.warn("Null player UUID in savings accounts, skipping");
                    invalidCount++;
                    continue;
                }
                if (accountList == null) {
                    LOGGER.warn("Null account list for player {}, skipping", playerUUID);
                    invalidCount++;
                    continue;
                }

                // VALIDATE LIST SIZE
                if (accountList.size() > 100) {
                    LOGGER.warn("Player {} has too many savings accounts ({}), truncating to 100",
                        playerUUID, accountList.size());
                    accountList = new ArrayList<>(accountList.subList(0, 100));
                    correctedCount++;
                }

                // VALIDATE ACCOUNTS - check for null entries
                List<SavingsAccount> validAccounts = new ArrayList<>();
                for (SavingsAccount account : accountList) {
                    if (account == null) {
                        LOGGER.warn("Player {} has null savings account, skipping", playerUUID);
                        invalidCount++;
                        continue;
                    }
                    validAccounts.add(account);
                }

                if (validAccounts.size() != accountList.size()) {
                    correctedCount++;
                }

                accounts.put(playerUUID, validAccounts);
            } catch (Exception e) {
                LOGGER.error("Error loading savings accounts for player {}", entry.getKey(), e);
                invalidCount++;
            }
        }

        // SUMMARY
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
    }

    @Override
    protected Map<UUID, List<SavingsAccount>> getCurrentData() {
        return new HashMap<>(accounts);
    }

    @Override
    protected String getComponentName() {
        return "SavingsAccountManager";
    }

    @Override
    protected String getHealthDetails() {
        int totalAccounts = accounts.values().stream().mapToInt(List::size).sum();
        return totalAccounts + " Sparkonten aktiv";
    }

    @Override
    protected void onCriticalLoadFailure() {
        accounts.clear();
    }

    // ========== Public API ==========

    public static double getMaxSavingsPerPlayer() {
        return ModConfigHandler.COMMON.SAVINGS_MAX_PER_PLAYER.get();
    }

    public static double getMinDeposit() {
        return ModConfigHandler.COMMON.SAVINGS_MIN_DEPOSIT.get();
    }
}
