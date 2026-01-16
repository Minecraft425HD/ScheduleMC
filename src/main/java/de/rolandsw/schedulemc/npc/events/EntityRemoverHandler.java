package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.items.EntityRemoverItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Event-Handler für das Entity-Remover Admin-Tool
 * Behandelt Linksklick auf Entities (NPCs und Fahrzeuge)
 */
public class EntityRemoverHandler {

    @SubscribeEvent
    public void onLeftClickEntity(AttackEntityEvent event) {
        Player player = event.getEntity();

        if (player == null || player.level().isClientSide) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();

        // Prüfe ob Entity-Remover gehalten wird
        if (!(heldItem.getItem() instanceof EntityRemoverItem)) {
            return;
        }

        // Delegiere an EntityRemoverItem
        boolean handled = EntityRemoverItem.onEntityInteract(
            player,
            event.getTarget(),
            heldItem
        );

        if (handled) {
            event.setCanceled(true); // Verhindere normalen Angriff
        }
    }
}
