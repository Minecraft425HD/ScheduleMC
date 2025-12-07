package de.rolandsw.schedulemc.car.net;

import de.maxhenkel.corelib.net.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageSpawnCar implements Message<MessageSpawnCar> {

    private BlockPos pos;

    public MessageSpawnCar() {
    }

    public MessageSpawnCar(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        // NOTE: TileEntityCarWorkshop was removed in a previous cleanup
        // This message handler is currently non-functional and may need to be reimplemented
        // if car workshop functionality is restored in the future
        BlockEntity te = context.getSender().level().getBlockEntity(pos);
        // TODO: Implement car spawning functionality when workshop block is added
    }

    @Override
    public MessageSpawnCar fromBytes(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

}
