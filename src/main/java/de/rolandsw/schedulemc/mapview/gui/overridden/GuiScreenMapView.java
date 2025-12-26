package de.rolandsw.schedulemc.lightmap.gui.overridden;

import de.rolandsw.schedulemc.lightmap.MinimapSettings;
import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GuiScreenMinimap extends Screen {
    protected GuiScreenMinimap() { this (Component.literal("")); }

    protected GuiScreenMinimap(Component title) {
        super (title);
    }

    @Override
    public void removed() { MinimapSettings.instance.saveAll(); }

    public void renderTooltip(GuiGraphics drawContext, Component text, int x, int y) {
        if (!(text != null && text.getString() != null && !text.getString().isEmpty())) {
            return;
        }

//        ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(text.getVisualOrderText());
//        drawContext.renderTooltip(LightMapConstants.getMinecraft().font, List.of(clientTooltipComponent), x, y, DefaultTooltipPositioner.INSTANCE, null);

        Tooltip tooltip = Tooltip.create(text);
        drawContext.renderTooltip(this.font, tooltip.toCharSequence(LightMapConstants.getMinecraft()), x, y);
    }

    public Font getFont() { return this.font; }

    @Override
    public List<? extends GuiEventListener> children() { return super.children(); }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    private Screen parentScreen;

    protected void setParentScreen(Object parent) {
        if (parent instanceof Screen) {
            parentScreen = (Screen) parent;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && parentScreen != null) {
            LightMapConstants.getMinecraft().setScreen(parentScreen);

            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}