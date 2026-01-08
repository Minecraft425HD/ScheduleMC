package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.screen.SmartphoneScreen;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Schützt Spieler vor Schaden, wenn das Smartphone-GUI geöffnet ist.
 * Angreifer erhalten einen Wanted-Stern.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class SmartphoneProtectionHandler {

    /**
     * Verhindert Schaden an Spielern mit geöffnetem Smartphone-GUI
     * und bestraft den Angreifer mit einem Wanted-Stern
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        EventHelper.handleEvent(() -> {
            // Nur für Spieler relevant
            if (!(event.getEntity() instanceof Player victim)) {
                return;
            }

            // Prüfe ob der angegriffene Spieler ein Server-Spieler ist
            if (!(victim instanceof ServerPlayer serverVictim)) {
                return;
            }

            // Prüfe ob Angreifer ein Spieler ist
            if (event.getSource().getEntity() instanceof ServerPlayer attacker) {

                // Prüfe ob Opfer das Smartphone-GUI offen hat
                // Da wir server-side sind, müssen wir eine andere Methode verwenden
                // Wir erstellen ein System, das trackt, wer das GUI offen hat
                if (SmartphoneTracker.hasSmartphoneOpen(serverVictim.getUUID())) {

                    // Verhindere den Schaden
                    event.setCanceled(true);

                    // Gebe dem Angreifer einen Wanted-Stern
                    long currentDay = serverVictim.getServer().overworld().getDayTime() / 24000L;
                    CrimeManager.addWantedLevel(attacker.getUUID(), 1, currentDay);

                    // Benachrichtige beide Spieler
                    attacker.sendSystemMessage(
                        Component.literal("§c§l⚠ Du hast einen geschützten Spieler angegriffen! +1 Wanted-Stern")
                    );

                    serverVictim.sendSystemMessage(
                        Component.translatable("message.smartphone.protected")
                    );

                    ScheduleMC.LOGGER.info("Player {} attacked protected player {} (Smartphone active). +1 Wanted-Level",
                        attacker.getName().getString(), serverVictim.getName().getString());
                }
            }
        }, "onLivingAttack");
    }
}
