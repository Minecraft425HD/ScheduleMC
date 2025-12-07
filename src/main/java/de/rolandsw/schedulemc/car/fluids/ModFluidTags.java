package de.rolandsw.schedulemc.car.fluids;

import de.rolandsw.schedulemc.car.Main;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModFluidTags {

    public static final TagKey<Fluid> BLINDING = TagKey.create(Registries.FLUID, new ResourceLocation(Main.MODID, "blinding"));

}
