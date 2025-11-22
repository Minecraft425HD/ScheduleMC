package de.rolandsw.schedulemc.client;

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
     * Verhindert dass das Inventar-GUI geöffnet wird
     */
    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent.Opening event) {
        // Prüfe ob das Inventar-GUI geöffnet werden soll
        if (event.getNewScreen() instanceof InventoryScreen) {
            // Blockiere das Öffnen
            event.setCanceled(true);

            // Zeige Nachricht
            if (event.getScreen() != null && event.getScreen().getMinecraft().player != null) {
                event.getScreen().getMinecraft().player.displayClientMessage(
                    Component.literal("§c⚠ Inventar ist gesperrt! Nutze nur die Schnellzugriffsleiste (1-9)."),
                    true
                );
            }
        }
    }
}
