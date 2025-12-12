package de.rolandsw.schedulemc.car.sounds;

import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundLoopHigh extends SoundLoopCar {

    public SoundLoopHigh(EntityGenericCar car, SoundEvent event, SoundSource category) {
        super(car, event, category);
    }

    @Override
    public void tick() {
        pitch = car.getPitch();
        super.tick();
    }

    @Override
    public boolean shouldStopSound() {
        if (car.getSpeed() == 0F) {
            return true;
        } else if (!car.isStarted()) {
            return true;
        }

        return false;
    }

}
