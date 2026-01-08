package de.rolandsw.schedulemc.vehicle.mixins;

import de.rolandsw.schedulemc.vehicle.events.RenderEvents;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// DISABLED: Mixin incompatible with Minecraft 1.20.1
// NOTE: Method renderExperienceBar() signature changed in 1.20.1
// Requires investigation of Gui.class implementation in current Minecraft version
//@Mixin(Gui.class)
public class GuiMixin {

    //@Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true, require = 0)
    public void renderExperienceBar(GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        if (RenderEvents.onRenderExperienceBar(guiGraphics, i)) {
            ci.cancel();
        }
    }

}
