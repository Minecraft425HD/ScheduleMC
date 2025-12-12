package de.rolandsw.schedulemc.car.net;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageStarting implements Message<MessageStarting> {

    private boolean start;
    private boolean playSound;
    private UUID uuid;

    public MessageStarting() {

    }

    public MessageStarting(boolean start, boolean playSound, Player player) {
        this.start = start;
        this.playSound = playSound;
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

        if (!(riding instanceof EntityGenericCar)) {
            return;
        }

        EntityGenericCar car = (EntityGenericCar) riding;
        if (context.getSender().equals(car.getDriver())) {
            car.getBatteryComponent().setStarting(start, playSound);
        }
    }

    @Override
    public MessageStarting fromBytes(FriendlyByteBuf buf) {
        this.start = buf.readBoolean();
        this.playSound = buf.readBoolean();

        this.uuid = buf.readUUID();

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(start);
        buf.writeBoolean(playSound);

        buf.writeUUID(uuid);
    }

}
