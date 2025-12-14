package de.rolandsw.schedulemc.vehicle.sounds;

import de.rolandsw.schedulemc.vehicle.Main;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUND_REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MODID);

    public static RegistryObject<SoundEvent> ENGINE_STOP = addSound("motor_stop");
    public static RegistryObject<SoundEvent> ENGINE_STARTING = addSound("motor_starting");
    public static RegistryObject<SoundEvent> ENGINE_START = addSound("motor_start");
    public static RegistryObject<SoundEvent> ENGINE_IDLE = addSound("motor_idle");
    public static RegistryObject<SoundEvent> ENGINE_HIGH = addSound("motor_high");
    public static RegistryObject<SoundEvent> ENGINE_FAIL = addSound("motor_fail");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_STOP = addSound("performance_motor_stop");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_STARTING = addSound("performance_motor_starting");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_START = addSound("performance_motor_start");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_IDLE = addSound("performance_motor_idle");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_HIGH = addSound("performance_motor_high");
    public static RegistryObject<SoundEvent> SPORT_ENGINE_FAIL = addSound("performance_motor_fail");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_STOP = addSound("industrial_motor_stop");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_STARTING = addSound("industrial_motor_starting");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_START = addSound("industrial_motor_start");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_IDLE = addSound("industrial_motor_idle");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_HIGH = addSound("industrial_motor_high");
    public static RegistryObject<SoundEvent> TRUCK_ENGINE_FAIL = addSound("industrial_motor_fail");
    public static RegistryObject<SoundEvent> VEHICLE_CRASH = addSound("vehicle_crash");
    public static RegistryObject<SoundEvent> FUEL_STATION = addSound("fuel_station");
    public static RegistryObject<SoundEvent> GENERATOR = addSound("generator");
    public static RegistryObject<SoundEvent> VEHICLE_HORN = addSound("vehicle_horn");
    public static RegistryObject<SoundEvent> VEHICLE_LOCK = addSound("vehicle_lock");
    public static RegistryObject<SoundEvent> VEHICLE_UNLOCK = addSound("vehicle_unlock");
    public static RegistryObject<SoundEvent> RATCHET = addSound("ratchet");
    public static RegistryObject<SoundEvent> FUEL_STATION_ATTENDANT = addSound("fuel_station_attendant");

    public static RegistryObject<SoundEvent> addSound(String soundName) {
        return SOUND_REGISTER.register(soundName, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, soundName)));
    }

    public static void init() {
        SOUND_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
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
        if (event == null) {
            return false;
        }
        return event.equals(ENGINE_STOP.get()) ||
                event.equals(ENGINE_STARTING.get()) ||
                event.equals(ENGINE_START.get()) ||
                event.equals(ENGINE_IDLE.get()) ||
                event.equals(ENGINE_HIGH.get()) ||
                event.equals(ENGINE_FAIL.get()) ||
                event.equals(SPORT_ENGINE_STOP.get()) ||
                event.equals(SPORT_ENGINE_STARTING.get()) ||
                event.equals(SPORT_ENGINE_START.get()) ||
                event.equals(SPORT_ENGINE_IDLE.get()) ||
                event.equals(SPORT_ENGINE_HIGH.get()) ||
                event.equals(SPORT_ENGINE_FAIL.get()) ||
                event.equals(TRUCK_ENGINE_STOP.get()) ||
                event.equals(TRUCK_ENGINE_STARTING.get()) ||
                event.equals(TRUCK_ENGINE_START.get()) ||
                event.equals(TRUCK_ENGINE_IDLE.get()) ||
                event.equals(TRUCK_ENGINE_HIGH.get()) ||
                event.equals(TRUCK_ENGINE_FAIL.get()) ||
                event.equals(VEHICLE_CRASH.get()) ||
                event.equals(VEHICLE_HORN.get()) ||
                event.equals(VEHICLE_LOCK.get()) ||
                event.equals(VEHICLE_UNLOCK.get());

    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundLoop(AbstractTickableSoundInstance loop, Level world) {
        if (world.isClientSide) {
            Minecraft.getInstance().getSoundManager().play(loop);
        }
    }

}
