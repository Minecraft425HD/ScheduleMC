package de.rolandsw.schedulemc.client.gui.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Main Config Screen - Category Selection (with scrollable list)
 * Allows access to all config categories
 */
@OnlyIn(Dist.CLIENT)
public class ConfigCategoryScreen extends Screen {

    private final Screen parent;
    private CategoryList categoryList;

    public ConfigCategoryScreen(Screen parent) {
        super(Component.literal("ScheduleMC Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable category list (from y=55 to y=height-55)
        this.categoryList = new CategoryList(this.minecraft, this.width, this.height, 55, this.height - 55, 25);
        this.addWidget(this.categoryList);

        // Add all categories to the list
        categoryList.addCategoryRow(
            "§b⚙ Client Settings", () -> new ClientConfigScreen(this),
            "§e$ Economy Settings", () -> new EconomyConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§a▣ Plot Settings", () -> new PlotConfigScreen(this),
            "§c★ Police Settings", () -> new PoliceConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§d☺ NPC Settings", () -> new NPCConfigScreen(this),
            "§6■ Warehouse Settings", () -> new WarehouseConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§9≈ Dynamic Pricing", () -> new DynamicPricingConfigScreen(this),
            "§2⚘ Tobacco Settings", () -> new TobaccoConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§6⚒ Werkstatt/Workshop", () -> new WerkstattConfigScreen(this),
            "§4⚠ Stealing/Crime", () -> new StealingConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§e⚡ Advanced Economy", () -> new AdvancedEconomyConfigScreen(this),
            null, null  // Only left button in this row
        );

        // Done Button (fixed at bottom)
        this.addRenderableWidget(Button.builder(
            Component.literal("Done"),
            button -> this.minecraft.setScreen(parent)
        )
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
        .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Render the scrollable list
        this.categoryList.render(graphics, mouseX, mouseY, partialTick);

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

    // === SCROLLABLE CATEGORY LIST ===

    private class CategoryList extends ContainerObjectSelectionList<CategoryList.CategoryEntry> {

        public CategoryList(net.minecraft.client.Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
            super(mc, width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return 420; // Width for two columns of buttons
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 220; // Position scrollbar on the right
        }

        public void addCategoryRow(String leftLabel, ScreenSupplier leftScreen,
                                   String rightLabel, ScreenSupplier rightScreen) {
            this.addEntry(new CategoryEntry(leftLabel, leftScreen, rightLabel, rightScreen));
        }

        // === CATEGORY ENTRY (Row with 2 buttons) ===

        public class CategoryEntry extends ContainerObjectSelectionList.Entry<CategoryEntry> {
            private final Button leftButton;
            private final Button rightButton;

            public CategoryEntry(String leftLabel, ScreenSupplier leftScreen,
                               String rightLabel, ScreenSupplier rightScreen) {
                // Left button
                if (leftLabel != null && leftScreen != null) {
                    this.leftButton = Button.builder(
                        Component.literal(leftLabel),
                        btn -> minecraft.setScreen(leftScreen.create())
                    ).bounds(0, 0, 200, 20).build();
                } else {
                    this.leftButton = null;
                }

                // Right button
                if (rightLabel != null && rightScreen != null) {
                    this.rightButton = Button.builder(
                        Component.literal(rightLabel),
                        btn -> minecraft.setScreen(rightScreen.create())
                    ).bounds(0, 0, 200, 20).build();
                } else {
                    this.rightButton = null;
                }
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                             int mouseX, int mouseY, boolean hovering, float partialTick) {
                int centerX = ConfigCategoryScreen.this.width / 2;
                int leftCol = centerX - 205;
                int rightCol = centerX + 5;

                // Render left button
                if (leftButton != null) {
                    leftButton.setY(top);
                    leftButton.setX(leftCol);
                    leftButton.render(graphics, mouseX, mouseY, partialTick);
                }

                // Render right button
                if (rightButton != null) {
                    rightButton.setY(top);
                    rightButton.setX(rightCol);
                    rightButton.render(graphics, mouseX, mouseY, partialTick);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                if (leftButton != null && rightButton != null) {
                    return List.of(leftButton, rightButton);
                } else if (leftButton != null) {
                    return List.of(leftButton);
                } else if (rightButton != null) {
                    return List.of(rightButton);
                }
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                if (leftButton != null && rightButton != null) {
                    return List.of(leftButton, rightButton);
                } else if (leftButton != null) {
                    return List.of(leftButton);
                } else if (rightButton != null) {
                    return List.of(rightButton);
                }
                return List.of();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (leftButton != null && leftButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                if (rightButton != null && rightButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                return false;
            }
        }
    }

    // Functional interface for screen creation
    @FunctionalInterface
    private interface ScreenSupplier {
        Screen create();
    }
}
