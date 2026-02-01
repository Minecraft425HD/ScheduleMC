package de.rolandsw.schedulemc.vehicle.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import de.rolandsw.schedulemc.vehicle.items.IVehiclePart;
import de.rolandsw.schedulemc.vehicle.net.*;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiWerkstatt extends ScreenBase<ContainerWerkstatt> {

    private static final ResourceLocation WERKSTATT_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/gui_werkstatt.png");

    // Colors
    private static final int COL_BLACK = 0xFF000000;
    private static final int COL_WHITE = 0xFFFFFFFF;
    private static final int COL_SHADOW = 0xFF555555;
    private static final int COL_BG = 0xFFC6C6C6;
    private static final int COL_CARD_BG = 0xFFAAAAAA;
    private static final int COL_CART_BG = 0xFFB8B8B8;
    private static final int COL_TAB_ACTIVE = 0xFF9090D0;
    private static final int COL_TAB_INACTIVE = 0xFF808080;

    private static final int COL_TEXT = 0x404040;
    private static final int COL_TEXT_LIGHT = 0x555555;
    private static final int COL_GREEN = 0x00AA00;
    private static final int COL_RED = 0xAA0000;
    private static final int COL_TITLE = 0xFFFFFF;
    private static final int COL_PRICE = 0x006600;
    private static final int COL_BAR_GOOD = 0xFF00CC00;
    private static final int COL_BAR_MED = 0xFFCCCC00;
    private static final int COL_BAR_BAD = 0xFFCC0000;
    private static final int COL_BAR_BG = 0xFF555555;

    private Inventory playerInv;
    private EntityGenericVehicle vehicle;
    private VehicleUtils.VehicleRenderer vehicleRenderer;

    // Tab system
    private enum Tab { OVERVIEW, SERVICE, UPGRADE, PAINT, CONTAINER }
    private Tab currentTab = Tab.OVERVIEW;

    // Shopping cart
    private final List<WerkstattCartItem> cart = new ArrayList<>();

    // Tab buttons
    private Button tabOverview, tabService, tabUpgrade, tabPaint, tabContainer;

    // Service buttons (add to cart)
    private Button btnAddRepair, btnAddBattery, btnAddOil;

    // Upgrade buttons (add to cart)
    private Button btnAddMotor, btnAddTank, btnAddTire, btnAddFender;

    // Paint buttons
    private final List<Button> paintButtons = new ArrayList<>();
    private int selectedPaintColor = -1;
    private Button btnAddPaint;

    // Container buttons (direct operations, not cart)
    private Button btnInstallItemContainer, btnRemoveItemContainer;
    private Button btnInstallFluidContainer, btnRemoveFluidContainer;

    // Cart remove buttons
    private final List<Button> cartRemoveButtons = new ArrayList<>();

    // Bottom buttons
    private Button btnCheckout, btnLeave;

    // Cart scroll offset
    private int cartScrollOffset = 0;
    private static final int CART_VISIBLE_ITEMS = 6;

    // Guard against double checkout messages
    private boolean checkoutSent = false;

    // Color translation keys (indexed by paint color ID)
    private static final String[] COLOR_KEYS = {
            "werkstatt.gui.color.white", "werkstatt.gui.color.black",
            "werkstatt.gui.color.red", "werkstatt.gui.color.blue",
            "werkstatt.gui.color.yellow"
    };

    private String tr(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    private String getColorName(int index) {
        if (index >= 0 && index < COLOR_KEYS.length) {
            return tr(COLOR_KEYS[index]);
        }
        return "?";
    }

    public GuiWerkstatt(ContainerWerkstatt containerWerkstatt, Inventory playerInv, Component title) {
        super(WERKSTATT_GUI_TEXTURE, containerWerkstatt, playerInv, title);
        this.playerInv = playerInv;
        this.vehicle = containerWerkstatt.getVehicle();
        this.vehicleRenderer = new VehicleUtils.VehicleRenderer(1.0F);

        imageWidth = 370;
        imageHeight = 280;
    }

    @Override
    protected void init() {
        super.init();
        if (vehicle == null) return;

        selectedPaintColor = vehicle.getPaintColor();

        // Tab buttons
        int tabW = 50;
        int tabH = 16;
        int tabY = topPos + 5;
        int tabX = leftPos + 5;

        tabOverview = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.tab.overview"),
                b -> switchTab(Tab.OVERVIEW))
                .bounds(tabX, tabY, tabW, tabH).build());
        tabX += tabW + 2;

        tabService = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.tab.service"),
                b -> switchTab(Tab.SERVICE))
                .bounds(tabX, tabY, tabW, tabH).build());
        tabX += tabW + 2;

        tabUpgrade = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.tab.upgrades"),
                b -> switchTab(Tab.UPGRADE))
                .bounds(tabX, tabY, tabW + 5, tabH).build());
        tabX += tabW + 7;

        tabPaint = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.tab.paint"),
                b -> switchTab(Tab.PAINT))
                .bounds(tabX, tabY, tabW, tabH).build());
        tabX += tabW + 2;

        tabContainer = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.container.tab"),
                b -> switchTab(Tab.CONTAINER))
                .bounds(tabX, tabY, tabW + 5, tabH).build());

        // Service buttons
        initServiceButtons();
        initUpgradeButtons();
        initPaintButtons();
        initContainerButtons();
        initCartRemoveButtons();
        initBottomButtons();

        updateWidgetVisibility();
    }

    private void initServiceButtons() {
        int btnW = 96;
        int btnH = 16;
        int btnX = leftPos + 100;

        btnAddRepair = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> addToCart(new WerkstattCartItem(WerkstattCartItem.Type.SERVICE_REPAIR)))
                .bounds(btnX, topPos + 68, btnW, btnH).build());

        btnAddBattery = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> addToCart(new WerkstattCartItem(WerkstattCartItem.Type.SERVICE_BATTERY)))
                .bounds(btnX, topPos + 114, btnW, btnH).build());

        btnAddOil = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> addToCart(new WerkstattCartItem(WerkstattCartItem.Type.SERVICE_OIL)))
                .bounds(btnX, topPos + 160, btnW, btnH).build());
    }

    private void initUpgradeButtons() {
        int btnW = 96;
        int btnH = 16;
        int btnX = leftPos + 100;
        int spacing = 48;

        int motorLevel = getCurrentMotorLevel();
        btnAddMotor = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> addToCart(new WerkstattCartItem(WerkstattCartItem.Type.UPGRADE_MOTOR, motorLevel + 1)))
                .bounds(btnX, topPos + 68, btnW, btnH).build());

        int tankLevel = getCurrentTankLevel();
        btnAddTank = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> addToCart(new WerkstattCartItem(WerkstattCartItem.Type.UPGRADE_TANK, tankLevel + 1)))
                .bounds(btnX, topPos + 68 + spacing, btnW, btnH).build());

        int tireIdx = getCurrentTireIndex();
        btnAddTire = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> addToCart(new WerkstattCartItem(WerkstattCartItem.Type.UPGRADE_TIRE, tireIdx + 1)))
                .bounds(btnX, topPos + 68 + spacing * 2, btnW, btnH).build());

        int fenderLevel = getCurrentFenderLevel();
        btnAddFender = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> addToCart(new WerkstattCartItem(WerkstattCartItem.Type.UPGRADE_FENDER, fenderLevel + 1)))
                .bounds(btnX, topPos + 68 + spacing * 3, btnW, btnH).build());
    }

    private void initPaintButtons() {
        paintButtons.clear();
        int size = 30;
        int gap = 8;
        int startX = leftPos + 12;
        int startY = topPos + 60;

        for (int i = 0; i < 5; i++) {
            final int colorIdx = i;
            int col = i % 3;
            int row = i / 3;
            Button btn = addRenderableWidget(Button.builder(
                    Component.literal(""),
                    b -> { selectedPaintColor = colorIdx; updateWidgetVisibility(); })
                    .bounds(startX + col * (size + gap), startY + row * (size + gap), size, size).build());
            paintButtons.add(btn);
        }

        btnAddPaint = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.add_to_cart"),
                b -> {
                    if (selectedPaintColor >= 0 && selectedPaintColor != vehicle.getPaintColor()) {
                        // Remove existing paint item from cart
                        cart.removeIf(item -> item.getType() == WerkstattCartItem.Type.PAINT_CHANGE);
                        addToCart(new WerkstattCartItem(WerkstattCartItem.Type.PAINT_CHANGE, selectedPaintColor));
                    }
                })
                .bounds(leftPos + 8, topPos + 178, 96, 16).build());
    }

    private void initContainerButtons() {
        int btnW = 96;
        int btnH = 16;
        int btnX = leftPos + 100;

        btnInstallItemContainer = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.container.install"),
                b -> sendContainerOperation(MessageContainerOperation.Operation.INSTALL_ITEM))
                .bounds(btnX, topPos + 80, btnW, btnH).build());

        btnRemoveItemContainer = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.container.remove"),
                b -> sendContainerOperation(MessageContainerOperation.Operation.REMOVE_ITEM))
                .bounds(btnX, topPos + 80, btnW, btnH).build());

        btnInstallFluidContainer = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.container.install"),
                b -> sendContainerOperation(MessageContainerOperation.Operation.INSTALL_FLUID))
                .bounds(btnX, topPos + 150, btnW, btnH).build());

        btnRemoveFluidContainer = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.container.remove"),
                b -> sendContainerOperation(MessageContainerOperation.Operation.REMOVE_FLUID))
                .bounds(btnX, topPos + 150, btnW, btnH).build());
    }

    private void sendContainerOperation(MessageContainerOperation.Operation operation) {
        if (vehicle == null || minecraft == null) return;
        Main.SIMPLE_CHANNEL.sendToServer(new MessageContainerOperation(vehicle.getId(), operation));
    }

    private void initCartRemoveButtons() {
        cartRemoveButtons.forEach(this::removeWidget);
        cartRemoveButtons.clear();

        int btnW = 12;
        int btnH = 10;
        int cartX = leftPos + imageWidth - 160 + 135;
        int cartItemY = topPos + 40;

        int visibleCount = Math.min(cart.size() - cartScrollOffset, CART_VISIBLE_ITEMS);
        for (int i = 0; i < visibleCount; i++) {
            final int idx = i + cartScrollOffset;
            Button removeBtn = addRenderableWidget(Button.builder(
                    Component.literal("\u00D7"),
                    b -> {
                        if (idx < cart.size()) {
                            cart.remove(idx);
                            rebuildCartButtons();
                            updateCheckoutButton();
                        }
                    })
                    .bounds(cartX, cartItemY + i * 14, btnW, btnH).build());
            cartRemoveButtons.add(removeBtn);
        }
    }

    private void rebuildCartButtons() {
        initCartRemoveButtons();
        updateWidgetVisibility();
    }

    private void initBottomButtons() {
        int btnW = 160;
        int btnH = 20;
        int centerX = leftPos + (imageWidth - 160) / 2 - 80;

        btnCheckout = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.checkout"),
                b -> { sendCheckout(); onClose(); })
                .bounds(leftPos + 10, topPos + imageHeight - 28, btnW, btnH).build());

        btnLeave = addRenderableWidget(Button.builder(
                Component.translatable("werkstatt.btn.leave"),
                b -> onClose())
                .bounds(leftPos + 180, topPos + imageHeight - 28, btnW, btnH).build());

        updateCheckoutButton();
    }

    private void updateCheckoutButton() {
        if (btnCheckout == null) return;
        double total = calculateCartTotal();
        if (cart.isEmpty()) {
            btnCheckout.active = false;
            btnCheckout.setMessage(Component.translatable("werkstatt.btn.checkout_empty"));
        } else {
            btnCheckout.active = true;
            btnCheckout.setMessage(Component.literal(tr("werkstatt.gui.checkout_total", total)));
        }
    }

    // === Tab Switching ===

    private void switchTab(Tab newTab) {
        currentTab = newTab;
        updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        boolean isService = currentTab == Tab.SERVICE;
        boolean isUpgrade = currentTab == Tab.UPGRADE;
        boolean isPaint = currentTab == Tab.PAINT;
        boolean isContainer = currentTab == Tab.CONTAINER;
        boolean isTruck = isTruckVehicle();

        // Container tab only visible for trucks
        if (tabContainer != null) tabContainer.visible = isTruck;

        // Service buttons
        if (btnAddRepair != null) btnAddRepair.visible = isService && !isInCart(WerkstattCartItem.Type.SERVICE_REPAIR);
        if (btnAddBattery != null) btnAddBattery.visible = isService && !isInCart(WerkstattCartItem.Type.SERVICE_BATTERY);
        if (btnAddOil != null) btnAddOil.visible = isService && !isInCart(WerkstattCartItem.Type.SERVICE_OIL);

        // Upgrade buttons
        if (btnAddMotor != null) btnAddMotor.visible = isUpgrade && getCurrentMotorLevel() < 3 && !isInCart(WerkstattCartItem.Type.UPGRADE_MOTOR);
        if (btnAddTank != null) btnAddTank.visible = isUpgrade && getCurrentTankLevel() < 3 && !isInCart(WerkstattCartItem.Type.UPGRADE_TANK);
        if (btnAddTire != null) btnAddTire.visible = isUpgrade && getCurrentTireIndex() < 2 && !isInCart(WerkstattCartItem.Type.UPGRADE_TIRE);
        if (btnAddFender != null) btnAddFender.visible = isUpgrade && getCurrentFenderLevel() < 3 && !isInCart(WerkstattCartItem.Type.UPGRADE_FENDER);

        // Paint buttons
        for (Button pb : paintButtons) pb.visible = isPaint;
        if (btnAddPaint != null) btnAddPaint.visible = isPaint
                && selectedPaintColor >= 0
                && selectedPaintColor != vehicle.getPaintColor()
                && !isInCart(WerkstattCartItem.Type.PAINT_CHANGE);

        // Container buttons
        boolean hasItemContainer = vehicle != null && vehicle.getPartByClass(PartContainer.class) != null;
        boolean hasFluidContainer = vehicle != null && vehicle.getPartByClass(PartTankContainer.class) != null;
        if (btnInstallItemContainer != null) btnInstallItemContainer.visible = isContainer && isTruck && !hasItemContainer && !hasFluidContainer;
        if (btnRemoveItemContainer != null) btnRemoveItemContainer.visible = isContainer && isTruck && hasItemContainer;
        if (btnInstallFluidContainer != null) btnInstallFluidContainer.visible = isContainer && isTruck && !hasFluidContainer && !hasItemContainer;
        if (btnRemoveFluidContainer != null) btnRemoveFluidContainer.visible = isContainer && isTruck && hasFluidContainer;

        // Cart remove buttons always visible
        for (Button rb : cartRemoveButtons) rb.visible = true;
    }

    private boolean isTruckVehicle() {
        if (vehicle == null) return false;
        PartBody body = vehicle.getPartByClass(PartBody.class);
        return body instanceof PartTruckChassis;
    }

    // === Cart Management ===

    private void addToCart(WerkstattCartItem item) {
        // Don't add duplicates of same type
        if (isInCart(item.getType())) return;
        cart.add(item);
        rebuildCartButtons();
        updateCheckoutButton();
        updateWidgetVisibility();
    }

    private boolean isInCart(WerkstattCartItem.Type type) {
        return cart.stream().anyMatch(item -> item.getType() == type);
    }

    private double calculateCartTotal() {
        if (vehicle == null) return 0;
        double inspectionFee = cart.isEmpty() ? 0 : ModConfigHandler.COMMON.WERKSTATT_BASE_INSPECTION_FEE.get();
        double itemsTotal = 0;
        for (WerkstattCartItem item : cart) {
            itemsTotal += item.calculateCost(vehicle);
        }
        return inspectionFee + itemsTotal;
    }

    // === Actions ===

    private void sendCheckout() {
        if (checkoutSent || vehicle == null || minecraft == null || minecraft.player == null || cart.isEmpty()) return;
        checkoutSent = true;
        Main.SIMPLE_CHANNEL.sendToServer(new MessageWerkstattCheckout(
                minecraft.player.getUUID(),
                vehicle.getUUID(),
                new ArrayList<>(cart)
        ));
    }

    private void leaveWithoutPaying() {
        if (checkoutSent || vehicle == null || minecraft == null || minecraft.player == null) return;
        checkoutSent = true;
        // Send empty checkout = just unlock and leave
        Main.SIMPLE_CHANNEL.sendToServer(new MessageWerkstattCheckout(
                minecraft.player.getUUID(),
                vehicle.getUUID(),
                new ArrayList<>()
        ));
    }

    // === Tick-based updates ===

    @Override
    protected void containerTick() {
        super.containerTick();
        // Refresh container tab buttons after server processes install/remove
        if (currentTab == Tab.CONTAINER) {
            updateWidgetVisibility();
        }
    }

    // === ESC/Close behavior ===

    @Override
    public void onClose() {
        leaveWithoutPaying();
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            leaveWithoutPaying();
            super.onClose();
            return true;
        }
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true; // Block inventory key while in werkstatt
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // === Rendering ===

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;

        // Main frame
        drawFrame(g, x, y, w, h, COL_BG);

        // Tab bar background
        drawInsetPanel(g, x + 3, y + 3, w - 6, 18, 0xFF8B8B8B);

        // Left content panel
        int contentW = w - 165;
        drawInsetPanel(g, x + 3, y + 24, contentW, h - 56, COL_CARD_BG);

        // Right cart panel
        int cartX = x + contentW + 6;
        int cartW = w - contentW - 9;
        drawInsetPanel(g, cartX, y + 24, cartW, h - 56, COL_CART_BG);

        // Bottom bar
        drawInsetPanel(g, x + 3, y + h - 30, w - 6, 27, 0xFF9B9B9B);
    }

    private void drawFrame(GuiGraphics g, int x, int y, int w, int h, int bgColor) {
        g.fill(x, y, x + w, y + 1, COL_BLACK);
        g.fill(x, y + h - 1, x + w, y + h, COL_BLACK);
        g.fill(x, y, x + 1, y + h, COL_BLACK);
        g.fill(x + w - 1, y, x + w, y + h, COL_BLACK);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, COL_WHITE);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, COL_WHITE);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, COL_SHADOW);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, COL_SHADOW);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, bgColor);
    }

    private void drawInsetPanel(GuiGraphics g, int x, int y, int w, int h, int bgColor) {
        g.fill(x, y, x + w, y + 1, COL_SHADOW);
        g.fill(x, y, x + 1, y + h, COL_SHADOW);
        g.fill(x, y + h - 1, x + w, y + h, COL_WHITE);
        g.fill(x + w - 1, y, x + w, y + h, COL_WHITE);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        if (vehicle == null || vehicle.isRemoved()) {
            g.drawString(font, tr("werkstatt.gui.no_vehicle"), 10, 30, COL_RED, false);
            return;
        }

        // Render tab content (left side)
        switch (currentTab) {
            case OVERVIEW -> renderOverviewTab(g);
            case SERVICE -> renderServiceTab(g);
            case UPGRADE -> renderUpgradeTab(g);
            case PAINT -> renderPaintTab(g);
            case CONTAINER -> renderContainerTab(g);
        }

        // Render cart (right side)
        renderCart(g);
    }

    // === Tab: Overview ===

    private void renderOverviewTab(GuiGraphics g) {
        int x = 8;
        int y = 28;

        // Vehicle name
        g.drawString(font, vehicle.getDisplayName().getString(), x, y, COL_TEXT, false);
        y += 14;

        // 3D vehicle preview
        vehicleRenderer.tick();
        if (vehicle != null) {
            vehicleRenderer.render(g, vehicle, 75, y + 35, 22);
        }
        y += 75;

        // Odometer
        long odo = vehicle.getOdometer();
        String odoFormatted = String.format("%,d", odo).replace(",", tr("werkstatt.gui.thousand_sep"));
        g.drawString(font, tr("werkstatt.gui.odometer", odoFormatted), x, y, COL_TEXT, false);
        y += 14;

        // Status bars
        g.drawString(font, tr("werkstatt.gui.condition_header"), x, y, COL_TEXT, false);
        y += 12;
        renderBar(g, x, y, tr("werkstatt.gui.damage"), 100 - getDamagePercent());
        y += 16;
        renderBar(g, x, y, tr("werkstatt.gui.battery_label"), getBatteryPercent());
        y += 18;

        // Current parts
        g.drawString(font, tr("werkstatt.gui.parts_header"), x, y, COL_TEXT, false);
        y += 12;

        g.drawString(font, tr("werkstatt.gui.part_motor", getMotorName()), x, y, COL_TEXT_LIGHT, false);
        y += 10;
        g.drawString(font, tr("werkstatt.gui.part_tank", getTankName()), x, y, COL_TEXT_LIGHT, false);
        y += 10;
        g.drawString(font, tr("werkstatt.gui.part_tire", getTireName()), x, y, COL_TEXT_LIGHT, false);
        y += 10;
        g.drawString(font, tr("werkstatt.gui.part_fender", getFenderName()), x, y, COL_TEXT_LIGHT, false);
        y += 10;

        g.drawString(font, tr("werkstatt.gui.part_paint", getColorName(vehicle.getPaintColor())), x, y, COL_TEXT_LIGHT, false);
    }

    // === Tab: Service ===

    private void renderServiceTab(GuiGraphics g) {
        int x = 8;
        int y = 28;

        g.drawString(font, tr("werkstatt.gui.service_title"), x, y, COL_TEXT, false);
        y += 16;

        // Repair card
        drawServiceCard(g, x, y, tr("werkstatt.gui.service.repair"),
                tr("werkstatt.gui.service.repair_status", getDamagePercent()),
                getDamagePercent() > 0 ? getDamagePercent() * ModConfigHandler.COMMON.WERKSTATT_REPAIR_COST_PER_PERCENT.get() : 0,
                isInCart(WerkstattCartItem.Type.SERVICE_REPAIR));
        y += 46;

        // Battery card
        drawServiceCard(g, x, y, tr("werkstatt.gui.service.battery"),
                tr("werkstatt.gui.service.battery_status", getBatteryPercent()),
                getBatteryPercent() < 100 ? (100 - getBatteryPercent()) * ModConfigHandler.COMMON.WERKSTATT_BATTERY_COST_PER_PERCENT.get() : 0,
                isInCart(WerkstattCartItem.Type.SERVICE_BATTERY));
        y += 46;

        // Oil card
        drawServiceCard(g, x, y, tr("werkstatt.gui.service.oil"),
                tr("werkstatt.gui.service.oil_hint"),
                ModConfigHandler.COMMON.WERKSTATT_OIL_CHANGE_COST.get(),
                isInCart(WerkstattCartItem.Type.SERVICE_OIL));
    }

    private void drawServiceCard(GuiGraphics g, int x, int y, String title, String subtitle, double cost, boolean inCart) {
        // Card background
        g.fill(x, y, x + 190, y + 40, 0xFF999999);
        g.fill(x + 1, y + 1, x + 189, y + 39, 0xFFBBBBBB);

        g.drawString(font, title, x + 4, y + 3, COL_TEXT, false);
        g.drawString(font, subtitle, x + 4, y + 14, COL_TEXT_LIGHT, false);

        if (inCart) {
            g.drawString(font, tr("werkstatt.gui.in_order"), x + 4, y + 26, COL_GREEN, false);
        } else {
            g.drawString(font, tr("werkstatt.gui.price_format", cost), x + 4, y + 26, COL_PRICE, false);
        }
    }

    // === Tab: Upgrades ===

    private void renderUpgradeTab(GuiGraphics g) {
        int x = 8;
        int y = 28;
        int spacing = 48;

        g.drawString(font, tr("werkstatt.gui.upgrade_title"), x, y, COL_TEXT, false);
        y += 16;

        // Motor
        int motorLevel = getCurrentMotorLevel();
        drawUpgradeCard(g, x, y, tr("werkstatt.gui.upgrade.motor"),
                tr("werkstatt.gui.upgrade.current", getMotorName(), motorLevel),
                motorLevel < 3 ? tr("werkstatt.gui.upgrade.next", getMotorNameByLevel(motorLevel + 1), motorLevel + 1) : null,
                motorLevel < 3 ? getMotorUpgradeCost(motorLevel) : -1,
                motorLevel >= 3,
                isInCart(WerkstattCartItem.Type.UPGRADE_MOTOR));
        y += spacing;

        // Tank
        int tankLevel = getCurrentTankLevel();
        drawUpgradeCard(g, x, y, tr("werkstatt.gui.upgrade.tank"),
                tr("werkstatt.gui.upgrade.current", getTankName(), tankLevel),
                tankLevel < 3 ? tr("werkstatt.gui.upgrade.next", getTankNameByLevel(tankLevel + 1), tankLevel + 1) : null,
                tankLevel < 3 ? getTankUpgradeCost(tankLevel) : -1,
                tankLevel >= 3,
                isInCart(WerkstattCartItem.Type.UPGRADE_TANK));
        y += spacing;

        // Tire
        int tireIdx = getCurrentTireIndex();
        drawUpgradeCard(g, x, y, tr("werkstatt.gui.upgrade.tire"),
                tr("werkstatt.gui.upgrade.current", getTireName(), tireIdx + 1),
                tireIdx < 2 ? tr("werkstatt.gui.upgrade.next", getTireNameByIndex(tireIdx + 1), tireIdx + 2) : null,
                tireIdx < 2 ? ModConfigHandler.COMMON.WERKSTATT_TIRE_UPGRADE_COST.get() : -1,
                tireIdx >= 2,
                isInCart(WerkstattCartItem.Type.UPGRADE_TIRE));
        y += spacing;

        // Fender
        int fenderLevel = getCurrentFenderLevel();
        drawUpgradeCard(g, x, y, tr("werkstatt.gui.upgrade.fender"),
                tr("werkstatt.gui.upgrade.current", getFenderName(), fenderLevel),
                fenderLevel < 3 ? tr("werkstatt.gui.upgrade.next", getFenderNameByLevel(fenderLevel + 1), fenderLevel + 1) : null,
                fenderLevel < 3 ? getFenderUpgradeCost(fenderLevel) : -1,
                fenderLevel >= 3,
                isInCart(WerkstattCartItem.Type.UPGRADE_FENDER));
    }

    private void drawUpgradeCard(GuiGraphics g, int x, int y, String title, String current, String next, double cost, boolean isMax, boolean inCart) {
        g.fill(x, y, x + 190, y + 42, 0xFF999999);
        g.fill(x + 1, y + 1, x + 189, y + 41, 0xFFBBBBBB);

        if (isMax) {
            g.drawString(font, title + "  " + tr("werkstatt.gui.upgrade.max"), x + 4, y + 3, COL_GREEN, false);
            g.drawString(font, current, x + 4, y + 14, COL_TEXT_LIGHT, false);
        } else {
            g.drawString(font, title, x + 4, y + 3, COL_TEXT, false);
            g.drawString(font, current, x + 4, y + 14, COL_TEXT_LIGHT, false);
            if (next != null) {
                g.drawString(font, next, x + 4, y + 24, COL_TEXT_LIGHT, false);
            }
            if (inCart) {
                g.drawString(font, tr("werkstatt.gui.in_order"), x + 100, y + 3, COL_GREEN, false);
            }
        }
    }

    // === Tab: Paint ===

    private void renderPaintTab(GuiGraphics g) {
        int x = 8;
        int y = 28;

        g.drawString(font, tr("werkstatt.gui.paint_title"), x, y, COL_TEXT, false);
        y += 14;

        int currentColor = vehicle.getPaintColor();
        g.drawString(font, tr("werkstatt.gui.paint.current", getColorName(currentColor)), x, y, COL_TEXT_LIGHT, false);
        y += 18;

        // Paint color swatches (rendered over the buttons)
        int[] colorHex = {0xFFFFFF, 0x000000, 0xFF0000, 0x0000FF, 0xFFFF00};
        int size = 30;
        int gap = 8;
        int startX = 12;
        int startY = y;

        for (int i = 0; i < 5 && i < paintButtons.size(); i++) {
            int col = i % 3;
            int row = i / 3;
            int bx = startX + col * (size + gap);
            int by = startY + row * (size + gap);

            // Color swatch
            g.fill(bx, by, bx + size, by + size, 0xFF000000 | colorHex[i]);

            // Selection border
            if (selectedPaintColor == i) {
                g.fill(bx - 2, by - 2, bx + size + 2, by, COL_WHITE);
                g.fill(bx - 2, by + size, bx + size + 2, by + size + 2, COL_WHITE);
                g.fill(bx - 2, by, bx, by + size, COL_WHITE);
                g.fill(bx + size, by, bx + size + 2, by + size, COL_WHITE);
            }
        }

        // Cost info below swatches
        int infoY = startY + 2 * (size + gap) + 5;
        if (selectedPaintColor >= 0 && selectedPaintColor < COLOR_KEYS.length) {
            g.drawString(font, tr("werkstatt.gui.paint.selection", getColorName(selectedPaintColor)), x, infoY, COL_TEXT, false);
            if (selectedPaintColor == vehicle.getPaintColor()) {
                g.drawString(font, tr("werkstatt.gui.paint.same_color"), x, infoY + 12, COL_TEXT_LIGHT, false);
            } else {
                g.drawString(font, tr("werkstatt.gui.paint.cost", ModConfigHandler.COMMON.WERKSTATT_PAINT_CHANGE_COST.get()), x, infoY + 12, COL_PRICE, false);
                if (isInCart(WerkstattCartItem.Type.PAINT_CHANGE)) {
                    g.drawString(font, tr("werkstatt.gui.in_order"), x, infoY + 24, COL_GREEN, false);
                }
            }
        }
    }

    // === Tab: Container ===

    private void renderContainerTab(GuiGraphics g) {
        int x = 8;
        int y = 28;

        g.drawString(font, tr("werkstatt.container.tab"), x, y, COL_TEXT, false);
        y += 16;

        if (!isTruckVehicle()) {
            g.drawString(font, tr("werkstatt.container.truck_only"), x, y, COL_RED, false);
            return;
        }

        boolean hasItemContainer = vehicle.getPartByClass(PartContainer.class) != null;
        boolean hasFluidContainer = vehicle.getPartByClass(PartTankContainer.class) != null;

        // Item Container card
        drawContainerCard(g, x, y,
                tr("werkstatt.container.item_container"),
                tr("werkstatt.container.item_slots", "12"),
                hasItemContainer);
        y += 70;

        // Fluid Container card
        drawContainerCard(g, x, y,
                tr("werkstatt.container.fluid_container"),
                tr("werkstatt.container.fluid_capacity", "100"),
                hasFluidContainer);
    }

    private void drawContainerCard(GuiGraphics g, int x, int y, String title, String info, boolean installed) {
        g.fill(x, y, x + 190, y + 60, 0xFF999999);
        g.fill(x + 1, y + 1, x + 189, y + 59, 0xFFBBBBBB);

        g.drawString(font, title, x + 4, y + 3, COL_TEXT, false);
        g.drawString(font, info, x + 4, y + 14, COL_TEXT_LIGHT, false);

        if (installed) {
            g.drawString(font, tr("werkstatt.container.status_installed"), x + 4, y + 26, COL_GREEN, false);
        } else {
            g.drawString(font, tr("werkstatt.container.status_not_installed"), x + 4, y + 26, COL_TEXT_LIGHT, false);
            g.drawString(font, tr("werkstatt.container.cost_free"), x + 4, y + 38, COL_PRICE, false);
        }
    }

    // === Cart Panel ===

    private void renderCart(GuiGraphics g) {
        int cartX = imageWidth - 157;
        int y = 27;

        g.drawString(font, tr("werkstatt.gui.cart_title"), cartX, y, COL_TEXT, false);
        y += 13;

        if (cart.isEmpty()) {
            g.drawString(font, tr("werkstatt.gui.cart.empty"), cartX, y, COL_TEXT_LIGHT, false);
        } else {
            // Cart items
            int visibleCount = Math.min(cart.size() - cartScrollOffset, CART_VISIBLE_ITEMS);
            for (int i = 0; i < visibleCount; i++) {
                WerkstattCartItem item = cart.get(i + cartScrollOffset);
                String name = getCartItemDisplayName(item);
                double cost = item.calculateCost(vehicle);
                g.drawString(font, name, cartX, y, COL_TEXT, false);
                g.drawString(font, tr("werkstatt.gui.price_format_short", cost), cartX + 100, y, COL_PRICE, false);
                y += 14;
            }

            if (cart.size() > CART_VISIBLE_ITEMS) {
                g.drawString(font, "...", cartX, y, COL_TEXT_LIGHT, false);
                y += 10;
            }
        }

        // Separator and totals
        y = imageHeight - 95;
        g.fill(cartX, y, cartX + 148, y + 1, COL_SHADOW);
        y += 4;

        if (!cart.isEmpty()) {
            double inspectionFee = ModConfigHandler.COMMON.WERKSTATT_BASE_INSPECTION_FEE.get();
            double itemsTotal = 0;
            for (WerkstattCartItem item : cart) {
                itemsTotal += item.calculateCost(vehicle);
            }
            double total = inspectionFee + itemsTotal;

            g.drawString(font, tr("werkstatt.gui.cart.inspection"), cartX, y, COL_TEXT_LIGHT, false);
            g.drawString(font, tr("werkstatt.gui.price_format", inspectionFee), cartX + 90, y, COL_TEXT_LIGHT, false);
            y += 11;

            g.drawString(font, tr("werkstatt.gui.cart.services"), cartX, y, COL_TEXT_LIGHT, false);
            g.drawString(font, tr("werkstatt.gui.price_format", itemsTotal), cartX + 90, y, COL_TEXT_LIGHT, false);
            y += 13;

            // Total line
            g.fill(cartX, y - 2, cartX + 148, y - 1, COL_TEXT);
            g.drawString(font, tr("werkstatt.gui.cart.total"), cartX, y, COL_TEXT, false);
            g.drawString(font, tr("werkstatt.gui.price_format", total), cartX + 90, y, COL_TEXT, false);
            y += 16;

            // Balance info
            if (minecraft != null && minecraft.player != null) {
                double balance = EconomyManager.getBalance(minecraft.player.getUUID());
                double afterPayment = balance - total;

                g.drawString(font, tr("werkstatt.gui.cart.balance"), cartX, y, COL_TEXT_LIGHT, false);
                g.drawString(font, tr("werkstatt.gui.price_format", balance), cartX + 90, y, COL_TEXT_LIGHT, false);
                y += 11;

                g.drawString(font, tr("werkstatt.gui.cart.after"), cartX, y, COL_TEXT_LIGHT, false);
                int balColor = afterPayment >= 0 ? COL_GREEN : COL_RED;
                g.drawString(font, tr("werkstatt.gui.price_format", afterPayment), cartX + 90, y, balColor, false);
            }
        }
    }

    private String getCartItemDisplayName(WerkstattCartItem item) {
        return switch (item.getType()) {
            case SERVICE_REPAIR -> tr("werkstatt.gui.cart.item.repair");
            case SERVICE_BATTERY -> tr("werkstatt.gui.cart.item.battery");
            case SERVICE_OIL -> tr("werkstatt.gui.cart.item.oil");
            case UPGRADE_MOTOR -> tr("werkstatt.gui.cart.item.motor", item.getValue());
            case UPGRADE_TANK -> tr("werkstatt.gui.cart.item.tank", item.getValue());
            case UPGRADE_TIRE -> tr("werkstatt.gui.cart.item.tire", item.getValue() + 1);
            case UPGRADE_FENDER -> tr("werkstatt.gui.cart.item.fender", item.getValue());
            case PAINT_CHANGE -> tr("werkstatt.gui.cart.item.paint");
        };
    }

    // === Helper rendering ===

    private void renderBar(GuiGraphics g, int x, int y, String label, float percent) {
        percent = Math.max(0, Math.min(100, percent));
        g.drawString(font, label + ":", x, y, COL_TEXT, false);

        int barX = x;
        int barY = y + 10;
        int barW = 130;
        int barH = 5;

        g.fill(barX, barY, barX + barW, barY + barH, COL_BAR_BG);
        int fillW = (int) (barW * (percent / 100f));
        int barColor = percent > 75 ? COL_BAR_GOOD : percent > 40 ? COL_BAR_MED : COL_BAR_BAD;
        g.fill(barX, barY, barX + fillW, barY + barH, barColor);

        g.drawString(font, tr("werkstatt.gui.percent_format", percent), barX + barW + 4, y, COL_TEXT_LIGHT, false);
    }

    // === Vehicle Data Helpers ===

    public float getBatteryPercent() {
        if (vehicle == null) return 0;
        return MathUtils.round(vehicle.getBatteryComponent().getBatteryPercentage() * 100F, 1);
    }

    public float getDamagePercent() {
        if (vehicle == null) return 0;
        return MathUtils.round(Math.min(vehicle.getDamageComponent().getDamage(), 100), 1);
    }

    private int getCurrentMotorLevel() {
        if (vehicle == null) return 1;
        Container partInv = vehicle.getInventoryComponent().getPartInventory();
        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IVehiclePart partItem) {
                Part part = partItem.getPart(stack);
                if (part instanceof PartEngine) {
                    if (part == PartRegistry.PERFORMANCE_2_MOTOR) return 3;
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
                        return 0;
                    } else {
                        if (part == PartRegistry.PREMIUM_TIRE) return 2;
                        if (part == PartRegistry.SPORT_TIRE) return 1;
                        return 0;
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

    // === Name Helpers ===

    private String getMotorName() {
        return getMotorNameByLevel(getCurrentMotorLevel());
    }

    private String getMotorNameByLevel(int level) {
        return switch (level) {
            case 1 -> tr("werkstatt.gui.motor.normal");
            case 2 -> tr("werkstatt.gui.motor.performance");
            case 3 -> tr("werkstatt.gui.motor.performance2");
            default -> "?";
        };
    }

    private String getTankName() {
        return getTankNameByLevel(getCurrentTankLevel());
    }

    private String getTankNameByLevel(int level) {
        return switch (level) {
            case 1 -> tr("werkstatt.gui.tank.11l");
            case 2 -> tr("werkstatt.gui.tank.15l");
            case 3 -> tr("werkstatt.gui.tank.20l");
            default -> "?";
        };
    }

    private String getTireName() {
        return getTireNameByIndex(getCurrentTireIndex());
    }

    private String getTireNameByIndex(int index) {
        PartBody body = vehicle != null ? vehicle.getPartByClass(PartBody.class) : null;
        boolean isTruck = body != null && (body.getTranslationKey().contains("transporter")
                || body.getTranslationKey().contains("delivery"));
        if (isTruck) {
            return switch (index) {
                case 0 -> tr("werkstatt.gui.tire.offroad");
                case 1 -> tr("werkstatt.gui.tire.allterrain");
                case 2 -> tr("werkstatt.gui.tire.heavyduty");
                default -> "?";
            };
        } else {
            return switch (index) {
                case 0 -> tr("werkstatt.gui.tire.standard");
                case 1 -> tr("werkstatt.gui.tire.sport");
                case 2 -> tr("werkstatt.gui.tire.premium");
                default -> "?";
            };
        }
    }

    private String getFenderName() {
        return getFenderNameByLevel(getCurrentFenderLevel());
    }

    private String getFenderNameByLevel(int level) {
        return switch (level) {
            case 1 -> tr("werkstatt.gui.fender.basic");
            case 2 -> tr("werkstatt.gui.fender.chrome");
            case 3 -> tr("werkstatt.gui.fender.sport");
            default -> "?";
        };
    }

    // === Cost Helpers ===

    private double getMotorUpgradeCost(int currentLevel) {
        return currentLevel == 1 ? ModConfigHandler.COMMON.WERKSTATT_MOTOR_UPGRADE_COST_LVL2.get()
                : ModConfigHandler.COMMON.WERKSTATT_MOTOR_UPGRADE_COST_LVL3.get();
    }

    private double getTankUpgradeCost(int currentLevel) {
        return currentLevel == 1 ? ModConfigHandler.COMMON.WERKSTATT_TANK_UPGRADE_COST_LVL2.get()
                : ModConfigHandler.COMMON.WERKSTATT_TANK_UPGRADE_COST_LVL3.get();
    }

    private double getFenderUpgradeCost(int currentLevel) {
        return currentLevel == 1 ? ModConfigHandler.COMMON.WERKSTATT_FENDER_UPGRADE_COST_LVL2.get()
                : ModConfigHandler.COMMON.WERKSTATT_FENDER_UPGRADE_COST_LVL3.get();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll cart
        int cartScreenX = leftPos + imageWidth - 160;
        if (mouseX >= cartScreenX) {
            if (delta > 0 && cartScrollOffset > 0) {
                cartScrollOffset--;
                rebuildCartButtons();
            } else if (delta < 0 && cartScrollOffset < cart.size() - CART_VISIBLE_ITEMS) {
                cartScrollOffset++;
                rebuildCartButtons();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
