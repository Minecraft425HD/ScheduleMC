package de.rolandsw.schedulemc.level;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.level.network.LevelUpNotificationPacket;
import de.rolandsw.schedulemc.level.network.ProducerLevelNetworkHandler;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import de.rolandsw.schedulemc.util.PersistenceHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentraler Manager für das Produzenten-Level-System.
 *
 * Verwaltet:
 * - Level-Daten aller Spieler
 * - XP-Vergabe und Level-Ups
 * - Unlock-Checks
 * - Persistenz
 *
 * Thread-Safety: Alle Methoden sind thread-safe.
 */
public class ProducerLevel implements IncrementalSaveManager.ISaveable {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Singleton
    private static volatile ProducerLevel instance;

    // Daten
    private final ConcurrentHashMap<UUID, ProducerLevelData> playerData = new ConcurrentHashMap<>();

    // Persistenz
    private static volatile File file = new File("config/schedulemc_producer_levels.json");
    private static final Gson gson = GsonHelper.get();
    private static volatile boolean needsSave = false;

    @Nullable
    private MinecraftServer server;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private ProducerLevel() {}

    public static ProducerLevel getInstance() {
        ProducerLevel localRef = instance;
        if (localRef == null) {
            synchronized (ProducerLevel.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new ProducerLevel();
                }
            }
        }
        return localRef;
    }

    public void setServer(@Nullable MinecraftServer server) {
        this.server = server;
    }

    // ═══════════════════════════════════════════════════════════
    // XP VERGABE
    // ═══════════════════════════════════════════════════════════

    /**
     * Vergibt XP an einen Spieler für eine bestimmte Aktion.
     *
     * @param playerUUID        Spieler-UUID
     * @param source            XP-Quelle
     * @param amount            Menge der Items (z.B. verkaufte Einheiten)
     * @param qualityMultiplier Qualitäts-Multiplikator
     * @return true wenn ein Level-Up stattfand
     */
    public boolean awardXP(UUID playerUUID, XPSource source, int amount, double qualityMultiplier) {
        ProducerLevelData data = getOrCreateData(playerUUID);

        int xp = source.calculateXP(amount, qualityMultiplier);
        if (xp <= 0) return false;

        int oldLevel = data.getLevel();
        boolean leveledUp = data.addXP(xp);

        if (leveledUp) {
            int newLevel = data.getLevel();
            onLevelUp(playerUUID, oldLevel, newLevel);
        }

        needsSave = true;

        LOGGER.debug("XP awarded: player={}, source={}, xp={}, level={}",
                playerUUID, source.name(), xp, data.getLevel());

        return leveledUp;
    }

    /**
     * Vergibt XP und registriert gleichzeitig einen Verkauf.
     *
     * @param playerUUID Spieler-UUID
     * @param source     XP-Quelle (SELL_*)
     * @param amount     Verkaufte Menge
     * @param quality    Qualitäts-Multiplikator
     * @param revenue    Einnahmen
     * @return true wenn Level-Up
     */
    public boolean awardSaleXP(UUID playerUUID, XPSource source, int amount,
                                double quality, double revenue) {
        ProducerLevelData data = getOrCreateData(playerUUID);
        data.recordSale(amount, source.isIllegal(), revenue);

        return awardXP(playerUUID, source, amount, quality);
    }

    // ═══════════════════════════════════════════════════════════
    // LEVEL-UP HANDLING
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird aufgerufen wenn ein Spieler ein Level aufsteigt.
     */
    private void onLevelUp(UUID playerUUID, int oldLevel, int newLevel) {
        LOGGER.info("LEVEL UP: Player {} leveled from {} to {}!", playerUUID, oldLevel, newLevel);

        // Sende Nachricht an Spieler
        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                // Level-Up Nachricht
                player.sendSystemMessage(Component.literal(
                        "§6§l★ LEVEL UP! ★ §fDu bist jetzt §6Level " + newLevel + "§f!"
                ));

                // Neue Unlockables anzeigen
                List<Unlockable> newUnlocks = getNewUnlocks(oldLevel, newLevel);
                if (!newUnlocks.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§a§lNeu freigeschaltet:"));
                    for (Unlockable unlock : newUnlocks) {
                        player.sendSystemMessage(Component.literal(
                                "  §a✔ " + unlock.getCategory().getColorCode() +
                                unlock.getDescription()
                        ));
                    }
                }

                // Smartphone-Notification senden
                List<String> unlockDescriptions = new ArrayList<>();
                for (Unlockable unlock : newUnlocks) {
                    unlockDescriptions.add(unlock.getDescription());
                }
                ProducerLevelNetworkHandler.sendToPlayer(
                        new LevelUpNotificationPacket(newLevel, unlockDescriptions), player);
            }
        }
    }

    /**
     * Gibt die Unlockables zurück die zwischen oldLevel und newLevel freigeschaltet wurden.
     */
    private List<Unlockable> getNewUnlocks(int oldLevel, int newLevel) {
        List<Unlockable> newUnlocks = new ArrayList<>();
        for (Unlockable unlock : Unlockable.values()) {
            int req = unlock.getRequiredLevel();
            if (req > oldLevel && req <= newLevel) {
                newUnlocks.add(unlock);
            }
        }
        return newUnlocks;
    }

    // ═══════════════════════════════════════════════════════════
    // UNLOCK CHECKS (für andere Systeme)
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob ein Spieler einen bestimmten Inhalt freigeschaltet hat.
     *
     * @param playerUUID Spieler-UUID
     * @param unlockable Freischaltbarer Inhalt
     * @return true wenn freigeschaltet
     */
    public boolean isUnlocked(UUID playerUUID, Unlockable unlockable) {
        ProducerLevelData data = playerData.get(playerUUID);
        if (data == null) return unlockable.getRequiredLevel() <= 0;
        return data.isUnlocked(unlockable);
    }

    /**
     * Prüft ob ein Spieler ein bestimmtes Level erreicht hat.
     */
    public boolean hasLevel(UUID playerUUID, int level) {
        ProducerLevelData data = playerData.get(playerUUID);
        if (data == null) return level <= 0;
        return data.getLevel() >= level;
    }

    /**
     * Gibt das Level eines Spielers zurück.
     */
    public int getPlayerLevel(UUID playerUUID) {
        ProducerLevelData data = playerData.get(playerUUID);
        return data != null ? data.getLevel() : 0;
    }

    /**
     * Gibt die Level-Daten eines Spielers zurück.
     */
    @Nullable
    public ProducerLevelData getPlayerData(UUID playerUUID) {
        return playerData.get(playerUUID);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN FUNKTIONEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt das Level eines Spielers (Admin-Befehl).
     */
    public void setLevel(UUID playerUUID, int level) {
        level = Math.max(0, Math.min(LevelRequirements.MAX_LEVEL, level));
        ProducerLevelData data = getOrCreateData(playerUUID);

        // XP auf das Minimum des Ziel-Levels setzen
        int requiredXP = LevelRequirements.getRequiredXP(level);

        // Neue Daten erstellen mit dem gesetzten Level
        ProducerLevelData newData = new ProducerLevelData(
                playerUUID, level, requiredXP, data.getUnlockedItems(),
                data.getTotalItemsSold(), data.getTotalIllegalSold(),
                data.getTotalLegalSold(), data.getTotalRevenue()
        );

        // Unlocks aktualisieren
        for (Unlockable unlock : Unlockable.values()) {
            if (unlock.isUnlockedAt(level)) {
                newData.getUnlockedItems().add(unlock.name());
            }
        }

        playerData.put(playerUUID, newData);
        needsSave = true;

        LOGGER.info("Admin: Level for {} set to {}", playerUUID, level);
    }

    /**
     * Gibt die Top-Spieler nach Level zurück.
     */
    public List<Map.Entry<UUID, Integer>> getTopPlayers(int count) {
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>();
        playerData.forEach((uuid, data) -> entries.add(Map.entry(uuid, data.getLevel())));
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return entries.subList(0, Math.min(count, entries.size()));
    }

    // ═══════════════════════════════════════════════════════════
    // DATA MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    private ProducerLevelData getOrCreateData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, ProducerLevelData::new);
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    private static final Type DATA_MAP_TYPE = new TypeToken<Map<String, SavedLevelData>>(){}.getType();

    public void loadData() {
        PersistenceHelper.LoadResult<Map<String, SavedLevelData>> result =
                PersistenceHelper.load(file, gson, DATA_MAP_TYPE, "ProducerLevel");

        if (result.isSuccess() && result.hasData()) {
            Map<String, SavedLevelData> loaded = result.getData();
            playerData.clear();

            for (Map.Entry<String, SavedLevelData> entry : loaded.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    SavedLevelData saved = entry.getValue();

                    ProducerLevelData data = new ProducerLevelData(
                            uuid, saved.level, saved.totalXP,
                            saved.unlockedItems != null ? saved.unlockedItems : new HashSet<>(),
                            saved.totalItemsSold, saved.totalIllegalSold,
                            saved.totalLegalSold, saved.totalRevenue
                    );

                    playerData.put(uuid, data);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in level data: {}", entry.getKey());
                }
            }

            LOGGER.info("ProducerLevel loaded: {} players", playerData.size());
        }
    }

    public void saveData() {
        Map<String, SavedLevelData> saveMap = new HashMap<>();

        playerData.forEach((uuid, data) -> {
            SavedLevelData saved = new SavedLevelData();
            saved.level = data.getLevel();
            saved.totalXP = data.getTotalXP();
            saved.unlockedItems = data.getUnlockedItems();
            saved.totalItemsSold = data.getTotalItemsSold();
            saved.totalIllegalSold = data.getTotalIllegalSold();
            saved.totalLegalSold = data.getTotalLegalSold();
            saved.totalRevenue = data.getTotalRevenue();
            saveMap.put(uuid.toString(), saved);
        });

        PersistenceHelper.SaveResult saveResult =
                PersistenceHelper.save(file, gson, saveMap, "ProducerLevel");

        if (saveResult.isSuccess()) {
            needsSave = false;
        }
    }

    /**
     * Interne Datenstruktur für JSON-Serialisierung
     */
    private static class SavedLevelData {
        int level;
        int totalXP;
        Set<String> unlockedItems;
        int totalItemsSold;
        int totalIllegalSold;
        int totalLegalSold;
        double totalRevenue;
    }

    // ═══════════════════════════════════════════════════════════
    // INCREMENTAL SAVE MANAGER
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isDirty() {
        return needsSave;
    }

    @Override
    public void save() {
        saveData();
    }

    @Override
    public String getName() {
        return "ProducerLevel";
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
