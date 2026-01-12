# Abschlepper-NPC Rechnungs-Screen - Implementierungsanleitung

## Übersicht

Der Abschlepper-NPC soll einen Shop-ähnlichen Screen bekommen, wo Spieler ihre Abschlepp-Rechnung sehen und bezahlen können.

## Ähnlich zu: Tankstellen-NPC (FuelStation)

Basiert auf:
- `ContainerFuelStation.java`
- `GuiFuelStation.java`
- `TileEntityFuelStation.java`

## Benötigte Komponenten

### 1. TowingInvoiceData Klasse

```java
package de.rolandsw.schedulemc.towing;

public class TowingInvoiceData {
    private final UUID playerId;
    private final UUID vehicleId;
    private final String towingYardPlotId;
    private final double amount;
    private final boolean paid;

    // Constructor, getters, NBT serialization
}
```

### 2. NPC Interaction Handler

In `CustomNPCEntity.java` Interaktions-Logik erweitern:

```java
if (npcType == NPCType.ABSCHLEPPER) {
    // Finde offene Rechnung für Spieler
    TowingInvoiceData invoice = TowingYardManager.getUnpaidInvoice(player.getUUID());

    if (invoice != null) {
        // Öffne Rechnungs-Screen
        player.openMenu(new SimpleMenuProvider(
            (id, playerInv, p) -> new TowingInvoiceMenu(id, playerInv, invoice),
            Component.translatable("menu.towing_invoice")
        ));
    } else {
        // Keine offenen Rechnungen
        player.displayClientMessage(
            Component.translatable("towing.no_invoices"),
            false
        );
    }
}
```

### 3. TowingInvoiceMenu (Container)

```java
package de.rolandsw.schedulemc.towing.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TowingInvoiceMenu extends AbstractContainerMenu {
    private final TowingInvoiceData invoice;
    private final Inventory playerInventory;

    public TowingInvoiceMenu(int id, Inventory playerInv, TowingInvoiceData invoice) {
        super(ModMenuTypes.TOWING_INVOICE.get(), id);
        this.invoice = invoice;
        this.playerInventory = playerInv;

        // Slot 0: Rechnung (Read-Only Display)
        addSlot(new InvoiceSlot(invoice, 0, 80, 35));

        // Player Inventory
        addPlayerInventorySlots(playerInv);
    }

    public TowingInvoiceData getInvoice() {
        return invoice;
    }

    // Slot-Klasse für Rechnung (immer Menge 1)
    private static class InvoiceSlot extends Slot {
        private final TowingInvoiceData invoice;

        public InvoiceSlot(TowingInvoiceData invoice, int index, int x, int y) {
            super(new InvoiceInventory(invoice), index, x, y);
            this.invoice = invoice;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false; // Kein Platzieren erlaubt
        }

        @Override
        public boolean mayPickup(Player player) {
            return false; // Kein Aufnehmen erlaubt
        }
    }

    // Virtuelles Inventory für die Rechnung
    private static class InvoiceInventory implements Container {
        private final TowingInvoiceData invoice;
        private final ItemStack invoiceItem;

        public InvoiceInventory(TowingInvoiceData invoice) {
            this.invoice = invoice;
            this.invoiceItem = createInvoiceItem(invoice);
        }

        private ItemStack createInvoiceItem(TowingInvoiceData invoice) {
            ItemStack stack = new ItemStack(Items.PAPER);
            stack.setCount(1); // IMMER 1
            stack.setHoverName(Component.translatable("item.towing_invoice"));

            // Lore mit Details
            CompoundTag tag = stack.getOrCreateTag();
            ListTag lore = new ListTag();
            lore.add(StringTag.valueOf("{\"text\":\"Betrag: " + String.format("%.0f€", invoice.getAmount()) + "\"}"));
            lore.add(StringTag.valueOf("{\"text\":\"Abschlepphof: " + invoice.getTowingYardPlotId() + "\"}"));
            tag.put("Lore", lore);

            return stack;
        }

        @Override
        public ItemStack getItem(int index) {
            return invoiceItem;
        }

        // ... andere Container-Methoden
    }
}
```

### 4. TowingInvoiceScreen (GUI)

