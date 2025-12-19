/**
 * Plot management system with spatial indexing
 *
 * <h2>Overview</h2>
 * This package implements a high-performance plot management system with
 * spatial indexing for fast position queries.
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link de.rolandsw.schedulemc.region.PlotManager} -
 *       Central plot management with caching and spatial indexing</li>
 *   <li>{@link de.rolandsw.schedulemc.region.PlotRegion} -
 *       Represents a claimed plot with owner and permissions</li>
 *   <li>{@link de.rolandsw.schedulemc.region.PlotSpatialIndex} -
 *       Chunk-based spatial indexing for O(1) lookups</li>
 *   <li>{@link de.rolandsw.schedulemc.region.PlotCache} -
 *       LRU cache for frequently accessed plots</li>
 *   <li>{@link de.rolandsw.schedulemc.region.PlotArea} -
 *       Geometric calculations for plot areas</li>
 *   <li>{@link de.rolandsw.schedulemc.region.PlotType} -
 *       Plot types (RESIDENCE, BUSINESS, APARTMENT, etc.)</li>
 * </ul>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Buy, sell, and rent plots</li>
 *   <li>Apartment sub-rental with deposits</li>
 *   <li>Trusted player system (max 10 per plot)</li>
 *   <li>5-star rating system</li>
 *   <li>Block protection handlers</li>
 *   <li>Warehouse linking</li>
 *   <li>Utility connections</li>
 * </ul>
 *
 * <h2>Performance Optimizations</h2>
 * <ul>
 *   <li><b>Spatial Indexing</b>: Uses chunk-based indexing (16x16x16) to reduce
 *       plot lookup from O(n) to O(1) in most cases</li>
 *   <li><b>LRU Caching</b>: Frequently accessed plots are cached in memory</li>
 *   <li><b>Dirty Flag Tracking</b>: Only modified data is saved</li>
 *   <li><b>Atomic Writes</b>: Prevents data corruption during saves</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * Plots are configurable with:
 * <ul>
 *   <li>Size limits: 64 - 1,000,000 blocks</li>
 *   <li>Price limits: 1€ - 1,000,000€</li>
 *   <li>Max trusted players: 10</li>
 *   <li>Refund on abandon: 50%</li>
 * </ul>
 *
 * @see de.rolandsw.schedulemc.commands.PlotCommand
 * @since 1.0.0
 */
package de.rolandsw.schedulemc.region;
