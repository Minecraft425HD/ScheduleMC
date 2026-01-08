package de.rolandsw.schedulemc.market;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Market Command - /market
 *
 * Commands:
 * - /market prices - Zeigt aktuelle Marktpreise
 * - /market trends - Zeigt Trends
 * - /market stats - Zeigt Statistiken
 */
public class MarketCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("market")
                .then(Commands.literal("prices")
                    .executes(MarketCommand::showPrices))
                .then(Commands.literal("trends")
                    .executes(MarketCommand::showTrends))
                .then(Commands.literal("stats")
                    .executes(MarketCommand::showStats))
                .then(Commands.literal("top")
                    .executes(MarketCommand::showTopPrices))
        );
    }

    /**
     * /market prices - Zeigt alle Preise
     */
    private static int showPrices(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        DynamicMarketManager manager = DynamicMarketManager.getInstance();

        if (!manager.isEnabled()) {
            source.sendFailure(Component.translatable("command.market.disabled"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("command.market.prices_header"), false);

        int count = 0;
        for (MarketData data : manager.getAllMarketData()) {
            MarketData.PriceTrend trend = data.getPriceTrend();

            source.sendSuccess(() -> Component.literal(String.format(
                "§f%s: §6%.2f€ %s%s §7(Base: %.2f€)",
                data.getItemName(),
                data.getCurrentPrice(),
                trend.getColorCode(),
                trend.getSymbol(),
                data.getBasePrice()
            )), false);

            count++;
        }

        final int totalCount = count;
        source.sendSuccess(() -> Component.translatable("command.market.total_items", totalCount), false);

        return 1;
    }

    /**
     * /market trends - Zeigt Trends
     */
    private static int showTrends(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        DynamicMarketManager manager = DynamicMarketManager.getInstance();

        if (!manager.isEnabled()) {
            source.sendFailure(Component.translatable("command.market.disabled"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("command.market.trends_header"), false);

        // Top 5 Steigend
        source.sendSuccess(() -> Component.translatable("command.market.rising"), false);
        List<MarketData> rising = manager.getTrendingUpItems(5);
        for (MarketData data : rising) {
            source.sendSuccess(() -> Component.literal(String.format(
                "  §f%s: §a+%.1f%% §7(%.2f€)",
                data.getItemName(),
                data.getPriceChangePercent(),
                data.getCurrentPrice()
            )), false);
        }

        // Top 5 Fallend
        source.sendSuccess(() -> Component.translatable("command.market.falling"), false);
        List<MarketData> falling = manager.getTrendingDownItems(5);
        for (MarketData data : falling) {
            source.sendSuccess(() -> Component.literal(String.format(
                "  §f%s: §c%.1f%% §7(%.2f€)",
                data.getItemName(),
                data.getPriceChangePercent(),
                data.getCurrentPrice()
            )), false);
        }

        return 1;
    }

    /**
     * /market stats - Zeigt Statistiken
     */
    private static int showStats(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        DynamicMarketManager manager = DynamicMarketManager.getInstance();

        if (!manager.isEnabled()) {
            source.sendFailure(Component.translatable("command.market.disabled"));
            return 0;
        }

        DynamicMarketManager.MarketStatistics stats = manager.getStatistics();

        source.sendSuccess(() -> Component.translatable("command.market.stats_header"), false);
        source.sendSuccess(() -> Component.translatable("command.market.registered_items", stats.totalItems()), false);
        source.sendSuccess(() -> Component.translatable("command.market.rising_prices", stats.risingCount()), false);
        source.sendSuccess(() -> Component.translatable("command.market.falling_prices", stats.fallingCount()), false);
        source.sendSuccess(() -> Component.translatable("command.market.stable_prices", stats.stableCount()), false);
        source.sendSuccess(() -> Component.translatable("command.market.average_price", String.format("%.2f", stats.averagePrice())), false);
        source.sendSuccess(() -> Component.translatable("command.market.average_multiplier", String.format("%.2f", stats.averageMultiplier())), false);
        source.sendSuccess(() -> Component.translatable("command.market.total_updates", stats.totalUpdates()), false);

        return 1;
    }

    /**
     * /market top - Zeigt Top-Preise
     */
    private static int showTopPrices(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        DynamicMarketManager manager = DynamicMarketManager.getInstance();

        if (!manager.isEnabled()) {
            source.sendFailure(Component.translatable("command.market.disabled"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("command.market.top_header"), false);

        List<MarketData> top = manager.getTopPricedItems(10);
        for (int i = 0; i < top.size(); i++) {
            MarketData data = top.get(i);
            final int rank = i + 1;

            source.sendSuccess(() -> Component.literal(String.format(
                "§7%d. §f%s: §6%.2f€ §7(×%.2fx)",
                rank,
                data.getItemName(),
                data.getCurrentPrice(),
                data.getPriceMultiplier()
            )), false);
        }

        return 1;
    }
}
