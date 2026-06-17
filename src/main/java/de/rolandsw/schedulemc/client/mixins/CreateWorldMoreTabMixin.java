package de.rolandsw.schedulemc.client.mixins;

import de.rolandsw.schedulemc.client.gui.config.ConfigCategoryScreen;
import de.rolandsw.schedulemc.config.DefaultConfigEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fügt im "More"-Reiter des Welt-erstellen-Screens einen Button "ScheduleMC Config"
 * hinzu — als neue Zeile direkt unter den vorhandenen Buttons, mit gleicher Breite
 * und gleichem Abstand (über dieselbe GridLayout des Reiters).
 *
 * Der Button öffnet die Config-Vorlage für NEUE Welten ({@link DefaultConfigEditor}).
 */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$MoreTab")
public abstract class CreateWorldMoreTabMixin {

    // Inherited from GridLayoutTab: das Grid, in dem die Reiter-Buttons liegen.
    @Shadow @Final protected GridLayout layout;

    @Inject(method = "<init>", at = @At("TAIL"), require = 0)
    private void schedulemc$addConfigButton(CallbackInfo ci) {
        // Bestehende Buttons zählen (1 Spalte -> Zeilenanzahl) und ihre Breite ermitteln,
        // damit der neue Button exakt darunter und gleich breit erscheint.
        int[] rows = {0};
        int[] width = {150};
        this.layout.visitWidgets(widget -> {
            rows[0]++;
            width[0] = Math.max(width[0], widget.getWidth());
        });

        Button button = Button.builder(
            Component.literal("ScheduleMC Config"),
            b -> {
                DefaultConfigEditor.begin();
                Screen parent = Minecraft.getInstance().screen;
                Minecraft.getInstance().setScreen(new ConfigCategoryScreen(parent, true));
            }
        ).width(width[0]).build();

        // Neue Zeile unter den vorhandenen Buttons (gleiches rowSpacing des Grids).
        this.layout.addChild(button, rows[0], 0);
    }
}
