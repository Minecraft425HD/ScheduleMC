package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrales Überziehungskreditmanagement (Dispo) für ScheduleMC.
 *
 * <p>Verwaltet Überziehungskredite (Dispositionskredite) mit konfigurierbaren Limits,
 * wöchentlichen Überziehungszinsen und automatischer Pfändung bei Limit-Überschreitung.</p>
 *
 * <h2>Hauptfunktionen:</h2>
 * <ul>
 *   <li><b>Überziehungslimit:</b> Configurable max. negatives Guthaben (z.B. -10.000€)</li>
 *   <li><b>Überziehungszinsen:</b> Wöchentliche Zinsen auf negative Beträge (z.B. 15%/Woche)</li>
 *   <li><b>Warnsystem:</b> Automatische Warnungen bei Erreichen des Warnschwellenwerts</li>
 *   <li><b>Pfändung:</b> Automatische Pfändung (Wallet-Leerung) bei Limit-Überschreitung</li>
 * </ul>
 *
 * <h2>Konfiguration:</h2>
 * <p>Alle Limits und Zinssätze sind über {@link ModConfigHandler} konfigurierbar:</p>
 * <ul>
 *   <li><b>OVERDRAFT_MAX_LIMIT:</b> Maximales Überziehungslimit (standard: -10.000€)</li>
 *   <li><b>OVERDRAFT_WARNING_THRESHOLD:</b> Warnschwelle (standard: -5.000€)</li>
 *   <li><b>OVERDRAFT_INTEREST_RATE:</b> Wöchentlicher Zinssatz (standard: 15%)</li>
 * </ul>
 *
 * <h2>Beispiel-Ablauf:</h2>
 * <pre>{@code
 * // Spieler-Kontostand: -6.000€ (unter Warnschwelle)
 * 1. Warnung wird einmal pro Woche gesendet
 * 2. Wöchentliche Zinsen: -6.000€ * 15% = -900€
 * 3. Neuer Kontostand: -6.900€
 * 4. Bei -10.000€: Pfändung (Wallet geleert, Konto auf Limit gesetzt)
 * }</pre>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Thread-sicher durch ConcurrentHashMap für Tracking-Maps und
 * Double-Checked Locking für Singleton.</p>
 *
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 * @see EconomyManager
 * @see WalletManager
 * @see ModConfigHandler
 */
public class OverdraftManager extends AbstractPersistenceManager<Map<String, Object>> {
    /**
     * Singleton-Instanz des OverdraftManagers.
     * <p>Volatile für Double-Checked Locking Pattern.</p>
     */
    private static volatile OverdraftManager instance;

    /**
     * Tracking-Map für letzte Warnungstage pro Spieler.
     * <p>Verhindert Spam (max. 1 Warnung pro 7 Tage).</p>
     */
    private final Map<UUID, Long> lastWarningDay = new ConcurrentHashMap<>();

    /**
     * Tracking-Map für letzte Zinszahlungstage pro Spieler.
     * <p>Stellt sicher, dass Zinsen nur einmal pro Woche berechnet werden.</p>
     */
    private final Map<UUID, Long> lastInterestDay = new ConcurrentHashMap<>();

    /**
     * MinecraftServer-Referenz für Spielerzugriffe.
     */
    private MinecraftServer server;

