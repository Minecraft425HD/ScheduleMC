package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrales Steuerverwaltungssystem für ScheduleMC mit progressiver Einkommenssteuer
 * und Grundsteuer.
 *
 * <p>Verwaltet die automatische monatliche Steuererhebung für alle Spieler, bestehend aus
 * Einkommenssteuer (progressiv) und Grundsteuer (basierend auf Grundbesitz).</p>
 *
 * <h2>Einkommenssteuer (Progressiv):</h2>
 * <p>Das System verwendet ein progressives Steuersystem mit vier Stufen:</p>
 * <table border="1">
 *   <tr><th>Einkommen</th><th>Steuersatz</th><th>Beispiel</th></tr>
 *   <tr><td>0€ - 10.000€</td><td>0%</td><td>Steuerfrei</td></tr>
 *   <tr><td>10.001€ - 50.000€</td><td>10%</td><td>15.000€ → 500€ Steuern</td></tr>
 *   <tr><td>50.001€ - 100.000€</td><td>15%</td><td>75.000€ → 7.750€ Steuern</td></tr>
 *   <tr><td>Über 100.000€</td><td>20%</td><td>150.000€ → 17.500€ Steuern</td></tr>
 * </table>
 *
 * <h2>Grundsteuer:</h2>
 * <ul>
 *   <li><b>Basis:</b> Configurable per chunk (standard: 100€/chunk/monat)</li>
 *   <li><b>Berechnung:</b> Basiert auf horizontaler Fläche aller Plots (X * Z)</li>
 *   <li><b>Chunk-Größe:</b> 16x16 Blöcke = 256 Blöcke</li>
 * </ul>
 *
 * <h2>Steuerperiode:</h2>
 * <p>Steuern werden alle 7 Minecraft-Tage (1 Woche) automatisch erhoben.</p>
 *
 * <h2>Steuerschulden:</h2>
 * <p>Bei unzureichendem Kontostand:</p>
 * <ul>
 *   <li>Steuerschulden werden aufgebaut</li>
 *   <li>Spieler erhält Zahlungsaufforderung (3 Tage Frist)</li>
 *   <li>Schulden können manuell beglichen werden</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * // Steuer berechnen
 * TaxManager manager = TaxManager.getInstance(server);
 * double incomeTax = manager.calculateIncomeTax(75000.0);
 * double propertyTax = manager.calculatePropertyTax(playerUUID);
 * double total = incomeTax + propertyTax;
 *
 * // Steuerschuld prüfen
 * double debt = manager.getTaxDebt(playerUUID);
 * if (debt > 0) {
 *     manager.payTaxDebt(playerUUID);
 * }
 * }</pre>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Thread-sichere Implementierung durch ConcurrentHashMap und Double-Checked Locking.</p>
 *
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 * @see EconomyManager
 * @see StateAccount
 * @see PlotManager
 */
public class TaxManager extends AbstractPersistenceManager<Map<String, Object>> {
    /**
     * Singleton-Instanz des TaxManagers.
     * <p>Volatile für korrekte Sichtbarkeit im Double-Checked Locking Pattern.</p>
     */
    private static volatile TaxManager instance;

    /**
     * Grundfreibetrag für Einkommenssteuer.
     * <p>Einkommen bis 10.000€ sind steuerfrei.</p>
     */
    private static final double TAX_FREE_AMOUNT = 10000.0;

    /**
     * Erste Steuerstufe (10% Steuersatz).
     * <p>Einkommen von 10.001€ bis 50.000€ werden mit 10% besteuert.</p>
     */
    private static final double TAX_BRACKET_1 = 50000.0; // 10%

    /**
     * Zweite Steuerstufe (15% Steuersatz).
     * <p>Einkommen von 50.001€ bis 100.000€ werden mit 15% besteuert.</p>
     */
    private static final double TAX_BRACKET_2 = 100000.0; // 15%
    // Darüber: 20%

    /**
     * Steuerperiode in Minecraft-Tagen.
     * <p>Standard: 7 Tage (1 Woche) - Steuern werden wöchentlich erhoben.</p>
     */
    private static final int TAX_PERIOD_DAYS = 7; // 1 Woche

