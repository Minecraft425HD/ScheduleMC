package de.rolandsw.schedulemc.npc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.items.NPCLocationTool;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Command für NPC-Verwaltung
 * /npc movement <true|false> - Aktiviert/Deaktiviert Bewegung für ausgewählten NPC
 * /npc speed <value> - Setzt Bewegungsgeschwindigkeit für ausgewählten NPC
 * /npc schedule workstart <time> - Setzt Arbeitsbeginn (Format: HHMM, z.B. 0700)
 * /npc schedule workend <time> - Setzt Arbeitsende (Format: HHMM, z.B. 1800)
 * /npc schedule home <time> - Setzt Heimzeit (Format: HHMM, z.B. 2300)
 * /npc leisure add - Fügt aktuelle Position als Freizeitort hinzu
 * /npc leisure remove <index> - Entfernt Freizeitort
 * /npc leisure list - Listet alle Freizeitorte auf
 * /npc leisure clear - Löscht alle Freizeitorte
 * /npc inventory - Zeigt das Inventar des NPCs (nur Bewohner & Verkäufer)
 * /npc inventory give <slot> <item> - Gibt dem NPC ein Item in Slot 0-8
 * /npc inventory clear [slot] - Löscht das gesamte Inventar oder einen Slot
 * /npc wallet - Zeigt die Geldbörse des NPCs (nur Bewohner & Verkäufer)
 * /npc wallet set <amount> - Setzt die Geldbörse auf einen Betrag
 * /npc wallet add <amount> - Fügt Geld zur Geldbörse hinzu
 * /npc wallet remove <amount> - Entfernt Geld von der Geldbörse
 * /npc info - Zeigt Informationen über ausgewählten NPC
 */
