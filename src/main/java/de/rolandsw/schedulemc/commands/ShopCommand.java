package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.data.ShopItem;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.managers.ShopManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

/**
 * Shop-System Commands
 */
public class ShopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shop")
                // /shop - Öffnet GUI (später) oder zeigt Liste
                .executes(ShopCommand::showShop)
                
                // /shop buy <item> <menge>
                .then(Commands.literal("buy")
                        .then(Commands.argument("item", StringArgumentType.string())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(ShopCommand::buyItem))))
                
                // /shop sell <item> <menge>
                .then(Commands.literal("sell")
                        .then(Commands.argument("item", StringArgumentType.string())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(ShopCommand::sellItem))))
                
                // /shop prices - Zeigt alle Preise
                .then(Commands.literal("prices")
                        .executes(ShopCommand::showPrices))
                
                // /shop info <item>
                .then(Commands.literal("info")
                        .then(Commands.argument("item", StringArgumentType.string())
                                .executes(ShopCommand::showItemInfo)))
                
                // ADMIN: /shop setprice <item> <buy> <sell>
                .then(Commands.literal("setprice")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("item", StringArgumentType.string())
                                .then(Commands.argument("buyPrice", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("sellPrice", IntegerArgumentType.integer(0))
                                                .executes(ShopCommand::setPrices)))))
                
                // ADMIN: /shop additem <item> <buy> <sell>
                .then(Commands.literal("additem")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("item", StringArgumentType.string())
                                .then(Commands.argument("buyPrice", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("sellPrice", IntegerArgumentType.integer(0))
                                                .executes(ShopCommand::addItem)))))
        );
    }

    // ═══════════════════════════════════════════════════════════
    // SPIELER-COMMANDS
    // ═══════════════════════════════════════════════════════════

    private static int showShop(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§6═══════════════════════════════\n" +
                "§e§l         SHOP-MENÜ\n" +
                "§6═══════════════════════════════\n" +
                "§7Verfügbare Befehle:\n" +
                "§e/shop buy <item> <menge> §7- Item kaufen\n" +
                "§e/shop sell <item> <menge> §7- Item verkaufen\n" +
                "§e/shop prices §7- Alle Preise anzeigen\n" +
                "§e/shop info <item> §7- Item-Info\n" +
                "\n§7Beispiel: §e/shop buy diamond 10\n" +
                "§6═══════════════════════════════"
            ), false);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int buyItem(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String itemId = StringArgumentType.getString(ctx, "item");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            
            // Vollständige ID wenn nötig
            if (!itemId.contains(":")) {
                itemId = "minecraft:" + itemId;
            }
            
            ShopItem shopItem = ShopManager.getItem(itemId);
            
            if (shopItem == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cItem nicht im Shop gefunden!\n" +
                    "§7Tipp: §e/shop prices §7für alle Items"
                ));
                return 0;
            }
            
            if (!shopItem.canBuy()) {
                ctx.getSource().sendFailure(Component.literal("§cDieses Item kann nicht gekauft werden!"));
                return 0;
            }
            
            if (!shopItem.hasStock(amount)) {
                ctx.getSource().sendFailure(Component.literal("§cNicht genug auf Lager!"));
                return 0;
            }
            
            double totalPrice = shopItem.getTotalBuyPrice(amount);
            
            if (EconomyManager.getBalance(player.getUUID()) < totalPrice) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Benötigt: §e" + String.format("%.2f", totalPrice) + "€\n" +
                    "§7Dein Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "€"
                ));
                return 0;
            }
            
            // Zahlung
            EconomyManager.withdraw(player.getUUID(), totalPrice);
            
            // Items geben
            ItemStack itemStack = shopItem.getItemStack(amount);
            if (!player.getInventory().add(itemStack)) {
                // Inventar voll - Geld zurück
                EconomyManager.deposit(player.getUUID(), totalPrice);
                ctx.getSource().sendFailure(Component.literal("§cDein Inventar ist voll!"));
                return 0;
            }
            
            // Lagerbestand reduzieren
            shopItem.reduceStock(amount);
            ShopManager.saveIfNeeded();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Gekauft!\n" +
                "§7Item: §e" + shopItem.getDisplayName() + " §7x" + amount + "\n" +
                "§7Bezahlt: §e" + String.format("%.2f", totalPrice) + "€\n" +
                "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "€"
            ), false);
            
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cFehler: " + e.getMessage()));
            return 0;
        }
    }

    private static int sellItem(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String itemId = StringArgumentType.getString(ctx, "item");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            
            // Vollständige ID wenn nötig
            if (!itemId.contains(":")) {
                itemId = "minecraft:" + itemId;
            }
            
            ShopItem shopItem = ShopManager.getItem(itemId);
            
            if (shopItem == null) {
                ctx.getSource().sendFailure(Component.literal("§cItem nicht im Shop!"));
                return 0;
            }
            
            if (!shopItem.canSell()) {
                ctx.getSource().sendFailure(Component.literal("§cDieses Item kann nicht verkauft werden!"));
                return 0;
            }
            
            // Prüfen ob Spieler Items hat
            ItemStack itemStack = shopItem.getItemStack(1);
            int count = 0;
            
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (ItemStack.isSameItemSameTags(stack, itemStack)) {
                    count += stack.getCount();
                }
            }
            
            if (count < amount) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Items!\n" +
                    "§7Benötigt: §e" + amount + "x\n" +
                    "§7Du hast: §e" + count + "x"
                ));
                return 0;
            }
            
            // Items aus Inventar nehmen
            int remaining = amount;
            for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (ItemStack.isSameItemSameTags(stack, itemStack)) {
                    int toRemove = Math.min(remaining, stack.getCount());
                    stack.shrink(toRemove);
                    remaining -= toRemove;
                }
            }
            
            // Geld geben
            double totalPrice = shopItem.getTotalSellPrice(amount);
            EconomyManager.deposit(player.getUUID(), totalPrice);
            
            // Lagerbestand erhöhen
            shopItem.increaseStock(amount);
            ShopManager.saveIfNeeded();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Verkauft!\n" +
                "§7Item: §e" + shopItem.getDisplayName() + " §7x" + amount + "\n" +
                "§7Erhalten: §e" + String.format("%.2f", totalPrice) + "€\n" +
                "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "€"
            ), false);
            
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cFehler: " + e.getMessage()));
            return 0;
        }
    }

    private static int showPrices(CommandContext<CommandSourceStack> ctx) {
        Collection<ShopItem> items = ShopManager.getAllItems();
        
        if (items.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cShop ist leer!"));
            return 0;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "§6═══════════════════════════════\n" +
            "§e§l      SHOP-PREISLISTE\n" +
            "§6═══════════════════════════════"
        ), false);
        
        for (ShopItem item : items) {
            if (!item.isAvailable()) continue;
            
            String buyInfo = item.canBuy() ? 
                "§aKauf: §e" + String.format("%.2f", item.getBuyPrice()) + "€" : 
                "§c[Nicht kaufbar]";
            
            String sellInfo = item.canSell() ? 
                "§aSell: §e" + String.format("%.2f", item.getSellPrice()) + "€" : 
                "§c[Nicht verkaufbar]";
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§7• §e" + item.getDisplayName() + "\n" +
                "  " + buyInfo + " §7| " + sellInfo
            ), false);
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal("§6═══════════════════════════════"), false);
        
        return 1;
    }

    private static int showItemInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            String itemId = StringArgumentType.getString(ctx, "item");
            
            if (!itemId.contains(":")) {
                itemId = "minecraft:" + itemId;
            }
            
            ShopItem shopItem = ShopManager.getItem(itemId);
            
            if (shopItem == null) {
                ctx.getSource().sendFailure(Component.literal("§cItem nicht im Shop!"));
                return 0;
            }
            
            String stockInfo = shopItem.getStock() == -1 ? 
                "§aUnbegrenzt" : 
                "§e" + shopItem.getStock() + " Stück";
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§6═══ §eItem-Info §6═══\n" +
                "§7Name: §e" + shopItem.getDisplayName() + "\n" +
                "§7ID: §f" + shopItem.getItemId() + "\n" +
                "§7Kaufpreis: §e" + String.format("%.2f", shopItem.getBuyPrice()) + "€\n" +
                "§7Verkaufspreis: §e" + String.format("%.2f", shopItem.getSellPrice()) + "€\n" +
                "§7Lagerbestand: " + stockInfo + "\n" +
                "§7Kaufbar: " + (shopItem.canBuy() ? "§aJa" : "§cNein") + "\n" +
                "§7Verkaufbar: " + (shopItem.canSell() ? "§aJa" : "§cNein")
            ), false);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN-COMMANDS
    // ═══════════════════════════════════════════════════════════

    private static int setPrices(CommandContext<CommandSourceStack> ctx) {
        try {
            String itemId = StringArgumentType.getString(ctx, "item");
            double buyPrice = IntegerArgumentType.getInteger(ctx, "buyPrice");
            double sellPrice = IntegerArgumentType.getInteger(ctx, "sellPrice");
            
            if (!itemId.contains(":")) {
                itemId = "minecraft:" + itemId;
            }
            
            ShopItem shopItem = ShopManager.getItem(itemId);
            
            if (shopItem == null) {
                ctx.getSource().sendFailure(Component.literal("§cItem nicht im Shop!"));
                return 0;
            }
            
            ShopManager.updatePrices(itemId, buyPrice, sellPrice);
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Preise aktualisiert!\n" +
                "§7Item: §e" + shopItem.getDisplayName() + "\n" +
                "§7Kauf: §e" + buyPrice + "€\n" +
                "§7Verkauf: §e" + sellPrice + "€"
            ), true);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int addItem(CommandContext<CommandSourceStack> ctx) {
        try {
            String itemId = StringArgumentType.getString(ctx, "item");
            double buyPrice = IntegerArgumentType.getInteger(ctx, "buyPrice");
            double sellPrice = IntegerArgumentType.getInteger(ctx, "sellPrice");
            
            if (!itemId.contains(":")) {
                itemId = "minecraft:" + itemId;
            }
            
            if (ShopManager.hasItem(itemId)) {
                ctx.getSource().sendFailure(Component.literal("§cItem bereits im Shop! Nutze /shop setprice"));
                return 0;
            }
            
            ShopManager.addItem(itemId, buyPrice, sellPrice);
            ShopManager.save();

	    // ✅ Fix: finale Kopie für Lambda
            final String finalItemId = itemId;
            final double finalBuyPrice = buyPrice;
            final double finalSellPrice = sellPrice;
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Item zum Shop hinzugefügt!\n" +
                "§7ID: §f" + finalItemId + "\n" +
                "§7Kauf: §e" + finalBuyPrice + "€\n" +
                "§7Verkauf: §e" + finalSellPrice + "€"
            ), true);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
