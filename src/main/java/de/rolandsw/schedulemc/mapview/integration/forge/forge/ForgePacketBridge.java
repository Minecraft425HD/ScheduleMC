package de.rolandsw.schedulemc.mapview.integration.forge;

import de.rolandsw.schedulemc.mapview.integration.PacketBridge;
import de.rolandsw.schedulemc.mapview.integration.network.WorldIdC2S;

public class ForgePacketBridge implements PacketBridge {
    @Override
    public void sendWorldIDPacket() {
        ForgeEvents.CHANNEL.sendToServer(new WorldIdC2S());
    }
}
