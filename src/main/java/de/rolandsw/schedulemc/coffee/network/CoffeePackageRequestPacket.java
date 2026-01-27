package de.rolandsw.schedulemc.coffee.network;

import de.rolandsw.schedulemc.coffee.blockentity.CoffeePackagingTableBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Coffee Packaging Table Verpackungs-Anfragen
 * weight: 250, 500, oder 1000 (in Gramm)
 */
public class CoffeePackageRequestPacket {
    private final BlockPos pos;
    private final int weightGrams;

    public CoffeePackageRequestPacket(BlockPos pos, int weightGrams) {
        this.pos = pos;
        this.weightGrams = weightGrams;
    }

    public static void encode(CoffeePackageRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.weightGrams);
    }

    public static CoffeePackageRequestPacket decode(FriendlyByteBuf buf) {
        return new CoffeePackageRequestPacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(CoffeePackageRequestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof CoffeePackagingTableBlockEntity packagingTable) {
                CoffeePackagingTableBlockEntity.PackageSize size = switch (packet.weightGrams) {
                    case 250 -> CoffeePackagingTableBlockEntity.PackageSize.SMALL;
                    case 500 -> CoffeePackagingTableBlockEntity.PackageSize.MEDIUM;
                    case 1000 -> CoffeePackagingTableBlockEntity.PackageSize.LARGE;
                    default -> CoffeePackagingTableBlockEntity.PackageSize.MEDIUM;
                };
                packagingTable.setPackageSize(size);
            }
        });
    }
}
