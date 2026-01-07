/**
 * Achievement and milestone tracking system for player progression and rewards.
 *
 * <h2>Overview</h2>
 * <p>This package implements a comprehensive achievement system that tracks player milestones,
 * rewards completion, and provides notifications. Achievements are data-driven, persistent,
 * and integrated with the economy and statistics systems.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>{@link de.rolandsw.schedulemc.achievement.AchievementManager}:</strong> Central achievement tracking</li>
 *   <li><strong>Achievement Registry:</strong> 30+ predefined achievements</li>
 *   <li><strong>Progress Tracking:</strong> Incremental progress for multi-step achievements</li>
 *   <li><strong>Rewards:</strong> Money, items, or special unlocks</li>
 * </ul>
 *
 * <h2>Achievement Categories</h2>
 * <ul>
 *   <li><strong>Economy:</strong> Earn first €100, €1000, €10000, etc.</li>
 *   <li><strong>Production:</strong> Grow first plant, process 100 items, etc.</li>
 *   <li><strong>Plot:</strong> Claim first plot, own 5 plots, etc.</li>
 *   <li><strong>Social:</strong> Trust 10 players, rent apartment, etc.</li>
 *   <li><strong>Crime:</strong> First arrest, escape prison, etc.</li>
 * </ul>
 *
 * <h2>Achievement Definition Example</h2>
 * <pre>{@code
 * Achievement firstEarning = Achievement.builder()
 *     .id("first_earning")
 *     .title("Erstes Geld verdient")
 *     .description("Verdiene deine ersten 100€")
 *     .category(Category.ECONOMY)
 *     .requirement(earnings >= 100.0)
 *     .reward(Money.of(50.0))
 *     .build();
 * }</pre>
 *
 * <h2>Integration Points</h2>
 * <ul>
 *   <li><strong>Economy:</strong> Transaction hooks trigger wealth achievements</li>
 *   <li><strong>Production:</strong> Harvest hooks trigger production achievements</li>
 *   <li><strong>Plot:</strong> Claim hooks trigger plot achievements</li>
 *   <li><strong>Notifications:</strong> {@link de.rolandsw.schedulemc.messaging.MessageManager} displays achievement toasts</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.achievement.AchievementManager
 * @see de.rolandsw.schedulemc.messaging.MessageManager
 */
package de.rolandsw.schedulemc.achievement;
