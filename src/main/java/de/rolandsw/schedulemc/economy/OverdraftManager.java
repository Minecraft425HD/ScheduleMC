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
 * Verwaltet Überziehungskredite (Dispo)
 * - Max -5.000€ Überziehung
 * - 25% Zinsen pro Woche
 * - Automatische Pfändung bei Limit
 */
public class OverdraftManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static OverdraftManager instance;

    public static final double MAX_OVERDRAFT = -5000.0;
    private static final double WARNING_THRESHOLD = -2500.0;
    private static final double OVERDRAFT_INTEREST_RATE = 0.25; // 25% pro Woche

    private final Map<UUID, Long> lastWarningDay = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastInterestDay = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path savePath;
    private MinecraftServer server;

    private long currentDay = 0;

    private OverdraftManager(MinecraftServer server) {
        this.server = server;
        this.savePath = server.getServerDirectory().toPath().resolve("config").resolve("plotmod_overdraft.json");
        load();
    }

    public static OverdraftManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new OverdraftManager(server);
        }
        instance.server = server;
        return instance;
    }

    /**
     * Prüft ob Überziehung erlaubt ist
     */
    public static boolean canOverdraft(double newBalance) {
        return newBalance >= MAX_OVERDRAFT;
    }

    /**
     * Gibt Überziehungsbetrag zurück
     */
    public static double getOverdraftAmount(double balance) {
        if (balance >= 0) {
            return 0.0;
        }
        return Math.abs(balance);
    }

    /**
     * Tick-Methode
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
                // Warnung bei -2.500€
                if (balance <= WARNING_THRESHOLD) {
                    sendWarning(playerUUID, balance);
                }

                // Wöchentliche Überziehungszinsen
                chargeOverdraftInterest(playerUUID, balance);

                // Pfändung bei Limit
                if (balance <= MAX_OVERDRAFT) {
                    executeSeizure(playerUUID, balance);
                }
            }
        }
    }

    /**
     * Sendet Warnung an Spieler
     */
    private void sendWarning(UUID playerUUID, double balance) {
        Long lastWarning = lastWarningDay.get(playerUUID);
        if (lastWarning != null && currentDay - lastWarning < 7) {
            return; // Max 1 Warnung pro Woche
        }

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§c§l⚠ WARNUNG: KONTO ÜBERZOGEN!\n" +
                "§7Kontostand: §c" + String.format("%.2f€", balance) + "\n" +
                "§7Limit: §c-5.000€\n" +
                "§7Überziehungszinsen: §c25% pro Woche\n" +
                "§cBitte zahle Geld ein um Pfändung zu vermeiden!"
            ));
        }

        lastWarningDay.put(playerUUID, currentDay);
    }

    /**
     * Berechnet wöchentliche Überziehungszinsen
     */
    private void chargeOverdraftInterest(UUID playerUUID, double balance) {
        Long lastInterest = lastInterestDay.get(playerUUID);
        if (lastInterest != null && currentDay - lastInterest < 7) {
            return; // Nur 1x pro Woche
        }

        double overdraftAmount = getOverdraftAmount(balance);
        double interest = overdraftAmount * OVERDRAFT_INTEREST_RATE;

        // Ziehe Zinsen ab (macht Balance noch negativer)
        EconomyManager.setBalance(playerUUID, balance - interest, TransactionType.OVERDRAFT_FEE,
            "Überziehungszinsen");

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[DISPO] Überziehungszinsen\n" +
                "§7Überzogen: §c" + String.format("%.2f€", overdraftAmount) + "\n" +
                "§7Zinssatz: §c25%\n" +
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
    private void executeSeizure(UUID playerUUID, double balance) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

        if (player != null) {
            // Leere Geldbörse
            WalletManager.setBalance(playerUUID, 0.0);

            // Setze Konto auf -5000€ (nicht weiter verschlimmern)
            EconomyManager.setBalance(playerUUID, MAX_OVERDRAFT, TransactionType.OTHER,
                "Pfändung durchgeführt");

            player.sendSystemMessage(Component.literal(
                "§4§l⚠⚠⚠ PFÄNDUNG! ⚠⚠⚠\n" +
                "§cDein Konto wurde gepfändet!\n" +
                "§7Grund: Überziehungslimit erreicht\n" +
                "§7Alte Schulden: §c" + String.format("%.2f€", balance) + "\n" +
                "§7Geldbörse geleert\n" +
                "§7Konto auf §c-5.000€ §7gesetzt\n" +
                "§eZahle Schulden ab um wieder handlungsfähig zu sein!"
            ));

            LOGGER.warn("Seizure executed for {}: balance was {}€", playerUUID, balance);
        }
    }

    /**
     * Gibt Info über Dispo
     */
    public String getOverdraftInfo(UUID playerUUID) {
        double balance = EconomyManager.getBalance(playerUUID);
        double overdraft = getOverdraftAmount(balance);

        if (overdraft == 0) {
            return "§aDein Konto ist nicht überzogen.\n" +
                "§7Dispo-Limit: §e5.000€\n" +
                "§7Überziehungszinsen: §c25% pro Woche";
        }

        double available = MAX_OVERDRAFT - balance;

        return "§c§lKONTO ÜBERZOGEN!\n" +
            "§7Kontostand: §c" + String.format("%.2f€", balance) + "\n" +
            "§7Überzogen um: §c" + String.format("%.2f€", overdraft) + "\n" +
            "§7Verfügbar bis Limit: §e" + String.format("%.2f€", available) + "\n" +
            "§7Dispo-Limit: §c-5.000€\n" +
            "§7Überziehungszinsen: §c25% pro Woche";
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
                Object warningObj = data.get("lastWarningDay");
                if (warningObj instanceof Map) {
                    ((Map<String, Number>) warningObj).forEach((k, v) ->
                        lastWarningDay.put(UUID.fromString(k), v.longValue()));
                }

                Object interestObj = data.get("lastInterestDay");
                if (interestObj instanceof Map) {
                    ((Map<String, Number>) interestObj).forEach((k, v) ->
                        lastInterestDay.put(UUID.fromString(k), v.longValue()));
                }

                LOGGER.info("Loaded overdraft data");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load overdraft data", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                Map<String, Object> data = new HashMap<>();

                Map<String, Long> warningMap = new HashMap<>();
                lastWarningDay.forEach((k, v) -> warningMap.put(k.toString(), v));
                data.put("lastWarningDay", warningMap);

                Map<String, Long> interestMap = new HashMap<>();
                lastInterestDay.forEach((k, v) -> interestMap.put(k.toString(), v));
                data.put("lastInterestDay", interestMap);

                gson.toJson(data, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save overdraft data", e);
        }
    }
}
