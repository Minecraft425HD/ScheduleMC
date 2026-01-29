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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Card-based fuel station GUI - fully programmatic rendering.
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
    private static final int COL_SLOT_BG = 0xFF8B8B8B;

    // Text colors
    private static final int TEXT_DARK = 0x404040;
    private static final int TEXT_WHITE = 0xFFFFFF;

    private TileEntityFuelStation fuelStation;
    private Inventory playerInventory;

    protected Button buttonStart;
    protected Button buttonStop;

    public GuiFuelStation(ContainerFuelStation fuelStation, Inventory playerInventory, Component title) {
        super(GUI_TEXTURE, fuelStation, playerInventory, title);
        this.fuelStation = fuelStation.getFuelStation();
        this.playerInventory = playerInventory;

        imageWidth = 176;
        imageHeight = 217;
    }

    @Override
    protected void init() {
        super.init();

        buttonStart = addRenderableWidget(Button.builder(Component.translatable("button.vehicle.start"), button -> {
            fuelStation.setFueling(true);
            fuelStation.sendStartFuelPacket(true);
        }).bounds((width / 2) - 20, topPos + 100, 40, 20).build());
        buttonStop = addRenderableWidget(Button.builder(Component.translatable("button.vehicle.stop"), button -> {
            fuelStation.setFueling(false);
            fuelStation.sendStartFuelPacket(false);
            minecraft.setScreen(null);
        }).bounds(leftPos + imageWidth - 40 - 7, topPos + 100, 40, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        // Fully programmatic - do NOT call super
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;

        // === Main GUI frame ===
        drawFrame(g, x, y, w, h);

        // === INFO CARD (Y=3 to Y=90) ===
        drawInsetPanel(g, x + 5, y + 3, w - 10, 87, COL_CARD_BG);
        g.fill(x + 6, y + 4, x + w - 6, y + 16, COL_CARD_HEADER);

        // === ACTION SECTION (Y=93 to Y=122) ===
        drawInsetPanel(g, x + 5, y + 93, w - 10, 29, COL_CARD_BG);
        // Trade slot backgrounds
        drawSlotBg(g, x + 18, y + 99);
        drawSlotBg(g, x + 38, y + 99);

        // Button state
        buttonStart.active = !fuelStation.isFueling();
        buttonStop.active = fuelStation.isFueling();

        // === PLAYER INVENTORY (with dark header for label) ===
        int invOffset = 51;
        int mainInvY = y + 84 + invOffset;
        drawInsetPanel(g, x + 5, mainInvY - 14, w - 10, 93, COL_BG);
        g.fill(x + 6, mainInvY - 13, x + w - 6, mainInvY - 2, COL_CARD_HEADER);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBg(g, x + 8 + col * 18, mainInvY + row * 18);
            }
        }
        int hotbarY = y + 142 + invOffset;
        for (int col = 0; col < 9; col++) {
            drawSlotBg(g, x + 8 + col * 18, hotbarY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Do NOT call super - fully custom

        // === Info card header ===
        g.drawString(font, Component.translatable("gui.fuel_station").getString(), 8, 5, TEXT_WHITE, false);

        // === Info content ===
        IFluidHandler fluidHandler = fuelStation.getFluidHandlerInFront();

        int ly = 19;
        if (fluidHandler instanceof Entity entity) {
            // Vehicle name
            String name;
            if (entity instanceof EntityGenericVehicle vehicle) {
                name = vehicle.getShortName().getString();
            } else {
                name = entity.getDisplayName().getString();
            }
            g.drawString(font, Component.translatable("fuel_station.vehicle_info",
                    Component.literal(name).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                    8, ly, TEXT_DARK, false);
            ly += 13;

            // Fuel info
            drawFuelInfo(g, fluidHandler, ly);
            ly += 26; // two lines for fuel (13px each)

            // Odometer
            if (entity instanceof EntityGenericVehicle vehicle) {
                long odo = vehicle.getOdometer();
                String odometerText = String.format("%,d Bl.", odo).replace(',', '.');
                g.drawString(font, Component.translatable("fuel_station.odometer",
                        Component.literal(odometerText).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                        8, ly, TEXT_DARK, false);
                ly += 13;
            }
        } else {
            g.drawString(font, Component.translatable("fuel_station.no_vehicle").getVisualOrderText(),
                    8, ly, TEXT_DARK, false);
            ly += 26;
        }

        // Refueled counter
        g.drawString(font, Component.translatable("fuel_station.refueled",
                Component.literal(String.valueOf(fuelStation.getFuelCounter())).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                8, ly, TEXT_DARK, false);
        ly += 13;

        // Buffer info
        drawBufferInfo(g, ly);

        // === Player inventory label (inside dark header) ===
        int mainInvRelY = 84 + 51; // 51 = invOffset for fuel station
        g.drawString(font, playerInventory.getDisplayName().getVisualOrderText(), 8, mainInvRelY - 11, TEXT_WHITE, false);

        // === Tooltip for trade slot ===
        ItemStack stack = fuelStation.getTradingInventory().getItem(0);
        if (!stack.isEmpty()) {
            if (mouseX >= leftPos + 18 && mouseX <= leftPos + 33) {
                if (mouseY >= topPos + 99 && mouseY <= topPos + 114) {
                    List<FormattedCharSequence> list = new ArrayList<>();
                    list.add(Component.translatable("tooltip.trade", stack.getCount(), stack.getHoverName(), fuelStation.getTradeAmount()).getVisualOrderText());
                    g.renderTooltip(font, list, mouseX - leftPos, mouseY - topPos);
                }
            }
        }
    }

    private void drawFuelInfo(GuiGraphics g, IFluidHandler handler, int y) {
        if (handler.getTanks() <= 0) {
            g.drawString(font, Component.translatable("fuel_station.fuel_empty").getVisualOrderText(),
                    8, y, TEXT_DARK, false);
            return;
        }
        FluidStack tank = handler.getFluidInTank(0);
        g.drawString(font, Component.translatable("fuel_station.vehicle_fuel_amount",
                Component.literal(String.valueOf(tank.getAmount())).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(handler.getTankCapacity(0))).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                8, y, TEXT_DARK, false);
        if (!tank.isEmpty()) {
            g.drawString(font, Component.translatable("fuel_station.vehicle_fuel_type",
                    Component.literal(tank.getDisplayName().getString()).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                    8, y + 13, TEXT_DARK, false);
        }
    }

    private void drawBufferInfo(GuiGraphics g, int y) {
        FluidStack stack = fuelStation.getStorage();
        if (stack.isEmpty()) {
            g.drawString(font, Component.translatable("fuel_station.fuel_empty").getVisualOrderText(),
                    8, y, TEXT_DARK, false);
            return;
        }
        int amount = fuelStation.getFuelAmount();
        g.drawString(font, Component.translatable("fuel_station.fuel_buffer_amount",
                Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(fuelStation.maxStorageAmount)).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                8, y, TEXT_DARK, false);
        g.drawString(font, Component.translatable("fuel_station.fuel_buffer_type",
                Component.literal(stack.getDisplayName().getString()).withStyle(ChatFormatting.WHITE)).getVisualOrderText(),
                8, y + 13, TEXT_DARK, false);
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

    private void drawSlotBg(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 1, COL_SHADOW);
        g.fill(x, y, x + 1, y + 18, COL_SHADOW);
        g.fill(x + 1, y + 17, x + 18, y + 18, COL_WHITE);
        g.fill(x + 17, y + 1, x + 18, y + 18, COL_WHITE);
        g.fill(x + 1, y + 1, x + 17, y + 17, COL_SLOT_BG);
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
