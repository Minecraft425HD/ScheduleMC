package de.rolandsw.schedulemc.cheese.network;

import de.rolandsw.schedulemc.cheese.CheeseProcessingMethod;
import de.rolandsw.schedulemc.cheese.blockentity.PackagingStationBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Verpackungsstation Verarbeitungsmethode-Auswahl (Natural, Smoked, Herb)
 */
public class ProcessingMethodPacket {
    private final BlockPos pos;
    private final CheeseProcessingMethod method;

    public ProcessingMethodPacket(BlockPos pos, CheeseProcessingMethod method) {
        this.pos = pos;
        this.method = method;
    }

    public static void encode(ProcessingMethodPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.method);
    }

    public static ProcessingMethodPacket decode(FriendlyByteBuf buf) {
        return new ProcessingMethodPacket(buf.readBlockPos(), buf.readEnum(CheeseProcessingMethod.class));
    }

    public static void handle(ProcessingMethodPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof PackagingStationBlockEntity station) {
                station.setProcessingMethod(packet.method);
            }
        });
    }
}
