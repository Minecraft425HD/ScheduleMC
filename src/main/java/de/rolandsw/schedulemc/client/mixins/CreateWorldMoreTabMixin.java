package de.rolandsw.schedulemc.client.mixins;

import de.rolandsw.schedulemc.client.WorldCreateConfigState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fügt im "More"-Reiter des Welt-erstellen-Screens einen Button "ScheduleMC Config" hinzu —
 * als neue Zeile direkt unter den vorhandenen Buttons (Breite 210 wie vanilla, gleicher
 * Abstand über dasselbe GridLayout).
 *
 * Der Mixin ERBT von GridLayoutTab, um direkt auf das geerbte, protected Feld {@code layout}
 * zuzugreifen (kein @Shadow / kein MixinExtras nötig — beides griff in diesem Setup nicht).
 * Der nur-zum-Kompilieren nötige Konstruktor wird zur Laufzeit nie verwendet.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$MoreTab")
public abstract class CreateWorldMoreTabMixin extends GridLayoutTab {

    public CreateWorldMoreTabMixin(Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("TAIL"), require = 0)
    private void schedulemc$addConfigButton(CallbackInfo ci) {
        // Bestehende Buttons zählen (1 Spalte -> Zeilenanzahl), damit der neue Button
        // exakt darunter landet.
        int[] rows = {0};
        this.layout.visitWidgets(widget -> rows[0]++);

        Button button = Button.builder(
            Component.literal("ScheduleMC Config"),
            b -> WorldCreateConfigState.openConfig(Minecraft.getInstance().screen)
        ).width(210).build();

        this.layout.addChild(button, rows[0], 0);
        WorldCreateConfigState.moreTabButtonAdded = true;
    }
}
