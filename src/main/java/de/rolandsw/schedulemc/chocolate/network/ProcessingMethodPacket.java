package de.rolandsw.schedulemc.chocolate.network;

import de.rolandsw.schedulemc.chocolate.ChocolateProcessingMethod;
import de.rolandsw.schedulemc.chocolate.blockentity.AbstractMoldingStationBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Molding Station Verarbeitungsmethode-Auswahl (Plain, Filled, Mixed)
 */
public class ProcessingMethodPacket {
    private final BlockPos pos;
    private final ChocolateProcessingMethod method;

    public ProcessingMethodPacket(BlockPos pos, ChocolateProcessingMethod method) {
        this.pos = pos;
        this.method = method;
    }

    public static void encode(ProcessingMethodPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.method);
    }

    public static ProcessingMethodPacket decode(FriendlyByteBuf buf) {
        return new ProcessingMethodPacket(buf.readBlockPos(), buf.readEnum(ChocolateProcessingMethod.class));
    }

    public static void handle(ProcessingMethodPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof AbstractMoldingStationBlockEntity station) {
                station.setProcessingMethod(packet.method);
            }
        });
    }
}
