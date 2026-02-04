package de.rolandsw.schedulemc.lock.network;

import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.lock.LockType;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> Server: Ergebnis des Hacking-Minigames.
 *
 * enteredCode = der vom Spieler eingegebene/geknackte Code
 * posKey      = Position des Schlosses
 */
public class HackingResultPacket {

    private final String posKey;
    private final String enteredCode;
    private final boolean success;

    public HackingResultPacket(String posKey, String enteredCode, boolean success) {
        this.posKey = posKey;
        this.enteredCode = enteredCode;
        this.success = success;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(posKey);
        buf.writeUtf(enteredCode);
        buf.writeBoolean(success);
    }

    public static HackingResultPacket decode(FriendlyByteBuf buf) {
        return new HackingResultPacket(buf.readUtf(), buf.readUtf(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            LockManager mgr = LockManager.getInstance();
            if (mgr == null) return;

            LockData lockData = mgr.getLock(posKey);
            if (lockData == null) return;

            if (success && lockData.getCode() != null && lockData.getCode().equals(enteredCode)) {
                // Code korrekt geknackt!
                player.sendSystemMessage(Component.literal(
                        "\u00A7a\u2714 Code geknackt! Tuer entriegelt."));

                // Tuer oeffnen
                openDoor(player, lockData);
            } else {
                // Fehlgeschlagen
                player.sendSystemMessage(Component.literal(
                        "\u00A7c\u2716 Hacking fehlgeschlagen!"));

                // Alarm bei Dual-Lock
                if (lockData.getType().triggersAlarm()) {
                    player.sendSystemMessage(Component.literal(
                            "\u00A74\u26A0 ALARM AUSGELOEST! Fahndungslevel erhoecht!"));
                    triggerAlarm(player);
                }
            }
        });
    }

    private void openDoor(ServerPlayer player, LockData lockData) {
        try {
            // Parse posKey: "dim:x:y:z"
            String[] parts = posKey.split(":");
            // Dimension kann Doppelpunkte enthalten (z.B. minecraft:overworld)
            // Format: dim:x:y:z -> letzte 3 Parts sind Koordinaten
            int z = Integer.parseInt(parts[parts.length - 1]);
            int y = Integer.parseInt(parts[parts.length - 2]);
            int x = Integer.parseInt(parts[parts.length - 3]);
            BlockPos pos = new BlockPos(x, y, z);

            var state = player.level().getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock door) {
                door.setOpen(null, player.level(), state, pos, !state.getValue(DoorBlock.OPEN));
            }
        } catch (Exception e) {
            // Fallback: ignorieren
        }
    }

    private void triggerAlarm(ServerPlayer player) {
        try {
            long currentDay = player.level().getDayTime() / 24000;
            de.rolandsw.schedulemc.npc.crime.CrimeManager.addWantedLevel(player.getUUID(), 2, currentDay);

            if (player.level() instanceof ServerLevel serverLevel) {
                var witnessManager = de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getManager(serverLevel);
                witnessManager.registerCrime(
                        player,
                        de.rolandsw.schedulemc.npc.life.witness.CrimeType.PETTY_THEFT,
                        player.blockPosition(),
                        serverLevel,
                        null
                );
            }
        } catch (Exception ignored) {
        }
    }
}
