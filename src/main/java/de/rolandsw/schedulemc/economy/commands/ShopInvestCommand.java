package de.rolandsw.schedulemc.economy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.ShopAccount;
import de.rolandsw.schedulemc.economy.ShopAccountManager;
import de.rolandsw.schedulemc.economy.ShareHolder;
import de.rolandsw.schedulemc.economy.WalletManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Shop Invest Command - Player commands for shop investment system
 *
 * /shopinvest list - Lists all shops
 * /shopinvest info <shopId> - Shows shop info and shareholder details
 * /shopinvest buy <shopId> <shares> - Buy shares in a shop
 * /shopinvest sell <shopId> <shares> - Sell shares
 * /shopinvest myshares - Shows all shares the player owns
 */
public class ShopInvestCommand {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PRICE_PER_SHARE = 1000; // 1000€ per share

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("shopinvest")

                .then(Commands.literal("list")
                    .executes(ShopInvestCommand::listShops)
                )

                .then(Commands.literal("info")
                    .then(Commands.argument("shopId", StringArgumentType.string())
                        .executes(ShopInvestCommand::showInfo)
                    )
                )

                .then(Commands.literal("buy")
                    .then(Commands.argument("shopId", StringArgumentType.string())
                        .then(Commands.argument("shares", IntegerArgumentType.integer(1, 99))
                            .executes(ShopInvestCommand::buyShares)
                        )
                    )
                )

                .then(Commands.literal("sell")
                    .then(Commands.argument("shopId", StringArgumentType.string())
                        .then(Commands.argument("shares", IntegerArgumentType.integer(1, 99))
                            .executes(ShopInvestCommand::sellShares)
                        )
                    )
                )

