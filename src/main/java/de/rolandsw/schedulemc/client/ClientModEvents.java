package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.menu.EconomyMenuTypes;
import de.rolandsw.schedulemc.economy.screen.ATMScreen;
import de.rolandsw.schedulemc.tobacco.menu.ModMenuTypes;
import de.rolandsw.schedulemc.tobacco.screen.PackagingTableScreen;
import de.rolandsw.schedulemc.tobacco.screen.TobaccoNegotiationScreen;
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
            MenuScreens.register(ModMenuTypes.PACKAGING_TABLE_MENU.get(), PackagingTableScreen::new);
            MenuScreens.register(ModMenuTypes.TOBACCO_NEGOTIATION_MENU.get(), TobaccoNegotiationScreen::new);
        });
    }
}
