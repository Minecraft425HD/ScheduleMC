package de.rolandsw.schedulemc.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * NPCNameRegistry - Verwaltet eindeutige NPC-Namen
 *
 * Features:
 * - Globale Name-Registrierung (verhindert Duplikate)
 * - Name → Entity-ID Mapping
 * - Persistenz über Server-Neustarts
 * - Thread-safe Operations
 */
public class NPCNameRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Integer> nameToEntityId = new ConcurrentHashMap<>();
    private static final File REGISTRY_FILE = new File("config/npc_names.json");
    private static final Gson GSON = GsonHelper.get();

    // SICHERHEIT: volatile für Memory Visibility zwischen Threads
    private static volatile boolean dirty = false;

    // ═══════════════════════════════════════════════════════════
    // REGISTRIERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen NPC-Namen
     *
     * @param name Der NPC-Name
     * @param entityId Die Entity-ID des NPCs
     * @return true wenn erfolgreich registriert, false wenn Name bereits existiert
     */
    public static boolean registerName(String name, int entityId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String normalizedName = name.trim();

        // Prüfe ob Name bereits existiert
        if (nameToEntityId.containsKey(normalizedName)) {
            LOGGER.warn("NPC-Name bereits vergeben: {}", normalizedName);
            return false;
        }

        nameToEntityId.put(normalizedName, entityId);
        dirty = true;

        LOGGER.info("NPC registriert: {} (Entity-ID: {})", normalizedName, entityId);

        return true;
    }

    /**
     * Entfernt einen NPC-Namen aus der Registrierung
     *
     * @param name Der zu entfernende Name
     */
    public static void unregisterName(String name) {
        if (name == null) return;

        String normalizedName = name.trim();
        if (nameToEntityId.remove(normalizedName) != null) {
            dirty = true;
            LOGGER.info("NPC-Name entfernt: {}", normalizedName);
        }
    }

    /**
     * Entfernt einen NPC anhand seiner Entity-ID
     *
     * @param entityId Die Entity-ID
     */
    public static void unregisterByEntityId(int entityId) {
        String nameToRemove = null;

        for (Map.Entry<String, Integer> entry : nameToEntityId.entrySet()) {
            if (entry.getValue() == entityId) {
                nameToRemove = entry.getKey();
                break;
            }
        }

        if (nameToRemove != null) {
            unregisterName(nameToRemove);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SUCHE & VALIDIERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob ein Name bereits registriert ist
     *
     * @param name Der zu prüfende Name
     * @return true wenn Name bereits existiert
     */
    public static boolean isNameTaken(String name) {
        if (name == null) return false;
        return nameToEntityId.containsKey(name.trim());
    }

    /**
     * Gibt die Entity-ID für einen Namen zurück
     *
     * @param name Der NPC-Name
     * @return Die Entity-ID oder null wenn nicht gefunden
     */
    public static Integer getEntityId(String name) {
        if (name == null) return null;
        return nameToEntityId.get(name.trim());
    }

    /**
     * Sucht einen NPC nach Namen in einem ServerLevel
     *
     * @param name Der NPC-Name
     * @param level Das ServerLevel
     * @return Der gefundene CustomNPCEntity oder null
     */
    public static CustomNPCEntity findNPCByName(String name, ServerLevel level) {
        Integer entityId = getEntityId(name);
        if (entityId == null) return null;

        Entity entity = level.getEntity(entityId);
        if (entity instanceof CustomNPCEntity) {
            return (CustomNPCEntity) entity;
        }

        // Entity existiert nicht mehr → Aufräumen
        LOGGER.warn("NPC nicht gefunden für registrierten Namen: {} (ID: {})", name, entityId);
        unregisterName(name);

        return null;
    }

    /**
     * Gibt alle registrierten NPC-Namen zurück
     *
     * @return Set aller Namen
     */
    public static Set<String> getAllNames() {
        return new HashSet<>(nameToEntityId.keySet());
    }

    /**
     * Gibt alle registrierten NPC-Namen als sortierte Liste zurück
     *
     * @return Sortierte Liste aller Namen
     */
    public static List<String> getAllNamesSorted() {
        List<String> names = new ArrayList<>(nameToEntityId.keySet());
        Collections.sort(names);
        return names;
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    /**
     * Lädt die NPC-Namen aus der Datei
     */
    public static void loadRegistry() {
        try {
            if (!REGISTRY_FILE.exists()) {
                LOGGER.info("NPC-Namen-Datei existiert nicht, erstelle neue");
                REGISTRY_FILE.getParentFile().mkdirs();
                saveRegistry();
                return;
            }

            Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();

            try (FileReader reader = new FileReader(REGISTRY_FILE)) {
                Map<String, Integer> loaded = GSON.fromJson(reader, mapType);

                if (loaded != null) {
                    nameToEntityId.clear();
                    nameToEntityId.putAll(loaded);
                    LOGGER.info("NPC-Namen geladen: {} NPCs", nameToEntityId.size());
                } else {
                    LOGGER.warn("Leere NPC-Namen-Datei");
                }
            }

            dirty = false;

        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der NPC-Namen", e);
        }
    }

    /**
     * Speichert die NPC-Namen in eine Datei
     */
    public static void saveRegistry() {
        try {
            REGISTRY_FILE.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(REGISTRY_FILE)) {
                GSON.toJson(nameToEntityId, writer);
            }

            dirty = false;
            LOGGER.info("NPC-Namen gespeichert: {} NPCs", nameToEntityId.size());

        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der NPC-Namen", e);
        }
    }

    /**
     * Speichert nur wenn Änderungen vorhanden (dirty)
     */
    public static void saveIfNeeded() {
        if (dirty) {
            saveRegistry();
        }
    }

    /**
     * Bereinigt ungültige Einträge (NPCs die nicht mehr existieren)
     *
     * @param level Das ServerLevel zum Prüfen
     */
    public static void cleanup(ServerLevel level) {
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : nameToEntityId.entrySet()) {
            Entity entity = level.getEntity(entry.getValue());
            if (!(entity instanceof CustomNPCEntity)) {
                toRemove.add(entry.getKey());
            }
        }

        if (!toRemove.isEmpty()) {
            for (String name : toRemove) {
                unregisterName(name);
            }
            LOGGER.info("NPC-Namen bereinigt: {} ungültige Einträge entfernt", toRemove.size());
        }
    }

    /**
     * Löscht alle Einträge (für Tests/Debug)
     */
    public static void clear() {
        nameToEntityId.clear();
        dirty = true;
        LOGGER.warn("NPC-Namen-Registry geleert!");
    }

    /**
     * Gibt die Anzahl registrierter NPCs zurück
     */
    public static int size() {
        return nameToEntityId.size();
    }
}
