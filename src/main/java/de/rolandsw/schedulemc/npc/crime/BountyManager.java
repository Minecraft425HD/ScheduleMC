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
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile BountyManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    private static final double AUTO_BOUNTY_PER_STAR = 2000.0; // 2000€ pro Wanted-Star
    private static final int MIN_WANTED_LEVEL_FOR_BOUNTY = 3;  // Ab 3 Stars

    private final Map<UUID, BountyData> activeBounties = new ConcurrentHashMap<>();
    private final Map<UUID, List<BountyData>> bountyHistory = new ConcurrentHashMap<>();
    private volatile MinecraftServer server;

    private BountyManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_bounties.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * SICHERHEIT: Thread-safe Singleton mit Double-Checked Locking
     */
    public static BountyManager initialize(MinecraftServer server) {
        BountyManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new BountyManager(server);
                }
            }
        }
        result.server = server;
        return result;
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
    /** Kopfgeld pro Stern bei Zeugenbericht-Eingang. */
    public static final double REPORT_BOUNTY_PER_STAR = 200.0;
    /** Extra-Kopfgeld bei voller Fahndungsstufe (5 Sterne). */
    public static final double REPORT_BOUNTY_MAX_LEVEL_EXTRA = 1000.0;
    /** Steuersatz auf Kopfgeld-Auszahlungen. */
    public static final double BOUNTY_TAX_RATE = 0.19;

    /**
     * Server-Kopfgeld nach eingegangenem Zeugenbericht: 200€ pro Stern,
     * bei 5 Sternen 1000€ extra. Erhöht ein bestehendes Kopfgeld auf
     * mindestens diesen Betrag.
     *
     * @return das aktuelle Gesamt-Kopfgeld nach der Anpassung
     */
    public double placeServerBounty(UUID criminal, int wantedLevel) {
        if (wantedLevel <= 0) {
            BountyData current = activeBounties.get(criminal);
            return current != null && current.isActive() ? current.getAmount() : 0.0;
        }
        double target = wantedLevel * REPORT_BOUNTY_PER_STAR
            + (wantedLevel >= 5 ? REPORT_BOUNTY_MAX_LEVEL_EXTRA : 0.0);

        BountyData existing = activeBounties.get(criminal);
        if (existing != null && existing.isActive()) {
            if (existing.getAmount() < target) {
                existing.increaseAmount(target - existing.getAmount());
            }
            save();
            return existing.getAmount();
        }
        BountyData bounty = new BountyData(criminal, target, null,
            "Witness report, wanted level " + wantedLevel + " ⭐");
        activeBounties.put(criminal, bounty);
        save();
        return target;
    }

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
                player.sendSystemMessage(Component.translatable("manager.bounty.placed", String.format("%.2f€", bountyAmount)));
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
            target.sendSystemMessage(Component.translatable("manager.bounty.increased", String.format("%.2f€", amount), reason));
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

        // Belohnung auszahlen: 19% Steuer gehen an die Staatskasse,
        // der Rest direkt aufs Konto des Jägers
        double gross = bounty.getAmount();
        double tax = Math.round(gross * BOUNTY_TAX_RATE * 100.0) / 100.0;
        double reward = gross - tax;
        EconomyManager.deposit(hunterUUID, reward, TransactionType.OTHER,
            "Bounty (after 19% tax): " + targetUUID);
        de.rolandsw.schedulemc.economy.StateAccount.getInstance(server)
            .deposit(tax, "Bounty tax (19%)");

        // Zu Historie hinzufügen
        bountyHistory.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(bounty);

        // Aus aktiven Bounties entfernen
        activeBounties.remove(targetUUID);

        // Benachrichtige Hunter
        ServerPlayer hunter = server.getPlayerList().getPlayer(hunterUUID);
        if (hunter != null) {
            hunter.sendSystemMessage(Component.translatable("manager.bounty.claimed_taxed",
                String.format("%.2f", reward), String.format("%.2f", tax)));
        }

        // Benachrichtige Target
        ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
        if (target != null) {
            target.sendSystemMessage(Component.translatable("manager.bounty.claimed.target"));
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
     * OPTIMIERUNG: Single-pass statt doppelter Stream-Operation
     */
    public String getStatistics() {
        int active = 0;
        double totalAmount = 0.0;

        for (BountyData bounty : activeBounties.values()) {
            if (bounty.isActive()) {
                active++;
                totalAmount += bounty.getAmount();
            }
        }

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

        int invalidCount = 0;
        int correctedCount = 0;

        // NULL CHECK
        if (data == null) {
            LOGGER.warn("Null data loaded for bounties");
            return;
        }

        // Check collection size
        if (data.size() > 10000) {
            LOGGER.warn("Bounties map size ({}) exceeds limit, potential corruption",
                data.size());
            correctedCount++;
        }

        for (Map.Entry<UUID, BountyData> entry : data.entrySet()) {
            try {
                UUID targetUUID = entry.getKey();
                BountyData bounty = entry.getValue();

                // NULL CHECK
                if (targetUUID == null) {
                    LOGGER.warn("Null target UUID in bounties, skipping");
                    invalidCount++;
                    continue;
                }
                if (bounty == null) {
                    LOGGER.warn("Null bounty for target {}, skipping", targetUUID);
                    invalidCount++;
                    continue;
                }

                activeBounties.put(targetUUID, bounty);
            } catch (Exception e) {
                LOGGER.error("Error loading bounty for target {}", entry.getKey(), e);
                invalidCount++;
            }
        }

        cleanupExpiredBounties();

        // SUMMARY
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
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
