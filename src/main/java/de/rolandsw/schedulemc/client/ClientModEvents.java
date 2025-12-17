package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.menu.EconomyMenuTypes;
import de.rolandsw.schedulemc.economy.screen.ATMScreen;
import de.rolandsw.schedulemc.tobacco.menu.ModMenuTypes;
import de.rolandsw.schedulemc.tobacco.screen.SmallPackagingTableScreen;
import de.rolandsw.schedulemc.tobacco.screen.MediumPackagingTableScreen;
import de.rolandsw.schedulemc.tobacco.screen.LargePackagingTableScreen;
import de.rolandsw.schedulemc.tobacco.screen.TobaccoNegotiationScreen;
import de.rolandsw.schedulemc.tobacco.screen.SmallDryingRackScreen;
import de.rolandsw.schedulemc.tobacco.screen.MediumDryingRackScreen;
import de.rolandsw.schedulemc.tobacco.screen.BigDryingRackScreen;
import de.rolandsw.schedulemc.warehouse.menu.WarehouseMenuTypes;
import de.rolandsw.schedulemc.warehouse.screen.WarehouseScreen;
import de.rolandsw.schedulemc.meth.menu.MethMenuTypes;
import de.rolandsw.schedulemc.meth.screen.ReduktionskesselScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * CLIENT-SIDE Event Handler für Screen-Registrierung
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(EconomyMenuTypes.ATM_MENU.get(), ATMScreen::new);
            MenuScreens.register(ModMenuTypes.SMALL_PACKAGING_TABLE_MENU.get(), SmallPackagingTableScreen::new);
            MenuScreens.register(ModMenuTypes.MEDIUM_PACKAGING_TABLE_MENU.get(), MediumPackagingTableScreen::new);
            MenuScreens.register(ModMenuTypes.LARGE_PACKAGING_TABLE_MENU.get(), LargePackagingTableScreen::new);
            MenuScreens.register(ModMenuTypes.TOBACCO_NEGOTIATION_MENU.get(), TobaccoNegotiationScreen::new);
            MenuScreens.register(WarehouseMenuTypes.WAREHOUSE_MENU.get(), WarehouseScreen::new);
            MenuScreens.register(ModMenuTypes.SMALL_DRYING_RACK_MENU.get(), SmallDryingRackScreen::new);
            MenuScreens.register(ModMenuTypes.MEDIUM_DRYING_RACK_MENU.get(), MediumDryingRackScreen::new);
            MenuScreens.register(ModMenuTypes.BIG_DRYING_RACK_MENU.get(), BigDryingRackScreen::new);

            // Meth-System
            MenuScreens.register(MethMenuTypes.REDUKTIONSKESSEL_MENU.get(), ReduktionskesselScreen::new);
        });
    }
}
