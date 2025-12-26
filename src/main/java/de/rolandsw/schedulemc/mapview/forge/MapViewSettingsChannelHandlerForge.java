package de.rolandsw.schedulemc.mapview.forge;

import de.rolandsw.schedulemc.mapview.packets.MapViewSettingsS2C;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MapViewSettingsChannelHandlerForge {

    public static void handleDataOnMain(final MapViewSettingsS2C data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> MapViewSettingsS2C.parsePacket(data));
        ctx.get().setPacketHandled(true);
    }
}
