package de.rolandsw.schedulemc.achievement;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
            Component.translatable("achievement.first_euro.name").getString(),
            Component.translatable("achievement.first_euro.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "RICH",
            Component.translatable("achievement.rich.name").getString(),
            Component.translatable("achievement.rich.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            10000.0,
            false
        ));

        register(new Achievement(
            "WEALTHY",
            Component.translatable("achievement.wealthy.name").getString(),
            Component.translatable("achievement.wealthy.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.SILVER,
            100000.0,
            false
        ));

        register(new Achievement(
            "MILLIONAIRE",
            Component.translatable("achievement.millionaire.name").getString(),
            Component.translatable("achievement.millionaire.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.GOLD,
            1000000.0,
            false
        ));

        register(new Achievement(
            "LOAN_MASTER",
            Component.translatable("achievement.loan_master.name").getString(),
            Component.translatable("achievement.loan_master.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.SILVER,
            10.0,
            false
        ));

        register(new Achievement(
            "SAVINGS_KING",
            Component.translatable("achievement.savings_king.name").getString(),
            Component.translatable("achievement.savings_king.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.GOLD,
            100000.0,
            false
        ));

        register(new Achievement(
            "BIG_SPENDER",
            Component.translatable("achievement.big_spender.name").getString(),
            Component.translatable("achievement.big_spender.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.DIAMOND,
            1000000.0,
            false
        ));

        // ========== STOCK TRADING ACHIEVEMENTS ==========
        register(new Achievement(
            "FIRST_TRADE",
            Component.translatable("achievement.first_trade.name").getString(),
            Component.translatable("achievement.first_trade.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "FIRST_PROFIT",
            Component.translatable("achievement.first_profit.name").getString(),
            Component.translatable("achievement.first_profit.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "FIRST_LOSS",
            Component.translatable("achievement.first_loss.name").getString(),
            Component.translatable("achievement.first_loss.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "PROFIT_100",
            Component.translatable("achievement.profit_100.name").getString(),
            Component.translatable("achievement.profit_100.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.BRONZE,
            100.0,
            false
        ));

        register(new Achievement(
            "PROFIT_1K",
            Component.translatable("achievement.profit_1k.name").getString(),
            Component.translatable("achievement.profit_1k.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.SILVER,
            1000.0,
            false
        ));

        register(new Achievement(
            "PROFIT_10K",
            Component.translatable("achievement.profit_10k.name").getString(),
            Component.translatable("achievement.profit_10k.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.GOLD,
            10000.0,
            false
        ));

        register(new Achievement(
            "PROFIT_MASTER",
            Component.translatable("achievement.profit_master.name").getString(),
            Component.translatable("achievement.profit_master.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.DIAMOND,
            100000.0,
            false
        ));

        register(new Achievement(
            "ACTIVE_TRADER",
            Component.translatable("achievement.active_trader.name").getString(),
            Component.translatable("achievement.active_trader.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.SILVER,
            50.0,
            false
        ));

        register(new Achievement(
            "DAY_TRADER",
            Component.translatable("achievement.day_trader.name").getString(),
            Component.translatable("achievement.day_trader.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.GOLD,
            500.0,
            false
        ));

        register(new Achievement(
            "WOLF_OF_MINECRAFT",
            Component.translatable("achievement.wolf_of_minecraft.name").getString(),
            Component.translatable("achievement.wolf_of_minecraft.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.PLATINUM,
            5000.0,
            false
        ));

        register(new Achievement(
            "TRADING_GENIUS",
            Component.translatable("achievement.trading_genius.name").getString(),
            Component.translatable("achievement.trading_genius.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.DIAMOND,
            1.0,
            false
        ));

        register(new Achievement(
            "BIG_LOSER",
            Component.translatable("achievement.big_loser.name").getString(),
            Component.translatable("achievement.big_loser.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.SILVER,
            5000.0,
            true // Hidden achievement für Verluste
        ));

        register(new Achievement(
            "PATIENT_INVESTOR",
            Component.translatable("achievement.patient_investor.name").getString(),
            Component.translatable("achievement.patient_investor.desc").getString(),
            AchievementCategory.ECONOMY,
            AchievementTier.GOLD,
            100.0,
            false
        ));

        // ========== CRIME ACHIEVEMENTS ==========
        register(new Achievement(
            "FIRST_CRIME",
            Component.translatable("achievement.first_crime.name").getString(),
            Component.translatable("achievement.first_crime.desc").getString(),
            AchievementCategory.CRIME,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "WANTED",
            Component.translatable("achievement.wanted.name").getString(),
            Component.translatable("achievement.wanted.desc").getString(),
            AchievementCategory.CRIME,
            AchievementTier.SILVER,
            3.0,
            false
        ));

        register(new Achievement(
            "MOST_WANTED",
            Component.translatable("achievement.most_wanted.name").getString(),
            Component.translatable("achievement.most_wanted.desc").getString(),
            AchievementCategory.CRIME,
            AchievementTier.GOLD,
            5.0,
            false
        ));

        register(new Achievement(
            "ESCAPE_ARTIST",
            Component.translatable("achievement.escape_artist.name").getString(),
            Component.translatable("achievement.escape_artist.desc").getString(),
            AchievementCategory.CRIME,
            AchievementTier.SILVER,
            10.0,
            false
        ));

        register(new Achievement(
            "PRISON_VETERAN",
            Component.translatable("achievement.prison_veteran.name").getString(),
            Component.translatable("achievement.prison_veteran.desc").getString(),
            AchievementCategory.CRIME,
            AchievementTier.GOLD,
            100.0,
            false
        ));

        register(new Achievement(
            "CLEAN_RECORD",
            Component.translatable("achievement.clean_record.name").getString(),
            Component.translatable("achievement.clean_record.desc").getString(),
            AchievementCategory.CRIME,
            AchievementTier.DIAMOND,
            30.0,
            false
        ));

        // ========== PRODUCTION ACHIEVEMENTS ==========
        register(new Achievement(
            "HOBBYIST",
            Component.translatable("achievement.hobbyist.name").getString(),
            Component.translatable("achievement.hobbyist.desc").getString(),
            AchievementCategory.PRODUCTION,
            AchievementTier.BRONZE,
            100.0,
            false
        ));

        register(new Achievement(
            "FARMER",
            Component.translatable("achievement.farmer.name").getString(),
            Component.translatable("achievement.farmer.desc").getString(),
            AchievementCategory.PRODUCTION,
            AchievementTier.SILVER,
            100.0,
            false
        ));

        register(new Achievement(
            "PRODUCER",
            Component.translatable("achievement.producer.name").getString(),
            Component.translatable("achievement.producer.desc").getString(),
            AchievementCategory.PRODUCTION,
            AchievementTier.GOLD,
            1000.0,
            false
        ));

        register(new Achievement(
            "DRUG_LORD",
            Component.translatable("achievement.drug_lord.name").getString(),
            Component.translatable("achievement.drug_lord.desc").getString(),
            AchievementCategory.PRODUCTION,
            AchievementTier.DIAMOND,
            10000.0,
            false
        ));

        register(new Achievement(
            "EMPIRE_BUILDER",
            Component.translatable("achievement.empire_builder.name").getString(),
            Component.translatable("achievement.empire_builder.desc").getString(),
            AchievementCategory.PRODUCTION,
            AchievementTier.PLATINUM,
            10.0,
            false
        ));

        // ========== SOCIAL ACHIEVEMENTS ==========
        register(new Achievement(
            "FIRST_PLOT",
            Component.translatable("achievement.first_plot.name").getString(),
            Component.translatable("achievement.first_plot.desc").getString(),
            AchievementCategory.SOCIAL,
            AchievementTier.BRONZE,
            1.0,
            false
        ));

        register(new Achievement(
            "PROPERTY_MOGUL",
            Component.translatable("achievement.property_mogul.name").getString(),
            Component.translatable("achievement.property_mogul.desc").getString(),
            AchievementCategory.SOCIAL,
            AchievementTier.GOLD,
            5.0,
            false
        ));

        register(new Achievement(
            "LANDLORD",
            Component.translatable("achievement.landlord.name").getString(),
            Component.translatable("achievement.landlord.desc").getString(),
            AchievementCategory.SOCIAL,
            AchievementTier.DIAMOND,
            100000.0,
            false
        ));

        register(new Achievement(
            "POPULAR",
            Component.translatable("achievement.popular.name").getString(),
            Component.translatable("achievement.popular.desc").getString(),
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
     * Gibt PlayerAchievements für Spieler zurück
     */
    public PlayerAchievements getPlayerAchievements(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, PlayerAchievements::new);
    }

    /**
     * Addiert Fortschritt zu Achievement
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
     * Setzt Fortschritt für Achievement (absolut)
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
     * Schaltet Achievement frei
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
     * Gibt Achievement zurück
     */
    @Nullable
    public Achievement getAchievement(String achievementId) {
        return achievements.get(achievementId);
    }

    /**
     * Gibt alle Achievements zurück
     */
    public Collection<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }

    /**
     * Gibt Achievements einer Kategorie zurück
     */
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        return achievements.values().stream()
            .filter(a -> a.getCategory() == category)
            .toList();
    }

    /**
     * Gibt Statistiken zurück
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
