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
    private final boolean newWorldDefaults;
    private CategoryList categoryList;
    private String statusMessage = "";

    public ConfigCategoryScreen(Screen parent) {
        this(parent, false);
    }

    /**
     * @param newWorldDefaults true, wenn die Vorlage für NEUE Welten bearbeitet wird
     *                         (über {@link de.rolandsw.schedulemc.config.DefaultConfigEditor},
     *                         erreichbar aus dem Welt-erstellen-Screen).
     */
    public ConfigCategoryScreen(Screen parent, boolean newWorldDefaults) {
        super(Component.literal("ScheduleMC Configuration"));
        this.parent = parent;
        this.newWorldDefaults = newWorldDefaults;
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable category list (leave room for buttons at bottom)
        this.categoryList = new CategoryList(this.minecraft, this.width, this.height, 55, this.height - 104, 25);
        this.addWidget(this.categoryList);

        // Add all categories to the list. Gameplay-Configs gelten PRO SPIELSTAND
        // (SERVER-Config) und sind nur bei geladener Welt lesbar/editierbar -> gated().
        // "Client Settings" ist global und bleibt immer verfügbar.
        categoryList.addCategoryRow(
            "§b⚙ Client Settings", () -> new ClientConfigScreen(this),
            "§e$ Economy Settings", gated(() -> new EconomyConfigScreen(this))
        );
        categoryList.addCategoryRow(
            "§a▣ Plot Settings", gated(() -> new PlotConfigScreen(this)),
            "§c★ Police Settings", gated(() -> new PoliceConfigScreen(this))
        );
        categoryList.addCategoryRow(
            "§d☺ NPC Settings", gated(() -> new NPCConfigScreen(this)),
            "§6■ Warehouse Settings", gated(() -> new WarehouseConfigScreen(this))
        );
        categoryList.addCategoryRow(
            "§9≈ Dynamic Pricing", gated(() -> new DynamicPricingConfigScreen(this)),
            "§2⚘ Tobacco Settings", gated(() -> new TobaccoConfigScreen(this))
        );
        categoryList.addCategoryRow(
            "§6⚒ Workshop/Workshop", gated(() -> new WorkshopConfigScreen(this)),
            "§4⚠ Stealing/Crime", gated(() -> new StealingConfigScreen(this))
        );
        categoryList.addCategoryRow(
            "§e⚡ Advanced Economy", gated(() -> new AdvancedEconomyConfigScreen(this)),
            "§3⚙ Plot Block Restrictions", gated(() -> new PlotBlockRestrictionConfigScreen(this))
        );
        categoryList.addCategoryRow(
            "§b⚡ Utility Consumer Blocks", gated(() -> new UtilityBlockListConfigScreen(this)),
            "§6$ Produkt-Referenzpreise", gated(() -> new EconomyPricesConfigScreen(this))
        );
        categoryList.addCategoryRow(
            "§a⚙ Produktionsblock-Katalog", gated(() -> new ProductionBlockCatalogScreen(this)),
            "§c🔫 Weapon Settings", gated(() -> new WeaponConfigScreen(this))
        );

        // Set the current save's config as the template for NEW worlds (Forge defaultconfigs/)
        this.addRenderableWidget(Button.builder(
            Component.literal("§b★ Set as Default for New Worlds"),
            button -> doSaveAsDefault()
        ).bounds(this.width / 2 - 140, this.height - 76, 280, 20).build());

        // Import / Export row (native file dialog, single portable .zip)
        this.addRenderableWidget(Button.builder(
            Component.literal("§a⬇ Export Config..."),
            button -> doExport()
        ).bounds(this.width / 2 - 205, this.height - 52, 200, 20).build());
        this.addRenderableWidget(Button.builder(
            Component.literal("§e⬆ Load Config File..."),
            button -> doImport()
        ).bounds(this.width / 2 + 5, this.height - 52, 200, 20).build());

        // Done Button (fixed at bottom) - über onClose(), damit die Vorlagen-Bindung gelöst wird
        this.addRenderableWidget(Button.builder(
            Component.literal("Done"),
            button -> this.onClose()
        )
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
        .build());
    }

    /**
     * Pro-Welt-Config nur öffnen, wenn die Werte verfügbar sind: entweder eine Welt ist
     * geladen, oder die Vorlage für neue Welten ist gerade gebunden (DefaultConfigEditor).
     * Sonst Hinweis statt Crash.
     */
    private ScreenSupplier gated(ScreenSupplier real) {
        return () -> {
            boolean available = (this.minecraft != null && this.minecraft.level != null)
                || de.rolandsw.schedulemc.config.DefaultConfigEditor.isActive();
            if (available) {
                return real.create();
            }
            this.statusMessage = "§ePer-world config - load or create a world first to edit these settings.";
            return this;
        };
    }

    private void doSaveAsDefault() {
        try {
            int n = de.rolandsw.schedulemc.config.ConfigTransfer.saveAsNewWorldDefaults();
            if (n < 0) {
                this.statusMessage = "§eLoad or create a world first - this saves the current save's config.";
            } else {
                this.statusMessage = "§a" + n + " config file(s) saved as default for new worlds.";
            }
        } catch (Exception e) {
            this.statusMessage = "§cFailed: " + e.getMessage();
        }
    }

    private void doExport() {
        final String defaultPath = de.rolandsw.schedulemc.config.ConfigTransfer.suggestedExportPath();
        final net.minecraft.client.Minecraft mc = this.minecraft;
        this.statusMessage = "§7Opening file dialog...";
        // Dialog NICHT auf dem Render-/Main-Thread (macOS: Deadlock mit der GLFW-Eventloop).
        runDialogAsync(() -> {
            String chosen = saveFileDialog("Export ScheduleMC config", defaultPath);
            String result;
            if (chosen == null) {
                result = "§7Export cancelled.";
            } else {
                try {
                    java.nio.file.Path target = java.nio.file.Paths.get(
                        chosen.toLowerCase(java.util.Locale.ROOT).endsWith(".zip") ? chosen : chosen + ".zip");
                    int n = de.rolandsw.schedulemc.config.ConfigTransfer.exportZip(target);
                    result = "§a" + n + " config file(s) exported to: " + target;
                } catch (Exception e) {
                    result = "§cExport failed: " + e.getMessage();
                }
            }
            final String r = result;
            mc.execute(() -> this.statusMessage = r);
        });
    }

    private void doImport() {
        final net.minecraft.client.Minecraft mc = this.minecraft;
        this.statusMessage = "§7Opening file dialog...";
        // Dialog NICHT auf dem Render-/Main-Thread (macOS: Deadlock mit der GLFW-Eventloop).
        runDialogAsync(() -> {
            String chosen = openFileDialog("Load ScheduleMC config file");
            String result;
            if (chosen == null) {
                result = "§7Import cancelled.";
            } else {
                try {
                    int n = de.rolandsw.schedulemc.config.ConfigTransfer.importZip(java.nio.file.Paths.get(chosen));
                    result = (n > 0)
                        ? "§a" + n + " config file(s) loaded - changes apply automatically."
                        : "§eNo ScheduleMC config found in that file.";
                } catch (Exception e) {
                    result = "§cImport failed: " + e.getMessage();
                }
            }
            final String r = result;
            mc.execute(() -> this.statusMessage = r);
        });
    }

    /** Führt den nativen Datei-Dialog auf einem Daemon-Hintergrund-Thread aus (macOS-sicher). */
    private static void runDialogAsync(Runnable task) {
        Thread t = new Thread(task, "ScheduleMC-Config-Dialog");
        t.setDaemon(true);
        t.start();
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
            Component.literal(newWorldDefaults
                ? "§b✎ Editing defaults for NEW worlds"
                : "§7160+ Config Options - Full Control!"),
            this.width / 2, 35, 0xFFFF55);

        // Status (button feedback) or default hint
        String info = statusMessage.isEmpty()
            ? "§8Per-world config - settings are saved per save game"
            : statusMessage;
        graphics.drawCenteredString(this.font,
            Component.literal(info),
            this.width / 2, this.height - 94, 0x808080);
    }

    @Override
    public void onClose() {
        // Vorlagen-Bearbeitung beenden: speichern + Bindung lösen, damit ein echter
        // Weltladevorgang die Pro-Welt-Config sauber neu lädt.
        if (newWorldDefaults) {
            de.rolandsw.schedulemc.config.DefaultConfigEditor.end();
        }
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
