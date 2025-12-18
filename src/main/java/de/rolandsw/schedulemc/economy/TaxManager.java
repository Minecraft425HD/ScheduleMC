package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Steuersystem für ScheduleMC
 * - Einkommenssteuer: Progressiv (0%, 10%, 15%, 20%)
 * - Monatliche Abrechnung (alle 7 MC-Tage)
 */
public class TaxManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static TaxManager instance;

    // Steuerstufen
    private static final double TAX_FREE_AMOUNT = 10000.0;
    private static final double TAX_BRACKET_1 = 50000.0; // 10%
    private static final double TAX_BRACKET_2 = 100000.0; // 15%
    // Darüber: 20%

    private static final int TAX_PERIOD_DAYS = 7; // 1 Woche

    private final Map<UUID, Long> lastTaxDay = new ConcurrentHashMap<>();
    private final Map<UUID, Double> taxDebt = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path savePath;
    private MinecraftServer server;

    private long currentDay = 0;

    private TaxManager(MinecraftServer server) {
        this.server = server;
        this.savePath = server.getServerDirectory().toPath().resolve("config").resolve("plotmod_taxes.json");
        load();
    }

    public static TaxManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new TaxManager(server);
        }
        instance.server = server;
        return instance;
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
        double tax = calculateIncomeTax(balance);

        if (tax <= 0) {
            return;
        }

        // Versuche Abbuchung
        if (EconomyManager.withdraw(playerUUID, tax, TransactionType.TAX_INCOME, "Einkommenssteuer")) {
            StateAccount.getInstance(server).deposit(tax, "Einkommenssteuer");

            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                    "§e§l[STEUERN] Monatliche Abrechnung\n" +
                    "§7Kontostand: §6" + String.format("%.2f€", balance) + "\n" +
                    "§7Steuern: §c-" + String.format("%.2f€", tax) + "\n" +
                    "§7Neuer Kontostand: §6" + String.format("%.2f€", EconomyManager.getBalance(playerUUID))
                ));
            }

            LOGGER.info("Tax collected: {} € from {}", tax, playerUUID);
        } else {
            // Schulden aufbauen
            double debt = taxDebt.getOrDefault(playerUUID, 0.0) + tax;
            taxDebt.put(playerUUID, debt);

            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                    "§c§l[STEUERN] Zahlung fehlgeschlagen!\n" +
                    "§7Fällig: §c" + String.format("%.2f€", tax) + "\n" +
                    "§7Steuerschuld: §c" + String.format("%.2f€", debt) + "\n" +
                    "§cZahle innerhalb von 3 Tagen!"
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

    // Persistence
    private void load() {
        if (!Files.exists(savePath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(savePath)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> data = gson.fromJson(reader, type);

            if (data != null) {
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

                LOGGER.info("Loaded tax data");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load tax data", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                Map<String, Object> data = new HashMap<>();

                Map<String, Long> lastTaxMap = new HashMap<>();
                lastTaxDay.forEach((k, v) -> lastTaxMap.put(k.toString(), v));
                data.put("lastTaxDay", lastTaxMap);

                Map<String, Double> debtMap = new HashMap<>();
                taxDebt.forEach((k, v) -> debtMap.put(k.toString(), v));
                data.put("taxDebt", debtMap);

                gson.toJson(data, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save tax data", e);
        }
    }
}
