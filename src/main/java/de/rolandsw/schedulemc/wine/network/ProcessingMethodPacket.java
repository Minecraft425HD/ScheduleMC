package de.rolandsw.schedulemc.wine.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.wine.WineProcessingMethod;
import de.rolandsw.schedulemc.wine.blockentity.WineBottlingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Weinabfüllstation Verarbeitungsmethode-Auswahl (Trocken, Halbtrocken, Süß, Dessertwein)
 */
public class ProcessingMethodPacket {
    private final BlockPos pos;
    private final WineProcessingMethod method;

    public ProcessingMethodPacket(BlockPos pos, WineProcessingMethod method) {
        this.pos = pos;
        this.method = method;
    }

    public static void encode(ProcessingMethodPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.method);
    }

    public static ProcessingMethodPacket decode(FriendlyByteBuf buf) {
        return new ProcessingMethodPacket(buf.readBlockPos(), buf.readEnum(WineProcessingMethod.class));
    }

    public static void handle(ProcessingMethodPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof WineBottlingStationBlockEntity station) {
                station.setProcessingMethod(packet.method);
            }
        });
    }
}
