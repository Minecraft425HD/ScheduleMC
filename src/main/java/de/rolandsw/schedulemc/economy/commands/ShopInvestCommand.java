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
                ctx.getSource().sendSuccess(() -> Component.literal("§eKeine Shops verfügbar."), false);
                return 1;
            }

            StringBuilder sb = new StringBuilder("§e§l=== Verfügbare Shops ===\n");
            for (ShopAccount shop : shops) {
                int availableShares = shop.getAvailableShares();
                sb.append("§7- §e").append(shop.getShopId())
                    .append(" §7(§e").append(availableShares).append("§7 Anteile verfügbar)\n");
            }

            String result = sb.toString();
            ctx.getSource().sendSuccess(() -> Component.literal(result), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop list", e);
            return 0;
        }
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            String shopId = StringArgumentType.getString(ctx, "shopId");
            ShopAccount shop = ShopAccountManager.getAccount(shopId);

            if (shop == null) {
                ctx.getSource().sendFailure(Component.literal("§cShop '§e" + shopId + "§c' nicht gefunden!"));
                return 0;
            }

            StringBuilder sb = new StringBuilder("§e§l=== Shop Info: " + shopId + " ===\n");
            sb.append("§7Verfügbare Anteile: §e").append(shop.getAvailableShares()).append(" §7/ 100\n");
            sb.append("§77-Tage Netto-Umsatz: §e").append(shop.get7DayNetRevenue()).append("€\n\n");

            if (shop.getShareholders().isEmpty()) {
                sb.append("§7Keine Teilhaber");
            } else {
                sb.append("§e§lTeilhaber:\n");
                for (var holder : shop.getShareholders()) {
                    String playerName = ctx.getSource().getServer()
                        .getPlayerList()
                        .getPlayer(holder.getPlayerUUID())
                        .getName().getString();
                    sb.append("§7- §e").append(playerName)
                        .append(" §7(§e").append(holder.getSharesOwned()).append("§7 Anteile, ")
                        .append(String.format("%.1f", holder.getOwnershipPercentage())).append("%)\n");
                }
            }

            String result = sb.toString();
            ctx.getSource().sendSuccess(() -> Component.literal(result), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop info", e);
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
                ctx.getSource().sendFailure(Component.literal("§cShop '§e" + shopId + "§c' nicht gefunden!"));
                return 0;
            }

            // Check if player already owns shares
            UUID playerId = player.getUUID();
            boolean alreadyInvested = shop.hasShareholder(playerId);

            if (!alreadyInvested && !shop.canAddShareholder()) {
                ctx.getSource().sendFailure(Component.literal("§cMaximal 2 Teilhaber pro Shop erlaubt!"));
                return 0;
            }

            if (shares > shop.getAvailableShares()) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Anteile verfügbar!\n" +
                    "§7Verfügbar: §e" + shop.getAvailableShares()
                ));
                return 0;
            }

            int cost = shares * PRICE_PER_SHARE;
            double balance = WalletManager.getBalance(playerId);
            if (balance < cost) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Benötigt: §e" + cost + "€\n" +
                    "§7Kontostand: §e" + (int)balance + "€"
                ));
                return 0;
            }

            if (shop.purchaseShares(playerId, player.getName().getString(), shares, cost)) {
                WalletManager.removeMoney(playerId, (double)cost);

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Anteile gekauft!\n" +
                    "§7Shop: §e" + shopId + "\n" +
                    "§7Anteile: §e" + shares + "\n" +
                    "§7Kosten: §e" + cost + "€\n" +
                    "§7Besitz: §e" + String.format("%.1f", (shares / 100.0f) * 100) + "%"
                ), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cFehler beim Kauf der Anteile!"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop buy", e);
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
                ctx.getSource().sendFailure(Component.literal("§cShop '§e" + shopId + "§c' nicht gefunden!"));
                return 0;
            }

            UUID playerId = player.getUUID();
            int refund = shop.sellShares(playerId, shares);

            if (refund > 0) {
                WalletManager.addMoney(playerId, (double)refund);

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Anteile verkauft!\n" +
                    "§7Shop: §e" + shopId + "\n" +
                    "§7Anteile: §e" + shares + "\n" +
                    "§7Rückerstattung (75%): §e" + refund + "€"
                ), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cDu besitzt keine Anteile an diesem Shop!"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop sell", e);
            return 0;
        }
    }

    private static int showMyShares(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID playerId = player.getUUID();

            StringBuilder sb = new StringBuilder("§e§l=== Meine Shop-Anteile ===\n");
            boolean hasShares = false;

            for (ShopAccount shop : ShopAccountManager.getAllAccounts()) {
                ShareHolder holder = shop.getShareholder(playerId);

                if (holder != null) {
                    hasShares = true;
                    sb.append("§7Shop: §e").append(shop.getShopId()).append("\n");
                    sb.append("§7  Anteile: §e").append(holder.getSharesOwned())
                        .append(" §7(").append(String.format("%.1f", holder.getOwnershipPercentage())).append("%)\n");
                    sb.append("§7  Kaufpreis: §e").append(holder.getTotalInvestment()).append("€\n");
                    sb.append("§7  7-Tage Netto: §e").append(shop.get7DayNetRevenue()).append("€\n\n");
                }
            }

            if (!hasShares) {
                sb.append("§7Du besitzt keine Shop-Anteile.");
            }

            String result = sb.toString();
            ctx.getSource().sendSuccess(() -> Component.literal(result), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /shop myshares", e);
            return 0;
        }
    }
}
