/**
 * Production configuration and registry for defining custom production chains.
 *
 * <h2>Overview</h2>
 * <p>This package provides configuration and registration for production types, including
 * plants, drugs, and custom production chains. Production types are defined declaratively
 * and registered at mod initialization.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.production.config.ProductionRegistry}:</strong> Central production type registry</li>
 *   <li><strong>ProductionType:</strong> Configuration for growth time, yields, requirements</li>
 *   <li><strong>ProductionRecipe:</strong> Multi-step processing recipes</li>
 * </ul>
 *
 * <h2>Production Type Definition</h2>
 * <pre>{@code
 * ProductionType cannabis = ProductionType.builder()
 *     .id("cannabis")
 *     .growthTime(7200) // 6 Minecraft hours
 *     .waterRequired(10) // 10L per growth stage
 *     .lightRequired(8) // Light level 8+
 *     .yieldSmall(5) // 5 items for small plant
 *     .yieldMedium(10) // 10 items for medium
 *     .yieldLarge(20) // 20 items for large
 *     .build();
 *
 * ProductionRegistry.register(cannabis);
 * }</pre>
 *
 * <h2>Registered Production Types</h2>
 * <ul>
 *   <li><strong>Plants:</strong> Cannabis, Coca, Tobacco, Poppy, Mushrooms</li>
 *   <li><strong>Drugs:</strong> LSD, Meth, MDMA (processed from plants)</li>
 *   <li><strong>Custom:</strong> Configurable via data-driven approach</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.production.config.ProductionRegistry
 * @see de.rolandsw.schedulemc.production.GenericProductionSystem
 */
package de.rolandsw.schedulemc.production.config;
