package de.rolandsw.schedulemc.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.rolandsw.schedulemc.client.WorldCreateConfigState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fügt im "More"-Reiter des Welt-erstellen-Screens einen Button "ScheduleMC Config" hinzu —
 * als neue Zeile direkt unter den vorhandenen Buttons, gleiche Breite (210) und gleicher
 * Abstand (über die RowHelper desselben Grids).
 *
 * Statt eines @Shadow auf das geerbte layout-Feld (das die Mixin-AP nicht auflösen kann) wird
 * die lokale GridLayout.RowHelper des MoreTab-Konstruktors via MixinExtras @Local abgefangen.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$MoreTab")
public class CreateWorldMoreTabMixin {

    @Inject(method = "<init>", at = @At("TAIL"), require = 0)
    private void schedulemc$addConfigButton(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        Button button = Button.builder(
            Component.literal("ScheduleMC Config"),
            b -> WorldCreateConfigState.openConfig(Minecraft.getInstance().screen)
        ).width(210).build();
        rowHelper.addChild(button);
        WorldCreateConfigState.moreTabButtonAdded = true;
    }
}
