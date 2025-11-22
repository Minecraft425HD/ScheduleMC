package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Handler für tägliches Einkommen der NPCs
 * Zahlt jeden Tag um 00:00 Uhr (Minecraft-Zeit) einen Betrag zwischen 20-150 an BEWOHNER und VERKÄUFER
 * POLIZEI erhält kein tägliches Einkommen
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class NPCDailyIncomeHandler {

    // Konfiguration
    private static final int MIN_DAILY_INCOME = 20;
    private static final int MAX_DAILY_INCOME = 150;

    // Zeit-Tracking (um 00:00 Uhr = dayTime 0)
    private static final long MIDNIGHT_TIME = 0;
    private static final long TIME_WINDOW = 100; // Zeitfenster um Mitternacht herum (5 Sekunden)

    private static long lastProcessedDay = -1;
    private static final Random random = new Random();

    /**
     * Server Tick Event - prüft auf Mitternacht und zahlt NPCs aus
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Nur auf Server-Seite
        if (event.getServer() == null) return;

        // Prüfe alle Welten
        for (ServerLevel level : event.getServer().getAllLevels()) {
            long dayTime = level.getDayTime() % 24000;
            long currentDay = level.getDayTime() / 24000;

            // Prüfe ob es Mitternacht ist (oder kurz danach im Zeitfenster)
            boolean isMidnight = dayTime >= MIDNIGHT_TIME && dayTime < TIME_WINDOW;

            // Verhindere mehrfaches Ausführen am selben Tag
            if (isMidnight && currentDay != lastProcessedDay) {
                lastProcessedDay = currentDay;
                processNPCDailyIncome(level, currentDay);
            }
        }
    }

    /**
     * Zahlt allen NPCs in der Welt ihr tägliches Einkommen
     */
    private static void processNPCDailyIncome(ServerLevel level, long currentDay) {
        int npcCount = 0;
        int totalPaid = 0;

        // Iteriere über alle Entities in der Welt
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof CustomNPCEntity npc) {
                // Nur für BEWOHNER und VERKÄUFER, nicht für POLIZEI
                if (!npc.getNpcData().hasEconomyFeatures()) {
                    continue;
                }

                // Prüfe ob dieser NPC heute schon Geld erhalten hat
                if (npc.getNpcData().getLastDailyIncomeDay() >= currentDay) {
                    continue; // Bereits bezahlt heute
                }

                // Generiere zufälligen Betrag zwischen MIN und MAX
                int income = MIN_DAILY_INCOME + random.nextInt(MAX_DAILY_INCOME - MIN_DAILY_INCOME + 1);

                // Zahle dem NPC das Geld
                npc.getNpcData().addCash(income);
                npc.getNpcData().setLastDailyIncomeDay(currentDay);

                npcCount++;
                totalPaid += income;

                // Debug-Log
                ScheduleMC.LOGGER.debug("NPC '{}' (Type: {}) erhielt {}€ tägliches Einkommen. Neuer Kontostand: {}€",
                    npc.getNpcName(),
                    npc.getNpcType().getDisplayName(),
                    income,
                    npc.getNpcData().getCash()
                );
            }
        }

        if (npcCount > 0) {
            ScheduleMC.LOGGER.info("Tägliches Einkommen ausgezahlt: {} NPCs erhielten insgesamt {}€ (Tag: {})",
                npcCount, totalPaid, currentDay);
        }
    }

    /**
     * Setzt das minimale tägliche Einkommen (für Konfiguration)
     */
    public static void setMinDailyIncome(int amount) {
        // Diese Methode könnte später für eine Config-Datei genutzt werden
        ScheduleMC.LOGGER.warn("Warnung: MIN_DAILY_INCOME ist derzeit fest auf {} gesetzt", MIN_DAILY_INCOME);
    }

    /**
     * Setzt das maximale tägliche Einkommen (für Konfiguration)
     */
    public static void setMaxDailyIncome(int amount) {
        // Diese Methode könnte später für eine Config-Datei genutzt werden
        ScheduleMC.LOGGER.warn("Warnung: MAX_DAILY_INCOME ist derzeit fest auf {} gesetzt", MAX_DAILY_INCOME);
    }
}
