/**
 * Crime detection, tracking, and bounty management system for ScheduleMC.
 *
 * <h2>Overview</h2>
 * <p>This package implements a comprehensive crime system where player actions (theft, violence,
 * trespassing) are detected, tracked, and result in wanted levels, police pursuit, and bounties.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.npc.crime.CrimeManager}:</strong> Central crime tracking and level calculation</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.npc.crime.BountyManager}:</strong> Bounty creation and reward distribution</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.npc.crime.BountyData}:</strong> Bounty information (amount, reason, issuer)</li>
 * </ul>
 *
 * <h2>Crime Levels</h2>
 * <p>Crime is tracked on a scale from 0 (clean) to 100+ (most wanted):</p>
 * <ul>
 *   <li><strong>0-10:</strong> Clean record (no police interest)</li>
 *   <li><strong>10-30:</strong> Suspicious (police may investigate)</li>
 *   <li><strong>30-60:</strong> Wanted (active police pursuit)</li>
 *   <li><strong>60+:</strong> Most Wanted (aggressive police response, high bounty)</li>
 * </ul>
 *
 * <h2>Crime Types and Penalties</h2>
 * <pre>
 * Theft:         +5 to +20 crime level (based on value)
 * Violence:      +10 to +30 crime level (based on damage)
 * Trespassing:   +2 to +5 crime level
 * Murder:        +50 crime level
 * </pre>
 *
 * <h2>Decay System</h2>
 * <p>Crime levels decay over time:</p>
 * <ul>
 *   <li>-1 crime level per minute (if not actively committing crimes)</li>
 *   <li>Faster decay when in prison or paying fines</li>
 *   <li>Bounties remain until collected or pardoned</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <ul>
 *   <li><strong>Police AI:</strong> {@link de.rolandsw.schedulemc.npc.events.PoliceAIHandler} uses crime levels for targeting</li>
 *   <li><strong>Prison:</strong> {@link de.rolandsw.schedulemc.npc.crime.prison.PrisonManager} processes arrests</li>
 *   <li><strong>Economy:</strong> Bounties paid through {@link de.rolandsw.schedulemc.economy.EconomyManager}</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.npc.crime.CrimeManager
 * @see de.rolandsw.schedulemc.npc.crime.BountyManager
 * @see de.rolandsw.schedulemc.npc.crime.prison
 */
package de.rolandsw.schedulemc.npc.crime;
