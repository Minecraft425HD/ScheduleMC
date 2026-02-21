package de.rolandsw.schedulemc.vehicle.net;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.TireSeasonType;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageOpenTireChange implements Message<MessageOpenTireChange> {

    private UUID vehicleUuid;
    private TireSeasonType currentTireSeasonType;
    private int summerSwapCount;
    private int winterSwapCount;
    private boolean isTruck;

    public MessageOpenTireChange() {}

    public MessageOpenTireChange(UUID vehicleUuid, TireSeasonType currentTireSeasonType,
                                  int summerSwapCount, int winterSwapCount, boolean isTruck) {
        this.vehicleUuid = vehicleUuid;
        this.currentTireSeasonType = currentTireSeasonType;
        this.summerSwapCount = summerSwapCount;
        this.winterSwapCount = winterSwapCount;
        this.isTruck = isTruck;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        openGui();
    }

    @OnlyIn(Dist.CLIENT)
    private void openGui() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Minecraft.getInstance().setScreen(
            new de.rolandsw.schedulemc.vehicle.gui.GuiTireChange(
                vehicleUuid, currentTireSeasonType, summerSwapCount, winterSwapCount, isTruck));
    }

    @Override
    public MessageOpenTireChange fromBytes(FriendlyByteBuf buf) {
        vehicleUuid = buf.readUUID();
        currentTireSeasonType = buf.readEnum(TireSeasonType.class);
        summerSwapCount = buf.readInt();
        winterSwapCount = buf.readInt();
        isTruck = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(vehicleUuid);
        buf.writeEnum(currentTireSeasonType);
        buf.writeInt(summerSwapCount);
        buf.writeInt(winterSwapCount);
        buf.writeBoolean(isTruck);
    }
}
