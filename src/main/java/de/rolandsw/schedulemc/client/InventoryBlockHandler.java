package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * CLIENT-SIDE: Blockiert das Öffnen des Inventar-GUIs
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InventoryBlockHandler {

    /**
     * Verhindert dass das Inventar-GUI geöffnet wird (außer für Admins/OP)
     */
    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent.Opening event) {
        EventHelper.handleEvent(() -> {
            // Prüfe ob das Inventar-GUI geöffnet werden soll
            if (event.getNewScreen() instanceof InventoryScreen) {
                Minecraft mc = Minecraft.getInstance();

                // Admins (OP) dürfen weiterhin auf Inventar zugreifen
                if (mc.player != null && mc.player.hasPermissions(2)) {
                    return; // Admin bypass
                }

                // Blockiere das Öffnen für normale Spieler
                event.setCanceled(true);

                // Zeige Nachricht
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                        Component.literal("§c⚠ Inventar ist gesperrt! Nutze nur die Schnellzugriffsleiste (1-9)."),
                        true
                    );
                }
            }
        }, "onGuiOpen");
    }
}
