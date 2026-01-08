package de.rolandsw.schedulemc.economy;
nimport de.rolandsw.schedulemc.util.StringUtils;
nimport de.rolandsw.schedulemc.util.GameConstants;

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
 * Zentrales Kreditverwaltungssystem für ScheduleMC.
 *
 * <p>Dieser Manager verwaltet die Kreditvergabe, tägliche Ratenzahlungen und vorzeitige
 * Rückzahlungen für Spieler. Das System unterstützt verschiedene Kredittypen mit
 * unterschiedlichen Konditionen (Betrag, Zinssatz, Laufzeit).</p>
 *
 * <h2>Hauptfunktionalität:</h2>
 * <ul>
 *   <li><b>Kreditvergabe:</b> Spieler können Kredite beantragen, sofern sie die
 *       Mindestanforderungen erfüllen (Kontostand >= 1000€)</li>
 *   <li><b>Tägliche Abbuchung:</b> Automatische Ratenzahlungen werden täglich über den
 *       Tick-Mechanismus abgebucht</li>
 *   <li><b>Vorzeitige Rückzahlung:</b> Spieler können Kredite jederzeit vollständig tilgen</li>
 *   <li><b>Benachrichtigungen:</b> Spieler erhalten Meldungen bei Kreditvergabe, Zahlungen
 *       und vollständiger Tilgung</li>
 * </ul>
 *
 * <h2>Kredittypen:</h2>
 * <p>Verfügbare Kredittypen werden durch {@link Loan.LoanType} definiert und beinhalten:</p>
 * <ul>
 *   <li><b>SMALL:</b> Kleinkredit für schnelle Liquidität</li>
 *   <li><b>MEDIUM:</b> Mittelkredit für größere Investitionen</li>
 *   <li><b>LARGE:</b> Großkredit für umfangreiche Projekte</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * // Kredit beantragen
 * LoanManager manager = LoanManager.getInstance(server);
 * boolean success = manager.applyForLoan(playerUUID, Loan.LoanType.MEDIUM);
 *
 * // Kredit-Status prüfen
 * if (manager.hasActiveLoan(playerUUID)) {
 *     Loan loan = manager.getLoan(playerUUID);
 *     double remaining = loan.getRemaining();
 * }
 *
 * // Vorzeitige Rückzahlung
 * boolean repaid = manager.repayLoan(playerUUID);
 * }</pre>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Der Manager verwendet ein {@link ConcurrentHashMap} für Thread-sichere Kreditverwaltung
 * und implementiert das Double-Checked Locking Pattern für die Singleton-Instanz.</p>
 *
 * <h2>Persistierung:</h2>
 * <p>Alle aktiven Kredite werden automatisch in {@code plotmod_loans.json} gespeichert
 * durch die Basisklasse {@link AbstractPersistenceManager}.</p>
 *
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 * @see Loan
 * @see Loan.LoanType
 * @see AbstractPersistenceManager
 */
public class LoanManager extends AbstractPersistenceManager<Map<UUID, Loan>> {
    /**
     * Singleton-Instanz des LoanManagers.
     * <p>Volatile für korrekte Sichtbarkeit im Double-Checked Locking Pattern.</p>
     */
    private static volatile LoanManager instance;

    /**
     * Mindest-Kontostand, der für eine Kreditvergabe erforderlich ist.
     * <p>Standard: 1000€ - Stellt sicher, dass Spieler über grundlegende finanzielle
     * Stabilität verfügen.</p>
     */
    private static final double MIN_BALANCE_FOR_LOAN = 1000.0;

    /**
     * Mindest-Spielzeit in Tagen, die für eine Kreditvergabe erforderlich ist.
     * <p>Standard: 7 Tage - Verhindert Missbrauch durch neue Spieler.</p>
     */
    private static final int MIN_PLAYTIME_DAYS = 7;

    /**
     * Map aller aktiven Kredite, indexiert nach Spieler-UUID.
     * <p>ConcurrentHashMap für Thread-sichere Zugriffe während der Tick-Verarbeitung.</p>
     */
    private final Map<UUID, Loan> activeLoans = new ConcurrentHashMap<>();

    /**
     * Referenz zum MinecraftServer für Spielerzugriffe und Benachrichtigungen.
     */
    private MinecraftServer server;

