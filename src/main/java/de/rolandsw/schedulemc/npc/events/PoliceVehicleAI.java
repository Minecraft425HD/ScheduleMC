package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.vehicle.VehicleConstants;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.VehicleFactory;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import de.rolandsw.schedulemc.vehicle.items.InternalVehiclePartItem;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Echtes Polizei-Fahrzeug für Fahrzeugverfolgungen (statt der reinen Fahr-Illusion des
 * bestehenden {@code NPCDrivingScheduler}/{@code NPCVehicleAssignment}-Systems). Nutzt
 * bewusst das bestehende Fahrzeugmodell (Limousine, Farbe Blau) - keine neue Fahrzeugart.
 *
 * Erscheint (per Nutzeranforderung) NUR wenn ein Spieler tatsächlich per Fahrzeug flieht
 * (siehe {@code PoliceVehiclePursuit.startVehiclePursuit}, dort bereits auf
 * {@code isPlayerInVehicle} gegated). Fährt eine gezielte PIT-Manöver-Annäherung statt
 * direkt auf die Fahrzeugmitte zuzusteuern. Wird das Fahrzeug per Schuss/Kollision auf
 * maximalen Schaden gebracht, bleibt es als Wrack liegen und der Polizist wird abgezogen
 * (verschwindet) - der Spieler ist diesen Verfolger dann vollständig los.
 */
public final class PoliceVehicleAI {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Polizei-NPC-UUID -> Fahrzeug-UUID der aktuell gefahrenen echten Verfolgungs-Vehicle. */
    private static final Map<UUID, UUID> policeToVehicle = new ConcurrentHashMap<>();

    private PoliceVehicleAI() {
    }

    /**
     * Spawnt ein echtes Polizei-Fahrzeug (falls noch keins existiert) und montiert den
     * Polizei-NPC als Fahrer. Farbe: Blau (Index 3), analog zur bestehenden
     * NPCVehicleAssignment-Konvention für Polizei-NPCs.
     */
    public static boolean spawnAndMount(CustomNPCEntity police) {
        if (policeToVehicle.containsKey(police.getUUID())) {
            return true;
        }
        if (!(police.level() instanceof ServerLevel level)) {
            return false;
        }

        EntityGenericVehicle vehicle = VehicleFactory.createVehicle(level, buildPoliceParts());
        if (vehicle == null) {
            return false;
        }

        vehicle.setPos(police.getX(), police.getY(), police.getZ());
        vehicle.setYRot(police.getYRot());
        vehicle.setPaintColor(3); // Blau
        vehicle.setVehicleUUID(UUID.randomUUID());
        vehicle.setFuelAmount(vehicle.getMaxFuel());
        vehicle.setBatteryLevel(500);
        vehicle.setNpcDriver(police);

        level.addFreshEntity(vehicle);
        vehicle.setIsSpawned(true);
        vehicle.initTemperature();
        vehicle.getPhysicsComponent().setStarted(true, false, false);

        police.startRiding(vehicle);

        policeToVehicle.put(police.getUUID(), vehicle.getUUID());
        LOGGER.info("[POLICE VEHICLE AI] {} steigt in echtes Verfolgungsfahrzeug ein", police.getNpcName());
        return true;
    }

    /**
     * Erstellt eine einfache, aber vollständige Standard-Limousine (analog
     * {@code ItemSpawnVehicle}) - reicht aus, damit Physik/Schaden/Kraftstoff normal
     * funktionieren, ohne eine eigene Polizei-Fahrzeugkonfiguration zu benötigen.
     */
    private static List<ItemStack> buildPoliceParts() {
        List<ItemStack> parts = new ArrayList<>();
        parts.add(InternalVehiclePartItem.create(PartRegistry.LIMOUSINE_CHASSIS));
        parts.add(InternalVehiclePartItem.create(PartRegistry.NORMAL_MOTOR));
        parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        parts.add(InternalVehiclePartItem.create(PartRegistry.FENDER_BASIC));
        parts.add(InternalVehiclePartItem.create(PartRegistry.TANK_15L));
        parts.add(InternalVehiclePartItem.createLicensePlate(""));
        parts.add(InternalVehiclePartItem.create(PartRegistry.LICENSE_PLATE_HOLDER));
        return parts;
    }

