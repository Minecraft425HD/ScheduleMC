package de.rolandsw.schedulemc.gang.client;

import de.rolandsw.schedulemc.gang.network.PlayerGangInfo;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Modifiziert die TAB-Liste um Gang-Tag, Rang und Level-Fortschritt anzuzeigen.
 *
 * Anzeige:
 *   §c[MAFIA §4★★★§c] §7Boss  §fSpieler1  §6Lv.18 §a████░░ §772%
 *
 * Nutzt Forges PlayerEvent.TabListNameFormat Event.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "schedulemc")
public class GangTabListHandler {

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        EventHelper.handleEvent(() -> {
            Player player = event.getEntity();
            if (player == null) return;

            PlayerGangInfo info = ClientGangCache.getPlayerInfo(player.getUUID());
            if (info == null) return;

            // Formatierter Name zusammenbauen
            StringBuilder sb = new StringBuilder();

            // Gang-Tag mit Sternen
            if (info.isInGang()) {
                sb.append(info.getFormattedGangTag());
                sb.append(" ");
                sb.append(info.getRankColorCode()).append(info.getRankName());
                sb.append("  ");
            }

            // Spielername
            sb.append("\u00A7f").append(player.getGameProfile().getName());

            // Level + Progress
            sb.append("  ").append(info.getFormattedLevel());

            MutableComponent formatted = Component.literal(sb.toString());
            event.setDisplayName(formatted);

        }, "onTabListNameFormat");
    }
}
