/**
 * Smartphone app screens for Finance, Plot Management, Settings, and Warehouse.
 *
 * <h2>Overview</h2>
 * <p>This package contains smartphone app implementations that provide mobile-style interfaces
 * for managing plots, viewing finances, adjusting settings, and accessing warehouse stats.</p>
 *
 * <h2>Major Apps</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.client.screen.apps.SettingsAppScreen}:</strong> Settings (1,071 LOC, refactored)</li>
 *   <li><strong>PlotAppScreen:</strong> Plot management with finance tab (180 LOC)</li>
 *   <li><strong>FinanceAppScreen:</strong> Transaction history and balance</li>
 *   <li><strong>WarehouseAppScreen:</strong> Warehouse statistics</li>
 * </ul>
 *
 * <h2>SettingsAppScreen Refactoring</h2>
 * <p>Extensively refactored with Extract-Method pattern:</p>
 * <ul>
 *   <li>renderPlotSettingsTab: 286 → 73 LOC (5 helpers, -74.5%)</li>
 *   <li>renderNotificationsTab: 143 → 32 LOC (3 helpers, -77.6%)</li>
 *   <li>renderAccountTab: 137 → 34 LOC (4 helpers, -75.2%)</li>
 *   <li><strong>Total: 566 → 139 LOC (-75.4%, 12 methods extracted)</strong></li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.client.screen.apps.SettingsAppScreen
 */
package de.rolandsw.schedulemc.client.screen.apps;
