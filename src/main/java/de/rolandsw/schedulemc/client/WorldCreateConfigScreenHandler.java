package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.gui.config.ConfigCategoryScreen;
import de.rolandsw.schedulemc.config.DefaultConfigEditor;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Fügt dem Welt-erstellen-Screen einen garantiert sichtbaren Button "ScheduleMC Config"
 * hinzu (oben rechts), mit dem die Pro-Welt-Config-Vorlage für NEUE Welten bearbeitet
 * werden kann ({@link DefaultConfigEditor}).
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

            Button configButton = Button.builder(
                Component.literal("⚙ ScheduleMC Config"),
                button -> {
                    DefaultConfigEditor.begin();
                    Minecraft.getInstance().setScreen(new ConfigCategoryScreen(screen, true));
                }
            ).bounds(screen.width - 155, 6, 150, 20).build();

            event.addListener(configButton);
        }, "onWorldCreateScreenInit");
    }
}
