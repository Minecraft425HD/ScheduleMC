/**
 * Configuration system for server and client settings with Forge config integration.
 *
 * <h2>Overview</h2>
 * <p>This package manages all ScheduleMC configuration including economy rates, plot sizes,
 * production timers, crime thresholds, and UI preferences. Configurations are persisted
 * using Forge's config system and support hot-reloading.</p>
 *
 * <h2>Configuration Categories</h2>
 * <ul>
 *   <li><strong>Economy:</strong> Transaction fees, interest rates, starting balance</li>
 *   <li><strong>Plot:</strong> Plot sizes, rent prices, claim costs</li>
 *   <li><strong>Production:</strong> Growth timers, yield multipliers, water/electricity costs</li>
 *   <li><strong>Crime:</strong> Wanted thresholds, prison times, police AI aggression</li>
 *   <li><strong>UI:</strong> Map settings, notification preferences, smartphone apps</li>
 * </ul>
 *
 * <h2>ModConfigHandler</h2>
 * <p>The central {@link de.rolandsw.schedulemc.config.ModConfigHandler} class (483 LOC)
 * registers all configuration options and provides type-safe access methods.</p>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.config.ModConfigHandler
 */
package de.rolandsw.schedulemc.config;
