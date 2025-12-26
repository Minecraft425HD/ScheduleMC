package de.rolandsw.schedulemc.mapview.gui.overridden;

import de.rolandsw.schedulemc.mapview.MapConfiguration;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GuiScreenMapView extends Screen {
    protected GuiScreenMapView() { this (Component.literal("")); }

    protected GuiScreenMapView(Component title) {
        super (title);
    }

    @Override
    public void removed() { MapConfiguration.instance.saveAll(); }

    public void renderTooltip(GuiGraphics drawContext, Component text, int x, int y) {
        if (!(text != null && text.getString() != null && !text.getString().isEmpty())) {
            return;
        }

//        ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(text.getVisualOrderText());
//        drawContext.renderTooltip(MapViewConstants.getMinecraft().font, List.of(clientTooltipComponent), x, y, DefaultTooltipPositioner.INSTANCE, null);

        Tooltip tooltip = Tooltip.create(text);
        drawContext.renderTooltip(this.font, tooltip.toCharSequence(MapViewConstants.getMinecraft()), x, y);
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
            MapViewConstants.getMinecraft().setScreen(parentScreen);

            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}