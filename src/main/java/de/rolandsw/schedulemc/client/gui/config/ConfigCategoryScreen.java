package de.rolandsw.schedulemc.client.gui.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Main Config Screen - Category Selection
 * Allows access to all config categories
 */
@OnlyIn(Dist.CLIENT)
public class ConfigCategoryScreen extends Screen {

    private final Screen parent;

    public ConfigCategoryScreen(Screen parent) {
        super(Component.literal("ScheduleMC Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 60;
        int buttonWidth = 200;
        int spacing = 25;

        // CLIENT SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§b⚙ Client Settings"),
            button -> this.minecraft.setScreen(new ClientConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY, buttonWidth, 20)
        .build());

        // ECONOMY SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§e$ Economy Settings"),
            button -> this.minecraft.setScreen(new EconomyConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, 20)
        .build());

        // PLOT SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§a▣ Plot Settings"),
            button -> this.minecraft.setScreen(new PlotConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, 20)
        .build());

        // POLICE SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§c★ Police Settings"),
            button -> this.minecraft.setScreen(new PoliceConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, 20)
        .build());

        // NPC SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§d☺ NPC Settings"),
            button -> this.minecraft.setScreen(new NPCConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, 20)
        .build());

        // WAREHOUSE SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§6■ Warehouse Settings"),
            button -> this.minecraft.setScreen(new WarehouseConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY + spacing * 5, buttonWidth, 20)
        .build());

        // DYNAMIC PRICING (UDPS)
        this.addRenderableWidget(Button.builder(
            Component.literal("§9≈ Dynamic Pricing (UDPS)"),
            button -> this.minecraft.setScreen(new DynamicPricingConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, 20)
        .build());

        // TOBACCO SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§2⚘ Tobacco Settings"),
            button -> this.minecraft.setScreen(new TobaccoConfigScreen(this))
        )
        .bounds(centerX - buttonWidth / 2, startY + spacing * 7, buttonWidth, 20)
        .build());

        // Done Button
        this.addRenderableWidget(Button.builder(
            Component.literal("Done"),
            button -> this.minecraft.setScreen(parent)
        )
        .bounds(centerX - 100, this.height - 28, 200, 20)
        .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Subtitle
        graphics.drawCenteredString(this.font,
            Component.literal("§7Select a category to configure"),
            this.width / 2, 40, 0xFFFFFF);

        // Info
        graphics.drawCenteredString(this.font,
            Component.literal("§8All changes are saved immediately"),
            this.width / 2, this.height - 45, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
