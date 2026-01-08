package de.rolandsw.schedulemc.vehicle;

import java.util.UUID;

import com.google.common.base.Predicate;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;

public class PredicateUUID implements Predicate<EntityGenericVehicle> {

    private UUID uuid;

    public PredicateUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean apply(EntityGenericVehicle input) {
        return input.getUUID().equals(uuid);
    }

}
