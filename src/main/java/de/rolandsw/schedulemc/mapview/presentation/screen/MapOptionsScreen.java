package de.rolandsw.schedulemc.mapview.presentation.screen;

import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.config.MapOption;
import de.rolandsw.schedulemc.mapview.presentation.component.OptionButton;
import de.rolandsw.schedulemc.mapview.presentation.screen.BaseMapScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MapOptionsScreen extends BaseMapScreen {
    private final Screen parent;
    private final MapViewConfiguration options;
    protected String screenTitle = "Minimap Options";

    public MapOptionsScreen(Screen parent) {
        this.parent = parent;
        this.setParentScreen(this.parent);

        this.options = MapViewConstants.getLightMapInstance().getMapOptions();
    }

    @Override
    public void init() {
        MapOption[] relevantOptions = { MapOption.LOCATION, MapOption.SIZE, MapOption.SHOW_TERRITORIES };
        this.screenTitle = I18n.get("options.minimap.title");

        for (int i = 0; i < relevantOptions.length; i++) {
            MapOption option = relevantOptions[i];
            OptionButton optionButton = new OptionButton(getWidth() / 2 - 155 + i % 2 * 160, getHeight() / 6 + 24 * (i >> 1), option, Component.literal(options.getKeyText(option)), this::optionClicked);
            this.addRenderableWidget(optionButton);
        }

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.done"), button -> MapViewConstants.getMinecraft().setScreen(this.parent)).bounds(this.getWidth() / 2 - 100, this.getHeight() - 28, 200, 20).build());
    }

    protected void optionClicked(Button par1GuiButton) {
        MapOption option = ((OptionButton) par1GuiButton).returnEnumOptions();
        this.options.setOptionValue(option);
        par1GuiButton.setMessage(Component.literal(this.options.getKeyText(option)));
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawCenteredString(this.font, this.screenTitle, this.getWidth() / 2, 20, 0xFFFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
