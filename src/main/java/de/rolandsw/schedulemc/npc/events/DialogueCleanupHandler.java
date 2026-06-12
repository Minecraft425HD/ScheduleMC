package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Räumt aktive Dialoge auf, wenn ein Spieler den Server verlässt —
 * verhindert verwaiste Einträge in DialogueManager.activeDialogues.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public final class DialogueCleanupHandler {

    private DialogueCleanupHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DialogueManager manager = DialogueManager.getInstance();
            if (manager != null) {
                manager.endDialogue(player);
            }
        }
    }
}
