package de.rolandsw.schedulemc.achievement;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
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
