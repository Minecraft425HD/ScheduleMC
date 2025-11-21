package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.menu.ShopEditorMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.UpdateShopItemsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI für Shop-Editor (Admin-Only)
 * - Items aus Inventar in Shop-Slots ziehen
 * - Preise für Items eingeben
 * - Speichern-Button
 */
@OnlyIn(Dist.CLIENT)
public class ShopEditorScreen extends AbstractContainerScreen<ShopEditorMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    private List<EditBox> priceInputFields;
    private Button saveButton;

    public ShopEditorScreen(ShopEditorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.priceInputFields = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Erstelle Preis-Eingabefelder für jedes Shop-Item
        for (int i = 0; i < ShopEditorMenu.SHOP_SLOTS; i++) {
            int row = i / 3;
            int col = i % 3;

            EditBox priceInput = new EditBox(this.font,
                x + 80 + col * 32, y + 18 + row * 18, 30, 16,
                Component.literal("Preis"));
            priceInput.setMaxLength(6);
            priceInput.setValue(String.valueOf(menu.getItemPrices()[i]));
            priceInput.setFilter(s -> s.matches("\\d*")); // Nur Zahlen

            final int slotIndex = i;
            priceInput.setResponder(value -> {
                try {
                    int price = value.isEmpty() ? 0 : Integer.parseInt(value);
                    menu.setItemPrice(slotIndex, price);
                } catch (NumberFormatException e) {
                    // Ignoriere ungültige Eingaben
                }
            });

            priceInputFields.add(priceInput);
            addRenderableWidget(priceInput);
        }

        // Speichern-Button
        saveButton = addRenderableWidget(Button.builder(Component.literal("Speichern"), button -> {
            saveShopItems();
        }).bounds(x + imageWidth - 70, y + imageHeight - 24, 60, 20).build());
    }

    /**
     * Speichert die Shop-Items zum Server
     */
    private void saveShopItems() {
        List<ItemStack> items = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();

        // Sammle Items und Preise
        for (int i = 0; i < ShopEditorMenu.SHOP_SLOTS; i++) {
            ItemStack item = menu.getShopContainer().getItem(i);
            if (!item.isEmpty()) {
                items.add(item.copy());
                prices.add(menu.getItemPrices()[i]);
            }
        }

        // Sende Packet an Server
        NPCNetworkHandler.sendToServer(new UpdateShopItemsPacket(
            menu.getEntityId(),
            items,
            prices
        ));

        // Schließe GUI
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render "Preis:" Labels
        for (int i = 0; i < 3; i++) {
            guiGraphics.drawString(this.font, "Preis:", x + 80, y + 8 + i * 18, 0x404040, false);
        }

        // Render Hinweistext
        guiGraphics.drawString(this.font, "Items aus Inventar hierher ziehen", x + 8, y + 72, 0x888888, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render Container-Texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, 222);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String title = "Shop Editor: " + menu.getCategory().getDisplayName();
        guiGraphics.drawString(this.font, title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 0x404040, false);
    }
}
