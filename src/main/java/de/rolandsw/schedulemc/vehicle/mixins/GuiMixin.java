package de.rolandsw.schedulemc.vehicle.mixins;

import de.rolandsw.schedulemc.vehicle.events.RenderEvents;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Method signature confirmed for Minecraft 1.20.1: renderExperienceBar(GuiGraphics, int)
@SuppressWarnings("PMD.UnusedFormalParameter")
@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    public void renderExperienceBar(GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        if (RenderEvents.onRenderExperienceBar(guiGraphics, i)) {
            ci.cancel();
        }
    }

}
