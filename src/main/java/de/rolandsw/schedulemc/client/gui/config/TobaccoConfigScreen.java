package de.rolandsw.schedulemc.client.gui.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TobaccoConfigScreen extends Screen {
    private final Screen parent;

    public TobaccoConfigScreen(Screen parent) {
        super(Component.literal("Tobacco Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent))
            .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("§7Tobacco Production System"),
            this.width / 2, this.height / 2, 0x808080);
        graphics.drawCenteredString(this.font, Component.literal("§econfig/schedulemc-common.toml"),
            this.width / 2, this.height / 2 + 20, 0xFFAA00);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
