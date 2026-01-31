package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.towing.MembershipData;
import de.rolandsw.schedulemc.towing.MembershipTier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Membership selection/management screen for towing service
 * Allows players to choose or change their membership tier
 */
@OnlyIn(Dist.CLIENT)
public class MembershipSelectionScreen extends Screen {

    private final Screen parentScreen;
    private final MembershipData currentMembership;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int MARGIN_TOP = 5;
    private static final int TIER_ITEM_HEIGHT = 50;

    private int leftPos;
    private int topPos;

    // PERFORMANCE: Cache translatable strings
    private String cachedMembershipHeader;
    private String cachedActiveText;
    private String[] cachedTierNames;
    private String[] cachedCoverageTexts;
    private String[] cachedFeeTexts;

    public MembershipSelectionScreen(Screen parent, MembershipData currentMembership) {
        super(Component.translatable("gui.app.towing.membership"));
        this.parentScreen = parent;
        this.currentMembership = currentMembership;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        // PERFORMANCE: Cache all tier translatable strings once in init()
        cachedMembershipHeader = Component.translatable("gui.app.towing.membership").getString();
        cachedActiveText = Component.translatable("gui.app.towing.active").getString();
        MembershipTier[] tiers = {MembershipTier.BRONZE, MembershipTier.SILVER, MembershipTier.GOLD};
        cachedTierNames = new String[tiers.length];
        cachedCoverageTexts = new String[tiers.length];
        cachedFeeTexts = new String[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            cachedTierNames[i] = Component.translatable(tiers[i].getTranslationKey()).getString();
            cachedCoverageTexts[i] = Component.translatable("gui.app.towing.coverage", tiers[i].getCoveragePercent()).getString();
            cachedFeeTexts[i] = Component.translatable("gui.app.towing.monthly_fee", String.format("%.0f", tiers[i].getMonthlyFee())).getString();
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.back"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 60, 20).build());
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
        guiGraphics.drawString(this.font, "\u00a70\u00a7l" + cachedMembershipHeader, leftPos + 10, topPos + 13, 0x000000, false);

        // Render tier options
        int contentY = topPos + 45;
        MembershipTier currentTier = currentMembership != null && currentMembership.isActive() ?
            currentMembership.getTier() : MembershipTier.NONE;

        renderTierOption(guiGraphics, MembershipTier.BRONZE, 0, contentY, currentTier, mouseX, mouseY);
        renderTierOption(guiGraphics, MembershipTier.SILVER, 1, contentY + TIER_ITEM_HEIGHT + 5, currentTier, mouseX, mouseY);
        renderTierOption(guiGraphics, MembershipTier.GOLD, 2, contentY + (TIER_ITEM_HEIGHT + 5) * 2, currentTier, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTierOption(GuiGraphics guiGraphics, MembershipTier tier, int tierIndex, int y, MembershipTier currentTier, int mouseX, int mouseY) {
        int x = leftPos + 10;
        int itemWidth = WIDTH - 20;

        boolean isCurrentTier = tier == currentTier;
        boolean isHovering = mouseX >= x && mouseX <= x + itemWidth &&
                            mouseY >= y && mouseY <= y + TIER_ITEM_HEIGHT;

        // Background color
        int bgColor;
        if (isCurrentTier) {
            bgColor = 0xFFE6F3FF;
        } else if (isHovering) {
            bgColor = 0xFFF5F5F5;
        } else {
            bgColor = 0xFFFFFFFF;
        }

        guiGraphics.fill(x, y, x + itemWidth, y + TIER_ITEM_HEIGHT, bgColor);

        // Border
        int borderColor = isCurrentTier ? 0xFF0066CC : 0xFFE0E0E0;
        guiGraphics.fill(x, y, x + itemWidth, y + 1, borderColor);
        guiGraphics.fill(x, y + TIER_ITEM_HEIGHT - 1, x + itemWidth, y + TIER_ITEM_HEIGHT, borderColor);

        // PERFORMANCE: Use cached strings instead of per-frame translations
        guiGraphics.drawString(this.font, "§0§l" + cachedTierNames[tierIndex], x + 5, y + 5, 0x000000, false);
        guiGraphics.drawString(this.font, "§7" + cachedCoverageTexts[tierIndex], x + 5, y + 18, 0xFF666666, false);
        guiGraphics.drawString(this.font, "§7" + cachedFeeTexts[tierIndex], x + 5, y + 30, 0xFF666666, false);

        // Status badge
        if (isCurrentTier) {
            int badgeWidth = font.width(cachedActiveText) + 10;
            int badgeX = x + itemWidth - badgeWidth - 5;
            int badgeY = y + 5;

            guiGraphics.fill(badgeX, badgeY, badgeX + badgeWidth, badgeY + 12, 0xFF00AA00);
            guiGraphics.drawString(this.font, cachedActiveText, badgeX + 5, badgeY + 2, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int contentY = topPos + 45;
            int x = leftPos + 10;
            int itemWidth = WIDTH - 20;

            MembershipTier currentTier = currentMembership != null && currentMembership.isActive() ?
                currentMembership.getTier() : MembershipTier.NONE;

            // Check click on each tier
            MembershipTier[] tiers = {MembershipTier.BRONZE, MembershipTier.SILVER, MembershipTier.GOLD};
            for (int i = 0; i < tiers.length; i++) {
                int tierY = contentY + i * (TIER_ITEM_HEIGHT + 5);

                if (mouseX >= x && mouseX <= x + itemWidth &&
                    mouseY >= tierY && mouseY <= tierY + TIER_ITEM_HEIGHT) {

                    if (tiers[i] != currentTier) {
                        // Request membership change via network packet
                        requestMembershipChange(tiers[i]);
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void requestMembershipChange(MembershipTier newTier) {
        // Send network packet to server to change membership
        de.rolandsw.schedulemc.towing.network.TowingNetworkHandler.sendToServer(
            new de.rolandsw.schedulemc.towing.network.ChangeMembershipPacket(newTier)
        );

        // Go back to parent screen
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
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
}
