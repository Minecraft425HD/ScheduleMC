package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.screen.SmartphoneScreen;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
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
            EventHelper.handleEvent(() -> {
                event.register(KeyBindings.OPEN_SMARTPHONE);
            }, "onRegisterKeyMappings");
        }
    }

    /**
     * Handler für das Drücken der Smartphone-Taste
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        EventHelper.handleEvent(() -> {
            Minecraft mc = Minecraft.getInstance();

            // Nur verarbeiten wenn im Spiel und kein anderes GUI offen ist
            if (mc.screen == null && KeyBindings.OPEN_SMARTPHONE.consumeClick()) {
                if (mc.player != null) {
                    Entity riding = mc.player.getVehicle();
                    if (riding instanceof EntityGenericVehicle vehicle && vehicle.getPhysicsComponent().isStarted()) {
                        mc.player.displayClientMessage(Component.translatable("message.smartphone.engine_running"), true);
                        return;
                    }
                }
                mc.setScreen(new SmartphoneScreen());
            }
            // Wenn Smartphone bereits offen ist, schließen
            else if (mc.screen instanceof SmartphoneScreen && KeyBindings.OPEN_SMARTPHONE.consumeClick()) {
                mc.setScreen(null);
            }
        }, "onKeyInput");
    }
}
