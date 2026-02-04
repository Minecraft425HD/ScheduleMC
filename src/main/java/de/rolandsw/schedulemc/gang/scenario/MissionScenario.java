package de.rolandsw.schedulemc.gang.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ein vollstaendiges Mission-Szenario bestehend aus mehreren Bausteinen (Objectives).
 *
 * Szenarien werden im Editor erstellt und koennen als Gang-Auftraege aktiviert werden.
 * Jedes Szenario hat Metadaten (Name, Schwierigkeit, Belohnungen) und eine
 * geordnete Liste von Objectives mit visuellen Positionen und Verbindungen.
 */
public class MissionScenario {

    private String id;
    private String name;
    private String description;
    private int difficulty;         // 1-5
    private int minGangLevel;       // 0 = beliebig
    private boolean active;         // In Rotation fuer Gang-Auftraege
    private String missionType;     // HOURLY, DAILY, WEEKLY
    private List<ScenarioObjective> objectives;

    /**
     * Neues leeres Szenario.
     */
    public MissionScenario() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = "Neues Szenario";
        this.description = "";
        this.difficulty = 1;
        this.minGangLevel = 0;
        this.active = false;
        this.missionType = "DAILY";
        this.objectives = new ArrayList<>();

        // Standard: START + REWARD Bloecke
        ScenarioObjective start = new ScenarioObjective("s0", ObjectiveType.START, 120, 30);
        ScenarioObjective reward = new ScenarioObjective("r0", ObjectiveType.REWARD, 120, 250);
        start.setNextObjectiveId("r0");
        objectives.add(start);
        objectives.add(reward);
    }

    /**
     * Deserialisierungs-Konstruktor.
     */
    public MissionScenario(String id, String name, String description,
                           int difficulty, int minGangLevel, boolean active,
                           String missionType, List<ScenarioObjective> objectives) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.minGangLevel = minGangLevel;
        this.active = active;
        this.missionType = missionType;
        this.objectives = objectives != null ? new ArrayList<>(objectives) : new ArrayList<>();
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = Math.max(1, Math.min(5, difficulty)); }
    public int getMinGangLevel() { return minGangLevel; }
    public void setMinGangLevel(int level) { this.minGangLevel = Math.max(0, Math.min(30, level)); }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getMissionType() { return missionType; }
    public void setMissionType(String type) { this.missionType = type; }
    public List<ScenarioObjective> getObjectives() { return objectives; }

    // ═══════════════════════════════════════════════════════════
    // OBJECTIVE-VERWALTUNG
    // ═══════════════════════════════════════════════════════════

    public void addObjective(ScenarioObjective obj) {
        objectives.add(obj);
    }

    public void removeObjective(String objectiveId) {
        // Verbindungen aufloesen die auf dieses Objective zeigen
        for (ScenarioObjective obj : objectives) {
            if (objectiveId.equals(obj.getNextObjectiveId())) {
                obj.setNextObjectiveId(null);
            }
        }
        objectives.removeIf(o -> o.getId().equals(objectiveId));
    }

    public ScenarioObjective getObjective(String objectiveId) {
        for (ScenarioObjective obj : objectives) {
            if (obj.getId().equals(objectiveId)) return obj;
        }
        return null;
    }

    /**
     * Generiert eine neue eindeutige Objective-ID.
     */
    public String nextObjectiveId() {
        int max = 0;
        for (ScenarioObjective obj : objectives) {
            String id = obj.getId();
            if (id.startsWith("o")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "o" + (max + 1);
    }

    /**
     * Zaehlt die Objectives (ohne START und REWARD).
     */
    public int getStepCount() {
        int count = 0;
        for (ScenarioObjective obj : objectives) {
            if (obj.getType() != ObjectiveType.START && obj.getType() != ObjectiveType.REWARD) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gibt die Gesamt-XP des Szenarios zurueck (aus REWARD-Block).
     */
    public int getTotalXP() {
        for (ScenarioObjective obj : objectives) {
            if (obj.getType() == ObjectiveType.REWARD) {
                try { return Integer.parseInt(obj.getParam("xp")); }
                catch (NumberFormatException e) { return 0; }
            }
        }
        return 0;
    }

    /**
     * Gibt das Gesamt-Geld des Szenarios zurueck (aus REWARD-Block).
     */
    public int getTotalMoney() {
        for (ScenarioObjective obj : objectives) {
            if (obj.getType() == ObjectiveType.REWARD) {
                try { return Integer.parseInt(obj.getParam("money")); }
                catch (NumberFormatException e) { return 0; }
            }
        }
        return 0;
    }

    /**
     * Schwierigkeits-Anzeige als Sterne.
     */
    public String getDifficultyStars() {
        return "\u2605".repeat(difficulty) + "\u2606".repeat(5 - difficulty);
    }
}
