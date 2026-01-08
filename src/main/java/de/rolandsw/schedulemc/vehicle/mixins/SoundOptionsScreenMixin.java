package de.rolandsw.schedulemc.vehicle.mixins;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Temporarily disabled - method may be private or signature changed in 1.20.1
// TODO: Find correct method name/signature for getAllSoundOptionsExceptMaster in Minecraft 1.20.1
//@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenMixin extends OptionsSubScreen {

    public SoundOptionsScreenMixin(Screen screen, Options options, Component component) {
        super(screen, options, component);
    }

    //@Inject(method = "getAllSoundOptionsExceptMaster", at = @At("RETURN"), cancellable = true, require = 0)
    private void getAllSoundOptionsExceptMaster(CallbackInfoReturnable<OptionInstance<?>[]> cir) {
        OptionInstance<?>[] returnValue = cir.getReturnValue();
        OptionInstance<?>[] newReturnValue = new OptionInstance<?>[returnValue.length + 1];
        System.arraycopy(returnValue, 0, newReturnValue, 0, returnValue.length);
        newReturnValue[returnValue.length] = new OptionInstance<>("soundCategory.vehicle", OptionInstance.noTooltip(), (component, volume) -> {
            return volume == 0D
                    ?
                    Component.translatable("options.generic_value", component, CommonComponents.OPTION_OFF)
                    :
                    Component.translatable("options.percent_value", component, (int) (volume * 100D));
        }, OptionInstance.UnitDouble.INSTANCE, ModConfigHandler.VEHICLE_CLIENT.vehicleVolume.get(), (value) -> {
            ModConfigHandler.VEHICLE_CLIENT.vehicleVolume.set(value);
            ModConfigHandler.VEHICLE_CLIENT.vehicleVolume.save();
        });

        cir.setReturnValue(newReturnValue);
    }

}
