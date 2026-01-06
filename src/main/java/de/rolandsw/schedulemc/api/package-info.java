/**
 * Public API interfaces for ScheduleMC integration and extension.
 *
 * <h2>Overview</h2>
 * <p>This package provides stable public APIs for interacting with ScheduleMC systems.
 * These interfaces are designed for external mods, plugins, and internal decoupling.</p>
 *
 * <h2>API Modules</h2>
 * <ul>
 *   <li><strong>api.economy:</strong> {@link de.rolandsw.schedulemc.api.economy} - Economy transaction APIs</li>
 *   <li><strong>api.plot:</strong> {@link de.rolandsw.schedulemc.api.plot} - Plot management APIs</li>
 *   <li><strong>api.npc:</strong> {@link de.rolandsw.schedulemc.api.npc} - NPC control APIs</li>
 *   <li><strong>api.production:</strong> {@link de.rolandsw.schedulemc.api.production} - Production system APIs</li>
 *   <li><strong>api.achievement:</strong> {@link de.rolandsw.schedulemc.api.achievement} - Achievement APIs</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Stable:</strong> API contracts remain backward compatible</li>
 *   <li><strong>Documented:</strong> Comprehensive JavaDoc for all public methods</li>
 *   <li><strong>Type-Safe:</strong> Strong typing with generics where appropriate</li>
 *   <li><strong>Thread-Safe:</strong> All APIs safe for concurrent access</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.api.economy
 * @see de.rolandsw.schedulemc.api.plot
 */
package de.rolandsw.schedulemc.api;
