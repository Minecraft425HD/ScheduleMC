/**
 * Prison system for arrested players with time-based sentences and release mechanics.
 *
 * <h2>Overview</h2>
 * <p>This package manages the arrest, imprisonment, and release of players who have been
 * caught by police NPCs. Players serve time-based sentences and are released automatically
 * or can pay fines for early release.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.npc.crime.prison.PrisonManager}:</strong> Arrest processing and release</li>
 *   <li><strong>Prison Data:</strong> Sentence time, crime level, location</li>
 *   <li><strong>Release Mechanics:</strong> Automatic release, parole, fine payment</li>
 * </ul>
 *
 * <h2>Arrest Flow</h2>
 * <pre>
 * Police catches player (PoliceAIHandler)
 *     ↓
 * PrisonManager.arrest(player, crimeLevel)
 *     ↓
 * Calculate sentence (crimeLevel × 60 seconds)
 *     ↓
 * Teleport to prison spawn
 *     ↓
 * Start sentence timer
 *     ↓
 * Automatic release on timer expiration
 * </pre>
 *
 * <h2>Sentence Calculation</h2>
 * <ul>
 *   <li><strong>Formula:</strong> sentenceTime = crimeLevel × 60 seconds</li>
 *   <li><strong>Minimum:</strong> 1 minute (crime level 1-9)</li>
 *   <li><strong>Maximum:</strong> 100 minutes (crime level 100+)</li>
 *   <li><strong>Early Release:</strong> Pay fine (10€ per minute remaining)</li>
 * </ul>
 *
 * <h2>Prison Mechanics</h2>
 * <ul>
 *   <li>Players cannot break blocks while imprisoned</li>
 *   <li>Players cannot leave prison area (forced teleport back)</li>
 *   <li>Time served shown in action bar</li>
 *   <li>Release teleports player to spawn or plot</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.npc.crime.prison.PrisonManager
 * @see de.rolandsw.schedulemc.npc.crime.CrimeManager
 * @see de.rolandsw.schedulemc.npc.events.PoliceAIHandler
 */
package de.rolandsw.schedulemc.npc.crime.prison;
