package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import org.joml.Vector3d;
import de.rolandsw.schedulemc.vehicle.Main;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PartTankContainer extends PartTransporterBack {

    private int fluidAmount;

    public PartTankContainer(ResourceLocation texture) {
        super(new OBJModel(new ResourceLocation(Main.MODID, "models/entity/tank_container.obj")), texture, new Vector3d(0D / 16D, 17D / 16D, 5.5D / 16D));
        this.fluidAmount = 64000;
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {
        if (Part.getAmount(parts, part -> part instanceof PartLkwChassis) != 1) {
            messages.add(Component.translatable("message.parts.no_body_for_tank_container"));
            return false;
        }
        return true;
    }

}
