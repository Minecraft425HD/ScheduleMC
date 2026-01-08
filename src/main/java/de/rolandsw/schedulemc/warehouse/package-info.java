/**
 * Warehouse system for bulk item storage, sales tracking, and production statistics.
 *
 * <h2>Overview</h2>
 * <p>This package provides a warehouse management system where players can store large
 * quantities of items, track sales history, view statistics, and manage automated sales.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>WarehouseManager:</strong> Central warehouse data management</li>
 *   <li><strong>WarehouseScreen:</strong> GUI for warehouse interaction (refactored)</li>
 *   <li><strong>WarehouseBlockEntity:</strong> Physical warehouse block entity</li>
 *   <li><strong>Sales Tracking:</strong> Historical sales data and revenue analytics</li>
 * </ul>
 *
 * <h2>Warehouse Screen Refactoring</h2>
 * <p>WarehouseScreen.renderStatsTab() was refactored with Extract-Method pattern:</p>
 * <ul>
 *   <li>Original: 152 LOC</li>
 *   <li>Refactored: 45 LOC</li>
 *   <li>Reduction: -107 LOC (-70.4%)</li>
 *   <li>4 helper methods extracted for statistics sections</li>
 * </ul>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Bulk item storage (unlimited capacity per plot)</li>
 *   <li>Sales tracking with timestamps and revenue</li>
 *   <li>Statistics: Total sales, revenue, average price, best-selling items</li>
 *   <li>Integration with economy system for automated sales</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.warehouse.WarehouseManager
 * @see de.rolandsw.schedulemc.client.screen.WarehouseScreen
 */
package de.rolandsw.schedulemc.warehouse;
