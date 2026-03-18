package de.rolandsw.schedulemc.weapon.sound;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WeaponSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ScheduleMC.MOD_ID);

    public static final RegistryObject<SoundEvent> GUN_SHOT = register("weapon_gun_shot");
    public static final RegistryObject<SoundEvent> EMPTY_CLICK = register("weapon_empty_click");
    public static final RegistryObject<SoundEvent> CLICK = register("weapon_click");
    public static final RegistryObject<SoundEvent> GRENADE_EXPLODE = register("weapon_grenade_explode");
    public static final RegistryObject<SoundEvent> RELOAD = register("weapon_reload");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(ScheduleMC.MOD_ID, name)));
    }
}
