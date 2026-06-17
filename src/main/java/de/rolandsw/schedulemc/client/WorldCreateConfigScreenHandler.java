package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Fallback für den "ScheduleMC Config"-Button im Welt-erstellen-Screen: Greift der Mixin
 * (Button im "More"-Reiter) NICHT, wird hier ein garantiert sichtbarer Button oben rechts
 * eingefügt. So ist immer genau ein Button vorhanden.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WorldCreateConfigScreenHandler {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        EventHelper.handleEvent(() -> {
            Screen screen = event.getScreen();
            if (!(screen instanceof CreateWorldScreen)) {
                return;
            }
            // Mixin hat den Button bereits in den More-Reiter gesetzt -> kein Fallback nötig.
            if (WorldCreateConfigState.moreTabButtonAdded) {
                return;
            }

            Button configButton = Button.builder(
                Component.literal("⚙ ScheduleMC Config"),
                button -> WorldCreateConfigState.openConfig(screen)
            ).bounds(screen.width - 155, 6, 150, 20).build();

            event.addListener(configButton);
        }, "onWorldCreateScreenInit");
    }
}
