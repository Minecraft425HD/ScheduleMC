package de.rolandsw.schedulemc.npc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.items.NPCLocationTool;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
 * /npc info - Zeigt Informationen über ausgewählten NPC
 */
public class NPCCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
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
        player.sendSystemMessage(
            Component.literal("Arbeitsort: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getWorkLocation() != null ?
                    data.getWorkLocation().toShortString() : "Nicht gesetzt")
                    .withStyle(data.getWorkLocation() != null ? ChatFormatting.GREEN : ChatFormatting.RED))
        );

        // Schedule Zeiten
        player.sendSystemMessage(Component.literal("=== Zeitplan ===").withStyle(ChatFormatting.GOLD));
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
