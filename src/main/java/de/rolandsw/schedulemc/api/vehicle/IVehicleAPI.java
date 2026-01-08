package de.rolandsw.schedulemc.api.vehicle;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Public Vehicle API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Fahrzeug-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Fahrzeug-Spawning und -Verwaltung</li>
 *   <li>Fahrzeug-Besitzer-Verwaltung</li>
 *   <li>Kraftstoff-System</li>
 *   <li>Fahrzeug-Kauf und -Verkauf</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap-basierte Registry.
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IVehicleAPI vehicleAPI = ScheduleMCAPI.getVehicleAPI();
 *
 * // Fahrzeug spawnen
 * EntityGenericVehicle vehicle = vehicleAPI.spawnVehicle(level, pos, "car_sports");
 *
 * // Besitzer setzen
 * vehicleAPI.setVehicleOwner(vehicle, playerUUID);
 *
 * // Kraftstoff tanken
 * vehicleAPI.refuelVehicle(vehicle, 50.0);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IVehicleAPI {

    /**
     * Spawnt ein Fahrzeug an der angegebenen Position.
     *
     * @param level Das ServerLevel
     * @param position Die Spawn-Position
     * @param vehicleType Der Fahrzeugtyp (z.B. "car_sports")
     * @return Das gespawnte Fahrzeug oder null bei Fehler
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    @Nullable
    EntityGenericVehicle spawnVehicle(@Nonnull ServerLevel level, @Nonnull BlockPos position, @Nonnull String vehicleType);

    /**
     * Setzt den Besitzer eines Fahrzeugs.
     *
     * @param vehicle Das Fahrzeug
     * @param ownerUUID Die UUID des neuen Besitzers
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void setVehicleOwner(@Nonnull EntityGenericVehicle vehicle, @Nonnull UUID ownerUUID);

    /**
     * Gibt den Besitzer eines Fahrzeugs zurück.
     *
     * @param vehicle Das Fahrzeug
     * @return UUID des Besitzers oder null wenn kein Besitzer
     * @throws IllegalArgumentException wenn vehicle null ist
     */
    @Nullable
    UUID getVehicleOwner(@Nonnull EntityGenericVehicle vehicle);

    /**
     * Tankt ein Fahrzeug.
     *
     * @param vehicle Das Fahrzeug
     * @param amount Kraftstoffmenge in Litern
     * @return true wenn erfolgreich, false wenn Tank voll
     * @throws IllegalArgumentException wenn Parameter ungültig
     */
    boolean refuelVehicle(@Nonnull EntityGenericVehicle vehicle, double amount);

    /**
     * Gibt den aktuellen Kraftstoffstand zurück.
     *
     * @param vehicle Das Fahrzeug
     * @return Kraftstoffstand in Litern
     * @throws IllegalArgumentException wenn vehicle null ist
     */
    double getFuelLevel(@Nonnull EntityGenericVehicle vehicle);

    /**
     * Gibt die maximale Tankkapazität zurück.
     *
     * @param vehicle Das Fahrzeug
     * @return Tankkapazität in Litern
     * @throws IllegalArgumentException wenn vehicle null ist
     */
    double getFuelCapacity(@Nonnull EntityGenericVehicle vehicle);

    /**
     * Gibt alle Fahrzeuge eines Spielers zurück.
     *
     * @param level Das ServerLevel
     * @param ownerUUID Die UUID des Besitzers
     * @return Liste aller Fahrzeuge
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    @Nonnull
    List<EntityGenericVehicle> getPlayerVehicles(@Nonnull ServerLevel level, @Nonnull UUID ownerUUID);

    /**
     * Entfernt ein Fahrzeug aus der Welt.
     *
     * @param vehicle Das zu entfernende Fahrzeug
     * @throws IllegalArgumentException wenn vehicle null ist
     */
    void removeVehicle(@Nonnull EntityGenericVehicle vehicle);
}