    /**
     * Map der letzten Steuertage pro Spieler für wöchentliche Erhebung.
     * <p>ConcurrentHashMap für Thread-sichere Zugriffe.</p>
     */
    private final Map<UUID, Long> lastTaxDay = new ConcurrentHashMap<>();

    /**
     * Map der ausstehenden Steuerschulden pro Spieler.
     * <p>Schulden entstehen bei nicht ausreichendem Kontostand während der Steuererhebung.</p>
     */
    private final Map<UUID, Double> taxDebt = new ConcurrentHashMap<>();

    /**
     * Referenz zum MinecraftServer für Spielerzugriffe.
     */
    private MinecraftServer server;

    /**
     * Aktueller Spieltag für wöchentliche Steuerverarbeitung.
     */
    private long currentDay = 0;

    private TaxManager(MinecraftServer server) {
        super(
            new File(server.getServerDirectory().toPath().resolve("config").resolve("plotmod_taxes.json").toString()),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<String, Object>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<String, Object> data) {
        lastTaxDay.clear();
        taxDebt.clear();

        // Load lastTaxDay
        Object lastTaxObj = data.get("lastTaxDay");
        if (lastTaxObj instanceof Map) {
            ((Map<String, Number>) lastTaxObj).forEach((k, v) -> {
                try {
                    lastTaxDay.put(UUID.fromString(k), v.longValue());
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in lastTaxDay: {}", k);
                }
            });
        }

        // Load taxDebt
        Object debtObj = data.get("taxDebt");
        if (debtObj instanceof Map) {
            ((Map<String, Number>) debtObj).forEach((k, v) -> {
                try {
                    taxDebt.put(UUID.fromString(k), v.doubleValue());
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in taxDebt: {}", k);
                }
            });
        }
    }

    @Override
    protected Map<String, Object> getCurrentData() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Long> lastTaxMap = new HashMap<>();
        lastTaxDay.forEach((k, v) -> lastTaxMap.put(k.toString(), v));
        data.put("lastTaxDay", lastTaxMap);

        Map<String, Double> debtMap = new HashMap<>();
        taxDebt.forEach((k, v) -> debtMap.put(k.toString(), v));
        data.put("taxDebt", debtMap);

        return data;
    }

    @Override
    protected String getComponentName() {
        return "TaxManager";
    }

    @Override
    protected String getHealthDetails() {
        return lastTaxDay.size() + " players tracked, " + taxDebt.size() + " debts";
    }

    @Override
    protected void onCriticalLoadFailure() {
        lastTaxDay.clear();
        taxDebt.clear();
        LOGGER.warn("TaxManager: Gestartet mit leeren Daten nach kritischem Fehler");
    }