    /**
     * Aktueller Spieltag für tägliche Verarbeitung.
     */
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
     * Gibt die Singleton-Instanz des OverdraftManagers zurück.
     *
     * @param server MinecraftServer für Spielerzugriffe (non-null)
     * @return Singleton-Instanz des OverdraftManagers
     */
    public static OverdraftManager getInstance(@Nonnull MinecraftServer server) {
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
     * Prüft ob eine Überziehung bis zum angegebenen Betrag erlaubt ist.
     *
     * <p>Vergleicht den neuen Kontostand mit dem konfigurierten Überziehungslimit.</p>
     *
     * @param newBalance Der geplante neue Kontostand
     * @return {@code true} wenn Überziehung erlaubt (newBalance >= maxLimit), sonst {@code false}
     * @see ModConfigHandler.Common#OVERDRAFT_MAX_LIMIT
     */
    public static boolean canOverdraft(double newBalance) {
        double maxLimit = ModConfigHandler.COMMON.OVERDRAFT_MAX_LIMIT.get();
        return newBalance >= maxLimit;
    }

    /**
     * Berechnet den aktuellen Überziehungsbetrag (Absolutwert des negativen Guthabens).
     *
     * @param balance Aktueller Kontostand
     * @return Überziehungsbetrag (0 bei positivem Kontostand, sonst Absolutwert)
     */
    public static double getOverdraftAmount(double balance) {
        if (balance >= 0) {
            return 0.0;
        }
        return Math.abs(balance);
    }

    /**
     * Verarbeitet tägliche Überziehungsprüfungen.
     *
     * <p>Führt bei Tageswechsel folgende Aktionen durch:</p>
     * <ul>
     *   <li>Warnungen bei Erreichen des Warning-Thresholds (max. 1/Woche)</li>
     *   <li>Wöchentliche Überziehungszinsen auf negative Beträge</li>
     *   <li>Pfändung bei Überschreitung des Limits</li>
     * </ul>
     *
     * @param dayTime Aktuelle Spielzeit in Ticks
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            processOverdrafts();
        }
    }

    /**
     * Verarbeitet Überziehungen
     */
    private void processOverdrafts() {
        Map<UUID, Double> balances = EconomyManager.getAllAccounts();

        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            UUID playerUUID = entry.getKey();
            double balance = entry.getValue();

            if (balance < 0) {
                double warningThreshold = ModConfigHandler.COMMON.OVERDRAFT_WARNING_THRESHOLD.get();
                double maxLimit = ModConfigHandler.COMMON.OVERDRAFT_MAX_LIMIT.get();

                // Warnung bei Schwelle
                if (balance <= warningThreshold) {
                    sendWarning(playerUUID, balance);
                }

                // Wöchentliche Überziehungszinsen
                chargeOverdraftInterest(playerUUID, balance);

                // Pfändung bei Limit
                if (balance <= maxLimit) {
                    executeSeizure(playerUUID, balance);
                }
            }
        }
    }

