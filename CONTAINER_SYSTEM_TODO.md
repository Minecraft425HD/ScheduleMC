# 🚚 Container System Implementation - TODO

## ✅ FERTIG GESTELLT (Part 1 & 2)

### **Config-System** ✅
- Chassis-spezifische Inventory-Slots (Limousine: 4, Van: 6, Truck: 0, SUV: 6, Sport: 3)
- Container-Kapazitäten (Item: 12 Slots, Fluid: 100L)
- Reinstallations-Kosten (7500€, konfigurierbar)

### **Part-System** ✅
- `PartBody.getBaseInventorySize()` - Abstrakte Methode
- Alle 5 Chassis implementieren config-backed Slot-Methode
- `PartContainer.canMountOn()` - Truck-only Validation (12 Slots)
- `PartTankContainer.canMountOn()` - Truck-only Validation (100L)

### **Inventory-System** ✅
- `EntityGenericVehicle.checkInitializing()` - Replacement-Modus Logik
- Container ersetzt Base-Inventar bei Trucks
- Andere Fahrzeuge: Nur Base-Inventar

### **NBT Container-Tracking** ✅
- `hasHadItemContainer` Flag (für Kosten-System)
- `hasHadFluidContainer` Flag (für Kosten-System)
- Getter/Setter in EntityGenericVehicle
- NBT Serialization (save/load)

### **Language Files** ✅
- Deutsche Übersetzungen (de_de.json)
- Englische Übersetzungen (en_us.json)

---

## 🚧 NOCH ZU IMPLEMENTIEREN

### **1. Garage GUI - Container Tab**

#### **1.1 Tab-Enum erweitern** (`GuiGarage.java`)
```java
private enum Tab { REPAIR, UPGRADE, CONTAINER }
```

#### **1.2 Container-Tab Button** (nur bei Trucks)
```java
// In init() Methode nach upgradeTabButton
if (vehicle.getChassis() instanceof PartTruckChassis) {
    containerTabButton = addRenderableWidget(Button.builder(
        Component.translatable("garage.container.tab"),
        button -> switchTab(Tab.CONTAINER))
        .bounds(leftPos + 180, tabY, tabWidth, tabHeight)
        .build()
    );
}
```

#### **1.3 Container-Installations-Buttons**
```java
// Item Container
private Button installItemContainerButton;
private Button removeItemContainerButton;

// Fluid Container
private Button installFluidContainerButton;
private Button removeFluidContainerButton;
```

#### **1.4 Button Initialisierung**
```java
private void initContainerButtons() {
    int startY = topPos + 40;
    int buttonWidth = 100;
    int buttonHeight = 20;

    // Item Container Section
    installItemContainerButton = addRenderableWidget(Button.builder(
        Component.translatable("garage.container.install"),
        button -> installContainer(ContainerType.ITEM))
        .bounds(leftPos + 10, startY, buttonWidth, buttonHeight)
        .build()
    );

    removeItemContainerButton = addRenderableWidget(Button.builder(
        Component.translatable("garage.container.remove"),
        button -> removeContainer(ContainerType.ITEM))
        .bounds(leftPos + 120, startY, buttonWidth, buttonHeight)
        .build()
    );

    // Fluid Container Section (startY + 60)
    installFluidContainerButton = addRenderableWidget(Button.builder(
        Component.translatable("garage.container.install"),
        button -> installContainer(ContainerType.FLUID))
        .bounds(leftPos + 10, startY + 60, buttonWidth, buttonHeight)
        .build()
    );

    removeFluidContainerButton = addRenderableWidget(Button.builder(
        Component.translatable("garage.container.remove"),
        button -> removeContainer(ContainerType.FLUID))
        .bounds(leftPos + 120, startY + 60, buttonWidth, buttonHeight)
        .build()
    );
}
```

#### **1.5 Visibility Update**
```java
private void updateWidgetVisibility() {
    boolean isRepair = currentTab == Tab.REPAIR;
    boolean isUpgrade = currentTab == Tab.UPGRADE;
    boolean isContainer = currentTab == Tab.CONTAINER;

    // ... existing code ...

    // Container tab widgets
    if (containerTabButton != null) containerTabButton.visible = true;
    installItemContainerButton.visible = isContainer;
    removeItemContainerButton.visible = isContainer;
    installFluidContainerButton.visible = isContainer;
    removeFluidContainerButton.visible = isContainer;
}
```

---

### **2. Network Packets**

#### **2.1 Container-Operation Message** (NEU)
Erstelle: `MessageContainerOperation.java`

