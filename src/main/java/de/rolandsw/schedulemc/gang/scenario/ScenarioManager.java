package de.rolandsw.schedulemc.gang.scenario;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet alle Mission-Szenarien (CRUD, Persistenz, Aktivierung).
 *
 * Szenarien werden als JSON gespeichert und koennen im Editor erstellt/bearbeitet werden.
 * Aktive Szenarien werden vom GangMissionManager als Auftrags-Pool genutzt.
 */
public class ScenarioManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioManager.class);
    private static volatile ScenarioManager instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, MissionScenario> scenarios = new ConcurrentHashMap<>();
    private final Path saveFile;

    private ScenarioManager(Path saveDir) {
        this.saveFile = saveDir.resolve("schedulemc_scenarios.json");
        load();
    }

    public static ScenarioManager getInstance() {
        return instance;
    }

    public static ScenarioManager getInstance(Path saveDir) {
        if (instance == null) {
            synchronized (ScenarioManager.class) {
                if (instance == null) {
                    instance = new ScenarioManager(saveDir);
                }
            }
        }
        return instance;
    }

    public static void resetInstance() {
        if (instance != null) {
            instance.save();
            instance = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════

    public MissionScenario getScenario(String id) {
        return scenarios.get(id);
    }

    public Collection<MissionScenario> getAllScenarios() {
        return Collections.unmodifiableCollection(scenarios.values());
    }

    public List<MissionScenario> getActiveScenarios() {
        List<MissionScenario> active = new ArrayList<>();
        for (MissionScenario s : scenarios.values()) {
            if (s.isActive()) active.add(s);
        }
        return active;
    }

    public void saveScenario(MissionScenario scenario) {
        scenarios.put(scenario.getId(), scenario);
        save();
        LOGGER.info("Scenario saved: '{}' ({})", scenario.getName(), scenario.getId());
    }

    public boolean deleteScenario(String id) {
        MissionScenario removed = scenarios.remove(id);
        if (removed != null) {
            save();
            LOGGER.info("Scenario deleted: '{}' ({})", removed.getName(), id);
            return true;
        }
        return false;
    }

    public void toggleActive(String id) {
        MissionScenario s = scenarios.get(id);
        if (s != null) {
            s.setActive(!s.isActive());
            save();
        }
    }

    public int getScenarioCount() {
        return scenarios.size();
    }

    public int getActiveCount() {
        return (int) scenarios.values().stream().filter(MissionScenario::isActive).count();
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALISIERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Serialisiert alle Szenarien als JSON-String (fuer Netzwerk-Pakete).
     */
    public String toJson() {
        List<SavedScenario> saved = new ArrayList<>();
        for (MissionScenario scenario : scenarios.values()) {
            saved.add(toSaved(scenario));
        }
        return GSON.toJson(saved);
    }

    /**
     * Deserialisiert ein einzelnes Szenario aus JSON.
     */
    public static MissionScenario fromJson(String json) {
        SavedScenario saved = GSON.fromJson(json, SavedScenario.class);
        return fromSaved(saved);
    }

    /**
     * Deserialisiert eine Liste von Szenarien aus JSON.
     */
    public static List<MissionScenario> listFromJson(String json) {
        Type listType = new TypeToken<List<SavedScenario>>() {}.getType();
        List<SavedScenario> savedList = GSON.fromJson(json, listType);
        if (savedList == null) return new ArrayList<>();
        List<MissionScenario> result = new ArrayList<>();
        for (SavedScenario saved : savedList) {
            try {
                result.add(fromSaved(saved));
            } catch (Exception e) {
                LOGGER.warn("Failed to load scenario: {}", e.getMessage());
            }
        }
        return result;
    }

    /**
     * Serialisiert ein einzelnes Szenario als JSON.
     */
    public static String scenarioToJson(MissionScenario scenario) {
        return GSON.toJson(toSaved(scenario));
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    public void save() {
        try {
            List<SavedScenario> saveData = new ArrayList<>();
            for (MissionScenario scenario : scenarios.values()) {
                saveData.add(toSaved(scenario));
            }
            Files.writeString(saveFile, GSON.toJson(saveData));
        } catch (Exception e) {
            LOGGER.error("Failed to save scenarios", e);
        }
    }

    private void load() {
        if (!Files.exists(saveFile)) return;
        try {
            String json = Files.readString(saveFile);
            Type listType = new TypeToken<List<SavedScenario>>() {}.getType();
            List<SavedScenario> saveData = GSON.fromJson(json, listType);
            if (saveData == null) return;

            for (SavedScenario saved : saveData) {
                try {
                    MissionScenario scenario = fromSaved(saved);
                    scenarios.put(scenario.getId(), scenario);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load scenario '{}': {}", saved.name, e.getMessage());
                }
            }
            LOGGER.info("Loaded {} scenarios", scenarios.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load scenarios", e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // KONVERTIERUNG
    // ═══════════════════════════════════════════════════════════

    private static SavedScenario toSaved(MissionScenario scenario) {
        SavedScenario s = new SavedScenario();
        s.id = scenario.getId();
        s.name = scenario.getName();
        s.description = scenario.getDescription();
        s.difficulty = scenario.getDifficulty();
        s.minGangLevel = scenario.getMinGangLevel();
        s.active = scenario.isActive();
        s.missionType = scenario.getMissionType();

        for (ScenarioObjective obj : scenario.getObjectives()) {
            SavedObjective so = new SavedObjective();
            so.id = obj.getId();
            so.type = obj.getType().name();
            so.x = obj.getEditorX();
            so.y = obj.getEditorY();
            so.params = new HashMap<>(obj.getParams());
            so.nextId = obj.getNextObjectiveId();
            s.objectives.add(so);
        }
        return s;
    }

    private static MissionScenario fromSaved(SavedScenario saved) {
        List<ScenarioObjective> objectives = new ArrayList<>();
        for (SavedObjective so : saved.objectives) {
            ObjectiveType type = ObjectiveType.valueOf(so.type);
            objectives.add(new ScenarioObjective(
                    so.id, type, so.x, so.y, so.params, so.nextId
            ));
        }
        return new MissionScenario(
                saved.id, saved.name, saved.description,
                saved.difficulty, saved.minGangLevel, saved.active,
                saved.missionType, objectives
        );
    }

    // Serialisierungs-Klassen
    static class SavedScenario {
        String id, name, description, missionType;
        int difficulty, minGangLevel;
        boolean active;
        List<SavedObjective> objectives = new ArrayList<>();
    }

    static class SavedObjective {
        String id, type, nextId;
        int x, y;
        Map<String, String> params = new HashMap<>();
    }
}
