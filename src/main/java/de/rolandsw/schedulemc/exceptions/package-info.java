/**
 * Custom exception hierarchy for ScheduleMC domain-specific error handling.
 *
 * <h2>Overview</h2>
 * <p>This package provides a comprehensive exception system that replaces generic RuntimeExceptions
 * with domain-specific exceptions for better error handling, debugging, and user feedback.</p>
 *
 * <h2>Exception Hierarchy</h2>
 * <pre>
 * {@link de.rolandsw.schedulemc.exceptions.ScheduleMCException} (base)
 *     ├── {@link de.rolandsw.schedulemc.exceptions.ValidationException} - Input validation errors
 *     ├── {@link de.rolandsw.schedulemc.exceptions.PlotException} - Plot management errors
 *     ├── {@link de.rolandsw.schedulemc.exceptions.EconomyException} - Economy/transaction errors
 *     ├── {@link de.rolandsw.schedulemc.exceptions.ProductionException} - Production system errors
 *     ├── {@link de.rolandsw.schedulemc.exceptions.NPCException} - NPC management errors
 *     ├── {@link de.rolandsw.schedulemc.exceptions.NetworkException} - Network/packet errors
 *     ├── {@link de.rolandsw.schedulemc.exceptions.PersistenceException} - Data persistence errors
 *     └── {@link de.rolandsw.schedulemc.exceptions.CrimeException} - Crime system errors
 * </pre>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Fail-Fast:</strong> Exceptions are thrown immediately when invalid state is detected</li>
 *   <li><strong>Context-Rich:</strong> Exceptions carry domain-specific context (field names, values)</li>
 *   <li><strong>Type-Safe:</strong> Specific exception types enable precise catch blocks</li>
 *   <li><strong>Localized:</strong> All exception messages are in German for end-user clarity</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Validation Errors</h3>
 * <pre>{@code
 * // Throw with context
 * throw new ValidationException("Ungültiger Name", "plotName", invalidName);
 *
 * // Catch and handle
 * try {
 *     PlotManager.createPlot(name);
 * } catch (ValidationException e) {
 *     System.err.println("Field: " + e.getFieldName());
 *     System.err.println("Value: " + e.getInvalidValue());
 * }
 * }</pre>
 *
 * <h3>Economy Errors</h3>
 * <pre>{@code
 * // Insufficient funds
 * if (balance < amount) {
 *     throw new EconomyException("Unzureichendes Guthaben", player, balance, amount);
 * }
 *
 * // Transaction failure with rollback
 * try {
 *     EconomyManager.transfer(from, to, amount);
 * } catch (EconomyException e) {
 *     // Rollback already performed
 *     sendMessage("Transaktion fehlgeschlagen: " + e.getMessage());
 * }
 * }</pre>
 *
 * <h3>Plot Management Errors</h3>
 * <pre>{@code
 * // Plot not found
 * PlotRegion plot = PlotManager.getPlotById(plotId)
 *     .orElseThrow(() -> new PlotException("Plot nicht gefunden", plotId));
 *
 * // Permission denied
 * if (!plot.isOwner(player)) {
 *     throw new PlotException("Keine Berechtigung", plotId, player.getUUID());
 * }
 * }</pre>
 *
 * <h2>Integration Points</h2>
 * <ul>
 *   <li><strong>InputValidation:</strong> Uses {@link ValidationException} for input sanitization</li>
 *   <li><strong>EconomyManager:</strong> Uses {@link EconomyException} for transaction safety</li>
 *   <li><strong>PlotManager:</strong> Uses {@link PlotException} for region management</li>
 *   <li><strong>Production System:</strong> Uses {@link ProductionException} for production errors</li>
 *   <li><strong>Crime System:</strong> Uses {@link CrimeException} for crime/bounty errors</li>
 * </ul>
 *
 * <h2>Testing</h2>
 * <p>All exception classes have comprehensive unit tests in {@link de.rolandsw.schedulemc.exceptions.ExceptionTest}
 * covering construction, message formatting, context fields, and serialization.</p>
 *
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>Exceptions are used for <em>exceptional</em> cases only, not control flow</li>
 *   <li>Exception messages are pre-formatted to avoid string concatenation overhead</li>
 *   <li>Stack traces are preserved for debugging while keeping messages user-friendly</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.exceptions.ScheduleMCException
 * @see de.rolandsw.schedulemc.util.InputValidation
 * @see de.rolandsw.schedulemc.economy.EconomyManager
 * @see de.rolandsw.schedulemc.region.PlotManager
 */
package de.rolandsw.schedulemc.exceptions;
