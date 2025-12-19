package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Synchronisieren von NPC Daten zum Client
 */
public class SyncNPCDataPacket {
    private final int entityId;
    private final CompoundTag npcData;

    public SyncNPCDataPacket(int entityId, CompoundTag npcData) {
        this.entityId = entityId;
        this.npcData = npcData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeNbt(npcData);
    }

    public static SyncNPCDataPacket decode(FriendlyByteBuf buf) {
        return new SyncNPCDataPacket(
            buf.readInt(),
            buf.readNbt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleClientPacket(ctx, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(entityId);
                if (entity instanceof CustomNPCEntity npc && npcData != null) {
                    npc.getNpcData().load(npcData);
                }
            }
        });
    }
}
