package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Lightweight Packet zum Synchronisieren nur des NPC Wallet (Balance) zum Client
 *
 * Performance-Optimierung:
 * - SyncNPCDataPacket sendet komplettes CompoundTag (500-2000 Bytes)
 * - Dieses Paket sendet nur entityId + wallet (8 Bytes)
 * - 99% Größenreduktion für Wallet-Updates
 */
public class SyncNPCBalancePacket {
    private final int entityId;
    private final int wallet;

    public SyncNPCBalancePacket(int entityId, int wallet) {
        this.entityId = entityId;
        this.wallet = wallet;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);  // VarInt für kompaktere Kodierung (1-5 Bytes)
        buf.writeVarInt(wallet);    // VarInt (1-5 Bytes)
        // Total: 2-10 Bytes vs 500-2000 Bytes!
    }

    public static SyncNPCBalancePacket decode(FriendlyByteBuf buf) {
        return new SyncNPCBalancePacket(
            buf.readVarInt(),
            buf.readVarInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleClientPacket(ctx, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(entityId);
                if (entity instanceof CustomNPCEntity npc) {
                    npc.getNpcData().setWallet(wallet);
                }
            }
        });
    }
}
