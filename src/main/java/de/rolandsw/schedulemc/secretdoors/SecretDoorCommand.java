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
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class SecretDoorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("secretdoor")
            .requires(source -> source.hasPermission(2))

            // /secretdoor size <x> <y> <z> <breite> <höhe>
            .then(Commands.literal("size")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .then(Commands.argument("breite", IntegerArgumentType.integer(1, 20))
                        .then(Commands.argument("hoehe", IntegerArgumentType.integer(1, 20))
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
                source.sendFailure(Component.translatable("message.secret_door.not_a_secret_door"));
                return 0;
            }

            if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
                source.sendFailure(Component.translatable("message.secret_door.no_block_entity"));
                return 0;
            }

            be.setSize(width, height, level);
            doorBlock.spawnFillers(level, pos, be, width, height, state.getValue(AbstractSecretDoorBlock.FACING));

            source.sendSuccess(() -> Component.translatable("message.secret_door.size_set", pos.toShortString(), width, height), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("message.secret_door.error_generic", e.getMessage()));
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
                source.sendFailure(Component.translatable("message.secret_door.not_a_secret_door"));
                return 0;
            }

            if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
                source.sendFailure(Component.translatable("message.secret_door.no_block_entity"));
                return 0;
            }

            ServerPlayer player = source.getPlayerOrException();
            be.toggle(level, player);

            source.sendSuccess(() -> Component.translatable("message.secret_door.toggled_at", pos.toShortString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("message.secret_door.error_generic", e.getMessage()));
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
                source.sendFailure(Component.translatable("message.secret_door.not_a_secret_door"));
                return 0;
            }

            if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
                source.sendFailure(Component.translatable("message.secret_door.no_block_entity"));
                return 0;
            }

            source.sendSuccess(() -> Component.translatable("message.secret_door.info", doorBlock.getDoorType().name(),
                be.getDoorWidth(), be.getDoorHeight(),
                be.isOpen() ? Component.translatable("message.secret_door.status_open") : Component.translatable("message.secret_door.status_closed"),
                be.getOwnerName().isEmpty() ? Component.translatable("message.secret_door.owner_none") : Component.literal(be.getOwnerName()),
                be.getFillerOffsets().size(), be.getLinkedSwitches().size()
            ), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("message.secret_door.error_generic", e.getMessage()));
            return 0;
        }
    }
}
