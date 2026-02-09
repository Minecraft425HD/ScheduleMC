package de.rolandsw.schedulemc.lock.network;

import de.rolandsw.schedulemc.client.screen.CombinationLockScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> Client: Oeffnet die Code-Eingabe GUI.
 */
public class OpenCodeEntryPacket {

    private final String lockId;
    private final BlockPos doorPos;
    private final String dimension;

    public OpenCodeEntryPacket(String lockId, BlockPos doorPos, String dimension) {
        this.lockId = lockId;
        this.doorPos = doorPos;
        this.dimension = dimension;
    }

    public OpenCodeEntryPacket(FriendlyByteBuf buf) {
        this.lockId = buf.readUtf(256);
        this.doorPos = buf.readBlockPos();
        this.dimension = buf.readUtf(128);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(lockId);
        buf.writeBlockPos(doorPos);
        buf.writeUtf(dimension);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new CombinationLockScreen(
                        lockId, false,
                        enteredCode -> {
                            // Code an Server senden
                            LockNetworkHandler.sendToServer(new CodeEntryPacket(
                                    lockId, doorPos, dimension, enteredCode));
                        }
                ));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
