package de.rolandsw.schedulemc.vehicle.net;

import java.util.UUID;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageVehicleHorn implements Message<MessageVehicleHorn> {

    private boolean pressed;
    private UUID uuid;

    public MessageVehicleHorn() {

    }

    public MessageVehicleHorn(boolean pressed, Player player) {
        this.pressed = pressed;
        this.uuid = player.getUUID();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (!pressed) {
            return;
        }

        if (!context.getSender().getUUID().equals(uuid)) {
            Main.LOGGER.error("The UUID of the sender was not equal to the packet UUID");
            return;
        }

        Entity riding = context.getSender().getVehicle();

        if (!(riding instanceof EntityGenericVehicle)) {
            return;
        }

        EntityGenericVehicle vehicle = (EntityGenericVehicle) riding;
        if (context.getSender().equals(vehicle.getDriver())) {
            vehicle.getPhysicsComponent().onHornPressed(context.getSender());
        }
    }

    @Override
    public MessageVehicleHorn fromBytes(FriendlyByteBuf buf) {
        this.pressed = buf.readBoolean();
        this.uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(pressed);
        buf.writeUUID(uuid);
    }

}
