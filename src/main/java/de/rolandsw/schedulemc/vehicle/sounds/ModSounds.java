package de.rolandsw.schedulemc.vehicle.sounds;

import de.rolandsw.schedulemc.vehicle.Main;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUND_REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MODID);
    private static final List<RegistryObject<SoundEvent>> VEHICLE_SOUND_EVENTS = new ArrayList<>();

    public static RegistryObject<SoundEvent> ENGINE_STOP = vehicleSound("motor_stop");
    public static RegistryObject<SoundEvent> ENGINE_STARTING = vehicleSound("motor_starting");
    public static RegistryObject<SoundEvent> ENGINE_START = vehicleSound("motor_start");
    public static RegistryObject<SoundEvent> ENGINE_IDLE = vehicleSound("motor_idle");
    public static RegistryObject<SoundEvent> ENGINE_HIGH = vehicleSound("motor_high");
    public static RegistryObject<SoundEvent> ENGINE_FAIL = vehicleSound("motor_fail");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_STOP = vehicleSound("performance_motor_stop");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_STARTING = vehicleSound("performance_motor_starting");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_START = vehicleSound("performance_motor_start");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_IDLE = vehicleSound("performance_motor_idle");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_HIGH = vehicleSound("performance_motor_high");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_FAIL = vehicleSound("performance_motor_fail");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_STOP = vehicleSound("performance_2_motor_stop");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_STARTING = vehicleSound("performance_2_motor_starting");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_START = vehicleSound("performance_2_motor_start");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_IDLE = vehicleSound("performance_2_motor_idle");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_HIGH = vehicleSound("performance_2_motor_high");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_FAIL = vehicleSound("performance_2_motor_fail");
    public static RegistryObject<SoundEvent> VEHICLE_CRASH = vehicleSound("vehicle_crash");
    public static RegistryObject<SoundEvent> VEHICLE_HORN = vehicleSound("vehicle_horn");
    public static RegistryObject<SoundEvent> VEHICLE_LOCK = vehicleSound("vehicle_lock");
    public static RegistryObject<SoundEvent> VEHICLE_UNLOCK = vehicleSound("vehicle_unlock");
    public static RegistryObject<SoundEvent> FUEL_STATION = addSound("fuel_station");
    public static RegistryObject<SoundEvent> GENERATOR = addSound("generator");
    public static RegistryObject<SoundEvent> RATCHET = addSound("ratchet");
    public static RegistryObject<SoundEvent> FUEL_STATION_ATTENDANT = addSound("fuel_station_attendant");

    /** Registriert einen Sound und markiert ihn als Fahrzeug-Sound für isVehicleSoundCategory(). */
    private static RegistryObject<SoundEvent> vehicleSound(String soundName) {
        RegistryObject<SoundEvent> reg = addSound(soundName);
        VEHICLE_SOUND_EVENTS.add(reg);
        return reg;
    }

    public static RegistryObject<SoundEvent> addSound(String soundName) {
        return SOUND_REGISTER.register(soundName, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Main.MODID, soundName)));
    }

    public static void init(IEventBus modEventBus) {
        SOUND_REGISTER.register(modEventBus);
    }

    public static void playSound(SoundEvent evt, Level world, BlockPos pos, Player entity, SoundSource category, float volume) {
        playSound(evt, world, pos, entity, category, volume, 1.0F);
    }

    public static void playSound(SoundEvent evt, Level world, BlockPos pos, Player entity, SoundSource category, float volume, float pitch) {
        if (entity != null) {
            world.playSound(entity, pos, evt, category, volume, pitch);
        } else {
            if (!world.isClientSide) {
                world.playSound(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, evt, category, volume, pitch);
            }
        }
    }

    public static void playSound(SoundEvent evt, Level world, BlockPos pos, Player entity, SoundSource category) {
        playSound(evt, world, pos, entity, category, 0.15F);
    }

    public static boolean isVehicleSoundCategory(SoundEvent event) {
        if (event == null) return false;
        return VEHICLE_SOUND_EVENTS.stream().anyMatch(r -> event.equals(r.get()));
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundLoop(AbstractTickableSoundInstance loop, Level world) {
        if (world.isClientSide) {
            Minecraft.getInstance().getSoundManager().play(loop);
        }
    }

}
