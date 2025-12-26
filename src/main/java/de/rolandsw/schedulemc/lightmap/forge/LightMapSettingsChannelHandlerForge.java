package de.rolandsw.schedulemc.lightmap.forge;

import de.rolandsw.schedulemc.lightmap.packets.LightMapSettingsS2C;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LightMapSettingsChannelHandlerForge {

    public static void handleDataOnMain(final LightMapSettingsS2C data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> LightMapSettingsS2C.parsePacket(data));
        ctx.get().setPacketHandled(true);
    }
}
