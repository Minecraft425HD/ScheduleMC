package de.rolandsw.schedulemc.mapview.forge;

import de.rolandsw.schedulemc.mapview.PacketBridge;
import de.rolandsw.schedulemc.mapview.packets.WorldIdC2S;

public class ForgePacketBridge implements PacketBridge {
    @Override
    public void sendWorldIDPacket() {
        ForgeEvents.CHANNEL.sendToServer(new WorldIdC2S());
    }
}
