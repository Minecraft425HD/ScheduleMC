/**
 * Client-side GUI screens, apps, and rendering for player interfaces.
 *
 * <h2>Overview</h2>
 * <p>This package contains all client-side rendering code including smartphone apps,
 * settings screens, map views, and plot management UIs. Screens follow Minecraft's
 * GuiGraphics API and support scrolling, buttons, and interactive elements.</p>
 *
 * <h2>Major Screen Components</h2>
 * <ul>
 *   <li><strong>Smartphone Apps:</strong> Finance, Plot Management, Settings, Warehouse</li>
 *   <li><strong>Map Views:</strong> World map, plot boundaries, territory visualization</li>
 *   <li><strong>Plot Screens:</strong> Plot info, apartment management, utility stats</li>
 *   <li><strong>Warehouse:</strong> Inventory management, sales, statistics</li>
 * </ul>
 *
 * <h2>Settings App Refactoring</h2>
 * <p>{@link de.rolandsw.schedulemc.client.screen.apps.SettingsAppScreen} was extensively refactored:</p>
 * <ul>
 *   <li>renderPlotSettingsTab: 286 → 73 LOC (5 helpers, -74.5%)</li>
 *   <li>renderNotificationsTab: 143 → 32 LOC (3 helpers, -77.6%)</li>
 *   <li>renderAccountTab: 137 → 34 LOC (4 helpers, -75.2%)</li>
 *   <li><strong>Total: 566 → 139 LOC (-75.4% reduction, 12 methods extracted)</strong></li>
 * </ul>
 *
 * <h2>Rendering Architecture</h2>
 * <pre>
 * Screen (AbstractScreen)
 *     ↓
 * Render Tabs (renderPlotSettings, renderNotifications, renderAccount)
 *     ↓
 * Helper Methods (renderSaleRentSection, renderThresholdsSection, etc.)
 *     ↓
 * GuiGraphics (drawString, fill, drawTexture)
 * </pre>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Tab-based navigation with scrollable content</li>
 *   <li>Interactive buttons with click regions</li>
 *   <li>Sliders for threshold settings</li>
 *   <li>Real-time data updates from managers</li>
 *   <li>Input dialogs for text/number entry</li>
 *   <li>Confirmation dialogs for destructive actions</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.client.screen.apps.SettingsAppScreen
 * @see de.rolandsw.schedulemc.client.screen.PlotInfoScreen
 * @see de.rolandsw.schedulemc.client.screen.WarehouseScreen
 */
package de.rolandsw.schedulemc.client;
