package de.rolandsw.schedulemc.secretdoors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blocks.AbstractSecretDoorBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Befehle für das Secret Doors System.
 *
 * /secretdoor size <x> <y> <z> <breite> <höhe>
 *   → Setzt die Größe einer Geheimtür
 *
 * /secretdoor toggle <x> <y> <z>
 *   → Öffnet/schließt eine Geheimtür
 *
 * /secretdoor info <x> <y> <z>
 *   → Zeigt Informationen über eine Geheimtür
 */
public class SecretDoorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("secretdoor")
            .requires(source -> source.hasPermission(2))

            // /secretdoor size <x> <y> <z> <breite> <höhe>
            .then(Commands.literal("size")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .then(Commands.argument("breite", IntegerArgumentType.integer(1, 10))
                        .then(Commands.argument("hoehe", IntegerArgumentType.integer(1, 10))
                            .executes(SecretDoorCommand::executeSize)
                        )
                    )
                )
            )

            // /secretdoor toggle <x> <y> <z>
            .then(Commands.literal("toggle")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(SecretDoorCommand::executeToggle)
                )
            )

            // /secretdoor info <x> <y> <z>
            .then(Commands.literal("info")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(SecretDoorCommand::executeInfo)
                )
            )
        );
    }

    private static int executeSize(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            ServerLevel level = source.getLevel();
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
            int width = IntegerArgumentType.getInteger(ctx, "breite");
            int height = IntegerArgumentType.getInteger(ctx, "hoehe");

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof AbstractSecretDoorBlock doorBlock)) {
                source.sendFailure(Component.literal("§cKein Geheimtür-Block an dieser Position!"));
                return 0;
            }

            if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
                source.sendFailure(Component.literal("§cKein BlockEntity gefunden!"));
                return 0;
            }

            be.setSize(width, height, level);
            doorBlock.spawnFillers(level, pos, be, width, height, state.getValue(AbstractSecretDoorBlock.FACING));

            source.sendSuccess(() -> Component.literal(
                "§a[Geheimtür] Größe bei §e" + pos.toShortString() + "§a auf §e"
                + width + "×" + height + "§a gesetzt."), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cFehler: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeToggle(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            ServerLevel level = source.getLevel();
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof AbstractSecretDoorBlock)) {
                source.sendFailure(Component.literal("§cKein Geheimtür-Block an dieser Position!"));
                return 0;
            }

            if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
                source.sendFailure(Component.literal("§cKein BlockEntity gefunden!"));
                return 0;
            }

            ServerPlayer player = source.getPlayerOrException();
            be.toggle(level, player);

            source.sendSuccess(() -> Component.literal(
                "§a[Geheimtür] Tür bei §e" + pos.toShortString() + "§a geschaltet."), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cFehler: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            ServerLevel level = source.getLevel();
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof AbstractSecretDoorBlock doorBlock)) {
                source.sendFailure(Component.literal("§cKein Geheimtür-Block an dieser Position!"));
                return 0;
            }

            if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
                source.sendFailure(Component.literal("§cKein BlockEntity gefunden!"));
                return 0;
            }

            source.sendSuccess(() -> Component.literal(
                "§6=== Geheimtür Info ===\n" +
                "§7Typ: §e" + doorBlock.getDoorType().name() + "\n" +
                "§7Material: §e" + doorBlock.getMaterial().name() + "\n" +
                "§7Größe: §e" + be.getDoorWidth() + "×" + be.getDoorHeight() + "\n" +
                "§7Status: §e" + (be.isOpen() ? "§aOFFEN" : "§cGESCHLOSSEN") + "\n" +
                "§7Besitzer: §e" + (be.getOwnerName().isEmpty() ? "Niemand" : be.getOwnerName()) + "\n" +
                "§7Füller-Blöcke: §e" + be.getFillerOffsets().size() + "\n" +
                "§7Verknüpfte Schalter: §e" + be.getLinkedSwitches().size()
            ), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cFehler: " + e.getMessage()));
            return 0;
        }
    }
}
