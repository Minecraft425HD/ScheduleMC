package de.rolandsw.schedulemc.npc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.items.NPCLocationTool;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

/**
 * Command für NPC-Verwaltung
 * /npc <name> info - Zeigt Informationen über NPC
 * /npc <name> movement <true|false> - Aktiviert/Deaktiviert Bewegung
 * /npc <name> speed <value> - Setzt Bewegungsgeschwindigkeit
 * /npc <name> schedule workstart <time> - Setzt Arbeitsbeginn (Format: HHMM, z.B. 0700)
 * /npc <name> schedule workend <time> - Setzt Arbeitsende (Format: HHMM, z.B. 1800)
 * /npc <name> schedule home <time> - Setzt Heimzeit (Format: HHMM, z.B. 2300)
 * /npc <name> leisure add - Fügt aktuelle Position als Freizeitort hinzu
 * /npc <name> leisure remove <index> - Entfernt Freizeitort
 * /npc <name> leisure list - Listet alle Freizeitorte auf
 * /npc <name> leisure clear - Löscht alle Freizeitorte
 * /npc <name> inventory - Zeigt das Inventar des NPCs
 * /npc <name> inventory give <slot> <item> - Gibt dem NPC ein Item
 * /npc <name> inventory clear [slot] - Löscht Inventar
 * /npc <name> wallet - Zeigt die Geldbörse des NPCs
 * /npc <name> wallet set <amount> - Setzt die Geldbörse
 * /npc <name> wallet add <amount> - Fügt Geld hinzu
 * /npc <name> wallet remove <amount> - Entfernt Geld
 */
public class NPCCommand {

