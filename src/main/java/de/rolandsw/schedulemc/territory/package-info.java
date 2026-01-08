/**
 * Territory control system for gang-based area ownership and conflict.
 *
 * <h2>Overview</h2>
 * <p>This package implements a territory control system where groups (gangs, factions)
 * can claim areas, defend them, and compete for control. Territories provide benefits
 * like reduced taxes, safe zones, and revenue from activities within the area.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>TerritoryManager:</strong> Territory claim and conflict resolution</li>
 *   <li><strong>Territory Data:</strong> Ownership, boundaries, benefits</li>
 *   <li><strong>Conflict System:</strong> Territory takeover mechanics</li>
 * </ul>
 *
 * <h2>Territory Mechanics</h2>
 * <ul>
 *   <li>Area-based ownership (rectangular regions)</li>
 *   <li>Group/gang affiliation required</li>
 *   <li>Benefits: Tax reduction, safe zones, revenue sharing</li>
 *   <li>Conflict resolution through PvP or timer-based takeover</li>
 * </ul>
 *
 * <h2>Integration</h2>
 * <ul>
 *   <li><strong>Plot System:</strong> Territories can contain multiple plots</li>
 *   <li><strong>Economy:</strong> Territory ownership affects taxes and revenue</li>
 *   <li><strong>Crime System:</strong> Territories can be safe zones or lawless areas</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.territory.TerritoryManager
 */
package de.rolandsw.schedulemc.territory;
