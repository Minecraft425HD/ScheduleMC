package de.rolandsw.schedulemc.lightmap.gui;

import de.rolandsw.schedulemc.lightmap.MinimapSettings;
import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import de.rolandsw.schedulemc.lightmap.gui.overridden.EnumOptionsMinimap;
import de.rolandsw.schedulemc.lightmap.gui.overridden.GuiOptionButtonMinimap;
import de.rolandsw.schedulemc.lightmap.gui.overridden.GuiScreenMinimap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GuiMinimapOptions extends GuiScreenMinimap {
    private final Screen parent;
    private final MinimapSettings options;
    protected String screenTitle = "Minimap Options";

    public GuiMinimapOptions(Screen parent) {
        this.parent = parent;
        this.setParentScreen(this.parent);

        this.options = LightMapConstants.getLightMapInstance().getMapOptions();
    }

    @Override
    public void init() {
        EnumOptionsMinimap[] relevantOptions = { EnumOptionsMinimap.LOCATION, EnumOptionsMinimap.SIZE };
        this.screenTitle = I18n.get("options.minimap.title");

        for (int i = 0; i < relevantOptions.length; i++) {
            EnumOptionsMinimap option = relevantOptions[i];
            GuiOptionButtonMinimap optionButton = new GuiOptionButtonMinimap(getWidth() / 2 - 155 + i % 2 * 160, getHeight() / 6 + 24 * (i >> 1), option, Component.literal(options.getKeyText(option)), this::optionClicked);
            this.addRenderableWidget(optionButton);
        }

        this.addRenderableWidget(new Button.Builder(Component.translatable("gui.done"), button -> LightMapConstants.getMinecraft().setScreen(this.parent)).bounds(this.getWidth() / 2 - 100, this.getHeight() - 28, 200, 20).build());
    }

    protected void optionClicked(Button par1GuiButton) {
        EnumOptionsMinimap option = ((GuiOptionButtonMinimap) par1GuiButton).returnEnumOptions();
        this.options.setOptionValue(option);
        par1GuiButton.setMessage(Component.literal(this.options.getKeyText(option)));
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawCenteredString(this.font, this.screenTitle, this.getWidth() / 2, 20, 0xFFFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
