package de.rolandsw.schedulemc.mapview.entityrender.variants;

import de.rolandsw.schedulemc.mapview.entityrender.EntityVariantData;
import de.rolandsw.schedulemc.mapview.entityrender.EntityVariantDataFactory;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class DefaultEntityVariantDataFactory implements EntityVariantDataFactory {
    private final EntityType<?> type;
    private final ResourceLocation secondaryTexture;

    public DefaultEntityVariantDataFactory(EntityType<?> type) {
        this(type, null);
    }

    public DefaultEntityVariantDataFactory(EntityType<?> type, ResourceLocation secondaryTexture) {
        this.type = type;
        this.secondaryTexture = secondaryTexture;
    }

    @Override
    public EntityType<?> getType() {
        return type;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public EntityVariantData createVariantData(Entity entity, EntityRenderer renderer, int size, boolean addBorder) {
        if (renderer instanceof EnderDragonRenderer) {
            return null;
        }

        return new DefaultEntityVariantData(type, ((LivingEntityRenderer) renderer).getTextureLocation(entity), secondaryTexture, size, addBorder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static EntityVariantData createSimpleVariantData(Entity entity, EntityRenderer renderer, int size, boolean addBorder) {
        if (renderer instanceof EnderDragonRenderer) {
            return null;
        }

        return new DefaultEntityVariantData(entity.getType(), ((LivingEntityRenderer) renderer).getTextureLocation(entity), null, size, addBorder);
    }

}
