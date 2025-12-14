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

    private Inventory playerInv;
    private EntityGenericVehicle vehicle;
    private Button payButton;
    private VehicleUtils.VehicleRenderer vehicleRenderer;

    public GuiGarage(ContainerGarage containerGarage, Inventory playerInv, Component title) {
        super(GARAGE_GUI_TEXTURE, containerGarage, playerInv, title);
        this.playerInv = playerInv;
        this.vehicle = containerGarage.getVehicle();
        this.vehicleRenderer = new VehicleUtils.VehicleRenderer(2.0F); // Slow rotation

        imageWidth = 256;
        imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();

        // Null check for vehicle
        if (vehicle == null) {
            return;
        }

        // Add "Bezahlen" button centered at bottom
        int buttonWidth = 140;
        int buttonHeight = 20;
        int buttonX = leftPos + (imageWidth - buttonWidth) / 2;
        int buttonY = topPos + imageHeight - 26;

        payButton = addRenderableWidget(Button.builder(
            Component.literal("Service bezahlen: " + String.format("%.2f€", calculateTotalCost())),
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

        // Try to use custom texture, fall back to vehicle texture if not found
        try {
            guiGraphics.blit(GARAGE_GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        } catch (Exception e) {
            // Fallback: draw a simple background
            guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
            guiGraphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF8B8B8B);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Don't call super to avoid default title rendering

        if (vehicle == null || vehicle.isRemoved()) {
            guiGraphics.drawString(font, "Kein Fahrzeug!", 10, 10, 0xFF0000, false);
            return;
        }

        // Title
        String title = "== FAHRZEUG-WERKSTATT ==";
        int titleX = (imageWidth - font.width(title)) / 2;
        guiGraphics.drawString(font, title, titleX, 8, titleColor, true);

        // Left side: Vehicle visualization and info
        renderVehicleDisplay(guiGraphics);

        // Right side: Parts, status, and costs
        renderVehicleInfo(guiGraphics);
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

    private void renderVehicleInfo(GuiGraphics guiGraphics) {
        int rightX = 140;
        int startY = 20;
        int lineHeight = 10;

        // Vehicle stats
        guiGraphics.drawString(font, "=== ZUSTAND ===", rightX, startY, fontColor, false);

        guiGraphics.drawString(font, "Treibstoff:", rightX, startY + lineHeight * 2, partColor, false);
        guiGraphics.drawString(font, String.format("%.1f%%", getFuelPercent()), rightX + 60, startY + lineHeight * 2, fontColor, false);

        guiGraphics.drawString(font, "Batterie:", rightX, startY + lineHeight * 3, partColor, false);
        guiGraphics.drawString(font, String.format("%.1f%%", getBatteryPercent()), rightX + 60, startY + lineHeight * 3, fontColor, false);

        guiGraphics.drawString(font, "Schaden:", rightX, startY + lineHeight * 4, partColor, false);
        guiGraphics.drawString(font, String.format("%.1f%%", getDamagePercent()), rightX + 60, startY + lineHeight * 4, fontColor, false);

        guiGraphics.drawString(font, "Temp:", rightX, startY + lineHeight * 5, partColor, false);
        guiGraphics.drawString(font, String.format("%.1f°C", getTemperatureCelsius()), rightX + 60, startY + lineHeight * 5, fontColor, false);

        // Installed parts
        int partsY = startY + lineHeight * 7;
        guiGraphics.drawString(font, "=== TEILE ===", rightX, partsY, fontColor, false);
        renderInstalledParts(guiGraphics, rightX, partsY + lineHeight);

        // Service costs
        int costY = partsY + lineHeight * 7;
        guiGraphics.drawString(font, "=== KOSTEN ===", rightX, costY, fontColor, false);
        renderCostBreakdown(guiGraphics, rightX, costY + lineHeight);
    }

    private void renderInstalledParts(GuiGraphics guiGraphics, int x, int y) {
        int line = 0;

        // Show installed parts
        Part chassis = vehicle.getBodyPart();
        if (chassis != null) {
            guiGraphics.drawString(font, "Karosserie", x, y + line * 10, partColor, false);
            line++;
        }

        Part engine = vehicle.getEngine();
        if (engine != null) {
            guiGraphics.drawString(font, "Motor", x, y + line * 10, partColor, false);
            line++;
        }

        if (vehicle.getWheels() != null && !vehicle.getWheels().isEmpty()) {
            guiGraphics.drawString(font, "Reifen", x, y + line * 10, partColor, false);
            line++;
        }
    }

    private void renderCostBreakdown(GuiGraphics guiGraphics, int x, int y) {
        int line = 0;

        // Inspection fee
        guiGraphics.drawString(font, "Inspektion:", x, y + line * 10, partColor, false);
        guiGraphics.drawString(font, "10.00€", x + 60, y + line * 10, costColor, false);
        line++;

        // Damage repair
        float damage = getDamagePercent();
        if (damage > 0) {
            guiGraphics.drawString(font, "Reparatur:", x, y + line * 10, partColor, false);
            guiGraphics.drawString(font, String.format("%.2f€", damage * 2.0), x + 60, y + line * 10, costColor, false);
            line++;
        }

        // Battery service
        float batteryPercent = getBatteryPercent();
        if (batteryPercent < 50) {
            guiGraphics.drawString(font, "Batterie:", x, y + line * 10, partColor, false);
            guiGraphics.drawString(font, String.format("%.2f€", (50 - batteryPercent) * 0.5), x + 60, y + line * 10, costColor, false);
            line++;
        }

        line++; // Empty line
        // Total
        guiGraphics.drawString(font, "GESAMT:", x, y + line * 10, fontColor, true);
        guiGraphics.drawString(font, String.format("%.2f€", calculateTotalCost()), x + 60, y + line * 10, costColor, true);
    }

    private double calculateTotalCost() {
        if (vehicle == null) return 0.0;

        double cost = 10.0; // Base inspection

        float damage = getDamagePercent();
        if (damage > 0) {
            cost += damage * 2.0;
        }

        float batteryPercent = getBatteryPercent();
        if (batteryPercent < 50) {
            cost += (50 - batteryPercent) * 0.5;
        }

        return cost;
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
