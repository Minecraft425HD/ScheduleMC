package de.rolandsw.schedulemc.vehicle.sounds;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundLoopHigh extends SoundLoopVehicle {

    public SoundLoopHigh(EntityGenericVehicle vehicle, SoundEvent event, SoundSource category) {
        super(vehicle, event, category);
    }

    @Override
    public void tick() {
        pitch = vehicle.getPitch();
        super.tick();
    }

    @Override
    public boolean shouldStopSound() {
        if (vehicle.getSpeed() == 0F) {
            return true;
        } else if (!vehicle.isStarted()) {
            return true;
        }

        return false;
    }

}
