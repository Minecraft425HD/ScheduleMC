package de.rolandsw.schedulemc.npc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.npc.data.ActivityType;
import de.rolandsw.schedulemc.npc.data.ScheduleEntry;
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
 * /npc info - Zeigt Informationen über ausgewählten NPC
 * /npc schedule ... - Schedule-Verwaltung
 * /npc freetime ... - Freetime-Locations-Verwaltung
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
                .then(Commands.literal("info")
                    .executes(NPCCommand::showInfo)
                )
                .then(Commands.literal("debug")
                    .then(Commands.literal("move")
                        .executes(NPCCommand::debugMove)
                    )
                )
                .then(Commands.literal("schedule")
                    .then(Commands.literal("add")
                        .then(Commands.argument("activity", StringArgumentType.word())
                            .then(Commands.argument("startTime", IntegerArgumentType.integer(0, 24000))
                                .then(Commands.argument("endTime", IntegerArgumentType.integer(0, 24000))
                                    .executes(NPCCommand::scheduleAdd)
                                )
                            )
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(NPCCommand::scheduleList)
                    )
                    .then(Commands.literal("clear")
                        .executes(NPCCommand::scheduleClear)
                    )
                    .then(Commands.literal("default")
                        .executes(NPCCommand::scheduleDefault)
                    )
                )
                .then(Commands.literal("freetime")
                    .then(Commands.literal("add")
                        .executes(NPCCommand::freetimeAdd)
                    )
                    .then(Commands.literal("list")
                        .executes(NPCCommand::freetimeList)
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                            .executes(NPCCommand::freetimeRemove)
                        )
                    )
                    .then(Commands.literal("clear")
                        .executes(NPCCommand::freetimeClear)
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

        // Warnung, wenn Movement aktiviert wird, aber Locations fehlen
        if (enabled && player != null) {
            var data = npc.getNpcData();
            boolean hasLocations = false;
            StringBuilder missingLocations = new StringBuilder();

            // Prüfe welche Locations in Schedule verwendet werden
            for (var entry : data.getSchedule()) {
                switch (entry.getActivityType()) {
                    case HOME:
                        if (data.getHomeLocation() == null) {
                            if (missingLocations.length() > 0) missingLocations.append(", ");
                            missingLocations.append("Home");
                        } else {
                            hasLocations = true;
                        }
                        break;
                    case WORK:
                        if (data.getWorkLocation() == null) {
                            if (missingLocations.length() > 0) missingLocations.append(", ");
                            missingLocations.append("Work");
                        } else {
                            hasLocations = true;
                        }
                        break;
                    case FREETIME:
                        if (data.getFreetimeLocations().isEmpty()) {
                            if (missingLocations.length() > 0) missingLocations.append(", ");
                            missingLocations.append("Freetime");
                        } else {
                            hasLocations = true;
                        }
                        break;
                }
            }

            if (missingLocations.length() > 0) {
                player.sendSystemMessage(
                    Component.literal("⚠ Warnung: Folgende Locations fehlen: ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(missingLocations.toString())
                            .withStyle(ChatFormatting.YELLOW))
                );
                player.sendSystemMessage(
                    Component.literal("Verwende das LocationTool (Shift-Click), um Locations zu setzen!")
                        .withStyle(ChatFormatting.GRAY)
                );
            }
        }

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

    private static int debugMove(CommandContext<CommandSourceStack> context) {
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

        player.sendSystemMessage(Component.literal("=== Movement Debug ===").withStyle(ChatFormatting.GOLD));

        // Prüfe Movement-Einstellung
        player.sendSystemMessage(
            Component.literal("1. Movement aktiviert: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(behavior.canMove() ? "✓ Ja" : "✗ Nein")
                    .withStyle(behavior.canMove() ? ChatFormatting.GREEN : ChatFormatting.RED))
        );

        if (!behavior.canMove()) {
            player.sendSystemMessage(
                Component.literal("   → Aktiviere Movement mit /npc movement true")
                    .withStyle(ChatFormatting.YELLOW)
            );
            return 0;
        }

        // Prüfe Schedule
        long dayTime = player.level().getDayTime();
        ScheduleEntry current = data.getCurrentScheduleEntry(dayTime);

        if (current == null) {
            player.sendSystemMessage(
                Component.literal("2. Schedule-Eintrag: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("✗ Keiner gefunden!")
                        .withStyle(ChatFormatting.RED))
            );
            player.sendSystemMessage(
                Component.literal("   → Erstelle Schedule mit /npc schedule default")
                    .withStyle(ChatFormatting.YELLOW)
            );
            return 0;
        }

        player.sendSystemMessage(
            Component.literal("2. Schedule-Eintrag: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("✓ " + current.getActivityType().getDisplayName())
                    .withStyle(ChatFormatting.GREEN))
        );

        // Prüfe Ziel-Location
        BlockPos targetLoc = data.getTargetLocationForEntry(current);
        if (targetLoc == null) {
            player.sendSystemMessage(
                Component.literal("3. Ziel-Location: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("✗ Nicht gesetzt!")
                        .withStyle(ChatFormatting.RED))
            );
            player.sendSystemMessage(
                Component.literal("   → Setze Locations mit dem LocationTool (Shift-Click)")
                    .withStyle(ChatFormatting.YELLOW)
            );
            return 0;
        }

        player.sendSystemMessage(
            Component.literal("3. Ziel-Location: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("✓ " + targetLoc.toShortString())
                    .withStyle(ChatFormatting.GREEN))
        );

        // Prüfe Distanz
        BlockPos currentPos = npc.blockPosition();
        double distance = Math.sqrt(currentPos.distSqr(targetLoc));

        player.sendSystemMessage(
            Component.literal("4. Distanz zum Ziel: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f Blöcke", distance))
                    .withStyle(distance > 2.0 ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
        );

        if (distance <= 2.0) {
            player.sendSystemMessage(
                Component.literal("   ℹ NPC ist bereits am Ziel angekommen!")
                    .withStyle(ChatFormatting.GREEN)
            );
            return 1;
        }

        // Versuche manuell Pfad zu starten
        player.sendSystemMessage(
            Component.literal("5. Starte Pathfinding manuell...")
                .withStyle(ChatFormatting.GRAY)
        );

        boolean success = npc.getNavigation().moveTo(
            targetLoc.getX() + 0.5,
            targetLoc.getY(),
            targetLoc.getZ() + 0.5,
            behavior.getMovementSpeed()
        );

        if (success) {
            player.sendSystemMessage(
                Component.literal("   ✓ Pathfinding erfolgreich gestartet!")
                    .withStyle(ChatFormatting.GREEN)
            );
        } else {
            player.sendSystemMessage(
                Component.literal("   ✗ Pathfinding fehlgeschlagen!")
                    .withStyle(ChatFormatting.RED)
            );
            player.sendSystemMessage(
                Component.literal("   → Mögliche Ursachen:")
                    .withStyle(ChatFormatting.YELLOW)
            );
            player.sendSystemMessage(
                Component.literal("      - Kein Pfad zum Ziel vorhanden")
                    .withStyle(ChatFormatting.GRAY)
            );
            player.sendSystemMessage(
                Component.literal("      - Ziel ist blockiert oder unzugänglich")
                    .withStyle(ChatFormatting.GRAY)
            );
            player.sendSystemMessage(
                Component.literal("      - Ziel ist zu weit entfernt")
                    .withStyle(ChatFormatting.GRAY)
            );
        }

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
        player.sendSystemMessage(
            Component.literal("Freizeitorte: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.getFreetimeLocations().size()))
                    .withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.literal("Schedule-Einträge: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.getSchedule().size()))
                    .withStyle(ChatFormatting.YELLOW))
        );

        // Zeige aktuelle Position des NPCs
        BlockPos currentPos = npc.blockPosition();
        player.sendSystemMessage(
            Component.literal("Aktuelle Position: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(currentPos.toShortString())
                    .withStyle(ChatFormatting.YELLOW))
        );

        // Zeige aktuellen Schedule-Eintrag
        ScheduleEntry current = data.getCurrentScheduleEntry(player.level().getDayTime());
        if (current != null) {
            player.sendSystemMessage(
                Component.literal("Aktuelle Aktivität: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(current.getActivityType().getDisplayName())
                        .withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" (" + current.getTimeDescription() + ")")
                        .withStyle(ChatFormatting.DARK_GRAY))
            );

            // Zeige Ziel-Location für aktuelle Aktivität
            BlockPos targetLoc = data.getTargetLocationForEntry(current);
            if (targetLoc != null) {
                player.sendSystemMessage(
                    Component.literal("Ziel: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(targetLoc.toShortString())
                            .withStyle(ChatFormatting.GREEN))
                );

                // Zeige Distanz zum Ziel
                double distance = Math.sqrt(currentPos.distSqr(targetLoc));
                player.sendSystemMessage(
                    Component.literal("Distanz zum Ziel: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.1f Blöcke", distance))
                            .withStyle(distance > 2.0 ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                );

                // Debug: Ist Movement-Goal aktiv?
                boolean isMoving = npc.getNavigation().isInProgress();
                player.sendSystemMessage(
                    Component.literal("Navigiert gerade: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(isMoving ? "Ja" : "Nein")
                            .withStyle(isMoving ? ChatFormatting.GREEN : ChatFormatting.RED))
                );

                if (!isMoving && distance > 2.0) {
                    player.sendSystemMessage(
                        Component.literal("⚠ PROBLEM: NPC ist zu weit weg vom Ziel, aber navigiert nicht!")
                            .withStyle(ChatFormatting.RED)
                    );
                }
            } else {
                player.sendSystemMessage(
                    Component.literal("⚠ Ziel: ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("Nicht gesetzt!")
                            .withStyle(ChatFormatting.RED))
                );
            }
        }

        // Warnung, wenn Movement aktiviert ist, aber NPC sich nicht bewegen kann
        if (behavior.canMove() && !data.getSchedule().isEmpty()) {
            boolean canMove = false;
            for (ScheduleEntry entry : data.getSchedule()) {
                if (data.getTargetLocationForEntry(entry) != null) {
                    canMove = true;
                    break;
                }
            }

            if (!canMove) {
                player.sendSystemMessage(
                    Component.literal("⚠ PROBLEM: Movement aktiviert, aber keine Locations gesetzt!")
                        .withStyle(ChatFormatting.RED)
                );
                player.sendSystemMessage(
                    Component.literal("Verwende das LocationTool um Locations zu setzen!")
                        .withStyle(ChatFormatting.YELLOW)
                );
            }
        }

        return 1;
    }

    private static int scheduleAdd(CommandContext<CommandSourceStack> context) {
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

        String activityStr = StringArgumentType.getString(context, "activity");
        int startTime = IntegerArgumentType.getInteger(context, "startTime");
        int endTime = IntegerArgumentType.getInteger(context, "endTime");

        ActivityType activityType;
        try {
            activityType = ActivityType.valueOf(activityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(
                Component.literal("Ungültiger Aktivitätstyp! Verwende: work, home oder freetime")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        ScheduleEntry entry = new ScheduleEntry(startTime, endTime, activityType);
        npc.getNpcData().addScheduleEntry(entry);

        context.getSource().sendSuccess(
            () -> Component.literal("Schedule-Eintrag hinzugefügt: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(activityType.getDisplayName())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" (" + entry.getTimeDescription() + ")")
                    .withStyle(ChatFormatting.GRAY)),
            false
        );

        return 1;
    }

    private static int scheduleList(CommandContext<CommandSourceStack> context) {
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

        var schedule = npc.getNpcData().getSchedule();
        if (schedule.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("Keine Schedule-Einträge vorhanden!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        player.sendSystemMessage(
            Component.literal("=== Schedule von " + npc.getNpcName() + " ===")
                .withStyle(ChatFormatting.GOLD)
        );

        for (int i = 0; i < schedule.size(); i++) {
            ScheduleEntry entry = schedule.get(i);
            player.sendSystemMessage(
                Component.literal(i + ". ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(entry.getActivityType().getDisplayName())
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" - " + entry.getTimeDescription())
                        .withStyle(ChatFormatting.AQUA))
            );
        }

        return 1;
    }

    private static int scheduleClear(CommandContext<CommandSourceStack> context) {
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

        npc.getNpcData().clearSchedule();

        context.getSource().sendSuccess(
            () -> Component.literal("Schedule gelöscht für NPC ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int scheduleDefault(CommandContext<CommandSourceStack> context) {
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

        npc.getNpcData().createDefaultSchedule();

        context.getSource().sendSuccess(
            () -> Component.literal("Standard-Schedule erstellt für NPC ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("\n06:00-18:00: Arbeit\n18:00-22:00: Freizeit\n22:00-06:00: Zuhause")
                    .withStyle(ChatFormatting.GRAY)),
            false
        );

        // Warnung, wenn Locations fehlen
        if (player != null) {
            var data = npc.getNpcData();
            StringBuilder missingLocations = new StringBuilder();

            if (data.getWorkLocation() == null) {
                missingLocations.append("Work");
            }
            if (data.getFreetimeLocations().isEmpty()) {
                if (missingLocations.length() > 0) missingLocations.append(", ");
                missingLocations.append("Freetime");
            }
            if (data.getHomeLocation() == null) {
                if (missingLocations.length() > 0) missingLocations.append(", ");
                missingLocations.append("Home");
            }

            if (missingLocations.length() > 0) {
                player.sendSystemMessage(
                    Component.literal("⚠ Wichtig: Folgende Locations müssen noch gesetzt werden: ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(missingLocations.toString())
                            .withStyle(ChatFormatting.YELLOW))
                );
                player.sendSystemMessage(
                    Component.literal("1. Verwende das LocationTool (Shift-Click auf NPC)")
                        .withStyle(ChatFormatting.GRAY)
                );
                player.sendSystemMessage(
                    Component.literal("2. Dann aktiviere Movement mit /npc movement true")
                        .withStyle(ChatFormatting.GRAY)
                );
            } else if (!data.getBehavior().canMove()) {
                player.sendSystemMessage(
                    Component.literal("✓ Alle Locations gesetzt! Aktiviere Movement mit /npc movement true")
                        .withStyle(ChatFormatting.GREEN)
                );
            }
        }

        return 1;
    }

    private static int freetimeAdd(CommandContext<CommandSourceStack> context) {
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

        // Verwende die aktuelle Position des Spielers als Freetime-Location
        BlockPos location = player.blockPosition();
        npc.getNpcData().addFreetimeLocation(location);

        context.getSource().sendSuccess(
            () -> Component.literal("Freizeitort hinzugefügt: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(location.toShortString())
                    .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" für NPC ")
                    .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int freetimeList(CommandContext<CommandSourceStack> context) {
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

        var locations = npc.getNpcData().getFreetimeLocations();
        if (locations.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("Keine Freizeitorte vorhanden!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        player.sendSystemMessage(
            Component.literal("=== Freizeitorte von " + npc.getNpcName() + " ===")
                .withStyle(ChatFormatting.GOLD)
        );

        for (int i = 0; i < locations.size(); i++) {
            BlockPos pos = locations.get(i);
            player.sendSystemMessage(
                Component.literal(i + ". ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(pos.toShortString())
                        .withStyle(ChatFormatting.AQUA))
            );
        }

        return 1;
    }

    private static int freetimeRemove(CommandContext<CommandSourceStack> context) {
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

        int index = IntegerArgumentType.getInteger(context, "index");
        var locations = npc.getNpcData().getFreetimeLocations();

        if (index < 0 || index >= locations.size()) {
            context.getSource().sendFailure(
                Component.literal("Ungültiger Index! Verwende /npc freetime list um alle Locations zu sehen.")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        BlockPos removed = locations.get(index);
        npc.getNpcData().removeFreetimeLocation(index);

        context.getSource().sendSuccess(
            () -> Component.literal("Freizeitort entfernt: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(removed.toShortString())
                    .withStyle(ChatFormatting.YELLOW)),
            false
        );

        return 1;
    }

    private static int freetimeClear(CommandContext<CommandSourceStack> context) {
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

        npc.getNpcData().clearFreetimeLocations();

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
