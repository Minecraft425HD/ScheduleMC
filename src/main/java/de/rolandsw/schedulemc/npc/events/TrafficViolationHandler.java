package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
import de.rolandsw.schedulemc.util.ConfigCache;
import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 9: Verkehrsdelikte
 *
 * Erkennt und bestraft Verkehrsvergehen:
 * - NPC ueberfahren (Hit and Run): +2 Sterne
 * - Ruecksichtsloses Fahren (Kollision mit NPC): +1 Stern
 *
 * Integration mit dem Vehicle-System und WitnessManager.
 */
@Mod.EventBusSubscriber
public class TrafficViolationHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Cooldown fuer Verkehrsdelikte pro Spieler (verhindert Spam) */
    private static final Map<UUID, Long> violationCooldowns = new ConcurrentHashMap<>();

    /** Cooldown-Dauer in Millisekunden (30 Sekunden) */
    private static final long VIOLATION_COOLDOWN_MS = 30000L;

    /**
     * Erkennt NPC-Kollisionen durch Fahrzeuge
     */
    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if (!ModConfigHandler.COMMON.POLICE_TRAFFIC_VIOLATIONS_ENABLED.get()) {
            return;
        }

        // Nur wenn ein NPC verletzt wird
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) {
            return;
        }

        // Finde den Verursacher
        Entity source = event.getSource().getEntity();
        if (source == null) return;

        // Pruefe ob Verursacher ein Spieler in einem Fahrzeug ist
        ServerPlayer driver = null;
        if (source instanceof ServerPlayer sp) {
            driver = sp;
        } else if (source.getControllingPassenger() instanceof ServerPlayer sp) {
            driver = sp;
        }

        if (driver == null) return;

        // Pruefe ob Spieler in einem Fahrzeug sitzt
        if (driver.getVehicle() == null) return;

        // Cooldown pruefen
        UUID driverUUID = driver.getUUID();
        Long lastViolation = violationCooldowns.get(driverUUID);
        if (lastViolation != null && System.currentTimeMillis() - lastViolation < VIOLATION_COOLDOWN_MS) {
            return; // Cooldown aktiv
        }
        violationCooldowns.put(driverUUID, System.currentTimeMillis());

        // Verkehrsdelikt registrieren
        long currentDay = driver.level().getDayTime() / 24000;

        if (event.getAmount() > 5.0f) {
            // Schwere Kollision -> Hit and Run
            CrimeManager.addWantedLevel(driverUUID, 2, currentDay,
                CrimeType.HIT_AND_RUN, npc.blockPosition());

            driver.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("event.traffic.hit_and_run"));

            LOGGER.info("[TRAFFIC] {} - Hit and Run auf NPC {}",
                driver.getName().getString(), npc.getNpcName());
        } else {
            // Leichte Kollision -> Ruecksichtsloses Fahren
            CrimeManager.addWantedLevel(driverUUID, 1, currentDay,
                CrimeType.RECKLESS_DRIVING, npc.blockPosition());

            driver.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("event.traffic.reckless_driving"));

            LOGGER.info("[TRAFFIC] {} - Ruecksichtsloses Fahren, NPC {} getroffen",
                driver.getName().getString(), npc.getNpcName());
        }

        // WitnessManager informieren
        if (driver.level() instanceof ServerLevel serverLevel) {
            WitnessManager witnessManager = WitnessManager.getManager(serverLevel);
            CrimeType crimeType = event.getAmount() > 5.0f
                ? CrimeType.HIT_AND_RUN : CrimeType.RECKLESS_DRIVING;
            witnessManager.registerCrime(driver, crimeType, npc.blockPosition(), serverLevel, null);
        }
    }

    /**
     * Feature 3 (Backlog #3): Geschwindigkeitskontrolle
     *
     * Prueft periodisch, ob ein fahrender Spieler das konfigurierte Speed-Limit
     * (`police.speed_limit_default`, in Bloecken/Tick - identische Einheit wie
     * {@code EntityGenericVehicle.getSpeed()}) ueberschreitet UND dabei von einer
     * Polizei-NPC gesehen wird (Sichtlinie, nicht nur Radius - nutzt dieselbe
     * {@code PoliceSearchBehavior.isPlayerHidden}-Pruefung wie das bestehende
     * Verstecken-System). Nur dann wird eine Verkehrsstrafe ausgeloest - ein Limit
     * ohne jede Kontrollinstanz in der Naehe waere unrealistisch strikt.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EventHelper.handlePlayerTickEnd(event, p -> {
            if (!ModConfigHandler.COMMON.POLICE_TRAFFIC_VIOLATIONS_ENABLED.get()) return;
            if (!(p instanceof ServerPlayer driver)) return;
            if (driver.tickCount % 20 != 0) return; // Nur 1x pro Sekunde pruefen

            if (!(driver.getVehicle() instanceof EntityGenericVehicle vehicle)) return;

            float speedLimit = ModConfigHandler.COMMON.POLICE_SPEED_LIMIT_DEFAULT.get().floatValue();
            if (Math.abs(vehicle.getSpeed()) <= speedLimit) return;

            // Cooldown pruefen (gemeinsam mit Kollisions-Verkehrsdelikten, verhindert Spam)
            UUID driverUUID = driver.getUUID();
            Long lastViolation = violationCooldowns.get(driverUUID);
            if (lastViolation != null && System.currentTimeMillis() - lastViolation < VIOLATION_COOLDOWN_MS) {
                return;
            }

            // Nur bestrafen wenn eine Polizei-NPC den Spieler tatsaechlich sehen kann
            int detectionRadius = ConfigCache.getPoliceDetectionRadius();
            List<CustomNPCEntity> nearbyPolice = new ArrayList<>();
            PoliceAIHandler.getPoliceInRadius(driver.position(), detectionRadius, nearbyPolice);

            boolean seenByPolice = false;
            for (CustomNPCEntity police : nearbyPolice) {
                if (!PoliceSearchBehavior.isPlayerHidden(driver, police)) {
                    seenByPolice = true;
                    break;
                }
            }
            if (!seenByPolice) return;

            violationCooldowns.put(driverUUID, System.currentTimeMillis());

            long currentDay = driver.level().getDayTime() / 24000;
            CrimeManager.addWantedLevel(driverUUID, CrimeType.TRAFFIC_VIOLATION.getWantedStars(), currentDay,
                CrimeType.TRAFFIC_VIOLATION, driver.blockPosition());

            driver.sendSystemMessage(
                net.minecraft.network.chat.Component.translatable("event.traffic.speeding"));

            if (driver.level() instanceof ServerLevel serverLevel) {
                WitnessManager witnessManager = WitnessManager.getManager(serverLevel);
                witnessManager.registerCrime(driver, CrimeType.TRAFFIC_VIOLATION,
                    driver.blockPosition(), serverLevel, null);
            }

            LOGGER.info("[TRAFFIC] {} - Geschwindigkeitsueberschreitung ({} > Limit {})",
                driver.getName().getString(), vehicle.getSpeed(), speedLimit);
        });
    }

    /**
     * Bereinigt abgelaufene Cooldowns (sollte periodisch aufgerufen werden)
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        violationCooldowns.entrySet().removeIf(
            entry -> now - entry.getValue() > VIOLATION_COOLDOWN_MS * 2
        );
    }
}
