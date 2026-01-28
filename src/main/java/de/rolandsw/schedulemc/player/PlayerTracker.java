package de.rolandsw.schedulemc.player;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks all players who have ever joined the server
 * Used by Contacts App to display all available contacts
 */
public class PlayerTracker {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, PlayerContact> playerContacts = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_player_contacts.json");
    private static final Gson gson = GsonHelper.get();

    private static final PlayerContactPersistenceManager persistence =
        new PlayerContactPersistenceManager(file, gson);

    /**
     * Loads all player contacts from JSON file
     */
    public static void load() {
        persistence.load();
    }

    /**
     * Saves all player contacts to JSON file
     */
    public static void save() {
        persistence.save();
    }

    /**
     * Saves only if changes are present
     */
    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    private static void markDirty() {
        persistence.markDirty();
    }

    /**
     * Registers a player when they join the server
     */
    public static void registerPlayer(UUID uuid, String name) {
        PlayerContact contact = playerContacts.get(uuid);
        if (contact == null) {
            contact = new PlayerContact(uuid, name);
            playerContacts.put(uuid, contact);
            markDirty();
            LOGGER.info("Registered new player contact: {} ({})", name, uuid);
        } else {
            // Update name if changed
            if (!contact.getName().equals(name)) {
                contact.setName(name);
                markDirty();
                LOGGER.info("Updated player name: {} -> {} ({})", contact.getName(), name, uuid);
            }
        }

        // Update last seen time
        contact.setLastSeen(System.currentTimeMillis());
        markDirty();
    }

    /**
     * Gets all registered player contacts, sorted alphabetically
     */
    public static List<PlayerContact> getAllContacts() {
        return playerContacts.values().stream()
            .sorted(Comparator.comparing(PlayerContact::getName))
            .collect(Collectors.toList());
    }

    /**
     * Gets a specific player contact
     */
    public static PlayerContact getContact(UUID uuid) {
        return playerContacts.get(uuid);
    }

    /**
     * Gets all service contacts (Pannenhilfe, Taxi, etc.)
     */
    public static List<ServiceContact> getServiceContacts() {
        List<ServiceContact> services = new ArrayList<>();
        services.add(new ServiceContact("towing_service", "Pannenhilfe ADAC", ServiceContact.ServiceType.TOWING));
        // Future: Add more services like Taxi, Emergency, etc.
        return services;
    }

    /**
     * Simple player contact data class
     */
    public static class PlayerContact {
        private final UUID uuid;
        private String name;
        private long lastSeen;

        public PlayerContact(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
            this.lastSeen = System.currentTimeMillis();
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getLastSeen() {
            return lastSeen;
        }

        public void setLastSeen(long lastSeen) {
            this.lastSeen = lastSeen;
        }
    }

    /**
     * Data class for JSON serialization
     */
    private static class PlayerContactData {
        String name;
        long lastSeen;
    }

    /**
     * Persistence manager for player contacts
     */
    private static class PlayerContactPersistenceManager extends AbstractPersistenceManager<Map<String, PlayerContactData>> {

        public PlayerContactPersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, PlayerContactData>>(){}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, PlayerContactData> data) {
            playerContacts.clear();

            int invalidCount = 0;
            int correctedCount = 0;

            // NULL CHECK
            if (data == null) {
                LOGGER.warn("Null data loaded for player contacts");
                invalidCount++;
                return;
            }

            // Check collection size
            if (data.size() > 10000) {
                LOGGER.warn("Player contacts map size ({}) exceeds limit, potential corruption",
                    data.size());
                correctedCount++;
            }

            data.forEach((uuidStr, contactData) -> {
                try {
                    // VALIDATE UUID STRING
                    if (uuidStr == null || uuidStr.isEmpty()) {
                        LOGGER.warn("Null/empty UUID string in player contacts, skipping");
                        return;
                    }

                    // NULL CHECK
                    if (contactData == null) {
                        LOGGER.warn("Null contact data for UUID {}, skipping", uuidStr);
                        return;
                    }

                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid UUID in player contacts: {}", uuidStr, e);
                        return;
                    }

                    // VALIDATE NAME
                    if (contactData.name == null || contactData.name.isEmpty()) {
                        LOGGER.warn("Player contact {} has null/empty name, setting default", uuid);
                        contactData.name = "Unknown Player";
                    } else if (contactData.name.length() > 100) {
                        LOGGER.warn("Player contact {} name too long ({} chars), truncating",
                            uuid, contactData.name.length());
                        contactData.name = contactData.name.substring(0, 100);
                    }

                    // VALIDATE LAST SEEN (>= 0)
                    if (contactData.lastSeen < 0) {
                        LOGGER.warn("Player contact {} has negative lastSeen {}, resetting to 0",
                            uuid, contactData.lastSeen);
                        contactData.lastSeen = 0;
                    }

                    PlayerContact contact = new PlayerContact(uuid, contactData.name);
                    contact.setLastSeen(contactData.lastSeen);
                    playerContacts.put(uuid, contact);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in player contacts: {}", uuidStr, e);
                }
            });

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
        protected Map<String, PlayerContactData> getCurrentData() {
            Map<String, PlayerContactData> saveMap = new HashMap<>();

            playerContacts.forEach((uuid, contact) -> {
                PlayerContactData data = new PlayerContactData();
                data.name = contact.getName();
                data.lastSeen = contact.getLastSeen();
                saveMap.put(uuid.toString(), data);
            });

            return saveMap;
        }

        @Override
        protected String getComponentName() {
            return "Player Contact Tracker";
        }

        @Override
        protected String getHealthDetails() {
            return String.format("%d registered contacts", playerContacts.size());
        }

        @Override
        protected void onCriticalLoadFailure() {
            playerContacts.clear();
        }
    }
}
