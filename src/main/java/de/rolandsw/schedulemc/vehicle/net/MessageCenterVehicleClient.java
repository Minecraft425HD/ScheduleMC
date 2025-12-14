package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageCenterVehicleClient implements Message<MessageCenterVehicleClient> {

    private UUID uuid;

    public MessageCenterVehicleClient() {

    }

    public MessageCenterVehicleClient(Player player) {
        this.uuid = player.getUUID();
    }

    public MessageCenterVehicleClient(UUID uuid) {
        this.uuid = uuid;
    }

    @OnlyIn(Dist.CLIENT)
    public void centerClient() {
        Player player = Minecraft.getInstance().player;
        Player ridingPlayer = player.level().getPlayerByUUID(uuid);
        Entity riding = ridingPlayer.getVehicle();

        if (!(riding instanceof EntityGenericVehicle)) {
            return;
        }

        EntityGenericVehicle vehicle = (EntityGenericVehicle) riding;
        if (ridingPlayer.equals(vehicle.getDriver())) {
            vehicle.centerVehicle();
        }
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        centerClient();
    }

    @Override
    public MessageCenterVehicleClient fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

}
