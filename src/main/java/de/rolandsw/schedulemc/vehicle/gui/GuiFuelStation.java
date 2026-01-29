package de.rolandsw.schedulemc.vehicle.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityFuelStation;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Card-based fuel station GUI - fully programmatic rendering.
 * 3 cards: Vehicle Info, Prices (day/night), Fueling Session.
 * No slots, no player inventory.
 */
public class GuiFuelStation extends ScreenBase<ContainerFuelStation> {

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/gui_fuel_station.png");

    // Frame colors
    private static final int COL_BLACK = 0xFF000000;
    private static final int COL_WHITE = 0xFFFFFFFF;
    private static final int COL_SHADOW = 0xFF555555;
    private static final int COL_BG = 0xFFC6C6C6;

    // Card colors
    private static final int COL_CARD_BG = 0xFFAAAAAA;
    private static final int COL_CARD_HEADER = 0xFF404040;

    // Bar colors
    private static final int COL_BAR_BG = 0xFF8B8B8B;
    private static final int COL_BAR_FILL = 0xFF00AA00;

    // Text colors
    private static final int TEXT_DARK = 0x404040;
    private static final int TEXT_WHITE = 0xFFFFFF;
    private static final int TEXT_LIGHT = 0xAAAAAA;
    private static final int TEXT_GREEN = 0x55FF55;
    private static final int TEXT_GRAY = 0x888888;

    private TileEntityFuelStation fuelStation;

    protected Button buttonStart;
    protected Button buttonStop;

