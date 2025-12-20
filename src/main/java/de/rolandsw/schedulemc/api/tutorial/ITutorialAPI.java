package de.rolandsw.schedulemc.api.tutorial;

import de.rolandsw.schedulemc.tutorial.TutorialData;
import de.rolandsw.schedulemc.tutorial.TutorialStep;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Public Tutorial API
 *
 * Ermöglicht externen Mods Zugriff auf das Tutorial-System.
 */
public interface ITutorialAPI {

    /**
     * Holt Tutorial-Daten eines Spielers
     *
     * @param playerUUID UUID des Spielers
     * @return Tutorial-Daten (niemals null)
     */
    TutorialData getTutorialData(UUID playerUUID);

    /**
     * Ist Tutorial für Spieler aktiviert?
     *
     * @param playerUUID UUID des Spielers
     * @return true wenn aktiviert
     */
    boolean isTutorialEnabled(UUID playerUUID);

    /**
     * Aktueller Tutorial-Schritt
     *
     * @param playerUUID UUID des Spielers
     * @return Aktueller Schritt
     */
    TutorialStep getCurrentStep(UUID playerUUID);

    /**
     * Ist ein Schritt abgeschlossen?
     *
     * @param playerUUID UUID des Spielers
     * @param step Tutorial-Schritt
     * @return true wenn abgeschlossen
     */
    boolean isStepCompleted(UUID playerUUID, TutorialStep step);

    /**
     * Ist Tutorial komplett abgeschlossen?
     *
     * @param playerUUID UUID des Spielers
     * @return true wenn abgeschlossen
     */
    boolean isTutorialCompleted(UUID playerUUID);

    /**
     * Fortschritt in Prozent
     *
     * @param playerUUID UUID des Spielers
     * @return Prozent (0-100)
     */
    int getTutorialProgress(UUID playerUUID);

    /**
     * Markiert einen Schritt als abgeschlossen
     *
     * @param playerUUID UUID des Spielers
     * @param step Tutorial-Schritt
     */
    void completeStep(UUID playerUUID, TutorialStep step);

    /**
     * Setzt Tutorial-Aktivierung
     *
     * @param playerUUID UUID des Spielers
     * @param enabled true = aktivieren, false = deaktivieren
     */
    void setTutorialEnabled(UUID playerUUID, boolean enabled);

    /**
     * Setzt Tutorial zurück
     *
     * @param playerUUID UUID des Spielers
     */
    void resetTutorial(UUID playerUUID);

    /**
     * Statistiken
     *
     * @return Statistik-String
     */
    String getStatistics();

    /**
     * Tutorial-Manager-Instanz (für erweiterten Zugriff)
     *
     * @return Manager oder null wenn nicht verfügbar
     */
    @Nullable
    Object getTutorialManager();
}
