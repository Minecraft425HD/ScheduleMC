package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-seitiger Handler für Plot-Info-Block
 * Diese Klasse wird nur auf der Client-Seite geladen
 */
@OnlyIn(Dist.CLIENT)
public class PlotInfoClientHandler {

    /**
     * Öffnet das Plot-Info-GUI (nur Client-Seite)
     */
    public static void openPlotInfoScreen(PlotRegion plot) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PlotInfoScreen(plot));
    }
}
