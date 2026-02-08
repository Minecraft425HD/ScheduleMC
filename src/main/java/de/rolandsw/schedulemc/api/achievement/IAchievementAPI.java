package de.rolandsw.schedulemc.api.achievement;

import de.rolandsw.schedulemc.achievement.Achievement;
import de.rolandsw.schedulemc.achievement.AchievementCategory;
import de.rolandsw.schedulemc.achievement.PlayerAchievements;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Public Achievement API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Achievement-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>4 Achievement-Kategorien (Economy, Crime, Production, Social)</li>
 *   <li>5 Achievement-Tiers (Bronze, Silver, Gold, Diamond, Platinum)</li>
 *   <li>Fortschritt-Tracking und Belohnungen</li>
 *   <li>Automatische Geld-Belohnungen beim Freischalten</li>
 *   <li>Persistente Speicherung</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap-basierte Datenhaltung.
 *
 * <h2>Achievement-Kategorien:</h2>
 * <ul>
 *   <li>ECONOMY - Wirtschafts-Achievements (Geld verdienen, Kredite, Sparen)</li>
 *   <li>CRIME - Verbrechen-Achievements (Wanted-Level, Escape, Gefängnis)</li>
 *   <li>PRODUCTION - Produktions-Achievements (Pflanzen, Drogen, Imperium)</li>
 *   <li>SOCIAL - Sozial-Achievements (Plots, Vermietung, Ratings)</li>
 * </ul>
 *
 * <h2>Achievement-Tiers und Belohnungen:</h2>
 * <ul>
 *   <li>BRONZE - 100€ Belohnung</li>
 *   <li>SILVER - 500€ Belohnung</li>
 *   <li>GOLD - 2.500€ Belohnung</li>
 *   <li>DIAMOND - 10.000€ Belohnung</li>
 *   <li>PLATINUM - 50.000€ Belohnung</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IAchievementAPI achievementAPI = ScheduleMCAPI.getAchievementAPI();
 *
 * // Fortschritt hinzufügen
 * achievementAPI.addProgress(playerUUID, "FIRST_EURO", 1.0);
 *
 * // Fortschritt absolut setzen
 * achievementAPI.setProgress(playerUUID, "RICH", 10000.0);
 *
 * // Achievement manuell freischalten
 * achievementAPI.unlockAchievement(playerUUID, "MILLIONAIRE");
 *
 * // Player-Achievements abrufen
 * PlayerAchievements playerAch = achievementAPI.getPlayerAchievements(playerUUID);
 * int unlocked = playerAch.getUnlockedCount();
 *
 * // Alle Achievements einer Kategorie
 * List<Achievement> economyAchievements = achievementAPI.getAchievementsByCategory(
 *     AchievementCategory.ECONOMY
 * );
 *
 * // Statistiken anzeigen
 * String stats = achievementAPI.getStatistics(playerUUID);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IAchievementAPI {

    /**
     * Gibt PlayerAchievements für einen Spieler zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @return PlayerAchievements-Objekt (niemals null, wird erstellt falls nicht vorhanden)
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    PlayerAchievements getPlayerAchievements(UUID playerUUID);

    /**
     * Fügt Fortschritt zu einem Achievement hinzu (inkrementell).
     * <p>
     * Schaltet Achievement automatisch frei wenn Anforderung erreicht.
     * Zahlt automatisch Belohnung aus.
     *
     * @param playerUUID Die UUID des Spielers
     * @param achievementId Die Achievement-ID (z.B. "FIRST_EURO")
     * @param amount Hinzuzufügender Fortschritt
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void addProgress(UUID playerUUID, String achievementId, double amount);

    /**
     * Setzt Fortschritt eines Achievements (absolut).
     * <p>
     * Überschreibt vorherigen Fortschritt.
     * Schaltet Achievement automatisch frei wenn Anforderung erreicht.
     *
     * @param playerUUID Die UUID des Spielers
     * @param achievementId Die Achievement-ID
     * @param value Der neue Fortschrittswert
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void setProgress(UUID playerUUID, String achievementId, double value);

    /**
     * Schaltet ein Achievement manuell frei.
     * <p>
     * Zahlt automatisch Belohnung aus.
     *
     * @param playerUUID Die UUID des Spielers
     * @param achievementId Die Achievement-ID
     * @return true wenn erfolgreich freigeschaltet, false wenn bereits freigeschaltet
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    boolean unlockAchievement(UUID playerUUID, String achievementId);

    /**
     * Gibt ein Achievement anhand seiner ID zurück.
     *
     * @param achievementId Die Achievement-ID
     * @return Das Achievement oder null wenn nicht gefunden
     * @throws IllegalArgumentException wenn achievementId null ist
     */
    @Nullable
    Achievement getAchievement(String achievementId);

    /**
     * Gibt alle registrierten Achievements zurück.
     *
     * @return Collection aller Achievements (unveränderbare Kopie)
     */
    Collection<Achievement> getAllAchievements();

    /**
     * Gibt alle Achievements einer bestimmten Kategorie zurück.
     *
     * @param category Die Achievement-Kategorie
     * @return Liste aller Achievements dieser Kategorie (kann leer sein)
     * @throws IllegalArgumentException wenn category null ist
     */
    List<Achievement> getAchievementsByCategory(AchievementCategory category);

    /**
     * Gibt Achievement-Statistiken für einen Spieler zurück.
     * <p>
     * Format: "Achievements: X/Y (Z%) - €A verdient"
     *
     * @param playerUUID Die UUID des Spielers
     * @return Formatierte Statistik-Zeichenkette
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    String getStatistics(UUID playerUUID);

    /**
     * Gibt die Gesamtanzahl registrierter Achievements zurück.
     *
     * @return Anzahl der Achievements
     */
    int getTotalAchievementCount();

    /**
     * Gibt Anzahl freigeschalteter Achievements für einen Spieler zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @return Anzahl freigeschalteter Achievements
     * @throws IllegalArgumentException wenn playerUUID null ist
     */
    int getUnlockedCount(UUID playerUUID);

    /**
     * Gibt Fortschritt eines Achievements zurück.
     *
     * @param playerUUID Die UUID des Spielers
     * @param achievementId Die Achievement-ID
     * @return Aktueller Fortschritt (0.0 wenn nicht begonnen)
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    double getProgress(UUID playerUUID, String achievementId);

    /**
     * Prüft ob ein Achievement freigeschaltet ist.
     *
     * @param playerUUID Die UUID des Spielers
     * @param achievementId Die Achievement-ID
     * @return true wenn freigeschaltet
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    boolean isUnlocked(UUID playerUUID, String achievementId);

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns the completion percentage for a player.
     *
     * @param playerUUID The player's UUID
     * @return Percentage 0.0 - 100.0
     * @throws IllegalArgumentException if playerUUID is null
     * @since 3.2.0
     */
    double getCompletionPercentage(UUID playerUUID);

    /**
     * Returns the total rewards earned by a player.
     *
     * @param playerUUID The player's UUID
     * @return Total rewards in Euro
     * @throws IllegalArgumentException if playerUUID is null
     * @since 3.2.0
     */
    double getTotalRewardsEarned(UUID playerUUID);

    /**
     * Returns all unlocked achievements for a player.
     *
     * @param playerUUID The player's UUID
     * @return List of unlocked Achievement objects
     * @throws IllegalArgumentException if playerUUID is null
     * @since 3.2.0
     */
    List<Achievement> getUnlockedAchievements(UUID playerUUID);

    /**
     * Resets all achievement progress for a player.
     *
     * @param playerUUID The player's UUID
     * @throws IllegalArgumentException if playerUUID is null
     * @since 3.2.0
     */
    void resetPlayerAchievements(UUID playerUUID);

    /**
     * Returns the top N players by achievement count.
     *
     * @param limit Maximum entries
     * @return Sorted list of UUID-Count pairs
     * @throws IllegalArgumentException if limit < 1
     * @since 3.2.0
     */
    java.util.List<java.util.Map.Entry<UUID, Integer>> getTopAchievers(int limit);
}
