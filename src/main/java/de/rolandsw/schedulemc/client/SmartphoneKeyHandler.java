package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.screen.SmartphoneScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handler für Smartphone Keybinding Events
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, value = Dist.CLIENT)
public class SmartphoneKeyHandler {

    /**
     * Registriert die Custom Keybindings
     */
    @Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEventHandler {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(KeyBindings.OPEN_SMARTPHONE);
        }
    }

    /**
     * Handler für das Drücken der Smartphone-Taste
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        // Nur verarbeiten wenn im Spiel und kein anderes GUI offen ist
        if (mc.screen == null && KeyBindings.OPEN_SMARTPHONE.consumeClick()) {
            mc.setScreen(new SmartphoneScreen());
        }
        // Wenn Smartphone bereits offen ist, schließen
        else if (mc.screen instanceof SmartphoneScreen && KeyBindings.OPEN_SMARTPHONE.consumeClick()) {
            mc.setScreen(null);
        }
    }
}
