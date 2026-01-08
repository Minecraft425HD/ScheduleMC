/**
 * Command system for player interactions with plots, NPCs, economy, and game features.
 *
 * <h2>Overview</h2>
 * <p>This package provides a comprehensive command interface for all major ScheduleMC systems.
 * Commands are registered with Forge's command system and use {@link de.rolandsw.schedulemc.commands.CommandExecutor}
 * for consistent error handling and permission checks.</p>
 *
 * <h2>Core Commands</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.commands.PlotCommand}</strong> - Plot management (create, claim, trust, apartments, rent)</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.commands.NPCCommand}</strong> - NPC management (spawn, schedule, info, remove)</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.commands.EconomyCommand}</strong> - Economy operations (balance, pay, stats)</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.commands.ProductionCommand}</strong> - Production management (info, list)</li>
 *   <li><strong>{@link de.rolandsw.schedulemc.commands.VehicleCommand}</strong> - Vehicle spawning and management</li>
 * </ul>
 *
 * <h2>Command Architecture</h2>
 * <pre>
 * Command Registration (ModCommands)
 *     ↓
 * CommandExecutor (error handling, validation)
 *     ↓
 * Manager Layer (PlotManager, EconomyManager, etc.)
 *     ↓
 * Persistence (save/load)
 * </pre>
 *
 * <h2>Error Handling</h2>
 * <p>All commands use {@link de.rolandsw.schedulemc.commands.CommandExecutor} which provides:</p>
 * <ul>
 *   <li>Automatic exception catching and user-friendly error messages</li>
 *   <li>Permission validation before execution</li>
 *   <li>Transaction rollback on failures</li>
 *   <li>Consistent feedback formatting (success = green, error = red)</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Register command
 * Commands.literal("plot")
 *     .then(Commands.literal("claim")
 *         .executes(PlotCommand::claimPlot))
 *     .register(dispatcher);
 *
 * // Execute with error handling
 * return CommandExecutor.executePlayerCommand(ctx, "Plot claim failed",
 *     player -> {
 *         PlotRegion plot = PlotManager.claimPlot(player, area);
 *         sendSuccess(player, "Plot claimed: " + plot.getPlotId());
 *     });
 * }</pre>
 *
 * <h2>Refactoring Notes</h2>
 * <p>PlotCommand has been extensively refactored with Extract-Method pattern:</p>
 * <ul>
 *   <li>createApartment: 89 → 57 LOC (-36%)</li>
 *   <li>rentApartmentDays: 76 → 49 LOC (-35%)</li>
 *   <li>9 helper methods extracted for validation and bounds checking</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.commands.CommandExecutor
 * @see de.rolandsw.schedulemc.commands.PlotCommand
 * @see de.rolandsw.schedulemc.commands.NPCCommand
 */
package de.rolandsw.schedulemc.commands;