```java
package de.rolandsw.schedulemc.towing.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.towing.menu.TowingInvoiceMenu;
import de.rolandsw.schedulemc.towing.network.PayTowingInvoicePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TowingInvoiceScreen extends AbstractContainerScreen<TowingInvoiceMenu> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation("schedulemc", "textures/gui/towing_invoice.png");

    private Button payButton;

    public TowingInvoiceScreen(TowingInvoiceMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();

        // Bezahlen-Button
        this.payButton = Button.builder(
            Component.translatable("towing.invoice.pay"),
            button -> payInvoice()
        ).bounds(leftPos + 60, topPos + 60, 56, 20).build();

        addRenderableWidget(payButton);
    }

    private void payInvoice() {
        // Sende Packet an Server
        TowingNetworkHandler.sendToServer(new PayTowingInvoicePacket(menu.getInvoice().getVehicleId()));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        // Betrag anzeigen
        String amountText = String.format("%.0f€", menu.getInvoice().getAmount());
        graphics.drawString(font, amountText, leftPos + 70, topPos + 20, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
```

### 5. PayTowingInvoicePacket

```java
package de.rolandsw.schedulemc.towing.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.towing.TowingYardManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PayTowingInvoicePacket {
    private final UUID vehicleId;

    public PayTowingInvoicePacket(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(vehicleId);
    }

    public static PayTowingInvoicePacket decode(FriendlyByteBuf buf) {
        return new PayTowingInvoicePacket(buf.readUUID());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, sender -> {
            TowingInvoiceData invoice = TowingYardManager.getUnpaidInvoice(sender.getUUID(), vehicleId);

            if (invoice == null) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.no_invoice"),
                    false
                );
                return;
            }

            double amount = invoice.getAmount();

            // Prüfe Geld
            if (!WalletManager.hasEnough(sender.getUUID(), amount)) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.not_enough_money"),
                    false
                );
                return;
            }

            // Bezahle
            WalletManager.withdraw(sender.getUUID(), amount);
            invoice.markAsPaid();
            TowingYardManager.save();

            sender.displayClientMessage(
                Component.translatable("towing.invoice.paid", String.format("%.0f", amount)),
                false
            );
        });
    }
}
```

### 6. Menu Type Registration

In einem ModMenuTypes oder TowingMenuTypes:

```java
public static final RegistryObject<MenuType<TowingInvoiceMenu>> TOWING_INVOICE =
    MENUS.register("towing_invoice", () ->
        IForgeMenuType.create((windowId, inv, data) -> {
            // Decode TowingInvoiceData from packet data
            TowingInvoiceData invoice = TowingInvoiceData.decode(data);
            return new TowingInvoiceMenu(windowId, inv, invoice);
        })
    );
```

### 7. Client-Side Screen Registration

```java
MenuScreens.register(ModMenuTypes.TOWING_INVOICE.get(), TowingInvoiceScreen::new);
```

## GUI Texture

Erstelle `assets/schedulemc/textures/gui/towing_invoice.png`:
- Größe: 176x166 Pixel
- Ähnlich zu Fuel Station GUI
- Ein Slot oben für Rechnung (Paper-Item)
- Anzeige des Betrags
- Bezahlen-Button

## Integration Steps

1. ✅ TowingTransaction tracking (bereits implementiert)
2. ⬜ TowingInvoiceData Klasse erstellen
3. ⬜ TowingYardManager um getUnpaidInvoice() erweitern
4. ⬜ TowingInvoiceMenu erstellen
5. ⬜ TowingInvoiceScreen erstellen
6. ⬜ PayTowingInvoicePacket erstellen
7. ⬜ Menu Type registrieren
8. ⬜ Screen registrieren
9. ⬜ NPC Interaktion für ABSCHLEPPER Typ hinzufügen
10. ⬜ GUI Texture erstellen
11. ⬜ Translations hinzufügen

## Translations (en_us.json)

```json
{
  "menu.towing_invoice": "Towing Invoice",
  "item.towing_invoice": "Towing Invoice",
  "towing.invoice.pay": "Pay Invoice",
  "towing.invoice.paid": "Invoice paid: %s€",
  "towing.no_invoices": "No pending invoices",
  "towing.error.no_invoice": "Invoice not found",
  "towing.error.not_enough_money": "Not enough money"
}
```

## Testing

1. Spawn ABSCHLEPPER NPC
2. Lass dein Fahrzeug abschleppen
3. Rechts-Klick auf ABSCHLEPPER NPC
4. Screen sollte sich öffnen mit:
   - Paper-Item (Rechnung) oben
   - Betrag angezeigt
   - "Bezahlen" Button
5. Klicke "Bezahlen"
6. Geld wird abgezogen
7. Rechnung ist bezahlt
