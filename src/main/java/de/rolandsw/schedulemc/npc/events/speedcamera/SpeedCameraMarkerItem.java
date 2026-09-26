package de.rolandsw.schedulemc.npc.events.speedcamera;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Admin-Werkzeug: markiert/entmarkiert einen möglichen Blitzer-Standort bei
 * {@link SpeedCameraManager}, ohne selbst einen Block zu platzieren. Rechtsklick auf
 * einen Block markiert die Position davor (analog zu normaler Block-Platzierung);
 * Rechtsklick auf eine bereits markierte Position entfernt die Markierung wieder
 * (und räumt einen dort ggf. gerade aktiven {@link SpeedCameraBlock} sofort ab).
 */
public class SpeedCameraMarkerItem extends Item {

    public SpeedCameraMarkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        SpeedCameraManager manager = SpeedCameraManager.getInstance();
        if (manager == null) return InteractionResult.PASS;

        if (manager.isMarked(pos)) {
            manager.unregisterMarker(serverLevel, pos);
            player.sendSystemMessage(Component.translatable("message.speed_camera.unmarked", pos.toShortString()));
        } else {
            manager.registerMarker(pos);
            player.sendSystemMessage(Component.translatable("message.speed_camera.marked",
                pos.toShortString(), manager.getMarkedCount()));
        }

        return InteractionResult.CONSUME;
    }
}
