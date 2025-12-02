package de.rolandsw.schedulemc.warehouse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

                .then(Commands.literal("deliver")
                    .executes(WarehouseCommand::manualDelivery)
                )

                .then(Commands.literal("reset")
                    .executes(WarehouseCommand::resetTimer)
                )
        );
    }

    /**
     * Findet das Warehouse, auf das der Spieler schaut oder an dem er steht
     */
    private static WarehouseBlockEntity findWarehouse(ServerPlayer player) {
        Level level = player.level();

        // Zuerst: Prüfe Block, auf den der Spieler schaut (Raycast)
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(5.0)); // 5 Blöcke Reichweite

        BlockHitResult hitResult = level.clip(new ClipContext(
            eyePos, endPos,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockEntity be = level.getBlockEntity(hitResult.getBlockPos());
            if (be instanceof WarehouseBlockEntity warehouse) {
                return warehouse;
            }
        }

        // Fallback: Prüfe Position unter dem Spieler
        BlockPos playerPos = player.blockPosition();
        BlockEntity be = level.getBlockEntity(playerPos.below());
        if (be instanceof WarehouseBlockEntity warehouse) {
            return warehouse;
        }

        // Prüfe Position des Spielers selbst
        be = level.getBlockEntity(playerPos);
        if (be instanceof WarehouseBlockEntity warehouse) {
            return warehouse;
        }

        return null;
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WarehouseBlockEntity warehouse = findWarehouse(player);

            if (warehouse == null) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
                return 0;
            }

            BlockPos pos = warehouse.getBlockPos();
            long currentDay = player.level().getDayTime() / 24000L;
            long lastDeliveryDay = warehouse.getLastDeliveryDay();
            long daysSinceLastDelivery = currentDay - lastDeliveryDay;
            long intervalDays = WarehouseConfig.DELIVERY_INTERVAL_DAYS.get();
            long daysUntilNext = (lastDeliveryDay + intervalDays) - currentDay;

            String nextDeliveryStr;
            if (daysUntilNext <= 0) {
                nextDeliveryStr = "§aÜBERFÄLLIG! Sollte jeden Moment erfolgen...";
            } else {
                nextDeliveryStr = "§e" + daysUntilNext + " Tage";
            }

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§e§l=== Warehouse Info ===\n" +
                "§7Position: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "\n" +
                "§7Slots belegt: §e" + warehouse.getUsedSlots() + " §7/ §e" + warehouse.getSlots().length + "\n" +
                "§7Total Items: §e" + warehouse.getTotalItems() + "\n" +
                "§7Shop-ID: §e" + (warehouse.getShopId() != null ? warehouse.getShopId() : "Nicht verknüpft") + "\n" +
                "§7Verkäufer: §e" + warehouse.getLinkedSellers().size() + "\n" +
                "§7\n" +
                "§6=== Lieferungs-Status (Tag-basiert wie NPCs!) ===\n" +
                "§7Aktueller Tag: §e" + currentDay + "\n" +
                "§7Letzte Lieferung: §eTag " + lastDeliveryDay + "\n" +
                "§7Tage seit letzter: §e" + daysSinceLastDelivery + " Tage\n" +
                "§7Lieferungs-Interval: §e" + intervalDays + " Tage\n" +
                "§7Nächste Lieferung in: " + nextDeliveryStr
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
            WarehouseBlockEntity warehouse = findWarehouse(player);

            if (warehouse == null) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
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
            WarehouseBlockEntity warehouse = findWarehouse(player);

            if (warehouse == null) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
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
            WarehouseBlockEntity warehouse = findWarehouse(player);

            if (warehouse == null) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
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
            WarehouseBlockEntity warehouse = findWarehouse(player);

            if (warehouse == null) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
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

    private static int manualDelivery(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WarehouseBlockEntity warehouse = findWarehouse(player);

            if (warehouse == null) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
                return 0;
            }

            long currentDay = player.level().getDayTime() / 24000L;
            long lastDeliveryDay = warehouse.getLastDeliveryDay();
            long daysSince = currentDay - lastDeliveryDay;

            LOGGER.info("Manual delivery triggered by {} at warehouse {}", player.getName().getString(), warehouse.getBlockPos());

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§e=== Manuelle Lieferung ausgelöst ===\n" +
                "§7Current Day: §e" + currentDay + "\n" +
                "§7Last Delivery Day: §e" + lastDeliveryDay + "\n" +
                "§7Days Since: §e" + daysSince + " Tage\n" +
                "§7Prüfe Server-Logs für Details..."
            ), false);

            // Rufe manuelle Lieferung auf
            warehouse.performManualDelivery(player.level());

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /warehouse deliver", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler: " + e.getMessage()));
            return 0;
        }
    }

    private static int resetTimer(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            WarehouseBlockEntity warehouse = findWarehouse(player);

            if (warehouse == null) {
                ctx.getSource().sendFailure(Component.literal("§cKein Warehouse gefunden! Schaue auf einen Warehouse-Block oder stehe direkt darauf."));
                return 0;
            }

            long currentDay = player.level().getDayTime() / 24000L;
            long oldLastDeliveryDay = warehouse.getLastDeliveryDay();
            long intervalDays = WarehouseConfig.DELIVERY_INTERVAL_DAYS.get();

            // Reset lastDeliveryDay to current day
            warehouse.setLastDeliveryDay(currentDay);
            warehouse.setChanged();
            warehouse.syncToClient();

            LOGGER.info("Warehouse delivery timer reset by {} at warehouse {} (old: Tag {}, new: Tag {})",
                player.getName().getString(), warehouse.getBlockPos(), oldLastDeliveryDay, currentDay);

            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Lieferungs-Timer zurückgesetzt!\n" +
                "§7Alter Tag: §eTag " + oldLastDeliveryDay + "\n" +
                "§7Neuer Tag: §eTag " + currentDay + "\n" +
                "§7Nächste Lieferung in " + intervalDays + " Tagen"
            ), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Fehler bei /warehouse reset", e);
            ctx.getSource().sendFailure(Component.literal("§cFehler: " + e.getMessage()));
            return 0;
        }
    }
}
