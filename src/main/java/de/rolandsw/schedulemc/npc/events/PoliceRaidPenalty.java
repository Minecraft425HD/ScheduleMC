package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Berechnet und wendet Strafen bei Polizei-Raids an
 *
 * Strafen:
 * - Geldstrafe: 10% vom Kontostand (min. 1.000€)
 * - Fahndungslevel-Erhöhung: Basiert auf gefundenen Items
 * - Gefängnis-Zeit: Wird verdoppelt wenn Kontostand zu niedrig
 */
public class PoliceRaidPenalty {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Wendet alle Strafen nach einem Raid an
     */
    public static void applyPenalties(ServerPlayer player, IllegalActivityScanner.ScanResult scanResult) {
        if (!scanResult.hasIllegalActivity()) {
            return; // Keine illegalen Items gefunden
        }

        // 1. Erhöhe Fahndungslevel
        int wantedIncrease = scanResult.calculateWantedIncrease();
        if (wantedIncrease > 0) {
            int currentWanted = CrimeManager.getWantedLevel(player.getUUID());
            long currentDay = player.level().getDayTime() / 24000L;
            CrimeManager.addWantedLevel(player.getUUID(), wantedIncrease, currentDay);

            // WitnessManager: Polizei-Raid registrieren (Polizei ist Zeuge)
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                var witnessManager = de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getManager(serverLevel);
                witnessManager.registerCrime(
                    player,
                    de.rolandsw.schedulemc.npc.life.witness.CrimeType.DRUG_TRAFFICKING,
                    player.blockPosition(),
                    serverLevel,
                    null  // kein spezifisches Opfer
                );

                // NPCLifeSystemIntegration: Crime Event
                var integration = de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration.get(serverLevel);
                // Finde einen Polizei-NPC in der Nähe als Zeugen
                var policeNPCs = serverLevel.getEntitiesOfClass(
                    de.rolandsw.schedulemc.npc.entity.CustomNPCEntity.class,
                    player.getBoundingBox().inflate(50),
                    npc -> npc.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI
                );
                if (!policeNPCs.isEmpty()) {
                    integration.onCrimeWitnessed(player,
                        de.rolandsw.schedulemc.npc.life.witness.CrimeType.DRUG_TRAFFICKING,
                        policeNPCs.get(0));
                }
            }

            player.sendSystemMessage(Component.literal(
                "§c⚠ FAHNDUNGSLEVEL ERHÖHT: +" + wantedIncrease + "★ (Illegal possession)"
            ));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[RAID] Player {} wanted level increased: {} -> {}",
                    player.getName().getString(), currentWanted, currentWanted + wantedIncrease);
            }
        }

        // 2. Berechne Geldstrafe
        double accountBalance = EconomyManager.getBalance(player.getUUID());
        double fine = calculateFine(accountBalance, scanResult.calculateSeverity());

        player.sendSystemMessage(Component.literal(
            "§c§l⚠ POLIZEI RAID - ILLEGALE AKTIVITÄTEN FESTGESTELLT ⚠"
        ));
        player.sendSystemMessage(Component.literal(
            "§7Gefunden: §c" + scanResult.illegalPlantCount + " Pflanzen, " +
            String.format("%.0f", scanResult.totalCashFound) + "€ Bargeld, " +
            scanResult.illegalItemCount + " Items"
        ));

        // 3. Ziehe Geldstrafe ab
        if (EconomyManager.getBalance(player.getUUID()) >= fine) {
            // Genug Geld - ziehe von Konto ab
            EconomyManager.withdraw(player.getUUID(), fine);
            player.sendSystemMessage(Component.literal(
                "§c✗ GELDSTRAFE: " + String.format("%.2f", fine) + "€ vom Konto abgezogen"
            ));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[RAID] Player {} fine: {}€ (Balance: {}€)",
                    player.getName().getString(), fine, accountBalance);
            }
        } else {
            // Nicht genug Geld - ziehe alles ab + verdoppele Gefängnis
            double availableBalance = EconomyManager.getBalance(player.getUUID());
            if (availableBalance > 0) {
                EconomyManager.withdraw(player.getUUID(), availableBalance);
            }

            player.sendSystemMessage(Component.literal(
                "§c§l✗ NICHT GENUG GELD FÜR STRAFE!"
            ));
            player.sendSystemMessage(Component.literal(
                "§7Kontostand konfisziert: §c" + String.format("%.2f", availableBalance) + "€"
            ));
            player.sendSystemMessage(Component.literal(
                "§c§lHAFTZEIT WIRD VERDOPPELT!"
            ));

            // Verdopple Gefängnis-Zeit wird in PoliceAIHandler.arrestPlayer() gemacht
            player.getPersistentData().putBoolean("DoublePenalty", true);

            LOGGER.warn("[RAID] Player {} cannot pay fine - jail time doubled",
                player.getName().getString());
        }
    }

    /**
     * Berechnet Geldstrafe
     * Option C: 10% vom Kontostand (min. 1.000€)
     */
    private static double calculateFine(double accountBalance, int severity) {
        double percentage = ModConfigHandler.COMMON.POLICE_RAID_ACCOUNT_PERCENTAGE.get();
        double minFine = ModConfigHandler.COMMON.POLICE_RAID_MIN_FINE.get();

        double fine = accountBalance * percentage;

        // Mindeststrafe
        if (fine < minFine) {
            fine = minFine;
        }

        return fine;
    }

    /**
     * Prüft ob Spieler genug Geld für Strafe hat
     */
    public static boolean canPayFine(ServerPlayer player, double fine) {
        return EconomyManager.getBalance(player.getUUID()) >= fine;
    }
}
