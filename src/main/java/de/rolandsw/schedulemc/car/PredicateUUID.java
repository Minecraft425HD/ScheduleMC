package de.rolandsw.schedulemc.car;

import java.util.UUID;

import com.google.common.base.Predicate;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;

public class PredicateUUID implements Predicate<EntityGenericCar> {

    private UUID uuid;

    public PredicateUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean apply(EntityGenericCar input) {
        return input.getUUID().equals(uuid);
    }

}
