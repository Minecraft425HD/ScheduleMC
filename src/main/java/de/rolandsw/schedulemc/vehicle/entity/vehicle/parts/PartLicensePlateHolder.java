package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import com.mojang.math.Axis;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.client.obj.OBJModel;
import de.maxhenkel.corelib.client.obj.OBJModelInstance;
import de.maxhenkel.corelib.client.obj.OBJModelOptions;
import de.maxhenkel.corelib.math.Rotation;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class PartLicensePlateHolder extends PartModel {

    protected Vector3d textOffset;

    public PartLicensePlateHolder(ResourceLocation texture) {
        super(new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/license_plate.obj")), texture, new Vector3d(0D, 0D, 0D), new Rotation(90F, Axis.ZP));
        this.textOffset = new Vector3d(0D, -0.5D / 16D, -0.5D / 16D - 0.001D);

    }

    public Vector3d getTextOffset() {
        return textOffset;
    }

    @Override
    public List<OBJModelInstance<EntityGenericVehicle>> getInstances(EntityGenericVehicle vehicle) {
        PartBody chassis = vehicle.getPartByClass(PartBody.class);

        if (chassis == null) {
            return super.getInstances(vehicle);
        }

        List<OBJModelInstance<EntityGenericVehicle>> list = new ArrayList<>();
        list.add(new OBJModelInstance<>(model, new OBJModelOptions<>(texture, chassis.getNumberPlateOffset(), rotation)));
        onPartAdd(list);
        return list;
    }

}
