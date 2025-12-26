package de.rolandsw.schedulemc.mapview.forge;

import de.rolandsw.schedulemc.mapview.ModApiBridge;
import net.minecraftforge.fml.ModList;

public class ForgeModApiBridge implements ModApiBridge {
    @Override
    public boolean isModEnabled(String modID) {
        return ModList.get().isLoaded(modID);
    }
}
