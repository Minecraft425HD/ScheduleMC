package de.rolandsw.schedulemc.mapview.entityrender.variants;

import com.google.common.collect.Maps;
import de.rolandsw.schedulemc.mapview.entityrender.EntityVariantData;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;

public class HorseVariantDataFactory extends DefaultEntityVariantDataFactory {
    private static final ResourceLocation INVISIBLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "invisible");
    private static final Map<Variant, ResourceLocation> LOCATION_BY_VARIANT = Maps.newEnumMap(
            Map.of(
                    Variant.values()[0],
                    INVISIBLE_TEXTURE,
                    Variant.values()[1],
                    ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/horse/horse_markings_white.png"),
                    Variant.values()[2],
                    ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/horse/horse_markings_whitefield.png"),
                    Variant.values()[3],
                    ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/horse/horse_markings_whitedots.png"),
                    Variant.values()[4],
                    ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/horse/horse_markings_blackdots.png")));

    public HorseVariantDataFactory(EntityType<?> type) {
        super(type);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public EntityVariantData createVariantData(Entity entity, EntityRenderer renderer, int size, boolean addBorder) {
        Horse horse = (Horse) entity;
        Variant variant = horse.getVariant();
        ResourceLocation secondaryTexture = LOCATION_BY_VARIANT.get(variant);
        return new DefaultEntityVariantData(getType(), ((LivingEntityRenderer) renderer).getTextureLocation(entity), secondaryTexture == INVISIBLE_TEXTURE ? null : secondaryTexture, size, addBorder);
    }

}
