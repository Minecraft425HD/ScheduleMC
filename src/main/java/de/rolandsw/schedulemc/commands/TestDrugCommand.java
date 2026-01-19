package de.rolandsw.schedulemc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.meth.MethQuality;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Test-Command zum Testen des Qualitäts-Vignetten-Systems
 *
 * Usage:
 * /testdrug all        - Gibt alle Qualitätsstufen (Tabak)
 * /testdrug cannabis   - Gibt alle Cannabis-Qualitäten
 * /testdrug tobacco    - Gibt alle Tabak-Qualitäten
 * /testdrug meth       - Gibt alle Meth-Qualitäten
 */
public class TestDrugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("testdrug")
                .requires(source -> source.hasPermission(2)) // OP Level 2
                .then(Commands.literal("all")
                    .executes(context -> giveAllQualities(context.getSource()))
                )
                .then(Commands.literal("tobacco")
                    .executes(context -> giveTobaccoQualities(context.getSource()))
                )
                .then(Commands.literal("cannabis")
                    .executes(context -> giveCannabisQualities(context.getSource()))
                )
                .then(Commands.literal("meth")
                    .executes(context -> giveMethQualities(context.getSource()))
                )
                .then(Commands.literal("cocaine")
                    .executes(context -> giveCocaineQualities(context.getSource()))
                )
        );
    }

    private static int giveAllQualities(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        long currentDay = player.level().getDayTime() / 24000L;

        // Tabak - alle Qualitäten
        for (TobaccoQuality quality : TobaccoQuality.values()) {
            ItemStack stack = PackagedDrugItem.create(
                DrugType.TOBACCO,
                5, // 5g
                quality,
                TobaccoType.VIRGINIA,
                currentDay
            );
            player.addItem(stack);
        }

        source.sendSuccess(() -> Component.literal("§aAlle Qualitätsstufen (Tabak) erhalten!"), false);
        return 1;
    }

    private static int giveTobaccoQualities(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        long currentDay = player.level().getDayTime() / 24000L;

        for (TobaccoQuality quality : TobaccoQuality.values()) {
            ItemStack stack = PackagedDrugItem.create(
                DrugType.TOBACCO,
                5,
                quality,
                TobaccoType.HAVANA,
                currentDay
            );
            player.addItem(stack);
        }

        source.sendSuccess(() -> Component.literal("§aAlle Tabak-Qualitäten erhalten! (Havana, 5g)"), false);
        return 1;
    }

    private static int giveCannabisQualities(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        long currentDay = player.level().getDayTime() / 24000L;

        for (CannabisQuality quality : CannabisQuality.values()) {
            ItemStack stack = PackagedDrugItem.create(
                DrugType.CANNABIS,
                5,
                quality,
                CannabisStrain.HYBRID,
                currentDay,
                "CURED_CANNABIS"
            );
            player.addItem(stack);
        }

        source.sendSuccess(() -> Component.literal("§aAlle Cannabis-Qualitäten erhalten! (Hybrid, 5g)"), false);
        return 1;
    }

    private static int giveMethQualities(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        long currentDay = player.level().getDayTime() / 24000L;

        for (MethQuality quality : MethQuality.values()) {
            ItemStack stack = PackagedDrugItem.create(
                DrugType.METH,
                5,
                quality,
                null, // Meth hat keine Varianten
                currentDay
            );
            player.addItem(stack);
        }

        source.sendSuccess(() -> Component.literal("§aAlle Meth-Qualitäten erhalten! (5g)"), false);
        return 1;
    }

    private static int giveCocaineQualities(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        long currentDay = player.level().getDayTime() / 24000L;

        // Cocaine verwendet TobaccoQuality
        for (TobaccoQuality quality : TobaccoQuality.values()) {
            ItemStack stack = PackagedDrugItem.create(
                DrugType.COCAINE,
                5,
                quality,
                CocaType.COLOMBIAN,
                currentDay,
                "COCAINE"
            );
            player.addItem(stack);
        }

        source.sendSuccess(() -> Component.literal("§aAlle Cocaine-Qualitäten erhalten! (Colombian, 5g)"), false);
        return 1;
    }
}