public class NPCCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
            Commands.literal("npc")
                .requires(source -> source.hasPermission(2)) // Nur Admins
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
                        .then(Commands.argument("index", IntegerArgumentType.integer(0, 2))
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
                .then(Commands.literal("info")
                    .executes(NPCCommand::showInfo)
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
        );
    }

    private static int setMovement(CommandContext<CommandSourceStack> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe! Wähle einen NPC mit dem LocationTool aus.")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().getBehavior().setCanMove(enabled);

        context.getSource().sendSuccess(
            () -> Component.literal("Bewegung ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(enabled ? "aktiviert" : "deaktiviert")
                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int setSpeed(CommandContext<CommandSourceStack> context) {
        float speed = FloatArgumentType.getFloat(context, "speed");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().getBehavior().setMovementSpeed(speed);

        context.getSource().sendSuccess(
            () -> Component.literal("Bewegungsgeschwindigkeit gesetzt auf ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(String.format("%.2f", speed))
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int showInfo(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        var data = npc.getNpcData();
        var behavior = data.getBehavior();

        player.sendSystemMessage(Component.literal("=== NPC Info ===").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(
            Component.literal("Name: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.literal("Typ: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getNpcType().toString())
                    .withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.literal("Bewegung: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(behavior.canMove() ? "Aktiviert" : "Deaktiviert")
                    .withStyle(behavior.canMove() ? ChatFormatting.GREEN : ChatFormatting.RED))
        );
        player.sendSystemMessage(
            Component.literal("Geschwindigkeit: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f", behavior.getMovementSpeed()))
                    .withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.literal("Wohnort: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getHomeLocation() != null ?
                    data.getHomeLocation().toShortString() : "Nicht gesetzt")
                    .withStyle(data.getHomeLocation() != null ? ChatFormatting.GREEN : ChatFormatting.RED))
        );

        // Arbeitsort nur für Verkäufer anzeigen
        if (data.getNpcType() == NPCType.VERKAEUFER) {
            player.sendSystemMessage(
                Component.literal("Arbeitsort: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getWorkLocation() != null ?
                        data.getWorkLocation().toShortString() : "Nicht gesetzt")
                        .withStyle(data.getWorkLocation() != null ? ChatFormatting.GREEN : ChatFormatting.RED))
            );
        } else if (data.getNpcType() == NPCType.BEWOHNER) {
            player.sendSystemMessage(
                Component.literal("Arbeitsort: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Bewohner arbeiten nicht")
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Freizeitorte: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getLeisureLocations().size() + "/10")
                        .withStyle(ChatFormatting.WHITE))
            );
        }

        // Schedule Zeiten - unterschiedlich je nach NPC-Typ
        player.sendSystemMessage(Component.literal("=== Zeitplan ===").withStyle(ChatFormatting.GOLD));

        if (data.getNpcType() == NPCType.VERKAEUFER) {
            // Verkäufer: Vollständiger Zeitplan
            player.sendSystemMessage(
                Component.literal("Arbeitsbeginn: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkStartTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Arbeitsende: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkEndTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Heimzeit: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("ab " + ticksToTime(data.getHomeTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
        } else if (data.getNpcType() == NPCType.BEWOHNER) {
            // Bewohner: Nur Heimzeit (Schlafenszeit)
            String homeStart = ticksToTime(data.getHomeTime());
            String homeEnd = ticksToTime(data.getWorkStartTime()); // Aufstehzeit
            player.sendSystemMessage(
                Component.literal("Heimzeit (Schlaf): ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(homeStart + " - " + homeEnd)
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Freizeit: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(homeEnd + " - " + homeStart + " (Aktiv in der Stadt)")
                        .withStyle(ChatFormatting.GREEN))
            );
        } else {
            // Polizei oder andere: Alte Anzeige
            player.sendSystemMessage(
                Component.literal("Arbeitsbeginn: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkStartTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Arbeitsende: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getWorkEndTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Heimzeit: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(data.getHomeTime()))
                        .withStyle(ChatFormatting.YELLOW))
            );
        }

        // Freizeitorte
        player.sendSystemMessage(Component.literal("=== Freizeitorte ===").withStyle(ChatFormatting.GOLD));
        var leisureLocations = data.getLeisureLocations();
        if (leisureLocations.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("Keine Freizeitorte definiert")
                    .withStyle(ChatFormatting.GRAY)
            );
        } else {
            for (int i = 0; i < leisureLocations.size(); i++) {
                player.sendSystemMessage(
                    Component.literal("[" + i + "] ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(leisureLocations.get(i).toShortString())
                            .withStyle(ChatFormatting.YELLOW))
                );
            }
        }

        // Inventar und Geldbörse (nur für Bewohner und Verkäufer)
        if (data.hasInventoryAndWallet()) {
            player.sendSystemMessage(Component.literal("=== Inventar & Geldbörse ===").withStyle(ChatFormatting.GOLD));

            // Inventar
            var inventory = data.getInventory();
            int itemCount = 0;
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    itemCount++;
                }
            }
            player.sendSystemMessage(
                Component.literal("Inventar: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(itemCount + "/9 Slots belegt")
                        .withStyle(itemCount > 0 ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
            );

            // Geldbörse
            player.sendSystemMessage(
                Component.literal("Geldbörse: ")
                    .withStyle(ChatFormatting.GRAY)
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
        String timeInput = StringArgumentType.getString(context, "time");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        // Parse Zeit (nur HHMM Format - 4 Ziffern)
        long ticks;
        try {
            if (timeInput.length() != 4) {
                context.getSource().sendFailure(
                    Component.literal("Ungültiges Format! Verwende HHMM (z.B. 0700, 1830)")
                        .withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            int hours = Integer.parseInt(timeInput.substring(0, 2));
            int minutes = Integer.parseInt(timeInput.substring(2, 4));

            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                context.getSource().sendFailure(
                    Component.literal("Ungültige Zeit! Stunden: 0-23, Minuten: 0-59")
                        .withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            ticks = timeToTicks(hours, minutes);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            context.getSource().sendFailure(
                Component.literal("Ungültiges Format! Verwende HHMM (z.B. 0700, 1830)")
                    .withStyle(ChatFormatting.RED)
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
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int addLeisureLocation(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (npc.getNpcData().getLeisureLocations().size() >= 3) {
            context.getSource().sendFailure(
                Component.literal("Maximale Anzahl von 3 Freizeitorten erreicht!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        BlockPos playerPos = player.blockPosition();
        npc.getNpcData().addLeisureLocation(playerPos);

        context.getSource().sendSuccess(
            () -> Component.literal("Freizeitort hinzugefügt: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(playerPos.toShortString())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int removeLeisureLocation(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (index >= npc.getNpcData().getLeisureLocations().size()) {
            context.getSource().sendFailure(
                Component.literal("Ungültiger Index! Verwende /npc leisure list um alle Orte zu sehen.")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        BlockPos removed = npc.getNpcData().getLeisureLocations().get(index);
        npc.getNpcData().removeLeisureLocation(index);

        context.getSource().sendSuccess(
            () -> Component.literal("Freizeitort entfernt: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(removed.toShortString())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int listLeisureLocations(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        var leisureLocations = npc.getNpcData().getLeisureLocations();

        player.sendSystemMessage(
            Component.literal("=== Freizeitorte von " + npc.getNpcName() + " ===")
                .withStyle(ChatFormatting.GOLD)
        );

        if (leisureLocations.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("Keine Freizeitorte definiert.")
                    .withStyle(ChatFormatting.GRAY)
            );
        } else {
            for (int i = 0; i < leisureLocations.size(); i++) {
                BlockPos pos = leisureLocations.get(i);
                player.sendSystemMessage(
                    Component.literal("[" + i + "] ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(pos.toShortString())
                            .withStyle(ChatFormatting.YELLOW))
                );
            }
        }

        return 1;
    }

    private static int clearLeisureLocations(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().clearLeisureLocations();

        context.getSource().sendSuccess(
            () -> Component.literal("Alle Freizeitorte entfernt für NPC ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    // ===== INVENTAR COMMANDS =====

    private static int showInventory(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat kein Inventar! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        player.sendSystemMessage(
            Component.literal("=== Inventar von " + npc.getNpcName() + " ===")
                .withStyle(ChatFormatting.GOLD)
        );

        var inventory = npc.getNpcData().getInventory();
        boolean isEmpty = true;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                isEmpty = false;
                player.sendSystemMessage(
                    Component.literal("[" + i + "] ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(stack.getCount() + "x ")
                            .withStyle(ChatFormatting.YELLOW))
                        .append(stack.getDisplayName())
                );
            }
        }

        if (isEmpty) {
            player.sendSystemMessage(
                Component.literal("Inventar ist leer")
                    .withStyle(ChatFormatting.GRAY)
            );
        }

        return 1;
    }

    private static int giveInventoryItem(CommandContext<CommandSourceStack> context) {
        int slot = IntegerArgumentType.getInteger(context, "slot");
        ItemInput itemInput = context.getArgument("item", ItemInput.class);
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat kein Inventar! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        try {
            ItemStack stack = itemInput.createItemStack(1, false);
            npc.getNpcData().setInventoryItem(slot, stack);

            context.getSource().sendSuccess(
                () -> Component.literal("Item hinzugefügt in Slot " + slot + ": ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(stack.getDisplayName())
                    .append(Component.literal(" für NPC ")
                        .withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW)),
                false
            );

            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(
                Component.literal("Ungültiges Item: " + e.getMessage())
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }
    }

    private static int clearInventory(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat kein Inventar! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        var inventory = npc.getNpcData().getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }

        context.getSource().sendSuccess(
            () -> Component.literal("Inventar geleert für NPC ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int clearInventorySlot(CommandContext<CommandSourceStack> context) {
        int slot = IntegerArgumentType.getInteger(context, "slot");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat kein Inventar! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().setInventoryItem(slot, ItemStack.EMPTY);

        context.getSource().sendSuccess(
            () -> Component.literal("Slot " + slot + " geleert für NPC ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    // ===== WALLET COMMANDS =====

    private static int showWallet(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat keine Geldbörse! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        int wallet = npc.getNpcData().getWallet();

        context.getSource().sendSuccess(
            () -> Component.literal("Geldbörse von ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(": ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(wallet + " Bargeld")
                    .withStyle(ChatFormatting.GOLD)),
            false
        );

        return 1;
    }

    private static int setWallet(CommandContext<CommandSourceStack> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat keine Geldbörse! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().setWallet(amount);

        context.getSource().sendSuccess(
            () -> Component.literal("Geldbörse gesetzt auf ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(amount + " Bargeld")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int addWallet(CommandContext<CommandSourceStack> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat keine Geldbörse! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().addMoney(amount);

        context.getSource().sendSuccess(
            () -> Component.literal(amount + " Bargeld hinzugefügt. Neue Geldbörse: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcData().getWallet() + " Bargeld")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int removeWallet(CommandContext<CommandSourceStack> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Player player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasInventoryAndWallet()) {
            context.getSource().sendFailure(
                Component.literal("Dieser NPC-Typ hat keine Geldbörse! (Nur Bewohner und Verkäufer)")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        boolean success = npc.getNpcData().removeMoney(amount);

        if (!success) {
            context.getSource().sendFailure(
                Component.literal("Nicht genug Geld! Aktuelle Geldbörse: " + npc.getNpcData().getWallet() + " Bargeld")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        context.getSource().sendSuccess(
            () -> Component.literal(amount + " Bargeld entfernt. Neue Geldbörse: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcData().getWallet() + " Bargeld")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
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
    private static CustomNPCEntity getSelectedOrNearestNPC(Player player) {
        // Prüfe ob ein NPC mit LocationTool ausgewählt wurde
        Integer selectedNpcId = NPCLocationTool.getSelectedNPC(player.getUUID());
        if (selectedNpcId != null) {
            Entity entity = player.level().getEntity(selectedNpcId);
            if (entity instanceof CustomNPCEntity npc) {
                return npc;
            }
        }

        // Sonst: Finde nächsten NPC in 10 Blöcken Reichweite
        if (player.level() instanceof ServerLevel serverLevel) {
            AABB searchArea = player.getBoundingBox().inflate(10.0);
            var nearbyNPCs = serverLevel.getEntitiesOfClass(
                CustomNPCEntity.class,
                searchArea
            );

            if (!nearbyNPCs.isEmpty()) {
                // Gib den nächsten NPC zurück
                return nearbyNPCs.stream()
                    .min((npc1, npc2) -> {
                        double dist1 = npc1.distanceToSqr(player);
                        double dist2 = npc2.distanceToSqr(player);
                        return Double.compare(dist1, dist2);
                    })
                    .orElse(null);
            }
        }

        return null;
    }
}
