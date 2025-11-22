package de.rolandsw.schedulemc.npc.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Erweiterbare Datenklasse für Custom NPCs
 * Enthält alle Eigenschaften eines NPCs inkl. Skin, Dialog, Shop, etc.
 */
public class NPCData {
    private String npcName;
    private String skinFileName; // Dateiname des Skins (z.B. "steve.png")
    private UUID npcUUID;

    // NPC Typ System
    private NPCType npcType;
    private MerchantCategory merchantCategory; // Nur relevant wenn npcType == VERKAEUFER

    // Dialog System
    private List<DialogEntry> dialogEntries;
    private int currentDialogIndex;

    // Shop System
    private ShopInventory buyShop;  // Was der NPC verkauft
    private ShopInventory sellShop; // Was der NPC kauft

    // Erweiterbare Features (für zukünftige Features)
    private CompoundTag customData;

    // Verhalten
    private NPCBehavior behavior;

    // Locations
    @Nullable
    private BlockPos homeLocation;  // Wohnbereich
    @Nullable
    private BlockPos workLocation;  // Arbeitsstätte

    // Inventar & Ökonomie (nur für BEWOHNER und VERKAEUFER, nicht für POLIZEI)
    private ItemStack[] inventory;  // Hotbar-ähnliches Inventar (9 Slots)
    private int cash;  // Geldbörse in Bargeld
    private long lastDailyIncomeDay;  // Letzter Tag, an dem Einkommen erhalten wurde