```java
package de.rolandsw.schedulemc.vehicle.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class MessageContainerOperation {
    public enum Operation { INSTALL_ITEM, REMOVE_ITEM, INSTALL_FLUID, REMOVE_FLUID }

    private final int vehicleId;
    private final Operation operation;

    public MessageContainerOperation(int vehicleId, Operation operation) {
        this.vehicleId = vehicleId;
        this.operation = operation;
    }

    public static void encode(MessageContainerOperation msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.vehicleId);
        buf.writeEnum(msg.operation);
    }

    public static MessageContainerOperation decode(FriendlyByteBuf buf) {
        return new MessageContainerOperation(
            buf.readInt(),
            buf.readEnum(Operation.class)
        );
    }

    public static void handle(MessageContainerOperation msg, NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling (siehe 2.2)
        });
        ctx.setPacketHandled(true);
    }
}
```

#### **2.2 Server-side Container Handler**
In `MessageContainerOperation.handle()`:

```java
ServerPlayer player = ctx.getSender();
if (player == null) return;

Entity entity = player.level().getEntity(msg.vehicleId);
if (!(entity instanceof EntityGenericVehicle vehicle)) return;

// Permission check
if (!vehicle.getOwnerId().equals(player.getUUID())) {
    player.sendSystemMessage(Component.translatable("garage.container.error")
        .withStyle(ChatFormatting.RED));
    return;
}

// Only trucks allowed
if (!(vehicle.getChassis() instanceof PartTruckChassis)) {
    player.sendSystemMessage(Component.translatable("garage.container.truck_only"));
    return;
}

switch (msg.operation) {
    case INSTALL_ITEM -> installItemContainer(vehicle, player);
    case REMOVE_ITEM -> removeItemContainer(vehicle, player);
    case INSTALL_FLUID -> installFluidContainer(vehicle, player);
    case REMOVE_FLUID -> removeFluidContainer(vehicle, player);
}
```

---

### **3. Container Installation Logic**

#### **3.1 Install Item Container**
```java
private static void installItemContainer(EntityGenericVehicle vehicle, ServerPlayer player) {
    // Check if already installed
    if (vehicle.getPartByClass(PartContainer.class) != null) {
        player.sendSystemMessage(Component.literal("Already installed!").withStyle(ChatFormatting.YELLOW));
        return;
    }

    // Calculate cost
    double cost = 0;
    if (vehicle.hasHadItemContainer()) {
        cost = ModConfigHandler.VEHICLE_SERVER.containerReinstallationCost.get();

        // Check balance
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < cost) {
            player.sendSystemMessage(
                Component.translatable("garage.container.not_enough_money", String.format("%.0f", cost))
                    .withStyle(ChatFormatting.RED)
            );
            return;
        }

        // Deduct money
        EconomyManager.withdraw(player.getUUID(), cost);
    }

    // Add container to vehicle
    ItemStack containerItem = new ItemStack(ModItems.ITEM_CONTAINER.get());
    Container partInventory = vehicle.getInventoryComponent().getPartInventory();

    // Find empty slot
    for (int i = 0; i < partInventory.getContainerSize(); i++) {
        if (partInventory.getItem(i).isEmpty()) {
            partInventory.setItem(i, containerItem);
            break;
        }
    }

    // Mark as "had container"
    vehicle.setHasHadItemContainer(true);

    // Reinitialize vehicle
    vehicle.invalidatePartCache();
    vehicle.tryInitPartsAndModel();

    // Success message
    String costMsg = cost > 0 ? String.format(" (Kosten: %.0f€)", cost) : " (Kostenlos!)";
    player.sendSystemMessage(
        Component.translatable("garage.container.installed_successfully")
            .append(Component.literal(costMsg).withStyle(ChatFormatting.GRAY))
            .withStyle(ChatFormatting.GREEN)
    );
}
```

#### **3.2 Remove Item Container**
```java
private static void removeItemContainer(EntityGenericVehicle vehicle, ServerPlayer player) {
    PartContainer container = vehicle.getPartByClass(PartContainer.class);
    if (container == null) {
        player.sendSystemMessage(Component.literal("No container installed!").withStyle(ChatFormatting.YELLOW));
        return;
    }

    // Remove from part inventory
    Container partInventory = vehicle.getInventoryComponent().getPartInventory();
    for (int i = 0; i < partInventory.getContainerSize(); i++) {
        ItemStack stack = partInventory.getItem(i);
        if (stack.getItem() instanceof IVehiclePart vehiclePart) {
            if (vehiclePart.getPart(stack) instanceof PartContainer) {
                partInventory.removeItem(i, 1);
                break;
            }
        }
    }

    // Reinitialize
    vehicle.invalidatePartCache();
    vehicle.tryInitPartsAndModel();

    player.sendSystemMessage(
        Component.translatable("garage.container.removed_successfully")
            .withStyle(ChatFormatting.GREEN)
    );
}
```

#### **3.3 Install/Remove Fluid Container**
Analog zu Item Container, aber mit `PartTankContainer` und `hasHadFluidContainer()`.

---

### **4. GUI Rendering (Container Tab)**

In `GuiGarage.render()`:

