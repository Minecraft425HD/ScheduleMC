package de.rolandsw.schedulemc.npc.entity;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry für Custom NPC Entities
 */
public class NPCEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<EntityType<CustomNPCEntity>> CUSTOM_NPC = ENTITIES.register("custom_npc",
        () -> EntityType.Builder.of(CustomNPCEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.8F) // Player-Größe
            .clientTrackingRange(10)
            .build("custom_npc")
    );
}
