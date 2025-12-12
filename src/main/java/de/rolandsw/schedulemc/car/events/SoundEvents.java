package de.rolandsw.schedulemc.car.events;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class SoundEvents {

    @SubscribeEvent
    public void onSound(PlayLevelSoundEvent.AtEntity event) {
        if (event.getSound() != null && ModSounds.isCarSoundCategory(event.getSound().get())) {
            event.setNewVolume(ModConfigHandler.CAR_CLIENT.carVolume.get().floatValue());
        }
    }

    @SubscribeEvent
    public void onSound(PlayLevelSoundEvent.AtPosition event) {
        if (event.getSound() != null && ModSounds.isCarSoundCategory(event.getSound().get())) {
            event.setNewVolume(ModConfigHandler.CAR_CLIENT.carVolume.get().floatValue());
        }
    }

}
