package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Globaler Handler für tägliche NPC-Gehaltszahlungen
 *
 * WICHTIG: Dieser Handler funktioniert GLOBAL und prüft ALLE NPCs,
 * unabhängig davon ob Spieler in der Nähe sind oder ob Chunks geladen sind.
 *
 * Dies stellt sicher, dass NPCs ihr Gehalt IMMER bekommen, auch wenn:
 * - Spieler schlafen (Zeit wird übersprungen)
 * - NPCs in nicht geladenen Chunks sind
 * - Keine Spieler online sind
 */
@Mod.EventBusSubscriber
public class NPCDailySalaryHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static long lastSalaryDay = -1;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, () -> {
            // Überspringe wenn keine Spieler online sind (Zeit läuft nicht)
            if (event.getServer().getPlayerCount() == 0) {
                return;
            }

            ServerLevel level = event.getServer().overworld();
            long currentDay = level.getDayTime() / 24000L;

            // Prüfe ob ein neuer Tag begonnen hat
            // Dies funktioniert auch wenn Zeit übersprungen wird (z.B. durch Schlafen)
            if (currentDay > lastSalaryDay) {
                lastSalaryDay = currentDay;
                payAllNPCSalaries(level, currentDay);
            }
        });
    }

    /**
     * Zahlt allen NPCs ihr tägliches Gehalt aus
     */
    private static void payAllNPCSalaries(ServerLevel level, long currentDay) {
        int paidCount = 0;
        int totalAmount = 0;

        // Durchlaufe ALLE Entities in ALLEN geladenen Welten
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof CustomNPCEntity npc) {
                    // Nur BEWOHNER und VERKAEUFER (nicht Polizei)
                    if (!npc.getNpcData().hasInventoryAndWallet()) {
                        continue;
                    }

                    // Prüfen ob heute schon Gehalt ausgezahlt wurde
                    if (npc.getNpcData().getLastDailyIncome() < currentDay) {
                        // Zufälliger Betrag zwischen 20 und 150
                        int income = 20 + npc.getRandom().nextInt(131); // 20 + [0-130] = 20-150
                        int oldWallet = npc.getNpcData().getWallet();

                        npc.getNpcData().addMoney(income);
                        npc.getNpcData().setLastDailyIncome(currentDay);

                        paidCount++;
                        totalAmount += income;

                        // Nachricht an Spieler in der Nähe (optional)
                        serverLevel.getPlayers(player -> player.distanceToSqr(npc) < 2500) // 50 Blöcke
                            .forEach(player -> player.sendSystemMessage(
                                Component.literal("[NPC] " + npc.getNpcName() + " erhielt tägliches Einkommen: ")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal(income + " Bargeld")
                                        .withStyle(ChatFormatting.GOLD))
                                    .append(Component.literal(" (Tag " + currentDay + ", Geldbörse: " +
                                        oldWallet + " → " + npc.getNpcData().getWallet() + ")")
                                        .withStyle(ChatFormatting.GRAY))
                            ));
                    }
                }
            }
        }

        if (paidCount > 0) {
            LOGGER.info("Tägliche Gehälter ausgezahlt: {} NPCs erhielten insgesamt {}€ (Tag {})",
                paidCount, totalAmount, currentDay);
        }
    }
}