                .then(Commands.literal("myshares")
                    .executes(ShopInvestCommand::showMyShares)
                )
        );
    }

    private static int listShops(CommandContext<CommandSourceStack> ctx) {
        try {
            var shops = ShopAccountManager.getAllAccounts();

            if (shops.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.translatable("command.shop.invest.no_shops"), false);
                return 1;
            }

            Component header = Component.translatable("command.shop.invest.available_shops");
            for (ShopAccount shop : shops) {
                int availableShares = shop.getAvailableShares();
                Component shopLine = Component.translatable("command.shop.invest.shop_list_entry", shop.getShopId(), availableShares);
                header = header.copy().append("\n").append(shopLine);
            }

            final Component finalHeader = header;
            ctx.getSource().sendSuccess(() -> finalHeader, false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop list", e);
            ctx.getSource().sendFailure(Component.translatable("command.shop.invest.list_error"));
            return 0;
        }
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            String shopId = StringArgumentType.getString(ctx, "shopId");
            ShopAccount shop = ShopAccountManager.getAccount(shopId);

            if (shop == null) {
                ctx.getSource().sendFailure(Component.translatable("message.shop.not_found", shopId));
                return 0;
            }

            Component result = Component.translatable("command.shop.invest.info_header", shopId);
            result = result.copy().append("\n").append(Component.translatable("command.shop.invest.available_shares", shop.getAvailableShares()));
            result = result.copy().append("\n").append(Component.translatable("command.shop.invest.net_revenue", shop.get7DayNetRevenue()));
            result = result.copy().append("\n\n");

            if (shop.getShareholders().isEmpty()) {
                result = result.copy().append(Component.translatable("command.shop.invest.no_shareholders"));
            } else {
                result = result.copy().append(Component.translatable("command.shop.invest.shareholders_header"));
                for (var holder : shop.getShareholders()) {
                    String playerName = ctx.getSource().getServer()
                        .getPlayerList()
                        .getPlayer(holder.getPlayerUUID())
                        .getName().getString();
                    double percentage = holder.getOwnershipPercentage();
                    Component shareLine = Component.translatable("command.shop.invest.shareholder_entry", playerName, holder.getSharesOwned(), String.format("%.1f", percentage));
                    result = result.copy().append("\n").append(shareLine);
                }
            }

            final Component finalResult = result;
            ctx.getSource().sendSuccess(() -> finalResult, false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop info", e);
            ctx.getSource().sendFailure(Component.translatable("command.shop.invest.info_error"));
            return 0;
        }
    }

    private static int buyShares(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String shopId = StringArgumentType.getString(ctx, "shopId");
            int shares = IntegerArgumentType.getInteger(ctx, "shares");

            ShopAccount shop = ShopAccountManager.getAccount(shopId);
            if (shop == null) {
                ctx.getSource().sendFailure(Component.translatable("message.shop.not_found", shopId));
                return 0;
            }

            // Check if player already owns shares
            UUID playerId = player.getUUID();
            boolean alreadyInvested = shop.hasShareholder(playerId);

            if (!alreadyInvested && !shop.canAddShareholder()) {
                ctx.getSource().sendFailure(Component.translatable("command.shop.invest.max_shareholders"));
                return 0;
            }

            if (shares > shop.getAvailableShares()) {
                ctx.getSource().sendFailure(Component.translatable("command.shop.invest.insufficient_shares", shop.getAvailableShares()));
                return 0;
            }

            int cost = shares * PRICE_PER_SHARE;
            double balance = WalletManager.getBalance(playerId);
            if (balance < cost) {
                ctx.getSource().sendFailure(Component.translatable("command.shop.invest.insufficient_funds", cost, (int)balance));
                return 0;
            }

            if (shop.purchaseShares(playerId, player.getName().getString(), shares, cost)) {
                WalletManager.removeMoney(playerId, (double)cost);

                double ownership = (shares / 100.0f) * 100;
                ctx.getSource().sendSuccess(() -> Component.translatable("command.shop.invest.shares_purchased", shopId, shares, cost, String.format("%.1f", ownership)), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.translatable("command.shop.invest.purchase_error"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop buy", e);
            ctx.getSource().sendFailure(Component.translatable("command.shop.invest.buy_error"));
            return 0;
        }
    }

    private static int sellShares(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String shopId = StringArgumentType.getString(ctx, "shopId");
            int shares = IntegerArgumentType.getInteger(ctx, "shares");

            ShopAccount shop = ShopAccountManager.getAccount(shopId);
            if (shop == null) {
                ctx.getSource().sendFailure(Component.translatable("message.shop.not_found", shopId));
                return 0;
            }

            UUID playerId = player.getUUID();
            int refund = shop.sellShares(playerId, shares);

            if (refund > 0) {
                WalletManager.addMoney(playerId, (double)refund);

                ctx.getSource().sendSuccess(() -> Component.translatable("command.shop.invest.shares_sold", shopId, shares, refund), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.translatable("command.shop.invest.no_shares_to_sell"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop sell", e);
            ctx.getSource().sendFailure(Component.translatable("command.shop.invest.sell_error"));
            return 0;
        }
    }

    private static int showMyShares(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID playerId = player.getUUID();

            Component result = Component.translatable("command.shop.invest.my_shares");
            boolean hasShares = false;

            for (ShopAccount shop : ShopAccountManager.getAllAccounts()) {
                ShareHolder holder = shop.getShareholder(playerId);

                if (holder != null) {
                    hasShares = true;
                    double percentage = holder.getOwnershipPercentage();
                    Component shareEntry = Component.translatable("command.shop.invest.my_shares_entry",
                        shop.getShopId(),
                        holder.getSharesOwned(),
                        String.format("%.1f", percentage),
                        holder.getPurchasePrice(),
                        shop.get7DayNetRevenue());
                    result = result.copy().append("\n").append(shareEntry);
                }
            }

            if (!hasShares) {
                result = result.copy().append("\n").append(Component.translatable("command.shop.invest.no_shares_owned"));
            }

            final Component finalResult = result;
            ctx.getSource().sendSuccess(() -> finalResult, false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop myshares", e);
            ctx.getSource().sendFailure(Component.translatable("command.shop.invest.myshares_error"));
            return 0;
        }
    }
}
