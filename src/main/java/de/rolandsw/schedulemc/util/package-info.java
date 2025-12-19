/**
 * Core utilities and design patterns for ScheduleMC
 *
 * <h2>Overview</h2>
 * This package contains essential utility classes that provide reusable patterns
 * and helper functions used throughout the ScheduleMC mod.
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link de.rolandsw.schedulemc.util.AbstractPersistenceManager} -
 *       Template Method pattern for JSON persistence with backup/recovery</li>
 *   <li>{@link de.rolandsw.schedulemc.util.CommandExecutor} -
 *       Functional interface pattern for consistent command execution</li>
 *   <li>{@link de.rolandsw.schedulemc.util.PacketHandler} -
 *       Functional interface pattern for network packet handling</li>
 *   <li>{@link de.rolandsw.schedulemc.util.EventHelper} -
 *       Functional interface pattern for Forge event handling</li>
 *   <li>{@link de.rolandsw.schedulemc.util.BackupManager} -
 *       Automatic backup rotation for data files</li>
 *   <li>{@link de.rolandsw.schedulemc.util.GsonHelper} -
 *       Shared Gson instance with custom serializers</li>
 *   <li>{@link de.rolandsw.schedulemc.util.HealthCheckManager} -
 *       System health monitoring</li>
 *   <li>{@link de.rolandsw.schedulemc.util.LocaleHelper} -
 *       Client locale detection for multilingual support</li>
 * </ul>
 *
 * <h2>Design Patterns</h2>
 *
 * <h3>Template Method Pattern</h3>
 * {@link de.rolandsw.schedulemc.util.AbstractPersistenceManager} provides a template
 * for data persistence with:
 * <ul>
 *   <li>Automatic backup rotation</li>
 *   <li>Atomic file writes</li>
 *   <li>Corruption recovery</li>
 *   <li>Health monitoring</li>
 * </ul>
 *
 * <h3>Functional Interface Pattern</h3>
 * {@link de.rolandsw.schedulemc.util.CommandExecutor},
 * {@link de.rolandsw.schedulemc.util.PacketHandler}, and
 * {@link de.rolandsw.schedulemc.util.EventHelper} use lambda-based error handling
 * to eliminate boilerplate code.
 *
 * <h2>Code Reduction</h2>
 * The utilities in this package have eliminated approximately 2,852 lines of
 * duplicate code across the codebase through pattern-based refactoring.
 *
 * @since 2.0.0
 */
package de.rolandsw.schedulemc.util;
