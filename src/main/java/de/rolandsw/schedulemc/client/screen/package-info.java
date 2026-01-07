/**
 * GUI screen implementations for plot management, maps, warehouse, and world interactions.
 *
 * <h2>Overview</h2>
 * <p>This package contains all client-side screen implementations that provide player
 * interfaces for interacting with plots, maps, warehouses, and other game systems.</p>
 *
 * <h2>Screen Categories</h2>
 * <ul>
 *   <li><strong>Plot Screens:</strong> Plot info, apartment management, utility stats</li>
 *   <li><strong>Map Screens:</strong> World map, plot boundaries, territory visualization</li>
 *   <li><strong>Warehouse Screens:</strong> Inventory management, sales tracking</li>
 *   <li><strong>App Screens:</strong> Smartphone apps (Finance, Settings, Plot Management)</li>
 * </ul>
 *
 * <h2>Major Screens</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.client.screen.PlotInfoScreen}:</strong> Plot details and management (182 LOC)</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.client.screen.WarehouseScreen}:</strong> Warehouse UI (1,428 LOC, refactored)</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.client.screen.WorldMapScreen}:</strong> Interactive world map (1,209 LOC)</li>
 *   <li><strong>PlotAppScreen:</strong> Plot management app with finance tab (180 LOC)</li>
 * </ul>
 *
 * <h2>Refactoring Achievements</h2>
 * <p>WarehouseScreen.renderStatsTab() was refactored:</p>
 * <ul>
 *   <li>Original: 152 LOC</li>
 *   <li>Refactored: 45 LOC</li>
 *   <li>Reduction: -107 LOC (-70.4%)</li>
 *   <li>4 helper methods extracted</li>
 * </ul>
 *
 * <h2>Screen Architecture</h2>
 * <pre>
 * AbstractScreen (Minecraft)
 *     ↓
 * Custom Screen (PlotInfoScreen, WarehouseScreen, etc.)
 *     ↓
 * render() → renderTabs() → renderSections()
 *     ↓
 * GuiGraphics API (drawString, fill, drawTexture)
 * </pre>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.client.screen.PlotInfoScreen
 * @see de.rolandsw.schedulemc.client.screen.WarehouseScreen
 * @see de.rolandsw.schedulemc.client.screen.apps
 */
package de.rolandsw.schedulemc.client.screen;
