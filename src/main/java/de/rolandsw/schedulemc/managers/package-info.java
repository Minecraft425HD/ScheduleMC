/**
 * Core manager classes for cross-cutting concerns like daily rewards, rent, NPCs, and memory cleanup.
 *
 * <h2>Overview</h2>
 * <p>This package contains manager classes that coordinate between multiple systems
 * and handle scheduled/recurring operations. Managers follow singleton patterns and
 * provide thread-safe access to shared resources.</p>
 *
 * <h2>Core Managers</h2>
 * <ul>
 *   <li><strong>DailyRewardManager:</strong> Daily login bonuses and streaks</li>
 *   <li><strong>RentManager:</strong> Automated rent collection for plots/apartments</li>
 *   <li><strong>NPCEntityRegistry:</strong> NPC entity type registration and spawning</li>
 *   <li><strong>MemoryCleanupManager:</strong> Periodic cleanup of expired data</li>
 * </ul>
 *
 * <h2>Manager Pattern</h2>
 * <pre>
 * Manager (Singleton)
 *     ↓
 * Initialize (on mod load)
 *     ↓
 * Tick/Schedule (automated operations)
 *     ↓
 * Persist (save to file)
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <ul>
 *   <li>All managers use ConcurrentHashMap for data storage</li>
 *   <li>Write operations are synchronized</li>
 *   <li>Reads use lock-free patterns where possible</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.managers.DailyRewardManager
 * @see de.rolandsw.schedulemc.managers.RentManager
 */
package de.rolandsw.schedulemc.managers;