    /**
     * Sendet Warnung an Spieler
     */
    private void sendWarning(@Nonnull UUID playerUUID, double balance) {
        Long lastWarning = lastWarningDay.get(playerUUID);
        if (lastWarning != null && currentDay - lastWarning < 7) {
            return; // Max 1 Warnung pro Woche
        }

        double maxLimit = ModConfigHandler.COMMON.OVERDRAFT_MAX_LIMIT.get();
        double interestRate = ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE.get();

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§c§l⚠ WARNUNG: KONTO ÜBERZOGEN!\n" +
                "§7Kontostand: §c" + String.format("%.2f€", balance) + "\n" +
                "§7Limit: §c" + String.format("%.2f€", maxLimit) + "\n" +
                "§7Überziehungszinsen: §c" + String.format("%.0f%%", interestRate * 100) + " pro Woche\n" +
                "§cBitte zahle Geld ein um Pfändung zu vermeiden!"
            ));
        }

        lastWarningDay.put(playerUUID, currentDay);
    }

    /**
     * Berechnet wöchentliche Überziehungszinsen
     */
    private void chargeOverdraftInterest(@Nonnull UUID playerUUID, double balance) {
        Long lastInterest = lastInterestDay.get(playerUUID);
        if (lastInterest != null && currentDay - lastInterest < 7) {
            return; // Nur 1x pro Woche
        }

        double interestRate = ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE.get();
        double overdraftAmount = getOverdraftAmount(balance);
        double interest = overdraftAmount * interestRate;

        // Ziehe Zinsen ab (macht Balance noch negativer)
        EconomyManager.setBalance(playerUUID, balance - interest, TransactionType.OVERDRAFT_FEE,
            "Überziehungszinsen");

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[DISPO] Überziehungszinsen\n" +
                "§7Überzogen: §c" + String.format("%.2f€", overdraftAmount) + "\n" +
                "§7Zinssatz: §c" + String.format("%.0f%%", interestRate * 100) + "\n" +
                "§7Zinsen: §c-" + String.format("%.2f€", interest) + "\n" +
                "§7Neuer Kontostand: §c" + String.format("%.2f€", balance - interest)
            ));
        }

        lastInterestDay.put(playerUUID, currentDay);
        LOGGER.info("Overdraft interest charged: {}€ to {}", interest, playerUUID);
    }

    /**
     * Führt Pfändung durch
     */
    private void executeSeizure(@Nonnull UUID playerUUID, double balance) {
        double maxLimit = ModConfigHandler.COMMON.OVERDRAFT_MAX_LIMIT.get();
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

        if (player != null) {
            // Leere Geldbörse
            WalletManager.setBalance(playerUUID, 0.0);

            // Setze Konto auf Limit (nicht weiter verschlimmern)
            EconomyManager.setBalance(playerUUID, maxLimit, TransactionType.OTHER,
                "Pfändung durchgeführt");

            player.sendSystemMessage(Component.literal(
                "§4§l⚠⚠⚠ PFÄNDUNG! ⚠⚠⚠\n" +
                "§cDein Konto wurde gepfändet!\n" +
                "§7Grund: Überziehungslimit erreicht\n" +
                "§7Alte Schulden: §c" + String.format("%.2f€", balance) + "\n" +
                "§7Geldbörse geleert\n" +
                "§7Konto auf §c" + String.format("%.2f€", maxLimit) + " §7gesetzt\n" +
                "§eZahle Schulden ab um wieder handlungsfähig zu sein!"
            ));

            LOGGER.warn("Seizure executed for {}: balance was {}€", playerUUID, balance);
        }
    }

    /**
     * Gibt formatierte Dispo-Informationen für einen Spieler zurück.
     *
     * <p>Zeigt Kontostand, Überziehungsbetrag, verfügbares Limit und Zinssatz an.</p>
     *
     * @param playerUUID UUID des Spielers (non-null)
     * @return Formatierte Dispo-Informationen als String
     */
    public String getOverdraftInfo(@Nonnull UUID playerUUID) {
        double balance = EconomyManager.getBalance(playerUUID);
        double overdraft = getOverdraftAmount(balance);
        double maxLimit = ModConfigHandler.COMMON.OVERDRAFT_MAX_LIMIT.get();
        double interestRate = ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE.get();

        if (overdraft == 0) {
            return "§aDein Konto ist nicht überzogen.\n" +
                "§7Dispo-Limit: §e" + String.format("%.2f€", Math.abs(maxLimit)) + "\n" +
                "§7Überziehungszinsen: §c" + String.format("%.0f%%", interestRate * 100) + " pro Woche";
        }

        double available = maxLimit - balance;

        return "§c§lKONTO ÜBERZOGEN!\n" +
            "§7Kontostand: §c" + String.format("%.2f€", balance) + "\n" +
            "§7Überzogen um: §c" + String.format("%.2f€", overdraft) + "\n" +
            "§7Verfügbar bis Limit: §e" + String.format("%.2f€", available) + "\n" +
            "§7Dispo-Limit: §c" + String.format("%.2f€", maxLimit) + "\n" +
            "§7Überziehungszinsen: §c" + String.format("%.0f%%", interestRate * 100) + " pro Woche";
    }

    // ========== AbstractPersistenceManager Implementation ==========

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<String, Object>>(){}.getType();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onDataLoaded(Map<String, Object> data) {
        lastWarningDay.clear();
        lastInterestDay.clear();

        Object warningObj = data.get("lastWarningDay");
        if (warningObj instanceof Map) {
            ((Map<String, Number>) warningObj).forEach((k, v) -> {
                try {
                    lastWarningDay.put(UUID.fromString(k), v.longValue());
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in lastWarningDay: {}", k);
                }
            });
        }

        Object interestObj = data.get("lastInterestDay");
        if (interestObj instanceof Map) {
            ((Map<String, Number>) interestObj).forEach((k, v) -> {
                try {
                    lastInterestDay.put(UUID.fromString(k), v.longValue());
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in lastInterestDay: {}", k);
                }
            });
        }
    }

    @Override
    protected Map<String, Object> getCurrentData() {
        Map<String, Object> data = new HashMap<>();

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
        return lastWarningDay.size() + " Spieler mit Überziehung";
    }

    @Override
    protected void onCriticalLoadFailure() {
        lastWarningDay.clear();
        lastInterestDay.clear();
    }
}
