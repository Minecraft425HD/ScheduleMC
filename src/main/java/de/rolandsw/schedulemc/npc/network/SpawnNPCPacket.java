package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.entity.NPCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Spawnen eines NPCs vom Client zum Server
 */
public class SpawnNPCPacket {
    private final BlockPos position;
    private final String npcName;
    private final String skinFile;

    public SpawnNPCPacket(BlockPos position, String npcName, String skinFile) {
        this.position = position;
        this.npcName = npcName;
        this.skinFile = skinFile;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(position);
        buf.writeUtf(npcName);
        buf.writeUtf(skinFile);
    }

    public static SpawnNPCPacket decode(FriendlyByteBuf buf) {
        return new SpawnNPCPacket(
            buf.readBlockPos(),
            buf.readUtf(),
            buf.readUtf()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.serverLevel();

                // Spawne NPC
                CustomNPCEntity npc = NPCEntities.CUSTOM_NPC.get().create(level);
                if (npc != null) {
                    // Setze Position (über dem angeklickten Block)
                    npc.setPos(position.getX() + 0.5, position.getY() + 1.0, position.getZ() + 0.5);

                    // Konfiguriere NPC Data
                    NPCData data = new NPCData(npcName, skinFile);

                    // Füge Standard-Dialog hinzu
                    data.addDialogEntry(new NPCData.DialogEntry("Hallo! Ich bin " + npcName + ".", ""));
                    data.addDialogEntry(new NPCData.DialogEntry("Wie kann ich dir helfen?", ""));
                    data.addDialogEntry(new NPCData.DialogEntry("Komm bald wieder!", ""));

                    npc.setNpcData(data);
                    npc.setNpcName(npcName);
                    npc.setSkinFileName(skinFile);

                    // Füge Entity zur Welt hinzu
                    level.addFreshEntity(npc);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
