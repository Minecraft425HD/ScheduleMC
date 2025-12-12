package de.rolandsw.schedulemc.vehicle.sounds;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundLoopIdle extends SoundLoopVehicle {

    private float volumeToReach;

    public SoundLoopIdle(EntityGenericVehicle vehicle, SoundEvent event, SoundSource category) {
        super(vehicle, event, category);
        volumeToReach = volume;
        volume = volume / 2.5F;
    }

    @Override
    public void tick() {
        if (volume < volumeToReach) {
            volume = Math.min(volume + volumeToReach / 2.5F, volumeToReach);
        }
        super.tick();
    }

    @Override
    public boolean shouldStopSound() {
        if (vehicle.getSpeed() != 0) {
            return true;
        } else if (!vehicle.isStarted()) {
            return true;
        }
        return false;
    }


}
