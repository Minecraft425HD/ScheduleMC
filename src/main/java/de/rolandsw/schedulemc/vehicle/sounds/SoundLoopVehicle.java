package de.rolandsw.schedulemc.vehicle.sounds;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public abstract class SoundLoopVehicle extends AbstractTickableSoundInstance {

    protected EntityGenericVehicle vehicle;

    public SoundLoopVehicle(EntityGenericVehicle vehicle, SoundEvent event, SoundSource category) {
        super(event, category, SoundInstance.createUnseededRandom());
        this.vehicle = vehicle;
        this.looping = true;
        this.delay = 0;
        this.volume = ModConfigHandler.CAR_CLIENT.carVolume.get().floatValue();
        this.pitch = 1F;
        this.relative = false;
        this.attenuation = Attenuation.LINEAR;
        this.updatePos();
    }

    public void updatePos() {
        this.x = (float) vehicle.getX();
        this.y = (float) vehicle.getY();
        this.z = (float) vehicle.getZ();
    }

    @Override
    public void tick() {
        if (isStopped()) {
            return;
        }

        if (!vehicle.isAlive()) {
            setDonePlaying();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive()) {
            setDonePlaying();
            return;
        }

        if (shouldStopSound()) {
            setDonePlaying();
            return;
        }

        updatePos();
    }

    public void setDonePlaying() {
        stop();
    }

    public abstract boolean shouldStopSound();

}
