package de.rolandsw.schedulemc.economy;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrales Kredit-Score-System (Credit Scoring) für ScheduleMC.
 *
 * <p>Dieser Manager verwaltet Kreditwürdigkeits-Scores für alle Spieler basierend auf
 * Zahlungshistorie, Konto-Balance und abgeschlossenen Krediten. Das System beeinflusst
 * Kreditvergabe-Limits, Zinssätze und verfügbare Kreditprodukte.</p>
 *
 * <h2>Hauptfunktionalität:</h2>
 * <ul>
 *   <li><b>Credit Rating:</b> Dynamische Bewertung von EXCELLENT bis POOR</li>
 *   <li><b>Interest Rate Modifiers:</b> Bessere Scores = niedrigere Zinssätze (0.8x - 1.5x)</li>
 *   <li><b>Loan Limits:</b> Maximale Kredithöhe basierend auf Score</li>
 *   <li><b>Payment Tracking:</b> Pünktliche/verpasste Zahlungen beeinflussen Score</li>
 *   <li><b>Balance Tracking:</b> Durchschnittlicher Kontostand fließt in Rating ein</li>
 *   <li><b>Default Handling:</b> Kreditausfälle verschlechtern Score drastisch</li>
 * </ul>
 *
 * <h2>Credit Rating System:</h2>
 * <table border="1">
 *   <tr><th>Rating</th><th>Interest Modifier</th><th>Max Loan</th><th>Beschreibung</th></tr>
 *   <tr><td>EXCELLENT</td><td>0.8x</td><td>Unbegrenzt</td><td>Perfekte Zahlungshistorie</td></tr>
 *   <tr><td>GOOD</td><td>1.0x</td><td>50.000€</td><td>Gute Kreditwürdigkeit</td></tr>
 *   <tr><td>FAIR</td><td>1.2x</td><td>20.000€</td><td>Durchschnittlich</td></tr>
 *   <tr><td>POOR</td><td>1.5x</td><td>5.000€</td><td>Problematische Historie</td></tr>
 * </table>
 *
 * <h2>Score-Berechnung:</h2>
 * <p>Der Credit Score wird beeinflusst durch:</p>
 * <ul>
 *   <li><b>Payment History (40%):</b> Anzahl pünktlicher vs. verpasster Zahlungen</li>
 *   <li><b>Credit History (30%):</b> Alter des Accounts und abgeschlossene Kredite</li>
 *   <li><b>Average Balance (20%):</b> Durchschnittlicher Kontostand über Zeit</li>
 *   <li><b>Defaults (10%):</b> Kreditausfälle haben schwere negative Auswirkungen</li>
 * </ul>
 *
 * <h2>Beispiel-Workflow:</h2>
 * <pre>{@code
 * CreditScoreManager manager = CreditScoreManager.getInstance(server);
 *
 * // 1. Prüfe ob Spieler Kredit bekommen kann
 * if (manager.canTakeLoan(playerUUID, CreditLoan.CreditLoanType.MEDIUM)) {
 *     // 2. Berechne individuellen Zinssatz
 *     double rate = manager.getEffectiveInterestRate(playerUUID, CreditLoan.CreditLoanType.MEDIUM);
 *     // Gutes Rating: 5% * 0.8 = 4% Zinsen
 *     // Schlechtes Rating: 5% * 1.5 = 7.5% Zinsen
 *
 *     // 3. Nach erfolgreicher Zahlung
 *     manager.recordOnTimePayment(playerUUID); // Score verbessert sich
 * }
 * }</pre>
 *
 * <h2>Performance-Optimierung:</h2>
 * <p>Balance-Updates werden throttled (alle 60 Sekunden statt jeden Tick) um
 * unnötige Rechenoperationen zu vermeiden.</p>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Thread-sicher durch ConcurrentHashMap für Score-Map und
 * Double-Checked Locking für Singleton.</p>
 *
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 * @see CreditScore
 * @see CreditLoan
 * @see LoanManager
 */
