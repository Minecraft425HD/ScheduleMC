package de.rolandsw.schedulemc.vehicle.vehicle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Trackt Fahrzeug-Eigentümerschaft für Auto-LicensePlate Generierung
 * Format: XXX-YY wobei:
 * - XXX = 3 Anfangsbuchstaben des Spielernamens
 * - YY = Fahrzeug-Nummer (01-99)
 *
 * Bei gleichem Präfix erhalten Spieler Offsets (0, 10, 20, 30...)
 */
public class VehicleOwnershipTracker extends SavedData {

    private static final String DATA_NAME = "vehicle_ownership";

    // Spieler UUID -> Anzahl gekaufter Fahrzeuge
    private final Map<UUID, Integer> vehicleCounts = new HashMap<>();

    // Präfix (z.B. "MIN") -> (Spieler UUID -> Offset)
    private final Map<String, Map<UUID, Integer>> prefixOffsets = new HashMap<>();

    public VehicleOwnershipTracker() {
    }

    public VehicleOwnershipTracker(CompoundTag tag) {
        load(tag);
    }

    /**
     * Holt oder erstellt den Tracker für die gegebene Welt
     */
    public static VehicleOwnershipTracker get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(VehicleOwnershipTracker::new, VehicleOwnershipTracker::new, DATA_NAME);
    }

    /**
     * Registriert einen Fahrzeugkauf und gibt die Kennzeichen-Nummer zurück
     * @param player Der Spieler, der ein Fahrzeug kauft
     * @param prefix Das Kennzeichen-Präfix (3 Buchstaben)
     * @return Die Kennzeichen-Nummer (01-99)
     */
    public int registerVehiclePurchase(Player player, String prefix) {
        UUID playerUUID = player.getUUID();

        // Hole aktuelle Fahrzeuganzahl
        int currentCount = vehicleCounts.getOrDefault(playerUUID, 0);

        // Hole Offset für diesen Spieler bei diesem Präfix
        int offset = getOrCreateOffset(prefix, playerUUID);

        // Berechne Kennzeichen-Nummer: offset + currentCount + 1
        int plateNumber = offset + currentCount + 1;

        // Erhöhe Counter
        vehicleCounts.put(playerUUID, currentCount + 1);

        setDirty();
        return plateNumber;
    }

    /**
     * Holt oder erstellt den Offset für einen Spieler bei einem Präfix
     */
    private int getOrCreateOffset(String prefix, UUID playerUUID) {
        Map<UUID, Integer> prefixMap = prefixOffsets.computeIfAbsent(prefix, k -> new HashMap<>());

        if (prefixMap.containsKey(playerUUID)) {
            return prefixMap.get(playerUUID);
        }

        // Finde nächsten freien Offset (0, 10, 20, 30...)
        int nextOffset = 0;
        while (prefixMap.containsValue(nextOffset)) {
            nextOffset += 10;
            // Sicherheitsbegrenzung: Bei > 90 suche ersten ungenutzten Offset
            if (nextOffset > 90) {
                // Suche Lücken in existierenden Offsets (z.B. wenn ein Spieler entfernt wurde)
                for (int candidate = 0; candidate <= 90; candidate += 10) {
                    if (!prefixMap.containsValue(candidate)) {
                        nextOffset = candidate;
                        break;
                    }
                }
                // Wenn wirklich alle belegt, nutze overflow mit dreistelligen Nummern
                if (nextOffset > 90) {
                    nextOffset = prefixMap.size() * 10;
                }
                break;
            }
        }

        prefixMap.put(playerUUID, nextOffset);
        setDirty();
        return nextOffset;
    }

    /**
     * Gibt die Anzahl der Fahrzeuge eines Spielers zurück
     */
    public int getVehicleCount(UUID playerUUID) {
        return vehicleCounts.getOrDefault(playerUUID, 0);
    }

    /**
     * Lädt Daten aus NBT
     */
    private void load(CompoundTag tag) {
        // Lade Fahrzeug-Counts
        if (tag.contains("VehicleCounts")) {
            CompoundTag counts = tag.getCompound("VehicleCounts");
            for (String key : counts.getAllKeys()) {
                UUID uuid = UUID.fromString(key);
                int count = counts.getInt(key);
                vehicleCounts.put(uuid, count);
            }
        }

        // Lade Präfix-Offsets
        if (tag.contains("PrefixOffsets")) {
            ListTag prefixList = tag.getList("PrefixOffsets", Tag.TAG_COMPOUND);
            for (int i = 0; i < prefixList.size(); i++) {
                CompoundTag prefixTag = prefixList.getCompound(i);
                String prefix = prefixTag.getString("Prefix");

                Map<UUID, Integer> offsetMap = new HashMap<>();
                if (prefixTag.contains("Offsets")) {
                    CompoundTag offsets = prefixTag.getCompound("Offsets");
                    for (String key : offsets.getAllKeys()) {
                        UUID uuid = UUID.fromString(key);
                        int offset = offsets.getInt(key);
                        offsetMap.put(uuid, offset);
                    }
                }
                prefixOffsets.put(prefix, offsetMap);
            }
        }
    }

    /**
     * Speichert Daten in NBT
     */
    @Override
    @Nonnull
    public CompoundTag save(CompoundTag tag) {
        // Speichere Fahrzeug-Counts
        CompoundTag counts = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : vehicleCounts.entrySet()) {
            counts.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("VehicleCounts", counts);

        // Speichere Präfix-Offsets
        ListTag prefixList = new ListTag();
        for (Map.Entry<String, Map<UUID, Integer>> entry : prefixOffsets.entrySet()) {
            CompoundTag prefixTag = new CompoundTag();
            prefixTag.putString("Prefix", entry.getKey());

            CompoundTag offsets = new CompoundTag();
            for (Map.Entry<UUID, Integer> offsetEntry : entry.getValue().entrySet()) {
                offsets.putInt(offsetEntry.getKey().toString(), offsetEntry.getValue());
            }
            prefixTag.put("Offsets", offsets);

            prefixList.add(prefixTag);
        }
        tag.put("PrefixOffsets", prefixList);

        return tag;
    }
}
