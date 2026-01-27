package de.rolandsw.schedulemc.coffee.network;

import de.rolandsw.schedulemc.coffee.CoffeeGrindSize;
import de.rolandsw.schedulemc.coffee.blockentity.CoffeeGrinderBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Kaffeemühle Mahlgrad-Auswahl
 */
public class GrindSizePacket {
    private final BlockPos pos;
    private final CoffeeGrindSize grindSize;

    public GrindSizePacket(BlockPos pos, CoffeeGrindSize grindSize) {
        this.pos = pos;
        this.grindSize = grindSize;
    }

    public static void encode(GrindSizePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.grindSize);
    }

    public static GrindSizePacket decode(FriendlyByteBuf buf) {
        return new GrindSizePacket(buf.readBlockPos(), buf.readEnum(CoffeeGrindSize.class));
    }

    public static void handle(GrindSizePacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof CoffeeGrinderBlockEntity grinder) {
                grinder.setGrindSize(packet.grindSize);
            }
        });
    }
}
