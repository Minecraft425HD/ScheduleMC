/**
 * Generic production system for plants, drugs, and custom production chains with NBT persistence.
 *
 * <h2>Overview</h2>
 * <p>This package provides a flexible, data-driven production system that handles growing plants,
 * processing materials, and managing multi-stage production chains. All production data is
 * persisted using NBT and supports custom growth requirements, yields, and processing times.</p>
 *
 * <h2>Production Types</h2>
 * <ul>
 *   <li><strong>Plant Production:</strong> Cannabis, coca, tobacco, poppy, mushrooms</li>
 *   <li><strong>Drug Processing:</strong> LSD, meth, MDMA from raw materials</li>
 *   <li><strong>Generic Chains:</strong> Configurable multi-step production processes</li>
 * </ul>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.production.GenericProductionSystem}:</strong> Main production logic</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.production.config.ProductionRegistry}:</strong> Production type registration</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.production.nbt}:</strong> NBT serialization for plant data</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.production.ProductionSize}:</strong> Size-based yields</li>
 * </ul>
 *
 * <h2>Production Flow</h2>
 * <pre>
 * Planting → Growth (tick-based) → Harvesting → Processing → Final Product
 *    ↓           ↓                      ↓            ↓             ↓
 *  NBT Save   Light/Water Check    Yield Calc   Timer-based   Economy Sale
 * </pre>
 *
 * <h2>Data Model</h2>
 * <pre>{@code
 * PlantData (NBT):
 *   - plantType: String
 *   - plantedTime: long
 *   - size: ProductionSize (SMALL, MEDIUM, LARGE)
 *   - waterLevel: int
 *   - lightLevel: int
 *   - growthStage: int
 * }</pre>
 *
 * <h2>Integration Points</h2>
 * <ul>
 *   <li><strong>Economy:</strong> Production sells to {@link de.rolandsw.schedulemc.economy.EconomyManager}</li>
 *   <li><strong>Utility:</strong> Water/electricity consumption tracking</li>
 *   <li><strong>Plot:</strong> Production is tied to plot ownership</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.production.GenericProductionSystem
 * @see de.rolandsw.schedulemc.production.config.ProductionRegistry
 */
package de.rolandsw.schedulemc.production;
