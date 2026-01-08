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
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.wand.error",
            player -> {
                ItemStack wand = new ItemStack(ModItems.PLOT_SELECTION_TOOL.get());

                if (player.getInventory().add(wand)) {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.wand.success"), false);
                } else {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.inventory_full").getString());
                }
            });
    }

    /**
     * Erstellt einen Plot mit spezifischem Typ
     */
    private static int createPlotWithType(CommandContext<CommandSourceStack> ctx, PlotType type) {
        return CommandExecutor.executePlayerCommand(ctx, Component.translatable("command.plot.create.error", type.name()).getString(),
            player -> {
                String name = StringArgumentType.getString(ctx, "name");

                // ✅ INPUT VALIDATION: Name validieren
                InputValidation.ValidationResult nameValidation = InputValidation.validateName(name);
                if (nameValidation.isFailure()) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        Component.translatable("command.plot.invalid_name", nameValidation.getErrorMessage()).getString()
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
                                Component.translatable("command.plot.invalid_price", priceValidation.getErrorMessage()).getString()
                            );
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        CommandExecutor.sendFailure(ctx.getSource(),
                            Component.translatable("command.plot.create.price_required", type.getDisplayName(), type.name().toLowerCase()).getString()
                        );
                        return;
                    }
                }

                BlockPos pos1 = PlotSelectionTool.getPosition1(player.getUUID());
                BlockPos pos2 = PlotSelectionTool.getPosition2(player.getUUID());

                if (pos1 == null || pos2 == null) {
                    CommandExecutor.sendFailure(ctx.getSource(),
                        Component.translatable("command.plot.no_selection").getString()
                    );
                    return;
                }

                // Erstelle Plot mit Namen und Typ
                PlotRegion plot = PlotManager.createPlot(pos1, pos2, name, type, price);
                PlotSelectionTool.clearSelection(player.getUUID());

                // Erfolgs-Nachricht basierend auf Typ
                Component priceInfo = type.canBePurchased() ?
                    Component.translatable("command.plot.create.price_label", String.format("%.2f", price)) :
                    Component.translatable("command.plot.create.government_owned");

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.create.success",
                    type.getDisplayName(), plot.getPlotId(), type.getDisplayName()).append("\n").append(priceInfo).append("\n")
                    .append(Component.translatable("command.plot.create.size", plot.getVolume()))
                ), true);
            });
    }

    private static int setPlotOwner(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.setowner.error",
            player -> {
                ServerPlayer newOwner = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                plot.setOwner(newOwner.getUUID(), newOwner.getName().getString());
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.setowner.success",
                    plot.getPlotName(), newOwner.getName().getString()
                ), true);
            });
    }

    private static int buyPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.buy.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                executeBuyPlot(ctx, player, plot);
            });
    }

    private static int buyPlotById(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.buy.error_id",
            player -> {
                String plotId = StringArgumentType.getString(ctx, "plotId");
                PlotRegion plot = PlotManager.getPlot(plotId);

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_found", plotId).getString());
                    return;
                }

                executeBuyPlot(ctx, player, plot);
            });
    }

    private static int executeBuyPlot(CommandContext<CommandSourceStack> ctx, ServerPlayer player, PlotRegion plot) {
        if (plot.hasOwner()) {
            ctx.getSource().sendFailure(Component.translatable("command.plot.buy.already_owned"));
            return 0;
        }

        double price = plot.getPrice();

        if (EconomyManager.getBalance(player.getUUID()) < price) {
            ctx.getSource().sendFailure(Component.translatable("command.plot.buy.not_enough_money",
                String.format("%.2f", price), String.format("%.2f", EconomyManager.getBalance(player.getUUID()))
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
        ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.buy.success",
            plotName, String.format("%.2f", finalPrice), String.format("%.2f", EconomyManager.getBalance(player.getUUID()))
        ), false);

        return 1;
    }
    
    private static int listPlots(CommandContext<CommandSourceStack> ctx) {
        List<PlotRegion> plots = PlotManager.getPlots();

        if (plots.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.plot.list.none"));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.list.header"), false);

        for (PlotRegion plot : plots) {
            Component status = plot.hasOwner() ? Component.translatable("command.plot.list.occupied") : Component.translatable("command.plot.list.free");
            Component price = plot.hasOwner() && plot.isForSale() ?
                Component.translatable("command.plot.list.for_sale", String.format("%.2f", plot.getSalePrice())) : Component.literal("");

            ctx.getSource().sendSuccess(() -> status.copy().append(" §e" + plot.getPlotName() +
                " §7(§f" + plot.getVolume() + " ").append(Component.translatable("command.plot.list.blocks")).append("§7)").append(price)
            , false);
        }

        return 1;
    }
    
    private static int plotInfo(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.info.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                Component ownerInfo = plot.hasOwner() ?
                    Component.translatable("command.plot.info.owner", plot.getOwnerName()) :
                    Component.translatable("command.plot.info.for_sale");

                Component priceInfo = plot.hasOwner() ?
                    (plot.isForSale() ? Component.translatable("command.plot.info.sale_price", String.format("%.2f", plot.getSalePrice())) : Component.literal("")) :
                    Component.translatable("command.plot.info.price", String.format("%.2f", plot.getPrice()));

                Component ratingInfo = plot.getRatingCount() > 0 ?
                    Component.translatable("command.plot.info.rating", plot.getRatingStars(), plot.getRatingCount()) : Component.literal("");

                Component description = plot.getDescription() != null && !plot.getDescription().isEmpty() ?
                    Component.literal("\n").append(Component.translatable("command.plot.info.description", plot.getDescription())) : Component.literal("");

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.info.header")
                    .append("\n").append(Component.translatable("command.plot.info.name", plot.getPlotName()))
                    .append("\n").append(Component.translatable("command.plot.info.id", plot.getPlotId()))
                    .append("\n").append(ownerInfo)
                    .append("\n").append(priceInfo)
                    .append("\n").append(ratingInfo)
                    .append(description)
                    .append("\n").append(Component.translatable("command.plot.info.size", plot.getVolume()))
                , false);
            });
    }

    private static int setPlotName(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.name.error",
            player -> {
                String name = StringArgumentType.getString(ctx, "name");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                plot.setPlotName(name);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.name.success", name), false);
            });
    }

    private static int setPlotDescription(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.description.error",
            player -> {
                String description = StringArgumentType.getString(ctx, "description");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                plot.setDescription(description);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.description.success", description), false);
            });
    }

    private static int trustPlayer(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.trust.error",
            player -> {
                ServerPlayer trustPlayer = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                if (plot.isTrusted(trustPlayer.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.trust.already_trusted").getString());
                    return;
                }

                plot.addTrustedPlayer(trustPlayer.getUUID());
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.trust.success",
                    trustPlayer.getName().getString()
                ), false);
            });
    }

    private static int untrustPlayer(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.untrust.error",
            player -> {
                ServerPlayer untrustPlayer = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                if (!plot.isTrusted(untrustPlayer.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.untrust.not_trusted").getString());
                    return;
                }

                plot.removeTrustedPlayer(untrustPlayer.getUUID());
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.untrust.success",
                    untrustPlayer.getName().getString()
                ), false);
            });
    }

    private static int listTrusted(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.trustlist.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                List<String> trusted = new ArrayList<>(plot.getTrustedPlayers());

                if (trusted.isEmpty()) {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.trustlist.none"), false);
                    return;
                }

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.trustlist.header"), false);

                for (String uuidStr : trusted) {
                    ctx.getSource().sendSuccess(() -> Component.literal("§7• §b" + uuidStr), false);
                }
            });
    }

    private static int sellPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.sell.error",
            player -> {
                double price = DoubleArgumentType.getDouble(ctx, "price");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                plot.setForSale(true);
                plot.setSalePrice(price);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.sell.success",
                    String.format("%.2f", price)
                ), false);
            });
    }

    private static int unsellPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.unsell.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                plot.setForSale(false);
                plot.setSalePrice(0);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.unsell.success"), false);
            });
    }

    private static int transferPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.transfer.error",
            player -> {
                ServerPlayer newOwner = EntityArgument.getPlayer(ctx, "player");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                plot.setOwner(newOwner.getUUID(), newOwner.getName().getString());
                plot.setForSale(false);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.transfer.success",
                    newOwner.getName().getString()
                ), false);
            });
    }

    private static int abandonPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.abandon.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
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

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.abandon.success",
                    String.format("%.2f", refund), String.format("%.2f", EconomyManager.getBalance(player.getUUID()))
                ), false);
            });
    }

    private static int setForRent(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.rent.error",
            player -> {
                double pricePerDay = DoubleArgumentType.getDouble(ctx, "pricePerDay");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                plot.setForRent(true);
                plot.setRentPricePerDay(pricePerDay);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.rent.success",
                    String.format("%.2f", pricePerDay)
                ), false);
            });
    }

    private static int cancelRent(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.rentcancel.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.isOwnedBy(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                plot.setForRent(false);
                plot.setRentPricePerDay(0);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.rentcancel.success"), false);
            });
    }

    private static int rentPlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.rentplot.error",
            player -> {
                int days = IntegerArgumentType.getInteger(ctx, "days");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                executeRentPlot(ctx, player, plot, days);
            });
    }

    private static int rentPlotById(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.rentplot.error_id",
            player -> {
                int days = IntegerArgumentType.getInteger(ctx, "days");
                String plotId = StringArgumentType.getString(ctx, "plotId");

                PlotRegion plot = PlotManager.getPlot(plotId);

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_found", plotId).getString());
                    return;
                }

                executeRentPlot(ctx, player, plot, days);
            });
    }

    private static int executeRentPlot(CommandContext<CommandSourceStack> ctx, ServerPlayer player, PlotRegion plot, int days) {
        if (!plot.isForRent() || plot.isRented()) {
            ctx.getSource().sendFailure(Component.translatable("command.plot.rentplot.not_available"));
            return 0;
        }

        double totalCost = plot.getRentPricePerDay() * days;

        if (EconomyManager.getBalance(player.getUUID()) < totalCost) {
            ctx.getSource().sendFailure(Component.translatable("command.plot.rentplot.not_enough_money",
                String.format("%.2f", totalCost)
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
        ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.rentplot.success",
            finalDays, String.format("%.2f", finalTotalCost)
        ), false);

        return 1;
    }
    
    private static int extendRent(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.rentextend.error",
            player -> {
                int days = IntegerArgumentType.getInteger(ctx, "days");
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                if (!plot.getRenterUUID().equals(player.getUUID().toString())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.rentextend.not_renting").getString());
                    return;
                }

                double totalCost = plot.getRentPricePerDay() * days;

                if (EconomyManager.getBalance(player.getUUID()) < totalCost) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.rentextend.not_enough_money",
                        String.format("%.2f", totalCost)
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

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.rentextend.success", days), false);
            });
    }

    // ✅ REMOVED: ratePlot() - Ersetzt durch PlotInfoScreen Rating-Buttons + PlotRatingPacket
    // ✅ REMOVED: topPlots() - Ersetzt durch PlotInfoScreen Rating-Anzeige

    private static int removePlot(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.remove.error",
            admin -> {
                PlotRegion plot = PlotManager.getPlotAt(admin.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot").getString());
                    return;
                }

                String plotName = plot.getPlotName();
                String plotId = plot.getPlotId();

                PlotManager.removePlot(plotId);

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.remove.success",
                    plotId, plotName
                ), true);
            });
    }

    private static int reindexPlots(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executeSourceCommand(ctx, "command.plot.reindex.error",
            source -> {
                PlotManager.rebuildSpatialIndex();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.reindex.success"), true);
            });
    }

    private static int debugPosition(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.debug.error",
            player -> {
                BlockPos pos = player.blockPosition();
                PlotRegion plot = PlotManager.getPlotAt(pos);

                Component plotInfo = plot != null ?
                    Component.translatable("command.plot.debug.plot_found", plot.getPlotId(), plot.getPlotName()) :
                    Component.translatable("command.plot.debug.no_plot");

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.debug.header")
                    .append("\n").append(Component.translatable("command.plot.debug.position", pos.getX(), pos.getY(), pos.getZ()))
                    .append("\n").append(plotInfo)
                    .append("\n").append(Component.translatable("command.plot.debug.total_plots", PlotManager.getPlotCount()))
                , false);
            });
    }

    // ═══════════════════════════════════════════════════════════
    // APARTMENT COMMANDS
    // ═══════════════════════════════════════════════════════════

    private static int apartmentWand(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.wand.error",
            player -> {
                ItemStack wand = new ItemStack(ModItems.PLOT_SELECTION_TOOL.get());

                if (player.getInventory().add(wand)) {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.wand.success"), false);
                } else {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.inventory_full").getString());
                }
            });
    }

    private static int createApartment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.create.error",
            player -> {
                String name = StringArgumentType.getString(ctx, "name");
                double monthlyRent = DoubleArgumentType.getDouble(ctx, "monthlyRent");

                // Hole Selection
                BlockPos pos1 = de.rolandsw.schedulemc.items.PlotSelectionTool.getPosition1(player.getUUID());
                BlockPos pos2 = de.rolandsw.schedulemc.items.PlotSelectionTool.getPosition2(player.getUUID());

                if (pos1 == null || pos2 == null) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.create.no_selection"));
                    return;
                }

                // Prüfe ob Spieler auf einem Plot steht
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
                    return;
                }

                // Prüfe ob Spieler der Besitzer ist
                if (!plot.canManage(player.getUUID())) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.create.not_owner"));
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
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.apartment.create.outside_plot").getString());
                    return;
                }

                // Prüfe Überlappung mit anderen Apartments
                for (de.rolandsw.schedulemc.region.PlotArea existing : plot.getSubAreas()) {
                    if (existing.overlaps(min, max)) {
                        ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.create.overlap", existing.getName()));
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

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.create.success",
                    apartmentId, name, monthlyRent, apartment.getVolume()
                ), false);
            });
    }

    private static int deleteApartment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.delete.error",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
                    return;
                }

                if (!plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.not_found", apartmentId));
                    return;
                }

                if (apartment.isRented()) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.delete.is_rented", apartmentId));
                    return;
                }

                String apartmentName = apartment.getName();
                plot.removeSubArea(apartmentId);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.delete.success", apartmentName), false);
            });
    }

    private static int listApartments(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.list.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
                    return;
                }

                List<de.rolandsw.schedulemc.region.PlotArea> apartments = plot.getSubAreas();

                if (apartments.isEmpty()) {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.list.none"), false);
                    return;
                }

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.list.header", plot.getPlotName()), false);

                for (de.rolandsw.schedulemc.region.PlotArea apt : apartments) {
                    ctx.getSource().sendSuccess(() -> Component.literal("\n§e" + apt.getName() + " §7(§e" + apt.getId() + "§7)"), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.list.rent", apt.getMonthlyRent()), false);

                    if (apt.isRented()) {
                        long daysLeft = apt.getRentDaysLeft();
                        ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.list.rented", daysLeft), false);
                    } else if (apt.isForRent()) {
                        ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.list.available", apt.getId()), false);
                    } else {
                        ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.list.not_for_rent"), false);
                    }

                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.list.size", apt.getVolume()), false);
                }
            });
    }

    private static int apartmentInfo(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.info.error",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.not_found", apartmentId));
                    return;
                }

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.header", apartment.getName()), false);
                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.id", apartment.getId()), false);
                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.rent", apartment.getMonthlyRent()), false);
                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.size", apartment.getVolume()), false);

                if (apartment.isRented()) {
                    long days = apartment.getRentDaysLeft();
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.rented"), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.time_left", days), false);
                } else if (apartment.isForRent()) {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.for_rent"), false);
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.rent_command", apartment.getId()), false);
                } else {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.info.not_for_rent"), false);
                }
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
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.rent.error",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.not_found", apartmentId));
                    return;
                }

                // Prüfe ob Spieler der Plot-Besitzer ist
                if (plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.apartment.rent.cannot_rent_own").getString());
                    return;
                }

                if (!apartment.isForRent()) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.apartment.rent.not_for_rent").getString());
                    return;
                }

                if (apartment.isRented()) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.apartment.rent.already_rented").getString());
                    return;
                }

                // Berechne Kosten: Monatliche Miete * (Tage / 30) + Kaution (3x Monatsmiete)
                double monthlyCost = apartment.getMonthlyRent();
                double rentCost = (monthlyCost / 30.0) * days;
                double deposit = monthlyCost * 3.0;  // 3x Monatsmiete als Kaution
                double totalCost = rentCost + deposit;

                if (!EconomyManager.withdraw(player.getUUID(), totalCost)) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.rent.not_enough_money",
                        String.format("%.2f", totalCost), String.format("%.2f", rentCost), String.format("%.2f", deposit)
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
                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.rent.success",
                    apartmentName, finalDays, String.format("%.2f", finalTotalCost), String.format("%.2f", finalDeposit)
                ), false);
            });
    }

    private static int leaveApartment(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.leave.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
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
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.apartment.leave.no_rental").getString());
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
                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.leave.success",
                    apartmentName, String.format("%.2f", finalDeposit)
                ), false);
            });
    }

    private static int setApartmentRent(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.setrent.error",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");
                double monthlyRent = DoubleArgumentType.getDouble(ctx, "monthlyRent");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
                    return;
                }

                if (!plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.not_found", apartmentId));
                    return;
                }

                String apartmentName = apartment.getName();
                apartment.setMonthlyRent(monthlyRent);
                PlotManager.markDirty();

                final double finalMonthlyRent = monthlyRent;
                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.setrent.success",
                    apartmentName, finalMonthlyRent
                ), false);
            });
    }

    private static int evictTenant(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.apartment.evict.error",
            player -> {
                String apartmentId = StringArgumentType.getString(ctx, "apartmentId");

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());

                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_on_plot").getString());
                    return;
                }

                if (!plot.canManage(player.getUUID())) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_your_plot").getString());
                    return;
                }

                de.rolandsw.schedulemc.region.PlotArea apartment = findApartment(plot, apartmentId);

                if (apartment == null) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.apartment.not_found", apartmentId));
                    return;
                }

                if (!apartment.isRented()) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.apartment.evict.not_rented").getString());
                    return;
                }

                // KEINE Kaution zurück bei Rauswurf
                String apartmentName = apartment.getName();
                apartment.endRent();
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.apartment.evict.success",
                    apartmentName
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
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.settype.error",
            player -> {
                String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();

                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot_admin").getString());
                    return;
                }

                try {
                    de.rolandsw.schedulemc.region.PlotType type = de.rolandsw.schedulemc.region.PlotType.valueOf(typeStr);
                    plot.setType(type);
                    PlotManager.markDirty();

                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.settype.success",
                        type.getDisplayName()
                    ), false);
                } catch (IllegalArgumentException e) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.settype.invalid"));
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
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.warehouse.set.error",
            player -> {
                BlockPos warehousePos = findWarehouseBlockPos(player);
                if (warehousePos == null) {
                    ctx.getSource().sendFailure(Component.translatable("command.plot.warehouse.set.not_found"));
                    return;
                }

                PlotRegion plot = PlotManager.getPlotAt(warehousePos);
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.warehouse.set.not_in_plot").getString());
                    return;
                }

                plot.setWarehouseLocation(warehousePos);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.warehouse.set.success",
                    plot.getPlotId(), warehousePos.getX(), warehousePos.getY(), warehousePos.getZ()
                ), false);
            });
    }

    private static int clearWarehouseLocation(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.warehouse.clear.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot_admin").getString());
                    return;
                }

                plot.setWarehouseLocation(null);
                PlotManager.markDirty();

                ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.warehouse.clear.success",
                    plot.getPlotId()
                ), false);
            });
    }

    private static int warehouseInfo(CommandContext<CommandSourceStack> ctx) {
        return CommandExecutor.executePlayerCommand(ctx, "command.plot.warehouse.info.error",
            player -> {
                PlotRegion plot = PlotManager.getPlotAt(player.blockPosition());
                if (plot == null) {
                    CommandExecutor.sendFailure(ctx.getSource(), Component.translatable("command.plot.not_in_plot_admin").getString());
                    return;
                }

                BlockPos warehousePos = plot.getWarehouseLocation();
                if (warehousePos == null) {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.warehouse.info.no_warehouse",
                        plot.getPlotId()
                    ), false);
                } else {
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.plot.warehouse.info.success",
                        plot.getPlotId(), warehousePos.getX(), warehousePos.getY(), warehousePos.getZ()
                    ), false);
                }
            });
    }
}
