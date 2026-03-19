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

    // Per-player towing service contacts: playerUUID -> set of serviceIds
    private static final java.util.Map<java.util.UUID, java.util.Set<String>> playerServiceContacts =
        new java.util.concurrent.ConcurrentHashMap<>();

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
            playerServiceContacts
                .computeIfAbsent(playerId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(serviceId);
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

    /**
     * Check if a player has a specific towing service in their contacts
     */
    public static boolean playerHasService(java.util.UUID playerId, String serviceId) {
        java.util.Set<String> services = playerServiceContacts.get(playerId);
        return services != null && services.contains(serviceId);
    }

    /**
     * Get all towing service contacts for a specific player
     */
    public static List<ServiceContact> getTowingServicesForPlayer(java.util.UUID playerId) {
        java.util.Set<String> serviceIds = playerServiceContacts.getOrDefault(playerId, java.util.Collections.emptySet());
        if (serviceIds.isEmpty()) {
            // Default: all players get the global towing services
            return getAllTowingServices();
        }
        List<ServiceContact> result = new ArrayList<>();
        for (ServiceContact service : REGISTERED_SERVICES) {
            if (serviceIds.contains(service.getServiceId())) {
                result.add(service);
            }
        }
        return result;
    }
}
