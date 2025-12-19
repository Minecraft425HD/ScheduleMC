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

            // Prüfe ob Polizei in der Nähe ist
            int detectionRadius = ModConfigHandler.COMMON.POLICE_DETECTION_RADIUS.get();
            List<CustomNPCEntity> nearbyPolice = serverPlayer.level().getEntitiesOfClass(
                CustomNPCEntity.class,
                AABB.ofSize(serverPlayer.position(), detectionRadius, detectionRadius, detectionRadius),
                npc -> npc.getNpcType() == NPCType.POLIZEI && !npc.getPersistentData().getBoolean("IsKnockedOut")
            );

            if (!nearbyPolice.isEmpty()) {
                // Polizei ist in der Nähe → Tür blockiert!
                event.setCanceled(true);
                serverPlayer.sendSystemMessage(Component.literal("§c✗ Du kannst keine Türen öffnen, während die Polizei dich verfolgt!"));
                serverPlayer.sendSystemMessage(Component.literal("§7Verstecke dich vor der Polizei, um Türen zu benutzen."));
            }
        });
    }
}
