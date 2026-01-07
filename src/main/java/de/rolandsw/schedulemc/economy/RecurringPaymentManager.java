package de.rolandsw.schedulemc.economy;
nimport de.rolandsw.schedulemc.util.StringUtils;
nimport de.rolandsw.schedulemc.util.GameConstants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Collections;
import java.util.UUID;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

/**
 * Zentrales Dauerauftragssystem für ScheduleMC.
 *
 * <p>Dieser Manager ermöglicht Spielern die Erstellung und Verwaltung von automatischen,
 * wiederkehrenden Zahlungen an andere Spieler. Das System führt Zahlungen basierend auf
 * konfigurierbaren Intervallen automatisch durch und bietet umfassende Verwaltungsfunktionen.</p>
 *
 * <h2>Hauptfunktionalität:</h2>
 * <ul>
 *   <li><b>Dauerauftrag erstellen:</b> Spieler können automatische Zahlungen an andere Spieler
 *       mit anpassbarem Intervall (täglich, wöchentlich, monatlich) einrichten</li>
 *   <li><b>Pause/Resume:</b> Daueraufträge können temporär pausiert und wieder aktiviert werden</li>
 *   <li><b>Auto-Execution:</b> Zahlungen werden automatisch täglich geprüft und bei Fälligkeit
 *       durchgeführt</li>
 *   <li><b>Failure Handling:</b> Bei 3 fehlgeschlagenen Versuchen (z.B. unzureichendes Guthaben)
 *       wird der Dauerauftrag automatisch deaktiviert</li>
 *   <li><b>Limit-Management:</b> Konfigurierbare Maximalanzahl pro Spieler zur Spam-Vermeidung</li>
 * </ul>
 *
 * <h2>Konfiguration:</h2>
 * <p>Alle Limits sind über {@link ModConfigHandler} konfigurierbar:</p>
 * <ul>
 *   <li><b>RECURRING_MAX_PER_PLAYER:</b> Maximale Anzahl Daueraufträge pro Spieler (standard: 10)</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * RecurringPaymentManager manager = RecurringPaymentManager.getInstance(server);
 *
 * // Erstelle wöchentlichen Dauerauftrag über 100€
 * boolean success = manager.createRecurringPayment(
 *     senderUUID,
 *     recipientUUID,
 *     100.0,           // Betrag
 *     7,               // Interval (Tage)
 *     "Mietzahlung"    // Beschreibung
 * );
 *
 * // Pause bei Bedarf
 * manager.pauseRecurringPayment(senderUUID, "abc12345");
 *
 * // Später reaktivieren
 * manager.resumeRecurringPayment(senderUUID, "abc12345");
 * }</pre>
 *
 * <h2>Auto-Deaktivierung:</h2>
 * <p>Daueraufträge werden automatisch deaktiviert wenn:</p>
 * <ul>
 *   <li>3 aufeinanderfolgende Zahlungen fehlschlagen (meist durch unzureichendes Guthaben)</li>
 *   <li>Der Empfänger nicht mehr existiert</li>
 * </ul>
 * <p>Spieler erhalten bei Deaktivierung eine Warnung und können den Dauerauftrag nach
 * Guthaben-Aufladung manuell wieder aktivieren.</p>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Thread-sicher durch ConcurrentHashMap für Zahlungen-Map und
 * Double-Checked Locking für Singleton.</p>
 *
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 * @see RecurringPayment
 * @see EconomyManager
 * @see ModConfigHandler
 */
public class RecurringPaymentManager extends AbstractPersistenceManager<Map<UUID, List<RecurringPayment>>> {
    /**
     * Singleton-Instanz des RecurringPaymentManagers.
     * <p>Volatile für Double-Checked Locking Pattern.</p>
     */
    private static volatile RecurringPaymentManager instance;

    /**
     * Zentrale Map aller Daueraufträge organisiert nach Zahler-UUID.
     * <p>Thread-sicher durch ConcurrentHashMap.</p>
     */
    private final Map<UUID, List<RecurringPayment>> payments = new ConcurrentHashMap<>();

    /**
     * MinecraftServer-Referenz für Spielerzugriffe und Benachrichtigungen.
     */
    private MinecraftServer server;

    /**
     * Aktueller Spieltag für tägliche Verarbeitung der Daueraufträge.
     */
    private long currentDay = 0;

