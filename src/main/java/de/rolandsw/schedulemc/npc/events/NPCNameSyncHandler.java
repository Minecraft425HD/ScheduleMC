package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.client.ClientNPCNameCache;
import de.rolandsw.schedulemc.npc.network.DeltaSyncNPCNamesPacket;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.SyncNPCNamesPacket;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Event Handler für NPC-Namen Synchronisierung
 *
 * OPTIMIERT: Verwendet Delta-Sync für Änderungen statt Full-Sync.
 * - Bei Login: Full-Sync (alle Namen)
 * - Bei Änderungen: Delta-Sync (nur hinzugefügte/entfernte Namen)
 */
public class NPCNameSyncHandler {

    /**
     * Sendet die NPC-Namen an den Client wenn ein Spieler dem Server beitritt
     * (Full-Sync beim Login)
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        EventHelper.handlePlayerJoin(event, serverPlayer -> {
            // Full-Sync bei Login
            SyncNPCNamesPacket packet = new SyncNPCNamesPacket(NPCNameRegistry.getAllNames());
            NPCNetworkHandler.sendToPlayer(packet, serverPlayer);
        });
    }

    /**
     * Löscht den Client-Cache wenn ein Spieler disconnected
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EventHelper.handleEvent(() -> {
            // Nur auf Client-Seite
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientNPCNameCache.clear();
            }
        }, "onPlayerLogout");
    }

    /**
     * DEPRECATED: Verwendet Delta-Sync stattdessen
     * @see #broadcastNameAdded(net.minecraft.server.MinecraftServer, String)
     * @see #broadcastNameRemoved(net.minecraft.server.MinecraftServer, String)
     */
    @Deprecated
    public static void broadcastNameUpdate(net.minecraft.server.MinecraftServer server) {
        SyncNPCNamesPacket packet = new SyncNPCNamesPacket(NPCNameRegistry.getAllNames());

        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            NPCNetworkHandler.sendToPlayer(packet, player);
        }
    }

    /**
     * Sendet Delta-Update wenn ein NPC-Name hinzugefügt wurde
     * OPTIMIERT: Sendet nur den neuen Namen statt aller Namen
     */
    public static void broadcastNameAdded(net.minecraft.server.MinecraftServer server, String name) {
        DeltaSyncNPCNamesPacket packet = new DeltaSyncNPCNamesPacket(
            DeltaSyncNPCNamesPacket.Operation.ADD, name);

        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            NPCNetworkHandler.sendToPlayer(packet, player);
        }
    }

    /**
     * Sendet Delta-Update wenn ein NPC-Name entfernt wurde
     * OPTIMIERT: Sendet nur den entfernten Namen statt aller Namen
     */
    public static void broadcastNameRemoved(net.minecraft.server.MinecraftServer server, String name) {
        DeltaSyncNPCNamesPacket packet = new DeltaSyncNPCNamesPacket(
            DeltaSyncNPCNamesPacket.Operation.REMOVE, name);

        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            NPCNetworkHandler.sendToPlayer(packet, player);
        }
    }
}
