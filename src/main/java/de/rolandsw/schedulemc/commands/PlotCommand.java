package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.items.PlotSelectionTool;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import de.rolandsw.schedulemc.region.blocks.PlotBlocks;
import de.rolandsw.schedulemc.commands.CommandExecutor;
import de.rolandsw.schedulemc.util.InputValidation;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ScheduleMC 3.0 Commands - Vollständig implementiert
 * Refactored mit CommandExecutor
 */
public class PlotCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("plot")

                // ✅ /plot wand - ENTFERNT (wird nicht mehr benötigt)

                // /plot create <type> <name> [price]
                .then(Commands.literal("create")
                        .requires(source -> source.hasPermission(2))

                        // /plot create residential <name> <price>
                        .then(Commands.literal("residential")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("price", DoubleArgumentType.doubleArg(0.01))
                                                .executes(ctx -> createPlotWithType(ctx, PlotType.RESIDENTIAL)))))

                        // /plot create commercial <name> <price>
                        .then(Commands.literal("commercial")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("price", DoubleArgumentType.doubleArg(0.01))
                                                .executes(ctx -> createPlotWithType(ctx, PlotType.COMMERCIAL)))))

                        // /plot create shop <name>
                        .then(Commands.literal("shop")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(ctx -> createPlotWithType(ctx, PlotType.SHOP))))

                        // /plot create public <name>
                        .then(Commands.literal("public")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(ctx -> createPlotWithType(ctx, PlotType.PUBLIC))))

                        // /plot create government <name>
                        .then(Commands.literal("government")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(ctx -> createPlotWithType(ctx, PlotType.GOVERNMENT)))))
                
                // /plot setowner <player>
                .then(Commands.literal("setowner")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(PlotCommand::setPlotOwner)))

                // ✅ /plot buy - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot list - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot info - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot name - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot description - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot trust - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot untrust - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot trustlist - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot sell - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot unsell - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot transfer - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot abandon - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot rent - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot rentcancel - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot rentplot - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot rentextend - ENTFERNT (ersetzt durch Settings App UI)
                // ✅ /plot rate - ENTFERNT (ersetzt durch PlotInfoScreen Rating-Buttons)
                // ✅ /plot topplots - ENTFERNT (ersetzt durch PlotInfoScreen Rating-Anzeige)

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
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot wand",
            player -> {
                ItemStack wand = new ItemStack(ModItems.PLOT_SELECTION_TOOL.get());

                if (player.getInventory().add(wand)) {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§a✓ Plot-Auswahl-Werkzeug erhalten!\n" +
                        "§7Linksklick: §ePosition 1\n" +
                        "§7Rechtsklick auf Block: §ePosition 2\n" +
                        "§7Dann: §e/plot create <type> <name> [price]"
                    ), false);
                } else {
                    CommandExecutor.sendFailure(ctx.getSource(), "Inventar ist voll!");
                }
            });
    }

    /**
     * Erstellt einen Plot mit spezifischem Typ
     */
    private static int createPlotWithType(CommandContext<CommandSourceStack> ctx, PlotType type) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot create " + type.name(),
            player -> {
                String name = StringArgumentType.getString(ctx, "name");

                // ✅ INPUT VALIDATION: Name validieren
                InputValidation.ValidationResult nameValidation = InputValidation.validateName(name);
                if (nameValidation.isFailure()) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "§c❌ Ungültiger Name: §f" + nameValidation.getErrorMessage()
                    );
                    return;
                }

                // Preis nur für kaufbare Typen erforderlich
                double price = 0.0;
                if (type.canBePurchased()) {
                    try {
                        price = DoubleArgumentType.getDouble(ctx, "price");

                        // ✅ INPUT VALIDATION: Preis validieren
                        InputValidation.ValidationResult priceValidation = InputValidation.validatePrice(price);
                        if (priceValidation.isFailure()) {
                            CommandExecutor.sendFailure(ctx.getSource(),
                                "§c❌ Ungültiger Preis: §f" + priceValidation.getErrorMessage()
                            );
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            "Fehler: " + type.getDisplayName() + " benötigt einen Preis!\n" +
                            "Verwendung: /plot create " + type.name().toLowerCase() + " <name> <price>"
                        );
                        return;
                    }
                }

                BlockPos pos1 = PlotSelectionTool.getPosition1(player.getUUID());
                BlockPos pos2 = PlotSelectionTool.getPosition2(player.getUUID());

                if (pos1 == null || pos2 == null) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        "Keine Auswahl vorhanden!\n" +
                        "Benutze das Selection Tool um zwei Positionen zu markieren."
                    );
                    return;
                }

                // Erstelle Plot mit Namen und Typ
                PlotRegion plot = PlotManager.createPlot(pos1, pos2, name, type, price);
                PlotSelectionTool.clearSelection(player.getUUID());

                // Erfolgs-Nachricht basierend auf Typ
                String priceInfo = type.canBePurchased() ?
                    "\n§7Preis: §e" + String.format("%.2f", price) + "€" :
                    "\n§7Staatseigentum (nicht kaufbar)";

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ " + type.getDisplayName() + " erstellt!\n" +
                    "§7Name: §e" + plot.getPlotId() + "\n" +
                    "§7Typ: §b" + type.getDisplayName() +
                    priceInfo + "\n" +
                    "§7Größe: §e" + plot.getVolume() + " Blöcke"
                ), true);
            });
    }

    private static int setPlotOwner(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot setowner",
            player -> {
                ServerPlayer newOwner = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                plot.setOwner(newOwner.getUUID(), newOwner.getName().getString());
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Besitzer geändert!\n" +
                    "§7Plot: §e" + plot.getPlotName() + "\n" +
                    "§7Neuer Besitzer: §b" + newOwner.getName().getString()
                ), true);
            });
    }

    private static int buyPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot buy",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                executeBuyPlot(ctx, player, plot);
            });
    }

    private static int buyPlotById(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot buy <id>",
            player -> {
                String plotId = StringArgumentType.getString(ctx, "plotId");
                PlotRegion plot = PlotManager.getPlot(plotId);

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Plot nicht gefunden: " + plotId);
                    return;
                }

                executeBuyPlot(ctx, player, plot);
            });
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
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot info",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
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
            });
    }

    private static int setPlotName(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot name",
            player -> {
                String name = StringArgumentType.getString(ctx, "name");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                plot.setPlotName(name);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Plot-Name geändert!\n" +
                    "§7Neuer Name: §e" + name
                ), false);
            });
    }

    private static int setPlotDescription(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot description",
            player -> {
                String description = StringArgumentType.getString(ctx, "description");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                plot.setDescription(description);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Beschreibung geändert!\n" +
                    "§7Neue Beschreibung: §f" + description
                ), false);
            });
    }

    private static int trustPlayer(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot trust",
            player -> {
                ServerPlayer trustPlayer = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                if (plot.isTrusted(trustPlayer.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Spieler ist bereits berechtigt!");
                    return;
                }

                plot.addTrustedPlayer(trustPlayer.getUUID());
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Spieler berechtigt!\n" +
                    "§7Spieler: §b" + trustPlayer.getName().getString()
                ), false);
            });
    }

    private static int untrustPlayer(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot untrust",
            player -> {
                ServerPlayer untrustPlayer = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                if (!plot.isTrusted(untrustPlayer.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Spieler ist nicht berechtigt!");
                    return;
                }

                plot.removeTrustedPlayer(untrustPlayer.getUUID());
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Berechtigung entfernt!\n" +
                    "§7Spieler: §b" + untrustPlayer.getName().getString()
                ), false);
            });
    }

    private static int listTrusted(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot trustlist",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                List<String> trusted = new ArrayList<>(plot.getTrustedPlayers());

                if (trusted.isEmpty()) {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§7Keine berechtigten Spieler."
                    ), false);
                    return;
                }

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6═══ Berechtigte Spieler ═══"
                ), false);

                for (String uuidStr : trusted) {
                    ctx.getSource().sendSuccess(() -> Component.literal("§7• §b" + uuidStr), false);
                }
            });
    }

    private static int sellPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot sell",
            player -> {
                double price = DoubleArgumentType.getDouble(ctx, "price");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                plot.setForSale(true);
                plot.setSalePrice(price);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Plot zum Verkauf angeboten!\n" +
                    "§7Verkaufspreis: §e" + String.format("%.2f", price) + "€"
                ), false);
            });
    }

    private static int unsellPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot unsell",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                plot.setForSale(false);
                plot.setSalePrice(0);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Verkaufsangebot zurückgezogen!"
                ), false);
            });
    }

    private static int transferPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot transfer",
            player -> {
                ServerPlayer newOwner = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                plot.setOwner(newOwner.getUUID(), newOwner.getName().getString());
                plot.setForSale(false);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Plot übertragen!\n" +
                    "§7Neuer Besitzer: §b" + newOwner.getName().getString()
                ), false);
            });
    }

    private static int abandonPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot abandon",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
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
            });
    }

    private static int setForRent(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot rent",
            player -> {
                double pricePerDay = DoubleArgumentType.getDouble(ctx, "pricePerDay");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                plot.setForRent(true);
                plot.setRentPricePerDay(pricePerDay);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Plot zur Miete angeboten!\n" +
                    "§7Preis pro Tag: §e" + String.format("%.2f", pricePerDay) + "€"
                ), false);
            });
    }

    private static int cancelRent(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot rentcancel",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört dir nicht!");
                    return;
                }

                plot.setForRent(false);
                plot.setRentPricePerDay(0);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Mietangebot zurückgezogen!"
                ), false);
            });
    }
    
    private static int rentPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot rentplot",
            player -> {
                int days = IntegerArgumentType.getInteger(ctx, "days");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                executeRentPlot(ctx, player, plot, days);
            });
    }

    private static int rentPlotById(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot rentplot <id>",
            player -> {
                int days = IntegerArgumentType.getInteger(ctx, "days");
                String plotId = StringArgumentType.getString(ctx, "plotId");

                PlotRegion plot = PlotManager.getPlot(plotId);

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Plot nicht gefunden: " + plotId);
                    return;
                }

                executeRentPlot(ctx, player, plot, days);
            });
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
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot rentextend",
            player -> {
                int days = IntegerArgumentType.getInteger(ctx, "days");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                if (!plot.getRenterUUID().equals(player.getUUID().toString())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du mietest diesen Plot nicht!");
                    return;
                }

                double totalCost = plot.getRentPricePerDay() * days;

                if (EconomyManager.getBalance(player.getUUID()) < totalCost) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cNicht genug Geld!\n" +
                        "§7Kosten: §e" + String.format("%.2f", totalCost) + "€"
                    ));
                    return;
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
            });
    }

    // ✅ REMOVED: ratePlot() - Ersetzt durch PlotInfoScreen Rating-Buttons + PlotRatingPacket
    // ✅ REMOVED: topPlots() - Ersetzt durch PlotInfoScreen Rating-Anzeige

    private static int removePlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot remove",
            admin -> {
                PlotRegion plot = PlotManager.getPlotAt(admin.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst in keinem Plot!");
                    return;
                }

                String plotName = plot.getPlotName();
                String plotId = plot.getPlotId();

                PlotManager.removePlot(plotId);

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Plot entfernt!\n" +
                    "§7ID: §e" + plotId + "\n" +
                    "§7Name: §e" + plotName
                ), true);
            });
    }

    private static int reindexPlots(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeSourceCommand(ctx, "Fehler bei /plot reindex",
            source -> {
                PlotManager.rebuildSpatialIndex();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Spatial Index neu aufgebaut!\n" +
                    "§7Alle Plots wurden neu indiziert."
                ), true);
            });
    }

    private static int debugPosition(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot debug",
            player -> {
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
            });
    }

    // ═══════════════════════════════════════════════════════════
    // APARTMENT COMMANDS
    // ═══════════════════════════════════════════════════════════

    private static int apartmentWand(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment wand",
            player -> {
                ItemStack wand = new ItemStack(ModItems.PLOT_SELECTION_TOOL.get());

                if (player.getInventory().add(wand)) {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§d✓ Apartment-Auswahl-Werkzeug erhalten!\n" +
                        "§7Linksklick: §ePosition 1\n" +
                        "§7Rechtsklick auf Block: §ePosition 2\n" +
                        "§7Dann: §e/plot apartment create <name> <miete>"
                    ), false);
                } else {
                    CommandExecutor.sendFailure(ctx.getSource(), "Inventar ist voll!");
                }
            });
    }

    private static int createApartment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment create",
            player -> {
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
                    return;
                }

                // Prüfe ob Spieler auf einem Plot steht
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
                }

                // Prüfe ob Spieler der Besitzer ist
                if (!plot.canManage(player.getUUID())) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cDieser Plot gehört nicht dir!\n" +
                        "§7Nur der Besitzer kann Apartments erstellen."
                    ));
                    return;
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
                    CommandExecutor.sendFailure(ctx.getSource(), "Apartment muss komplett innerhalb deines Plots sein!");
                    return;
                }

                // Prüfe Überlappung mit anderen Apartments
                for (de.rolandsw.schedulemc.region.PlotArea existing : plot.getSubAreas()) {
                    if (existing.overlaps(min, max)) {
                        ctx.getSource().sendFailure(Component.literal(
                            "§cApartment überschneidet sich mit: §e" + existing.getName()
                        ));
                        return;
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
            });
    }

    private static int deleteApartment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment delete",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
                }

                if (!plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört nicht dir!");
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                        "§7Nutze /plot apartment list für verfügbare Apartments"
                    ));
                    return;
                }

                if (apartment.isRented()) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cApartment ist vermietet! Wirf zuerst den Mieter raus:\n" +
                        "§e/plot apartment evict " + apartmentId
                    ));
                    return;
                }

                String apartmentName = apartment.getName();
                plot.removeSubArea(apartmentId);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Apartment gelöscht: §e" + apartmentName
                ), false);
            });
    }

    private static int listApartments(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment list",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
                }

                List<de.rolandsw.schedulemc.region.PlotArea> apartments = plot.getSubAreas();

                if (apartments.isEmpty()) {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§7Dieser Plot hat keine Apartments.\n" +
                        "§7Erstelle ein Apartment mit:\n" +
                        "§e/plot apartment wand"
                    ), false);
                    return;
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
            });
    }

    private static int apartmentInfo(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment info",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                        "§7Nutze /plot apartment list für verfügbare Apartments"
                    ));
                    return;
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
            });
    }

    private static int rentApartment(CommandContext<CommandSourceStack> ctx) {
        return rentApartmentDays(ctx, 30); // Default: 30 Tage
    }

    private static int rentApartmentDays(CommandContext<CommandSourceStack> ctx) {
        int days = IntegerArgumentType.getInteger(ctx, "days");
        return rentApartmentDays(ctx, days);
    }

    private static int rentApartmentDays(CommandContext<CommandSourceStack> ctx, int days) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment rent",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                        "§7Nutze /plot apartment list für verfügbare Apartments"
                    ));
                    return;
                }

                // Prüfe ob Spieler der Plot-Besitzer ist
                if (plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du kannst nicht dein eigenes Apartment mieten!");
                    return;
                }

                if (!apartment.isForRent()) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieses Apartment wird nicht vermietet!");
                    return;
                }

                if (apartment.isRented()) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieses Apartment ist bereits vermietet!");
                    return;
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
                    return;
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
            });
    }

    private static int leaveApartment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment leave",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
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
                    CommandExecutor.sendFailure(ctx.getSource(), "Du hast kein Apartment in diesem Plot gemietet!");
                    return;
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
            });
    }

    private static int setApartmentRent(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment setrent",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");
                double monthlyRent = DoubleArgumentType.getDouble(ctx, "monthlyRent");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
                }

                if (!plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört nicht dir!");
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                        "§7Nutze /plot apartment list für verfügbare Apartments"
                    ));
                    return;
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
            });
    }

    private static int evictTenant(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot apartment evict",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst auf keinem Plot!");
                    return;
                }

                if (!plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieser Plot gehört nicht dir!");
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cApartment nicht gefunden: §e" + apartmentId + "\n" +
                        "§7Nutze /plot apartment list für verfügbare Apartments"
                    ));
                    return;
                }

                if (!apartment.isRented()) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Dieses Apartment ist nicht vermietet!");
                    return;
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
            });
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
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot settype",
            player -> {
                String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst nicht in einem Plot!");
                    return;
                }

                try {
                    de.rolandsw.schedulemc.region.PlotType type = de.rolandsw.schedulemc.region.PlotType.valueOf(typeStr);
                    plot.setType(type);
                    PlotManager.markDirty();

                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§a✓ Plot-Typ geändert!\n" +
                        "§7Neuer Typ: §e" + type.getDisplayName()
                    ), false);
                } catch (IllegalArgumentException e) {
                    ctx.getSource().sendFailure(Component.literal(
                        "§cUngültiger Plot-Typ!\n" +
                        "§7Verfügbar: §eRESIDENTIAL, COMMERCIAL, SHOP, PUBLIC, GOVERNMENT"
                    ));
                }
            });
    }

    /**
     * Findet die Position des Warehouse-Blocks, auf den der Spieler schaut
     */
    private static BlockPos findWarehouseBlockPos(ServerPlayer player) {
        // Zuerst: Prüfe Block, auf den der Spieler schaut (Raycast)
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(5.0)); // 5 Blöcke Reichweite

        BlockHitResult hitResult = player.level().clip(new ClipContext(
            eyePos, endPos,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockEntity be = player.level().getBlockEntity(hitResult.getBlockPos());
            if (be instanceof WarehouseBlockEntity) {
                return hitResult.getBlockPos();
            }
        }

        // Fallback: Prüfe Position unter dem Spieler
        BlockPos playerPos = player.blockPosition();
        BlockEntity be = player.level().getBlockEntity(playerPos.below());
        if (be instanceof WarehouseBlockEntity) {
            return playerPos.below();
        }

        // Prüfe Position des Spielers selbst
        be = player.level().getBlockEntity(playerPos);
        if (be instanceof WarehouseBlockEntity) {
            return playerPos;
        }

        return null;
    }

    private static int setWarehouseLocation(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot warehouse set",
            player -> {
                BlockPos warehousePos = findWarehouseBlockPos(player);
                if (warehousePos == null) {
                    ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
                    return;
                }

                PlotRegion plot = PlotManager.getPlotAt(warehousePos);
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Das Warehouse befindet sich nicht in einem Plot!");
                    return;
                }

                plot.setWarehouseLocation(warehousePos);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Warehouse-Position gesetzt!\n" +
                    "§7Plot: §e" + plot.getPlotId() + "\n" +
                    "§7Position: §f" + warehousePos.getX() + ", " + warehousePos.getY() + ", " + warehousePos.getZ()
                ), false);
            });
    }

    private static int clearWarehouseLocation(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot warehouse clear",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst nicht in einem Plot!");
                    return;
                }

                plot.setWarehouseLocation(null);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Warehouse-Position entfernt!\n" +
                    "§7Plot: §e" + plot.getPlotId()
                ), false);
            });
    }

    private static int warehouseInfo(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "Fehler bei /plot warehouse info",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), "Du stehst nicht in einem Plot!");
                    return;
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
            });
    }
}
