package de.rolandsw.schedulemc.vehicle.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.vehicle.VehicleMod;
import de.rolandsw.schedulemc.vehicle.client.model.SedanModel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side events for the vehicle system.
 * Handles renderer and model layer registration.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VehicleClientEvents {

    /**
     * Registers entity renderers during client setup.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register vehicle entity renderer
            EntityRenderers.register(VehicleMod.VEHICLE_ENTITY.get(), VehicleRenderer::new);
        });
    }

    /**
     * Registers model layer definitions.
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register sedan model layer
        event.registerLayerDefinition(SedanModel.LAYER_LOCATION, SedanModel::createBodyLayer);

        // TODO: Register other vehicle model layers (sport, suv, truck, transporter)
    }
}
