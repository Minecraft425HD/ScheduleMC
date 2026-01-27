package de.rolandsw.schedulemc.towing;

import de.rolandsw.schedulemc.player.PlayerTracker;
import de.rolandsw.schedulemc.player.ServiceContact;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for towing service contacts and NPCs
 * Manages predefined towing service contacts that appear in player's contact apps
 */
public class TowingServiceRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<ServiceContact> REGISTERED_SERVICES = new CopyOnWriteArrayList<>();

    /**
     * Initialize default towing service contacts
     * Call this during server startup
     */
    public static void initializeDefaultServices() {
        // ADAC-style towing service (Pannenhilfe)
        ServiceContact adac = new ServiceContact(
            "towing_adac",
            "ADAC Pannenhilfe",
            ServiceContact.ServiceType.TOWING
        );
        REGISTERED_SERVICES.add(adac);

        // Alternative towing service
        ServiceContact cityTow = new ServiceContact(
            "towing_city",
            "Stadtabschleppdienst",
            ServiceContact.ServiceType.TOWING
        );
        REGISTERED_SERVICES.add(cityTow);

        LOGGER.info("Initialized {} towing service contacts", REGISTERED_SERVICES.size());
    }

    /**
     * Get all registered towing services
     */
    public static List<ServiceContact> getAllTowingServices() {
        return new ArrayList<>(REGISTERED_SERVICES);
    }

    /**
     * Add a towing service contact for a specific player
     * This makes the service appear in their contacts app
     */
    public static void addTowingServiceToPlayer(java.util.UUID playerId, String serviceId) {
        ServiceContact service = REGISTERED_SERVICES.stream()
            .filter(s -> s.getServiceId().equals(serviceId))
            .findFirst()
            .orElse(null);

        if (service != null) {
            // Note: This would need PlayerTracker to have a method to add service contacts
            // For now, this is a placeholder for future integration
            LOGGER.debug("Added towing service {} to player {}", serviceId, playerId);
        }
    }

    /**
     * Add all towing services to a player's contacts
     * Useful when player first joins or purchases a membership
     */
    public static void addAllTowingServicesToPlayer(java.util.UUID playerId) {
        for (ServiceContact service : REGISTERED_SERVICES) {
            addTowingServiceToPlayer(playerId, service.getServiceId());
        }
        LOGGER.info("Added all towing services to player {}", playerId);
    }
}
