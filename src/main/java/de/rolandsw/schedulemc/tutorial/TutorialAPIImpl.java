package de.rolandsw.schedulemc.tutorial;

import de.rolandsw.schedulemc.api.tutorial.ITutorialAPI;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Implementierung der Tutorial-API
 */
public class TutorialAPIImpl implements ITutorialAPI {

    @Override
    public TutorialData getTutorialData(UUID playerUUID) {
        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            throw new IllegalStateException("TutorialManager not initialized!");
        }
        return manager.getTutorialData(playerUUID);
    }

    @Override
    public boolean isTutorialEnabled(UUID playerUUID) {
        return getTutorialData(playerUUID).isEnabled();
    }

    @Override
    public TutorialStep getCurrentStep(UUID playerUUID) {
        return getTutorialData(playerUUID).getCurrentStep();
    }

    @Override
    public boolean isStepCompleted(UUID playerUUID, TutorialStep step) {
        return getTutorialData(playerUUID).isStepCompleted(step);
    }

    @Override
    public boolean isTutorialCompleted(UUID playerUUID) {
        return getTutorialData(playerUUID).isCompleted();
    }

    @Override
    public int getTutorialProgress(UUID playerUUID) {
        return getTutorialData(playerUUID).getTotalProgress();
    }

    @Override
    public void completeStep(UUID playerUUID, TutorialStep step) {
        getTutorialData(playerUUID).completeStep(step);
    }

    @Override
    public void setTutorialEnabled(UUID playerUUID, boolean enabled) {
        getTutorialData(playerUUID).setEnabled(enabled);
    }

    @Override
    public void resetTutorial(UUID playerUUID) {
        getTutorialData(playerUUID).reset();
    }

    @Override
    public String getStatistics() {
        TutorialManager manager = TutorialManager.getInstance();
        if (manager == null) {
            return "TutorialManager not initialized";
        }
        return manager.getStatistics();
    }

    @Nullable
    @Override
    public Object getTutorialManager() {
        return TutorialManager.getInstance();
    }
}