    private RecurringPaymentManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_recurring.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * Gibt die Singleton-Instanz des RecurringPaymentManagers zurück.
     *
     * <p>Verwendet Double-Checked Locking Pattern für Thread-Safety bei minimaler
     * Synchronisierung. Die Server-Referenz wird bei jedem Aufruf aktualisiert.</p>
     *
     * @param server MinecraftServer für Spielerzugriffe (non-null)
     * @return Singleton-Instanz des RecurringPaymentManagers
     */
    public static RecurringPaymentManager getInstance(@Nonnull MinecraftServer server) {
        RecurringPaymentManager localRef = instance;
        if (localRef == null) {
            synchronized (RecurringPaymentManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new RecurringPaymentManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    /**
     * Erstellt einen neuen Dauerauftrag von einem Spieler an einen anderen.
     *
     * <p>Validiert alle Parameter und prüft das konfigurierte Limit pro Spieler.
     * Bei erfolgreicher Erstellung wird der Dauerauftrag sofort gespeichert und
     * der Zahler erhält eine Bestätigung mit der Dauerauftrags-ID.</p>
     *
     * <h3>Validierungen:</h3>
     * <ul>
     *   <li>Zahler und Empfänger dürfen nicht identisch sein</li>
     *   <li>Betrag muss positiv sein</li>
     *   <li>Interval muss mindestens 1 Tag betragen</li>
     *   <li>Zahler darf max. {@link ModConfigHandler.Common#RECURRING_MAX_PER_PLAYER} Daueraufträge haben</li>
     * </ul>
     *
     * @param fromPlayer UUID des zahlenden Spielers (non-null)
     * @param toPlayer UUID des Empfängers (non-null)
     * @param amount Zahlungsbetrag in € (muss > 0 sein)
     * @param intervalDays Zahlungsintervall in Tagen (muss >= 1 sein)
     * @param description Beschreibung des Dauerauftrags (z.B. "Mietzahlung")
     * @return {@code true} bei erfolgreicher Erstellung, {@code false} bei Validierungsfehlern
     * @see RecurringPayment
     */
    public boolean createRecurringPayment(@Nonnull UUID fromPlayer, @Nonnull UUID toPlayer, double amount,
                                         int intervalDays, String description) {
        if (fromPlayer.equals(toPlayer)) {
            return false;
        }

        if (amount <= 0 || intervalDays < 1) {
            return false;
        }

        // Prüfe Limit
        List<RecurringPayment> playerPayments = payments.get(fromPlayer);
        int maxPerPlayer = ModConfigHandler.COMMON.RECURRING_MAX_PER_PLAYER.get();
        if (playerPayments != null && playerPayments.size() >= maxPerPlayer) {
            return false;
        }

        RecurringPayment payment = new RecurringPayment(fromPlayer, toPlayer, amount, intervalDays,
            description, currentDay);

        payments.computeIfAbsent(fromPlayer, k -> new ArrayList<>()).add(payment);

        ServerPlayer player = server.getPlayerList().getPlayer(fromPlayer);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§a§l[DAUERAUFTRAG] Erstellt!\n" +
                "§7Empfänger: §e" + toPlayer + "\n" +
                "§7Betrag: §e" + StringUtils.formatMoney(amount) + "\n" +
                "§7Interval: §e" + intervalDays + " Tage\n" +
                "§7Beschreibung: §f" + description + "\n" +
                "§7ID: §f" + payment.getPaymentId().substring(0, 8)
            ));
        }

        save();
        LOGGER.info("Recurring payment created: {} -> {} ({}€ every {} days)",
            fromPlayer, toPlayer, amount, intervalDays);
        return true;
    }

