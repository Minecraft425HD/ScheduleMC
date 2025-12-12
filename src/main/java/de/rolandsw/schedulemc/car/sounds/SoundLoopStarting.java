package de.rolandsw.schedulemc.car.sounds;

import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundLoopStarting extends SoundLoopCar {

    public SoundLoopStarting(EntityGenericCar car, SoundEvent event, SoundSource category) {
        super(car, event, category);
        this.looping = true;
    }

    @Override
    public void tick() {
        if (car instanceof EntityGenericCar) {
            pitch = ((EntityGenericCar) car).getBatterySoundPitchLevel();
        }
        super.tick();
    }

    @Override
    public boolean shouldStopSound() {
        if (!(car instanceof EntityGenericCar)) {
            return true;
        }
        return !((EntityGenericCar) car).isStarting();
    }

}
