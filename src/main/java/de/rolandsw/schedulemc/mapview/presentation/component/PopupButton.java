package de.rolandsw.schedulemc.mapview.presentation.component;

import de.rolandsw.schedulemc.mapview.presentation.screen.IPopupScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class PopupButton extends Button {
    final IPopupScreen parentScreen;

    public PopupButton(int x, int y, int width, int height, Component message, OnPress onPress, IPopupScreen parentScreen) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.parentScreen = parentScreen;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        boolean canHover = this.parentScreen.overPopup(mouseX, mouseY);
        int effectiveMouseX = canHover ? mouseX : 0;
        int effectiveMouseY = canHover ? mouseY : 0;
        super.renderWidget(drawContext, effectiveMouseX, effectiveMouseY, delta);
    }
}