public class CreditScoreManager extends AbstractPersistenceManager<Map<UUID, CreditScore>> {
    /**
     * Singleton-Instanz des CreditScoreManagers.
     * <p>Volatile für Double-Checked Locking Pattern.</p>
     */
    private static volatile CreditScoreManager instance;

    /**
     * Map aller Kredit-Scores organisiert nach Spieler-UUID.
     * <p>Thread-sicher durch ConcurrentHashMap.</p>
     */
    private final Map<UUID, CreditScore> creditScores = new ConcurrentHashMap<>();

    /**
     * MinecraftServer-Referenz für Zugriffe.
     */
    private MinecraftServer server;

    /**
     * Aktueller Spieltag für Score-Berechnungen.
     */
    private long currentDay = 0;

    /**
     * Counter für Balance-Update-Throttling.
     * <p>Verhindert unnötige Updates jeden Tick.</p>
     */
    private int balanceUpdateCounter = 0;

    /**
     * Interval für Balance-Updates in Ticks.
     * <p>Standard: 1200 Ticks = 60 Sekunden = 1 Minute</p>
     */
    private static final int BALANCE_UPDATE_INTERVAL = 1200; // Alle 60 Sekunden (1200 Ticks)

    private CreditScoreManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_credit_scores.json").toFile(),
            GsonHelper.get() // Umgebungsabhängig: kompakt in Produktion
        );
        this.server = server;
        load();
    }

    /**
     * Gibt die Singleton-Instanz des CreditScoreManagers zurück.
     *
     * <p>Verwendet Double-Checked Locking Pattern für Thread-Safety bei minimaler
     * Synchronisierung. Die Server-Referenz wird bei jedem Aufruf aktualisiert.</p>
     *
     * @param server MinecraftServer für Zugriffe (non-null)
     * @return Singleton-Instanz des CreditScoreManagers
     */
    public static CreditScoreManager getInstance(@Nonnull MinecraftServer server) {
        CreditScoreManager localRef = instance;
        if (localRef == null) {
            synchronized (CreditScoreManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new CreditScoreManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    /**
     * Gibt den Credit-Score für einen Spieler zurück oder erstellt einen neuen.
     *
     * <p>Lazy-Initialization: Score wird automatisch bei erstem Zugriff erstellt.
     * Neue Scores starten mit neutralem Rating (FAIR) und leerer Historie.</p>
     *
     * @param playerUUID UUID des Spielers (non-null)
     * @return Credit-Score des Spielers (niemals {@code null})
     */
    public CreditScore getOrCreateScore(@Nonnull UUID playerUUID) {
        return creditScores.computeIfAbsent(playerUUID, uuid -> {
            CreditScore newScore = new CreditScore(uuid, currentDay);
            save();
            return newScore;
        });
    }

    /**
     * Gibt den Credit-Score für einen Spieler zurück (ohne Auto-Erstellung).
     *
     * <p>Nützlich für Prüfungen ob Score bereits existiert.</p>
     *
     * @param playerUUID UUID des Spielers
     * @return Credit-Score oder {@code null} wenn nicht vorhanden
     */
    @Nullable
    public CreditScore getScore(UUID playerUUID) {
        return creditScores.get(playerUUID);
    }

    /**
     * Prüft ob ein Spieler einen bestimmten Kredittyp aufnehmen kann.
     *
     * <p>Validiert zwei Kriterien:</p>
     * <ul>
     *   <li>Credit Rating muss mindestens dem erforderlichen Rating entsprechen</li>
     *   <li>Kreditbetrag darf maximales Limit nicht überschreiten</li>
     * </ul>
     *
     * @param playerUUID UUID des Spielers (non-null)
     * @param loanType Typ des gewünschten Kredits
     * @return {@code true} wenn Kredit gewährt werden kann, sonst {@code false}
     * @see CreditScore#getRating(long)
     * @see CreditScore#getMaxLoanAmount(long)
     */
    public boolean canTakeLoan(@Nonnull UUID playerUUID, CreditLoan.CreditLoanType loanType) {
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
     * Berechnet den effektiven Zinssatz für einen Kredit basierend auf Credit-Score.
     *
     * <p>Der effektive Zinssatz ergibt sich aus: Basis-Zinssatz × Score-Modifikator</p>
     *
     * <h3>Beispiele:</h3>
     * <ul>
     *   <li>EXCELLENT (0.8x): 5% Basis → 4% effektiv</li>
     *   <li>GOOD (1.0x): 5% Basis → 5% effektiv</li>
     *   <li>POOR (1.5x): 5% Basis → 7.5% effektiv</li>
     * </ul>
     *
     * @param playerUUID UUID des Spielers
     * @param loanType Typ des Kredits mit Basis-Zinssatz
     * @return Effektiver Zinssatz (z.B. 0.05 für 5%)
     * @see CreditScore#getInterestRateModifier(long)
     */
    public double getEffectiveInterestRate(UUID playerUUID, CreditLoan.CreditLoanType loanType) {
        CreditScore score = getOrCreateScore(playerUUID);
        double modifier = score.getInterestRateModifier(currentDay);
        return loanType.getBaseInterestRate() * modifier;
    }

    /**
     * Registriert eine pünktliche Zahlung für Credit-Score.
     *
     * <p>Verbessert Payment-History und kann Rating positiv beeinflussen.</p>
     *
     * @param playerUUID UUID des Spielers
     */
    public void recordOnTimePayment(UUID playerUUID) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordOnTimePayment();
        save();
    }

    /**
     * Registriert eine verpasste Zahlung für Credit-Score.
     *
     * <p>Verschlechtert Payment-History und kann Rating negativ beeinflussen.</p>
     *
     * @param playerUUID UUID des Spielers
     */
    public void recordMissedPayment(UUID playerUUID) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordMissedPayment();
        save();
    }

    /**
     * Registriert einen erfolgreich abgeschlossenen Kredit.
     *
     * <p>Verbessert Credit-History durch erfolgreichen Kreditabschluss.
     * Positiver Effekt auf Rating, besonders bei hohen Beträgen.</p>
     *
     * @param playerUUID UUID des Spielers
     * @param amountRepaid Gesamtbetrag der zurückgezahlt wurde
     */
    public void recordLoanCompleted(UUID playerUUID, double amountRepaid) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordLoanCompleted(amountRepaid);
        save();
        LOGGER.info("Credit score updated for {} - Loan completed, {} repaid", playerUUID, amountRepaid);
    }

    /**
     * Registriert einen Kreditausfall (Default).
     *
     * <p>Schwerwiegende negative Auswirkung auf Credit-Score.
     * Kann Rating stark verschlechtern (z.B. EXCELLENT → POOR).</p>
     *
     * @param playerUUID UUID des Spielers
     */
    public void recordLoanDefaulted(UUID playerUUID) {
        CreditScore score = getOrCreateScore(playerUUID);
        score.recordLoanDefaulted();
        save();
        LOGGER.warn("Credit score updated for {} - Loan DEFAULTED", playerUUID);
    }

    /**
     * Tick-Methode für tägliche Updates und Balance-Tracking.
     *
     * <p>Führt zwei Hauptaufgaben aus:</p>
     * <ul>
     *   <li>Aktualisiert currentDay bei Tageswechsel</li>
     *   <li>Triggert Balance-Updates alle 60 Sekunden (throttled)</li>
     * </ul>
     *
     * @param dayTime Aktuelle Spielzeit in Ticks (wird durch 24000 geteilt für Tagesberechnung)
     * @see #updateAllBalances()
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
     * Aktualisiert die durchschnittlichen Kontostände aller Spieler.
     *
     * <p>Wird throttled aufgerufen (alle 60 Sekunden) um Performance zu schonen.
     * Balance-Durchschnitt fließt in Credit-Rating-Berechnung ein.</p>
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
     * Gibt die aktuelle Spieltag-Nummer zurück.
     *
     * <p>Wird für Score-Berechnungen und Zeitstempel verwendet.</p>
     *
     * @return Aktuelle Tag-Nummer (Spielzeit / 24000)
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
