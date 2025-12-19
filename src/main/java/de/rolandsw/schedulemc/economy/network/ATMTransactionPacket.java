package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.blockentity.ATMBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für ATM-Transaktionen (Client → Server)
 */
public class ATMTransactionPacket {
    
    private final BlockPos pos;
    private final double amount;
    private final boolean isDeposit; // true = Einzahlung, false = Auszahlung
    
    public ATMTransactionPacket(BlockPos pos, double amount, boolean isDeposit) {
        this.pos = pos;
        this.amount = amount;
        this.isDeposit = isDeposit;
    }
    
    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(ATMTransactionPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeDouble(msg.amount);
        buffer.writeBoolean(msg.isDeposit);
    }
    
    /**
     * Decode - Liest Daten aus Packet
     */
    public static ATMTransactionPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        double amount = buffer.readDouble();
        boolean isDeposit = buffer.readBoolean();
        return new ATMTransactionPacket(pos, amount, isDeposit);
    }
    
    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(ATMTransactionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof ATMBlockEntity atmBE)) return;

            // Führe Transaktion aus
            if (msg.isDeposit) {
                atmBE.deposit(player, msg.amount);
            } else {
                atmBE.withdraw(player, msg.amount);
            }
        });
    }
}
