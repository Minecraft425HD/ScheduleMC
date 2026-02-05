package de.rolandsw.schedulemc.gang.scenario;

import java.util.HashMap;
import java.util.Map;

/**
 * Ein einzelner Baustein (Objective) innerhalb eines Szenarios.
 *
 * Speichert sowohl die logischen Daten (Typ, Parameter, Verbindung)
 * als auch die visuelle Position im Editor-Canvas.
 */
public class ScenarioObjective {

    private String id;
    private ObjectiveType type;
    private int editorX;
    private int editorY;
    private Map<String, String> params;
    private String nextObjectiveId; // null = kein Nachfolger

    public ScenarioObjective(String id, ObjectiveType type, int editorX, int editorY) {
        this.id = id;
        this.type = type;
        this.editorX = editorX;
        this.editorY = editorY;
        this.params = new HashMap<>();
        this.nextObjectiveId = null;

        // Standard-Parameter setzen
        for (ObjectiveType.ParamDef def : type.getParamDefs()) {
            params.put(def.key(), def.defaultValue());
        }
    }

    /**
     * Deserialisierungs-Konstruktor.
     */
    public ScenarioObjective(String id, ObjectiveType type, int editorX, int editorY,
                             Map<String, String> params, String nextObjectiveId) {
        this.id = id;
        this.type = type;
        this.editorX = editorX;
        this.editorY = editorY;
        this.params = params != null ? new HashMap<>(params) : new HashMap<>();
        this.nextObjectiveId = nextObjectiveId;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() { return id; }
    public ObjectiveType getType() { return type; }

    public int getEditorX() { return editorX; }
    public int getEditorY() { return editorY; }
    public void setEditorX(int x) { this.editorX = x; }
    public void setEditorY(int y) { this.editorY = y; }

    public Map<String, String> getParams() { return params; }
    public String getParam(String key) { return params.getOrDefault(key, ""); }
    public void setParam(String key, String value) { params.put(key, value); }

    public String getNextObjectiveId() { return nextObjectiveId; }
    public void setNextObjectiveId(String nextId) { this.nextObjectiveId = nextId; }

    /**
     * Gibt eine kurze Zusammenfassung der Parameter zurueck (fuer Block-Anzeige).
     */
    public String getParamSummary() {
        if (params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (ObjectiveType.ParamDef def : type.getParamDefs()) {
            String val = params.get(def.key());
            if (val != null && !val.isEmpty()) {
                if (count > 0) sb.append(" ");
                sb.append(def.label()).append(":").append(val);
                count++;
                if (count >= 2) break; // Maximal 2 Parameter anzeigen
            }
        }
        return sb.toString();
    }

    /**
     * Erstellt eine tiefe Kopie dieses Objectives.
     */
    public ScenarioObjective copy(String newId) {
        return new ScenarioObjective(newId, type, editorX, editorY,
                new HashMap<>(params), nextObjectiveId);
    }
}
