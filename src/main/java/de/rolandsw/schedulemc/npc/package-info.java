/**
 * NPC system providing autonomous entities with AI behaviors, schedules, crime detection, and police response.
 *
 * <h2>Overview</h2>
 * <p>This package implements a comprehensive NPC system where entities follow daily schedules,
 * perform work activities, detect crimes, and respond with police AI. NPCs are persistent,
 * configurable, and integrate deeply with the economy and plot systems.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>NPC Entities:</strong> Custom entity types with AI goals and behaviors</li>
 *   <li><strong>Schedule System:</strong> Time-based routines (work, leisure, sleep)</li>
 *   <li><strong>Crime Detection:</strong> NPCs detect illegal activities (stealing, trespassing, violence)</li>
 *   <li><strong>Police AI:</strong> Automated pursuit, arrest, and bounty system</li>
 *   <li><strong>Prison System:</strong> Arrest processing, time-based release, parole</li>
 * </ul>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li><strong>npc.crime:</strong> {@link de.rolandsw.schedulemc.npc.crime} - Crime tracking, bounty management</li>
 *   <li><strong>npc.crime.prison:</strong> {@link de.rolandsw.schedulemc.npc.crime.prison} - Prison system and release</li>
 *   <li><strong>npc.events:</strong> NPC event handlers (tick, interaction, AI)</li>
 *   <li><strong>npc.commands:</strong> {@link de.rolandsw.schedulemc.npc.commands} - NPC management commands</li>
 * </ul>
 *
 * <h2>Police AI System</h2>
 * <p>The {@link de.rolandsw.schedulemc.npc.events.PoliceAIHandler} has been extensively refactored:</p>
 * <ul>
 *   <li>onPoliceAI: 212 → 65 LOC (5 helper methods extracted)</li>
 *   <li>arrestPlayer: 134 → 40 LOC (5 helper methods extracted)</li>
 *   <li>onPlayerTick: 157 → 30 LOC (4 helper methods extracted)</li>
 *   <li><strong>Total: 503 → 135 LOC (-73.2% reduction)</strong></li>
 * </ul>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Time-based schedules with location navigation</li>
 *   <li>Crime level tracking and wanted status</li>
 *   <li>Police search patterns and pursuit behaviors</li>
 *   <li>Arrest mechanics with resistance checks</li>
 *   <li>Prison system with time-based release</li>
 *   <li>Bounty system for wanted criminals</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.npc.events.PoliceAIHandler
 * @see de.rolandsw.schedulemc.npc.crime.CrimeManager
 * @see de.rolandsw.schedulemc.npc.crime.prison.PrisonManager
 */
package de.rolandsw.schedulemc.npc;
