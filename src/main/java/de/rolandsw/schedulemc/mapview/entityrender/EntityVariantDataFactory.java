package de.rolandsw.schedulemc.lightmap.entityrender;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public interface EntityVariantDataFactory {

    EntityType<?> getType();

    EntityVariantData createVariantData(Entity entity, @SuppressWarnings("rawtypes") EntityRenderer renderer, int size, boolean addBorder);

}