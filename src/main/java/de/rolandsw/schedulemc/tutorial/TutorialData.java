package de.rolandsw.schedulemc.tutorial;

import com.google.gson.annotations.SerializedName;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tutorial-Fortschritt eines Spielers
 *
 * Speichert:
 * - Aktueller Schritt
 * - Abgeschlossene Schritte
 * - Task-Fortschritt pro Schritt
 * - Tutorial aktiviert/deaktiviert
 */
public class TutorialData {

    @SerializedName("playerUUID")
    private final UUID playerUUID;

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("currentStep")
    private TutorialStep currentStep;

    @SerializedName("completedSteps")
    private final Map<TutorialStep, Boolean> completedSteps;

    @SerializedName("taskProgress")
    private final Map<TutorialStep, Integer> taskProgress; // Step -> completed task count

    @SerializedName("startTime")
    private long startTime; // Wann Tutorial gestartet

    @SerializedName("completionTime")
    private long completionTime; // Wann Tutorial abgeschlossen

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public TutorialData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.enabled = true;
        this.currentStep = TutorialStep.WELCOME;
        this.completedSteps = new HashMap<>();
        this.taskProgress = new HashMap<>();
        this.startTime = System.currentTimeMillis();
        this.completionTime = 0;

        // Initialize all steps as not completed
        for (TutorialStep step : TutorialStep.values()) {
            completedSteps.put(step, false);
            taskProgress.put(step, 0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public TutorialStep getCurrentStep() {
        return currentStep;
    }

    public boolean isStepCompleted(TutorialStep step) {
        return completedSteps.getOrDefault(step, false);
    }

    public int getTaskProgress(TutorialStep step) {
        return taskProgress.getOrDefault(step, 0);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    /**
     * Ist Tutorial komplett abgeschlossen?
     */
    public boolean isCompleted() {
        return completionTime > 0;
    }

    /**
     * Gesamtfortschritt in Prozent
     */
    public int getTotalProgress() {
        int completed = (int) completedSteps.values().stream().filter(b -> b).count();
        return (int) ((completed / (float) TutorialStep.values().length) * 100);
    }

    // ═══════════════════════════════════════════════════════════
    // SETTERS
    // ═══════════════════════════════════════════════════════════

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCurrentStep(TutorialStep step) {
        this.currentStep = step;
    }

    /**
     * Markiert einen Schritt als abgeschlossen
     */
    public void completeStep(TutorialStep step) {
        completedSteps.put(step, true);

        // Wenn letzter Schritt, setze Completion Time
        if (step.isLast()) {
            this.completionTime = System.currentTimeMillis();
        }
    }

    /**
     * Erhöht Task-Fortschritt für einen Schritt
     */
    public void incrementTaskProgress(TutorialStep step) {
        int current = taskProgress.getOrDefault(step, 0);
        taskProgress.put(step, current + 1);
    }

    /**
     * Setzt Task-Fortschritt zurück
     */
    public void resetTaskProgress(TutorialStep step) {
        taskProgress.put(step, 0);
    }

    /**
     * Geht zum nächsten Schritt
     */
    public boolean advanceToNextStep() {
        TutorialStep next = currentStep.getNext();
        if (next != null) {
            completeStep(currentStep);
            currentStep = next;
            return true;
        }
        return false;
    }

    /**
     * Überspringt einen Schritt
     */
    public boolean skipCurrentStep() {
        return advanceToNextStep();
    }

    /**
     * Setzt Tutorial zurück
     */
    public void reset() {
        this.enabled = true;
        this.currentStep = TutorialStep.WELCOME;
        this.completionTime = 0;
        this.startTime = System.currentTimeMillis();

        for (TutorialStep step : TutorialStep.values()) {
            completedSteps.put(step, false);
            taskProgress.put(step, 0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Enabled", enabled);
        tag.putInt("CurrentStep", currentStep.ordinal());
        tag.putLong("StartTime", startTime);
        tag.putLong("CompletionTime", completionTime);

        // Save completed steps
        for (TutorialStep step : TutorialStep.values()) {
            tag.putBoolean("Completed_" + step.name(), completedSteps.getOrDefault(step, false));
            tag.putInt("Progress_" + step.name(), taskProgress.getOrDefault(step, 0));
        }

        return tag;
    }

    public static TutorialData load(UUID playerUUID, CompoundTag tag) {
        TutorialData data = new TutorialData(playerUUID);

        data.enabled = tag.getBoolean("Enabled");

        int stepOrdinal = tag.getInt("CurrentStep");
        if (stepOrdinal >= 0 && stepOrdinal < TutorialStep.values().length) {
            data.currentStep = TutorialStep.values()[stepOrdinal];
        }

        data.startTime = tag.getLong("StartTime");
        data.completionTime = tag.getLong("CompletionTime");

        // Load completed steps
        for (TutorialStep step : TutorialStep.values()) {
            if (tag.contains("Completed_" + step.name())) {
                data.completedSteps.put(step, tag.getBoolean("Completed_" + step.name()));
            }
            if (tag.contains("Progress_" + step.name())) {
                data.taskProgress.put(step, tag.getInt("Progress_" + step.name()));
            }
        }

        return data;
    }

    // ═══════════════════════════════════════════════════════════
    // DISPLAY
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("TutorialData[%s, step=%s, progress=%d%%, enabled=%b]",
            playerUUID, currentStep.name(), getTotalProgress(), enabled);
    }

    /**
     * Gibt formatierte Fortschritts-Anzeige zurück
     */
    public String getFormattedProgress() {
        int completed = (int) completedSteps.values().stream().filter(b -> b).count();
        int total = TutorialStep.values().length;

        StringBuilder bar = new StringBuilder();
        bar.append("§7[");

        for (int i = 0; i < total; i++) {
            if (i < completed) {
                bar.append("§a■");
            } else if (i == completed) {
                bar.append("§e■");
            } else {
                bar.append("§8□");
            }
        }

        bar.append("§7] §f").append(completed).append("/").append(total);
        bar.append(" (§e").append(getTotalProgress()).append("%§f)");

        return bar.toString();
    }
}
