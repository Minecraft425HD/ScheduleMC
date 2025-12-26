package de.rolandsw.schedulemc.lightmap.forge;

import de.rolandsw.schedulemc.lightmap.ModApiBridge;
import net.minecraftforge.fml.ModList;

public class ForgeModApiBridge implements ModApiBridge {
    @Override
    public boolean isModEnabled(String modID) {
        return ModList.get().isLoaded(modID);
    }
}
