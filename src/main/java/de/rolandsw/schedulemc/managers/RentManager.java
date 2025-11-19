package de.rolandsw.schedulemc.managers;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Verwaltet Plot-Vermietungen und automatische Zahlungen
 */
public class RentManager {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Vermietet einen Plot
     */
    public static boolean rentPlot(PlotRegion plot, UUID renterUUID, int days) {
        if (!ModConfigHandler.COMMON.RENT_ENABLED.get()) {
            return false;
        }
        
        if (plot.isRented()) {
            return false; // Bereits vermietet
        }
        
        if (!plot.isForRent()) {
            return false; // Nicht zur Miete verfügbar
        }
        
        // Preis berechnen
        double totalPrice = plot.getRentPricePerDay() * days;
        
        // Zahlung prüfen
        if (EconomyManager.getBalance(renterUUID) < totalPrice) {
            return false; // Nicht genug Geld
        }
        
        // Zahlung durchführen
        EconomyManager.withdraw(renterUUID, totalPrice);
        
        // An Besitzer auszahlen
        UUID ownerUUID = plot.getOwnerUUIDAsUUID();
        if (ownerUUID != null) {
            EconomyManager.deposit(ownerUUID, totalPrice);
        }
        
        // Miete setzen
        long rentDuration = (long) days * 24 * 60 * 60 * 1000; // Tage in Millisekunden
        plot.setRenterUUID(renterUUID.toString());
        plot.setRentEndTime(System.currentTimeMillis() + rentDuration);
        
        PlotManager.savePlots();

        LOGGER.info("Plot vermietet: {} an {} für {} Tage", plot.getPlotId(), renterUUID, days);

        return true;
    }
    
    /**
     * Verlängert eine bestehende Miete
     */
    public static boolean extendRent(PlotRegion plot, UUID renterUUID, int additionalDays) {
        if (!plot.isRented()) {
            return false; // Nicht vermietet
        }
        
        if (!plot.getRenterUUID().equals(renterUUID.toString())) {
            return false; // Nicht der Mieter
        }
        
        // Preis berechnen
        double totalPrice = plot.getRentPricePerDay() * additionalDays;
        
        // Zahlung prüfen
        if (EconomyManager.getBalance(renterUUID) < totalPrice) {
            return false;
        }
        
        // Zahlung durchführen
        EconomyManager.withdraw(renterUUID, totalPrice);
        
        // An Besitzer auszahlen
        UUID ownerUUID = plot.getOwnerUUIDAsUUID();
        if (ownerUUID != null) {
            EconomyManager.deposit(ownerUUID, totalPrice);
        }
        
        // Miete verlängern
        long extension = (long) additionalDays * 24 * 60 * 60 * 1000;
        plot.setRentEndTime(plot.getRentEndTime() + extension);
        
        PlotManager.savePlots();

        LOGGER.info("Miete verlängert: {} um {} Tage", plot.getPlotId(), additionalDays);

        return true;
    }
    
    /**
     * Beendet eine Miete vorzeitig (nur Besitzer oder Admin)
     */
    public static void cancelRent(PlotRegion plot) {
        if (!plot.isRented()) return;

        LOGGER.info("Miete beendet: {}", plot.getPlotId());

        plot.endRent();
        PlotManager.savePlots();
    }
    
    /**
     * Prüft alle Plots auf abgelaufene Mieten
     * Sollte regelmäßig aufgerufen werden (z.B. alle 5 Minuten)
     */
    public static void checkExpiredRents() {
        if (!ModConfigHandler.COMMON.RENT_ENABLED.get()) return;
        if (!ModConfigHandler.COMMON.AUTO_EVICT_EXPIRED.get()) return;
        
        int expired = 0;
        
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.isRentExpired()) {
                LOGGER.info("Miete abgelaufen: {} (Mieter: {})", plot.getPlotId(), plot.getRenterUUID());
                plot.endRent();
                expired++;
            }
        }

        if (expired > 0) {
            PlotManager.savePlots();
            LOGGER.info("Abgelaufene Mieten bereinigt: {}", expired);
        }
    }
    
    /**
     * Gibt alle vermieteten Plots zurück
     */
    public static List<PlotRegion> getRentedPlots() {
        List<PlotRegion> rented = new ArrayList<>();
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.isRented()) {
                rented.add(plot);
            }
        }
        return rented;
    }
    
    /**
     * Gibt alle zur Miete verfügbaren Plots zurück
     */
    public static List<PlotRegion> getAvailableRentPlots() {
        List<PlotRegion> available = new ArrayList<>();
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.isForRent() && !plot.isRented()) {
                available.add(plot);
            }
        }
        return available;
    }
    
    /**
     * Gibt alle Plots zurück die ein Spieler gemietet hat
     */
    public static List<PlotRegion> getPlayerRentedPlots(UUID playerUUID) {
        List<PlotRegion> plots = new ArrayList<>();
        String uuid = playerUUID.toString();
        
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.isRented() && plot.getRenterUUID().equals(uuid)) {
                plots.add(plot);
            }
        }
        return plots;
    }
    
    /**
     * Benachrichtigt Mieter über ablaufende Mieten (z.B. 24h vorher)
     */
    public static void notifyExpiringRents(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        List<PlotRegion> rentedPlots = getPlayerRentedPlots(playerUUID);
        
        for (PlotRegion plot : rentedPlots) {
            long hoursLeft = plot.getRentHoursLeft();
            
            if (hoursLeft <= 24 && hoursLeft > 0) {
                player.sendSystemMessage(Component.literal(
                    "§e⚠ Warnung: Die Miete für Plot §6" + plot.getPlotName() + 
                    " §eläuft in §c" + hoursLeft + " Stunden §eab!"
                ));
            }
        }
    }
    
    /**
     * Berechnet Gesamteinnahmen aus Vermietungen für einen Besitzer
     */
    public static double calculateRentIncome(UUID ownerUUID) {
        double income = 0;
        String uuid = ownerUUID.toString();
        
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.getOwnerUUID().equals(uuid) && plot.isRented()) {
                long daysRented = (plot.getRentEndTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                income += plot.getRentPricePerDay() * daysRented;
            }
        }
        
        return income;
    }
}
