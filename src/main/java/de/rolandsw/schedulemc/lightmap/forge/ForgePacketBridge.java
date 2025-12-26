package de.rolandsw.schedulemc.lightmap.forge;

import de.rolandsw.schedulemc.lightmap.PacketBridge;
import de.rolandsw.schedulemc.lightmap.packets.WorldIdC2S;

public class ForgePacketBridge implements PacketBridge {
    @Override
    public void sendWorldIDPacket() {
        ForgeEvents.CHANNEL.sendToServer(new WorldIdC2S());
    }
}
