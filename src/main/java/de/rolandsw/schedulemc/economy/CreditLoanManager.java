package de.rolandsw.schedulemc.economy;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Verwaltet das erweiterte Kredit-System mit Bonitätsprüfung
 * Ersetzt den alten LoanManager für das neue NPC-basierte System
 */
public class CreditLoanManager extends AbstractPersistenceManager<Map<UUID, CreditLoan>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile CreditLoanManager instance;

    private static final double MIN_BALANCE_FOR_LOAN = 1000.0;

    private final Map<UUID, CreditLoan> activeLoans = new ConcurrentHashMap<>();
    private MinecraftServer server;
    private long currentDay = 0;

    private CreditLoanManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_credit_loans.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static CreditLoanManager getInstance(MinecraftServer server) {
        CreditLoanManager localRef = instance;
        if (localRef == null) {
            synchronized (CreditLoanManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new CreditLoanManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    /**
     * Beantragt einen Kredit mit dynamischem Zinssatz basierend auf Bonität
     */
    public boolean applyForLoan(UUID playerUUID, CreditLoan.CreditLoanType type) {
        // Prüfe ob bereits Kredit aktiv
        if (hasActiveLoan(playerUUID)) {
            return false;
        }

        // Prüfe Mindestkontostand
        double balance = EconomyManager.getBalance(playerUUID);
        if (balance < MIN_BALANCE_FOR_LOAN) {
            return false;
        }

        // Hole CreditScoreManager für dynamische Zinsen
        CreditScoreManager scoreManager = CreditScoreManager.getInstance(server);

        // Prüfe Bonität
        if (!scoreManager.canTakeLoan(playerUUID, type)) {
            return false;
        }

        // Berechne effektiven Zinssatz
        double effectiveRate = scoreManager.getEffectiveInterestRate(playerUUID, type);

        // Erstelle Kredit mit dynamischem Zinssatz
        CreditLoan loan = new CreditLoan(playerUUID, type, effectiveRate, currentDay);
        activeLoans.put(playerUUID, loan);

        // Zahle Kreditsumme aus
        EconomyManager.deposit(playerUUID, type.getBaseAmount(), TransactionType.LOAN_DISBURSEMENT,
            "Kredit: " + type.getDisplayNameDE());

        LOGGER.info("Credit loan granted: {} {} ({}% interest) to {}",
            type.name(), type.getBaseAmount(), String.format("%.1f", effectiveRate * 100), playerUUID);

        save();
        return true;
    }

    /**
     * Tick-Methode für tägliche Abbuchungen
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            processDailyPayments();
        }
    }

    /**
     * Verarbeitet tägliche Ratenzahlungen
     */
    private void processDailyPayments() {
        List<UUID> toRemove = new ArrayList<>();
        CreditScoreManager scoreManager = CreditScoreManager.getInstance(server);

        for (Map.Entry<UUID, CreditLoan> entry : activeLoans.entrySet()) {
            UUID playerUUID = entry.getKey();
            CreditLoan loan = entry.getValue();

            double payment = Math.min(loan.getDailyPayment(), loan.getRemaining());

            // Versuche Abbuchung
            if (EconomyManager.withdraw(playerUUID, payment, TransactionType.LOAN_REPAYMENT,
                    "Kredit-Rate: " + loan.getType().getDisplayNameDE())) {

                loan.payDailyInstallment();

                // Pünktliche Zahlung registrieren (verbessert Kredit-Score)
                scoreManager.recordOnTimePayment(playerUUID);

                // Prüfe ob abbezahlt
                if (loan.isRepaid()) {
                    toRemove.add(playerUUID);

                    // Kredit erfolgreich abgeschlossen
                    scoreManager.recordLoanCompleted(playerUUID,
                        loan.getPrincipal() + loan.getTotalInterest());

                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "§a§l[KREDIT] Vollständig abbezahlt!\n" +
                            "§7Kredit: §e" + loan.getType().getDisplayNameDE() + "\n" +
                            "§aDu bist nun schuldenfrei!\n" +
                            "§7Dein Kredit-Score wurde verbessert!"
                        ));
                    }
                }
            } else {
                // Nicht genug Geld - Warnung und Score-Verschlechterung
                scoreManager.recordMissedPayment(playerUUID);

                ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                if (player != null) {
                    player.sendSystemMessage(Component.literal(
                        "§c§l[KREDIT] Zahlung fehlgeschlagen!\n" +
                        "§7Fällig: §c" + String.format("%.2f€", payment) + "\n" +
                        "§7Kontostand: §e" + String.format("%.2f€", EconomyManager.getBalance(playerUUID)) + "\n" +
                        "§c⚠ Dein Kredit-Score wurde verschlechtert!\n" +
                        "§7Zahle Geld ein um weitere Strafen zu vermeiden!"
                    ));
                }
            }
        }

        // Entferne abbezahlte Kredite
        toRemove.forEach(activeLoans::remove);

        if (!toRemove.isEmpty()) {
            save();
        }
    }

    /**
     * Zahlt Kredit vorzeitig zurück
     */
    public boolean repayLoan(UUID playerUUID) {
        CreditLoan loan = activeLoans.get(playerUUID);
        if (loan == null) {
            return false;
        }

        double remaining = loan.getRemaining();

        if (EconomyManager.withdraw(playerUUID, remaining, TransactionType.LOAN_REPAYMENT,
                "Kredit-Vollauszahlung: " + loan.getType().getDisplayNameDE())) {
            loan.payOff();
            activeLoans.remove(playerUUID);

            // Kredit erfolgreich abgeschlossen - verbessert Score
            CreditScoreManager scoreManager = CreditScoreManager.getInstance(server);
            scoreManager.recordLoanCompleted(playerUUID,
                loan.getPrincipal() + loan.getTotalInterest());

            save();
            return true;
        }

        return false;
    }

    public boolean hasActiveLoan(UUID playerUUID) {
        return activeLoans.containsKey(playerUUID);
    }

    @Nullable
    public CreditLoan getLoan(UUID playerUUID) {
        return activeLoans.get(playerUUID);
    }

    public long getCurrentDay() {
        return currentDay;
    }

    // ========== AbstractPersistenceManager Implementation ==========

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, CreditLoan>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, CreditLoan> data) {
        activeLoans.clear();
        if (data != null) {
            activeLoans.putAll(data);
        }
    }

    @Override
    protected Map<UUID, CreditLoan> getCurrentData() {
        return new HashMap<>(activeLoans);
    }

    @Override
    protected String getComponentName() {
        return "CreditLoanManager";
    }

    @Override
    protected String getHealthDetails() {
        return activeLoans.size() + " aktive Kredite";
    }

    @Override
    protected void onCriticalLoadFailure() {
        activeLoans.clear();
    }
}
