package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.items.PlotSelectionTool;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ScheduleMC 3.0 Commands - Vollständig implementiert
 */
public class PlotCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("plot")
                
                // /plot wand
                .then(Commands.literal("wand")
                        .executes(PlotCommand::giveWand))
                
                // /plot create <preis> [public]
                .then(Commands.literal("create")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("price", DoubleArgumentType.doubleArg(0.01))
                                .executes(PlotCommand::createPlot)
                                .then(Commands.literal("public")
                                        .executes(PlotCommand::createPublicPlot))))
                
                // /plot setowner <player>
                .then(Commands.literal("setowner")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PlotCommand::setPlotOwner)))
                
                // /plot buy
                .then(Commands.literal("buy")
                        .executes(PlotCommand::buyPlot))
                
                // /plot list
                .then(Commands.literal("list")
                        .executes(PlotCommand::listPlots))
                
                // /plot info
                .then(Commands.literal("info")
                        .executes(PlotCommand::plotInfo))
                
                // /plot name <n>
                .then(Commands.literal("name")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(PlotCommand::setPlotName)))
                
                // /plot description <text>
                .then(Commands.literal("description")
                        .then(Commands.argument("description", StringArgumentType.greedyString())
                                .executes(PlotCommand::setPlotDescription)))
                
                // /plot trust <player>
                .then(Commands.literal("trust")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PlotCommand::trustPlayer)))
                
                // /plot untrust <player>
                .then(Commands.literal("untrust")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PlotCommand::untrustPlayer)))
                
                // /plot trustlist
                .then(Commands.literal("trustlist")
                        .executes(PlotCommand::listTrusted))
                
                // /plot sell <preis>
                .then(Commands.literal("sell")
                        .then(Commands.argument("price", DoubleArgumentType.doubleArg(0.01))
                                .executes(PlotCommand::sellPlot)))
                
                // /plot unsell
                .then(Commands.literal("unsell")
                        .executes(PlotCommand::unsellPlot))
                
                // /plot transfer <player>
                .then(Commands.literal("transfer")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PlotCommand::transferPlot)))
                
                // /plot abandon
                .then(Commands.literal("abandon")
                        .executes(PlotCommand::abandonPlot))
                
                // /plot rent <preis>
                .then(Commands.literal("rent")
                        .then(Commands.argument("pricePerDay", DoubleArgumentType.doubleArg(0.01))
                                .executes(PlotCommand::setForRent)))
                
                // /plot rentcancel
                .then(Commands.literal("rentcancel")
                        .executes(PlotCommand::cancelRent))
                
                // /plot rentplot <tage>
                .then(Commands.literal("rentplot")
                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                .executes(PlotCommand::rentPlot)))
                
                // /plot rentextend <tage>
                .then(Commands.literal("rentextend")
                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                .executes(PlotCommand::extendRent)))
                
                // /plot rate <rating>
                .then(Commands.literal("rate")
                        .then(Commands.argument("rating", IntegerArgumentType.integer(1, 5))
                                .executes(PlotCommand::ratePlot)))
                
                // /plot topplots
                .then(Commands.literal("topplots")
                        .executes(PlotCommand::topPlots))
                
                // /plot remove
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .executes(PlotCommand::removePlot))

                // /plot reindex (Admin-Debug-Befehl)
                .then(Commands.literal("reindex")
                        .requires(source -> source.hasPermission(2))
                        .executes(PlotCommand::reindexPlots))

                // /plot debug (Admin-Debug-Befehl)
                .then(Commands.literal("debug")
                        .requires(source -> source.hasPermission(2))
                        .executes(PlotCommand::debugPosition))
        );
    }

    private static int giveWand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack wand = new ItemStack(ModItems.PLOT_SELECTION_TOOL.get());
            
            if (player.getInventory().add(wand)) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Plot-Auswahl-Werkzeug erhalten!\n" +
                    "§7Linksklick: §ePosition 1\n" +
                    "§7Rechtsklick auf Block: §ePosition 2\n" +
                    "§7Dann: §e/plot create <preis>"
                ), false);
            } else {
                ctx.getSource().sendFailure(Component.literal("§cInventar ist voll!"));
            }
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot wand", e);
            return 0;
        }
    }

    private static int createPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            double price = DoubleArgumentType.getDouble(ctx, "price");
            
            BlockPos pos1 = PlotSelectionTool.getPosition1(player.getUUID());
            BlockPos pos2 = PlotSelectionTool.getPosition2(player.getUUID());
            
            if (pos1 == null || pos2 == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cKeine Auswahl vorhanden!\n" +
                    "§7Benutze das Selection Tool um zwei Positionen zu markieren."
                ));
                return 0;
            }
            
            PlotRegion plot = PlotManager.createPlot(pos1, pos2, price);
            PlotSelectionTool.clearSelection(player.getUUID());
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot erstellt!\n" +
                "§7ID: §e" + plot.getPlotId() + "\n" +
                "§7Preis: §e" + String.format("%.2f", price) + "€\n" +
                "§7Größe: §e" + plot.getVolume() + " Blöcke"
            ), true);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot create", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Erstellen des Plots!"));
            return 0;
        }
    }

    private static int createPublicPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            double price = DoubleArgumentType.getDouble(ctx, "price");
            
            BlockPos pos1 = PlotSelectionTool.getPosition1(player.getUUID());
            BlockPos pos2 = PlotSelectionTool.getPosition2(player.getUUID());
            
            if (pos1 == null || pos2 == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cKeine Auswahl vorhanden!\n" +
                    "§7Benutze das Selection Tool um zwei Positionen zu markieren."
                ));
                return 0;
            }
            
            PlotRegion plot = PlotManager.createPlot(pos1, pos2, price);
            plot.setPublic(true);
            PlotSelectionTool.clearSelection(player.getUUID());
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Öffentlicher Plot erstellt!\n" +
                "§7ID: §e" + plot.getPlotId() + "\n" +
                "§d§lÖFFENTLICH\n" +
                "§7Jeder kann Objekte benutzen\n" +
                "§7Niemand kann bauen/abbauen"
            ), true);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot create public", e);
            return 0;
        }
    }

    private static int setPlotOwner(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer admin = ctx.getSource().getPlayerOrException();
            ServerPlayer newOwner = EntityArgument.getPlayer(ctx, "player");
            PlotRegion plot = PlotManager.getPlotAt(admin.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            plot.setOwner(newOwner.getUUID(), newOwner.getName().getString());
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Besitzer geändert!\n" +
                "§7Plot: §e" + plot.getPlotName() + "\n" +
                "§7Neuer Besitzer: §b" + newOwner.getName().getString()
            ), true);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot setowner", e);
            return 0;
        }
    }

    private static int buyPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (plot.hasOwner()) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot hat bereits einen Besitzer!"));
                return 0;
            }
            
            double price = plot.getPrice();
            
            if (EconomyManager.getBalance(player.getUUID()) < price) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Preis: §e" + String.format("%.2f", price) + "€\n" +
                    "§7Dein Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "€"
                ));
                return 0;
            }
            
            EconomyManager.withdraw(player.getUUID(), price);
            plot.setOwner(player.getUUID(), player.getName().getString());
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot gekauft!\n" +
                "§7Name: §e" + plot.getPlotName() + "\n" +
                "§7Preis: §e" + String.format("%.2f", price) + "€\n" +
                "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "€"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot buy", e);
            return 0;
        }
    }
    
    private static int listPlots(CommandContext<CommandSourceStack> ctx) {
        List<PlotRegion> plots = PlotManager.getPlots();
        
        if (plots.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cKeine Plots vorhanden!"));
            return 0;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "§6═══════════════════════════════\n" +
            "§e§l      VERFÜGBARE PLOTS\n" +
            "§6═══════════════════════════════"
        ), false);
        
        for (PlotRegion plot : plots) {
            String status = plot.hasOwner() ? "§c[BELEGT]" : "§a[FREI]";
            String price = plot.hasOwner() && plot.isForSale() ? 
                " §7- Verkauf: §e" + String.format("%.2f", plot.getSalePrice()) + "€" : "";
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                status + " §e" + plot.getPlotName() + 
                " §7(§f" + plot.getVolume() + " Blöcke§7)" + price
            ), false);
        }
        
        return 1;
    }
    
    private static int plotInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            String ownerInfo = plot.hasOwner() ? 
                "§7Besitzer: §b" + plot.getOwnerName() : 
                "§a§lZU VERKAUFEN";
            
            String priceInfo = plot.hasOwner() ? 
                (plot.isForSale() ? "§7Verkaufspreis: §e" + String.format("%.2f", plot.getSalePrice()) + "€" : "") :
                "§7Preis: §e" + String.format("%.2f", plot.getPrice()) + "€";
            
            String ratingInfo = plot.getRatingCount() > 0 ?
                "§7Rating: §6" + plot.getRatingStars() + " §7(" + plot.getRatingCount() + " Bewertungen)" : "";
            
            String description = plot.getDescription() != null && !plot.getDescription().isEmpty() ?
                "\n§7Beschreibung: §f" + plot.getDescription() : "";
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§6═══ Plot-Info ═══\n" +
                "§7Name: §e" + plot.getPlotName() + "\n" +
                "§7ID: §f" + plot.getPlotId() + "\n" +
                ownerInfo + "\n" +
                priceInfo + "\n" +
                ratingInfo +
                description + "\n" +
                "§7Größe: §e" + plot.getVolume() + " Blöcke"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot info", e);
            return 0;
        }
    }

    private static int setPlotName(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(ctx, "name");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            plot.setPlotName(name);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot-Name geändert!\n" +
                "§7Neuer Name: §e" + name
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot name", e);
            return 0;
        }
    }
    
    private static int setPlotDescription(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String description = StringArgumentType.getString(ctx, "description");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            plot.setDescription(description);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Beschreibung geändert!\n" +
                "§7Neue Beschreibung: §f" + description
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot description", e);
            return 0;
        }
    }
    
    private static int trustPlayer(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerPlayer trustPlayer = EntityArgument.getPlayer(ctx, "player");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            if (plot.isTrusted(trustPlayer.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Spieler ist bereits berechtigt!"));
                return 0;
            }
            
            plot.addTrustedPlayer(trustPlayer.getUUID());
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Spieler berechtigt!\n" +
                "§7Spieler: §b" + trustPlayer.getName().getString()
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot trust", e);
            return 0;
        }
    }
    
    private static int untrustPlayer(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerPlayer untrustPlayer = EntityArgument.getPlayer(ctx, "player");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            if (!plot.isTrusted(untrustPlayer.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Spieler ist nicht berechtigt!"));
                return 0;
            }
            
            plot.removeTrustedPlayer(untrustPlayer.getUUID());
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Berechtigung entfernt!\n" +
                "§7Spieler: §b" + untrustPlayer.getName().getString()
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot untrust", e);
            return 0;
        }
    }
    
    private static int listTrusted(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            List<String> trusted = new ArrayList<>(plot.getTrustedPlayers());
            
            if (trusted.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§7Keine berechtigten Spieler."
                ), false);
                return 1;
            }
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§6═══ Berechtigte Spieler ═══"
            ), false);
            
            for (String uuidStr : trusted) {
                ctx.getSource().sendSuccess(() -> Component.literal("§7• §b" + uuidStr), false);
            }
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot trustlist", e);
            return 0;
        }
    }

    private static int sellPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            double price = DoubleArgumentType.getDouble(ctx, "price");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            plot.setForSale(true);
            plot.setSalePrice(price);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot zum Verkauf angeboten!\n" +
                "§7Verkaufspreis: §e" + String.format("%.2f", price) + "€"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot sell", e);
            return 0;
        }
    }
    
    private static int unsellPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            plot.setForSale(false);
            plot.setSalePrice(0);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Verkaufsangebot zurückgezogen!"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot unsell", e);
            return 0;
        }
    }
    
    private static int transferPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerPlayer newOwner = EntityArgument.getPlayer(ctx, "player");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            plot.setOwner(newOwner.getUUID(), newOwner.getName().getString());
            plot.setForSale(false);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot übertragen!\n" +
                "§7Neuer Besitzer: §b" + newOwner.getName().getString()
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot transfer", e);
            return 0;
        }
    }
    
    private static int abandonPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            double refund = plot.getPrice() * 0.5;
            EconomyManager.deposit(player.getUUID(), refund);
            
            plot.setOwnerUUID("");
            plot.setOwnerName(null);
            plot.setForSale(false);
            plot.setSalePrice(0);
            plot.setForRent(false);
            plot.setRentPricePerDay(0);
            plot.clearTrustedPlayers();
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot aufgegeben!\n" +
                "§7Rückerstattung: §e" + String.format("%.2f", refund) + "€ §7(50%)\n" +
                "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "€"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot abandon", e);
            return 0;
        }
    }

    private static int setForRent(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            double pricePerDay = DoubleArgumentType.getDouble(ctx, "pricePerDay");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            plot.setForRent(true);
            plot.setRentPricePerDay(pricePerDay);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot zur Miete angeboten!\n" +
                "§7Preis pro Tag: §e" + String.format("%.2f", pricePerDay) + "€"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot rent", e);
            return 0;
        }
    }
    
    private static int cancelRent(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört dir nicht!"));
                return 0;
            }
            
            plot.setForRent(false);
            plot.setRentPricePerDay(0);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Mietangebot zurückgezogen!"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot rentcancel", e);
            return 0;
        }
    }
    
    private static int rentPlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int days = IntegerArgumentType.getInteger(ctx, "days");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.isForRent() || plot.isRented()) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot ist nicht zur Miete verfügbar!"));
                return 0;
            }
            
            double totalCost = plot.getRentPricePerDay() * days;
            
            if (EconomyManager.getBalance(player.getUUID()) < totalCost) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Kosten: §e" + String.format("%.2f", totalCost) + "€"
                ));
                return 0;
            }
            
            EconomyManager.withdraw(player.getUUID(), totalCost);
            UUID ownerUUID = plot.getOwnerUUIDAsUUID();
            if (ownerUUID != null) {
                EconomyManager.deposit(ownerUUID, totalCost);
            }
            
            long rentEndTime = System.currentTimeMillis() + (days * 24L * 60L * 60L * 1000L);
            plot.setRenterUUID(player.getUUID().toString());
            plot.setRentEndTime(rentEndTime);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot gemietet!\n" +
                "§7Dauer: §e" + days + " Tag(e)\n" +
                "§7Kosten: §e" + String.format("%.2f", totalCost) + "€"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot rentplot", e);
            return 0;
        }
    }
    
    private static int extendRent(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int days = IntegerArgumentType.getInteger(ctx, "days");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.getRenterUUID().equals(player.getUUID().toString())) {
                ctx.getSource().sendFailure(Component.literal("§cDu mietest diesen Plot nicht!"));
                return 0;
            }
            
            double totalCost = plot.getRentPricePerDay() * days;
            
            if (EconomyManager.getBalance(player.getUUID()) < totalCost) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Kosten: §e" + String.format("%.2f", totalCost) + "€"
                ));
                return 0;
            }
            
            EconomyManager.withdraw(player.getUUID(), totalCost);
            UUID ownerUUID = plot.getOwnerUUIDAsUUID();
            if (ownerUUID != null) {
                EconomyManager.deposit(ownerUUID, totalCost);
            }
            
            long additionalTime = days * 24L * 60L * 60L * 1000L;
            plot.setRentEndTime(plot.getRentEndTime() + additionalTime);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Miete verlängert!\n" +
                "§7Verlängerung: §e" + days + " Tag(e)"
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot rentextend", e);
            return 0;
        }
    }

    private static int ratePlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int rating = IntegerArgumentType.getInteger(ctx, "rating");
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }
            
            if (!plot.hasOwner()) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot hat keinen Besitzer!"));
                return 0;
            }
            
            if (plot.isOwnedBy(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDu kannst deinen eigenen Plot nicht bewerten!"));
                return 0;
            }
            
            plot.addRating(player.getUUID(), rating);
            PlotManager.markDirty();
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Bewertung abgegeben!\n" +
                "§7Deine Bewertung: §6" + "★".repeat(rating) + "§7" + "☆".repeat(5 - rating) + "\n" +
                "§7Durchschnitt: §6" + plot.getRatingStars()
            ), false);
            
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot rate", e);
            return 0;
        }
    }
    
    private static int topPlots(CommandContext<CommandSourceStack> ctx) {
        List<PlotRegion> topPlots = PlotManager.getTopRatedPlots(10);
        
        if (topPlots.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cKeine bewerteten Plots vorhanden!"));
            return 0;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "§6═══════════════════════════════\n" +
            "§e§l       TOP PLOTS\n" +
            "§6═══════════════════════════════"
        ), false);
        
        int rank = 1;
        for (PlotRegion plot : topPlots) {
            String medal = rank == 1 ? "§6🥇" : rank == 2 ? "§7🥈" : rank == 3 ? "§c🥉" : "§7" + rank + ".";
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                medal + " §e" + plot.getPlotName() + " §7- §6" + plot.getRatingStars()
            ), false);
            
            rank++;
        }
        
        return 1;
    }

    private static int removePlot(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer admin = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(admin.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst in keinem Plot!"));
                return 0;
            }

            String plotName = plot.getPlotName();
            String plotId = plot.getPlotId();

            PlotManager.removePlot(plotId);

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Plot entfernt!\n" +
                "§7ID: §e" + plotId + "\n" +
                "§7Name: §e" + plotName
            ), true);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot remove", e);
            return 0;
        }
    }

    private static int reindexPlots(CommandContext<CommandSourceStack> ctx) {
        try {
            PlotManager.rebuildSpatialIndex();

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Spatial Index neu aufgebaut!\n" +
                "§7Alle Plots wurden neu indiziert."
            ), true);

            LOGGER.info("Spatial Index manuell neu aufgebaut durch Admin");
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot reindex", e);
            return 0;
        }
    }

    private static int debugPosition(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition();
            PlotRegion plot = PlotManager.getPlotAt(pos);

            String plotInfo = plot != null ?
                "§aPlot gefunden: §e" + plot.getPlotId() + " (" + plot.getPlotName() + ")" :
                "§cKein Plot an dieser Position";

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§6═══ Debug-Info ═══\n" +
                "§7Position: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "\n" +
                plotInfo + "\n" +
                "§7Alle Plots: §f" + PlotManager.getPlotCount()
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot debug", e);
            return 0;
        }
    }
}
