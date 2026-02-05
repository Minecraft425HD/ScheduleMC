package de.rolandsw.schedulemc.lock.network;

import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> Server: Spieler hat Code eingegeben.
 * Server validiert und oeffnet ggf. die Tuer.
 */
public class CodeEntryPacket {

    private final String lockId;
    private final BlockPos doorPos;
    private final String dimension;
    private final String enteredCode;

    public CodeEntryPacket(String lockId, BlockPos doorPos, String dimension, String enteredCode) {
        this.lockId = lockId;
        this.doorPos = doorPos;
        this.dimension = dimension;
        this.enteredCode = enteredCode;
    }

    public CodeEntryPacket(FriendlyByteBuf buf) {
        this.lockId = buf.readUtf();
        this.doorPos = buf.readBlockPos();
        this.dimension = buf.readUtf();
        this.enteredCode = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(lockId);
        buf.writeBlockPos(doorPos);
        buf.writeUtf(dimension);
        buf.writeUtf(enteredCode);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            LockManager mgr = LockManager.getInstance();
            if (mgr == null) return;

            // Verifiziere dass die Tuer in der richtigen Dimension ist
            String playerDim = player.level().dimension().location().toString();
            if (!playerDim.equals(dimension)) {
                player.sendSystemMessage(Component.literal("\u00A7cFehler: Falsche Dimension!"));
                return;
            }

            // Pruefe Entfernung zum Schloss (max 5 Bloecke)
            if (player.distanceToSqr(doorPos.getX() + 0.5, doorPos.getY() + 0.5, doorPos.getZ() + 0.5) > 25) {
                player.sendSystemMessage(Component.literal("\u00A7cDu bist zu weit von der Tuer entfernt!"));
                return;
            }

            String posKey = LockManager.posKey(dimension, doorPos.getX(), doorPos.getY(), doorPos.getZ());
            LockData lockData = mgr.getLock(posKey);

            if (lockData == null) {
                player.sendSystemMessage(Component.literal("\u00A7cDiese Tuer ist nicht mehr gesperrt."));
                return;
            }

            // Verifiziere Lock-ID
            if (!lockData.getLockId().equals(lockId)) {
                player.sendSystemMessage(Component.literal("\u00A7cUngueltige Lock-ID!"));
                return;
            }

            // Pruefe Code
            if (lockData.getCode() != null && lockData.getCode().equals(enteredCode)) {
                // Code korrekt! Tuer oeffnen
                player.sendSystemMessage(Component.literal("\u00A7a\u2714 Code korrekt!"));

                // Tuer oeffnen
                Level level = player.level();
                BlockState state = level.getBlockState(doorPos);
                if (state.getBlock() instanceof DoorBlock) {
                    boolean currentlyOpen = state.getValue(BlockStateProperties.OPEN);
                    if (!currentlyOpen) {
                        level.setBlock(doorPos, state.setValue(BlockStateProperties.OPEN, true), 10);
                        // Sound abspielen
                        level.levelEvent(null, 1005, doorPos, 0);
                    }
                }
            } else {
                // Code falsch
                player.sendSystemMessage(Component.literal("\u00A7c\u2716 Falscher Code!"));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
