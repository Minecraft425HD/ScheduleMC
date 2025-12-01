package de.rolandsw.schedulemc.warehouse.events;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.items.WarehouseTool;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event-Handler für Warehouse-Tool Interaktionen
 * Fängt Linksklicks auf NPCs ab, wenn der Spieler das Warehouse-Tool hält
 */
@Mod.EventBusSubscriber
public class WarehouseToolEventHandler {

    /**
     * Fängt Linksklicks auf Entities ab (Attack-Events)
     */
    @SubscribeEvent
    public static void onLeftClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();

        // Prüfe ob Spieler Warehouse-Tool hält
        ItemStack heldItem = player.getItemInHand(event.getHand());
        if (!(heldItem.getItem() instanceof WarehouseTool)) {
            return;
        }

        // Prüfe ob das Target ein NPC ist
        if (!(event.getTarget() instanceof CustomNPCEntity npc)) {
            return;
        }

        // Rufe die Handler-Methode im Tool auf
        WarehouseTool.onLeftClickNPC(player, npc, heldItem);

        // Event canceln, damit der NPC nicht angegriffen wird
        event.setCanceled(true);
    }

    /**
     * Alternative: Fängt Attack-Events ab (funktioniert auch für Linksklick)
     * Wichtig für Linksklick-Erkennung!
     */
    @SubscribeEvent
    public static void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        Player player = event.getEntity();

        // Prüfe beide Hände
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.getItem() instanceof WarehouseTool) {
                // Prüfe ob das Target ein NPC ist
                if (event.getTarget() instanceof CustomNPCEntity npc) {
                    // Rufe die Handler-Methode im Tool auf
                    WarehouseTool.onLeftClickNPC(player, npc, heldItem);

                    // Event canceln, damit der NPC nicht angegriffen wird
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }
}
