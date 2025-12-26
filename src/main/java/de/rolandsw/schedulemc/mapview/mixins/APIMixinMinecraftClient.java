package de.rolandsw.schedulemc.mapview.mixins;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(Minecraft.class)
public class APIMixinMinecraftClient {

    // @Inject(method = "tick()V", at = @At("RETURN"), require = 0)
    private void onTick(CallbackInfo ci) {
        MapViewConstants.clientTick();
    }

}
