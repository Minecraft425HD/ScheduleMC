package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Zinsen auf Konten
 * - Reguläres Konto: 2% pro Woche
 * - Sparkonten: 5% pro Woche (separate Manager)
 * - Max 10.000€ Zinsen pro Woche (verhindert Inflation)
 */
public class InterestManager extends AbstractPersistenceManager<Map<UUID, Long>> {
    private static InterestManager instance;

    private static final double INTEREST_RATE = 0.02; // 2% pro Woche
    private static final double MAX_INTEREST_PER_WEEK = 10000.0;
    private static final long WEEK_IN_DAYS = 7;

    private final Map<UUID, Long> lastInterestPayout = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private long currentDay = 0;

    private InterestManager(MinecraftServer server) {
        super(
            new File(server.getServerDirectory().toPath().resolve("config").resolve("plotmod_interest.json").toString()),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, Long>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, Long> data) {
        lastInterestPayout.clear();
        lastInterestPayout.putAll(data);
    }

    @Override
    protected Map<UUID, Long> getCurrentData() {
        return new HashMap<>(lastInterestPayout);
    }

    public static InterestManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new InterestManager(server);
        }
        instance.server = server;
        return instance;
    }

    /**
     * Tick-Methode - aufgerufen jeden Server-Tick oder Tag-Wechsel
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            checkWeeklyPayouts();
        }
    }

    /**
     * Prüft und zahlt wöchentliche Zinsen
     */
    private void checkWeeklyPayouts() {
        Map<UUID, Double> balances = EconomyManager.getAllAccounts();

        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            UUID playerUUID = entry.getKey();
            double balance = entry.getValue();

            long lastPayout = lastInterestPayout.getOrDefault(playerUUID, 0L);
            long daysSinceLastPayout = currentDay - lastPayout;

            if (daysSinceLastPayout >= WEEK_IN_DAYS) {
                payoutInterest(playerUUID, balance);
                lastInterestPayout.put(playerUUID, currentDay);
            }
        }

        save();
    }

    @Override
    protected String getComponentName() {
        return "InterestManager";
    }

    @Override
    protected String getHealthDetails() {
        return lastInterestPayout.size() + " accounts tracked";
    }

    @Override
    protected void onCriticalLoadFailure() {
        lastInterestPayout.clear();
        LOGGER.warn("InterestManager: Gestartet mit leeren Daten nach kritischem Fehler");
    }

    /**
     * Zahlt Zinsen an einen Spieler
     */
    private void payoutInterest(UUID playerUUID, double balance) {
        if (balance <= 0) {
            return;
        }

        double interest = balance * INTEREST_RATE;
        interest = Math.min(interest, MAX_INTEREST_PER_WEEK);

        EconomyManager.deposit(playerUUID, interest, TransactionType.INTEREST,
            String.format("Wöchentliche Zinsen (%.1f%%)", INTEREST_RATE * 100));

        // Benachrichtige Spieler wenn online
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                "§a§l[BANK] Zinsen gutgeschrieben!\n" +
                "§7Rate: §e2.0%\n" +
                "§7Betrag: §a+" + String.format("%.2f€", interest) + "\n" +
                "§7Neuer Kontostand: §6" + String.format("%.2f€", EconomyManager.getBalance(playerUUID))
            ));
        }
    }

    /**
     * Gibt nächsten Auszahlungstag zurück
     */
    public long getDaysUntilNextPayout(UUID playerUUID) {
        long lastPayout = lastInterestPayout.getOrDefault(playerUUID, 0L);
        long daysSince = currentDay - lastPayout;
        return Math.max(0, WEEK_IN_DAYS - daysSince);
    }

    /**
     * Berechnet voraussichtliche Zinsen
     */
    public double calculateNextInterest(UUID playerUUID) {
        double balance = EconomyManager.getBalance(playerUUID);
        double interest = balance * INTEREST_RATE;
        return Math.min(interest, MAX_INTEREST_PER_WEEK);
    }
}
