package de.rolandsw.schedulemc.lock.network;

import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.lock.LockType;
import de.rolandsw.schedulemc.lock.items.KeyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
 * Tuer schliesst sich automatisch nach 3 Sekunden.
 */
public class CodeEntryPacket {

    /** Ticks bis die Tuer sich automatisch schliesst (3 Sekunden = 60 Ticks). */
    private static final int AUTO_CLOSE_TICKS = 60;

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
        this.lockId = buf.readUtf(256);
        this.doorPos = buf.readBlockPos();
        this.dimension = buf.readUtf(128);
        this.enteredCode = buf.readUtf(10);
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

                // DUAL-Lock: Schluessel-Nutzung abziehen (Schluessel wurde zuvor in KeyItem validiert)
                if (lockData.getType() == LockType.DUAL) {
                    ItemStack held = player.getMainHandItem();
                    if (held.getItem() instanceof KeyItem) {
                        boolean depleted = KeyItem.consumeUse(held);
                        if (depleted) {
                            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                            player.sendSystemMessage(Component.literal("\u00A77(Schluessel verbraucht)"));
                        }
                    }
                }

                // Tuer oeffnen
                Level level = player.level();
                BlockState state = level.getBlockState(doorPos);
                if (state.getBlock() instanceof DoorBlock door) {
                    boolean currentlyOpen = state.getValue(BlockStateProperties.OPEN);
                    if (!currentlyOpen) {
                        level.setBlock(doorPos, state.setValue(BlockStateProperties.OPEN, true), 10);
                        // Sound abspielen
                        level.levelEvent(null, 1005, doorPos, 0);

                        // Automatisch schliessen nach 3 Sekunden
                        if (level instanceof ServerLevel serverLevel) {
                            scheduleAutoClose(serverLevel, doorPos);
                        }
                    }
                }
            } else {
                // Code falsch
                player.sendSystemMessage(Component.literal("\u00A7c\u2716 Falscher Code!"));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Plant das automatische Schliessen der Tuer nach AUTO_CLOSE_TICKS.
     */
    private void scheduleAutoClose(ServerLevel level, BlockPos pos) {
        // Einfacher Delayed-Task via Server
        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + AUTO_CLOSE_TICKS,
                () -> {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof DoorBlock && state.getValue(BlockStateProperties.OPEN)) {
                        level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, false), 10);
                        // Schliess-Sound
                        level.levelEvent(null, 1006, pos, 0);
                    }
                }
        ));
    }
}
