/**
 * Vehicle system with custom rideable entities, spawning, and persistence.
 *
 * <h2>Overview</h2>
 * <p>This package provides a vehicle system with custom entity types that players can
 * ride, store, and spawn. Vehicles support fuel consumption, damage, and ownership tracking.</p>
 *
 * <h2>Vehicle Types</h2>
 * <ul>
 *   <li>Cars (various models)</li>
 *   <li>Motorcycles</li>
 *   <li>Boats</li>
 *   <li>Custom vehicles (configurable)</li>
 * </ul>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>EntityGenericVehicle:</strong> Base vehicle entity (1,008 LOC)</li>
 *   <li><strong>VehicleSpawnRegistry:</strong> Vehicle type registration</li>
 *   <li><strong>Vehicle Commands:</strong> Spawn and management commands</li>
 * </ul>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Ownership tracking with player UUIDs</li>
 *   <li>Fuel consumption system</li>
 *   <li>Damage and health tracking</li>
 *   <li>Custom models and textures</li>
 *   <li>Persistent storage (NBT)</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry
 */
package de.rolandsw.schedulemc.vehicle;
