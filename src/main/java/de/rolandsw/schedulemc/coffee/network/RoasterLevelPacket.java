package de.rolandsw.schedulemc.coffee.network;

import de.rolandsw.schedulemc.coffee.CoffeeRoastLevel;
import de.rolandsw.schedulemc.coffee.blockentity.AbstractCoffeeRoasterBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Kaffeeröster Röstgrad-Auswahl
 * Funktioniert für alle drei Röster (Small, Medium, Large)
 */
public class RoasterLevelPacket {
    private final BlockPos pos;
    private final CoffeeRoastLevel roastLevel;

    public RoasterLevelPacket(BlockPos pos, CoffeeRoastLevel roastLevel) {
        this.pos = pos;
        this.roastLevel = roastLevel;
    }

    public static void encode(RoasterLevelPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.roastLevel);
    }

    public static RoasterLevelPacket decode(FriendlyByteBuf buf) {
        return new RoasterLevelPacket(buf.readBlockPos(), buf.readEnum(CoffeeRoastLevel.class));
    }

    public static void handle(RoasterLevelPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof AbstractCoffeeRoasterBlockEntity roaster) {
                roaster.setRoastLevel(packet.roastLevel);
            }
        });
    }
}
