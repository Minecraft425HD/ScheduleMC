package de.rolandsw.schedulemc.api;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.managers.DailyRewardManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PlotModAPI {

    // ═══════════════════════════════════════════════════════════
    // ECONOMY API
    // ═══════════════════════════════════════════════════════════
    
    public static class Economy {

        /**
         * Gibt das Guthaben eines Spielers zurück
         */
        public static double getBalance(@Nonnull ServerPlayer player) {
            return EconomyManager.getBalance(player.getUUID());
        }

        /**
         * Gibt das Guthaben eines Spielers zurück (per UUID)
         */
        public static double getBalance(@Nonnull UUID uuid) {
            return EconomyManager.getBalance(uuid);
        }

        /**
         * Gibt Geld an einen Spieler
         */
        public static void giveMoney(@Nonnull ServerPlayer player, double amount) {
            EconomyManager.deposit(player.getUUID(), amount);
        }

        /**
         * Nimmt Geld von einem Spieler
         * @return true wenn erfolgreich, false wenn nicht genug Geld
         */
        public static boolean takeMoney(@Nonnull ServerPlayer player, double amount) {
            return EconomyManager.withdraw(player.getUUID(), amount);
        }

        /**
         * Prüft ob Spieler genug Geld hat
         */
        public static boolean hasEnoughMoney(@Nonnull ServerPlayer player, double amount) {
            return getBalance(player) >= amount;
        }

        /**
         * Setzt Guthaben eines Spielers
         */
        public static void setBalance(@Nonnull ServerPlayer player, double amount) {
            EconomyManager.setBalance(player.getUUID(), amount);
        }

        /**
         * Transferiert Geld zwischen Spielern
         */
        public static boolean transferMoney(@Nonnull ServerPlayer from, @Nonnull ServerPlayer to, double amount) {
            if (takeMoney(from, amount)) {
                giveMoney(to, amount);
                return true;
            }
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT API
    // ═══════════════════════════════════════════════════════════
    
    public static class Plots {

        /**
         * Gibt den Plot an einer Position zurück
         */
        @Nullable
        public static PlotRegion getPlotAt(@Nonnull BlockPos pos) {
            for (PlotRegion plot : PlotManager.getPlots()) {
                if (plot.contains(pos)) {
                    return plot;
                }
            }
            return null;
        }

        /**
         * Gibt den Plot an Spieler-Position zurück
         */
        @Nullable
        public static PlotRegion getPlotAt(@Nonnull ServerPlayer player) {
            return getPlotAt(player.blockPosition());
        }

        /**
         * Prüft ob Spieler in einem Plot steht
         */
        public static boolean isPlayerInPlot(@Nonnull ServerPlayer player) {
            return getPlotAt(player) != null;
        }

        /**
         * Prüft ob Spieler in seinem eigenen Plot steht
         */
        public static boolean isPlayerInOwnPlot(@Nonnull ServerPlayer player) {
            PlotRegion plot = getPlotAt(player);
            return plot != null && plot.isOwnedBy(player.getUUID());
        }

        /**
         * Prüft ob Spieler Zugriff auf Plot hat (Besitzer, Trusted, Mieter)
         */
        public static boolean hasPlotAccess(@Nonnull ServerPlayer player, @Nonnull BlockPos pos) {
            PlotRegion plot = getPlotAt(pos);
            return plot == null || plot.hasAccess(player.getUUID());
        }

        /**
         * Gibt alle Plots eines Spielers zurück
         */
        @Nonnull
        public static List<PlotRegion> getPlayerPlots(@Nonnull ServerPlayer player) {
            return PlotManager.getPlots().stream()
                .filter(p -> p.isOwnedBy(player.getUUID()))
                .toList();
        }

        /**
         * Gibt Anzahl der Plots eines Spielers zurück
         */
        public static int getPlayerPlotCount(@Nonnull ServerPlayer player) {
            return getPlayerPlots(player).size();
        }

        /**
         * Prüft ob Spieler Trusted ist in aktuellem Plot
         */
        public static boolean isPlayerTrusted(@Nonnull ServerPlayer player) {
            PlotRegion plot = getPlotAt(player);
            return plot != null && plot.isTrusted(player.getUUID());
        }

        /**
         * Gibt Plot-Namen zurück
         */
        @Nullable
        public static String getPlotName(@Nonnull BlockPos pos) {
            PlotRegion plot = getPlotAt(pos);
            return plot != null ? plot.getPlotName() : null;
        }

        /**
         * Gibt Plot-Rating zurück
         */
        public static double getPlotRating(@Nonnull BlockPos pos) {
            PlotRegion plot = getPlotAt(pos);
            return plot != null ? plot.getAverageRating() : 0.0;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DAILY REWARDS API
    // ═══════════════════════════════════════════════════════════
    
    public static class Daily {

        /**
         * Prüft ob Spieler heute bereits claimed hat
         */
        public static boolean hasClaimedToday(@Nonnull ServerPlayer player) {
            return !DailyRewardManager.canClaim(player.getUUID());
        }

        /**
         * Gibt aktuellen Streak zurück
         */
        public static int getStreak(@Nonnull ServerPlayer player) {
            return DailyRewardManager.getStreak(player.getUUID());
        }

        /**
         * Gibt längsten Streak zurück
         */
        public static int getLongestStreak(@Nonnull ServerPlayer player) {
            return DailyRewardManager.getLongestStreak(player.getUUID());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY API
    // ═══════════════════════════════════════════════════════════
    
    public static class Util {
        
        /**
         * Formatiert Geld-Betrag
         */
        public static String formatMoney(double amount) {
            return String.format("%.2f€", amount);
        }
        
        /**
         * Prüft ob ScheduleMC geladen ist
         */
        public static boolean isPlotModLoaded() {
            return true; // Wenn diese Klasse existiert, ist ScheduleMC geladen
        }
        
        /**
         * Gibt ScheduleMC Version zurück
         */
        public static String getVersion() {
            return "3.0";
        }
    }
}
