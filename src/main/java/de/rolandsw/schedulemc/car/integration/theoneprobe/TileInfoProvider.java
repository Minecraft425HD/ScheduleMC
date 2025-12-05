package de.rolandsw.schedulemc.car.integration.theoneprobe;

import de.rolandsw.schedulemc.car.Main;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileInfoProvider implements IProbeInfoProvider {

    public static final ResourceLocation ID = new ResourceLocation(Main.MODID, "probeinfoprovider");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player playerEntity, Level world, BlockState blockState, IProbeHitData iProbeHitData) {
        // No custom probe info for remaining blocks
    }
}
