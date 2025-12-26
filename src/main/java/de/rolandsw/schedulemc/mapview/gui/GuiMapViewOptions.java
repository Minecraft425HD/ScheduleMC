package de.rolandsw.schedulemc.mapview.gui;

import de.rolandsw.schedulemc.mapview.MapConfiguration;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.gui.overridden.EnumOptionsMapView;
import de.rolandsw.schedulemc.mapview.gui.overridden.GuiOptionButtonMapView;
import de.rolandsw.schedulemc.mapview.gui.overridden.GuiScreenMapView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GuiMapViewOptions extends GuiScreenMapView {
    private final Screen parent;
    private final MapConfiguration options;
    protected String screenTitle = "Minimap Options";

    public GuiMapViewOptions(Screen parent) {
        this.parent = parent;
        this.setParentScreen(this.parent);

        this.options = MapViewConstants.getLightMapInstance().getMapOptions();
    }

    @Override
    public void init() {
        EnumOptionsMapView[] relevantOptions = { EnumOptionsMapView.LOCATION, EnumOptionsMapView.SIZE };
        this.screenTitle = I18n.get("options.minimap.title");

        for (int i = 0; i < relevantOptions.length; i++) {
            EnumOptionsMapView option = relevantOptions[i];
            GuiOptionButtonMapView optionButton = new GuiOptionButtonMapView(getWidth() / 2 - 155 + i % 2 * 160, getHeight() / 6 + 24 * (i >> 1), option, Component.literal(options.getKeyText(option)), this::optionClicked);
            this.addRenderableWidget(optionButton);
        }

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.done"), button -> MapViewConstants.getMinecraft().setScreen(this.parent)).bounds(this.getWidth() / 2 - 100, this.getHeight() - 28, 200, 20).build());
    }

    protected void optionClicked(Button par1GuiButton) {
        EnumOptionsMapView option = ((GuiOptionButtonMapView) par1GuiButton).returnEnumOptions();
        this.options.setOptionValue(option);
        par1GuiButton.setMessage(Component.literal(this.options.getKeyText(option)));
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawCenteredString(this.font, this.screenTitle, this.getWidth() / 2, 20, 0xFFFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
