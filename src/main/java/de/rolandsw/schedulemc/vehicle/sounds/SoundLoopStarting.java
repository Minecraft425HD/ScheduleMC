package de.rolandsw.schedulemc.vehicle.sounds;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundLoopStarting extends SoundLoopVehicle {

    public SoundLoopStarting(EntityGenericVehicle vehicle, SoundEvent event, SoundSource category) {
        super(vehicle, event, category);
        this.looping = true;
    }

    @Override
    public void tick() {
        if (vehicle instanceof EntityGenericVehicle) {
            pitch = ((EntityGenericVehicle) vehicle).getBatterySoundPitchLevel();
        }
        super.tick();
    }

    @Override
    public boolean shouldStopSound() {
        if (!(vehicle instanceof EntityGenericVehicle)) {
            return true;
        }
        return !((EntityGenericVehicle) vehicle).isStarting();
    }

}
