/**
 * Complete economy system for ScheduleMC
 *
 * <h2>Overview</h2>
 * This package implements a comprehensive server economy with banking, transactions,
 * loans, savings, taxes, and shop management.
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link de.rolandsw.schedulemc.economy.EconomyManager} -
 *       Central economy system managing all accounts and transactions</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.WalletManager} -
 *       Physical cash item management</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.TransactionHistory} -
 *       Complete audit log of all transactions</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.LoanManager} -
 *       Interest-bearing loans with repayment schedules</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.SavingsAccountManager} -
 *       Locked savings accounts with interest (5% weekly default)</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.OverdraftManager} -
 *       Negative balance protection with penalty interest</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.RecurringPaymentManager} -
 *       Automatic scheduled payments</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.TaxManager} -
 *       Property tax and sales tax collection</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.InterestManager} -
 *       Daily interest calculation and application</li>
 *   <li>{@link de.rolandsw.schedulemc.economy.ShopAccountManager} -
 *       Per-shop revenue tracking and dividends</li>
 * </ul>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Player accounts with balance tracking</li>
 *   <li>Thread-safe concurrent operations</li>
 *   <li>Complete transaction history</li>
 *   <li>Interest-bearing loans and savings</li>
 *   <li>Overdraft protection with penalties</li>
 *   <li>Recurring automated payments</li>
 *   <li>Property and sales taxes</li>
 *   <li>Shop investment with weekly dividends</li>
 *   <li>State account for government operations</li>
 *   <li>Physical cash vs bank balance separation</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * The economy system is highly configurable with 30+ config options including:
 * <ul>
 *   <li>Start balance (default: 1,000€)</li>
 *   <li>Interest rates (savings, loans, overdraft)</li>
 *   <li>Tax rates (property, sales)</li>
 *   <li>Transaction limits</li>
 *   <li>Save intervals</li>
 * </ul>
 *
 * @see de.rolandsw.schedulemc.commands.MoneyCommand
 * @see de.rolandsw.schedulemc.commands.LoanCommand
 * @see de.rolandsw.schedulemc.commands.SavingsCommand
 * @since 1.0.0
 */
package de.rolandsw.schedulemc.economy;
