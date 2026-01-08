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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Steuersystem für ScheduleMC
 * - Einkommenssteuer: Progressiv (0%, 10%, 15%, 20%)
 * - Grundsteuer: 100€ pro Chunk pro Monat
 * - Monatliche Abrechnung (alle 7 MC-Tage)
 */
public class TaxManager extends AbstractPersistenceManager<Map<String, Object>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile TaxManager instance;

    // Steuerstufen
    private static final double TAX_FREE_AMOUNT = 10000.0;
    private static final double TAX_BRACKET_1 = 50000.0; // 10%
    private static final double TAX_BRACKET_2 = 100000.0; // 15%
    // Darüber: 20%

    private static final int TAX_PERIOD_DAYS = 7; // 1 Woche

    private final Map<UUID, Long> lastTaxDay = new ConcurrentHashMap<>();
    private final Map<UUID, Double> taxDebt = new ConcurrentHashMap<>();
    private MinecraftServer server;

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
            ((Map<String, Number>) lastTaxObj).forEach((k, v) ->
                lastTaxDay.put(UUID.fromString(k), v.longValue()));
        }

        // Load taxDebt
        Object debtObj = data.get("taxDebt");
        if (debtObj instanceof Map) {
            ((Map<String, Number>) debtObj).forEach((k, v) ->
                taxDebt.put(UUID.fromString(k), v.doubleValue()));
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
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static TaxManager getInstance(MinecraftServer server) {
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
     * Tick-Methode
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            processTaxes();
        }
    }

    /**
     * Berechnet Einkommenssteuer
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
     * Berechnet Grundsteuer basierend auf Grundbesitz
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
                if (incomeTax > 0 && propertyTax > 0) {
                    double taxPerChunk = ModConfigHandler.COMMON.TAX_PROPERTY_PER_CHUNK.get();
                    int chunks = (int)(propertyTax / taxPerChunk);
                    player.sendSystemMessage(Component.translatable("manager.tax.charged_both",
                        String.format("%.2f€", balance),
                        String.format("%.2f€", incomeTax),
                        String.format("%.2f€", propertyTax),
                        String.valueOf(chunks),
                        String.format("%.2f€", totalTax),
                        String.format("%.2f€", EconomyManager.getBalance(playerUUID))
                    ));
                } else if (incomeTax > 0) {
                    player.sendSystemMessage(Component.translatable("manager.tax.charged_income",
                        String.format("%.2f€", balance),
                        String.format("%.2f€", incomeTax),
                        String.format("%.2f€", totalTax),
                        String.format("%.2f€", EconomyManager.getBalance(playerUUID))
                    ));
                } else if (propertyTax > 0) {
                    double taxPerChunk = ModConfigHandler.COMMON.TAX_PROPERTY_PER_CHUNK.get();
                    int chunks = (int)(propertyTax / taxPerChunk);
                    player.sendSystemMessage(Component.translatable("manager.tax.charged_property",
                        String.format("%.2f€", balance),
                        String.format("%.2f€", propertyTax),
                        String.valueOf(chunks),
                        String.format("%.2f€", totalTax),
                        String.format("%.2f€", EconomyManager.getBalance(playerUUID))
                    ));
                }
            }
        } else {
            // Schulden aufbauen
            double debt = taxDebt.getOrDefault(playerUUID, 0.0) + totalTax;
            taxDebt.put(playerUUID, debt);

            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                player.sendSystemMessage(Component.translatable("manager.tax.payment_failed",
                    String.format("%.2f€", totalTax),
                    String.format("%.2f€", incomeTax),
                    String.format("%.2f€", propertyTax),
                    String.format("%.2f€", debt)
                ));
            }
        }
    }

    /**
     * Gibt Steuerschuld zurück
     */
    public double getTaxDebt(UUID playerUUID) {
        return taxDebt.getOrDefault(playerUUID, 0.0);
    }

    /**
     * Zahlt Steuerschuld
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
