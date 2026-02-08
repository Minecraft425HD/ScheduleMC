package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Blockiert Türinteraktionen während aktiver Polizeiverfolgung
 *
 * Türen sind blockiert wenn:
 * 1. Spieler hat Wanted-Level
 * 2. Polizei ist in der Nähe
 * 3. Escape-Timer läuft NICHT (Spieler versteckt sich nicht)
 */
public class PoliceDoorBlockHandler {

    @SubscribeEvent
    public void onDoorInteract(PlayerInteractEvent.RightClickBlock event) {
        EventHelper.handleRightClickBlock(event, player -> {
            if (!ModConfigHandler.COMMON.POLICE_BLOCK_DOORS_DURING_PURSUIT.get()) {
                return; // Feature deaktiviert
            }

            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            // Prüfe ob Spieler mit einer Tür interagiert
            if (!(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof DoorBlock)) {
                return;
            }

            // Prüfe ob Spieler Wanted-Level hat
            int wantedLevel = CrimeManager.getWantedLevel(serverPlayer.getUUID());
            if (wantedLevel <= 0) {
                return; // Kein Wanted-Level, alles OK
            }

            // Prüfe ob Spieler sich versteckt (Escape-Timer läuft)
            boolean isHiding = CrimeManager.isHiding(serverPlayer.getUUID());

            if (isHiding) {
                return; // Spieler versteckt sich → Türen erlaubt
            }

            // FIX 6: Nutze PoliceAIHandler-Cache statt unkached getEntitiesOfClass
            int detectionRadius = ModConfigHandler.COMMON.POLICE_DETECTION_RADIUS.get();
            List<CustomNPCEntity> nearbyPolice = new ArrayList<>();
            PoliceAIHandler.getPoliceInRadius(serverPlayer.position(), detectionRadius, nearbyPolice);

            if (!nearbyPolice.isEmpty()) {
                // Polizei ist in der Nähe → Tür blockiert!
                event.setCanceled(true);
                serverPlayer.sendSystemMessage(Component.translatable("message.police.cannot_use_doors"));
                serverPlayer.sendSystemMessage(Component.translatable("message.protection.hide_from_police"));
            }
        });
    }
}
