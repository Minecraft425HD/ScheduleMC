package de.rolandsw.schedulemc.mapview.integration.forge;

import de.rolandsw.schedulemc.mapview.integration.ModApiBridge;
import net.minecraftforge.fml.ModList;

public class ForgeModApiBridge implements ModApiBridge {
    @Override
    public boolean isModEnabled(String modID) {
        return ModList.get().isLoaded(modID);
    }
}
