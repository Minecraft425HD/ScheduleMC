package de.rolandsw.schedulemc.honey.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.honey.HoneyProcessingMethod;
import de.rolandsw.schedulemc.honey.blockentity.BottlingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Honigabfüllstation Verarbeitungsmethode-Auswahl (Liquid, Creamed, Chunk)
 */
public class ProcessingMethodPacket {
    private final BlockPos pos;
    private final HoneyProcessingMethod method;

    public ProcessingMethodPacket(BlockPos pos, HoneyProcessingMethod method) {
        this.pos = pos;
        this.method = method;
    }

    public static void encode(ProcessingMethodPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.method);
    }

    public static ProcessingMethodPacket decode(FriendlyByteBuf buf) {
        return new ProcessingMethodPacket(buf.readBlockPos(), buf.readEnum(HoneyProcessingMethod.class));
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
