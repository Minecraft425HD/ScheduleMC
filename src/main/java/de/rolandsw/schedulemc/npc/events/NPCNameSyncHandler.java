package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.client.ClientNPCNameCache;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.SyncNPCNamesPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Event Handler für NPC-Namen Synchronisierung
 * Sendet die Liste aller registrierten NPC-Namen an den Client wenn ein Spieler beitritt
 */
public class NPCNameSyncHandler {

    /**
     * Sendet die NPC-Namen an den Client wenn ein Spieler dem Server beitritt
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // Sende die aktuelle Liste aller NPC-Namen an den Client
            SyncNPCNamesPacket packet = new SyncNPCNamesPacket(NPCNameRegistry.getAllNames());
            NPCNetworkHandler.sendToPlayer(packet, serverPlayer);
        }
    }

    /**
     * Löscht den Client-Cache wenn ein Spieler disconnected
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Nur auf Client-Seite
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientNPCNameCache.clear();
        }
    }

    /**
     * Sendet aktualisierte NPC-Namen an alle Spieler
     * Wird aufgerufen nachdem ein NPC gespawnt oder entfernt wurde
     */
    public static void broadcastNameUpdate(net.minecraft.server.MinecraftServer server) {
        SyncNPCNamesPacket packet = new SyncNPCNamesPacket(NPCNameRegistry.getAllNames());

        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            NPCNetworkHandler.sendToPlayer(packet, player);
        }
    }
}
