package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.net.MessageGaragePayment;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.math.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiGarage extends ScreenBase<ContainerGarage> {

    private static final ResourceLocation GARAGE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_vehicle.png");

    private static final int fontColor = 4210752;
    private static final int costColor = 0x00AA00; // Green for cost

    private Inventory playerInv;
    private EntityGenericVehicle vehicle;
    private Button payButton;

    public GuiGarage(ContainerGarage containerGarage, Inventory playerInv, Component title) {
        super(GARAGE_GUI_TEXTURE, containerGarage, playerInv, title);
        this.playerInv = playerInv;
        this.vehicle = containerGarage.getVehicle();

        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        // Add "Bezahlen" button centered at bottom
        int buttonWidth = 120;
        int buttonHeight = 20;
        int buttonX = leftPos + (imageWidth - buttonWidth) / 2;
        int buttonY = topPos + imageHeight - 28;

        payButton = addRenderableWidget(Button.builder(
            Component.literal("Bezahlen (" + String.format("%.2f€", calculateTotalCost()) + ")"),
            button -> {
                // Send payment packet to server
                Main.SIMPLE_CHANNEL.sendToServer(new MessageGaragePayment(
                    minecraft.player.getUUID(),
                    vehicle.getUUID()
                ));
            })
            .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
            .build()
        );
    }

    @Override
    public void onClose() {
        // Prevent closing via ESC/E - only allow closing via payment button
        // We don't call super.onClose() to prevent normal closing behavior
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block ESC key (keyCode 256) and inventory key
        if (keyCode == 256 || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true; // Consume the event without closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        // Title
        String title = "FAHRZEUG-WERKSTATT";
        int titleX = (imageWidth - font.width(title)) / 2;
        guiGraphics.drawString(font, title, titleX, 6, fontColor, false);

        if (vehicle != null && !vehicle.isRemoved()) {
            // Vehicle status display
            int startY = 20;
            int lineHeight = 10;
            int labelX = 8;
            int valueX = 72;

            guiGraphics.drawString(font, "Fahrzeug:", labelX, startY, fontColor, false);

            guiGraphics.drawString(font, "Treibstoff:", labelX, startY + lineHeight, fontColor, false);
            guiGraphics.drawString(font, getFuelValueString(), valueX, startY + lineHeight, fontColor, false);

            guiGraphics.drawString(font, "Batterie:", labelX, startY + lineHeight * 2, fontColor, false);
            guiGraphics.drawString(font, getBatteryValueString(), valueX, startY + lineHeight * 2, fontColor, false);

            guiGraphics.drawString(font, "Schaden:", labelX, startY + lineHeight * 3, fontColor, false);
            guiGraphics.drawString(font, getDamageValueString(), valueX, startY + lineHeight * 3, fontColor, false);

            // Cost breakdown
            int costY = startY + lineHeight * 5;
            guiGraphics.drawString(font, "--- KOSTEN ---", labelX, costY, fontColor, false);

            guiGraphics.drawString(font, "Inspektion:", labelX, costY + lineHeight, fontColor, false);
            guiGraphics.drawString(font, "10.00€", valueX, costY + lineHeight, costColor, false);

            float damage = getDamagePercent();
            if (damage > 0) {
                guiGraphics.drawString(font, "Reparatur:", labelX, costY + lineHeight * 2, fontColor, false);
                guiGraphics.drawString(font, String.format("%.2f€", damage * 2.0), valueX, costY + lineHeight * 2, costColor, false);
            }

            float batteryPercent = getBatteryPercent();
            if (batteryPercent < 50) {
                int line = damage > 0 ? 3 : 2;
                guiGraphics.drawString(font, "Batterie:", labelX, costY + lineHeight * line, fontColor, false);
                guiGraphics.drawString(font, String.format("%.2f€", (50 - batteryPercent) * 0.5), valueX, costY + lineHeight * line, costColor, false);
            }

            // Total
            int totalLine = costY + lineHeight * 5;
            guiGraphics.drawString(font, "GESAMT:", labelX, totalLine, fontColor, false);
            guiGraphics.drawString(font, String.format("%.2f€", calculateTotalCost()), valueX, totalLine, costColor, false);
        } else {
            guiGraphics.drawString(font, "Kein Fahrzeug!", 8, 20, 0xFF0000, false);
        }
    }

    private double calculateTotalCost() {
        if (vehicle == null) return 0.0;

        double cost = 10.0; // Base inspection fee

        // Repair cost
        float damage = getDamagePercent();
        if (damage > 0) {
            cost += damage * 2.0;
        }

        // Battery service
        float batteryPercent = getBatteryPercent();
        if (batteryPercent < 50) {
            cost += (50 - batteryPercent) * 0.5;
        }

        return cost;
    }

    // ===== Calculation Methods =====

    public float getFuelPercent() {
        if (vehicle == null) return 0;
        float fuelPerc = ((float) vehicle.getFuelComponent().getFuelAmount()) / ((float) vehicle.getMaxFuel()) * 100F;
        return MathUtils.round(fuelPerc, 2);
    }

    public float getFuelLiters() {
        if (vehicle == null) return 0;
        float liters = vehicle.getFuelComponent().getFuelAmount() / 1000.0F;
        return MathUtils.round(liters, 1);
    }

    public float getBatteryPercent() {
        if (vehicle == null) return 0;
        return MathUtils.round(vehicle.getBatteryComponent().getBatteryPercentage() * 100F, 1);
    }

    public float getBatteryVolts() {
        if (vehicle == null) return 0;
        float percentage = vehicle.getBatteryComponent().getBatteryPercentage();
        float volts = 11.0F + (percentage * 1.5F);
        return MathUtils.round(volts, 1);
    }

    public float getDamagePercent() {
        if (vehicle == null) return 0;
        float dmg = vehicle.getDamageComponent().getDamage();
        dmg = Math.min(dmg, 100);
        return MathUtils.round(dmg, 1);
    }

    // ===== Display String Methods =====

    public String getFuelValueString() {
        return String.format("%.1f%% | %.1f L", getFuelPercent(), getFuelLiters());
    }

    public String getBatteryValueString() {
        return String.format("%.1f%% | %.1f V", getBatteryPercent(), getBatteryVolts());
    }

    public String getDamageValueString() {
        return String.format("%.1f%%", getDamagePercent());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

        // Cover old graphics with solid background
        int bgColor = 0xFFC6C6C6;
        guiGraphics.fill(leftPos + 7, topPos + 5, leftPos + 169, topPos + 72, bgColor);
    }

}
