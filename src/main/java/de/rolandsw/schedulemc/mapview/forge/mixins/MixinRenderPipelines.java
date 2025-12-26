package de.rolandsw.schedulemc.lightmap.forge.mixins;

import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import de.rolandsw.schedulemc.lightmap.forge.ForgeModApiBridge;
// This entire mixin needs to be disabled or replaced with a 1.20.1 alternative
// For now, commenting out to allow compilation
/*
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPipelines.class)
public class MixinRenderPipelines {

    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void onRegisterPipelines(CallbackInfo ci) {
        LightMapConstants.setModApiBride(new ForgeModApiBridge());
    }
}
*/

// The mod API bridge needs to be set somewhere else since RenderPipelines doesn't exist
public class MixinRenderPipelines {
    // Mixin disabled for 1.20.1 compatibility
}
