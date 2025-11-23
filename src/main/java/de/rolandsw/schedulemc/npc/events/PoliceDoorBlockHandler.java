package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Blockiert Türinteraktionen während aktiver Polizeiverfolgung
 *
 * Spieler können Türen erst öffnen, wenn sie die Polizei abgehängt haben
 * und der Escape-Timer läuft.
 */
public class PoliceDoorBlockHandler {

    @SubscribeEvent
    public void onDoorInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!ModConfigHandler.COMMON.POLICE_BLOCK_DOORS_DURING_PURSUIT.get()) {
            return; // Feature deaktiviert
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Prüfe ob Spieler mit einer Tür interagiert
        if (!(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof DoorBlock)) {
            return;
        }

        // Prüfe ob Spieler Wanted-Level hat
        int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());
        if (wantedLevel <= 0) {
            return; // Kein Wanted-Level, alles OK
        }

        // Prüfe ob Spieler sich versteckt (Escape-Timer läuft)
        boolean isHiding = CrimeManager.isHiding(player.getUUID());

        if (!isHiding) {
            // Spieler wird noch verfolgt → Tür blockiert!
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c✗ Du kannst keine Türen öffnen, während du verfolgt wirst!"));
            player.sendSystemMessage(Component.literal("§7Hänge die Polizei ab, um Türen zu benutzen."));
        }
        // Wenn isHiding = true, kann der Spieler Türen öffnen
    }
}
