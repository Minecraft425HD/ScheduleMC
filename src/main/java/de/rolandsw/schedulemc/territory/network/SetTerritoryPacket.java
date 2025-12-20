package de.rolandsw.schedulemc.territory.network;

import de.rolandsw.schedulemc.territory.TerritoryManager;
import de.rolandsw.schedulemc.territory.TerritoryType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet um Territory vom Client zum Server zu setzen
 */
public class SetTerritoryPacket {

    private final int chunkX;
    private final int chunkZ;
    private final TerritoryType type;
    private final String name;
    private final boolean remove;

    public SetTerritoryPacket(int chunkX, int chunkZ, TerritoryType type, String name) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.type = type;
        this.name = name;
        this.remove = false;
    }

    public SetTerritoryPacket(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.type = null;
        this.name = "";
        this.remove = true;
    }

    public static void encode(SetTerritoryPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.chunkX);
        buf.writeInt(msg.chunkZ);
        buf.writeBoolean(msg.remove);

        if (!msg.remove) {
            buf.writeEnum(msg.type);
            buf.writeUtf(msg.name);
        }
    }

    public static SetTerritoryPacket decode(FriendlyByteBuf buf) {
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        boolean remove = buf.readBoolean();

        if (remove) {
            return new SetTerritoryPacket(chunkX, chunkZ);
        } else {
            TerritoryType type = buf.readEnum(TerritoryType.class);
            String name = buf.readUtf();
            return new SetTerritoryPacket(chunkX, chunkZ, type, name);
        }
    }

    public static void handle(SetTerritoryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Nur OPs dürfen Territories setzen
            if (!player.hasPermissions(2)) {
                return;
            }

            TerritoryManager manager = TerritoryManager.getInstance(player.server);
            if (manager != null) {
                if (msg.remove) {
                    manager.removeTerritory(msg.chunkX, msg.chunkZ);
                } else {
                    manager.setTerritory(msg.chunkX, msg.chunkZ, msg.type, msg.name, null);
                }

                // Sync territories back to client
                TerritoryNetworkHandler.sendToPlayer(new SyncTerritoriesPacket(manager.getTerritoriesMap()), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
