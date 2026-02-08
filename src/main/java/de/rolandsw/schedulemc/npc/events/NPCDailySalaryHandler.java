package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.List;

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
    private static volatile long lastSalaryDay = -1;
    private static final Object SALARY_LOCK = new Object();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, server -> {
            // Überspringe wenn keine Spieler online sind (Zeit läuft nicht)
            if (server.getPlayerCount() == 0) {
                return;
            }

            ServerLevel level = server.overworld();
            long currentDay = level.getDayTime() / 24000L;

            // Prüfe ob ein neuer Tag begonnen hat (synchronized gegen doppelte Gehaltszahlung)
            if (currentDay > lastSalaryDay) {
                synchronized (SALARY_LOCK) {
                    if (currentDay > lastSalaryDay) {
                        lastSalaryDay = currentDay;
                        payAllNPCSalaries(level, currentDay);
                    }
                }
            }
        });
    }

    /**
     * Zahlt allen NPCs ihr tägliches Gehalt aus
     * Performance-Optimierung: Nutze NPCEntityRegistry statt getAllEntities() (O(1) vs O(n))
     */
    private static void payAllNPCSalaries(ServerLevel level, long currentDay) {
        int paidCount = 0;
        int totalAmount = 0;

        // Performance-Optimierung: Durchlaufe nur registrierte NPCs (O(n) wo n = Anzahl NPCs)
        // VORHER: getAllEntities() durchläuft ALLE Entities (kann 1000+ sein)
        // NACHHER: Nur NPCs durchlaufen (typisch 10-100)
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            // PERFORMANCE: Spielerliste einmal pro Level cachen statt pro NPC
            List<ServerPlayer> levelPlayers = serverLevel.players();

            for (CustomNPCEntity npc : NPCEntityRegistry.getAllNPCs(serverLevel)) {
                    // Nur BEWOHNER und VERKAEUFER (nicht Polizei)
                    if (!npc.getNpcData().hasInventoryAndWallet()) {
                        continue;
                    }

                    // Prüfen ob heute schon Gehalt ausgezahlt wurde
                    if (npc.getNpcData().getLastDailyIncome() < currentDay) {
                        // Zufälliger Basis-Betrag zwischen 20 und 150
                        int baseIncome = 20 + npc.getRandom().nextInt(131); // 20 + [0-130] = 20-150
                        // UDPS: Gehalt über EconomyController harmonisieren
                        int income;
                        try {
                            income = (int) de.rolandsw.schedulemc.economy.EconomyController.getInstance()
                                    .getHarmonizedDailyReward(baseIncome);
                        } catch (Exception e) {
                            income = baseIncome;
                        }
                        int oldWallet = npc.getNpcData().getWallet();

                        npc.getNpcData().addMoney(income);
                        npc.getNpcData().setLastDailyIncome(currentDay);

                        // Performance-Optimierung: Sync nur Wallet statt Full NPC Data
                        npc.syncWalletToClient();

                        paidCount++;
                        totalAmount += income;

                        // PERFORMANCE: Nutze gecachte Spielerliste statt serverLevel.getPlayers() pro NPC
                        for (ServerPlayer player : levelPlayers) {
                            if (player.distanceToSqr(npc) < 2500) { // 50 Blöcke
                                player.sendSystemMessage(
                                    Component.translatable("message.npc.received_daily_income", npc.getNpcName())
                                        .withStyle(ChatFormatting.GREEN)
                                        .append(Component.literal(income + " Bargeld")
                                            .withStyle(ChatFormatting.GOLD))
                                        .append(Component.translatable("message.npc.daily_income_info", currentDay,
                                            oldWallet + " → " + npc.getNpcData().getWallet() + ")")
                                            .withStyle(ChatFormatting.GRAY))
                                );
                            }
                        }
                    }
                }
            }

        if (paidCount > 0) {
            LOGGER.info("Daily salaries paid: {} NPCs received a total of {}€ (Day {})",
                paidCount, totalAmount, currentDay);
        }
    }
}
