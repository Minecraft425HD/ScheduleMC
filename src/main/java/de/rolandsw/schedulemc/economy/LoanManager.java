package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Kredite für Spieler
 * Refactored mit AbstractPersistenceManager
 */
public class LoanManager extends AbstractPersistenceManager<Map<UUID, Loan>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile LoanManager instance;

    private static final double MIN_BALANCE_FOR_LOAN = 1000.0;
    private static final int MIN_PLAYTIME_DAYS = 7;

    private final Map<UUID, Loan> activeLoans = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private long currentDay = 0;

    private LoanManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_loans.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static LoanManager getInstance(@Nonnull MinecraftServer server) {
        LoanManager localRef = instance;
        if (localRef == null) {
            synchronized (LoanManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new LoanManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    /**
     * Beantragt einen Kredit
     */
    public boolean applyForLoan(@Nonnull UUID playerUUID, Loan.LoanType type) {
        // Prüfe ob bereits Kredit aktiv
        if (hasActiveLoan(playerUUID)) {
            return false;
        }

        // Prüfe Mindestkontostand
        double balance = EconomyManager.getBalance(playerUUID);
        if (balance < MIN_BALANCE_FOR_LOAN) {
            return false;
        }

        // Erstelle Kredit
        Loan loan = new Loan(playerUUID, type, currentDay);
        activeLoans.put(playerUUID, loan);

        // Zahle Kreditsumme aus
        EconomyManager.deposit(playerUUID, type.getAmount(), TransactionType.LOAN_DISBURSEMENT,
            "Kredit: " + type.name());

        LOGGER.info("Loan granted: {} {} to {}", type.name(), type.getAmount(), playerUUID);

        // Benachrichtige Spieler
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§a§l[KREDIT] Bewilligt!\n" +
                "§7Typ: §e" + type.name() + "\n" +
                "§7Betrag: §a+" + String.format("%.2f€", type.getAmount()) + "\n" +
                "§7Zinssatz: §c" + (int)(type.getInterestRate() * 100) + "%\n" +
                "§7Laufzeit: §e" + type.getDurationDays() + " Tage\n" +
                "§7Tägliche Rate: §c-" + String.format("%.2f€", loan.getDailyPayment())
            ));
        }

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

        for (Map.Entry<UUID, Loan> entry : activeLoans.entrySet()) {
            UUID playerUUID = entry.getKey();
            Loan loan = entry.getValue();

            double payment = Math.min(loan.getDailyPayment(), loan.getRemaining());

            // Versuche Abbuchung
            if (EconomyManager.withdraw(playerUUID, payment, TransactionType.LOAN_REPAYMENT,
                    "Kredit-Rate: " + loan.getType().name())) {

                loan.payDailyInstallment();

                // Prüfe ob abbezahlt
                if (loan.isRepaid()) {
                    toRemove.add(playerUUID);

                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "§a§l[KREDIT] Vollständig abbezahlt!\n" +
                            "§7Kredit: §e" + loan.getType().name() + "\n" +
                            "§aDu bist nun schuldenfrei!"
                        ));
                    }
                }
            } else {
                // Nicht genug Geld - Warnung
                ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                if (player != null) {
                    player.sendSystemMessage(Component.literal(
                        "§c§l[KREDIT] Zahlung fehlgeschlagen!\n" +
                        "§7Fällig: §c" + String.format("%.2f€", payment) + "\n" +
                        "§7Kontostand: §e" + String.format("%.2f€", EconomyManager.getBalance(playerUUID)) + "\n" +
                        "§cZahle Geld ein um Strafen zu vermeiden!"
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
    public boolean repayLoan(@Nonnull UUID playerUUID) {
        Loan loan = activeLoans.get(playerUUID);
        if (loan == null) {
            return false;
        }

        double remaining = loan.getRemaining();

        if (EconomyManager.withdraw(playerUUID, remaining, TransactionType.LOAN_REPAYMENT,
                "Kredit-Vollauszahlung: " + loan.getType().name())) {
            loan.payOff();
            activeLoans.remove(playerUUID);

            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                    "§a§l[KREDIT] Vorzeitig abbezahlt!\n" +
                    "§7Betrag: §c-" + String.format("%.2f€", remaining)
                ));
            }

            save();
            return true;
        }

        return false;
    }

    public boolean hasActiveLoan(UUID playerUUID) {
        return activeLoans.containsKey(playerUUID);
    }

    @Nullable
    public Loan getLoan(UUID playerUUID) {
        return activeLoans.get(playerUUID);
    }

    // ========== AbstractPersistenceManager Implementation ==========

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, Loan>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, Loan> data) {
        activeLoans.clear();
        activeLoans.putAll(data);
    }

    @Override
    protected Map<UUID, Loan> getCurrentData() {
        return new HashMap<>(activeLoans);
    }

    @Override
    protected String getComponentName() {
        return "LoanManager";
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
