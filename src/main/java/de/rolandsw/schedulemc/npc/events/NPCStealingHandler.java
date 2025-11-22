package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.StealingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;

/**
 * Event Handler für das Bestehlen von NPCs
 * - Sneak + Links-Klick auf NPC öffnet Diebstahl-Minigame
 * - Nur für Bewohner und Verkäufer (nicht Polizei)
 */
public class NPCStealingHandler {

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Nur Server-Side
        if (event.getLevel().isClientSide) return;

        Player player = event.getEntity();

        // Prüfe ob Spieler schleicht
        if (!player.isCrouching()) return;

        // Prüfe ob Links-Klick (MAIN_HAND)
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        // Prüfe ob Target ein CustomNPC ist
        if (event.getTarget() instanceof CustomNPCEntity npc) {
            // Prüfe ob es kein Polizist ist
            if (npc.getNpcType() == NPCType.POLIZEI) {
                player.displayClientMessage(Component.literal("§c✗ Du kannst keine Polizisten bestehlen!"), true);
                event.setCanceled(true);
                return;
            }

            // Öffne Diebstahl-GUI
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, playerInventory, p) -> new StealingMenu(id, playerInventory, npc),
                    Component.literal("§cBestehle " + npc.getNpcName())
                ), buf -> {
                    buf.writeInt(npc.getId());
                });
            }

            event.setCanceled(true);
        }
    }
}
