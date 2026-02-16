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
        int leftCol = centerX - 205;
        int rightCol = centerX + 5;
        int buttonWidth = 200;
        int startY = 50;
        int spacing = 23;

        // LEFT COLUMN

        // CLIENT SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§b⚙ Client Settings"),
            button -> this.minecraft.setScreen(new ClientConfigScreen(this))
        )
        .bounds(leftCol, startY, buttonWidth, 20)
        .build());

        // ECONOMY SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§e$ Economy Settings"),
            button -> this.minecraft.setScreen(new EconomyConfigScreen(this))
        )
        .bounds(leftCol, startY + spacing, buttonWidth, 20)
        .build());

        // PLOT SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§a▣ Plot Settings"),
            button -> this.minecraft.setScreen(new PlotConfigScreen(this))
        )
        .bounds(leftCol, startY + spacing * 2, buttonWidth, 20)
        .build());

        // POLICE SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§c★ Police Settings"),
            button -> this.minecraft.setScreen(new PoliceConfigScreen(this))
        )
        .bounds(leftCol, startY + spacing * 3, buttonWidth, 20)
        .build());

        // NPC SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§d☺ NPC Settings"),
            button -> this.minecraft.setScreen(new NPCConfigScreen(this))
        )
        .bounds(leftCol, startY + spacing * 4, buttonWidth, 20)
        .build());

        // WAREHOUSE SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§6■ Warehouse Settings"),
            button -> this.minecraft.setScreen(new WarehouseConfigScreen(this))
        )
        .bounds(leftCol, startY + spacing * 5, buttonWidth, 20)
        .build());

        // RIGHT COLUMN

        // DYNAMIC PRICING (UDPS)
        this.addRenderableWidget(Button.builder(
            Component.literal("§9≈ Dynamic Pricing"),
            button -> this.minecraft.setScreen(new DynamicPricingConfigScreen(this))
        )
        .bounds(rightCol, startY, buttonWidth, 20)
        .build());

        // TOBACCO SETTINGS
        this.addRenderableWidget(Button.builder(
            Component.literal("§2⚘ Tobacco Settings"),
            button -> this.minecraft.setScreen(new TobaccoConfigScreen(this))
        )
        .bounds(rightCol, startY + spacing, buttonWidth, 20)
        .build());

        // WERKSTATT/WORKSHOP
        this.addRenderableWidget(Button.builder(
            Component.literal("§6⚒ Werkstatt/Workshop"),
            button -> this.minecraft.setScreen(new WerkstattConfigScreen(this))
        )
        .bounds(rightCol, startY + spacing * 2, buttonWidth, 20)
        .build());

        // STEALING
        this.addRenderableWidget(Button.builder(
            Component.literal("§4⚠ Stealing/Crime"),
            button -> this.minecraft.setScreen(new StealingConfigScreen(this))
        )
        .bounds(rightCol, startY + spacing * 3, buttonWidth, 20)
        .build());

        // ADVANCED ECONOMY
        this.addRenderableWidget(Button.builder(
            Component.literal("§e⚡ Advanced Economy"),
            button -> this.minecraft.setScreen(new AdvancedEconomyConfigScreen(this))
        )
        .bounds(rightCol, startY + spacing * 4, buttonWidth, 20)
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
            Component.literal("§7160+ Config Options - Full Control!"),
            this.width / 2, 35, 0xFFFF55);

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
