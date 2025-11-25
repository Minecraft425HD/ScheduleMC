package de.rolandsw.schedulemc.tobacco.entity;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Entities
 */
public class ModEntities {
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ScheduleMC.MOD_ID);
}
