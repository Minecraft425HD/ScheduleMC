package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Kredit-Scores für alle Spieler
 * Verwendet AbstractPersistenceManager für robuste Datenpersistenz
 */
public class CreditScoreManager extends AbstractPersistenceManager<Map<UUID, CreditScore>> {
    private static CreditScoreManager instance;

    private final Map<UUID, CreditScore> creditScores = new ConcurrentHashMap<>();
    private MinecraftServer server;
    private long currentDay = 0;

    // Balance-Update Throttling (nicht jeden Tick updaten)
    private int balanceUpdateCounter = 0;
    private static final int BALANCE_UPDATE_INTERVAL = 1200; // Alle 60 Sekunden (1200 Ticks)

    private CreditScoreManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_credit_scores.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    public static CreditScoreManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new CreditScoreManager(server);
        }
        instance.server = server;
        return instance;
    }

    /**
     * Gibt den CreditScore für einen Spieler zurück
     * Erstellt automatisch einen neuen Score falls nicht vorhanden
     */
    public CreditScore getOrCreateScore(UUID playerUUID) {
        return creditScores.computeIfAbsent(playerUUID, uuid -> {
            CreditScore newScore = new CreditScore(uuid, currentDay);
            save();
            return newScore;
        });
    }

    /**
     * Gibt den CreditScore für einen Spieler zurück (null wenn nicht vorhanden)
     */
    @Nullable
    public CreditScore getScore(UUID playerUUID) {
        return creditScores.get(playerUUID);
    }

    /**
     * Prüft ob ein Spieler einen bestimmten Kredittyp aufnehmen kann
     */
    public boolean canTakeLoan(UUID playerUUID, CreditLoan.CreditLoanType loanType) {
        CreditScore score = getOrCreateScore(playerUUID);

        // Prüfe maximalen Kreditbetrag basierend auf Rating
        double maxAmount = score.getMaxLoanAmount(currentDay);
        if (loanType.getBaseAmount() > maxAmount) {
            return false;
        }

        // Prüfe Rating-Anforderungen
        CreditScore.CreditRating rating = score.getRating(currentDay);
        CreditScore.CreditRating requiredRating = loanType.getRequiredRating();

        return rating.ordinal() <= requiredRating.ordinal(); // Niedrigerer Ordinal = besseres Rating
    }

    /**
     * Berechnet den effektiven Zinssatz für einen Kredit
     * Basiert auf Basis-Zinssatz und Kredit-Score-Modifikator
     */
    public double getEffectiveInterestRate(UUID playerUUID, CreditLoan.CreditLoanType loanType) {
        CreditScore score = getOrCreateScore(playerUUID);
        double modifier = score.getInterestRateModifier(currentDay);
        return loanType.getBaseInterestRate() * modifier;
    }

    /**
     * Registriert eine pünktliche Zahlung
     */
    public void recordOnTimePayment(UUID playerUUID) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordOnTimePayment();
        save();
    }

    /**
     * Registriert eine verpasste Zahlung
     */
    public void recordMissedPayment(UUID playerUUID) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordMissedPayment();
        save();
    }

    /**
     * Registriert einen erfolgreich abgeschlossenen Kredit
     */
    public void recordLoanCompleted(UUID playerUUID, double amountRepaid) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordLoanCompleted(amountRepaid);
        save();
        LOGGER.info("Credit score updated for {} - Loan completed, {} repaid", playerUUID, amountRepaid);
    }

    /**
     * Registriert einen Kreditausfall
     */
    public void recordLoanDefaulted(UUID playerUUID) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordLoanDefaulted();
        save();
        LOGGER.warn("Credit score updated for {} - Loan DEFAULTED", playerUUID);
    }

    /**
     * Tick-Methode für tägliche Updates und Balance-Tracking
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
        }

        // Balance-Update Throttling
        balanceUpdateCounter++;
        if (balanceUpdateCounter >= BALANCE_UPDATE_INTERVAL) {
            balanceUpdateCounter = 0;
            updateAllBalances();
        }
    }

    /**
     * Aktualisiert die durchschnittlichen Kontostände aller Spieler
     */
    private void updateAllBalances() {
        for (UUID playerUUID : creditScores.keySet()) {
            double balance = EconomyManager.getBalance(playerUUID);
            CreditScore score = creditScores.get(playerUUID);
            if (score != null) {
                score.updateAverageBalance(balance);
            }
        }
    }

    /**
     * Gibt die aktuelle Tag-Zahl zurück
     */
    public long getCurrentDay() {
        return currentDay;
    }

    // ========== AbstractPersistenceManager Implementation ==========

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, CreditScore>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, CreditScore> data) {
        creditScores.clear();
        if (data != null) {
            creditScores.putAll(data);
        }
    }

    @Override
    protected Map<UUID, CreditScore> getCurrentData() {
        return new HashMap<>(creditScores);
    }

    @Override
    protected String getComponentName() {
        return "CreditScoreManager";
    }

    @Override
    protected String getHealthDetails() {
        return creditScores.size() + " Kredit-Scores gespeichert";
    }

    @Override
    protected void onCriticalLoadFailure() {
        creditScores.clear();
    }
}