```java
if (currentTab == Tab.CONTAINER) {
    renderContainerTab(guiGraphics, mouseX, mouseY);
}

private void renderContainerTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    int startY = topPos + 40;

    // Item Container Section
    guiGraphics.drawString(font,
        Component.translatable("garage.container.item_container"),
        leftPos + 10, startY - 15, titleColor, false
    );

    PartContainer itemContainer = vehicle.getPartByClass(PartContainer.class);
    String itemStatus = itemContainer != null ?
        Component.translatable("garage.container.status_installed").getString() :
        Component.translatable("garage.container.status_not_installed").getString();

    guiGraphics.drawString(font, itemStatus, leftPos + 10, startY + 25, fontColor, false);

    // Cost display
    if (vehicle.hasHadItemContainer() && itemContainer == null) {
        double cost = ModConfigHandler.VEHICLE_SERVER.containerReinstallationCost.get();
        String costText = Component.translatable("garage.container.cost", String.format("%.0f", cost)).getString();
        guiGraphics.drawString(font, costText, leftPos + 10, startY + 35, costColor, false);
    } else if (!vehicle.hasHadItemContainer()) {
        String freeText = Component.translatable("garage.container.cost_free").getString();
        guiGraphics.drawString(font, freeText, leftPos + 10, startY + 35, barGoodColor, false);
    }

    // Fluid Container Section (analog)
    // ...
}
```

---

### **5. Packet Registration**

In `Main.java` oder Network-Handler:

```java
SIMPLE_CHANNEL.registerMessage(
    messageId++,
    MessageContainerOperation.class,
    MessageContainerOperation::encode,
    MessageContainerOperation::decode,
    MessageContainerOperation::handle
);
```

---

## 📝 IMPLEMENTATION CHECKLIST

- [ ] GuiGarage: Tab enum erweitern (CONTAINER)
- [ ] GuiGarage: Container-Tab Button (nur Trucks)
- [ ] GuiGarage: Container-Installations-Buttons (4x)
- [ ] GuiGarage: initContainerButtons() Methode
- [ ] GuiGarage: updateWidgetVisibility() erweitern
- [ ] GuiGarage: renderContainerTab() Rendering
- [ ] MessageContainerOperation: Packet erstellen
- [ ] MessageContainerOperation: Server-side Handler
- [ ] Container Logic: installItemContainer()
- [ ] Container Logic: removeItemContainer()
- [ ] Container Logic: installFluidContainer()
- [ ] Container Logic: removeFluidContainer()
- [ ] Packet Registration in Network-Handler
- [ ] Testing: Container Installation (kostenlos beim ersten Mal)
- [ ] Testing: Container Removal
- [ ] Testing: Container Reinstallation (7500€ Kosten)
- [ ] Testing: Nur Trucks erlaubt
- [ ] Testing: Inventory Slot Update (0→12 bei Installation)

---

## 🎯 ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────┐
│  CLIENT: GuiGarage (Container Tab)                          │
│  - Tab Button (nur bei Trucks)                              │
│  - Install/Remove Buttons für Item & Fluid Container       │
│  - Status & Kosten Anzeige                                  │
└─────────────┬───────────────────────────────────────────────┘
              │ MessageContainerOperation (Network Packet)
              ▼
┌─────────────────────────────────────────────────────────────┐
│  SERVER: Container Handler                                   │
│  - Permission Check (Owner only)                            │
│  - Truck Validation                                         │
│  - Cost Calculation (hasHadContainer → 7500€)              │
│  - Economy Deduction                                        │
│  - Part Inventory Modification                             │
│  - Vehicle Reinitialization                                │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│  EntityGenericVehicle                                        │
│  - hasHadItemContainer (NBT Flag)                           │
│  - hasHadFluidContainer (NBT Flag)                          │
│  - checkInitializing() → Inventory Size Update             │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚠️ WICHTIGE HINWEISE

1. **Erste Installation KOSTENLOS**: Prüfe `hasHadItemContainer()` bzw. `hasHadFluidContainer()` - wenn `false`, keine Kosten!
2. **Reinstallation 7500€**: Nach Entfernung sind Flags auf `true`, also Kosten beim nächsten Mal
3. **Nur Trucks**: `canMountOn()` bereits implementiert, aber GUI muss zusätzlich validieren
4. **Inventory Update**: Nach Part-Änderung IMMER `invalidatePartCache()` + `tryInitPartsAndModel()` aufrufen
5. **Network Sync**: Fahrzeug-Daten müssen zum Client gesynct werden nach Änderungen

---

## 🚀 ESTIMATED EFFORT

- **GUI Implementation**: ~3-4 Stunden (Buttons, Rendering, Layout)
- **Network Packet**: ~1 Stunde (Encode/Decode/Handle)
- **Server Logic**: ~2-3 Stunden (Install/Remove für beide Container-Typen)
- **Testing & Debugging**: ~2 Stunden
- **TOTAL**: ~8-10 Stunden

**Status**: Core-Systeme (Config, Parts, NBT, Inventory Logic) sind fertig ✅
**Verbleibend**: GUI-Frontend und Network-Backend

---

EOF 2026-01-15
