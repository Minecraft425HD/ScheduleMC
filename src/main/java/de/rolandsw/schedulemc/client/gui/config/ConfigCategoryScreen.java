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

import javax.annotation.Nullable;
import java.util.List;

/**
 * Main Config Screen - Category Selection (with scrollable list)
 * Allows access to all config categories
 */
@OnlyIn(Dist.CLIENT)
public class ConfigCategoryScreen extends Screen {

    private final Screen parent;
    private CategoryList categoryList;
    private String statusMessage = "";

    public ConfigCategoryScreen(Screen parent) {
        super(Component.literal("ScheduleMC Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable category list (leave room for import/export + done buttons)
        this.categoryList = new CategoryList(this.minecraft, this.width, this.height, 55, this.height - 80, 25);
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
            "§6⚒ Workshop/Workshop", () -> new WorkshopConfigScreen(this),
            "§4⚠ Stealing/Crime", () -> new StealingConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§e⚡ Advanced Economy", () -> new AdvancedEconomyConfigScreen(this),
            "§3⚙ Plot Block Restrictions", () -> new PlotBlockRestrictionConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§b⚡ Utility Consumer Blocks", () -> new UtilityBlockListConfigScreen(this),
            "§6$ Produkt-Referenzpreise", () -> new EconomyPricesConfigScreen(this)
        );
        categoryList.addCategoryRow(
            "§a⚙ Produktionsblock-Katalog", () -> new ProductionBlockCatalogScreen(this),
            "§c🔫 Weapon Settings", () -> new WeaponConfigScreen(this)
        );

        // Import / Export row (native file dialog, single portable .zip)
        this.addRenderableWidget(Button.builder(
            Component.literal("§a⬇ Export Config..."),
            button -> doExport()
        ).bounds(this.width / 2 - 205, this.height - 52, 200, 20).build());
        this.addRenderableWidget(Button.builder(
            Component.literal("§e⬆ Load Config File..."),
            button -> doImport()
        ).bounds(this.width / 2 + 5, this.height - 52, 200, 20).build());

        // Done Button (fixed at bottom)
        this.addRenderableWidget(Button.builder(
            Component.literal("Done"),
            button -> this.minecraft.setScreen(parent)
        )
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
        .build());
    }

    private void doExport() {
        String chosen = saveFileDialog("Export ScheduleMC config",
            de.rolandsw.schedulemc.config.ConfigTransfer.suggestedExportPath());
        if (chosen == null) {
            this.statusMessage = "§7Export cancelled.";
            return;
        }
        try {
            java.nio.file.Path target = java.nio.file.Paths.get(
                chosen.toLowerCase(java.util.Locale.ROOT).endsWith(".zip") ? chosen : chosen + ".zip");
            int n = de.rolandsw.schedulemc.config.ConfigTransfer.exportZip(target);
            this.statusMessage = "§a" + n + " config file(s) exported to: " + target;
        } catch (Exception e) {
            this.statusMessage = "§cExport failed: " + e.getMessage();
        }
    }

    private void doImport() {
        String chosen = openFileDialog("Load ScheduleMC config file");
        if (chosen == null) {
            this.statusMessage = "§7Import cancelled.";
            return;
        }
        try {
            int n = de.rolandsw.schedulemc.config.ConfigTransfer.importZip(java.nio.file.Paths.get(chosen));
            this.statusMessage = (n > 0)
                ? "§a" + n + " config file(s) loaded - changes apply automatically."
                : "§eNo ScheduleMC config found in that file.";
        } catch (Exception e) {
            this.statusMessage = "§cImport failed: " + e.getMessage();
        }
    }

    /** Native Speichern-Dialog (LWJGL TinyFileDialogs). Liefert den Pfad oder null. */
    @Nullable
    private static String saveFileDialog(String title, String defaultPath) {
        try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
            org.lwjgl.PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.zip"));
            filters.flip();
            return org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(
                title, defaultPath, filters, "ScheduleMC config (*.zip)");
        } catch (Throwable t) {
            return null;
        }
    }

    /** Native Öffnen-Dialog (LWJGL TinyFileDialogs). Liefert den Pfad oder null. */
    @Nullable
    private static String openFileDialog(String title) {
        try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
            org.lwjgl.PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.zip"));
            filters.flip();
            return org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
                title, "", filters, "ScheduleMC config (*.zip)", false);
        } catch (Throwable t) {
            return null;
        }
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

        // Status (import/export feedback) or default hint
        String info = statusMessage.isEmpty()
            ? "§8All changes are saved immediately"
            : statusMessage;
        graphics.drawCenteredString(this.font,
            Component.literal(info),
            this.width / 2, this.height - 66, 0x808080);
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
                return rightButton != null && rightButton.mouseClicked(mouseX, mouseY, button) || false;
            }
        }
    }

    // Functional interface for screen creation
    @FunctionalInterface
    private interface ScreenSupplier {
        Screen create();
    }
}
