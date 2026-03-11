package de.rolandsw.schedulemc.secretdoors;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.ElevatorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blocks.AbstractSecretDoorBlock;
import de.rolandsw.schedulemc.secretdoors.blocks.ElevatorBlock;
import de.rolandsw.schedulemc.secretdoors.blocks.HiddenSwitchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event-Handler für das Secret Doors System.
 * Handhabt den Verknüpfungs-Modus: Wenn ein Schalter im Verknüpfungs-Modus ist
 * und ein Spieler auf eine Tür klickt, wird die Verknüpfung hergestellt.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SecretDoorEventHandler {

    // ── Aufzug: Springen = hoch, Sneaken = runter ─────────────────────

    /** true = Spieler war letzten Tick auf dem Boden */
    private static final Map<UUID, Boolean> lastOnGround = new ConcurrentHashMap<>();
    /** GameTime des letzten Sneak-Teleports pro Spieler (Cooldown 30 Ticks = 1,5 s) */
    private static final Map<UUID, Long>    sneakCooldown = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        UUID uid = player.getUUID();
        boolean onGround   = player.isOnGround();
        boolean wasOnGround = lastOnGround.getOrDefault(uid, true);
        lastOnGround.put(uid, onGround);

        ElevatorBlockEntity be = getElevatorUnderPlayer(player);
        if (be == null) return;

        // ── Sprung: war am Boden, jetzt in der Luft mit positiver Y-Geschwindigkeit
        if (wasOnGround && !onGround && player.getDeltaMovement().y > 0.1) {
            if (be.canUse(player)) {
                be.teleportPlayerUp(player);
            }
            return; // kein Sneak-Check im selben Tick
        }

        // ── Sneaken: am Boden + Shift gedrückt, mit Cooldown
        if (player.isShiftKeyDown() && onGround && be.canUse(player)) {
            long now      = player.level().getGameTime();
            long lastTime = sneakCooldown.getOrDefault(uid, -40L);
            if (now - lastTime >= 30) {
                sneakCooldown.put(uid, now);
                be.teleportPlayerDown(player);
            }
        }
    }

    /**
     * Gibt das {@link ElevatorBlockEntity} zurück, auf dem der Spieler gerade steht
     * (Controller oder Filler), oder {@code null} wenn keins vorhanden.
     */
    private static ElevatorBlockEntity getElevatorUnderPlayer(Player player) {
        Level level = player.level();
        // Spieler-Füße befinden sich bei blockPosition(); der Block darunter ist bei below()
        BlockPos under = player.blockPosition().below();

        BlockState bs = level.getBlockState(under);
        if (bs.getBlock() instanceof ElevatorBlock) {
            if (level.getBlockEntity(under) instanceof ElevatorBlockEntity be) return be;
        }
        if (bs.is(SecretDoors.DOOR_FILLER.get())) {
            if (level.getBlockEntity(under) instanceof DoorFillerBlockEntity fbe) {
                BlockPos ctrl = fbe.getControllerPos();
                if (ctrl != null && level.getBlockEntity(ctrl) instanceof ElevatorBlockEntity be) {
                    return be;
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Prüfen ob auf eine Geheimtür geklickt wurde
        if (!(clickedState.getBlock() instanceof AbstractSecretDoorBlock)) return;

        // Suche in 32-Block-Radius nach Schaltern im Verknüpfungs-Modus
        // (nur Schalter die dem Spieler gehören oder für den der OP ist)
        BlockPos playerPos = player.blockPosition();
        int searchRadius = 32;

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    BlockPos checkPos = playerPos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof HiddenSwitchBlock) {
                        if (level.getBlockEntity(checkPos) instanceof HiddenSwitchBlockEntity be) {
                            if (be.isLinkingMode() && be.canEdit(player)) {
                                // Verknüpfe diese Tür mit dem Schalter
                                HiddenSwitchBlock switchBlock = (HiddenSwitchBlock) checkState.getBlock();
                                switchBlock.tryLinkDoor(level, checkPos, clickedPos, player);

                                // Verknüpfungs-Modus beenden
                                be.setLinkingMode(false);
                                be.setChanged();

                                // Event konsumieren damit die Tür nicht gleichzeitig geöffnet wird
                                event.setCanceled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Hilfsmethode: Setzt die Größe einer Geheimtür.
     * Wird über Befehl oder GUI aufgerufen.
     */
    public static void resizeDoor(Level level, BlockPos pos, int width, int height, ServerPlayer player) {
        if (!level.getBlockState(pos).getBlock().getClass().getSuperclass()
            .isAssignableFrom(AbstractSecretDoorBlock.class)
            && !(level.getBlockState(pos).getBlock() instanceof AbstractSecretDoorBlock)) {
            player.sendSystemMessage(Component.literal("§cKein Geheimtür-Block an dieser Position!"));
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
            player.sendSystemMessage(Component.literal("§cKein BlockEntity gefunden!"));
            return;
        }

        if (!be.canUse(player)) {
            player.sendSystemMessage(Component.literal("§cKeine Berechtigung!"));
            return;
        }

        int w = Math.max(1, Math.min(10, width));
        int h = Math.max(1, Math.min(10, height));

        AbstractSecretDoorBlock doorBlock = (AbstractSecretDoorBlock) level.getBlockState(pos).getBlock();
        be.setSize(w, h, level);
        doorBlock.spawnFillers(level, pos, be, w, h, level.getBlockState(pos)
            .getValue(AbstractSecretDoorBlock.FACING));

        player.sendSystemMessage(Component.literal(
            "§a[Geheimtür] Größe auf §e" + w + "×" + h + "§a gesetzt."));
    }
}