    public NPCData() {
        this.npcName = "NPC";
        this.skinFileName = "default.png";
        this.npcUUID = UUID.randomUUID();
        this.npcType = NPCType.BEWOHNER;
        this.merchantCategory = MerchantCategory.BAUMARKT;
        this.dialogEntries = new ArrayList<>();
        this.currentDialogIndex = 0;
        this.buyShop = new ShopInventory();
        this.sellShop = new ShopInventory();
        this.customData = new CompoundTag();
        this.behavior = new NPCBehavior();
        this.inventory = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            this.inventory[i] = ItemStack.EMPTY;
        }
        this.cash = 0;
        this.lastDailyIncomeDay = -1;
    }

    public NPCData(String name, String skinFile) {
        this();
        this.npcName = name;
        this.skinFileName = skinFile;
    }

    public NPCData(String name, String skinFile, NPCType type, MerchantCategory category) {
        this();
        this.npcName = name;
        this.skinFileName = skinFile;
        this.npcType = type;
        this.merchantCategory = category;
    }

    // NBT Serialization
    public CompoundTag save(CompoundTag tag) {
        tag.putString("NPCName", npcName);
        tag.putString("SkinFileName", skinFileName);
        tag.putUUID("NPCUUID", npcUUID);
        tag.putInt("NPCType", npcType.ordinal());
        tag.putInt("MerchantCategory", merchantCategory.ordinal());
        tag.putInt("CurrentDialogIndex", currentDialogIndex);

        // Dialog speichern
        ListTag dialogList = new ListTag();
        for (DialogEntry entry : dialogEntries) {
            dialogList.add(entry.save(new CompoundTag()));
        }
        tag.put("DialogEntries", dialogList);

        // Shop speichern
        tag.put("BuyShop", buyShop.save(new CompoundTag()));
        tag.put("SellShop", sellShop.save(new CompoundTag()));

        // Custom Data
        tag.put("CustomData", customData);

        // Behavior
        tag.put("Behavior", behavior.save(new CompoundTag()));

        // Locations
        if (homeLocation != null) {
            tag.putLong("HomeLocation", homeLocation.asLong());
        }
        if (workLocation != null) {
            tag.putLong("WorkLocation", workLocation.asLong());
        }

        // Inventar & Ökonomie speichern (nur für BEWOHNER und VERKAEUFER)
        if (npcType != NPCType.POLIZEI) {
            ListTag inventoryList = new ListTag();
            for (int i = 0; i < inventory.length; i++) {
                if (!inventory[i].isEmpty()) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putByte("Slot", (byte) i);
                    inventory[i].save(itemTag);
                    inventoryList.add(itemTag);
                }
            }
            tag.put("Inventory", inventoryList);
            tag.putInt("Cash", cash);
            tag.putLong("LastDailyIncomeDay", lastDailyIncomeDay);
        }

        return tag;
    }

    public void load(CompoundTag tag) {
        npcName = tag.getString("NPCName");
        skinFileName = tag.getString("SkinFileName");
        npcUUID = tag.getUUID("NPCUUID");
        npcType = NPCType.fromOrdinal(tag.getInt("NPCType"));
        merchantCategory = MerchantCategory.fromOrdinal(tag.getInt("MerchantCategory"));
        currentDialogIndex = tag.getInt("CurrentDialogIndex");

        // Dialog laden
        dialogEntries.clear();
        ListTag dialogList = tag.getList("DialogEntries", Tag.TAG_COMPOUND);
        for (int i = 0; i < dialogList.size(); i++) {
            DialogEntry entry = new DialogEntry();
            entry.load(dialogList.getCompound(i));
            dialogEntries.add(entry);
        }

        // Shop laden
        buyShop = new ShopInventory();
        buyShop.load(tag.getCompound("BuyShop"));
        sellShop = new ShopInventory();
        sellShop.load(tag.getCompound("SellShop"));

        // Custom Data
        customData = tag.getCompound("CustomData");

        // Behavior
        behavior = new NPCBehavior();
        behavior.load(tag.getCompound("Behavior"));

        // Locations
        if (tag.contains("HomeLocation")) {
            homeLocation = BlockPos.of(tag.getLong("HomeLocation"));
        }
        if (tag.contains("WorkLocation")) {
            workLocation = BlockPos.of(tag.getLong("WorkLocation"));
        }

        // Inventar & Ökonomie laden (nur für BEWOHNER und VERKAEUFER)
        if (npcType != NPCType.POLIZEI) {
            // Inventar initialisieren
            inventory = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                inventory[i] = ItemStack.EMPTY;
            }

            // Inventar Items laden
            if (tag.contains("Inventory")) {
                ListTag inventoryList = tag.getList("Inventory", Tag.TAG_COMPOUND);
                for (int i = 0; i < inventoryList.size(); i++) {
                    CompoundTag itemTag = inventoryList.getCompound(i);
                    int slot = itemTag.getByte("Slot") & 255;
                    if (slot >= 0 && slot < inventory.length) {
                        inventory[slot] = ItemStack.of(itemTag);
                    }
                }
            }

            cash = tag.getInt("Cash");
            lastDailyIncomeDay = tag.getLong("LastDailyIncomeDay");
        }
    }

    // Getters & Setters
    public String getNpcName() {
        return npcName;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }

    public String getSkinFileName() {
        return skinFileName;
    }

    public void setSkinFileName(String skinFileName) {
        this.skinFileName = skinFileName;
    }

    public NPCType getNpcType() {
        return npcType;
    }

    public void setNpcType(NPCType npcType) {
        this.npcType = npcType;
    }

    public MerchantCategory getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(MerchantCategory merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public UUID getNpcUUID() {
        return npcUUID;
    }

    public List<DialogEntry> getDialogEntries() {
        return dialogEntries;
    }

    public void addDialogEntry(DialogEntry entry) {
        dialogEntries.add(entry);
    }

    public DialogEntry getCurrentDialog() {
        if (dialogEntries.isEmpty()) {
            return new DialogEntry("Hallo!", "");
        }
        if (currentDialogIndex >= dialogEntries.size()) {
            currentDialogIndex = 0;
        }
        return dialogEntries.get(currentDialogIndex);
    }

    public void nextDialog() {
        currentDialogIndex = (currentDialogIndex + 1) % Math.max(1, dialogEntries.size());
    }

    public ShopInventory getBuyShop() {
        return buyShop;
    }

    public ShopInventory getSellShop() {
        return sellShop;
    }

    public CompoundTag getCustomData() {
        return customData;
    }

    public NPCBehavior getBehavior() {
        return behavior;
    }

    @Nullable
    public BlockPos getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(@Nullable BlockPos homeLocation) {
        this.homeLocation = homeLocation;
    }

    @Nullable
    public BlockPos getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(@Nullable BlockPos workLocation) {
        this.workLocation = workLocation;
    }

    // Inventar & Ökonomie Getter/Setter
    public ItemStack[] getInventory() {
        return inventory;
    }

    public ItemStack getInventoryItem(int slot) {
        if (slot >= 0 && slot < inventory.length) {
            return inventory[slot];
        }
        return ItemStack.EMPTY;
    }

    public void setInventoryItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.length) {
            inventory[slot] = stack;
        }
    }

    public int getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = Math.max(0, cash); // Verhindert negativen Bargeldstand
    }

    public void addCash(int amount) {
        this.cash += amount;
        this.cash = Math.max(0, this.cash);
    }

    public void removeCash(int amount) {
        this.cash -= amount;
        this.cash = Math.max(0, this.cash);
    }

    public boolean hasCash(int amount) {
        return this.cash >= amount;
    }

    public long getLastDailyIncomeDay() {
        return lastDailyIncomeDay;
    }

    public void setLastDailyIncomeDay(long day) {
        this.lastDailyIncomeDay = day;
    }

    /**
     * Prüft, ob dieser NPC Inventar und Geldbörse haben sollte
     * (nur BEWOHNER und VERKAEUFER, nicht POLIZEI)
     */
    public boolean hasEconomyFeatures() {
        return npcType != NPCType.POLIZEI;
    }

    /**
     * Dialog Entry - einzelner Dialog-Eintrag
     */
    public static class DialogEntry {
        private String text;
        private String response; // Optionale Antwort des Spielers

        public DialogEntry() {
            this.text = "";
            this.response = "";
        }

        public DialogEntry(String text, String response) {
            this.text = text;
            this.response = response;
        }

        public CompoundTag save(CompoundTag tag) {
            tag.putString("Text", text);
            tag.putString("Response", response);
            return tag;
        }

        public void load(CompoundTag tag) {
            text = tag.getString("Text");
            response = tag.getString("Response");
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }

    /**
     * Shop Inventory - Kaufen/Verkaufen Items
     */
    public static class ShopInventory {
        private List<ShopEntry> entries;

        public ShopInventory() {
            this.entries = new ArrayList<>();
        }

        public void addEntry(ItemStack item, int price) {
            entries.add(new ShopEntry(item, price));
        }

        public void addEntry(ShopEntry entry) {
            entries.add(entry);
        }

        public void clear() {
            entries.clear();
        }

        public List<ShopEntry> getEntries() {
            return entries;
        }

        public CompoundTag save(CompoundTag tag) {
            ListTag list = new ListTag();
            for (ShopEntry entry : entries) {
                list.add(entry.save(new CompoundTag()));
            }
            tag.put("Entries", list);
            return tag;
        }

        public void load(CompoundTag tag) {
            entries.clear();
            ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ShopEntry entry = new ShopEntry();
                entry.load(list.getCompound(i));
                entries.add(entry);
            }
        }
    }

    /**
     * Shop Entry - einzelnes Shop-Item
     */
    public static class ShopEntry {
        private ItemStack item;
        private int price;
        private boolean unlimited; // True = unbegrenzt, False = Lagerware
        private int stock; // Aktueller Lagerbestand (nur relevant wenn unlimited = false)

        public ShopEntry() {
            this.item = ItemStack.EMPTY;
            this.price = 0;
            this.unlimited = true; // Default: unbegrenzt
            this.stock = 0;
        }

        public ShopEntry(ItemStack item, int price) {
            this.item = item.copy();
            this.price = price;
            this.unlimited = true;
            this.stock = 0;
        }

        public ShopEntry(ItemStack item, int price, boolean unlimited, int stock) {
            this.item = item.copy();
            this.price = price;
            this.unlimited = unlimited;
            this.stock = stock;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getPrice() {
            return price;
        }

        public boolean isUnlimited() {
            return unlimited;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }

        public void reduceStock(int amount) {
            if (!unlimited) {
                this.stock = Math.max(0, this.stock - amount);
            }
        }

        public boolean hasStock(int amount) {
            return unlimited || stock >= amount;
        }

        public CompoundTag save(CompoundTag tag) {
            tag.put("Item", item.save(new CompoundTag()));
            tag.putInt("Price", price);
            tag.putBoolean("Unlimited", unlimited);
            tag.putInt("Stock", stock);
            return tag;
        }

        public void load(CompoundTag tag) {
            item = ItemStack.of(tag.getCompound("Item"));
            price = tag.getInt("Price");
            unlimited = tag.getBoolean("Unlimited");
            stock = tag.getInt("Stock");
        }
    }

    /**
     * NPC Behavior - Verhaltenseinstellungen
     */
    public static class NPCBehavior {
        private boolean canMove;
        private boolean lookAtPlayer;
        private float movementSpeed;

        public NPCBehavior() {
            this.canMove = false; // Standardmäßig statisch
            this.lookAtPlayer = true;
            this.movementSpeed = 0.3f;
        }

        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean("CanMove", canMove);
            tag.putBoolean("LookAtPlayer", lookAtPlayer);
            tag.putFloat("MovementSpeed", movementSpeed);
            return tag;
        }

        public void load(CompoundTag tag) {
            canMove = tag.getBoolean("CanMove");
            lookAtPlayer = tag.getBoolean("LookAtPlayer");
            movementSpeed = tag.getFloat("MovementSpeed");
        }

        public boolean canMove() {
            return canMove;
        }

        public void setCanMove(boolean canMove) {
            this.canMove = canMove;
        }

        public boolean shouldLookAtPlayer() {
            return lookAtPlayer;
        }

        public void setLookAtPlayer(boolean lookAtPlayer) {
            this.lookAtPlayer = lookAtPlayer;
        }

        public float getMovementSpeed() {
            return movementSpeed;
        }

        public void setMovementSpeed(float movementSpeed) {
            this.movementSpeed = movementSpeed;
        }
    }
}
