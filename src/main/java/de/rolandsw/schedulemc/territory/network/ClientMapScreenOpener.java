package de.rolandsw.schedulemc.territory.network;

import de.rolandsw.schedulemc.mapview.presentation.screen.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-only utility class to open the WorldMapScreen
 * This class is only loaded on the client to avoid server-side class loading errors
 */
@OnlyIn(Dist.CLIENT)
public class ClientMapScreenOpener {

    /**
     * Opens the Territory Map Editor (WorldMapScreen with editMode=true)
     * IMPORTANT: This method must ONLY be called from client-side code
     */
    public static void openMapEditor() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new WorldMapScreen(null, true));
    }
}
