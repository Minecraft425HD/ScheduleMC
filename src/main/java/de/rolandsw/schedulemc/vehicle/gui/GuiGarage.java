package de.rolandsw.schedulemc.vehicle.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.Part;
import de.rolandsw.schedulemc.vehicle.net.MessageGaragePayment;
import de.rolandsw.schedulemc.vehicle.util.VehicleUtils;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.math.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiGarage extends ScreenBase<ContainerGarage> {

    private static final ResourceLocation GARAGE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_garage.png");
    private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_vehicle.png");

    private static final int fontColor = 4210752;
    private static final int costColor = 0x00AA00;
    private static final int titleColor = 0xFFFFFF;
    private static final int partColor = 0x555555;
    private static final int barGoodColor = 0x00FF00;
    private static final int barMediumColor = 0xFFFF00;
    private static final int barBadColor = 0xFF0000;
    private static final int barBackgroundColor = 0x333333;

    private Inventory playerInv;
    private EntityGenericVehicle vehicle;
    private Button payButton;
    private Button repairTabButton;
    private Button upgradeTabButton;
    private VehicleUtils.VehicleRenderer vehicleRenderer;

    // Tab system
    private enum Tab { REPAIR, UPGRADE }
    private Tab currentTab = Tab.REPAIR;

    // Repair toggle buttons (acting as checkboxes)
    private Button repairDamageCheckbox;
    private Button chargeBatteryCheckbox;
    private Button refuelCheckbox;
    private Button changeOilCheckbox;

    // Repair selection state
    private boolean repairDamageSelected = true;
    private boolean chargeBatterySelected = true;
    private boolean refuelSelected = true;
    private boolean changeOilSelected = true;

    // Upgrade options
    private Button upgradeEngineButton;
    private Button upgradeTiresButton;
    private Button paintButton;
    private int selectedColor = 0xFFFFFF;

    public GuiGarage(ContainerGarage containerGarage, Inventory playerInv, Component title) {
        super(GARAGE_GUI_TEXTURE, containerGarage, playerInv, title);
        this.playerInv = playerInv;
        this.vehicle = containerGarage.getVehicle();
        this.vehicleRenderer = new VehicleUtils.VehicleRenderer(2.0F); // Slow rotation

        imageWidth = 256;
        imageHeight = 215; // Etwas höher für besseren Platz
    }

    @Override
    protected void init() {
        super.init();

        // Null check for vehicle
        if (vehicle == null) {
            return;
        }

        // Tab buttons at top
        int tabWidth = 80;
        int tabHeight = 20;
        int tabY = topPos + 5;

        repairTabButton = addRenderableWidget(Button.builder(
            Component.literal("Reparatur"),
            button -> switchTab(Tab.REPAIR))
            .bounds(leftPos + 10, tabY, tabWidth, tabHeight)
            .build()
        );

        upgradeTabButton = addRenderableWidget(Button.builder(
            Component.literal("Upgrades"),
            button -> switchTab(Tab.UPGRADE))
            .bounds(leftPos + 95, tabY, tabWidth, tabHeight)
            .build()
        );

        // Initialize repair checkboxes
        initRepairCheckboxes();

        // Initialize upgrade buttons
        initUpgradeButtons();

        // Set initial widget visibility based on current tab
        updateWidgetVisibility();

        // Add "Bezahlen/Kaufen" button at bottom
        updatePayButton();
    }

    private void initRepairCheckboxes() {
        int checkX = leftPos + 140;
        int checkY = topPos + 110; // Angepasst an renderRepairTab
        int lineHeight = 22; // Mehr Abstand zwischen Zeilen
        int checkboxWidth = 12;
        int checkboxHeight = 12;

        // Initialize selection state
        repairDamageSelected = getDamagePercent() > 0;
        chargeBatterySelected = getBatteryPercent() < 50;
        refuelSelected = getFuelPercent() < 100;
        changeOilSelected = true;

        repairDamageCheckbox = addRenderableWidget(Button.builder(
            Component.literal(repairDamageSelected ? "☑" : "☐"),
            button -> {
                repairDamageSelected = !repairDamageSelected;
                button.setMessage(Component.literal(repairDamageSelected ? "☑" : "☐"));
                updatePayButton();
            })
            .bounds(checkX, checkY, checkboxWidth, checkboxHeight)
            .build()
        );

        chargeBatteryCheckbox = addRenderableWidget(Button.builder(
            Component.literal(chargeBatterySelected ? "☑" : "☐"),
            button -> {
                chargeBatterySelected = !chargeBatterySelected;
                button.setMessage(Component.literal(chargeBatterySelected ? "☑" : "☐"));
                updatePayButton();
            })
            .bounds(checkX, checkY + lineHeight, checkboxWidth, checkboxHeight)
            .build()
        );

        refuelCheckbox = addRenderableWidget(Button.builder(
            Component.literal(refuelSelected ? "☑" : "☐"),
            button -> {
                refuelSelected = !refuelSelected;
                button.setMessage(Component.literal(refuelSelected ? "☑" : "☐"));
                updatePayButton();
            })
            .bounds(checkX, checkY + lineHeight * 2, checkboxWidth, checkboxHeight)
            .build()
        );

        changeOilCheckbox = addRenderableWidget(Button.builder(
            Component.literal(changeOilSelected ? "☑" : "☐"),
            button -> {
                changeOilSelected = !changeOilSelected;
                button.setMessage(Component.literal(changeOilSelected ? "☑" : "☐"));
                updatePayButton();
            })
            .bounds(checkX, checkY + lineHeight * 3, checkboxWidth, checkboxHeight)
            .build()
        );
    }

    private void initUpgradeButtons() {
        int btnX = leftPos + 140;
        int btnY = topPos + 71; // Nach dem Text für Motor (55 + 8 + 8 = 71)
        int btnWidth = 100;
        int btnHeight = 18;
        int lineHeight = 40; // Entspricht dem Abstand im renderUpgradeTab

        upgradeEngineButton = addRenderableWidget(Button.builder(
            Component.literal("Motor: 250€"),
            button -> {
                // TODO: Upgrade engine logic
            })
            .bounds(btnX, btnY, btnWidth, btnHeight)
            .build()
        );

        upgradeTiresButton = addRenderableWidget(Button.builder(
            Component.literal("Reifen: 150€"),
            button -> {
                // TODO: Upgrade tires logic
            })
            .bounds(btnX, btnY + lineHeight, btnWidth, btnHeight)
            .build()
        );

        paintButton = addRenderableWidget(Button.builder(
            Component.literal("Lackierung: 50€"),
            button -> {
                // Cycle through colors
                selectedColor = getNextColor(selectedColor);
            })
            .bounds(btnX, btnY + lineHeight * 2, btnWidth, btnHeight)
            .build()
        );
    }

    private void updatePayButton() {
        if (payButton != null) {
            removeWidget(payButton);
        }

        int buttonWidth = 140;
        int buttonHeight = 20;
        int buttonX = leftPos + (imageWidth - buttonWidth) / 2;
        int buttonY = topPos + imageHeight - 30; // Adjusted for smaller GUI

        String buttonText = currentTab == Tab.REPAIR ?
            "Bezahlen: " + String.format("%.2f€", calculateSelectedCost()) :
            "Kaufen: " + String.format("%.2f€", calculateUpgradeCost());

        payButton = addRenderableWidget(Button.builder(
            Component.literal(buttonText),
            button -> {
                if (vehicle != null && minecraft != null && minecraft.player != null) {
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageGaragePayment(
                        minecraft.player.getUUID(),
                        vehicle.getUUID()
                    ));
                }
            })
            .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
            .build()
        );
    }

    private void switchTab(Tab newTab) {
        currentTab = newTab;
        updateWidgetVisibility();
        updatePayButton();
    }

    private void updateWidgetVisibility() {
        // Show/hide widgets based on current tab
        boolean isRepair = currentTab == Tab.REPAIR;

        if (repairDamageCheckbox != null) repairDamageCheckbox.visible = isRepair;
        if (chargeBatteryCheckbox != null) chargeBatteryCheckbox.visible = isRepair;
        if (refuelCheckbox != null) refuelCheckbox.visible = isRepair;
        if (changeOilCheckbox != null) changeOilCheckbox.visible = isRepair;

        if (upgradeEngineButton != null) upgradeEngineButton.visible = !isRepair;
        if (upgradeTiresButton != null) upgradeTiresButton.visible = !isRepair;
        if (paintButton != null) paintButton.visible = !isRepair;
    }

    private int getNextColor(int currentColor) {
        int[] colors = {0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF, 0x000000};
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == currentColor) {
                return colors[(i + 1) % colors.length];
            }
        }
        return colors[0];
    }

    @Override
    public void onClose() {
        // Prevent closing via ESC/E - only allow closing via payment button
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block ESC key (keyCode 256) and inventory key
        if (keyCode == 256 || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw a simple custom background (no texture needed)
        // Outer border - dark gray
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF404040);

        // Main background - light gray
        guiGraphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFFC6C6C6);

        // Top bar for tabs - darker
        guiGraphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + 28, 0xFF8B8B8B);

        // Right panel separator
        guiGraphics.fill(leftPos + 135, topPos + 30, leftPos + 137, topPos + imageHeight - 2, 0xFF666666);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Don't call super to avoid default title rendering

        if (vehicle == null || vehicle.isRemoved()) {
            guiGraphics.drawString(font, "Kein Fahrzeug!", 10, 10, 0xFF0000, false);
            return;
        }

        // Title - below tabs
        String title = "== FAHRZEUG-WERKSTATT ==";
        int titleX = (imageWidth - font.width(title)) / 2;
        guiGraphics.drawString(font, title, titleX, 30, titleColor, true);

        // Left side: Vehicle visualization
        renderVehicleDisplay(guiGraphics);

        // Right side: Render based on current tab
        if (currentTab == Tab.REPAIR) {
            renderRepairTab(guiGraphics, mouseX, mouseY);
        } else {
            renderUpgradeTab(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderVehicleDisplay(GuiGraphics guiGraphics) {
        int vehicleX = 64;
        int vehicleY = 80;
        int vehicleScale = 20;

        // Update and render rotating vehicle
        vehicleRenderer.tick();

        // Draw vehicle label
        guiGraphics.drawString(font, "Fahrzeug:", 10, 20, fontColor, false);
        guiGraphics.drawString(font, vehicle.getDisplayName().getString(), 10, 30, partColor, false);

        // Render 3D vehicle
        if (vehicle != null) {
            vehicleRenderer.render(guiGraphics, vehicle, vehicleX, vehicleY, vehicleScale);
        }

        // Vehicle status below 3D model
        int statusY = 120;
        guiGraphics.drawString(font, "Status:", 10, statusY, fontColor, false);
        guiGraphics.drawString(font, "Gesperrt", 10, statusY + 10, 0xFFAA00, false);
    }

    private void renderRepairTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int rightX = 140;
        int startY = 40;

        // Parts status with colored bars
        guiGraphics.drawString(font, "=== TEILEZUSTAND ===", rightX, startY, fontColor, false);

        int barY = startY + 10;
        renderPartStatusBar(guiGraphics, rightX, barY, "Motor", 100 - getDamagePercent(), mouseX, mouseY);
        renderPartStatusBar(guiGraphics, rightX, barY + 14, "Reifen", 85.0f, mouseX, mouseY);
        renderPartStatusBar(guiGraphics, rightX, barY + 28, "Karosserie", 100 - getDamagePercent() * 0.5f, mouseX, mouseY);

        // Repair options with checkboxes (already rendered by widgets)
        int checkY = 110;
        guiGraphics.drawString(font, "=== SERVICES ===", rightX, checkY - 8, fontColor, false);

        // Show labels and prices next to checkboxes
        int labelX = rightX + 15;
        int priceX = rightX + 80;
        int lineSpacing = 22; // Entspricht lineHeight in initRepairCheckboxes

        // Reparatur
        guiGraphics.drawString(font, "Reparatur", labelX, checkY, partColor, false);
        if (getDamagePercent() > 0) {
            guiGraphics.drawString(font, String.format("%.2f€", getDamagePercent() * 2.0), priceX, checkY, costColor, false);
        }

        // Batterie
        guiGraphics.drawString(font, "Batterie", labelX, checkY + lineSpacing, partColor, false);
        if (getBatteryPercent() < 50) {
            guiGraphics.drawString(font, String.format("%.2f€", (50 - getBatteryPercent()) * 0.5), priceX, checkY + lineSpacing, costColor, false);
        }

        // Tanken
        guiGraphics.drawString(font, "Tanken", labelX, checkY + lineSpacing * 2, partColor, false);
        if (getFuelPercent() < 100) {
            guiGraphics.drawString(font, String.format("%.2f€", (100 - getFuelPercent()) * 0.3), priceX, checkY + lineSpacing * 2, costColor, false);
        }

        // Ölwechsel
        guiGraphics.drawString(font, "Ölwechsel", labelX, checkY + lineSpacing * 3, partColor, false);
        guiGraphics.drawString(font, "15.00€", priceX, checkY + lineSpacing * 3, costColor, false);
    }

    private void renderUpgradeTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int rightX = 140;
        int startY = 40;

        guiGraphics.drawString(font, "=== UPGRADES ===", rightX, startY, fontColor, false);

        // Motor Upgrade - Text über Button
        int upgradeY = 55;
        guiGraphics.drawString(font, "120 -> 150 km/h", rightX + 2, upgradeY, fontColor, false);
        guiGraphics.drawString(font, "+25% Leistung", rightX + 2, upgradeY + 8, 0x00FF00, false);

        // Reifen Upgrade - Text über Button
        upgradeY += 40;
        guiGraphics.drawString(font, "Handling +30%", rightX + 2, upgradeY, fontColor, false);
        guiGraphics.drawString(font, "Grip +20%", rightX + 2, upgradeY + 8, 0x00FF00, false);

        // Lackierung - Text über Button
        upgradeY += 40;
        int colorX = rightX + 2;
        int colorY = upgradeY;
        guiGraphics.fill(colorX, colorY, colorX + 40, colorY + 8, 0xFF000000 | selectedColor);
        guiGraphics.drawString(font, "Farbe", rightX + 45, upgradeY, fontColor, false);
    }

    private void renderPartStatusBar(GuiGraphics guiGraphics, int x, int y, String partName, float percent, int mouseX, int mouseY) {
        // Draw part name
        guiGraphics.drawString(font, partName + ":", x, y, partColor, false);

        // Draw status bar
        int barX = x;
        int barY = y + 10;
        int barWidth = 100;
        int barHeight = 6;

        // Background
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF000000 | barBackgroundColor);

        // Foreground based on percentage
        int fillWidth = (int) (barWidth * (percent / 100.0f));
        int barColor = getBarColor(percent);
        guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFF000000 | barColor);

        // Percentage text
        guiGraphics.drawString(font, String.format("%.0f%%", percent), barX + barWidth + 5, y, fontColor, false);

        // Tooltip on hover
        if (mouseX >= leftPos + barX && mouseX <= leftPos + barX + barWidth &&
            mouseY >= topPos + barY && mouseY <= topPos + barY + barHeight) {

            int tooltipX = mouseX - leftPos;
            int tooltipY = mouseY - topPos - 15;
            String tooltip = String.format("%s: %.1f%% - ", partName, percent);

            if (percent > 75) {
                tooltip += "Gut";
            } else if (percent > 40) {
                tooltip += "Mittel";
            } else {
                tooltip += "Schlecht";
            }

            guiGraphics.drawString(font, tooltip, tooltipX, tooltipY, 0xFFFFFF, true);
        }
    }

    private int getBarColor(float percent) {
        if (percent > 75) {
            return barGoodColor;
        } else if (percent > 40) {
            return barMediumColor;
        } else {
            return barBadColor;
        }
    }

    private double calculateSelectedCost() {
        if (vehicle == null) return 0.0;

        double cost = 10.0; // Base inspection

        // Add costs only for selected checkboxes
        if (repairDamageSelected) {
            float damage = getDamagePercent();
            if (damage > 0) {
                cost += damage * 2.0;
            }
        }

        if (chargeBatterySelected) {
            float batteryPercent = getBatteryPercent();
            if (batteryPercent < 50) {
                cost += (50 - batteryPercent) * 0.5;
            }
        }

        if (refuelSelected) {
            float fuelPercent = getFuelPercent();
            if (fuelPercent < 100) {
                cost += (100 - fuelPercent) * 0.3;
            }
        }

        if (changeOilSelected) {
            cost += 15.0;
        }

        return cost;
    }

    private double calculateUpgradeCost() {
        // Calculate total cost of selected upgrades
        // For now, return 0 as upgrades need to be selected individually
        return 0.0;
    }

    // === Calculation Methods ===

    public float getFuelPercent() {
        if (vehicle == null) return 0;
        float fuelPerc = ((float) vehicle.getFuelComponent().getFuelAmount()) / ((float) vehicle.getMaxFuel()) * 100F;
        return MathUtils.round(fuelPerc, 2);
    }

    public float getBatteryPercent() {
        if (vehicle == null) return 0;
        return MathUtils.round(vehicle.getBatteryComponent().getBatteryPercentage() * 100F, 1);
    }

    public float getDamagePercent() {
        if (vehicle == null) return 0;
        float dmg = vehicle.getDamageComponent().getDamage();
        dmg = Math.min(dmg, 100);
        return MathUtils.round(dmg, 1);
    }

    public float getTemperatureCelsius() {
        if (vehicle == null) return 0;
        return MathUtils.round(vehicle.getDamageComponent().getTemperature(), 1);
    }
}
