package de.rolandsw.schedulemc.npc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.ScheduleMC;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Random;

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
 * /npc wallet info - Zeigt Geldbörsen-Informationen
 * /npc wallet set <amount> - Setzt Geldbörse auf Betrag
 * /npc wallet add <amount> - Fügt Betrag zur Geldbörse hinzu
 * /npc wallet remove <amount> - Entfernt Betrag von Geldbörse
 * /npc inventory info - Zeigt Inventar-Informationen
 * /npc inventory clear - Leert das Inventar
 * /npc dailyincome trigger - Triggert manuell das tägliche Einkommen (für Tests)
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
                // Wallet Commands
                .then(Commands.literal("wallet")
                    .then(Commands.literal("info")
                        .executes(NPCCommand::showWalletInfo)
                    )
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
                // Inventory Commands
                .then(Commands.literal("inventory")
                    .then(Commands.literal("info")
                        .executes(NPCCommand::showInventoryInfo)
                    )
                    .then(Commands.literal("clear")
                        .executes(NPCCommand::clearInventory)
                    )
                )
                // Daily Income Commands
                .then(Commands.literal("dailyincome")
                    .then(Commands.literal("trigger")
                        .executes(NPCCommand::triggerDailyIncome)
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
        player.sendSystemMessage(
            Component.literal("Arbeitsort: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getWorkLocation() != null ?
                    data.getWorkLocation().toShortString() : "Nicht gesetzt")
                    .withStyle(data.getWorkLocation() != null ? ChatFormatting.GREEN : ChatFormatting.RED))
        );

        // Ökonomie-Features (nur für BEWOHNER und VERKÄUFER)
        if (data.hasEconomyFeatures()) {
            player.sendSystemMessage(Component.literal("--- Ökonomie ---").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(
                Component.literal("Geldbörse: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getCash() + "€").withStyle(ChatFormatting.GREEN))
            );

            // Zähle Items im Inventar
            int itemCount = 0;
            for (ItemStack stack : data.getInventory()) {
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

            player.sendSystemMessage(
                Component.literal("Letztes Einkommen: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.getLastDailyIncomeDay() >= 0 ?
                        "Tag " + data.getLastDailyIncomeDay() : "Nie")
                        .withStyle(data.getLastDailyIncomeDay() >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED))
            );
        } else {
            player.sendSystemMessage(
                Component.literal("Ökonomie: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Nicht verfügbar (Polizei)")
                        .withStyle(ChatFormatting.RED))
            );
        }

        return 1;
    }

    // ==================== WALLET COMMANDS ====================

    private static int showWalletInfo(CommandContext<CommandSourceStack> context) {
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

        if (!npc.getNpcData().hasEconomyFeatures()) {
            context.getSource().sendFailure(
                Component.literal("NPC '")
                    .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("' hat keine Geldbörse (nur BEWOHNER und VERKÄUFER)!"))
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        int cash = npc.getNpcData().getCash();
        long lastIncomeDay = npc.getNpcData().getLastDailyIncomeDay();

        player.sendSystemMessage(Component.literal("=== Geldbörse Info ===").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(
            Component.literal("NPC: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(
            Component.literal("Bargeld: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(cash + "€").withStyle(ChatFormatting.GREEN))
        );
        player.sendSystemMessage(
            Component.literal("Letztes Einkommen (Tag): ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(lastIncomeDay >= 0 ? String.valueOf(lastIncomeDay) : "Nie")
                    .withStyle(lastIncomeDay >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED))
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
                Component.literal("Kein NPC ausgewählt oder in der Nähe!").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasEconomyFeatures()) {
            context.getSource().sendFailure(
                Component.literal("NPC hat keine Geldbörse (nur BEWOHNER und VERKÄUFER)!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        npc.getNpcData().setCash(amount);

        context.getSource().sendSuccess(
            () -> Component.literal("Geldbörse von ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" auf ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(amount + "€").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" gesetzt!").withStyle(ChatFormatting.GREEN)),
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
                Component.literal("Kein NPC ausgewählt oder in der Nähe!").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasEconomyFeatures()) {
            context.getSource().sendFailure(
                Component.literal("NPC hat keine Geldbörse (nur BEWOHNER und VERKÄUFER)!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        int oldCash = npc.getNpcData().getCash();
        npc.getNpcData().addCash(amount);
        int newCash = npc.getNpcData().getCash();

        context.getSource().sendSuccess(
            () -> Component.literal("")
                .append(Component.literal(amount + "€").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" zu Geldbörse von ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" hinzugefügt! (").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(oldCash + "€").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" → ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(newCash + "€").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(")").withStyle(ChatFormatting.GREEN)),
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
                Component.literal("Kein NPC ausgewählt oder in der Nähe!").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasEconomyFeatures()) {
            context.getSource().sendFailure(
                Component.literal("NPC hat keine Geldbörse (nur BEWOHNER und VERKÄUFER)!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        int oldCash = npc.getNpcData().getCash();
        npc.getNpcData().removeCash(amount);
        int newCash = npc.getNpcData().getCash();

        context.getSource().sendSuccess(
            () -> Component.literal("")
                .append(Component.literal(amount + "€").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" von Geldbörse von ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" entfernt! (").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(oldCash + "€").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" → ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(newCash + "€").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(")").withStyle(ChatFormatting.GREEN)),
            false
        );

        return 1;
    }

    // ==================== INVENTORY COMMANDS ====================

    private static int showInventoryInfo(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Nur Spieler können diesen Command verwenden!"));
            return 0;
        }

        CustomNPCEntity npc = getSelectedOrNearestNPC(player);
        if (npc == null) {
            context.getSource().sendFailure(
                Component.literal("Kein NPC ausgewählt oder in der Nähe!").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasEconomyFeatures()) {
            context.getSource().sendFailure(
                Component.literal("NPC '")
                    .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("' hat kein Inventar (nur BEWOHNER und VERKÄUFER)!"))
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        player.sendSystemMessage(Component.literal("=== Inventar Info ===").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(
            Component.literal("NPC: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
        );

        ItemStack[] inventory = npc.getNpcData().getInventory();
        int itemCount = 0;

        for (int i = 0; i < inventory.length; i++) {
            ItemStack stack = inventory[i];
            if (!stack.isEmpty()) {
                itemCount++;
                player.sendSystemMessage(
                    Component.literal("  Slot " + i + ": ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(stack.getCount() + "x ")
                            .withStyle(ChatFormatting.WHITE))
                        .append(stack.getDisplayName().copy().withStyle(ChatFormatting.AQUA))
                );
            }
        }

        if (itemCount == 0) {
            player.sendSystemMessage(
                Component.literal("  Inventar ist leer")
                    .withStyle(ChatFormatting.GRAY)
                    .withStyle(ChatFormatting.ITALIC)
            );
        }

        player.sendSystemMessage(
            Component.literal("Gesamt: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(itemCount + "/9 Slots belegt")
                    .withStyle(ChatFormatting.YELLOW))
        );

        return 1;
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
                Component.literal("Kein NPC ausgewählt oder in der Nähe!").withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        if (!npc.getNpcData().hasEconomyFeatures()) {
            context.getSource().sendFailure(
                Component.literal("NPC hat kein Inventar (nur BEWOHNER und VERKÄUFER)!")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        // Zähle Items vor dem Löschen
        int itemCount = 0;
        ItemStack[] inventory = npc.getNpcData().getInventory();
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                itemCount++;
            }
        }

        // Leere Inventar
        for (int i = 0; i < inventory.length; i++) {
            npc.getNpcData().setInventoryItem(i, ItemStack.EMPTY);
        }

        context.getSource().sendSuccess(
            () -> Component.literal("Inventar von ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" geleert! (").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(itemCount + " Items entfernt").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(")").withStyle(ChatFormatting.GREEN)),
            false
        );

        return 1;
    }

    // ==================== DAILY INCOME COMMANDS ====================

    private static int triggerDailyIncome(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        Random random = new Random();

        int npcCount = 0;
        int totalPaid = 0;

        // Iteriere über alle NPCs in der Welt
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof CustomNPCEntity npc) {
                // Nur für BEWOHNER und VERKÄUFER, nicht für POLIZEI
                if (!npc.getNpcData().hasEconomyFeatures()) {
                    continue;
                }

                // Generiere zufälligen Betrag zwischen 20 und 150
                int income = 20 + random.nextInt(131); // 20-150 inklusiv

                // Zahle dem NPC das Geld
                npc.getNpcData().addCash(income);
                npc.getNpcData().setLastDailyIncomeDay(level.getDayTime() / 24000);

                npcCount++;
                totalPaid += income;
            }
        }

        final int finalNpcCount = npcCount;
        final int finalTotalPaid = totalPaid;

        if (npcCount > 0) {
            context.getSource().sendSuccess(
                () -> Component.literal("Tägliches Einkommen manuell ausgelöst!")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("\n" + finalNpcCount + " NPCs erhielten insgesamt ")
                        .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(finalTotalPaid + "€").withStyle(ChatFormatting.GOLD)),
                true
            );
        } else {
            context.getSource().sendFailure(
                Component.literal("Keine NPCs mit Geldbörse gefunden!")
                    .withStyle(ChatFormatting.RED)
            );
        }

        return npcCount;
    }

    // ==================== HELPER METHODS ====================

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