    public GuiFuelStation(ContainerFuelStation container, Inventory playerInventory, Component title) {
        super(GUI_TEXTURE, container, playerInventory, title);
        this.fuelStation = container.getFuelStation();

        imageWidth = 176;
        imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();

        // START button: left half
        buttonStart = addRenderableWidget(Button.builder(Component.translatable("button.vehicle.start"), button -> {
            fuelStation.setFueling(true);
            fuelStation.sendStartFuelPacket(true);
        }).bounds(leftPos + 8, topPos + 150, 75, 20).build());

        // STOP button: right half
        buttonStop = addRenderableWidget(Button.builder(Component.translatable("button.vehicle.stop"), button -> {
            fuelStation.setFueling(false);
            fuelStation.sendStartFuelPacket(false);
            minecraft.setScreen(null);
        }).bounds(leftPos + 93, topPos + 150, 75, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        // Fully programmatic - do NOT call super
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;

        // === Main GUI frame (3D beveled) ===
        drawFrame(g, x, y, w, h);

        // === CARD 1: Vehicle Info (Y=4, h=60) ===
        drawInsetPanel(g, x + 5, y + 4, w - 10, 60, COL_CARD_BG);
        g.fill(x + 6, y + 5, x + w - 6, y + 17, COL_CARD_HEADER);

        // Tank fill bar (only when vehicle present)
        IFluidHandler handler = fuelStation.getFluidHandlerInFront();
        if (handler != null && fuelStation.getEntityInFront() != null) {
            int barX = x + 8;
            int barY = y + 55;
            int barW = 120;
            int barH = 5;
            g.fill(barX, barY, barX + barW, barY + barH, COL_BAR_BG);
            float percent = getTankPercent(handler);
            int fillW = (int) (barW * percent);
            if (fillW > 0) {
                g.fill(barX, barY, barX + fillW, barY + barH, COL_BAR_FILL);
            }
        }

        // === CARD 2: Prices (Y=66, h=40) ===
        drawInsetPanel(g, x + 5, y + 66, w - 10, 40, COL_CARD_BG);
        g.fill(x + 6, y + 67, x + w - 6, y + 79, COL_CARD_HEADER);

        // === CARD 3: Fueling Session (Y=108, h=66) ===
        drawInsetPanel(g, x + 5, y + 108, w - 10, 66, COL_CARD_BG);
        g.fill(x + 6, y + 109, x + w - 6, y + 121, COL_CARD_HEADER);

        // Button state
        buttonStart.active = !fuelStation.isFueling() && fuelStation.getFluidHandlerInFront() != null;
        buttonStop.active = fuelStation.isFueling();
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Do NOT call super - fully custom

        // === CARD 1: Vehicle Info ===
        g.drawString(font, Component.translatable("gui.fuel_station").getString(), 8, 6, TEXT_WHITE, false);

        Entity entity = fuelStation.getEntityInFront();
        IFluidHandler handler = fuelStation.getFluidHandlerInFront();
        if (entity != null && handler != null) {
            // Vehicle name
            String name;
            if (entity instanceof EntityGenericVehicle vehicle) {
                name = vehicle.getShortName().getString();
            } else {
                name = entity.getDisplayName().getString();
            }
            g.drawString(font, Component.translatable("fuel_station.vehicle_info",
                    Component.literal(name).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                    8, 19, TEXT_DARK, false);

            // Odometer
            if (entity instanceof EntityGenericVehicle vehicle) {
                long odo = vehicle.getOdometer();
                String odometerText = String.format("%,d Bl.", odo).replace(',', '.');
                g.drawString(font, Component.translatable("fuel_station.odometer",
                        Component.literal(odometerText).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                        8, 31, TEXT_DARK, false);
            }

            // Tank level text
            if (handler.getTanks() > 0) {
                FluidStack tank = handler.getFluidInTank(0);
                int cur = tank.getAmount();
                int max = handler.getTankCapacity(0);
                g.drawString(font, Component.translatable("fuel_station.vehicle_fuel_amount",
                        Component.literal(String.valueOf(cur)).withStyle(ChatFormatting.WHITE),
                        Component.literal(String.valueOf(max)).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                        8, 43, TEXT_DARK, false);

                // Percentage text after bar
                int pct = max > 0 ? (int) (100f * cur / max) : 0;
                drawRightAligned(g, pct + "%", 168, 54, TEXT_DARK);
            }
        } else {
            // No vehicle
            g.drawString(font, Component.translatable("fuel_station.no_vehicle").getVisualOrderText(),
                    8, 19, TEXT_DARK, false);
        }

        // === CARD 2: Prices ===
        g.drawString(font, Component.translatable("gui.fuel_station.prices").getString(), 8, 68, TEXT_WHITE, false);

        boolean isDay = isDaytime();
        int morningPrice = fuelStation.getMorningPrice();
        int eveningPrice = fuelStation.getEveningPrice();

        // Day price line (highlighted green if active)
        String dayText = "Tag:     " + (morningPrice * 100) + "\u20AC / 1000 mB";
        g.drawString(font, dayText, 8, 81, isDay ? TEXT_GREEN : TEXT_GRAY, false);
        // Arrow indicator for active price
        if (isDay) {
            drawRightAligned(g, "\u25C0", 168, 81, TEXT_GREEN);
        }

        // Night price line
        String nightText = "Nacht:  " + (eveningPrice * 100) + "\u20AC / 1000 mB";
        g.drawString(font, nightText, 8, 93, !isDay ? TEXT_GREEN : TEXT_GRAY, false);
        if (!isDay) {
            drawRightAligned(g, "\u25C0", 168, 93, TEXT_GREEN);
        }

        // === CARD 3: Fueling Session ===
        g.drawString(font, Component.translatable("gui.fuel_station.fueling").getString(), 8, 110, TEXT_WHITE, false);

        // Fueled amount
        int fueled = fuelStation.getFuelCounter();
        g.drawString(font, Component.translatable("fuel_station.refueled",
                Component.literal(String.valueOf(fueled)).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                8, 123, TEXT_DARK, false);

        // Session cost (in cents, formatted as euros incl. MwSt)
        int costCents = fuelStation.getTotalCostThisSessionCents();
        String costText = String.format("%.2f\u20AC", costCents / 100.0);
        g.drawString(font, Component.translatable("gui.fuel_station.session_cost",
                Component.literal(costText).withStyle(ChatFormatting.GOLD)).getVisualOrderText(),
                8, 135, TEXT_DARK, false);
        g.drawString(font, "inkl. MwSt", 8, 147, TEXT_GRAY, false);
    }

    // ==================== Helpers ====================

    private float getTankPercent(IFluidHandler handler) {
        if (handler.getTanks() <= 0) return 0f;
        int cur = handler.getFluidInTank(0).getAmount();
        int max = handler.getTankCapacity(0);
        return max > 0 ? (float) cur / max : 0f;
    }

    private boolean isDaytime() {
        if (minecraft != null && minecraft.level != null) {
            return minecraft.level.getDayTime() % 24000 < 12000;
        }
        return true;
    }

    private void drawRightAligned(GuiGraphics g, String text, int rightX, int y, int color) {
        int textW = font.width(text);
        g.drawString(font, text, rightX - textW, y, color, false);
    }

    // ==================== Drawing Helpers ====================

    private void drawFrame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + 1, COL_BLACK);
        g.fill(x, y + h - 1, x + w, y + h, COL_BLACK);
        g.fill(x, y, x + 1, y + h, COL_BLACK);
        g.fill(x + w - 1, y, x + w, y + h, COL_BLACK);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, COL_WHITE);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, COL_WHITE);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, COL_SHADOW);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, COL_SHADOW);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, COL_BG);
    }

    private void drawInsetPanel(GuiGraphics g, int x, int y, int w, int h, int bgColor) {
        g.fill(x, y, x + w, y + 1, COL_SHADOW);
        g.fill(x, y, x + 1, y + h, COL_SHADOW);
        g.fill(x, y + h - 1, x + w, y + h, COL_WHITE);
        g.fill(x + w - 1, y, x + w, y + h, COL_WHITE);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);
    }

    // ==================== Overrides ====================

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (fuelStation.isFueling()) {
            if (keyCode == 256 || keyCode == 69) {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                        Component.translatable("message.fuel_station.stop_refueling")
                            .withStyle(ChatFormatting.RED),
                        true
                    );
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (fuelStation.isFueling()) {
            fuelStation.setFueling(false);
            fuelStation.sendStartFuelPacket(false);
        }
        super.onClose();
    }
}
