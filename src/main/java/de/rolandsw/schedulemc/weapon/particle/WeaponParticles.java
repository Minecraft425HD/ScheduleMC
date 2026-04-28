package de.rolandsw.schedulemc.weapon.particle;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WeaponParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<SimpleParticleType> MUZZLE_FLASH = PARTICLES.register("weapon_muzzle_flash",
            () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BLOOD = PARTICLES.register("weapon_blood",
            () -> new SimpleParticleType(true));

    /** Großer, dunkler, langlebiger Rauchpartikel für Rauchgranaten */
    public static final RegistryObject<SimpleParticleType> GRENADE_SMOKE = PARTICLES.register("grenade_smoke",
            () -> new SimpleParticleType(true));
}