    /**
     * Löscht einen bestehenden Dauerauftrag permanent.
     *
     * <p>Entfernt den Dauerauftrag aus der Verwaltung und speichert die Änderungen.
     * Der Spieler erhält eine Bestätigung mit Details zum gelöschten Dauerauftrag.</p>
     *
     * @param playerUUID UUID des Spielers, dem der Dauerauftrag gehört (non-null)
     * @param paymentId ID des zu löschenden Dauerauftrags (Präfix-Match, min. 8 Zeichen empfohlen)
     * @return {@code true} bei erfolgreicher Löschung, {@code false} wenn Dauerauftrag nicht gefunden
     */
    public boolean deleteRecurringPayment(@Nonnull UUID playerUUID, @Nonnull String paymentId) {
        RecurringPayment payment = findPayment(playerUUID, paymentId);
        if (payment == null) {
            return false;
        }

        List<RecurringPayment> playerPayments = payments.get(playerUUID);
        if (playerPayments != null) {
            playerPayments.remove(payment);
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§e[DAUERAUFTRAG] Gelöscht\n" +
                "§7Empfänger: §e" + payment.getToPlayer() + "\n" +
                "§7Betrag: §e" + StringUtils.formatMoney(payment.getAmount())
            ));
        }

        save();
        return true;
    }

    /**
     * Pausiert einen aktiven Dauerauftrag temporär.
     *
     * <p>Der Dauerauftrag bleibt bestehen, wird aber nicht mehr automatisch ausgeführt.
     * Kann jederzeit mit {@link #resumeRecurringPayment(UUID, String)} reaktiviert werden.</p>
     *
     * @param playerUUID UUID des Spielers, dem der Dauerauftrag gehört (non-null)
     * @param paymentId ID des zu pausierenden Dauerauftrags
     * @return {@code true} bei erfolgreicher Pausierung, {@code false} wenn nicht gefunden
     * @see #resumeRecurringPayment(UUID, String)
     */
    public boolean pauseRecurringPayment(@Nonnull UUID playerUUID, @Nonnull String paymentId) {
        RecurringPayment payment = findPayment(playerUUID, paymentId);
        if (payment == null) {
            return false;
        }

        payment.pause();

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§e[DAUERAUFTRAG] Pausiert\n" +
                "§7ID: §f" + paymentId
            ));
        }

        save();
        return true;
    }

    /**
     * Reaktiviert einen pausierten Dauerauftrag.
     *
     * <p>Setzt den Dauerauftrag wieder auf aktiv und aktualisiert das letzte
     * Ausführungsdatum auf den aktuellen Tag. Die nächste Zahlung erfolgt dann
     * nach Ablauf des konfigurierten Intervalls.</p>
     *
     * @param playerUUID UUID des Spielers, dem der Dauerauftrag gehört (non-null)
     * @param paymentId ID des zu reaktivierenden Dauerauftrags
     * @return {@code true} bei erfolgreicher Reaktivierung, {@code false} wenn nicht gefunden
     * @see #pauseRecurringPayment(UUID, String)
     */
    public boolean resumeRecurringPayment(@Nonnull UUID playerUUID, @Nonnull String paymentId) {
        RecurringPayment payment = findPayment(playerUUID, paymentId);
        if (payment == null) {
            return false;
        }

        payment.resume(currentDay);

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§a[DAUERAUFTRAG] Aktiviert\n" +
                "§7ID: §f" + paymentId
            ));
        }

        save();
        return true;
    }

    /**
     * Tick-Methode zur täglichen Verarbeitung von Daueraufträgen.
     *
     * <p>Wird vom Hauptserver-Tick aufgerufen. Erkennt Tageswechsel und triggert
     * die Verarbeitung aller fälligen Daueraufträge über {@link #processPayments()}.</p>
     *
     * @param dayTime Aktuelle Spielzeit in Ticks (wird durch GameConstants.TICKS_PER_DAY geteilt für Tagesberechnung)
     * @see #processPayments()
     */
    public void tick(long dayTime) {
        long day = dayTime / GameConstants.TICKS_PER_DAY;

        if (day != currentDay) {
            currentDay = day;
            processPayments();
        }
    }

    /**
     * Verarbeitet alle fälligen Daueraufträge für den aktuellen Tag.
     *
     * <p>Iteriert durch alle registrierten Daueraufträge und führt fällige Zahlungen aus.
     * Bei erfolgreicher Zahlung werden Zahler und Empfänger benachrichtigt. Bei Fehlschlag
     * wird ein Fehlerzähler inkrementiert und nach 3 Fehlversuchen erfolgt automatische
     * Deaktivierung mit Warnung an den Zahler.</p>
     *
     * <h3>Failure-Handling:</h3>
     * <ul>
     *   <li>1-2 Fehlversuche: Stille Wiederholungsversuche</li>
     *   <li>3 Fehlversuche: Automatische Deaktivierung + Warnung</li>
     *   <li>Hauptgrund für Fehlschlag: Unzureichendes Guthaben</li>
     * </ul>
     */
    private void processPayments() {
        for (Map.Entry<UUID, List<RecurringPayment>> entry : payments.entrySet()) {
            UUID playerUUID = entry.getKey();

            for (RecurringPayment payment : entry.getValue()) {
                if (payment.execute(currentDay)) {
                    // Erfolgreich ausgeführt
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    ServerPlayer recipient = server.getPlayerList().getPlayer(payment.getToPlayer());

                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "§a[DAUERAUFTRAG] Ausgeführt\n" +
                            "§7Empfänger: §e" + payment.getToPlayer() + "\n" +
                            "§7Betrag: §c-" + StringUtils.formatMoney(payment.getAmount()) + "\n" +
                            "§7Beschreibung: §f" + payment.getDescription()
                        ));
                    }

                    if (recipient != null) {
                        recipient.sendSystemMessage(Component.literal(
                            "§a[DAUERAUFTRAG] Erhalten\n" +
                            "§7Von: §e" + playerUUID + "\n" +
                            "§7Betrag: §a+" + StringUtils.formatMoney(payment.getAmount()) + "\n" +
                            "§7Beschreibung: §f" + payment.getDescription()
                        ));
                    }

                    LOGGER.info("Recurring payment executed: {} -> {} ({}€)",
                        playerUUID, payment.getToPlayer(), payment.getAmount());
                } else {
                    // Fehlgeschlagen
                    if (payment.getFailureCount() >= 3 && !payment.isActive()) {
                        // Nach 3 Fehlversuchen deaktiviert
                        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(
                                "§c§l[DAUERAUFTRAG] Deaktiviert!\n" +
                                "§7Grund: 3 fehlgeschlagene Versuche\n" +
                                "§7Empfänger: §e" + payment.getToPlayer() + "\n" +
                                "§7Betrag: §e" + StringUtils.formatMoney(payment.getAmount()) + "\n" +
                                "§cBitte zahle Geld ein und aktiviere den Auftrag erneut!"
                            ));
                        }

                        LOGGER.warn("Recurring payment deactivated after 3 failures: {} -> {}",
                            playerUUID, payment.getToPlayer());
                    }
                }
            }
        }

        save();
    }

    /**
     * Gibt alle Daueraufträge eines Spielers zurück.
     *
     * <p>Liefert eine unveränderliche Liste aller Daueraufträge (aktiv und pausiert)
     * für den angegebenen Spieler.</p>
     *
     * @param playerUUID UUID des Spielers
     * @return Liste der Daueraufträge, leer wenn keine vorhanden
     */
    public List<RecurringPayment> getPayments(UUID playerUUID) {
        return payments.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Sucht einen spezifischen Dauerauftrag anhand der Payment-ID.
     *
     * <p>Verwendet Präfix-Matching für flexiblere Suche (z.B. "abc12345" matched
     * "abc12345-def67890-...").</p>
     *
     * @param playerUUID UUID des Spielers, dem der Dauerauftrag gehört
     * @param paymentId ID oder Präfix der Payment-ID (min. 8 Zeichen empfohlen)
     * @return Gefundener Dauerauftrag oder {@code null} wenn nicht gefunden
     */
    @Nullable
    private RecurringPayment findPayment(UUID playerUUID, String paymentId) {
        List<RecurringPayment> playerPayments = payments.get(playerUUID);
        if (playerPayments == null) {
            return null;
        }

        return playerPayments.stream()
            .filter(p -> p.getPaymentId().startsWith(paymentId))
            .findFirst()
            .orElse(null);
    }

    // ========== AbstractPersistenceManager Implementation ==========

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, List<RecurringPayment>>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, List<RecurringPayment>> data) {
        payments.clear();
        payments.putAll(data);
    }

    @Override
    protected Map<UUID, List<RecurringPayment>> getCurrentData() {
        return new HashMap<>(payments);
    }

    @Override
    protected String getComponentName() {
        return "RecurringPaymentManager";
    }

    @Override
    protected String getHealthDetails() {
        int totalPayments = payments.values().stream().mapToInt(List::size).sum();
        return totalPayments + " Daueraufträge aktiv";
    }

    @Override
    protected void onCriticalLoadFailure() {
        payments.clear();
    }
}
