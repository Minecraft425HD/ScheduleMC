package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.prison.PrisonManager;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Überziehungskredite (Dispo) - NEUES SYSTEM
 *
 * ABLAUF:
 * - Tag 1-6: Kontostand negativ, nur Zinsen
 * - Tag 7: Automatischer Ausgleich (Bargeld → Sparkonto → Girokonto)
 * - Tag 8-27: Countdown läuft
 * - Tag 28: Gefängnis (1000€ = 1 Minute)
 *
 * Refactored mit AbstractPersistenceManager
 */
public class OverdraftManager extends AbstractPersistenceManager<Map<String, Object>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile OverdraftManager instance;

    // Tracking: Wann wurde Spieler das erste Mal negativ?
    private final Map<UUID, Long> debtStartDay = new ConcurrentHashMap<>();

    // Tracking: Letzter Tag an dem Zinsen berechnet wurden
    private final Map<UUID, Long> lastInterestDay = new ConcurrentHashMap<>();

    // Tracking: Letzter Tag an dem Warnung gesendet wurde
    private final Map<UUID, Long> lastWarningDay = new ConcurrentHashMap<>();

    private MinecraftServer server;
    private long currentDay = 0;

    private OverdraftManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_overdraft.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static OverdraftManager getInstance(MinecraftServer server) {
        OverdraftManager localRef = instance;
        if (localRef == null) {
            synchronized (OverdraftManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new OverdraftManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    /**
     * Gibt Überziehungsbetrag zurück
     */
    public static double getOverdraftAmount(double balance) {
        if (balance >= 0) {
            return 0.0;
        }
        return Math.abs(balance);
    }

    /**
     * Berechnet wie viele Tage seit Start der Überziehung vergangen sind
     */
    public int getDaysSinceDebtStart(UUID playerUUID) {
        if (!debtStartDay.containsKey(playerUUID)) {
            return 0;
        }
        return (int)(currentDay - debtStartDay.get(playerUUID));
    }

    /**
     * Berechnet Tage bis zum nächsten Auto-Ausgleich (Tag 7)
     */
    public int getDaysUntilAutoRepay(UUID playerUUID) {
        int daysPassed = getDaysSinceDebtStart(playerUUID);
        if (daysPassed >= 7) {
            return 0; // Schon passiert oder vorbei
        }
        return 7 - daysPassed;
    }

    /**
     * Berechnet Tage bis zum Gefängnis (Tag 28)
     */
    public int getDaysUntilPrison(UUID playerUUID) {
        int daysPassed = getDaysSinceDebtStart(playerUUID);
        if (daysPassed >= 28) {
            return 0; // Sollte schon im Gefängnis sein
        }
        return 28 - daysPassed;
    }

    /**
     * Berechnet potentielle Gefängniszeit in Minuten
     */
    public double getPotentialPrisonMinutes(double debt) {
        if (debt <= 0) return 0.0;
        return Math.ceil(debt / 1000.0);
    }

    /**
     * Tick-Methode - Wird täglich aufgerufen
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            processOverdrafts();
        }
    }

    /**
     * Verarbeitet alle Überziehungen täglich
     */
    private void processOverdrafts() {
        Map<UUID, Double> balances = EconomyManager.getAllAccounts();

        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            UUID playerUUID = entry.getKey();
            double balance = entry.getValue();

            if (balance < 0) {
                // Spieler ist im Minus
                handleNegativeBalance(playerUUID, balance);
            } else {
                // Spieler ist positiv → Timer zurücksetzen
                resetDebtTimer(playerUUID);
            }
        }
    }

    /**
     * Behandelt negativen Kontostand
     */
    private void handleNegativeBalance(UUID playerUUID, double balance) {
        int daysPassed = getDaysSinceDebtStart(playerUUID);

        // Ersten Tag starten?
        if (daysPassed == 0) {
            startDebtTimer(playerUUID);
            sendInitialWarning(playerUUID, balance);
        }

        // Wöchentliche Zinsen (jede Woche!)
        chargeOverdraftInterest(playerUUID, balance);

        // Tag 7: Automatischer Ausgleich
        if (daysPassed == 7) {
            tryAutoRepay(playerUUID);
            // Nach Auto-Repay nochmal prüfen
            balance = EconomyManager.getBalance(playerUUID);
        }

        // Tag 7, 14, 21: Warnungen
        if (daysPassed == 7 || daysPassed == 14 || daysPassed == 21) {
            sendCountdownWarning(playerUUID, balance);
        }

        // Tag 28: GEFÄNGNIS!
        if (daysPassed >= 28) {
            sendToPrison(playerUUID, Math.abs(balance));
        }
    }

    /**
     * Startet den Schulden-Timer
     */
    private void startDebtTimer(UUID playerUUID) {
        if (!debtStartDay.containsKey(playerUUID)) {
            debtStartDay.put(playerUUID, currentDay);
            LOGGER.info("Debt timer started for {}", playerUUID);
            save();
        }
    }

    /**
     * Setzt den Schulden-Timer zurück (Konto wieder positiv)
     */
    private void resetDebtTimer(UUID playerUUID) {
        if (debtStartDay.containsKey(playerUUID)) {
            debtStartDay.remove(playerUUID);
            lastWarningDay.remove(playerUUID);
            LOGGER.info("Debt timer reset for {} (balance positive)", playerUUID);
            save();
        }
    }

    /**
     * Sendet erste Warnung (Tag 1)
     */
    private void sendInitialWarning(UUID playerUUID, double balance) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            double debt = Math.abs(balance);
            double interestRate = ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE.get();

            player.sendSystemMessage(Component.translatable("overdraft.warning.initial",
                String.format("%.2f€", debt),
                String.format("%.0f%%", interestRate * 100)
            ));
        }
    }

    /**
     * Sendet Countdown-Warnung (Tag 7, 14, 21)
     */
    private void sendCountdownWarning(UUID playerUUID, double balance) {
        Long lastWarning = lastWarningDay.get(playerUUID);
        if (lastWarning != null && currentDay - lastWarning < 1) {
            return; // Nur 1x pro Tag
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            int daysLeft = getDaysUntilPrison(playerUUID);
            double debt = Math.abs(balance);
            double prisonMinutes = getPotentialPrisonMinutes(debt);

            player.sendSystemMessage(Component.translatable("overdraft.warning.countdown",
                String.format("%.2f€", debt),
                String.valueOf(daysLeft),
                String.format("%.1f", prisonMinutes)
            ));
        }

        lastWarningDay.put(playerUUID, currentDay);
    }

    /**
     * Berechnet wöchentliche Überziehungszinsen
     */
    private void chargeOverdraftInterest(UUID playerUUID, double balance) {
        Long lastInterest = lastInterestDay.get(playerUUID);
        if (lastInterest != null && currentDay - lastInterest < 7) {
            return; // Nur 1x pro Woche
        }

        double interestRate = ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE.get();
        double overdraftAmount = getOverdraftAmount(balance);
        double interest = overdraftAmount * interestRate;

        // Ziehe Zinsen ab (macht Balance noch negativer)
        EconomyManager.setBalance(playerUUID, balance - interest, TransactionType.OVERDRAFT_FEE,
            "Überziehungszinsen (wöchentlich)");

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.translatable("overdraft.interest.charged",
                String.format("%.2f€", interest),
                String.format("%.2f€", balance - interest)
            ));
        }

        lastInterestDay.put(playerUUID, currentDay);
        LOGGER.info("Overdraft interest charged: {}€ to {}", interest, playerUUID);
        save();
    }

    /**
     * PHASE 1: Automatischer Ausgleich (Tag 7)
     * Reihenfolge: Bargeld → Sparkonto → Girokonto
     */
    public void tryAutoRepay(UUID playerUUID) {
        double balance = EconomyManager.getBalance(playerUUID);
        if (balance >= 0) {
            resetDebtTimer(playerUUID);
            return; // Nicht mehr negativ
        }

        double debt = Math.abs(balance);
        double originalDebt = debt;

        LOGGER.info("Auto-Repay started for {}: debt={}€", playerUUID, debt);

        // Tracking für Benachrichtigung
        double usedFromWallet = 0.0;
        double usedFromSavings = 0.0;

        // 1. BARGELD nutzen
        double wallet = WalletManager.getBalance(playerUUID);
        if (wallet > 0) {
            double useWallet = Math.min(wallet, debt);
            WalletManager.removeMoney(playerUUID, useWallet);
            EconomyManager.deposit(playerUUID, useWallet, TransactionType.OVERDRAFT_REPAY_WALLET,
                "Auto-Ausgleich: Bargeld");
            debt -= useWallet;
            usedFromWallet = useWallet;
            LOGGER.info("Auto-Repay: Used {}€ from wallet", useWallet);
        }

        // 2. SPARKONTO nutzen (falls Bargeld nicht reicht)
        if (debt > 0 && server != null) {
            SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(server);
            if (savingsManager != null) {
                List<SavingsAccount> savingsAccounts = savingsManager.getAccounts(playerUUID);

                // Sortiere nach Balance (größte zuerst)
                savingsAccounts.sort((a, b) -> Double.compare(b.getBalance(), a.getBalance()));

                // Nehme von jedem Sparkonto ab (größte zuerst) bis Schulden beglichen
                for (SavingsAccount account : savingsAccounts) {
                    if (debt <= 0) break;

                    double accountBalance = account.getBalance();
                    if (accountBalance > 0) {
                        double useFromThisAccount = Math.min(accountBalance, debt);

                        // Abhebung durchführen (forced=true, auch wenn gesperrt)
                        boolean success = savingsManager.withdrawFromSavings(
                            playerUUID,
                            account.getAccountId(),
                            useFromThisAccount,
                            true  // forced - auch gesperrte Konten nutzen
                        );

                        if (success) {
                            // withdrawFromSavings zahlt bereits auf Girokonto ein
                            // Wir müssen nur tracken
                            usedFromSavings += useFromThisAccount;
                            debt -= useFromThisAccount;
                            LOGGER.info("Auto-Repay: Used {}€ from savings account {}",
                                useFromThisAccount, account.getAccountId());
                        }
                    }
                }

                if (usedFromSavings > 0) {
                    LOGGER.info("Auto-Repay: Total used from savings: {}€", usedFromSavings);
                }
            }
        }

        // Benachrichtigung an Spieler
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            double repaidAmount = originalDebt - debt;
            if (repaidAmount > 0) {
                player.sendSystemMessage(Component.translatable("overdraft.autorepay.executed",
                    String.format("%.2f€", repaidAmount),
                    String.format("%.2f€", usedFromWallet),
                    String.format("%.2f€", usedFromSavings),
                    String.format("%.2f€", Math.max(0, debt))
                ));
            } else {
                player.sendSystemMessage(Component.translatable("overdraft.autorepay.failed",
                    String.format("%.2f€", debt)
                ));
            }
        }

        // Prüfe ob jetzt positiv
        balance = EconomyManager.getBalance(playerUUID);
        if (balance >= 0) {
            resetDebtTimer(playerUUID);
            LOGGER.info("Auto-Repay SUCCESS: Debt cleared for {}", playerUUID);
        } else {
            LOGGER.info("Auto-Repay PARTIAL: Remaining debt {}€ for {}", Math.abs(balance), playerUUID);
        }

        save();
    }

    /**
     * PHASE 3: Gefängnis (Tag 28)
     * Berechnung: Pro 1000€ Schulden = 1 Minute
     */
    private void sendToPrison(UUID playerUUID, double debt) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

        if (player == null) {
            LOGGER.warn("Cannot send offline player {} to prison. Debt: {}€", playerUUID, debt);
            return; // Kann nur Online-Spieler einsperren
        }

        // Berechne Gefängniszeit
        int minutes = (int) Math.ceil(debt / 1000.0);

        LOGGER.warn("Sending {} to prison for {} minutes (debt: {}€)", player.getName().getString(), minutes, debt);

        // Nachricht VOR Gefängnis
        player.sendSystemMessage(Component.translatable("overdraft.prison.sent",
            String.format("%.2f€", debt),
            String.valueOf(minutes)
        ));

        // Ab ins Gefängnis!
        boolean imprisoned = PrisonManager.imprisonPlayerForDebt(player, minutes, debt);

        if (imprisoned) {
            // Schulden auf 0€ setzen (Strafe ist abgegolten)
            EconomyManager.setBalance(playerUUID, 0.0, TransactionType.PRISON_DEBT_CLEARED,
                "Schulden durch Gefängnisstrafe beglichen");

            // Timer zurücksetzen
            resetDebtTimer(playerUUID);

            LOGGER.info("Player {} imprisoned for debt. Debt cleared.", player.getName().getString());
        } else {
            LOGGER.error("Failed to imprison player {} for debt!", player.getName().getString());
            // Fallback: Nochmal in 1 Tag versuchen (nicht resetten)
        }

        save();
    }

    /**
     * Gibt umfassende Info über Dispo-Situation
     */
    public Component getOverdraftInfo(UUID playerUUID) {
        double balance = EconomyManager.getBalance(playerUUID);
        double debt = getOverdraftAmount(balance);
        double interestRate = ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE.get();

        if (debt == 0) {
            return Component.translatable("overdraft.info.healthy",
                String.format("%.0f", interestRate * 100)
            );
        }

        int daysPassed = getDaysSinceDebtStart(playerUUID);
        int daysUntilPrison = getDaysUntilPrison(playerUUID);
        double prisonMinutes = getPotentialPrisonMinutes(debt);

        return Component.translatable("overdraft.info.overdrawn",
            String.format("%.2f", balance),
            String.format("%.2f", debt),
            String.valueOf(daysPassed),
            String.valueOf(daysUntilPrison),
            String.format("%.1f", prisonMinutes),
            String.format("%.0f", interestRate * 100)
        );
    }

    // ========== AbstractPersistenceManager Implementation ==========

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<String, Object>>(){}.getType();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onDataLoaded(Map<String, Object> data) {
        debtStartDay.clear();
        lastWarningDay.clear();
        lastInterestDay.clear();

        Object debtStartObj = data.get("debtStartDay");
        if (debtStartObj instanceof Map) {
            ((Map<String, Number>) debtStartObj).forEach((k, v) ->
                debtStartDay.put(UUID.fromString(k), v.longValue()));
        }

        Object warningObj = data.get("lastWarningDay");
        if (warningObj instanceof Map) {
            ((Map<String, Number>) warningObj).forEach((k, v) ->
                lastWarningDay.put(UUID.fromString(k), v.longValue()));
        }

        Object interestObj = data.get("lastInterestDay");
        if (interestObj instanceof Map) {
            ((Map<String, Number>) interestObj).forEach((k, v) ->
                lastInterestDay.put(UUID.fromString(k), v.longValue()));
        }
    }

    @Override
    protected Map<String, Object> getCurrentData() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Long> debtStartMap = new HashMap<>();
        debtStartDay.forEach((k, v) -> debtStartMap.put(k.toString(), v));
        data.put("debtStartDay", debtStartMap);

        Map<String, Long> warningMap = new HashMap<>();
        lastWarningDay.forEach((k, v) -> warningMap.put(k.toString(), v));
        data.put("lastWarningDay", warningMap);

        Map<String, Long> interestMap = new HashMap<>();
        lastInterestDay.forEach((k, v) -> interestMap.put(k.toString(), v));
        data.put("lastInterestDay", interestMap);

        return data;
    }

    @Override
    protected String getComponentName() {
        return "OverdraftManager";
    }

    @Override
    protected String getHealthDetails() {
        return Component.translatable("manager.overdraft.health_details",
            String.valueOf(debtStartDay.size())
        ).getString();
    }

    @Override
    protected void onCriticalLoadFailure() {
        debtStartDay.clear();
        lastWarningDay.clear();
        lastInterestDay.clear();
    }
}
