package de.rolandsw.schedulemc.vehicle;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class DamageSourceVehicle {

    public static final ResourceKey<DamageType> DAMAGE_VEHICLE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Main.MODID, "hit_vehicle"));

}
