package de.rolandsw.schedulemc.npc.life.witness;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Zentrale Zeugen-Erkennung für Gewaltverbrechen.
 *
 * Sowohl Angriffe auf NPCs (NPCKnockoutHandler) als auch Spieler-gegen-
 * Spieler-Gewalt (CrimeEventHandler) nutzen diese identische Logik:
 * Zeugen im 16-Block-Radius suchen, Erkennungschance bestimmen
 * (Polizei = 100%, sonst 15% pro Zeuge bis max 90%), bei Erkennung
 * sofort Fahndungssterne vergeben + Zeugenbericht registrieren.
 */
public final class CrimeWitnessUtil {

    private static final double WITNESS_RADIUS = 16.0;

    private CrimeWitnessUtil() {
    }

    /**
     * Erkennt ein Verbrechen über Zeugen und bestraft den Täter sofort.
     *
     * @param criminal    der Täter
     * @param crimeLocation Tatort
     * @param victimNpc   das NPC-Opfer (null bei Spieler-Opfern) — wird aus der
     *                    Zeugenliste entfernt; ein Polizist-Opfer gilt als Zeuge
     * @param victimUuid  Opfer-UUID (NPC- oder Spieler-UUID) für den Bericht
     * @param crimeType   Verbrechenstyp
     * @param starsToAdd  sofort zu vergebende Sterne
     * @param crimeLabel  Klartext fürs Chat-Feedback
     * @return true, wenn das Verbrechen gesehen (und bestraft) wurde
     */
    public static boolean detectAndPunish(ServerPlayer criminal, BlockPos crimeLocation,
                                          @Nullable CustomNPCEntity victimNpc,
                                          @Nullable UUID victimUuid,
                                          CrimeType crimeType, int starsToAdd, String crimeLabel) {
        if (!(criminal.level() instanceof ServerLevel level)) return false;

        List<CustomNPCEntity> witnesses = level.getEntitiesOfClass(
            CustomNPCEntity.class, AABB.ofSize(criminal.position(), WITNESS_RADIUS, WITNESS_RADIUS, WITNESS_RADIUS));
        if (victimNpc != null) {
            witnesses.remove(victimNpc);
        }

        // Ein Polizist als Opfer ist selbst Zeuge -> Angriff wird immer erkannt
        boolean victimIsPolice = victimNpc != null && victimNpc.getNpcType() == NPCType.POLICE;
        if (witnesses.isEmpty() && !victimIsPolice) {
            return false;
        }

        boolean policePresent = victimIsPolice;
        for (CustomNPCEntity witness : witnesses) {
            if (witness.getNpcType() == NPCType.POLICE) {
                policePresent = true;
                break;
            }
        }

        // Polizei in Sichtweite erkennt immer. Zivilisten melden nur, wenn Zeugenberichte
        // aktiviert sind (sonst rein polizeibasierte Wahrnehmung).
        boolean reportsEnabled = ModConfigHandler.COMMON.WITNESS_REPORTS_ENABLED.get();
        double detectionChance = policePresent ? 1.0
            : (reportsEnabled ? Math.min(0.9, witnesses.size() * 0.15) : 0.0);
        if (ThreadLocalRandom.current().nextDouble() >= detectionChance) {
            return false;
        }

        long currentDay = level.getDayTime() / 24000;
        CrimeManager.addWantedLevel(criminal.getUUID(), starsToAdd, currentDay);

        // Zeugenbericht-Pipeline nur bei aktivierten Reports (no-op'd ohnehin in registerCrime).
        if (reportsEnabled) {
            WitnessManager.getManager(level).registerCrime(criminal, crimeType, crimeLocation, level, victimUuid);
        }

        var integration = de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration.get(level);
        if (integration != null) {
            // Bestes verfügbares "Reaktions-NPC": Opfer-NPC, sonst erster Zeuge
            CustomNPCEntity reactor = victimNpc != null ? victimNpc
                : (witnesses.isEmpty() ? null : witnesses.get(0));
            if (reactor != null) {
                integration.onCrimeWitnessed(criminal, crimeType, reactor);
            }
        }

        int currentWantedLevel = CrimeManager.getWantedLevel(criminal.getUUID());
        String stars = "⭐".repeat(Math.max(0, currentWantedLevel));
        criminal.sendSystemMessage(Component.translatable(
            policePresent ? "message.crime.police_witnessed" : "message.crime.witnessed"));
        criminal.sendSystemMessage(Component.translatable("message.crime.type", crimeLabel));
        criminal.sendSystemMessage(Component.translatable("message.crime.wanted_level", stars, currentWantedLevel));
        return true;
    }

    /**
     * Ahndet ein bereits von einem Polizisten <b>gesehenes</b> Verbrechen direkt — ohne
     * erneuten 16-Block-Zeugen-Scan, da der Sichtkontakt schon feststeht.
     *
     * <p>Wird z.&nbsp;B. von {@code PoliceGunshotHandler} genutzt, wenn ein Schuss in
     * Sichtlinie eines Polizisten fällt (auch ohne Treffer). Vergibt sofort
     * Fahndungssterne, gibt Chat-Feedback und registriert — nur bei aktivierten
     * Zeugenberichten — zusätzlich einen Bericht.
     *
     * @param criminal der Täter
     * @param level    die ServerLevel
     * @param location Tatort
     * @param crimeType Verbrechenstyp
     * @param starsToAdd sofort zu vergebende Sterne
     * @param crimeLabel Klartext fürs Chat-Feedback
     */
    public static void reportPoliceSighted(ServerPlayer criminal, ServerLevel level, BlockPos location,
                                           CrimeType crimeType, int starsToAdd, String crimeLabel) {
        long currentDay = level.getDayTime() / 24000;
        CrimeManager.addWantedLevel(criminal.getUUID(), starsToAdd, currentDay);

        if (ModConfigHandler.COMMON.WITNESS_REPORTS_ENABLED.get()) {
            WitnessManager.getManager(level).registerCrime(criminal, crimeType, location, level, null);
        }

        int currentWantedLevel = CrimeManager.getWantedLevel(criminal.getUUID());
        String stars = "⭐".repeat(Math.max(0, currentWantedLevel));
        criminal.sendSystemMessage(Component.translatable("message.crime.police_witnessed"));
        criminal.sendSystemMessage(Component.translatable("message.crime.type", crimeLabel));
        criminal.sendSystemMessage(Component.translatable("message.crime.wanted_level", stars, currentWantedLevel));
    }
}
