package de.rolandsw.schedulemc.economy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import de.rolandsw.schedulemc.economy.events.RespawnHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * /hospital setspawn - Setzt Krankenhaus-Respawn-Position
 * /hospital setfee <amount> - Setzt Krankenhausrechnung
 */
public class HospitalCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hospital")
            .requires(source -> source.hasPermission(2)) // OP-Level 2
            
            // /hospital setspawn
            .then(Commands.literal("setspawn")
                .executes(context -> {
                    BlockPos pos = BlockPos.containing(context.getSource().getPosition());
                    RespawnHandler.setHospitalSpawn(pos);
                    
                    context.getSource().sendSuccess(() -> Component.literal(
                        "§a✓ Krankenhaus-Spawn gesetzt!\n" +
                        "§7Position: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "\n" +
                        "§7Spieler spawnen hier nach dem Tod"
                    ), true);
                    
                    return 1;
                }))
            
            // /hospital setfee <amount>
            .then(Commands.literal("setfee")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                    .executes(context -> {
                        double fee = DoubleArgumentType.getDouble(context, "amount");
                        RespawnHandler.setHospitalFee(fee);
                        
                        context.getSource().sendSuccess(() -> Component.literal(
                            "§a✓ Krankenhausrechnung gesetzt!\n" +
                            "§7Kosten pro Tod: §e" + String.format("%.0f€", fee)
                        ), true);
                        
                        return 1;
                    })))
            
            // /hospital info
            .then(Commands.literal("info")
                .executes(context -> {
                    BlockPos spawn = RespawnHandler.getHospitalSpawn();
                    double fee = RespawnHandler.getHospitalFee();
                    
                    context.getSource().sendSuccess(() -> Component.literal(
                        "§6╔══ KRANKENHAUS-INFO ══╗\n" +
                        "§7Spawn: §f" + spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ() + "\n" +
                        "§7Gebühr: §e" + String.format("%.0f€", fee) + " pro Tod\n" +
                        "§6╚══════════════════════╝"
                    ), false);
                    
                    return 1;
                }))
        );
    }
}
