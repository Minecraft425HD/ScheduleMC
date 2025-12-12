package de.rolandsw.schedulemc.car.net;

import java.util.UUID;

import de.rolandsw.schedulemc.car.Main;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageRepairCar implements Message<MessageRepairCar> {

    private BlockPos pos;
    private UUID uuid;

    public MessageRepairCar() {

    }

    public MessageRepairCar(BlockPos pos, Player player) {
        this.pos = pos;
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

        // NOTE: TileEntityCarWorkshop was removed in a previous cleanup
        // This message handler is currently non-functional and may need to be reimplemented
        // if car workshop functionality is restored in the future
        BlockEntity te = context.getSender().level().getBlockEntity(pos);
        // TODO: Implement car repair functionality when workshop block is added
    }

    @Override
    public MessageRepairCar fromBytes(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUUID(uuid);
    }

}
