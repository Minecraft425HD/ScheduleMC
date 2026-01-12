package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.towing.TowingYardManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Screen for selecting which towing yard to tow the vehicle to
 */
@OnlyIn(Dist.CLIENT)
public class TowingYardSelectionScreen extends Screen {

    private final Screen parentScreen;
    private final TowingServiceAppScreen.VehicleInfo vehicleInfo;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int MARGIN_TOP = 5;
    private static final int YARD_ITEM_HEIGHT = 55;

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;

    private List<TowingYardInfo> towingYards = new ArrayList<>();

    public TowingYardSelectionScreen(Screen parent, TowingServiceAppScreen.VehicleInfo vehicleInfo) {
        super(Component.translatable("gui.app.towing.select_yard"));
        this.parentScreen = parent;
        this.vehicleInfo = vehicleInfo;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        loadTowingYards();

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.back"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 60, 20).build());
    }

    private void loadTowingYards() {
        towingYards.clear();

        List<String> yardPlotIds = TowingYardManager.getAllTowingYards();

        for (String plotId : yardPlotIds) {
            PlotRegion plot = PlotManager.getPlot(plotId);
            if (plot != null && plot.getType().isTowingYard()) {
                int freeSpots = TowingYardManager.countFreeSpots(plotId);
                if (freeSpots > 0) {
                    towingYards.add(new TowingYardInfo(plotId, plot, freeSpots));
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone border
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);

        // White background
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFFFFFFFF);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 35, 0xFFF8F8F8);
        guiGraphics.drawString(this.font, "§0§lAbschlepphof wählen", leftPos + 10, topPos + 13, 0x000000, false);

        // Towing yard list
        int listY = topPos + 45;
        int listHeight = HEIGHT - 85;

        if (towingYards.isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.app.towing.no_yards").getString(),
                leftPos + WIDTH / 2, listY + 20, 0xFF999999);
        } else {
            renderYardList(guiGraphics, listY, listHeight, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderYardList(GuiGraphics guiGraphics, int listY, int listHeight, int mouseX, int mouseY) {
        for (int i = 0; i < towingYards.size(); i++) {
            int itemY = listY + (i * YARD_ITEM_HEIGHT) - scrollOffset;

            // Skip if not visible
            if (itemY + YARD_ITEM_HEIGHT < listY || itemY > listY + listHeight) {
                continue;
            }

            renderYardItem(guiGraphics, towingYards.get(i), leftPos, itemY, mouseX, mouseY);
        }
    }

    private void renderYardItem(GuiGraphics guiGraphics, TowingYardInfo yardInfo, int x, int y, int mouseX, int mouseY) {
        int itemWidth = WIDTH - 20;
        int itemX = x + 10;

        boolean isHovering = mouseX >= itemX && mouseX <= itemX + itemWidth &&
                            mouseY >= y && mouseY <= y + YARD_ITEM_HEIGHT - 5;

        // Background
        int bgColor = isHovering ? 0xFFF0F0F0 : 0xFFFFFFFF;
        guiGraphics.fill(itemX, y, itemX + itemWidth, y + YARD_ITEM_HEIGHT - 5, bgColor);

        // Border
        guiGraphics.fill(itemX, y + YARD_ITEM_HEIGHT - 6, itemX + itemWidth, y + YARD_ITEM_HEIGHT - 5, 0xFFE0E0E0);

        // Yard name/plot name
        String yardName = yardInfo.getDisplayName();
        guiGraphics.drawString(this.font, "§0§l" + yardName, itemX + 5, y + 5, 0x000000, false);

        // Distance
        double distance = calculateDistance(yardInfo);
        String distanceText = Component.translatable("gui.app.towing.distance",
            String.format("%.0f", distance)).getString();
        guiGraphics.drawString(this.font, "§7" + distanceText, itemX + 5, y + 18, 0xFF666666, false);

        // Free spots
        String spotsText = Component.translatable("gui.app.towing.free_spots", yardInfo.freeSpots).getString();
        guiGraphics.drawString(this.font, "§7" + spotsText, itemX + 5, y + 30, 0xFF666666, false);

        // Cost estimate
        double cost = TowingYardManager.calculateTowingCost(distance);
        String costText = String.format("%.0f€", cost);
        int costX = itemX + itemWidth - font.width(costText) - 5;
        guiGraphics.drawString(this.font, "§0§l" + costText, costX, y + 18, 0x000000, false);

        // Arrow indicator
        guiGraphics.drawString(this.font, "›", itemX + itemWidth - 15, y + 5, 0xFFC7C7CC, false);
    }

    private double calculateDistance(TowingYardInfo yardInfo) {
        if (minecraft == null || minecraft.player == null) {
            return 0;
        }

        BlockPos playerPos = minecraft.player.blockPosition();
        BlockPos yardCenter = yardInfo.plot.getCenter();

        return Math.sqrt(playerPos.distSqr(yardCenter));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int listY = topPos + 45;
            int itemX = leftPos + 10;
            int itemWidth = WIDTH - 20;

            for (int i = 0; i < towingYards.size(); i++) {
                int itemY = listY + (i * YARD_ITEM_HEIGHT) - scrollOffset;

                if (mouseX >= itemX && mouseX <= itemX + itemWidth &&
                    mouseY >= itemY && mouseY <= itemY + YARD_ITEM_HEIGHT - 5) {

                    // Request towing to this yard
                    requestTowing(towingYards.get(i));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void requestTowing(TowingYardInfo yardInfo) {
        // Send network packet to server to request towing
        de.rolandsw.schedulemc.towing.network.TowingNetworkHandler.sendToServer(
            new de.rolandsw.schedulemc.towing.network.RequestTowingPacket(
                vehicleInfo.getEntityId(),
                yardInfo.plotId
            )
        );

        // Go back to main towing app
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!towingYards.isEmpty() && towingYards.size() * YARD_ITEM_HEIGHT > HEIGHT - 85) {
            scrollOffset -= (int)(delta * 10);
            scrollOffset = Math.max(0, Math.min(scrollOffset, towingYards.size() * YARD_ITEM_HEIGHT - (HEIGHT - 85)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Helper class to store towing yard information
     */
    private static class TowingYardInfo {
        final String plotId;
        final PlotRegion plot;
        final int freeSpots;

        public TowingYardInfo(String plotId, PlotRegion plot, int freeSpots) {
            this.plotId = plotId;
            this.plot = plot;
            this.freeSpots = freeSpots;
        }

        public String getDisplayName() {
            String name = plot.getPlotName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
            return Component.translatable("gui.app.towing.yard.unnamed").getString();
        }
    }
}
