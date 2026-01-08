/**
 * Economy event system for transaction hooks, balance changes, and economic state changes.
 *
 * <h2>Overview</h2>
 * <p>This package provides event-driven hooks into the economy system, allowing other
 * systems to react to transactions, balance changes, loans, and economic state changes.</p>
 *
 * <h2>Event Types</h2>
 * <ul>
 *   <li><strong>TransactionEvent:</strong> Fired before/after money transfers</li>
 *   <li><strong>BalanceChangeEvent:</strong> Fired when player balance changes</li>
 *   <li><strong>LoanEvent:</strong> Fired on loan creation, payment, default</li>
 *   <li><strong>EconomicEvent:</strong> {@link de.rolandsw.schedulemc.economy.EconomicEvent} - Market events, inflation</li>
 * </ul>
 *
 * <h2>Event Flow</h2>
 * <pre>
 * EconomyManager.transfer(from, to, amount)
 *     ↓
 * TransactionEvent.Pre (cancellable)
 *     ↓
 * Balance deduction + addition
 *     ↓
 * TransactionEvent.Post
 *     ↓
 * BalanceChangeEvent (both players)
 * </pre>
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><strong>Achievement Triggers:</strong> Track when player reaches wealth milestones</li>
 *   <li><strong>Statistics:</strong> Record transaction volume and patterns</li>
 *   <li><strong>Anti-Fraud:</strong> Detect suspicious transaction patterns</li>
 *   <li><strong>Notifications:</strong> Alert players of large balance changes</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * @SubscribeEvent
 * public void onTransaction(TransactionEvent.Post event) {
 *     if (event.getAmount() > 10000) {
 *         // Log large transaction
 *         AuditLog.log("Large transfer: " + event.getAmount());
 *     }
 * }
 * }</pre>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.economy.EconomicEvent
 * @see de.rolandsw.schedulemc.economy.EconomyManager
 */
package de.rolandsw.schedulemc.economy.events;
