package de.rolandsw.schedulemc.mapview.integration.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.gui.Gui;

// @Mixin(Gui.class)
public class MixinInGameHud {

    // this method: private void displayScoreboardSidebar(GuiGraphics guiGraphics, Objective objective)
    // this variable: int o = guiGraphics.guiHeight() / 2 + n / 3;
    //
    // entriesHeight is: int n = m * 9;

    // @ModifyVariable(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V", at = @At("STORE"), ordinal = 6, require = 0)
    private int injected(int bottomX, @Local(ordinal = 5) int entriesHeight) {
        return MapViewConstants.moveScoreboard(bottomX, entriesHeight);
    }
}