    /**
     * Steuert das Fahrzeug einen Tick lang Richtung Ziel. Sitzt der Spieler selbst in
     * einem Fahrzeug, wird ein PIT-Manöver-Punkt (seitlich-hinten am Zielfahrzeug)
     * angesteuert statt der exakten Fahrzeugmitte - dadurch bleibt für den Spieler
     * eine reale Fluchtchance (Ausweichen, Gegenlenken), statt dass die KI stur auffährt.
     */
    public static void tick(CustomNPCEntity police, ServerPlayer target) {
        UUID vehicleUUID = policeToVehicle.get(police.getUUID());
        if (vehicleUUID == null) {
            return;
        }
        if (!(police.level() instanceof ServerLevel level)) {
            return;
        }

        Entity entity = level.getEntity(vehicleUUID);
        if (!(entity instanceof EntityGenericVehicle vehicle) || !vehicle.isAlive()) {
            policeToVehicle.remove(police.getUUID());
            return;
        }

        // Zerstörung: Fahrzeug bleibt als Wrack liegen, Polizist verschwindet (per
        // Nutzerentscheidung - kein Fortsetzen der Verfolgung zu Fuss).
        if (vehicle.getDamageComponent().getDamage() >= VehicleConstants.MAX_DAMAGE) {
            wreckAndWithdraw(police, vehicle);
            return;
        }

        Vec3 aimPoint = computePitAimPoint(vehicle, target);
        steerTowards(vehicle, aimPoint);
    }

    private static Vec3 computePitAimPoint(EntityGenericVehicle vehicle, ServerPlayer target) {
        Entity targetVehicle = target.getVehicle();
        if (targetVehicle == null) {
            return target.position();
        }

        Vec3 motion = targetVehicle.getDeltaMovement();
        Vec3 flatMotion = new Vec3(motion.x, 0D, motion.z);
        // Fast stehendes Ziel: keine sinnvolle Fluchtrichtung ableitbar - dann einfach
        // von der Verfolgerposition weg Richtung Zielfahrzeug als "Heck"-Richtung annehmen.
        Vec3 rearDir = flatMotion.lengthSqr() > 1.0E-4
            ? flatMotion.normalize()
            : targetVehicle.position().subtract(vehicle.position()).multiply(1D, 0D, 1D).normalize();

        Vec3 sideDir = new Vec3(-rearDir.z, 0D, rearDir.x);
        double approachSide = (vehicle.getX() - targetVehicle.getX()) * sideDir.x
            + (vehicle.getZ() - targetVehicle.getZ()) * sideDir.z;
        double side = approachSide >= 0 ? 1D : -1D;

        return targetVehicle.position()
            .subtract(rearDir.scale(VehicleConstants.PIT_REAR_OFFSET))
            .add(sideDir.scale(side * VehicleConstants.PIT_SIDE_OFFSET));
    }

    private static void steerTowards(EntityGenericVehicle vehicle, Vec3 aimPoint) {
        double dx = aimPoint.x - vehicle.getX();
        double dz = aimPoint.z - vehicle.getZ();
        if (dx * dx + dz * dz < 0.25D) {
            vehicle.updateAIControls(false, false, false, false);
            return;
        }

        double desiredYaw = -Math.toDegrees(Math.atan2(dx, dz));
        double diff = Mth.wrapDegrees(desiredYaw - vehicle.getYRot());

        boolean left = diff < -VehicleConstants.PIT_STEERING_TOLERANCE_DEGREES;
        boolean right = diff > VehicleConstants.PIT_STEERING_TOLERANCE_DEGREES;

        vehicle.updateAIControls(true, false, left, right);
    }

    /**
     * Wird auch von aussen genutzt (z. B. wenn die Verfolgung anderweitig endet), um das
     * Fahrzeug ordentlich abzumelden. {@code asWreck=true} entspricht der
     * Nutzerentscheidung "Fahrzeug bleibt liegen, Polizist verschwindet"; {@code false}
     * (z. B. normales Verfolgungsende ohne Zerstörung) despawnt das Fahrzeug wieder
     * sauber und lässt den Polizisten wie gewohnt zurück (kein Verschwinden).
     */
    public static void despawn(CustomNPCEntity police, boolean asWreck) {
        UUID vehicleUUID = policeToVehicle.remove(police.getUUID());
        if (vehicleUUID == null) {
            return;
        }
        if (!(police.level() instanceof ServerLevel level)) {
            return;
        }

        Entity entity = level.getEntity(vehicleUUID);
        if (entity instanceof EntityGenericVehicle vehicle) {
            vehicle.updateAIControls(false, false, false, false);
            vehicle.getPhysicsComponent().setStarted(false, false, false);
            vehicle.setNpcDriver(null);
            if (!asWreck) {
                vehicle.ejectPassengers();
                vehicle.discard();
            }
        }

        if (asWreck) {
            police.discard();
        }
    }

    private static void wreckAndWithdraw(CustomNPCEntity police, EntityGenericVehicle vehicle) {
        LOGGER.info("[POLICE VEHICLE AI] {} - pursuit vehicle destroyed, officer withdrawn", police.getNpcName());
        PoliceVehiclePursuit.stopVehiclePursuit(police);
        despawn(police, true);
    }

    public static boolean hasRealVehicle(UUID policeUUID) {
        return policeToVehicle.containsKey(policeUUID);
    }

    public static void cleanup(UUID policeUUID) {
        policeToVehicle.remove(policeUUID);
    }
}
