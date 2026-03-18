package de.rolandsw.schedulemc.weapon.entity;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WeaponEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<EntityType<ThrownWeaponGrenade>> THROWN_WEAPON_GRENADE = ENTITIES.register("thrown_weapon_grenade",
            () -> EntityType.Builder.<ThrownWeaponGrenade>of(ThrownWeaponGrenade::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation(ScheduleMC.MOD_ID, "thrown_weapon_grenade").toString()));

    public static final RegistryObject<EntityType<WeaponBulletEntity>> WEAPON_BULLET = ENTITIES.register("weapon_bullet",
            () -> EntityType.Builder.<WeaponBulletEntity>of(WeaponBulletEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .build(new ResourceLocation(ScheduleMC.MOD_ID, "weapon_bullet").toString()));
}
