package de.rolandsw.schedulemc.vehicle.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartBody;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartBumper;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartChromeBumper;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartEngine;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartSportBumper;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartTank;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartTireBase;
import de.rolandsw.schedulemc.vehicle.items.IVehiclePart;
import de.rolandsw.schedulemc.vehicle.net.MessageGaragePayment;
import de.rolandsw.schedulemc.vehicle.net.MessageGarageUpgrade;
import de.rolandsw.schedulemc.vehicle.net.UpgradeType;
import de.rolandsw.schedulemc.vehicle.util.VehicleUtils;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.math.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GuiGarage extends ScreenBase<ContainerGarage> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation GARAGE_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/gui_garage.png");
    private static final ResourceLocation FALLBACK_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/gui_vehicle.png");

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
    private Button changeOilCheckbox;

    // Repair selection state
    private boolean repairDamageSelected = true;
    private boolean chargeBatterySelected = true;
    private boolean changeOilSelected = true;

    // Upgrade options
    private Button upgradeMotorButton;
    private Button upgradeTankButton;
    private Button upgradeTireButton;
    private Button upgradeFenderButton;
    private List<Button> paintColorButtons = new ArrayList<>();
    private int selectedPaintColor = 0; // 0-4: white, black, red, blue, yellow

    /**
     * Helper-Methode: Widgets asynchron nach Delay neu aufbauen
     * CODE-QUALITÄT: Vermeidet Code-Duplikation & proper Exception-Handling
     */
    private void refreshWidgetsAsync(int delayMs) {
        minecraft.execute(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Widget refresh interrupted", e);
            }
            this.rebuildWidgets();
        });
    }

    public GuiGarage(ContainerGarage containerGarage, Inventory playerInv, Component title) {
        super(GARAGE_GUI_TEXTURE, containerGarage, playerInv, title);
        this.playerInv = playerInv;
        this.vehicle = containerGarage.getVehicle();
        this.vehicleRenderer = new VehicleUtils.VehicleRenderer(1.0F); // Slower rotation (50% speed)

        imageWidth = 290; // Breiter für besseren Platz
        imageHeight = 250; // Höher für mehr Platz zwischen Elementen
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
        int checkX = leftPos + 145; // Angepasst an neue rightX Position
        int checkY = topPos + 115; // Angepasst an renderRepairTab
        int lineHeight = 24; // Mehr Abstand zwischen Zeilen
        int checkboxWidth = 12;
        int checkboxHeight = 12;

        // Initialize selection state
        repairDamageSelected = getDamagePercent() > 0;
        chargeBatterySelected = getBatteryPercent() < 50;
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

        changeOilCheckbox = addRenderableWidget(Button.builder(
            Component.literal(changeOilSelected ? "☑" : "☐"),
            button -> {
                changeOilSelected = !changeOilSelected;
                button.setMessage(Component.literal(changeOilSelected ? "☑" : "☐"));
                updatePayButton();
            })
            .bounds(checkX, checkY + lineHeight * 2, checkboxWidth, checkboxHeight)
            .build()
        );
    }

    private void initUpgradeButtons() {
        int btnX = leftPos + 145;
        int btnY = topPos + 60;
        int btnWidth = 125;
        int btnHeight = 18;
        int spacing = 30;

        // Initialize selected paint color from vehicle
        if (vehicle != null) {
            selectedPaintColor = vehicle.getPaintColor();
        }

        // Motor upgrade button
        int motorLevel = getCurrentMotorLevel();
        if (motorLevel < 3) {
            double motorCost = motorLevel == 1 ?
                ModConfigHandler.COMMON.GARAGE_MOTOR_UPGRADE_COST_LVL2.get() :
                ModConfigHandler.COMMON.GARAGE_MOTOR_UPGRADE_COST_LVL3.get();
            upgradeMotorButton = addRenderableWidget(Button.builder(
                Component.literal(String.format("Motor Lvl %d: %.0f€", motorLevel + 1, motorCost)),
                button -> {
                    sendUpgrade(UpgradeType.MOTOR, motorLevel + 1);
                    refreshWidgetsAsync(100);
                })
                .bounds(btnX, btnY, btnWidth, btnHeight)
                .build()
            );
        }

        // Tank upgrade button
        btnY += spacing;
        int tankLevel = getCurrentTankLevel();
        if (tankLevel < 3) {
            double tankCost = tankLevel == 1 ?
                ModConfigHandler.COMMON.GARAGE_TANK_UPGRADE_COST_LVL2.get() :
                ModConfigHandler.COMMON.GARAGE_TANK_UPGRADE_COST_LVL3.get();
            upgradeTankButton = addRenderableWidget(Button.builder(
                Component.literal(String.format("Tank Lvl %d: %.0f€", tankLevel + 1, tankCost)),
                button -> {
                    sendUpgrade(UpgradeType.TANK, tankLevel + 1);
                    refreshWidgetsAsync(100);
                })
                .bounds(btnX, btnY, btnWidth, btnHeight)
                .build()
            );
        }

        // Tire upgrade button
        btnY += spacing;
        int currentTire = getCurrentTireIndex();
        if (currentTire < 2) {
            upgradeTireButton = addRenderableWidget(Button.builder(
                Component.literal(String.format("Reifen Lvl %d: %.0f€", currentTire + 2,
                    ModConfigHandler.COMMON.GARAGE_TIRE_UPGRADE_COST.get())),
                button -> {
                    sendUpgrade(UpgradeType.TIRE, currentTire + 1);
                    refreshWidgetsAsync(100);
                })
                .bounds(btnX, btnY, btnWidth, btnHeight)
                .build()
            );
        }

        // Fender upgrade button
        btnY += spacing;
        int fenderLevel = getCurrentFenderLevel();
        if (fenderLevel < 3) {
            double fenderCost = fenderLevel == 1 ?
                ModConfigHandler.COMMON.GARAGE_FENDER_UPGRADE_COST_LVL2.get() :
                ModConfigHandler.COMMON.GARAGE_FENDER_UPGRADE_COST_LVL3.get();
            upgradeFenderButton = addRenderableWidget(Button.builder(
                Component.literal(String.format("Fender Lvl %d: %.0f€", fenderLevel + 1, fenderCost)),
                button -> {
                    sendUpgrade(UpgradeType.FENDER, fenderLevel + 1);
                    refreshWidgetsAsync(100);
                })
                .bounds(btnX, btnY, btnWidth, btnHeight)
                .build()
            );
        }

        // Paint color buttons (5 colors in a row) - WEITER NACH UNTEN verschoben
        btnY += spacing + 15; // Extra Abstand für Lackierung
        int colorBtnSize = 20;
        int colorSpacing = 25;
        int colorStartX = btnX + 5;

        String[] colorNames = {"Weiß", "Schwarz", "Rot", "Blau", "Gelb"};
        int[] colorHex = {0xFFFFFF, 0x000000, 0xFF0000, 0x0000FF, 0xFFFF00};

        for (int i = 0; i < 5; i++) {
            final int colorIndex = i;
            Button colorBtn = addRenderableWidget(Button.builder(
                Component.literal(""),
                button -> {
                    selectedPaintColor = colorIndex;
                    sendUpgrade(UpgradeType.PAINT, colorIndex);
                })
                .bounds(colorStartX + i * colorSpacing, btnY, colorBtnSize, colorBtnSize)
                .build()
            );
            paintColorButtons.add(colorBtn);
        }
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
                        vehicle.getUUID(),
                        repairDamageSelected,
                        chargeBatterySelected,
                        changeOilSelected
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

        // Repair tab widgets
        if (repairDamageCheckbox != null) repairDamageCheckbox.visible = isRepair;
        if (chargeBatteryCheckbox != null) chargeBatteryCheckbox.visible = isRepair;
        if (changeOilCheckbox != null) changeOilCheckbox.visible = isRepair;

        // Upgrade tab widgets
        if (upgradeMotorButton != null) upgradeMotorButton.visible = !isRepair;
        if (upgradeTankButton != null) upgradeTankButton.visible = !isRepair;
        if (upgradeTireButton != null) upgradeTireButton.visible = !isRepair;
        if (upgradeFenderButton != null) upgradeFenderButton.visible = !isRepair;

        for (Button colorBtn : paintColorButtons) {
            colorBtn.visible = !isRepair;
        }
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
        int rightX = 145; // Etwas mehr nach rechts
        int startY = 42;

        // Parts status with colored bars
        guiGraphics.drawString(font, "=== TEILEZUSTAND ===", rightX, startY, fontColor, false);

        int barY = startY + 12;
        int barSpacing = 16; // Mehr Abstand zwischen Balken
        renderPartStatusBar(guiGraphics, rightX, barY, "Motor", 100 - getDamagePercent(), mouseX, mouseY);
        renderPartStatusBar(guiGraphics, rightX, barY + barSpacing, "Reifen", 100.0f, mouseX, mouseY);
        renderPartStatusBar(guiGraphics, rightX, barY + barSpacing * 2, "Karosserie", 100 - getDamagePercent() * 0.5f, mouseX, mouseY);

        // Repair options with checkboxes (already rendered by widgets)
        int checkY = 115; // Mehr Abstand nach unten
        guiGraphics.drawString(font, "=== SERVICES ===", rightX, checkY - 10, fontColor, false);

        // Show labels and prices next to checkboxes
        int labelX = rightX + 16;
        int priceX = rightX + 95; // Mehr Platz für Preise
        int lineSpacing = 24; // Mehr Abstand zwischen Zeilen

        // Reparatur
        guiGraphics.drawString(font, "Reparatur", labelX, checkY, partColor, false);
        if (getDamagePercent() > 0) {
            double repairCost = getDamagePercent() * ModConfigHandler.COMMON.GARAGE_REPAIR_COST_PER_PERCENT.get();
            guiGraphics.drawString(font, String.format("%.2f€", repairCost), priceX, checkY, costColor, false);
        }

        // Batterie
        guiGraphics.drawString(font, "Batterie", labelX, checkY + lineSpacing, partColor, false);
        if (getBatteryPercent() < 50) {
            double batteryCost = (50 - getBatteryPercent()) * ModConfigHandler.COMMON.GARAGE_BATTERY_COST_PER_PERCENT.get();
            guiGraphics.drawString(font, String.format("%.2f€", batteryCost), priceX, checkY + lineSpacing, costColor, false);
        }

        // Ölwechsel
        guiGraphics.drawString(font, "Ölwechsel", labelX, checkY + lineSpacing * 2, partColor, false);
        guiGraphics.drawString(font, String.format("%.2f€", ModConfigHandler.COMMON.GARAGE_OIL_CHANGE_COST.get()), priceX, checkY + lineSpacing * 2, costColor, false);
    }

    private void renderUpgradeTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int rightX = 145;
        int startY = 42;

        guiGraphics.drawString(font, "=== UPGRADES ===", rightX, startY, fontColor, false);

        int labelY = 48;
        int spacing = 30;

        // Motor
        int motorLevel = getCurrentMotorLevel();
        if (motorLevel < 3) {
            guiGraphics.drawString(font, "Motor Lvl " + motorLevel, rightX + 2, labelY, partColor, false);
        } else {
            guiGraphics.drawString(font, "Motor: MAX", rightX + 2, labelY, 0x00FF00, false);
        }

        // Tank
        labelY += spacing;
        int tankLevel = getCurrentTankLevel();
        if (tankLevel < 3) {
            guiGraphics.drawString(font, "Tank Lvl " + tankLevel, rightX + 2, labelY, partColor, false);
        } else {
            guiGraphics.drawString(font, "Tank: MAX", rightX + 2, labelY, 0x00FF00, false);
        }

        // Tire
        labelY += spacing;
        int tireIdx = getCurrentTireIndex();
        if (tireIdx < 2) {
            guiGraphics.drawString(font, "Reifen Lvl " + (tireIdx + 1), rightX + 2, labelY, partColor, false);
        } else {
            guiGraphics.drawString(font, "Reifen: MAX", rightX + 2, labelY, 0x00FF00, false);
        }

        // Fender
        labelY += spacing;
        int fenderLevel = getCurrentFenderLevel();
        if (fenderLevel < 3) {
            guiGraphics.drawString(font, "Fender Lvl " + fenderLevel, rightX + 2, labelY, partColor, false);
        } else {
            guiGraphics.drawString(font, "Fender: MAX", rightX + 2, labelY, 0x00FF00, false);
        }

        // Paint colors - render colored squares for the buttons - WEITER NACH UNTEN
        labelY += spacing + 17; // Extra Abstand für Lackierung (spacing + 15 + 2)
        guiGraphics.drawString(font, "Lackierung:", rightX, labelY - 12, fontColor, false);

        int[] colorHex = {0xFFFFFF, 0x000000, 0xFF0000, 0x0000FF, 0xFFFF00};
        int colorBtnSize = 20;
        int colorSpacing = 25;
        int colorStartX = rightX + 5;

        for (int i = 0; i < paintColorButtons.size() && i < colorHex.length; i++) {
            Button btn = paintColorButtons.get(i);
            if (btn.visible) {
                int btnX = colorStartX + i * colorSpacing;
                int btnY = labelY;

                // Draw colored square
                guiGraphics.fill(btnX, btnY, btnX + colorBtnSize, btnY + colorBtnSize, 0xFF000000 | colorHex[i]);

                // Draw border
                if (selectedPaintColor == i) {
                    // Selected color - thick border
                    guiGraphics.fill(btnX - 2, btnY - 2, btnX + colorBtnSize + 2, btnY, 0xFFFFFFFF);
                    guiGraphics.fill(btnX - 2, btnY + colorBtnSize, btnX + colorBtnSize + 2, btnY + colorBtnSize + 2, 0xFFFFFFFF);
                    guiGraphics.fill(btnX - 2, btnY, btnX, btnY + colorBtnSize, 0xFFFFFFFF);
                    guiGraphics.fill(btnX + colorBtnSize, btnY, btnX + colorBtnSize + 2, btnY + colorBtnSize, 0xFFFFFFFF);
                }
            }
        }
    }

    private void renderPartStatusBar(GuiGraphics guiGraphics, int x, int y, String partName, float percent, int mouseX, int mouseY) {
        // Draw part name
        guiGraphics.drawString(font, partName + ":", x, y, partColor, false);

        // Draw status bar
        int barX = x;
        int barY = y + 10;
        int barWidth = 110; // Etwas breiter
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

        // Base inspection fee (always charged)
        double cost = ModConfigHandler.COMMON.GARAGE_BASE_INSPECTION_FEE.get();

        // Add costs only for selected checkboxes
        if (repairDamageSelected) {
            float damage = getDamagePercent();
            if (damage > 0) {
                cost += damage * ModConfigHandler.COMMON.GARAGE_REPAIR_COST_PER_PERCENT.get();
            }
        }

        if (chargeBatterySelected) {
            float batteryPercent = getBatteryPercent();
            if (batteryPercent < 50) {
                cost += (50 - batteryPercent) * ModConfigHandler.COMMON.GARAGE_BATTERY_COST_PER_PERCENT.get();
            }
        }

        if (changeOilSelected) {
            cost += ModConfigHandler.COMMON.GARAGE_OIL_CHANGE_COST.get();
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

    // === Upgrade Helper Methods ===

    private void sendUpgrade(UpgradeType type, int value) {
        if (vehicle != null && minecraft != null && minecraft.player != null) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageGarageUpgrade(
                minecraft.player.getUUID(),
                vehicle.getUUID(),
                type,
                value
            ));
        }
    }

    private int getCurrentMotorLevel() {
        if (vehicle == null) return 1;
        Container partInv = vehicle.getInventoryComponent().getPartInventory();

        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IVehiclePart partItem) {
                Part part = partItem.getPart(stack);
                if (part instanceof PartEngine) {
                    if (part == PartRegistry.INDUSTRIAL_MOTOR) return 3;
                    if (part == PartRegistry.PERFORMANCE_MOTOR) return 2;
                    return 1;
                }
            }
        }
        return 1;
    }

    private int getCurrentTankLevel() {
        if (vehicle == null) return 1;
        Container partInv = vehicle.getInventoryComponent().getPartInventory();

        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IVehiclePart partItem) {
                Part part = partItem.getPart(stack);
                if (part instanceof PartTank) {
                    if (part == PartRegistry.TANK_50L) return 3;
                    if (part == PartRegistry.TANK_30L) return 2;
                    return 1;
                }
            }
        }
        return 1;
    }

    private int getCurrentTireIndex() {
        if (vehicle == null) return 0;
        Container partInv = vehicle.getInventoryComponent().getPartInventory();

        PartBody body = vehicle.getPartByClass(PartBody.class);
        boolean isTruck = body != null && (body.getTranslationKey().contains("transporter")
                                         || body.getTranslationKey().contains("delivery"));

        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IVehiclePart partItem) {
                Part part = partItem.getPart(stack);
                if (part instanceof PartTireBase) {
                    if (isTruck) {
                        if (part == PartRegistry.HEAVY_DUTY_TIRE) return 2;
                        if (part == PartRegistry.ALLTERRAIN_TIRE) return 1;
                        return 0; // OFFROAD
                    } else {
                        if (part == PartRegistry.PREMIUM_TIRE) return 2;
                        if (part == PartRegistry.SPORT_TIRE) return 1;
                        return 0; // STANDARD
                    }
                }
            }
        }
        return 0;
    }

    private int getCurrentFenderLevel() {
        if (vehicle == null) return 1;
        Container partInv = vehicle.getInventoryComponent().getPartInventory();

        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IVehiclePart partItem) {
                Part part = partItem.getPart(stack);
                if (part instanceof PartBumper || part instanceof PartChromeBumper || part instanceof PartSportBumper) {
                    if (part == PartRegistry.FENDER_SPORT) return 3;
                    if (part == PartRegistry.FENDER_CHROME) return 2;
                    return 1;
                }
            }
        }
        return 1;
    }
}
