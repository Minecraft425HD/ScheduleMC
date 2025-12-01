package de.rolandsw.schedulemc.warehouse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

/**
 * Warehouse Command - Admin-only commands for warehouse management
 *
 * /warehouse info - Shows warehouse info at player position
 * /warehouse add <item> <amount> - Adds item to warehouse
 * /warehouse remove <item> <amount> - Removes item from warehouse
 * /warehouse clear - Clears all items
 * /warehouse setshop <shopId> - Links warehouse to shop
 */
public class WarehouseCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
            Commands.literal("warehouse")
                .requires(source -> source.hasPermission(2)) // Admin only

                .then(Commands.literal("info")
                    .executes(WarehouseCommand::showInfo)
                )

                .then(Commands.literal("add")
                    .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000))
                            .executes(WarehouseCommand::addItem)
                        )
                    )
                )

                .then(Commands.literal("remove")
                    .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000))
                            .executes(WarehouseCommand::removeItem)
                        )
                    )
                )

                .then(Commands.literal("clear")
                    .executes(WarehouseCommand::clearWarehouse)
                )

                .then(Commands.literal("setshop")
                    .then(Commands.argument("shopId", StringArgumentType.string())
                        .executes(WarehouseCommand::setShopId)
                    )
                )
        );
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition();
            Level level = player.level();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse an dieser Position!"));
                return 0;
            }

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§e§l=== Warehouse Info ===\n" +
                "§7Position: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "\n" +
                "§7Slots belegt: §e" + warehouse.getUsedSlots() + " §7/ §e" + warehouse.getSlots().length + "\n" +
                "§7Total Items: §e" + warehouse.getTotalItems() + "\n" +
                "§7Shop-ID: §e" + (warehouse.getShopId() != null ? warehouse.getShopId() : "Nicht verknüpft") + "\n" +
                "§7Verkäufer: §e" + warehouse.getLinkedSellers().size() + "\n" +
                "§7Letzter Delivery: §e" + (warehouse.getLastDeliveryTime() / 24000) + " Tage"
            ), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /warehouse info", e);
            return 0;
        }
    }

    private static int addItem(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition();
            Level level = player.level();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse an dieser Position!"));
                return 0;
            }

            ItemInput itemInput = ItemArgument.getItem(ctx, "item");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            int added = warehouse.addItem(itemInput.getItem().asItem(), amount);

            if (added > 0) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Items hinzugefügt!\n" +
                    "§7Item: §e" + itemInput.getItem().asItem().getDescription().getString() + "\n" +
                    "§7Menge: §e" + added + (added < amount ? " §7(Rest passte nicht)" : "")
                ), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cKein Platz im Warehouse!"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /warehouse add", e);
            return 0;
        }
    }

    private static int removeItem(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition();
            Level level = player.level();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse an dieser Position!"));
                return 0;
            }

            ItemInput itemInput = ItemArgument.getItem(ctx, "item");
            int amount = IntegerArgumentType.getInteger(ctx, "amount");

            int removed = warehouse.removeItem(itemInput.getItem().asItem(), amount);

            if (removed > 0) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Items entfernt!\n" +
                    "§7Item: §e" + itemInput.getItem().asItem().getDescription().getString() + "\n" +
                    "§7Menge: §e" + removed
                ), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("§cItem nicht im Warehouse gefunden!"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei /warehouse remove", e);
            return 0;
        }
    }

    private static int clearWarehouse(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition();
            Level level = player.level();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse an dieser Position!"));
                return 0;
            }

            warehouse.clearAll();

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Warehouse geleert!"
            ), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /warehouse clear", e);
            return 0;
        }
    }

    private static int setShopId(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition();
            Level level = player.level();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse an dieser Position!"));
                return 0;
            }

            String shopId = StringArgumentType.getString(ctx, "shopId");
            warehouse.setShopId(shopId);

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Shop-ID gesetzt!\n" +
                "§7Shop-ID: §e" + shopId
            ), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /warehouse setshop", e);
            return 0;
        }
    }
}
