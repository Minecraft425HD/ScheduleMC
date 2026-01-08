package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityFuelStation;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageFuelStationAdminAmount implements Message<MessageFuelStationAdminAmount> {

    private BlockPos pos;
    private int amount;

    public MessageFuelStationAdminAmount() {

    }

    public MessageFuelStationAdminAmount(BlockPos pos, int amount) {
        this.pos = pos;
        this.amount = amount;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        BlockEntity te = context.getSender().level().getBlockEntity(pos);

        if (te instanceof TileEntityFuelStation) {
            ((TileEntityFuelStation) te).setTradeAmount(amount);
        }
    }

    @Override
    public MessageFuelStationAdminAmount fromBytes(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.amount = buf.readInt();

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(amount);
    }

}
