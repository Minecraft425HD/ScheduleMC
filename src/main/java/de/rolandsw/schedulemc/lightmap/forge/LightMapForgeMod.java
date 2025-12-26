package de.rolandsw.schedulemc.lightmap.forge;

import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value = "lightmap")
public class LightMapForgeMod {

    private static IEventBus modEventBus;

    public LightMapForgeMod() {
        LightMapForgeMod.modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LightMapConstants.setEvents(new ForgeEvents());
        LightMapConstants.setPacketBridge(new ForgePacketBridge());
        // Register event listeners early (LightMap initialization happens later in FMLClientSetupEvent)
        LightMapConstants.getEvents().initEvents(LightMapConstants.getLightMapInstance());
    }

    public static IEventBus getModEventBus() {
        return modEventBus;
    }
}
