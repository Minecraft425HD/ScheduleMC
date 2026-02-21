package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.blockentity.ATMBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Packet für ATM-Transaktionen (Client → Server)
 */
public class ATMTransactionPacket {

    /** Maximale Reichweite zum ATM in Blöcken (quadriert für distanceToSqr-Vergleich). */
    private static final double MAX_ATM_DISTANCE_SQUARED = 36.0; // 6 Blöcke = 6² = 36

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
            // Validierung: Betrag muss positiv, endlich und im gueltigen Bereich sein
            if (msg.amount <= 0 || Double.isInfinite(msg.amount) || Double.isNaN(msg.amount) || msg.amount > 1_000_000_000) return;

            // Entfernungs-Check (max 6 Bloecke zum ATM)
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > MAX_ATM_DISTANCE_SQUARED) return;

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof ATMBlockEntity atmBE)) return;

            // Führe Transaktion aus
            boolean success;
            if (msg.isDeposit) {
                success = atmBE.deposit(player, msg.amount);
            } else {
                success = atmBE.withdraw(player, msg.amount);
            }

            // Sende aktualisierten Stand zurück an Client
            double newBalance = EconomyManager.getBalance(player.getUUID());
            double newWalletBalance = WalletManager.getBalance(player.getUUID());

            SyncATMDataPacket response = new SyncATMDataPacket(
                newBalance,
                newWalletBalance,
                success,
                ""
            );
            EconomyNetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                response
            );
        });
    }
}
