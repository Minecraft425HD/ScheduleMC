package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.towing.MembershipManager;
import de.rolandsw.schedulemc.towing.MembershipData;
import de.rolandsw.schedulemc.towing.MembershipTier;
import de.rolandsw.schedulemc.towing.TowingYardManager;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pannenhilfe App - Towing service smartphone application
 * Shows membership status and allows requesting towing for vehicles
 */
@OnlyIn(Dist.CLIENT)
public class TowingServiceAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int MARGIN_TOP = 5;
    private static final int VEHICLE_ITEM_HEIGHT = 60;

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;

    private List<VehicleInfo> vehicles = new ArrayList<>();
    private MembershipData membership;
    private UUID playerId;

    public TowingServiceAppScreen(Screen parent) {
        super(Component.translatable("gui.app.towing.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        if (minecraft != null && minecraft.player != null) {
            this.playerId = minecraft.player.getUUID();
            loadVehicles();
            loadMembership();
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.back"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 60, 20).build());

        // Mitgliedschaft-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.towing.membership"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(new MembershipSelectionScreen(this, membership));
            }
        }).bounds(leftPos + 80, topPos + HEIGHT - 30, 110, 20).build());
    }

    private void loadVehicles() {
        vehicles.clear();
        if (minecraft == null || minecraft.level == null || minecraft.player == null || playerId == null) {
            return;
        }

        Level level = minecraft.level;
        String playerName = minecraft.player.getName().getString();
        String playerPrefix = extractLicensePlatePrefix(playerName);

        // Query all vehicle entities within a large radius around player
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(
            minecraft.player.getX() - 1000, minecraft.player.getY() - 256, minecraft.player.getZ() - 1000,
            minecraft.player.getX() + 1000, minecraft.player.getY() + 256, minecraft.player.getZ() + 1000
        );

        for (EntityGenericVehicle vehicle : level.getEntitiesOfClass(EntityGenericVehicle.class, searchBox)) {
            // Check 1: Owner ID matches (preferred method)
            if (playerId.equals(vehicle.getOwnerId())) {
                vehicles.add(new VehicleInfo(vehicle));
                continue;
            }

            // Check 2: License plate prefix matches player name (fallback)
            String licensePlate = vehicle.getLicensePlate();
            if (licensePlate != null && !licensePlate.isEmpty()) {
                if (licensePlate.startsWith(playerPrefix + "-")) {
                    vehicles.add(new VehicleInfo(vehicle));
                }
            }
        }
    }

    /**
     * Extracts 3-letter prefix from player name for license plate matching
     * Same logic as VehiclePurchaseHandler.extractPrefix()
     */
    private String extractLicensePlatePrefix(String playerName) {
        // Remove special characters and convert to uppercase
        StringBuilder cleaned = new StringBuilder();
        for (char c : playerName.toCharArray()) {
            char upper = Character.toUpperCase(c);

            // Convert German umlauts
            if (upper == 'Ä') upper = 'A';
            else if (upper == 'Ö') upper = 'O';
            else if (upper == 'Ü') upper = 'U';
            else if (upper == 'ß') upper = 'S';

            // Only keep A-Z
            if (upper >= 'A' && upper <= 'Z') {
                cleaned.append(upper);
            }
        }

        String result = cleaned.toString();

        // Ensure at least 3 characters
        if (result.length() < 3) {
            result = result + "XXX".substring(result.length());
        }

        return result.substring(0, 3);
    }

    private void loadMembership() {
        if (playerId != null) {
            membership = MembershipManager.getMembership(playerId);
            if (membership == null) {
                membership = new MembershipData(playerId, MembershipTier.NONE);
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
        guiGraphics.drawString(this.font, "\u00a70\u00a7l" + Component.translatable("gui.app.towing.title").getString(), leftPos + 10, topPos + 13, 0x000000, false);

        // Membership status area
        int membershipY = topPos + 40;
        renderMembershipStatus(guiGraphics, membershipY);

        // Vehicle list
        int vehicleListY = membershipY + 45;
        int vehicleListHeight = HEIGHT - 120;

        if (vehicles.isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.app.towing.no_vehicles").getString(),
                leftPos + WIDTH / 2, vehicleListY + 20, 0xFF999999);
        } else {
            renderVehicleList(guiGraphics, vehicleListY, vehicleListHeight, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderMembershipStatus(GuiGraphics guiGraphics, int y) {
        // Background for membership card
        guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 40, 0xFFF0F0F0);

        if (membership != null && membership.isActive()) {
            MembershipTier tier = membership.getTier();
            String tierName = Component.translatable(tier.getTranslationKey()).getString();
            int coverage = tier.getCoveragePercent();

            guiGraphics.drawString(this.font, "§0§l" + tierName, leftPos + 15, y + 5, 0x000000, false);
            guiGraphics.drawString(this.font, "§7" + Component.translatable("gui.app.towing.coverage", coverage).getString(),
                leftPos + 15, y + 18, 0xFF666666, false);

            int daysUntilPayment = membership.getDaysUntilPayment();
            guiGraphics.drawString(this.font, "§7" + Component.translatable("gui.app.towing.next_payment", daysUntilPayment).getString(),
                leftPos + 15, y + 28, 0xFF666666, false);
        } else {
            guiGraphics.drawString(this.font, "§0§l" + Component.translatable("towing.membership.tier.none").getString(),
                leftPos + 15, y + 5, 0x000000, false);
            guiGraphics.drawString(this.font, "§7" + Component.translatable("gui.app.towing.no_coverage").getString(),
                leftPos + 15, y + 18, 0xFF666666, false);
        }
    }

    private void renderVehicleList(GuiGraphics guiGraphics, int listY, int listHeight, int mouseX, int mouseY) {
        for (int i = 0; i < vehicles.size(); i++) {
            int itemY = listY + (i * VEHICLE_ITEM_HEIGHT) - scrollOffset;

            // Skip if not visible
            if (itemY + VEHICLE_ITEM_HEIGHT < listY || itemY > listY + listHeight) {
                continue;
            }

            renderVehicleItem(guiGraphics, vehicles.get(i), leftPos, itemY, mouseX, mouseY);
        }
    }

    private void renderVehicleItem(GuiGraphics guiGraphics, VehicleInfo vehicleInfo, int x, int y, int mouseX, int mouseY) {
        int itemWidth = WIDTH - 20;
        int itemX = x + 10;

        // Background
        guiGraphics.fill(itemX, y, itemX + itemWidth, y + VEHICLE_ITEM_HEIGHT - 5, 0xFFF8F8F8);

        // Border
        guiGraphics.fill(itemX, y + VEHICLE_ITEM_HEIGHT - 6, itemX + itemWidth, y + VEHICLE_ITEM_HEIGHT - 5, 0xFFE0E0E0);

        // Vehicle name/type
        String vehicleName = vehicleInfo.getDisplayName();
        guiGraphics.drawString(this.font, "§0§l" + vehicleName, itemX + 5, y + 5, 0x000000, false);

        // Damage display with color coding
        int damage = (int) vehicleInfo.damage;
        int damageColor = getDamageColor(damage);
        String damageText = Component.translatable("gui.app.towing.damage", damage).getString();
        guiGraphics.drawString(this.font, damageText, itemX + 5, y + 18, damageColor, false);

        // Engine status
        String engineStatus = vehicleInfo.isEngineRunning ?
            Component.translatable("gui.app.towing.engine.running").getString() :
            Component.translatable("gui.app.towing.engine.off").getString();
        int engineColor = vehicleInfo.isEngineRunning ? 0xFF00AA00 : 0xFF666666;
        guiGraphics.drawString(this.font, engineStatus, itemX + 5, y + 30, engineColor, false);

        // Towing button with cost
        int buttonY = y + 5;
        int buttonX = itemX + itemWidth - 75;

        double towingCost = calculateTowingCost(vehicleInfo);
        String costText = String.format("%.0f€", towingCost);

        boolean isHovering = mouseX >= buttonX && mouseX <= buttonX + 70 &&
                            mouseY >= buttonY && mouseY <= buttonY + 35;

        int buttonColor = isHovering ? 0xFF3399FF : 0xFF0066CC;
        guiGraphics.fill(buttonX, buttonY, buttonX + 70, buttonY + 35, buttonColor);

        guiGraphics.drawCenteredString(this.font,
            Component.translatable("gui.app.towing.request").getString(),
            buttonX + 35, buttonY + 7, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, costText, buttonX + 35, buttonY + 20, 0xFFFFFFFF);
    }

    private int getDamageColor(int damage) {
        if (damage >= 90) return 0xFFCC0000; // Red
        if (damage >= 70) return 0xFFFF6600; // Orange
        if (damage >= 40) return 0xFFFFCC00; // Yellow
        return 0xFF00AA00; // Green
    }

    private double calculateTowingCost(VehicleInfo vehicleInfo) {
        if (minecraft == null || minecraft.player == null) {
            return 0;
        }

        // Calculate distance to nearest towing yard
        // For now, use a dummy distance (this will be improved with towing yard selection)
        double distance = 100.0;

        double totalCost = TowingYardManager.calculateTowingCost(distance);

        // Apply membership discount
        if (membership != null && membership.isActive()) {
            totalCost = membership.getTier().calculatePlayerCost(totalCost);
        }

        return totalCost;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int vehicleListY = topPos + 85;

            for (int i = 0; i < vehicles.size(); i++) {
                int itemY = vehicleListY + (i * VEHICLE_ITEM_HEIGHT) - scrollOffset;
                int itemX = leftPos + 10;
                int itemWidth = WIDTH - 20;

                // Check if clicking towing button
                int buttonX = itemX + itemWidth - 75;
                int buttonY = itemY + 5;

                if (mouseX >= buttonX && mouseX <= buttonX + 70 &&
                    mouseY >= buttonY && mouseY <= buttonY + 35) {

                    // Open towing yard selection screen
                    if (minecraft != null) {
                        minecraft.setScreen(new TowingYardSelectionScreen(this, vehicles.get(i)));
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!vehicles.isEmpty() && vehicles.size() * VEHICLE_ITEM_HEIGHT > HEIGHT - 120) {
            scrollOffset -= (int)(delta * 10);
            scrollOffset = Math.max(0, Math.min(scrollOffset, vehicles.size() * VEHICLE_ITEM_HEIGHT - (HEIGHT - 120)));
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
     * Helper class to store vehicle information for rendering
     */
    public static class VehicleInfo {
        final UUID entityId;
        final String displayName;
        final float damage;
        final boolean isEngineRunning;
        final double x, y, z;

        public VehicleInfo(EntityGenericVehicle vehicle) {
            this.entityId = vehicle.getUUID();
            this.displayName = vehicle.getDisplayName().getString();
            this.damage = vehicle.getDamageComponent().getDamage();
            this.isEngineRunning = vehicle.getPhysicsComponent().isStarted();
            this.x = vehicle.getX();
            this.y = vehicle.getY();
            this.z = vehicle.getZ();
        }

        public String getDisplayName() {
            if (displayName.length() > 20) {
                return displayName.substring(0, 17) + "...";
            }
            return displayName;
        }

        public UUID getEntityId() {
            return entityId;
        }
    }
}
