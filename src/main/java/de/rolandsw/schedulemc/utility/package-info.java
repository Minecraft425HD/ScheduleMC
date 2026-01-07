/**
 * Utility consumption tracking for electricity and water usage per plot.
 *
 * <h2>Overview</h2>
 * <p>This package tracks electricity and water consumption for plots, calculates bills,
 * and triggers warnings when thresholds are exceeded. Consumption is tied to production
 * activities and affects economy.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>PlotUtilityManager:</strong> Track consumption per plot</li>
 *   <li><strong>UtilityRegistry:</strong> Block consumption rates</li>
 *   <li><strong>Billing:</strong> Calculate daily/weekly/monthly costs</li>
 *   <li><strong>Warnings:</strong> Alert players when consumption is high</li>
 * </ul>
 *
 * <h2>Consumption Tracking</h2>
 * <pre>
 * Production Block Active → Consumption Logged
 *     ↓
 * 7-Day Average Calculated
 *     ↓
 * Billing Cost = (kWh × 0.35€) + (L × 0.005€)
 *     ↓
 * Warning if >200 kWh or >1000 L
 * </pre>
 *
 * @since 1.0
 */
package de.rolandsw.schedulemc.utility;
