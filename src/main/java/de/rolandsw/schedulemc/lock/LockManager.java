package de.rolandsw.schedulemc.lock;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton-Manager fuer alle Tuer-Schloesser.
 * Persistiert als JSON-Datei (schedulemc_locks.json).
 */
public class LockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("ScheduleMC-LockManager");
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile LockManager instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // BlockPos-Key: "dim:x:y:z" (immer lower half der Tuer)
    private final ConcurrentHashMap<String, LockData> locks = new ConcurrentHashMap<>();
    private final Path saveFile;
    private volatile boolean dirty = false;

    private LockManager(Path configDir) {
        this.saveFile = configDir.resolve("schedulemc_locks.json");
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static LockManager getInstance(Path configDir) {
        LockManager localRef = instance;
        if (localRef == null) {
            synchronized (LockManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new LockManager(configDir);
                }
            }
        }
        return localRef;
    }

    public static LockManager getInstance() { return instance; }

    public static void resetInstance() {
        synchronized (LockManager.class) {
            if (instance != null) {
                instance.save();
            }
            instance = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // POSITIONS-KEY
    // ═══════════════════════════════════════════════════════════

    public static String posKey(String dimension, int x, int y, int z) {
        return dimension + ":" + x + ":" + y + ":" + z;
    }

    // ═══════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════

    public LockData getLock(String posKey) {
        return locks.get(posKey);
    }

    public LockData getLock(String dimension, int x, int y, int z) {
        return getLock(posKey(dimension, x, y, z));
    }

    public boolean isLocked(String posKey) {
        return locks.containsKey(posKey);
    }

    public LockData placeLock(LockType type, UUID ownerUUID, String ownerName,
                               int x, int y, int z, String dimension) {
        String key = posKey(dimension, x, y, z);
        String lockId = UUID.randomUUID().toString().substring(0, 8);
        LockData data = new LockData(lockId, type, ownerUUID, ownerName, x, y, z, dimension);

        // Automatisch Code generieren fuer Zahlenschloesser
        if (type.hasCode()) {
            data.rotateCode();
        }

        locks.put(key, data);
        dirty = true;
        LOGGER.info("Lock placed: {} ({}) at {},{},{} by {}",
                lockId, type.getDisplayName(), x, y, z, ownerName);
        return data;
    }

    public boolean removeLock(String posKey, UUID playerUUID) {
        LockData data = locks.get(posKey);
        if (data == null) return false;
        if (!data.getOwnerUUID().equals(playerUUID)) return false;
        locks.remove(posKey);
        dirty = true;
        LOGGER.info("Lock removed at {}", posKey);
        return true;
    }

    /** Fuer Admin: Schloss entfernen ohne Besitzer-Check. */
    public boolean removeLockForce(String posKey) {
        if (locks.remove(posKey) != null) {
            dirty = true;
            return true;
        }
        return false;
    }

    /** Alle Schloesser eines Spielers. */
    public List<LockData> getPlayerLocks(UUID playerUUID) {
        List<LockData> result = new ArrayList<>();
        for (LockData data : locks.values()) {
            if (data.getOwnerUUID().equals(playerUUID)) result.add(data);
        }
        return result;
    }

    /** Alle Schloesser (unabhaengig vom Besitzer). */
    public Collection<LockData> getAllLocks() {
        return Collections.unmodifiableCollection(locks.values());
    }

    /** Schloss anhand seiner Lock-ID finden. */
    public LockData findByLockId(String lockId) {
        for (LockData data : locks.values()) {
            if (data.getLockId().equals(lockId)) return data;
        }
        return null;
    }

    /** Code-Rotation fuer alle Schloesser pruefen (aufrufen in Tick/Save). */
    public void tickCodeRotation() {
        for (LockData data : locks.values()) {
            if (data.needsCodeRotation()) {
                String newCode = data.rotateCode();
                dirty = true;
                LOGGER.debug("Code rotated for lock {} -> {}", data.getLockId(), newCode);
            }
        }
    }

    /** Prueft ob ein Schluessel (per lock_id) gueltig ist fuer ein Schloss. */
    public boolean isKeyValidForLock(String lockId, String posKey) {
        LockData data = locks.get(posKey);
        return data != null && data.getLockId().equals(lockId);
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    public void save() {
        if (!dirty) return;
        try {
            JsonArray arr = new JsonArray();
            for (var entry : locks.entrySet()) {
                arr.add(serializeLock(entry.getKey(), entry.getValue()));
            }
            // Atomic write: temp file + move verhindert Korruption bei Crash
            File tempFile = new File(saveFile.toFile().getParent(), saveFile.getFileName() + ".tmp");
            try (Writer w = new FileWriter(tempFile)) {
                GSON.toJson(arr, w);
            }
            Files.move(tempFile.toPath(), saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            dirty = false;
        } catch (IOException e) {
            LOGGER.error("Failed to save locks", e);
        }
    }

    private void load() {
        File file = saveFile.toFile();
        if (!file.exists()) return;
        try (Reader r = new FileReader(file)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            int skipped = 0;
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String posKey = obj.get("posKey").getAsString();
                LockData data = deserializeLock(obj);
                if (data != null) {
                    locks.put(posKey, data);
                } else {
                    skipped++;
                }
            }
            if (skipped > 0) {
                LOGGER.error("Skipped {} corrupted lock entries during load!", skipped);
            }
            LOGGER.info("Loaded {} locks", locks.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load locks", e);
        }
    }

    private JsonObject serializeLock(String posKey, LockData data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("posKey", posKey);
        obj.addProperty("lockId", data.getLockId());
        obj.addProperty("type", data.getType().name());
        obj.addProperty("owner", data.getOwnerUUID().toString());
        obj.addProperty("ownerName", data.getOwnerName());
        obj.addProperty("doorX", data.getDoorX());
        obj.addProperty("doorY", data.getDoorY());
        obj.addProperty("doorZ", data.getDoorZ());
        obj.addProperty("dimension", data.getDimension());
        obj.addProperty("placedTime", data.getPlacedTime());
        if (data.getCode() != null) {
            obj.addProperty("code", data.getCode());
            obj.addProperty("lastCodeRotation", data.getLastCodeRotation());
        }
        JsonArray auth = new JsonArray();
        for (UUID uuid : data.getAuthorizedPlayers()) auth.add(uuid.toString());
        obj.add("authorized", auth);
        return obj;
    }

    private LockData deserializeLock(JsonObject obj) {
        try {
            LockType type = LockType.valueOf(obj.get("type").getAsString());
            LockData data = new LockData(
                    obj.get("lockId").getAsString(), type,
                    UUID.fromString(obj.get("owner").getAsString()),
                    obj.has("ownerName") ? obj.get("ownerName").getAsString() : "?",
                    obj.get("doorX").getAsInt(), obj.get("doorY").getAsInt(), obj.get("doorZ").getAsInt(),
                    obj.get("dimension").getAsString());
            data.setPlacedTime(obj.get("placedTime").getAsLong());
            if (obj.has("code")) {
                data.setCode(obj.get("code").getAsString());
                data.setLastCodeRotation(obj.get("lastCodeRotation").getAsLong());
            }
            if (obj.has("authorized")) {
                for (JsonElement e : obj.getAsJsonArray("authorized")) {
                    data.addAuthorized(UUID.fromString(e.getAsString()));
                }
            }
            return data;
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize lock entry: {}", obj, e);
            return null;
        }
    }

    public void markDirty() { dirty = true; }
    public int getLockCount() { return locks.size(); }
}
