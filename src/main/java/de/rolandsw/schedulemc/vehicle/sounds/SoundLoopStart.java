package de.rolandsw.schedulemc.vehicle.sounds;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundLoopStart extends SoundLoopVehicle {

    public SoundLoopStart(EntityGenericVehicle vehicle, SoundEvent event, SoundSource category) {
        super(vehicle, event, category);
        this.looping = false;
    }

    @Override
    public boolean shouldStopSound() {
        return !vehicle.isStarted();
    }
}
