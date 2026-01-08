package de.rolandsw.schedulemc.achievement;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.region.PlotManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Trackt automatisch Achievement-Fortschritte
 * @Mod.EventBusSubscriber für automatische Event-Registrierung
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class AchievementTracker {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Achievement Check Configuration
    private static final long CHECK_INTERVAL_TICKS = 1200; // Alle 60 Sekunden (20 ticks/sec * 60)

    private static AchievementManager achievementManager;
    private static long lastCheckTicks = 0;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        achievementManager = AchievementManager.getInstance(server);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        LOGGER.info("AchievementTracker: Player {} joined, checking achievements (UUID: {})",
            player.getName().getString(), player.getUUID());

        if (achievementManager == null) {
            achievementManager = AchievementManager.getInstance(player.getServer());
        }

        // Checke sofort beim Join
        LOGGER.info("AchievementTracker: About to check player achievements for {}",
            player.getName().getString());
        checkPlayerAchievements(player);
        LOGGER.info("AchievementTracker: Achievement check complete for {}",
            player.getName().getString());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || achievementManager == null) {
            return;
        }

        MinecraftServer server = event.getServer();
        long currentTicks = server.overworld().getGameTime();

        // Checke alle 60 Sekunden
        if (currentTicks - lastCheckTicks >= CHECK_INTERVAL_TICKS) {
            lastCheckTicks = currentTicks;

            // Checke alle online Spieler
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                checkPlayerAchievements(player);
            }
        }
    }

    /**
     * Checkt alle Achievements für einen Spieler
     */
    private static void checkPlayerAchievements(ServerPlayer player) {
        UUID uuid = player.getUUID();

        // ========== ECONOMY ACHIEVEMENTS ==========
        double balance = EconomyManager.getBalance(uuid);
        LOGGER.info("AchievementTracker: Reading balance for {}: {} €", player.getName().getString(), balance);

        // Balance-basierte Achievements
        achievementManager.setProgress(uuid, "FIRST_EURO", Math.max(1, balance));
        achievementManager.setProgress(uuid, "RICH", balance);
        achievementManager.setProgress(uuid, "WEALTHY", balance);
        achievementManager.setProgress(uuid, "MILLIONAIRE", balance);

        // ========== CRIME ACHIEVEMENTS ==========
        int wantedLevel = CrimeManager.getWantedLevel(uuid);

        if (wantedLevel > 0) {
            achievementManager.setProgress(uuid, "FIRST_CRIME", 1.0);
        }
        if (wantedLevel >= 3) {
            achievementManager.setProgress(uuid, "WANTED", 3.0);
        }
        if (wantedLevel >= 5) {
            achievementManager.setProgress(uuid, "MOST_WANTED", 5.0);
        }

        // ========== SOCIAL ACHIEVEMENTS ==========
        int plotCount = PlotManager.getPlotsByOwner(uuid).size();

        if (plotCount > 0) {
            achievementManager.setProgress(uuid, "FIRST_PLOT", 1.0);
        }
        achievementManager.setProgress(uuid, "PROPERTY_MOGUL", plotCount);
    }

    /**
     * Manuelles Tracking für spezifische Events
     */

    /**
     * Trackt Kredit-Rückzahlung
     */
    public static void trackLoanRepayment(UUID playerUUID) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "LOAN_MASTER", 1.0);
        }
    }

    /**
     * Trackt Sparkonten-Einzahlung
     */
    public static void trackSavingsDeposit(UUID playerUUID, double amount) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "SAVINGS_KING", amount);
        }
    }

    /**
     * Trackt Geld verdient (Total)
     */
    public static void trackMoneyEarned(UUID playerUUID, double amount) {
        if (achievementManager != null) {
            if (amount > 0) {
                achievementManager.addProgress(playerUUID, "BIG_SPENDER", amount);
            }
        }
    }

    /**
     * Trackt Polizei-Flucht
     */
    public static void trackPoliceEscape(UUID playerUUID) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "ESCAPE_ARTIST", 1.0);
        }
    }

    /**
     * Trackt Gefängnis-Zeit
     */
    public static void trackPrisonTime(UUID playerUUID, int days) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "PRISON_VETERAN", days);
        }
    }

    /**
     * Trackt Crime-freie Tage
     */
    public static void trackCleanDays(UUID playerUUID, int days) {
        if (achievementManager != null) {
            achievementManager.setProgress(playerUUID, "CLEAN_RECORD", days);
        }
    }

    /**
     * Trackt Pflanzen-Anbau
     */
    public static void trackPlantPlanted(UUID playerUUID) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "HOBBYIST", 1.0);
        }
    }

    /**
     * Trackt Produktion (in kg)
     */
    public static void trackProduction(UUID playerUUID, double kg) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "FARMER", kg);
            achievementManager.addProgress(playerUUID, "PRODUCER", kg);
            achievementManager.addProgress(playerUUID, "DRUG_LORD", kg);
        }
    }

    /**
     * Trackt Produktionsstätten
     */
    public static void trackProductionSites(UUID playerUUID, int count) {
        if (achievementManager != null) {
            achievementManager.setProgress(playerUUID, "EMPIRE_BUILDER", count);
        }
    }

    /**
     * Trackt Mieteinnahmen
     */
    public static void trackRentIncome(UUID playerUUID, double amount) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "LANDLORD", amount);
        }
    }

    /**
     * Trackt positive Ratings
     */
    public static void trackPositiveRating(UUID playerUUID) {
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "POPULAR", 1.0);
        }
    }
}
