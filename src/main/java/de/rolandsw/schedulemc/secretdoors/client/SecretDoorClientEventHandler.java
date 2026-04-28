package de.rolandsw.schedulemc.secretdoors.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blocks.AbstractSecretDoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Client-seitiger Event-Handler für Secret Doors.
 *
 * Wenn eine Geheimtür oder Bodenluke geöffnet ist (OPEN=true), wird der
 * gelbe Selektionsrahmen nur für berechtigte Spieler angezeigt:
 *
 *   - Admins (Permission-Level ≥ 2) sehen ihn immer.
 *   - Der Besitzer der Tür (= derjenige der sie gesetzt hat, i.d.R.
 *     der Plot-Käufer oder -Mieter) sieht ihn ebenfalls.
 *   - Alle anderen Spieler sehen keinen Rahmen – die Tür ist für sie
 *     vollständig unsichtbar und nicht als Block erkennbar.
 *
 * Technischer Hintergrund: getShape() liefert weiterhin Shapes.block(),
 * damit der Controller-Block immer anvisierbar und abbaubar bleibt.
 * RenderHighlightEvent.Block kontrolliert nur die visuelle Darstellung des Rahmens.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SecretDoorClientEventHandler {

    @SubscribeEvent
    public static void onDrawHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        BlockPos pos = event.getTarget().getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        // Nur für offene Geheimtüren / Bodenluken relevant
        boolean isSecretDoor = state.is(SecretDoors.SECRET_DOOR.get())
                            || state.is(SecretDoors.HATCH.get());
        if (!isSecretDoor || !state.getValue(AbstractSecretDoorBlock.OPEN)) return;

        // Admins (OP-Level ≥ 2) sehen den Rahmen immer
        if (mc.player.hasPermissions(2)) return;

        // Besitzer der Tür sieht den Rahmen (ownerId wird vom Server synced)
        if (mc.level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be) {
            UUID ownerId = be.getOwnerId();
            // Kein Owner gesetzt → Tür ist noch "öffentlich", Rahmen für alle
            if (ownerId == null) return;
            // Spieler ist Besitzer → Rahmen anzeigen
            if (ownerId.equals(mc.player.getUUID())) return;
        }

        // Kein Zugriff → Selektionsrahmen verstecken
        event.setCanceled(true);
    }
}