    /**
     * Aktueller Spieltag (dayTime / GameConstants.TICKS_PER_DAY) für tägliche Ratenzahlungen.
     * <p>Wird verwendet, um Ratenzahlungen exakt einmal pro Minecraft-Tag zu verarbeiten.</p>
     */
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
     * Gibt die Singleton-Instanz des LoanManagers zurück.
     *
     * <p>Verwendet das Double-Checked Locking Pattern für Thread-sichere
     * Lazy Initialization. Aktualisiert die Server-Referenz bei jedem Aufruf.</p>
     *
     * <h3>Thread-Safety:</h3>
     * <p>Diese Methode ist Thread-sicher durch:</p>
     * <ul>
     *   <li>Volatile-Deklaration der Singleton-Instanz</li>
     *   <li>Double-Checked Locking mit lokaler Referenz-Kopie</li>
     *   <li>Synchronized Block beim ersten Zugriff</li>
     * </ul>
     *
     * @param server Der MinecraftServer für Spielerzugriffe und Persistierung (non-null)
     * @return Die Singleton-Instanz des LoanManagers
     * @throws NullPointerException Falls server null ist
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
     * Beantragt einen Kredit für einen Spieler.
     *
     * <p>Prüft die Kreditwürdigkeit des Spielers und vergibt den Kredit bei Erfolg.
     * Die Kreditsumme wird sofort auf das Spielerkonto ausgezahlt.</p>
     *
     * <h3>Voraussetzungen für die Kreditvergabe:</h3>
     * <ul>
     *   <li>Spieler hat noch keinen aktiven Kredit</li>
     *   <li>Kontostand >= 1000€ (MIN_BALANCE_FOR_LOAN)</li>
     * </ul>
     *
     * <h3>Ablauf bei erfolgreicher Vergabe:</h3>
     * <ol>
     *   <li>Erstellen eines neuen Loan-Objekts mit gewähltem Typ</li>
     *   <li>Auszahlung der Kreditsumme auf das Spielerkonto</li>
     *   <li>Speichern des Kredits in activeLoans Map</li>
     *   <li>Benachrichtigung des Spielers mit allen Kreditdetails</li>
     *   <li>Persistierung der Kreditdaten</li>
     * </ol>
     *
     * @param playerUUID UUID des Spielers, der den Kredit beantragt (non-null)
     * @param type Der gewünschte Kredittyp (SMALL, MEDIUM, LARGE) (non-null)
     * @return {@code true} wenn der Kredit erfolgreich vergeben wurde, sonst {@code false}
     * @throws NullPointerException Falls playerUUID oder type null ist
     * @see Loan.LoanType
     * @see #hasActiveLoan(UUID)
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
                "§7Betrag: §a+" + StringUtils.formatMoney(type.getAmount()) + "\n" +
                "§7Zinssatz: §c" + (int)(type.getInterestRate() * 100) + "%\n" +
                "§7Laufzeit: §e" + type.getDurationDays() + " Tage\n" +
                "§7Tägliche Rate: §c-" + StringUtils.formatMoney(loan.getDailyPayment())
            ));
        }

        save();
        return true;
    }

    /**
     * Verarbeitet tägliche Ratenzahlungen für alle aktiven Kredite.
     *
     * <p>Diese Methode wird vom Server-Tick-System aufgerufen und verarbeitet
     * Zahlungen exakt einmal pro Minecraft-Tag (GameConstants.TICKS_PER_DAY Ticks).</p>
     *
     * <h3>Funktionsweise:</h3>
     * <ul>
     *   <li>Berechnet den aktuellen Spieltag aus der dayTime</li>
     *   <li>Prüft ob ein neuer Tag begonnen hat (Tag != letzter Tag)</li>
     *   <li>Ruft bei Tag-Wechsel {@link #processDailyPayments()} auf</li>
     * </ul>
     *
     * @param dayTime Die aktuelle Spiel-Zeit in Ticks
     * @see #processDailyPayments()
     */
    public void tick(long dayTime) {
        long day = dayTime / GameConstants.TICKS_PER_DAY;

        if (day != currentDay) {
            currentDay = day;
            processDailyPayments();
        }
    }

    /**
     * Verarbeitet die täglichen Ratenzahlungen für alle aktiven Kredite.
     *
     * <p>Für jeden aktiven Kredit wird versucht, die fällige Tagesrate abzubuchen.
     * Bei erfolgloser Zahlung wird der Spieler gewarnt.</p>
     *
     * <h3>Ablauf pro Kredit:</h3>
     * <ol>
     *   <li>Berechnung der fälligen Tagesrate (min: dailyPayment, max: remaining)</li>
     *   <li>Versuch der Abbuchung vom Spielerkonto</li>
     *   <li>Bei Erfolg: Aktualisierung des Kredits, ggf. Entfernung bei vollständiger Tilgung</li>
     *   <li>Bei Misserfolg: Warnung an den Spieler mit Zahlungsdetails</li>
     * </ol>
     *
     * <h3>Benachrichtigungen:</h3>
     * <ul>
     *   <li><b>Erfolgreiche Tilgung:</b> Glückwunsch-Nachricht an den Spieler</li>
     *   <li><b>Fehlgeschlagene Zahlung:</b> Warnung mit Zahlungsbetrag und Kontostand</li>
     * </ul>
     *
     * @see #tick(long)
     * @see Loan#payDailyInstallment()
     * @see Loan#isRepaid()
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
                        "§7Fällig: §c" + StringUtils.formatMoney(payment) + "\n" +
                        "§7Kontostand: §e" + StringUtils.formatMoney(EconomyManager.getBalance(playerUUID)) + "\n" +
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
     * Zahlt einen Kredit vorzeitig vollständig zurück.
     *
     * <p>Ermöglicht Spielern die sofortige Tilgung ihres Kredits durch Zahlung
     * des gesamten Restbetrags. Bei Erfolg wird der Kredit aus der activeLoans
     * Map entfernt und die Daten werden persistiert.</p>
     *
     * <h3>Voraussetzungen:</h3>
     * <ul>
     *   <li>Spieler hat einen aktiven Kredit</li>
     *   <li>Kontostand >= Restbetrag des Kredits</li>
     * </ul>
     *
     * <h3>Ablauf bei erfolgreicher Rückzahlung:</h3>
     * <ol>
     *   <li>Berechnung des Restbetrags ({@link Loan#getRemaining()})</li>
     *   <li>Abbuchung des Restbetrags vom Spielerkonto</li>
     *   <li>Markierung des Kredits als vollständig getilgt</li>
     *   <li>Entfernung aus der activeLoans Map</li>
     *   <li>Benachrichtigung des Spielers über erfolgreiche Tilgung</li>
     *   <li>Persistierung der aktualisierten Kreditdaten</li>
     * </ol>
     *
     * @param playerUUID UUID des Spielers, der den Kredit zurückzahlen möchte (non-null)
     * @return {@code true} wenn die Rückzahlung erfolgreich war, sonst {@code false}
     *         (kein aktiver Kredit oder nicht genug Geld)
     * @throws NullPointerException Falls playerUUID null ist
     * @see #hasActiveLoan(UUID)
     * @see #getLoan(UUID)
     * @see Loan#getRemaining()
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
                    "§7Betrag: §c-" + StringUtils.formatMoney(remaining)
                ));
            }

            save();
            return true;
        }

        return false;
    }

    /**
     * Prüft ob ein Spieler einen aktiven Kredit hat.
     *
     * <p>Diese Methode wird verwendet, um vor Kreditvergabe zu prüfen, ob der Spieler
     * bereits einen laufenden Kredit besitzt. Pro Spieler ist maximal ein aktiver
     * Kredit erlaubt.</p>
     *
     * @param playerUUID UUID des zu prüfenden Spielers
     * @return {@code true} wenn der Spieler einen aktiven Kredit hat, sonst {@code false}
     * @see #applyForLoan(UUID, Loan.LoanType)
     * @see #getLoan(UUID)
     */
    public boolean hasActiveLoan(UUID playerUUID) {
        return activeLoans.containsKey(playerUUID);
    }

    /**
     * Gibt den aktiven Kredit eines Spielers zurück.
     *
     * <p>Ermöglicht den Zugriff auf Kreditdetails wie Restbetrag, Tagesrate,
     * Kredittyp und Vergabedatum.</p>
     *
     * <h3>Verwendungsbeispiel:</h3>
     * <pre>{@code
     * if (manager.hasActiveLoan(playerUUID)) {
     *     Loan loan = manager.getLoan(playerUUID);
     *     double remaining = loan.getRemaining();
     *     double dailyPayment = loan.getDailyPayment();
     * }
     * }</pre>
     *
     * @param playerUUID UUID des Spielers
     * @return Das Loan-Objekt des Spielers, oder {@code null} wenn kein aktiver Kredit existiert
     * @see #hasActiveLoan(UUID)
     * @see Loan
     */
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
