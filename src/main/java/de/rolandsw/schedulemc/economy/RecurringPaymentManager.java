package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Daueraufträge
 * Refactored mit AbstractPersistenceManager
 */
public class RecurringPaymentManager extends AbstractPersistenceManager<Map<UUID, List<RecurringPayment>>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile RecurringPaymentManager instance;

    private final Map<UUID, List<RecurringPayment>> payments = new ConcurrentHashMap<>();
    private MinecraftServer server;

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
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static RecurringPaymentManager getInstance(MinecraftServer server) {
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
     * Erstellt neuen Dauerauftrag
     */
    public boolean createRecurringPayment(UUID fromPlayer, UUID toPlayer, double amount,
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
                "§7Betrag: §e" + String.format("%.2f€", amount) + "\n" +
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
     * Löscht Dauerauftrag
     */
    public boolean deleteRecurringPayment(UUID playerUUID, String paymentId) {
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
                "§7Betrag: §e" + String.format("%.2f€", payment.getAmount())
            ));
        }

        save();
        return true;
    }

    /**
     * Pausiert Dauerauftrag
     */
    public boolean pauseRecurringPayment(UUID playerUUID, String paymentId) {
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
     * Aktiviert Dauerauftrag wieder
     */
    public boolean resumeRecurringPayment(UUID playerUUID, String paymentId) {
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
     * Tick-Methode
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            processPayments();
        }
    }

    /**
     * Verarbeitet fällige Daueraufträge
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
                            "§7Betrag: §c-" + String.format("%.2f€", payment.getAmount()) + "\n" +
                            "§7Beschreibung: §f" + payment.getDescription()
                        ));
                    }

                    if (recipient != null) {
                        recipient.sendSystemMessage(Component.literal(
                            "§a[DAUERAUFTRAG] Erhalten\n" +
                            "§7Von: §e" + playerUUID + "\n" +
                            "§7Betrag: §a+" + String.format("%.2f€", payment.getAmount()) + "\n" +
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
                                "§7Betrag: §e" + String.format("%.2f€", payment.getAmount()) + "\n" +
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
     * Gibt alle Daueraufträge eines Spielers zurück
     */
    public List<RecurringPayment> getPayments(UUID playerUUID) {
        return payments.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Findet Dauerauftrag
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
