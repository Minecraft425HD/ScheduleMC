package de.rolandsw.schedulemc.secretdoors;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blocks.AbstractSecretDoorBlock;
import de.rolandsw.schedulemc.secretdoors.blocks.HiddenSwitchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event-Handler für das Secret Doors System.
 * Handhabt den Verknüpfungs-Modus: Wenn ein Schalter im Verknüpfungs-Modus ist
 * und ein Spieler auf eine Tür klickt, wird die Verknüpfung hergestellt.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SecretDoorEventHandler {

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
