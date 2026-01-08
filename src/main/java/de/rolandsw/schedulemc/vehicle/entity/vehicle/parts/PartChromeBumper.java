package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.client.obj.OBJModel;
import de.maxhenkel.corelib.client.obj.OBJModelInstance;
import de.maxhenkel.corelib.client.obj.OBJModelOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PartChromeBumper extends PartModel {

    public PartChromeBumper(ResourceLocation texture) {
        super(new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/wood_bumper.obj")), texture);
    }

    @Override
    public List<OBJModelInstance<EntityGenericVehicle>> getInstances(EntityGenericVehicle vehicle) {
        PartChassisBase chassis = vehicle.getPartByClass(PartChassisBase.class);

        if (chassis == null) {
            return super.getInstances(vehicle);
        }

        List<OBJModelInstance<EntityGenericVehicle>> list = new ArrayList<>();
        list.add(new OBJModelInstance<>(model, new OBJModelOptions<>(texture, chassis.getBumperOffset(), rotation)));
        onPartAdd(list);
        return list;
    }

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {

        if (Part.getAmount(parts, part -> part instanceof PartChassisBase) != 1) {
            messages.add(Component.translatable("message.parts.no_body_for_bumper"));
            return false;
        }

        return true;
    }

}
