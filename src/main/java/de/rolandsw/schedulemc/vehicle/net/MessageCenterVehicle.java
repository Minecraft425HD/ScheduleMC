package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageCenterVehicle implements Message<MessageCenterVehicle> {

    private UUID uuid;

    public MessageCenterVehicle() {

    }

    public MessageCenterVehicle(Player player) {
        this.uuid = player.getUUID();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
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
            vehicle.centerVehicle();
        }

        MessageCenterVehicleClient msg = new MessageCenterVehicleClient(uuid);
        context.getSender().serverLevel().getPlayers(player -> player.distanceTo(vehicle) <= 128F).forEach(player -> Main.SIMPLE_CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT));
    }

    @Override
    public MessageCenterVehicle fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

}
