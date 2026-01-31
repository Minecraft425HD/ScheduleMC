package de.rolandsw.schedulemc.events;

import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Sperrt das Spieler-Inventar (Slots 9-35)
 * Erlaubt nur die Schnellzugriffsleiste (Slots 0-8)
 */
public class InventoryRestrictionHandler {

    private static final int HOTBAR_SIZE = 9; // Slots 0-8
    private static final int INVENTORY_START = 9; // Slots 9-35 (3x9 Reihen)
    private static final int INVENTORY_END = 36;

    /**
     * Verhindert dass Items in das gesperrte Inventar gelegt werden
     * Verschiebt Items automatisch zurück in die Hotbar
     * Admins (OP) sind ausgenommen
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EventHelper.handlePlayerTickEnd(event, player -> {

        // Admins (OP) dürfen volles Inventar nutzen
        if (player.hasPermissions(2)) {
            return; // Admin bypass
        }

        // Prüfe alle Inventar-Slots (nicht Hotbar)
        for (int i = INVENTORY_START; i < INVENTORY_END; i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!stack.isEmpty()) {
                // Item gefunden im gesperrten Inventar
                // Versuche es in die Hotbar zu verschieben
                boolean moved = false;

                for (int hotbarSlot = 0; hotbarSlot < HOTBAR_SIZE; hotbarSlot++) {
                    ItemStack hotbarStack = player.getInventory().getItem(hotbarSlot);

                    if (hotbarStack.isEmpty()) {
                        // Leerer Hotbar-Slot gefunden
                        player.getInventory().setItem(hotbarSlot, stack.copy());
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                        moved = true;
                        break;
                    } else if (ItemStack.isSameItemSameTags(hotbarStack, stack) &&
                               hotbarStack.getCount() + stack.getCount() <= hotbarStack.getMaxStackSize()) {
                        // Stackable Item gefunden
                        hotbarStack.grow(stack.getCount());
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                        moved = true;
                        break;
                    }
                }

                if (!moved) {
                    // Kein Platz in Hotbar - Item droppen
                    player.drop(stack, false);
                    player.getInventory().setItem(i, ItemStack.EMPTY);

                    player.displayClientMessage(Component.translatable(
                        "message.inventory.locked_item_dropped"
                    ), true);
                } else {
                    player.displayClientMessage(Component.translatable(
                        "message.inventory.locked_item_moved"
                    ), true);
                }
            }
        }
        });
    }
}
