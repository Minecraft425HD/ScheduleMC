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

public class MessageVehicleGui implements Message<MessageVehicleGui> {

    private UUID uuid;

    public MessageVehicleGui() {

    }

    public MessageVehicleGui(Player player) {
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

        Entity e = context.getSender().getVehicle();
        if (e instanceof EntityGenericVehicle) {
            ((EntityGenericVehicle) e).openVehicleGUI(context.getSender());
        }
    }

    @Override
    public MessageVehicleGui fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

}
