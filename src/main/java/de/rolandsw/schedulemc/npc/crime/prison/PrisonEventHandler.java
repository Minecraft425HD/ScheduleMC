package de.rolandsw.schedulemc.npc.crime.prison;

import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-seitige Events für Gefängnis-System
 */
@Mod.EventBusSubscriber
public class PrisonEventHandler {

    private static volatile int tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EventHelper.handleEvent(() -> {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            PrisonManager manager = PrisonManager.getInstance();
            if (manager.isPrisoner(player.getUUID())) {
                manager.onPlayerLogout(player.getUUID(), player.level().getGameTime());
            }
        }, "onPrisonerLogout");
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EventHelper.handleEvent(() -> {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            PrisonManager manager = PrisonManager.getInstance();
            if (manager.isPrisoner(player.getUUID())) {
                manager.onPlayerLogin(player);
            }
        }, "onPrisonerLogin");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < 20) return; // Nur jede Sekunde
        tickCounter = 0;

        EventHelper.handleEvent(() -> {
            net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            ServerLevel level = server.overworld();
            if (level == null) return;

            PrisonManager.getInstance().onServerTick(level.getGameTime(), level);
        }, "onPrisonTick");
    }

    @SubscribeEvent
    public static void onPlayerTeleport(net.minecraftforge.event.entity.EntityTeleportEvent event) {
        EventHelper.handleEvent(() -> {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            PrisonManager manager = PrisonManager.getInstance();
            PrisonManager.PrisonerData data = manager.getPrisonerData(player.getUUID());

            if (data != null) {
                double targetX = event.getTargetX();
                double targetY = event.getTargetY();
                double targetZ = event.getTargetZ();

                var cellSpawn = data.getCellSpawn();
                boolean withinCell = Math.abs(targetX - cellSpawn.getX()) < 10 &&
                                     Math.abs(targetY - cellSpawn.getY()) < 10 &&
                                     Math.abs(targetZ - cellSpawn.getZ()) < 10;

                if (!withinCell) {
                    event.setCanceled(true);
                    player.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("message.prison.no_teleport"));
                }
            }
        }, "onPrisonerTeleport");
    }
}
