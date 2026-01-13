package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.items.EntityRemoverItem;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Event-Handler für das Entity-Remover Admin-Tool
 * Behandelt Fahrzeug-Interaktionen (Fahrzeuge sind keine LivingEntities)
 */
public class EntityRemoverHandler {

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EventHelper.handleEntityInteract(event, player -> {
            // Nur Main-Hand
            if (event.getHand() != InteractionHand.MAIN_HAND) return;

            ItemStack heldItem = player.getMainHandItem();

            // Prüfe ob Entity-Remover gehalten wird
            if (!(heldItem.getItem() instanceof EntityRemoverItem)) return;

            // Delegiere an EntityRemoverItem
            boolean handled = EntityRemoverItem.onEntityInteract(
                player,
                event.getTarget(),
                heldItem
            );

            if (handled) {
                event.setCanceled(true);
            }
        });
    }
}
