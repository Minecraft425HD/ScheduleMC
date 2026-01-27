package de.rolandsw.schedulemc.beer.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.beer.BeerProcessingMethod;
import de.rolandsw.schedulemc.beer.blockentity.BottlingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Bierabfüllstation Verarbeitungsmethode-Auswahl (DRAFT/BOTTLED/CANNED)
 */
public class ProcessingMethodPacket {
    private final BlockPos pos;
    private final BeerProcessingMethod method;

    public ProcessingMethodPacket(BlockPos pos, BeerProcessingMethod method) {
        this.pos = pos;
        this.method = method;
    }

    public static void encode(ProcessingMethodPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.method);
    }

    public static ProcessingMethodPacket decode(FriendlyByteBuf buf) {
        return new ProcessingMethodPacket(buf.readBlockPos(), buf.readEnum(BeerProcessingMethod.class));
    }

    public static void handle(ProcessingMethodPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof BottlingStationBlockEntity station) {
                station.setProcessingMethod(packet.method);
            }
        });
    }
}
