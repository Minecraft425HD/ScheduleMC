package de.rolandsw.schedulemc.vehicle.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.vehicle.VehicleMod;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-Side Events for Vehicle System
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VehicleClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register Vehicle Entity Renderer
            EntityRenderers.register(VehicleMod.VEHICLE_ENTITY.get(), VehicleRenderer::new);
        });
    }
}
