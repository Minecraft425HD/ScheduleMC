package de.rolandsw.schedulemc.achievement;
nimport de.rolandsw.schedulemc.util.StringUtils;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Verwaltet Achievement-System
 * Extends AbstractPersistenceManager für robuste Persistenz
 */
public class AchievementManager extends AbstractPersistenceManager<Map<UUID, PlayerAchievements>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile AchievementManager instance;

    private final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private final Map<UUID, PlayerAchievements> playerData = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private AchievementManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_achievements.json").toFile(),
            GsonHelper.get() // Umgebungsabhängig: kompakt in Produktion
        );
        this.server = server;
        registerAchievements();
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static AchievementManager getInstance(MinecraftServer server) {
        AchievementManager localRef = instance;
        if (localRef == null) {
            synchronized (AchievementManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new AchievementManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    @Nullable
    public static AchievementManager getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // ACHIEVEMENT REGISTRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert alle Achievements
     */
    private void registerAchievements() {
        // ========== ECONOMY ACHIEVEMENTS ==========
        register(new Achievement(
            "FIRST_EURO",
            "Erster Euro",
            "Verdiene dein erstes Geld",
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "RICH",
            "Wohlhabend",
            "Erreiche 10.000€ Kontostand",
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            10000.0,
            false
        ));

        register(new Achievement(
            "WEALTHY",
            "Reich",
            "Erreiche 100.000€ Kontostand",
            AchievementCategory.ECONOMY,
            AchievementTier.SILVER,
            100000.0,
            false
        ));

        register(new Achievement(
            "MILLIONAIRE",
            "Millionär",
            "Erreiche 1.000.000€ Kontostand",
            AchievementCategory.ECONOMY,
            AchievementTier.GOLD,
            1000000.0,
            false
        ));

        register(new Achievement(
            "LOAN_MASTER",
            "Kreditmeister",
            "Zahle 10 Kredite vollständig ab",
            AchievementCategory.ECONOMY,
            AchievementTier.SILVER,
            10.0,
            false
        ));

        register(new Achievement(
            "SAVINGS_KING",
            "Sparkönig",
            "Spare 100.000€ auf Sparkonten an",
            AchievementCategory.ECONOMY,
            AchievementTier.GOLD,
            100000.0,
            false
        ));

        register(new Achievement(
            "BIG_SPENDER",
            "Großverdiener",
            "Verdiene 1.000.000€ insgesamt",
            AchievementCategory.ECONOMY,
            AchievementTier.DIAMOND,
            1000000.0,
            false
        ));

        // ========== CRIME ACHIEVEMENTS ==========
        register(new Achievement(
            "FIRST_CRIME",
            "Erstes Verbrechen",
            "Erhalte deine ersten Wanted-Sterne",
            AchievementCategory.CRIME,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "WANTED",
            "Gesucht",
            "Erreiche 3 Wanted-Sterne",
            AchievementCategory.CRIME,
            AchievementTier.SILVER,
            3.0,
            false
        ));

        register(new Achievement(
            "MOST_WANTED",
            "Meist Gesucht",
            "Erreiche 5 Wanted-Sterne",
            AchievementCategory.CRIME,
            AchievementTier.GOLD,
            5.0,
            false
        ));

        register(new Achievement(
            "ESCAPE_ARTIST",
            "Fluchtkünstler",
            "Entkomme 10x der Polizei",
            AchievementCategory.CRIME,
            AchievementTier.SILVER,
            10.0,
            false
        ));

        register(new Achievement(
            "PRISON_VETERAN",
            "Knast-Veteran",
            "Verbringe 100 Tage im Gefängnis",
            AchievementCategory.CRIME,
            AchievementTier.GOLD,
            100.0,
            false
        ));

        register(new Achievement(
            "CLEAN_RECORD",
            "Unbescholten",
            "30 Tage ohne Verbrechen",
            AchievementCategory.CRIME,
            AchievementTier.DIAMOND,
            30.0,
            false
        ));

        // ========== PRODUCTION ACHIEVEMENTS ==========
        register(new Achievement(
            "HOBBYIST",
            "Hobbygärtner",
            "Pflanze 100 Pflanzen an",
            AchievementCategory.PRODUCTION,
            AchievementTier.BRONZE,
            100.0,
            false
        ));

        register(new Achievement(
            "FARMER",
            "Bauer",
            "Produziere 100kg Drogen",
            AchievementCategory.PRODUCTION,
            AchievementTier.SILVER,
            100.0,
            false
        ));

        register(new Achievement(
            "PRODUCER",
            "Produzent",
            "Produziere 1.000kg Drogen",
            AchievementCategory.PRODUCTION,
            AchievementTier.GOLD,
            1000.0,
            false
        ));

        register(new Achievement(
            "DRUG_LORD",
            "Drogenboß",
            "Produziere 10.000kg Drogen",
            AchievementCategory.PRODUCTION,
            AchievementTier.DIAMOND,
            10000.0,
            false
        ));

        register(new Achievement(
            "EMPIRE_BUILDER",
            "Imperium",
            "Besitze 10 Produktionsstätten",
            AchievementCategory.PRODUCTION,
            AchievementTier.PLATINUM,
            10.0,
            false
        ));

        // ========== SOCIAL ACHIEVEMENTS ==========
        register(new Achievement(
            "FIRST_PLOT",
            "Erste Immobilie",
            "Kaufe deinen ersten Plot",
            AchievementCategory.SOCIAL,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "PROPERTY_MOGUL",
            "Immobilienmogul",
            "Besitze 5 Plots",
            AchievementCategory.SOCIAL,
            AchievementTier.GOLD,
            5.0,
            false
        ));

        register(new Achievement(
            "LANDLORD",
            "Vermieter",
            "Verdiene 100.000€ Mieteinnahmen",
            AchievementCategory.SOCIAL,
            AchievementTier.DIAMOND,
            100000.0,
            false
        ));

        register(new Achievement(
            "POPULAR",
            "Beliebt",
            "Erhalte 50 positive Ratings",
            AchievementCategory.SOCIAL,
            AchievementTier.GOLD,
            50.0,
            false
        ));

        LOGGER.info("Registered {} achievements", achievements.size());
    }

    private void register(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }

    // ═══════════════════════════════════════════════════════════
    // ACHIEVEMENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Retrieves the achievement data for the specified player.
     *
     * If no achievement data exists for this player, a new PlayerAchievements instance
     * is automatically created and registered. This method is thread-safe and uses
     * atomic compute operations to prevent race conditions.
     *
     * @param playerUUID The unique identifier of the player
     * @return The PlayerAchievements instance containing all achievement data for this player
     */
    public PlayerAchievements getPlayerAchievements(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, PlayerAchievements::new);
    }

    /**
     * Adds incremental progress to a specific achievement for a player.
     *
     * This method increments the player's current progress toward the achievement by
     * the specified amount. If the achievement does not exist or is already unlocked,
     * the operation is ignored. When the progress reaches or exceeds the achievement
     * requirement, the achievement is automatically unlocked and the reward is granted.
     * Changes are immediately saved to disk.
     *
     * @param playerUUID The unique identifier of the player
     * @param achievementId The unique identifier of the achievement
     * @param amount The amount of progress to add (typically positive, but can be any value)
     */
    public void addProgress(UUID playerUUID, String achievementId, double amount) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) {
            LOGGER.warn("Unknown achievement: {}", achievementId);
            return;
        }

        PlayerAchievements playerAch = getPlayerAchievements(playerUUID);

        // Bereits freigeschaltet?
        if (playerAch.isUnlocked(achievementId)) {
            return;
        }

        // Fortschritt hinzufügen
        playerAch.addProgress(achievementId, amount);
        double currentProgress = playerAch.getProgress(achievementId);

        // Achievement freischalten?
        if (currentProgress >= achievement.getRequirement()) {
            unlockAchievement(playerUUID, achievementId);
        }

        save();
    }

    /**
     * Sets the absolute progress value for a specific achievement for a player.
     *
     * Unlike addProgress(), this method sets the progress to an exact value rather than
     * incrementing it. If the achievement does not exist or is already unlocked, the
     * operation is ignored. When the progress reaches or exceeds the achievement requirement,
     * the achievement is automatically unlocked and the reward is granted. Changes are
     * immediately saved to disk.
     *
     * @param playerUUID The unique identifier of the player
     * @param achievementId The unique identifier of the achievement
     * @param value The absolute progress value to set
     */
    public void setProgress(UUID playerUUID, String achievementId, double value) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) {
            LOGGER.warn("Unknown achievement: {}", achievementId);
            return;
        }

        PlayerAchievements playerAch = getPlayerAchievements(playerUUID);

        // Bereits freigeschaltet?
        if (playerAch.isUnlocked(achievementId)) {
            return;
        }

        // Fortschritt setzen
        playerAch.setProgress(achievementId, value);

        // Achievement freischalten?
        if (value >= achievement.getRequirement()) {
            unlockAchievement(playerUUID, achievementId);
        }

        save();
    }

    /**
     * Unlocks a specific achievement for a player and grants the associated reward.
     *
     * This method immediately unlocks the achievement regardless of current progress.
     * The monetary reward based on the achievement tier is deposited into the player's
     * economy account. If the achievement is already unlocked or does not exist, the
     * operation fails and returns false. Changes are immediately saved to disk.
     *
     * @param playerUUID The unique identifier of the player
     * @param achievementId The unique identifier of the achievement to unlock
     * @return true if the achievement was successfully unlocked, false if already unlocked or achievement doesn't exist
     */
    public boolean unlockAchievement(UUID playerUUID, String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) {
            LOGGER.warn("Unknown achievement: {}", achievementId);
            return false;
        }

        PlayerAchievements playerAch = getPlayerAchievements(playerUUID);

        // Bereits freigeschaltet?
        if (playerAch.isUnlocked(achievementId)) {
            return false;
        }

        // Achievement freischalten
        double reward = achievement.getTier().getRewardMoney();
        if (playerAch.unlock(achievementId, reward)) {
            // Belohnung auszahlen
            EconomyManager.deposit(playerUUID, reward, TransactionType.OTHER,
                "Achievement: " + achievement.getName());

            LOGGER.info("Player {} unlocked achievement: {}", playerUUID, achievementId);
            save();
            return true;
        }

        return false;
    }

    /**
     * Retrieves an achievement definition by its unique identifier.
     *
     * This method looks up the achievement configuration including its name, description,
     * category, tier, and requirements. The achievement ID is case-sensitive.
     *
     * @param achievementId The unique identifier of the achievement
     * @return The Achievement instance, or null if no achievement exists with that ID
     */
    @Nullable
    public Achievement getAchievement(String achievementId) {
        return achievements.get(achievementId);
    }

    /**
     * Retrieves all registered achievements in the system.
     *
     * Returns a defensive copy of all achievement definitions to prevent external
     * modification. This includes achievements from all categories and tiers. The
     * collection maintains insertion order (LinkedHashMap ordering).
     *
     * @return A new Collection containing all Achievement instances registered in the system
     */
    public Collection<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }

    /**
     * Retrieves all achievements belonging to a specific category.
     *
     * This method filters the achievement collection to return only achievements that
     * match the specified category (e.g., ECONOMY, CRIME, PRODUCTION, SOCIAL). The
     * returned list is a new collection that can be safely modified.
     *
     * @param category The achievement category to filter by
     * @return A List of Achievement instances in the specified category (empty list if no achievements in that category)
     */
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        return achievements.values().stream()
            .filter(a -> a.getCategory() == category)
            .toList();
    }

    /**
     * Generates a formatted statistics summary for a player's achievement progress.
     *
     * Creates a human-readable string containing the number of unlocked achievements,
     * total achievements, completion percentage, and total monetary rewards earned
     * from achievements. This is useful for displaying player progress in commands
     * or user interfaces.
     *
     * @param playerUUID The unique identifier of the player
     * @return A formatted string with achievement statistics (e.g., "Achievements: 5/42 (11.9%) - 1500.00€ verdient")
     */
    public String getStatistics(UUID playerUUID) {
        PlayerAchievements playerAch = getPlayerAchievements(playerUUID);
        int total = achievements.size();
        int unlocked = playerAch.getUnlockedCount();
        double percentage = (double) unlocked / total * 100.0;

        return String.format("Achievements: %d/%d (%.1f%%) - %.2f€ verdient",
            unlocked, total, percentage, playerAch.getTotalPointsEarned());
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, PlayerAchievements>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, PlayerAchievements> data) {
        playerData.clear();
        playerData.putAll(data);
    }

    @Override
    protected Map<UUID, PlayerAchievements> getCurrentData() {
        return new HashMap<>(playerData);
    }

    @Override
    protected String getComponentName() {
        return "AchievementManager";
    }

    @Override
    protected String getHealthDetails() {
        return playerData.size() + " Spieler, " + achievements.size() + " Achievements";
    }

    @Override
    protected void onCriticalLoadFailure() {
        playerData.clear();
    }
}
