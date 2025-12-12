package de.rolandsw.schedulemc.vehicle.net;

import de.maxhenkel.corelib.net.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageSpawnVehicle implements Message<MessageSpawnVehicle> {

    private BlockPos pos;

    public MessageSpawnVehicle() {
    }

    public MessageSpawnVehicle(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        // NOTE: TileEntityVehicleWorkshop was removed in a previous cleanup
        // This message handler is currently non-functional and may need to be reimplemented
        // if vehicle workshop functionality is restored in the future
        BlockEntity te = context.getSender().level().getBlockEntity(pos);
        // TODO: Implement vehicle spawning functionality when workshop block is added
    }

    @Override
    public MessageSpawnVehicle fromBytes(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

}
