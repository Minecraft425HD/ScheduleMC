package de.rolandsw.schedulemc.vehicle.net;

import java.util.UUID;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageCrash implements Message<MessageCrash> {

    private float speed;
    private UUID uuid;

    public MessageCrash() {

    }

    public MessageCrash(float speed, EntityGenericVehicle vehicle) {
        this.speed = speed;
        this.uuid = vehicle.getUUID();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Entity riding = context.getSender().getVehicle();

        if (!(riding instanceof EntityGenericVehicle)) {
            return;
        }

        EntityGenericVehicle vehicle = (EntityGenericVehicle) riding;

        if (!vehicle.getUUID().equals(uuid)) {
            return;
        }

        vehicle.onCollision(speed);
    }

    @Override
    public MessageCrash fromBytes(FriendlyByteBuf buf) {
        this.speed = buf.readFloat();
        this.uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(speed);
        buf.writeUUID(uuid);
    }

}
