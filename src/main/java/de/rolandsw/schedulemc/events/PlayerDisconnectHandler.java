package de.rolandsw.schedulemc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.items.PlotSelectionTool;
import de.rolandsw.schedulemc.npc.events.PoliceAIHandler;
import de.rolandsw.schedulemc.npc.events.PoliceBackupSystem;
import de.rolandsw.schedulemc.npc.events.PoliceSearchBehavior;
import de.rolandsw.schedulemc.npc.items.NPCLeisureTool;
import de.rolandsw.schedulemc.npc.items.NPCLocationTool;
import de.rolandsw.schedulemc.npc.items.NPCPatrolTool;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Handler für Player Disconnect Events
 *
 * Verhindert Memory Leaks durch Cleanup aller spieler-bezogenen Maps:
 * - PoliceAIHandler (4 Maps: playerCache, arrestTimers, lastSyncedWantedLevel, lastSyncedEscapeTime)
 * - PoliceSearchBehavior (5 Maps)
 * - PoliceBackupSystem (2 Maps)
 * - PlotSelectionTool (2 Maps)
 * - NPCLocationTool (1 Map)
 * - NPCLeisureTool (1 Map)
 * - NPCPatrolTool (1 Map)
 *
 * SICHERHEIT: Thread-safe da alle cleanup() Methoden ConcurrentHashMap verwenden
 */
public class PlayerDisconnectHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Event-Handler für Player Logout
     * Ruft alle cleanup() Methoden auf um Memory Leaks zu verhindern
     */
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID playerUUID = player.getUUID();
        String playerName = player.getName().getString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[CLEANUP] Player {} ({}) disconnected - cleaning up memory", playerName, playerUUID);
        }

        try {
            // Cleanup Police Systems
            PoliceAIHandler.cleanupPlayer(playerUUID);
            PoliceSearchBehavior.cleanup(playerUUID);
            PoliceBackupSystem.cleanup(playerUUID);

            // Cleanup Plot Selection
            PlotSelectionTool.cleanup(playerUUID);

            // Cleanup NPC Tools
            NPCLocationTool.cleanup(playerUUID);
            NPCLeisureTool.cleanup(playerUUID);
            NPCPatrolTool.cleanup(playerUUID);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[CLEANUP] Successfully cleaned up all data for player {}", playerName);
            }

        } catch (Exception e) {
            LOGGER.error("[CLEANUP] ERROR during cleanup for player {}: {}", playerName, e.getMessage(), e);
        }
    }
}
