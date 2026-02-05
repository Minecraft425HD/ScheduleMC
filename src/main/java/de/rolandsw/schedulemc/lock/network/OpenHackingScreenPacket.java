package de.rolandsw.schedulemc.lock.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> Client: Oeffnet den Hacking-Screen fuer ein Zahlenschloss.
 */
public class OpenHackingScreenPacket {

    private final String lockId;
    private final String posKey;
    private final String lockTypeName;
    private final int codeLength;

    public OpenHackingScreenPacket(String lockId, String posKey, String lockTypeName, int codeLength) {
        this.lockId = lockId;
        this.posKey = posKey;
        this.lockTypeName = lockTypeName;
        this.codeLength = codeLength;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(lockId);
        buf.writeUtf(posKey);
        buf.writeUtf(lockTypeName);
        buf.writeInt(codeLength);
    }

    public static OpenHackingScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenHackingScreenPacket(
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient());
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new de.rolandsw.schedulemc.client.screen.HackingScreen(
                    lockId, posKey, lockTypeName, codeLength));
        }
    }
}
