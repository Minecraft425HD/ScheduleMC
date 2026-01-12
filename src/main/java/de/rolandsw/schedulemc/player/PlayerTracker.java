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

            data.forEach((uuidStr, contactData) -> {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    PlayerContact contact = new PlayerContact(uuid, contactData.name);
                    contact.setLastSeen(contactData.lastSeen);
                    playerContacts.put(uuid, contact);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid UUID in player contacts: {}", uuidStr, e);
                }
            });
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
