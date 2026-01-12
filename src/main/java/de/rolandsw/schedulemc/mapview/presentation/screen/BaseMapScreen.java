package de.rolandsw.schedulemc.mapview.presentation.screen;

import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BaseMapScreen extends Screen {
    protected BaseMapScreen() { this (Component.literal("")); }

    protected BaseMapScreen(Component title) {
        super (title);
    }

    @Override
    public void removed() { MapViewConfiguration.instance.saveAll(); }

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

        
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}