    /**
     * Tab-Completion für NPC-Namen
     */
    private static final SuggestionProvider<CommandSourceStack> NPC_NAME_SUGGESTIONS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(
            NPCNameRegistry.getAllNamesSorted(),
            builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
            Commands.literal("npc")
                .requires(source -> source.hasPermission(2)) // Nur Admins
                .then(Commands.argument("npcName", StringArgumentType.string())
                    .suggests(NPC_NAME_SUGGESTIONS)
                    .then(Commands.literal("info")
                        .executes(NPCCommand::showInfo)
                    )
                    .then(Commands.literal("movement")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                            .executes(NPCCommand::setMovement)
                        )
                    )
                    .then(Commands.literal("speed")
                        .then(Commands.argument("speed", FloatArgumentType.floatArg(0.1f, 1.0f))
                            .executes(NPCCommand::setSpeed)
                        )
                    )
                    .then(Commands.literal("schedule")
                        .then(Commands.literal("workstart")
                            .then(Commands.argument("time", StringArgumentType.word())
                                .executes(NPCCommand::setWorkStartTime)
                            )
                        )
                        .then(Commands.literal("workend")
                            .then(Commands.argument("time", StringArgumentType.word())
                                .executes(NPCCommand::setWorkEndTime)
                            )
                        )
                        .then(Commands.literal("home")
                            .then(Commands.argument("time", StringArgumentType.word())
                                .executes(NPCCommand::setHomeTime)
                            )
                        )
                    )
                    .then(Commands.literal("leisure")
                        .then(Commands.literal("add")
                            .executes(NPCCommand::addLeisureLocation)
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("index", IntegerArgumentType.integer(0, 9))
                                .executes(NPCCommand::removeLeisureLocation)
                            )
                        )
                        .then(Commands.literal("list")
                            .executes(NPCCommand::listLeisureLocations)
                        )
                        .then(Commands.literal("clear")
                            .executes(NPCCommand::clearLeisureLocations)
                        )
                    )
                    .then(Commands.literal("inventory")
                        .executes(NPCCommand::showInventory)
                        .then(Commands.literal("give")
                            .then(Commands.argument("slot", IntegerArgumentType.integer(0, 8))
                                .then(Commands.argument("item", ItemArgument.item(buildContext))
                                    .executes(NPCCommand::giveInventoryItem)
                                )
                            )
                        )
                        .then(Commands.literal("clear")
                            .executes(NPCCommand::clearInventory)
                            .then(Commands.argument("slot", IntegerArgumentType.integer(0, 8))
                                .executes(NPCCommand::clearInventorySlot)
                            )
                        )
                    )
                    .then(Commands.literal("wallet")
                        .executes(NPCCommand::showWallet)
                        .then(Commands.literal("set")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(NPCCommand::setWallet)
                            )
                        )
                        .then(Commands.literal("add")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(NPCCommand::addWallet)
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(NPCCommand::removeWallet)
                            )
                        )
                    )
                    .then(Commands.literal("warehouse")
                        .then(Commands.literal("set")
                            .executes(NPCCommand::setWarehouse)
                        )
                        .then(Commands.literal("clear")
                            .executes(NPCCommand::clearWarehouse)
                        )
                        .then(Commands.literal("info")
                            .executes(NPCCommand::warehouseInfo)
                        )
                    )
                )
        );
    }

    private static int setMovement(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        ServerLevel level = context.getSource().getLevel();

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        npc.getNpcData().getBehavior().setCanMove(enabled);

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.movement_prefix").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(enabled ? "aktiviert" : "deaktiviert")
                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int setSpeed(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        float speed = FloatArgumentType.getFloat(context, "speed");
        ServerLevel level = context.getSource().getLevel();

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        npc.getNpcData().getBehavior().setMovementSpeed(speed);

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.speed_set_to").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(String.format("%.2f", speed))
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int showInfo(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        var data = npc.getNpcData();
        var behavior = data.getBehavior();

        player.sendSystemMessage(Component.translatable("message.npc.command_info_header").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(
            Component.translatable("message.common.name_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.translatable("message.common.type_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getNpcType().toString())
                    .withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.translatable("message.npc.movement_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(behavior.canMove() ? "Aktiviert" : "Deaktiviert")
                    .withStyle(behavior.canMove() ? ChatFormatting.GREEN : ChatFormatting.RED))
        );
        player.sendSystemMessage(
            Component.translatable("message.npc.speed_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f", behavior.getMovementSpeed()))
                    .withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.translatable("message.npc.home_label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getHomeLocation() != null ?
                    data.getHomeLocation().toShortString() : "Nicht gesetzt")
                    .withStyle(data.getHomeLocation() != null ? ChatFormatting.GREEN : ChatFormatting.RED))
        );

        // Arbeitsort nur für Verkäufer anzeigen
        if (data.getNpcType() == NPCType.VERKAEUFER) {
            player.sendSystemMessage(
                Component.translatable("message.npc.workplace_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getWorkLocation() != null ?
                        data.getWorkLocation().toShortString() : "Nicht gesetzt")
                        .withStyle(data.getWorkLocation() != null ? ChatFormatting.GREEN : ChatFormatting.RED))
            );
        } else if (data.getNpcType() == NPCType.BEWOHNER) {
            player.sendSystemMessage(
                Component.translatable("message.npc.workplace_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("message.npc.residents_dont_work")
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_locations_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getLeisureLocations().size() + "/10")
                        .withStyle(ChatFormatting.WHITE))
            );
        }

        // Schedule Zeiten - unterschiedlich je nach NPC-Typ
        player.sendSystemMessage(Component.translatable("message.npc.schedule_header").withStyle(ChatFormatting.GOLD));

        if (data.getNpcType() == NPCType.VERKAEUFER) {
            // Verkäufer: Vollständiger Zeitplan
            player.sendSystemMessage(
                Component.translatable("message.npc.work_start_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkStartTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.translatable("message.npc.work_end_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkEndTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.translatable("message.npc.home_time_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("message.common.from_time", ticksToTime(data.getHomeTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
        } else if (data.getNpcType() == NPCType.BEWOHNER) {
            // Bewohner: Nur Heimzeit (Schlafenszeit)
            String homeStart = ticksToTime(data.getHomeTime());
            String homeEnd = ticksToTime(data.getWorkStartTime()); // Aufstehzeit
            player.sendSystemMessage(
                Component.translatable("message.npc.sleep_time_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(homeStart + " - " + homeEnd)
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(homeEnd + " - " + homeStart + " (Aktiv in der Stadt)")
                        .withStyle(ChatFormatting.GREEN))
            );
        } else {
            // Polizei oder andere: Alte Anzeige
            player.sendSystemMessage(
                Component.translatable("message.npc.work_start_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkStartTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.translatable("message.npc.work_end_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkEndTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.translatable("message.npc.home_time_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getHomeTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
        }

        // Freizeitorte
        player.sendSystemMessage(Component.translatable("message.npc.leisure_header").withStyle(ChatFormatting.GOLD));
        var leisureLocations = data.getLeisureLocations();
        if (leisureLocations.isEmpty()) {
            player.sendSystemMessage(
                Component.translatable("message.npc.no_leisure_defined").withStyle(ChatFormatting.GRAY)
            );
        } else {
            for (int i = 0; i < leisureLocations.size(); i++) {
                player.sendSystemMessage(
                    Component.literal("[").append(Component.literal(String.valueOf(i))).append(Component.literal("] "))
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(leisureLocations.get(i).toShortString())
                            .withStyle(ChatFormatting.YELLOW))
                );
            }
        }

        // Inventar und Geldbörse (nur für Bewohner und Verkäufer)
        if (data.hasInventoryAndWallet()) {
            player.sendSystemMessage(Component.translatable("message.npc.inventory_wallet_header").withStyle(ChatFormatting.GOLD));

            // Inventar
            var inventory = data.getInventory();
            int itemCount = 0;
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    itemCount++;
                }
            }
            player.sendSystemMessage(
                Component.translatable("message.npc.inventory_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(itemCount + "/9 Slots belegt")
                        .withStyle(itemCount > 0 ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
            );

            // Geldbörse
            player.sendSystemMessage(
                Component.translatable("message.npc.wallet_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getWallet() + " Bargeld")
                        .withStyle(ChatFormatting.GOLD))
            );
        }

        return 1;
    }

    private static int setWorkStartTime(CommandContext<CommandSourceStack> context) {
        return setScheduleTime(context, "workstart");
    }

    private static int setWorkEndTime(CommandContext<CommandSourceStack> context) {
        return setScheduleTime(context, "workend");
    }

    private static int setHomeTime(CommandContext<CommandSourceStack> context) {
        return setScheduleTime(context, "home");
    }

    private static int setScheduleTime(CommandContext<CommandSourceStack> context, String timeType) {
        String npcName = StringArgumentType.getString(context, "npcName");
        String timeInput = StringArgumentType.getString(context, "time");
        ServerLevel level = context.getSource().getLevel();

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        // Parse Zeit (nur HHMM Format - 4 Ziffern)
        long ticks;
        try {
            if (timeInput.length() != 4) {
                context.getSource().sendFailure(
                    Component.translatable("message.common.invalid_time_format").withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            int hours = Integer.parseInt(timeInput.substring(0, 2));
            int minutes = Integer.parseInt(timeInput.substring(2, 4));

            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                context.getSource().sendFailure(
                    Component.translatable("message.common.invalid_time_range").withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            ticks = timeToTicks(hours, minutes);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            context.getSource().sendFailure(
                Component.translatable("message.common.invalid_time_format").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        // Setze die Zeit
        switch (timeType) {
            case "workstart" -> npc.getNpcData().setWorkStartTime(ticks);
            case "workend" -> npc.getNpcData().setWorkEndTime(ticks);
            case "home" -> npc.getNpcData().setHomeTime(ticks);
        }

        String timeName = switch (timeType) {
            case "workstart" -> "Arbeitsbeginn";
            case "workend" -> "Arbeitsende";
            case "home" -> "Heimzeit";
            default -> "Zeit";
        };

        context.getSource().sendSuccess(
            () -> Component.literal(timeName + " gesetzt auf ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(timeInput)
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int addLeisureLocation(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (npc.getNpcData().getLeisureLocations().size() >= 10) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.leisure_max_reached").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        BlockPos playerPos = player.blockPosition();
        npc.getNpcData().addLeisureLocation(playerPos);

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.leisure_added").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(playerPos.toShortString())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int removeLeisureLocation(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (index >= npc.getNpcData().getLeisureLocations().size()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.invalid_leisure_index").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        BlockPos removed = npc.getNpcData().getLeisureLocations().get(index);
        npc.getNpcData().removeLeisureLocation(index);

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.leisure_removed_label").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(removed.toShortString())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int listLeisureLocations(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        var leisureLocations = npc.getNpcData().getLeisureLocations();

        player.sendSystemMessage(
            Component.translatable("message.npc.leisure_of", npc.getNpcName())
                .withStyle(ChatFormatting.GOLD)
        );

        if (leisureLocations.isEmpty()) {
            player.sendSystemMessage(
                Component.translatable("message.npc.no_leisure_defined").withStyle(ChatFormatting.GRAY)
            );
        } else {
            for (int i = 0; i < leisureLocations.size(); i++) {
                BlockPos pos = leisureLocations.get(i);
                player.sendSystemMessage(
                    Component.literal("[").append(Component.literal(String.valueOf(i))).append(Component.literal("] "))
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(pos.toShortString())
                            .withStyle(ChatFormatting.YELLOW))
                );
            }
        }

        return 1;
    }

    private static int clearLeisureLocations(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        npc.getNpcData().clearLeisureLocations();

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.all_leisure_removed").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    // ===== INVENTAR COMMANDS =====

    private static int showInventory(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_inventory").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        player.sendSystemMessage(
            Component.translatable("message.npc.inventory_of", npc.getNpcName())
                .withStyle(ChatFormatting.GOLD)
        );

        var inventory = npc.getNpcData().getInventory();
        boolean isEmpty = true;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                isEmpty = false;
                player.sendSystemMessage(
                    Component.literal("[").append(Component.literal(String.valueOf(i))).append(Component.literal("] "))
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(stack.getCount() + "x ")
                            .withStyle(ChatFormatting.YELLOW))
                        .append(stack.getDisplayName())
                );
            }
        }

        if (isEmpty) {
            player.sendSystemMessage(
                Component.translatable("message.npc.inventory_empty").withStyle(ChatFormatting.GRAY)
            );
        }

        return 1;
    }

    private static int giveInventoryItem(CommandContext<CommandSourceStack> context) {
        int slot = IntegerArgumentType.getInteger(context, "slot");
        ItemInput itemInput = context.getArgument("item", ItemInput.class);
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_inventory").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        try {
            ItemStack stack = itemInput.createItemStack(1, false);
            npc.getNpcData().setInventoryItem(slot, stack);

            context.getSource().sendSuccess(
                () -> Component.translatable("message.npc.item_added_slot", slot)
                    .withStyle(ChatFormatting.GREEN)
                    .append(stack.getDisplayName())
                    .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW)),
                false
            );

            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.invalid_item_error", e.getMessage())
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }
    }

    private static int clearInventory(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_inventory").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        var inventory = npc.getNpcData().getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.inventory_cleared").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int clearInventorySlot(CommandContext<CommandSourceStack> context) {
        int slot = IntegerArgumentType.getInteger(context, "slot");
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_inventory").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().setInventoryItem(slot, ItemStack.EMPTY);

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.slot_cleared", slot)
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    // ===== WALLET COMMANDS =====

    private static int showWallet(CommandContext<CommandSourceStack> context) {
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_wallet").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        int wallet = npc.getNpcData().getWallet();

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.wallet_of").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(": ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(wallet + " Bargeld")
                    .withStyle(ChatFormatting.GOLD)),
            false
        );

        return 1;
    }

    private static int setWallet(CommandContext<CommandSourceStack> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_wallet").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().setWallet(amount);

        // Performance-Optimierung: Sync nur Wallet statt Full NPC Data
        npc.syncWalletToClient();

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.wallet_set_to").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(amount + " Bargeld")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int addWallet(CommandContext<CommandSourceStack> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_wallet").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().addMoney(amount);

        // Performance-Optimierung: Sync nur Wallet statt Full NPC Data
        npc.syncWalletToClient();

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.cash_added", amount)
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcData().getWallet() + " Bargeld")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int removeWallet(CommandContext<CommandSourceStack> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        String npcName = StringArgumentType.getString(context, "npcName");
        ServerLevel level = context.getSource().getLevel();
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.translatable("message.common.player_only"));
            return 0;
        }

        CustomNPCEntity npc = getNPCByName(npcName, level);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_found_prefix").withStyle(ChatFormatting.RED)
                    .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.npc.not_found_suffix").withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.no_wallet").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        boolean success = npc.getNpcData().removeMoney(amount);

        // Performance-Optimierung: Sync nur Wallet statt Full NPC Data (nur bei Erfolg)
        if (success) {
            npc.syncWalletToClient();
        }

        if (!success) {
            context.getSource().sendFailure(
                Component.translatable("message.npc.not_enough_money_wallet", npc.getNpcData().getWallet())
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        context.getSource().sendSuccess(
            () -> Component.translatable("message.npc.cash_removed", amount)
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcData().getWallet() + " Bargeld")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("message.common.for_npc").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    /**
     * Konvertiert Stunden und Minuten zu Minecraft-Ticks
     * Minecraft Zeit: 0 Ticks = 6:00 Uhr morgens
     * 1 Stunde = 1000 Ticks, 1 Tag = 24000 Ticks
     */
    private static long timeToTicks(int hours, int minutes) {
        // Berechne Gesamtminuten seit Mitternacht
        int totalMinutes = hours * 60 + minutes;

        // Minecraft Offset: 0 Ticks = 6:00 Uhr (360 Minuten seit Mitternacht)
        // Verwende double für präzise Berechnung
        long ticks = (long) ((totalMinutes - 360) * (1000.0 / 60.0));

        // Normalisiere zu 0-24000 (ein Minecraft-Tag)
        while (ticks < 0) {
            ticks += 24000;
        }
        ticks = ticks % 24000;

        return ticks;
    }

    /**
     * Konvertiert Minecraft-Ticks zu HHMM Format (4-Ziffern ohne Doppelpunkt)
     */
    private static String ticksToTime(long ticks) {
        // 0 Ticks = 6:00 Uhr morgens
        // Verwende double für präzise Berechnung
        int totalMinutes = (int) ((ticks * (60.0 / 1000.0)) + 360);

        int hours = (totalMinutes / 60) % 24;
        int minutes = totalMinutes % 60;

        return String.format("%02d%02d", hours, minutes);
    }

    /**
     * Gibt den mit dem LocationTool ausgewählten NPC zurück, oder den nächsten NPC
     */
    /**
     * Sucht einen NPC anhand seines Namens
     *
     * @param npcName Der Name des NPCs
     * @param level Das ServerLevel
     * @return Der gefundene CustomNPCEntity oder null
     */
    private static CustomNPCEntity getNPCByName(String npcName, ServerLevel level) {
        return NPCNameRegistry.findNPCByName(npcName, level);
    }

    // ═══════════════════════════════════════════════════════════
    // WAREHOUSE COMMANDS
    // ═══════════════════════════════════════════════════════════

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

    private static int setWarehouse(CommandContext<CommandSourceStack> context) {
        try {
            String npcName = StringArgumentType.getString(context, "npcName");
            ServerLevel level = context.getSource().getLevel();
            ServerPlayer player = context.getSource().getPlayerOrException();

            CustomNPCEntity npc = getNPCByName(npcName, level);
            if (npc == null) {
                context.getSource().sendFailure(
                    Component.translatable("message.npc.not_found_colored", npcName)
                );
                return 0;
            }

            BlockPos warehousePos = findWarehouseBlockPos(player);
            if (warehousePos == null) {
                context.getSource().sendFailure(
                    Component.translatable("message.warehouse.not_found_look_at")
                );
                return 0;
            }

            npc.getNpcData().setAssignedWarehouse(warehousePos);

            context.getSource().sendSuccess(() ->
                Component.translatable("message.warehouse.linked")
                    .append(Component.literal("\n§7NPC: §e" + npc.getNpcName() + "\n" +
                    "§7Position: §f" + warehousePos.getX() + ", " + warehousePos.getY() + ", " + warehousePos.getZ()))
                ), false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("message.npc.warehouse_link_error"));
            return 0;
        }
    }

    private static int clearWarehouse(CommandContext<CommandSourceStack> context) {
        try {
            String npcName = StringArgumentType.getString(context, "npcName");
            ServerLevel level = context.getSource().getLevel();

            CustomNPCEntity npc = getNPCByName(npcName, level);
            if (npc == null) {
                context.getSource().sendFailure(
                    Component.translatable("message.npc.not_found_colored", npcName)
                );
                return 0;
            }

            npc.getNpcData().setAssignedWarehouse(null);

            context.getSource().sendSuccess(() ->
                Component.translatable("message.warehouse.unlinked")
                    .append(Component.literal("\n§7NPC: §e" + npc.getNpcName()))
                ), false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("message.npc.warehouse_unlink_error"));
            return 0;
        }
    }

    private static int warehouseInfo(CommandContext<CommandSourceStack> context) {
        try {
            String npcName = StringArgumentType.getString(context, "npcName");
            ServerLevel level = context.getSource().getLevel();

            CustomNPCEntity npc = getNPCByName(npcName, level);
            if (npc == null) {
                context.getSource().sendFailure(
                    Component.translatable("message.npc.not_found_colored", npcName)
                );
                return 0;
            }

            BlockPos warehousePos = npc.getNpcData().getAssignedWarehouse();
            if (warehousePos == null) {
                context.getSource().sendSuccess(() ->
                    Component.translatable("message.warehouse.info_header").append(Component.literal("\n")).append(
                        "§7NPC: §e" + npc.getNpcName() + "\n" +
                        "§7Status: §cKein Warehouse verknüpft"
                    ), false
                );
            } else {
                de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity warehouse =
                    npc.getNpcData().getWarehouseEntity(level);
                String info = "§e=== Warehouse Info ===\n" +
                    "§7NPC: §e" + npc.getNpcName() + "\n" +
                    "§7Position: §f" + warehousePos.getX() + ", " + warehousePos.getY() + ", " + warehousePos.getZ() + "\n";

                if (warehouse != null) {
                    info += "§7Slots belegt: §e" + warehouse.getUsedSlots() + "§7/§e" + warehouse.getSlots().length + "\n" +
                            "§7Total Items: §e" + warehouse.getTotalItems();
                } else {
                    info += "§7Status: §cWarehouse-Block nicht gefunden!";
                }

                final String finalInfo = info;
                context.getSource().sendSuccess(() -> Component.literal(finalInfo), false);
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("message.warehouse.info_error"));
            return 0;
        }
    }
}
