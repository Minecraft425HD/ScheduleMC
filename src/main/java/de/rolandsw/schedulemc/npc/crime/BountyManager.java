package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Verwaltet Kopfgelder (Bounties) auf Spieler
 * Extends AbstractPersistenceManager
 */
public class BountyManager extends AbstractPersistenceManager<Map<UUID, BountyData>> {
    private static BountyManager instance;

    private static final double AUTO_BOUNTY_PER_STAR = 2000.0; // 2000€ pro Wanted-Star
    private static final int MIN_WANTED_LEVEL_FOR_BOUNTY = 3;  // Ab 3 Stars

    private final Map<UUID, BountyData> activeBounties = new ConcurrentHashMap<>();
    private final Map<UUID, List<BountyData>> bountyHistory = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private BountyManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_bounties.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    public static BountyManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new BountyManager(server);
        }
        instance.server = server;
        return instance;
    }

    @Nullable
    public static BountyManager getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // BOUNTY MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Platziert automatisches Bounty bei hohem Wanted-Level
     */
    public void createAutoBounty(UUID criminal, int wantedLevel) {
        if (wantedLevel < MIN_WANTED_LEVEL_FOR_BOUNTY) {
            return; // Zu niedriger Wanted-Level
        }

        // Berechne Kopfgeld
        double bountyAmount = wantedLevel * AUTO_BOUNTY_PER_STAR;

        // Existiert bereits ein Bounty?
        BountyData existing = activeBounties.get(criminal);
        if (existing != null && existing.isActive()) {
            // Erhöhe bestehendes Bounty
            existing.increaseAmount(AUTO_BOUNTY_PER_STAR);
            LOGGER.info("Increased bounty for {}: +{}", criminal, AUTO_BOUNTY_PER_STAR);
        } else {
            // Erstelle neues Bounty
            BountyData bounty = new BountyData(criminal, bountyAmount, null,
                "Wanted Level: " + wantedLevel + " ⭐");
            activeBounties.put(criminal, bounty);
            LOGGER.info("Created auto-bounty for {}: {}", criminal, bountyAmount);

            // Benachrichtige Spieler
            ServerPlayer player = server.getPlayerList().getPlayer(criminal);
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                    "§c§l⚠ KOPFGELD PLATZIERT!\n" +
                    "§7Auf deinen Kopf wurde ein Kopfgeld von §a" +
                    String.format("%.2f€", bountyAmount) + " §7ausgesetzt!"
                ));
            }
        }

        save();
    }

    /**
     * Platziert manuelles Bounty (von Spieler)
     */
    public boolean placeBounty(UUID placerUUID, UUID targetUUID, double amount, String reason) {
        // Validierung
        if (amount <= 0) {
            return false;
        }

        if (placerUUID.equals(targetUUID)) {
            return false; // Kann nicht auf sich selbst bounty platzieren
        }

        // Prüfe Kontostand
        if (!EconomyManager.withdraw(placerUUID, amount, TransactionType.OTHER,
                "Kopfgeld auf: " + targetUUID)) {
            return false; // Nicht genug Geld
        }

        // Existiert bereits ein Bounty?
        BountyData existing = activeBounties.get(targetUUID);
        if (existing != null && existing.isActive()) {
            // Erhöhe bestehendes Bounty
            existing.increaseAmount(amount);
        } else {
            // Erstelle neues Bounty
            BountyData bounty = new BountyData(targetUUID, amount, placerUUID, reason);
            activeBounties.put(targetUUID, bounty);
        }

        // Benachrichtige Target
        ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
        if (target != null) {
            target.sendSystemMessage(Component.literal(
                "§c§l⚠ KOPFGELD ERHÖHT!\n" +
                "§7Jemand hat §a" + String.format("%.2f€", amount) +
                " §7auf deinen Kopf ausgesetzt!\n" +
                "§7Grund: §e" + reason
            ));
        }

        LOGGER.info("Player {} placed bounty on {}: {}", placerUUID, targetUUID, amount);
        save();
        return true;
    }

    /**
     * Löst Kopfgeld ein (nach Verhaftung/Kill)
     */
    public boolean claimBounty(UUID hunterUUID, UUID targetUUID) {
        BountyData bounty = activeBounties.get(targetUUID);

        if (bounty == null || !bounty.isActive()) {
            return false; // Kein aktives Bounty
        }

        // Bounty einlösen
        if (!bounty.claim(hunterUUID)) {
            return false;
        }

        // Belohnung auszahlen
        double reward = bounty.getAmount();
        EconomyManager.deposit(hunterUUID, reward, TransactionType.OTHER,
            "Kopfgeld: " + targetUUID);

        // Zu Historie hinzufügen
        bountyHistory.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(bounty);

        // Aus aktiven Bounties entfernen
        activeBounties.remove(targetUUID);

        // Benachrichtige Hunter
        ServerPlayer hunter = server.getPlayerList().getPlayer(hunterUUID);
        if (hunter != null) {
            hunter.sendSystemMessage(Component.literal(
                "§a§l✓ KOPFGELD EINGELÖST!\n" +
                "§7Du hast §a" + String.format("%.2f€", reward) + " §7erhalten!"
            ));
        }

        // Benachrichtige Target
        ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
        if (target != null) {
            target.sendSystemMessage(Component.literal(
                "§c§lDein Kopfgeld wurde eingelöst!"
            ));
        }

        LOGGER.info("Bounty claimed: {} -> {}, amount: {}", targetUUID, hunterUUID, reward);
        save();
        return true;
    }

    /**
     * Gibt aktives Bounty für Spieler zurück
     */
    @Nullable
    public BountyData getActiveBounty(UUID playerUUID) {
        BountyData bounty = activeBounties.get(playerUUID);
        if (bounty != null && bounty.isActive()) {
            return bounty;
        }
        return null;
    }

    /**
     * Gibt alle aktiven Bounties zurück
     */
    public List<BountyData> getAllActiveBounties() {
        return activeBounties.values().stream()
            .filter(BountyData::isActive)
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount())) // Höchste zuerst
            .collect(Collectors.toList());
    }

    /**
     * Gibt Top-Bounties zurück
     */
    public List<BountyData> getTopBounties(int limit) {
        return getAllActiveBounties().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Gibt Bounty-Historie für Spieler zurück
     */
    public List<BountyData> getBountyHistory(UUID playerUUID) {
        return bountyHistory.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Entfernt abgelaufene Bounties
     */
    public void cleanupExpiredBounties() {
        int removed = 0;
        Iterator<Map.Entry<UUID, BountyData>> it = activeBounties.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, BountyData> entry = it.next();
            BountyData bounty = entry.getValue();

            if (bounty.isExpired()) {
                bountyHistory.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(bounty);
                it.remove();
                removed++;
            }
        }

        if (removed > 0) {
            LOGGER.info("Removed {} expired bounties", removed);
            save();
        }
    }

    /**
     * Gibt Statistiken zurück
     */
    public String getStatistics() {
        int active = (int) activeBounties.values().stream().filter(BountyData::isActive).count();
        double totalAmount = activeBounties.values().stream()
            .filter(BountyData::isActive)
            .mapToDouble(BountyData::getAmount)
            .sum();

        return String.format("Active Bounties: %d, Total: %.2f€", active, totalAmount);
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, BountyData>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, BountyData> data) {
        activeBounties.clear();
        activeBounties.putAll(data);
        cleanupExpiredBounties();
    }

    @Override
    protected Map<UUID, BountyData> getCurrentData() {
        return new HashMap<>(activeBounties);
    }

    @Override
    protected String getComponentName() {
        return "BountyManager";
    }

    @Override
    protected String getHealthDetails() {
        return getStatistics();
    }

    @Override
    protected void onCriticalLoadFailure() {
        activeBounties.clear();
        bountyHistory.clear();
    }
}
