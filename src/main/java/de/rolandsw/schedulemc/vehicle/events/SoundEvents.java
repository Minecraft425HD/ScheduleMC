package de.rolandsw.schedulemc.vehicle.events;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class SoundEvents {

    @SubscribeEvent
    public void onSound(PlayLevelSoundEvent.AtEntity event) {
        if (event.getSound() != null && ModSounds.isVehicleSoundCategory(event.getSound().get())) {
            event.setNewVolume(ModConfigHandler.VEHICLE_CLIENT.vehicleVolume.get().floatValue());
        }
    }

    @SubscribeEvent
    public void onSound(PlayLevelSoundEvent.AtPosition event) {
        if (event.getSound() != null && ModSounds.isVehicleSoundCategory(event.getSound().get())) {
            event.setNewVolume(ModConfigHandler.VEHICLE_CLIENT.vehicleVolume.get().floatValue());
        }
    }

}
