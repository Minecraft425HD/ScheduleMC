package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.client.obj.OBJModel;
import de.maxhenkel.corelib.client.obj.OBJModelInstance;
import de.maxhenkel.corelib.client.obj.OBJModelOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PartBody extends PartModel {

    protected Vector3d[] wheelOffsets;
    protected Vector3d[] playerOffsets;
    protected Vector3d numberPlateOffset;
    protected float width;
    protected float height;
    protected float minRotationSpeed;
    protected float maxRotationSpeed;
    protected Supplier<Float> fuelEfficiency;
    protected Supplier<Float> acceleration;
    protected Supplier<Float> maxSpeed;
    protected String translationKey;
    protected String materialTranslationKey;

    public PartBody(OBJModel model, ResourceLocation texture, Vector3d offset, String translationKey, String materialTranslationKey) {
        super(model, texture, offset);
        this.translationKey = translationKey;
        this.materialTranslationKey = materialTranslationKey;
    }

    @Override
    public List<OBJModelInstance<EntityGenericVehicle>> getInstances(EntityGenericVehicle vehicle) {
        // Get the vehicle's paint color and build dynamic texture path
        String colorName = vehicle.getPaintColorName();

        // Only use dynamic textures for non-white colors (white is the default/original texture)
        ResourceLocation textureToUse;
        if (colorName.equals("white")) {
            // Use original texture for white (default)
            textureToUse = texture;
        } else {
            // Try to use colored texture, but this will fall back to missing texture if not available
            // TODO: Create texture files: vehicle_{translationKey}_{colorName}.png
            // For now, just use the original texture as fallback
            textureToUse = texture;

            // Uncomment when textures are available:
            // textureToUse = new ResourceLocation(
            //     Main.MODID,
            //     "textures/entity/vehicle_" + translationKey + "_" + colorName + ".png"
            // );
        }

        List<OBJModelInstance<EntityGenericVehicle>> list = new ArrayList<>();
        list.add(new OBJModelInstance<>(model, new OBJModelOptions<>(textureToUse, offset, rotation)));
        onPartAdd(list);
        return list;
    }

    public Vector3d[] getWheelOffsets() {
        return wheelOffsets;
    }

    public Vector3d[] getPlayerOffsets() {
        return playerOffsets;
    }

    public Vector3d getNumberPlateOffset() {
        return numberPlateOffset;
    }

    public float getMinRotationSpeed() {
        return minRotationSpeed;
    }

    public float getMaxRotationSpeed() {
        return maxRotationSpeed;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    protected int getWheelAmount() {
        return wheelOffsets.length;
    }

    public float getFuelEfficiency() {
        return fuelEfficiency.get();
    }

    public float getAcceleration() {
        return acceleration.get();
    }

    public float getMaxSpeed() {
        return maxSpeed.get();
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getMaterialTranslationKey() {
        return materialTranslationKey;
    }

    public boolean canFitWheel(PartTireBase wheel) {
        return wheel instanceof PartStandardTire;
    }

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {
        int wheelAmount = getAmount(parts, part -> part instanceof PartTireBase);
        if (wheelAmount < getWheelAmount()) {
            messages.add(Component.translatable("message.parts.too_few_wheels", getWheelAmount()));
        } else if (wheelAmount > getWheelAmount()) {
            messages.add(Component.translatable("message.parts.too_many_wheels", getWheelAmount()));
        }

        int engineAmount = getAmount(parts, part -> part instanceof PartEngine);
        if (engineAmount <= 0) {
            messages.add(Component.translatable("message.parts.no_engine"));
        } else if (engineAmount > 1) {
            messages.add(Component.translatable("message.parts.too_many_engines"));
        }

        if (getAmount(parts, part -> part instanceof PartLicensePlateHolder) > 1) {
            messages.add(Component.translatable("message.parts.too_many_license_plates"));
        }

        if (getAmount(parts, part -> part instanceof PartBumper) > 1) {
            messages.add(Component.translatable("message.parts.too_many_bumpers"));
        }

        if (getAmount(parts, part -> part instanceof PartContainer) > 1) {
            messages.add(Component.translatable("message.parts.too_many_containers"));
        }

        return true;
    }

}
