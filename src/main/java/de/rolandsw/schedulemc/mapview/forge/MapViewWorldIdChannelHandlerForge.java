package de.rolandsw.schedulemc.lightmap.forge;

import de.rolandsw.schedulemc.lightmap.packets.WorldIdS2C;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LightMapWorldIdChannelHandlerForge {

    public static void handleDataOnMain(final WorldIdS2C data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> WorldIdS2C.updateWorld(data));
        ctx.get().setPacketHandled(true);
    }
}
