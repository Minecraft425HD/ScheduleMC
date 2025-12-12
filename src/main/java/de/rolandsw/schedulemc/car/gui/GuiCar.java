package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.math.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCar extends ScreenBase<ContainerCar> {

    private static final ResourceLocation CAR_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_car.png");

    private static final int fontColor = 4210752;

    private Inventory playerInv;
    private EntityGenericCar car;

    public GuiCar(ContainerCar containerCar, Inventory playerInv, Component title) {
        super(CAR_GUI_TEXTURE, containerCar, playerInv, title);
        this.playerInv = playerInv;
        this.car = containerCar.getCar();

        imageWidth = 176;
        imageHeight = 248;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        // Title
        guiGraphics.drawString(font, car.getDisplayName().getVisualOrderText(), 7, 87, fontColor, false);
        guiGraphics.drawString(font, playerInv.getDisplayName().getVisualOrderText(), 8, this.imageHeight - 96 + 2, fontColor, false);

        // Status Header with decorative line
        String header = "=== STATUS ===";
        int headerX = (imageWidth - font.width(header)) / 2;
        guiGraphics.drawString(font, header, headerX, 6, fontColor, false);

        // Status values in list format
        int startY = 18;
        int lineHeight = 12;

        guiGraphics.drawString(font, getFuelDisplayString(), 8, startY, fontColor, false);
        guiGraphics.drawString(font, getBatteryDisplayString(), 8, startY + lineHeight, fontColor, false);
        guiGraphics.drawString(font, getDamageDisplayString(), 8, startY + lineHeight * 2, fontColor, false);
        guiGraphics.drawString(font, getTempDisplayString(), 8, startY + lineHeight * 3, fontColor, false);
    }

    // ===== Calculation Methods =====

    public float getFuelPercent() {
        float fuelPerc = ((float) car.getFuelComponent().getFuelAmount()) / ((float) car.getMaxFuel()) * 100F;
        return MathUtils.round(fuelPerc, 2);
    }

    public float getFuelLiters() {
        float liters = car.getFuelComponent().getFuelAmount() / 1000.0F;
        return MathUtils.round(liters, 1);
    }

    public float getBatteryPercent() {
        return MathUtils.round(car.getBatteryComponent().getBatteryPercentage() * 100F, 1);
    }

    public float getBatteryVolts() {
        // Battery voltage range: 11.0V (0%) to 12.5V (100%)
        float percentage = car.getBatteryComponent().getBatteryPercentage();
        float volts = 11.0F + (percentage * 1.5F);
        return MathUtils.round(volts, 1);
    }

    public float getTemperatureCelsius() {
        return MathUtils.round(car.getDamageComponent().getTemperature(), 1);
    }

    public float getDamagePercent() {
        float dmg = car.getDamageComponent().getDamage();
        dmg = Math.min(dmg, 100);
        return MathUtils.round(dmg, 1);
    }

    // ===== Display String Methods =====

    public String getFuelDisplayString() {
        return String.format("Fuel: %4.1f%% | %4.1fL", getFuelPercent(), getFuelLiters());
    }

    public String getBatteryDisplayString() {
        return String.format("Batt: %4.1f%% | %4.1fV", getBatteryPercent(), getBatteryVolts());
    }

    public String getDamageDisplayString() {
        return String.format("Dmg:  %4.1f%%", getDamagePercent());
    }

    public String getTempDisplayString() {
        return String.format("Temp: %5.1f°C", getTemperatureCelsius());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        // Visual bars removed - using text-based display instead
    }

}