    /**
     * Gibt die Singleton-Instanz des TaxManagers zurück.
     *
     * <p>Thread-sicher durch Double-Checked Locking Pattern mit volatile Instanz.</p>
     *
     * @param server MinecraftServer für Spielerzugriffe und Persistierung (non-null)
     * @return Singleton-Instanz des TaxManagers
     * @throws NullPointerException Falls server null ist
     */
    public static TaxManager getInstance(@Nonnull MinecraftServer server) {
        TaxManager localRef = instance;
        if (localRef == null) {
            synchronized (TaxManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new TaxManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    /**
     * Verarbeitet wöchentliche Steuererhebung basierend auf Minecraft-Tagen.
     *
     * <p>Prüft alle 7 Tage (TAX_PERIOD_DAYS) ob Steuern fällig sind und
     * verarbeitet Einkommens- und Grundsteuern für alle Spieler.</p>
     *
     * @param dayTime Aktuelle Spielzeit in Ticks (für Tag-Berechnung)
     * @see #processTaxes()
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            processTaxes();
        }
    }

    /**
     * Berechnet die progressive Einkommenssteuer basierend auf Kontostand.
     *
     * <p>Progressives System mit 4 Stufen:</p>
     * <ul>
     *   <li>0€ - 10.000€: 0% (Steuerfrei)</li>
     *   <li>10.001€ - 50.000€: 10%</li>
     *   <li>50.001€ - 100.000€: 15%</li>
     *   <li>Über 100.000€: 20%</li>
     * </ul>
     *
     * <h3>Berechnungsbeispiel:</h3>
     * <pre>
     * Balance: 75.000€
     * - 10.000€ steuerfrei
     * - 40.000€ @ 10% = 4.000€
     * - 25.000€ @ 15% = 3.750€
     * Total: 7.750€
     * </pre>
     *
     * @param balance Aktueller Kontostand des Spielers
     * @return Fällige Einkommenssteuer in Euro
     */
    public double calculateIncomeTax(double balance) {
        if (balance <= TAX_FREE_AMOUNT) {
            return 0.0;
        }

        double taxable = balance - TAX_FREE_AMOUNT;
        double tax = 0.0;

        if (taxable <= TAX_BRACKET_1 - TAX_FREE_AMOUNT) {
            // 10% bis 50k
            tax = taxable * 0.10;
        } else if (taxable <= TAX_BRACKET_2 - TAX_FREE_AMOUNT) {
            // 10% bis 50k, dann 15%
            tax = (TAX_BRACKET_1 - TAX_FREE_AMOUNT) * 0.10;
            tax += (taxable - (TAX_BRACKET_1 - TAX_FREE_AMOUNT)) * 0.15;
        } else {
            // 10% bis 50k, 15% bis 100k, dann 20%
            tax = (TAX_BRACKET_1 - TAX_FREE_AMOUNT) * 0.10;
            tax += (TAX_BRACKET_2 - TAX_BRACKET_1) * 0.15;
            tax += (taxable - (TAX_BRACKET_2 - TAX_FREE_AMOUNT)) * 0.20;
        }

        return tax;
    }

    /**
     * Berechnet Grundsteuer basierend auf Grundbesitz des Spielers.
     *
     * <p>Berechnung basiert auf der Gesamtfläche aller Plots des Spielers:</p>
     * <ol>
     *   <li>Horizontale Fläche pro Plot: (maxX - minX + 1) * (maxZ - minZ + 1)</li>
     *   <li>Umrechnung in Chunks: Fläche / 256 (16x16 Blöcke pro Chunk)</li>
     *   <li>Grundsteuer: Anzahl Chunks * configurable Rate (standard: 100€/chunk)</li>
     * </ol>
     *
     * @param playerUUID UUID des Spielers
     * @return Fällige Grundsteuer in Euro (0 wenn keine Plots)
     * @see PlotManager#getPlotsByOwner(UUID)
     */
    public double calculatePropertyTax(UUID playerUUID) {
        List<PlotRegion> plots = PlotManager.getPlotsByOwner(playerUUID);

        if (plots.isEmpty()) {
            return 0.0;
        }

        int totalChunks = 0;
        for (PlotRegion plot : plots) {
            // Berechne horizontale Fläche (X * Z)
            BlockPos min = plot.getMin();
            BlockPos max = plot.getMax();
            long width = max.getX() - min.getX() + 1;
            long depth = max.getZ() - min.getZ() + 1;
            long area = width * depth;

            // Ein Chunk ist 16x16 = 256 Blöcke
            int chunks = (int) Math.ceil(area / 256.0);
            totalChunks += chunks;
        }

        double taxPerChunk = ModConfigHandler.COMMON.TAX_PROPERTY_PER_CHUNK.get();
        return totalChunks * taxPerChunk;
    }

    /**
     * Verarbeitet Steuern
     */
    private void processTaxes() {
        Map<UUID, Double> balances = EconomyManager.getAllAccounts();

        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            UUID playerUUID = entry.getKey();
            double balance = entry.getValue();

            long lastTax = lastTaxDay.getOrDefault(playerUUID, 0L);
            long daysSince = currentDay - lastTax;

            if (daysSince >= TAX_PERIOD_DAYS) {
                chargeTax(playerUUID, balance);
                lastTaxDay.put(playerUUID, currentDay);
            }
        }

        save();
    }

    /**
     * Zieht Steuern ab
     */
    private void chargeTax(UUID playerUUID, double balance) {
        double incomeTax = calculateIncomeTax(balance);
        double propertyTax = calculatePropertyTax(playerUUID);
        double totalTax = incomeTax + propertyTax;

        if (totalTax <= 0) {
            return;
        }

        // Versuche Abbuchung
        if (EconomyManager.withdraw(playerUUID, totalTax, TransactionType.TAX_INCOME, "Monatliche Steuern")) {
            StateAccount.getInstance(server).deposit(totalTax, "Steuern (Einkommen + Grundsteuer)");

            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                StringBuilder message = new StringBuilder();
                message.append("§e§l[STEUERN] Monatliche Abrechnung\n");
                message.append("§7Kontostand: §6").append(String.format("%.2f€", balance)).append("\n");

                if (incomeTax > 0) {
                    message.append("§7Einkommenssteuer: §c-").append(String.format("%.2f€", incomeTax)).append("\n");
                }

                if (propertyTax > 0) {
                    double taxPerChunk = ModConfigHandler.COMMON.TAX_PROPERTY_PER_CHUNK.get();
                    int chunks = (int)(propertyTax / taxPerChunk);
                    message.append("§7Grundsteuer: §c-").append(String.format("%.2f€", propertyTax))
                           .append(" §7(").append(chunks).append(" Chunks)\n");
                }

                message.append("§7Gesamt: §c-").append(String.format("%.2f€", totalTax)).append("\n");
                message.append("§7Neuer Kontostand: §6").append(String.format("%.2f€", EconomyManager.getBalance(playerUUID)));

                player.sendSystemMessage(Component.literal(message.toString()));
            }
        } else {
            // Schulden aufbauen
            double debt = taxDebt.getOrDefault(playerUUID, 0.0) + totalTax;
            taxDebt.put(playerUUID, debt);

            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                    "§c§l[STEUERN] Zahlung fehlgeschlagen!\n" +
                    "§7Fällig: §c" + String.format("%.2f€", totalTax) + "\n" +
                    "§7(Einkommen: " + String.format("%.2f€", incomeTax) +
                    ", Grundsteuer: " + String.format("%.2f€", propertyTax) + ")\n" +
                    "§7Steuerschuld: §c" + String.format("%.2f€", debt) + "\n" +
                    "§cZahle innerhalb von 3 Tagen!"
                ));
            }
        }
    }

    /**
     * Gibt die aktuelle Steuerschuld eines Spielers zurück.
     *
     * <p>Steuerschulden entstehen, wenn der Kontostand während der
     * Steuererhebung nicht ausreicht. Diese müssen manuell beglichen werden.</p>
     *
     * @param playerUUID UUID des Spielers
     * @return Ausstehende Steuerschuld in Euro (0 wenn keine Schulden)
     * @see #payTaxDebt(UUID)
     */
    public double getTaxDebt(UUID playerUUID) {
        return taxDebt.getOrDefault(playerUUID, 0.0);
    }

    /**
     * Zahlt die ausstehende Steuerschuld eines Spielers.
     *
     * <p>Bei erfolgreicher Zahlung:</p>
     * <ul>
     *   <li>Abbuchung des Schuldenbetrags vom Spielerkonto</li>
     *   <li>Überweisung an Staatskasse (StateAccount)</li>
     *   <li>Entfernung der Schulden aus der taxDebt Map</li>
     *   <li>Persistierung der aktualisierten Daten</li>
     * </ul>
     *
     * @param playerUUID UUID des Spielers
     * @return {@code true} wenn Zahlung erfolgreich, {@code false} bei
     *         fehlenden Schulden oder unzureichendem Kontostand
     * @see #getTaxDebt(UUID)
     * @see EconomyManager#withdraw(UUID, double, TransactionType, String)
     */
    public boolean payTaxDebt(UUID playerUUID) {
        double debt = getTaxDebt(playerUUID);
        if (debt <= 0) {
            return false;
        }

        if (EconomyManager.withdraw(playerUUID, debt, TransactionType.TAX_INCOME, "Steuerschuld-Zahlung")) {
            StateAccount.getInstance(server).deposit(debt, "Steuerschuld");
            taxDebt.remove(playerUUID);
            save();
            return true;
        }

        return false;
    }
}
