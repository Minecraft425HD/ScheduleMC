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
import de.rolandsw.schedulemc.region.blocks.PlotBlocks;
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
                
                // /plot buy [plotId]
                .then(Commands.literal("buy")
                        .executes(PlotCommand::buyPlot)
                        .then(Commands.argument("plotId", StringArgumentType.string())
                                .executes(PlotCommand::buyPlotById)))
                
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
                
                // /plot rentplot <tage> [plotId]
                .then(Commands.literal("rentplot")
                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                .executes(PlotCommand::rentPlot)
                                .then(Commands.argument("plotId", StringArgumentType.string())
                                        .executes(PlotCommand::rentPlotById))))
                
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

                // /plot apartment - Apartment-Verwaltung
                .then(Commands.literal("apartment")

                        // /plot apartment wand
                        .then(Commands.literal("wand")
                                .executes(PlotCommand::apartmentWand))

                        // /plot apartment create <name> <miete>
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("monthlyRent", DoubleArgumentType.doubleArg(0))
                                                .executes(PlotCommand::createApartment))))

                        // /plot apartment delete <id>
                        .then(Commands.literal("delete")
                                .then(Commands.argument("apartmentId", StringArgumentType.string())
                                        .executes(PlotCommand::deleteApartment)))

                        // /plot apartment list
                        .then(Commands.literal("list")
                                .executes(PlotCommand::listApartments))

                        // /plot apartment info <id>
                        .then(Commands.literal("info")
                                .then(Commands.argument("apartmentId", StringArgumentType.string())
                                        .executes(PlotCommand::apartmentInfo)))

                        // /plot apartment rent <id> [tage]
                        .then(Commands.literal("rent")
                                .then(Commands.argument("apartmentId", StringArgumentType.string())
                                        .executes(PlotCommand::rentApartment)
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                                .executes(PlotCommand::rentApartmentDays))))

                        // /plot apartment leave
                        .then(Commands.literal("leave")
                                .executes(PlotCommand::leaveApartment))

                        // /plot apartment setrent <id> <miete>
                        .then(Commands.literal("setrent")
                                .then(Commands.argument("apartmentId", StringArgumentType.string())
                                        .then(Commands.argument("monthlyRent", DoubleArgumentType.doubleArg(0))
                                                .executes(PlotCommand::setApartmentRent))))

                        // /plot apartment evict <id>
                        .then(Commands.literal("evict")
                                .then(Commands.argument("apartmentId", StringArgumentType.string())
                                        .executes(PlotCommand::evictTenant)))
                )

                // /plot settype <type>
                .then(Commands.literal("settype")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("type", StringArgumentType.string())
                                .executes(PlotCommand::setPlotType)))

                // /plot warehouse set
                .then(Commands.literal("warehouse")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set")
                                .executes(PlotCommand::setWarehouseLocation))
                        .then(Commands.literal("clear")
                                .executes(PlotCommand::clearWarehouseLocation))
                        .then(Commands.literal("info")
                                .executes(PlotCommand::warehouseInfo)))
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

            return executeBuyPlot(ctx, player, plot);
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot buy", e);
            return 0;
        }
    }

    private static int buyPlotById(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String plotId = StringArgumentType.getString(ctx, "plotId");

            PlotRegion plot = PlotManager.getPlot(plotId);

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cPlot nicht gefunden: §e" + plotId));
                return 0;
            }

            return executeBuyPlot(ctx, player, plot);
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot buy <id>", e);
            return 0;
        }
    }

    private static int executeBuyPlot(CommandContext<CommandSourceStack> ctx, ServerPlayer player, PlotRegion plot) {
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

        // Gebe Plot-Info-Block
        ItemStack infoBlock = new ItemStack(PlotBlocks.PLOT_INFO_BLOCK_ITEM.get());
        player.getInventory().add(infoBlock);

        String plotName = plot.getPlotName();
        final double finalPrice = price;
        ctx.getSource().sendSuccess(() -> Component.literal(
            "§a✓ Plot gekauft!\n" +
            "§7Name: §e" + plotName + "\n" +
            "§7Preis: §e" + String.format("%.2f", finalPrice) + "€\n" +
            "§7Neues Guthaben: §e" + String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "€\n" +
            "§a✓ Plot-Info-Block erhalten!"
        ), false);

        return 1;
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

            return executeRentPlot(ctx, player, plot, days);
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot rentplot", e);
            return 0;
        }
    }

    private static int rentPlotById(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            int days = IntegerArgumentType.getInteger(ctx, "days");
            String plotId = StringArgumentType.getString(ctx, "plotId");

            PlotRegion plot = PlotManager.getPlot(plotId);

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cPlot nicht gefunden: §e" + plotId));
                return 0;
            }

            return executeRentPlot(ctx, player, plot, days);
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot rentplot <id>", e);
            return 0;
        }
    }

    private static int executeRentPlot(CommandContext<CommandSourceStack> ctx, ServerPlayer player, PlotRegion plot, int days) {
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

        final int finalDays = days;
        final double finalTotalCost = totalCost;
        ctx.getSource().sendSuccess(() -> Component.literal(
            "§a✓ Plot gemietet!\n" +
            "§7Dauer: §e" + finalDays + " Tag(e)\n" +
            "§7Kosten: §e" + String.format("%.2f", finalTotalCost) + "€"
        ), false);

        return 1;
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

    // ═══════════════════════════════════════════════════════════
    // APARTMENT COMMANDS
    // ═══════════════════════════════════════════════════════════

    private static int apartmentWand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack wand = new ItemStack(ModItems.PLOT_SELECTION_TOOL.get());

            if (player.getInventory().add(wand)) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§d✓ Apartment-Auswahl-Werkzeug erhalten!\n" +
                    "§7Linksklick: §ePosition 1\n" +
                    "§7Rechtsklick auf Block: §ePosition 2\n" +
                    "§7Dann: §e/plot apartment create <name> <miete>"
                ), false);
            } else {
                ctx.getSource().sendFailure(Component.literal("§cInventar ist voll!"));
            }
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment wand", e);
            return 0;
        }
    }

    private static int createApartment(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(ctx, "name");
            double monthlyRent = DoubleArgumentType.getDouble(ctx, "monthlyRent");

            // Hole Selection
            BlockPos pos1 = de.rolandsw.schedulemc.items.PlotSelectionTool.getPosition1(player.getUUID());
            BlockPos pos2 = de.rolandsw.schedulemc.items.PlotSelectionTool.getPosition2(player.getUUID());

            if (pos1 == null || pos2 == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cKeine Auswahl vorhanden!\n" +
                    "§7Benutze /plot apartment wand und markiere zwei Positionen."
                ));
                return 0;
            }

            // Prüfe ob Spieler auf einem Plot steht
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cDu stehst auf keinem Plot!"
                ));
                return 0;
            }

            // Prüfe ob Spieler der Besitzer ist
            if (!plot.canManage(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cDieser Plot gehört nicht dir!\n" +
                    "§7Nur der Besitzer kann Apartments erstellen."
                ));
                return 0;
            }

            // Normalisiere Min/Max
            BlockPos min = new BlockPos(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
            );
            BlockPos max = new BlockPos(
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ())
            );

            // Prüfe ob Positionen innerhalb des Plots sind
            if (!plot.contains(min) || !plot.contains(max)) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cApartment muss komplett innerhalb deines Plots sein!"
                ));
                return 0;
            }

            // Prüfe Überlappung mit anderen Apartments
            for (de.rolandsw.schedulemc.region.PlotArea existing : plot.getSubAreas()) {
                if (existing.overlaps(min, max)) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cApartment überschneidet sich mit: §e" + existing.getName()
                    ));
                    return 0;
                }
            }

            // Erstelle Apartment
            String apartmentId = "apt_" + (plot.getSubAreaCount() + 1);
            de.rolandsw.schedulemc.region.PlotArea apartment = new de.rolandsw.schedulemc.region.PlotArea(
                apartmentId,
                name,
                plot.getPlotId(),
                min,
                max,
                monthlyRent
            );

            plot.addSubArea(apartment);
            PlotManager.markDirty();

            // Cleanup Selection
            de.rolandsw.schedulemc.items.PlotSelectionTool.clearSelection(player.getUUID());

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Apartment erstellt!\n" +
                "§7ID: §e" + apartmentId + "\n" +
                "§7Name: §e" + name + "\n" +
                "§7Miete: §e" + monthlyRent + "€/Monat\n" +
                "§7Größe: §e" + apartment.getVolume() + " Blöcke"
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment create", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Erstellen des Apartments!"));
            return 0;
        }
    }

    private static int deleteApartment(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst auf keinem Plot!"));
                return 0;
            }

            if (!plot.canManage(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört nicht dir!"));
                return 0;
            }

            de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

            if (apartment == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                    "§7Nutze /plot apartment list für verfügbare Apartments"
                ));
                return 0;
            }

            if (apartment.isRented()) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cApartment ist vermietet! Wirf zuerst den Mieter raus:\n" +
                    "§e/plot apartment evict " + apartmentId
                ));
                return 0;
            }

            String apartmentName = apartment.getName();
            plot.removeSubArea(apartmentId);
            PlotManager.markDirty();

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Apartment gelöscht: §e" + apartmentName
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment delete", e);
            return 0;
        }
    }

    private static int listApartments(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst auf keinem Plot!"));
                return 0;
            }

            List<de.rolandsw.schedulemc.region.PlotArea> apartments = plot.getSubAreas();

            if (apartments.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§7Dieser Plot hat keine Apartments.\n" +
                    "§7Erstelle ein Apartment mit:\n" +
                    "§e/plot apartment wand"
                ), false);
                return 1;
            }

            StringBuilder message = new StringBuilder();
            message.append("§6═══ Apartments in ").append(plot.getPlotName()).append(" ═══\n");

            for (de.rolandsw.schedulemc.region.PlotArea apt : apartments) {
                message.append("\n§e").append(apt.getName()).append(" §7(§e").append(apt.getId()).append("§7)\n");
                message.append("  §7Miete: §e").append(apt.getMonthlyRent()).append("€/Monat\n");

                if (apt.isRented()) {
                    long daysLeft = apt.getRentDaysLeft();
                    message.append("  §a§lVERMIETET §7- Noch §e").append(daysLeft).append(" Tage\n");
                } else if (apt.isForRent()) {
                    message.append("  §d§lVERFÜGBAR §7- §e/plot apartment rent ").append(apt.getId()).append("\n");
                } else {
                    message.append("  §c§lNICHT ZU VERMIETEN\n");
                }

                message.append("  §7Größe: §e").append(apt.getVolume()).append(" Blöcke");
            }

            String finalMessage = message.toString();
            ctx.getSource().sendSuccess(() -> Component.literal(finalMessage), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment list", e);
            return 0;
        }
    }

    private static int apartmentInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst auf keinem Plot!"));
                return 0;
            }

            de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

            if (apartment == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                    "§7Nutze /plot apartment list für verfügbare Apartments"
                ));
                return 0;
            }

            StringBuilder message = new StringBuilder();
            message.append("§6═══ ").append(apartment.getName()).append(" ═══\n");
            message.append("§7ID: §e").append(apartment.getId()).append("\n");
            message.append("§7Miete: §e").append(apartment.getMonthlyRent()).append("€/Monat\n");
            message.append("§7Größe: §e").append(apartment.getVolume()).append(" Blöcke\n");

            if (apartment.isRented()) {
                long days = apartment.getRentDaysLeft();
                message.append("§a§lVERMIETET\n");
                message.append("§7Verbleibende Zeit: §e").append(days).append(" Tage");
            } else if (apartment.isForRent()) {
                message.append("§d§lZU VERMIETEN\n");
                message.append("§7Miete mit: §e/plot apartment rent ").append(apartment.getId());
            } else {
                message.append("§c§lNICHT ZU VERMIETEN");
            }

            String finalMessage = message.toString();
            ctx.getSource().sendSuccess(() -> Component.literal(finalMessage), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment info", e);
            return 0;
        }
    }

    private static int rentApartment(CommandContext<CommandSourceStack> ctx) {
        return rentApartmentDays(ctx, 30); // Default: 30 Tage
    }

    private static int rentApartmentDays(CommandContext<CommandSourceStack> ctx) {
        int days = IntegerArgumentType.getInteger(ctx, "days");
        return rentApartmentDays(ctx, days);
    }

    private static int rentApartmentDays(CommandContext<CommandSourceStack> ctx, int days) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst auf keinem Plot!"));
                return 0;
            }

            de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

            if (apartment == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                    "§7Nutze /plot apartment list für verfügbare Apartments"
                ));
                return 0;
            }

            // Prüfe ob Spieler der Plot-Besitzer ist
            if (plot.canManage(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cDu kannst nicht dein eigenes Apartment mieten!"
                ));
                return 0;
            }

            if (!apartment.isForRent()) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cDieses Apartment wird nicht vermietet!"
                ));
                return 0;
            }

            if (apartment.isRented()) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cDieses Apartment ist bereits vermietet!"
                ));
                return 0;
            }

            // Berechne Kosten: Monatliche Miete * (Tage / 30) + Kaution (3x Monatsmiete)
            double monthlyCost = apartment.getMonthlyRent();
            double rentCost = (monthlyCost / 30.0) * days;
            double deposit = monthlyCost * 3.0;  // 3x Monatsmiete als Kaution
            double totalCost = rentCost + deposit;

            if (!EconomyManager.withdraw(player.getUUID(), totalCost)) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cNicht genug Geld!\n" +
                    "§7Benötigt: §e" + String.format("%.2f", totalCost) + "€\n" +
                    "§7(Miete: §e" + String.format("%.2f", rentCost) + "€ + Kaution: §e" + String.format("%.2f", deposit) + "€)"
                ));
                return 0;
            }

            // Zahle an Plot-Besitzer (nur Miete, Kaution bekommt Mieter später zurück)
            UUID landlordUUID = plot.getOwnerUUIDAsUUID();
            if (landlordUUID != null) {
                EconomyManager.deposit(landlordUUID, rentCost);
            }

            // Starte Miete
            String apartmentName = apartment.getName();
            apartment.startRent(player.getUUID(), days);
            PlotManager.markDirty();

            final int finalDays = days;
            final double finalTotalCost = totalCost;
            final double finalDeposit = deposit;
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Apartment gemietet!\n" +
                "§7Name: §e" + apartmentName + "\n" +
                "§7Dauer: §e" + finalDays + " Tage\n" +
                "§7Kosten: §e" + String.format("%.2f", finalTotalCost) + "€\n" +
                "§7Kaution: §e" + String.format("%.2f", finalDeposit) + "€ §7(bei Auszug zurück)"
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment rent", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler beim Mieten!"));
            return 0;
        }
    }

    private static int leaveApartment(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst auf keinem Plot!"));
                return 0;
            }

            // Finde Apartment wo Spieler Mieter ist
            de.rolandsw.schedulemc.region.PlotArea apartment = null;
            for (de.rolandsw.schedulemc.region.PlotArea apt : plot.getSubAreas()) {
                if (apt.canManage(player.getUUID())) {
                    apartment = apt;
                    break;
                }
            }

            if (apartment == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cDu hast kein Apartment in diesem Plot gemietet!"
                ));
                return 0;
            }

            // Gebe Kaution zurück (3x Monatsmiete)
            double deposit = apartment.getMonthlyRent() * 3.0;
            EconomyManager.deposit(player.getUUID(), deposit);

            // Beende Miete
            String apartmentName = apartment.getName();
            apartment.endRent();
            PlotManager.markDirty();

            final double finalDeposit = deposit;
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Mietvertrag gekündigt!\n" +
                "§7Apartment: §e" + apartmentName + "\n" +
                "§7Kaution zurückerstattet: §e" + String.format("%.2f", finalDeposit) + "€"
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment leave", e);
            return 0;
        }
    }

    private static int setApartmentRent(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String apartmentId = StringArgumentType.getString(ctx, "apartmentId");
            double monthlyRent = DoubleArgumentType.getDouble(ctx, "monthlyRent");

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst auf keinem Plot!"));
                return 0;
            }

            if (!plot.canManage(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört nicht dir!"));
                return 0;
            }

            de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

            if (apartment == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                    "§7Nutze /plot apartment list für verfügbare Apartments"
                ));
                return 0;
            }

            String apartmentName = apartment.getName();
            apartment.setMonthlyRent(monthlyRent);
            PlotManager.markDirty();

            final double finalMonthlyRent = monthlyRent;
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Miete geändert!\n" +
                "§7Apartment: §e" + apartmentName + "\n" +
                "§7Neue Miete: §e" + finalMonthlyRent + "€/Monat"
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment setrent", e);
            return 0;
        }
    }

    private static int evictTenant(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst auf keinem Plot!"));
                return 0;
            }

            if (!plot.canManage(player.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cDieser Plot gehört nicht dir!"));
                return 0;
            }

            de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

            if (apartment == null) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                    "§7Nutze /plot apartment list für verfügbare Apartments"
                ));
                return 0;
            }

            if (!apartment.isRented()) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cDieses Apartment ist nicht vermietet!"
                ));
                return 0;
            }

            // KEINE Kaution zurück bei Rauswurf
            String apartmentName = apartment.getName();
            apartment.endRent();
            PlotManager.markDirty();

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Mieter rausgeworfen!\n" +
                "§7Apartment: §e" + apartmentName + "\n" +
                "§c§lKaution wurde nicht zurückgezahlt!"
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot apartment evict", e);
            return 0;
        }
    }

    /**
     * Findet ein Apartment nach ID oder Name
     */
    private static de.rolandsw.schedulemc.region.PlotArea findApartment(PlotRegion plot, String idOrName) {
        // Suche zuerst nach ID
        de.rolandsw.schedulemc.region.PlotArea apartment = plot.getSubArea(idOrName);

        if (apartment == null) {
            // Versuche nach Name zu suchen
            for (de.rolandsw.schedulemc.region.PlotArea apt : plot.getSubAreas()) {
                if (apt.getName().equalsIgnoreCase(idOrName)) {
                    return apt;
                }
            }
        }

        return apartment;
    }

    // ═══════════════════════════════════════════════════════════
    // WAREHOUSE & PLOT TYPE COMMANDS
    // ═══════════════════════════════════════════════════════════

    private static int setPlotType(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst nicht in einem Plot!"));
                return 0;
            }

            try {
                de.rolandsw.schedulemc.region.PlotType type = de.rolandsw.schedulemc.region.PlotType.valueOf(typeStr);
                plot.setType(type);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Plot-Typ geändert!\n" +
                    "§7Neuer Typ: §e" + type.getDisplayName()
                ), false);
                return 1;
            } catch (IllegalArgumentException e) {
                ctx.getSource().sendFailure(Component.literal(
                    "§cUngültiger Plot-Typ!\n" +
                    "§7Verfügbar: §eRESIDENTIAL, COMMERCIAL, SHOP, PUBLIC, GOVERNMENT"
                ));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot settype", e);
            return 0;
        }
    }

    private static int setWarehouseLocation(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos playerPos = player.blockPosition();

            PlotRegion plot = PlotManager.getPlotAt(playerPos);
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst nicht in einem Plot!"));
                return 0;
            }

            plot.setWarehouseLocation(playerPos);
            PlotManager.markDirty();

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Warehouse-Position gesetzt!\n" +
                "§7Plot: §e" + plot.getPlotId() + "\n" +
                "§7Position: §f" + playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ()
            ), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot warehouse set", e);
            return 0;
        }
    }

    private static int clearWarehouseLocation(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst nicht in einem Plot!"));
                return 0;
            }

            plot.setWarehouseLocation(null);
            PlotManager.markDirty();

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Warehouse-Position entfernt!\n" +
                "§7Plot: §e" + plot.getPlotId()
            ), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot warehouse clear", e);
            return 0;
        }
    }

    private static int warehouseInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();

            PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
            if (plot == null) {
                ctx.getSource().sendFailure(Component.literal("§cDu stehst nicht in einem Plot!"));
                return 0;
            }

            BlockPos warehousePos = plot.getWarehouseLocation();
            if (warehousePos == null) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§e=== Warehouse Info ===\n" +
                    "§7Plot: §e" + plot.getPlotId() + "\n" +
                    "§7Status: §cKein Warehouse verknüpft"
                ), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§e=== Warehouse Info ===\n" +
                    "§7Plot: §e" + plot.getPlotId() + "\n" +
                    "§7Position: §f" + warehousePos.getX() + ", " + warehousePos.getY() + ", " + warehousePos.getZ()
                ), false);
            }
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /plot warehouse info", e);
            return 0;
        }
    }
}
