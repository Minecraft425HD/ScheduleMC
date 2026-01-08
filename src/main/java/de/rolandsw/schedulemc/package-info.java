/**
 * ScheduleMC - Comprehensive Roleplay & Economy Server Mod
 *
 * <h2>Overview</h2>
 * ScheduleMC is a large-scale Minecraft Forge mod for Minecraft 1.20.1 that provides
 * a complete server economy and roleplay system with NPCs, production chains, and police mechanics.
 *
 * <h2>Main Features</h2>
 * <ul>
 *   <li><b>Economy System</b>: Banking, loans, savings, taxes, investments</li>
 *   <li><b>Plot Management</b>: Land claiming with spatial indexing</li>
 *   <li><b>NPCs</b>: Intelligent NPCs with schedules, personalities, and shops</li>
 *   <li><b>Production Chains</b>: 8 different drug production systems</li>
 *   <li><b>Police & Crime</b>: Wanted levels, chases, arrests, raids</li>
 *   <li><b>Vehicles</b>: Drivable vehicles with fuel and garage systems</li>
 *   <li><b>Warehouses</b>: Inventory management with automatic deliveries</li>
 * </ul>
 *
 * <h2>Core Packages</h2>
 * <ul>
 *   <li>{@link de.rolandsw.schedulemc.economy} - Complete economy system</li>
 *   <li>{@link de.rolandsw.schedulemc.region} - Plot management and spatial indexing</li>
 *   <li>{@link de.rolandsw.schedulemc.npc} - NPC entities, AI, and shops</li>
 *   <li>{@link de.rolandsw.schedulemc.production} - Production chain systems</li>
 *   <li>{@link de.rolandsw.schedulemc.util} - Core utilities and patterns</li>
 *   <li>{@link de.rolandsw.schedulemc.commands} - Server commands</li>
 *   <li>{@link de.rolandsw.schedulemc.config} - Configuration system</li>
 * </ul>
 *
 * <h2>Design Patterns Used</h2>
 * <ul>
 *   <li><b>Template Method</b>: AbstractPersistenceManager, AbstractPackagingTableBlockEntity</li>
 *   <li><b>Strategy</b>: PlantSerializer family</li>
 *   <li><b>Functional Interface</b>: CommandExecutor, PacketHandler, EventHelper</li>
 *   <li><b>Singleton</b>: EconomyManager, PlotManager</li>
 * </ul>
 *
 * @author RolandSW
 * @version 2.5.0-beta
 * @since 1.0.0
 */
package de.rolandsw.schedulemc;
