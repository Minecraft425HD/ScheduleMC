package de.rolandsw.schedulemc.npc.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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
    private List<BlockPos> leisureLocations; // 3 Freizeitorte in der Stadt

    // Police Patrol System (nur für POLIZEI NPCs)
    @Nullable
    private BlockPos policeStation;  // Polizeistation
    private List<BlockPos> patrolPoints; // Bis zu 16 Patrouillenpunkte
    private int currentPatrolIndex; // Aktueller Patrol-Index
    private long patrolArrivalTime; // Letzte Ankunftszeit am Patrol-Punkt
    private long stationArrivalTime; // Letzte Ankunftszeit an der Station

    // Schedule - Zeiteinstellungen (in Minecraft Ticks, 24000 = 1 Tag)
    private long workStartTime;  // Wann geht NPC zur Arbeit (Standard: 0 = 6:00 Uhr)
    private long workEndTime;    // Wann endet die Arbeit (Standard: 13000 = 19:00 Uhr)
    private long homeTime;       // Wann muss NPC nach Hause (Standard: 23000 = 5:00 Uhr morgens)

    // Inventar und Geldbörse (nur für BEWOHNER und VERKAEUFER, nicht für POLIZEI)
    private NonNullList<ItemStack> inventory; // 9 Slots wie Hotbar
    private int wallet; // Geldbörse in Bargeld
    private long lastDailyIncome; // Letzter Tag, an dem Einkommen ausgezahlt wurde

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
        this.leisureLocations = new ArrayList<>();
        // Police Patrol System
        this.patrolPoints = new ArrayList<>();
        this.currentPatrolIndex = 0;
        this.patrolArrivalTime = 0;
        this.stationArrivalTime = 0;
        // Standard-Zeiten (Minecraft Ticks: 0 = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00)
        this.workStartTime = 0;      // 6:00 Uhr morgens
        this.workEndTime = 13000;    // 19:00 Uhr abends
        this.homeTime = 23000;       // 5:00 Uhr morgens (Zeit zum Schlafen)
        // Inventar und Geldbörse
        this.inventory = NonNullList.withSize(9, ItemStack.EMPTY); // 9 Slots wie Hotbar
        this.wallet = 0;
        this.lastDailyIncome = -1; // Noch kein Einkommen erhalten
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

        // Leisure Locations
        ListTag leisureList = new ListTag();
        for (BlockPos pos : leisureLocations) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            leisureList.add(posTag);
        }
        tag.put("LeisureLocations", leisureList);

        // Police Patrol System
        if (policeStation != null) {
            tag.putLong("PoliceStation", policeStation.asLong());
        }
        ListTag patrolList = new ListTag();
        for (BlockPos pos : patrolPoints) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            patrolList.add(posTag);
        }
        tag.put("PatrolPoints", patrolList);
        tag.putInt("CurrentPatrolIndex", currentPatrolIndex);
        tag.putLong("PatrolArrivalTime", patrolArrivalTime);
        tag.putLong("StationArrivalTime", stationArrivalTime);

        // Schedule Times
        tag.putLong("WorkStartTime", workStartTime);
        tag.putLong("WorkEndTime", workEndTime);
        tag.putLong("HomeTime", homeTime);

        // Inventar (nur für BEWOHNER und VERKAEUFER)
        if (npcType != NPCType.POLIZEI) {
            ListTag inventoryList = new ListTag();
            for (int i = 0; i < inventory.size(); i++) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                inventory.get(i).save(itemTag);
                inventoryList.add(itemTag);
            }
            tag.put("Inventory", inventoryList);
            tag.putInt("Wallet", wallet);
            tag.putLong("LastDailyIncome", lastDailyIncome);
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

        // Leisure Locations
        leisureLocations.clear();
        if (tag.contains("LeisureLocations")) {
            ListTag leisureList = tag.getList("LeisureLocations", Tag.TAG_COMPOUND);
            for (int i = 0; i < leisureList.size(); i++) {
                CompoundTag posTag = leisureList.getCompound(i);
                leisureLocations.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }

        // Police Patrol System
        if (tag.contains("PoliceStation")) {
            policeStation = BlockPos.of(tag.getLong("PoliceStation"));
        }
        patrolPoints.clear();
        if (tag.contains("PatrolPoints")) {
            ListTag patrolList = tag.getList("PatrolPoints", Tag.TAG_COMPOUND);
            for (int i = 0; i < patrolList.size(); i++) {
                CompoundTag posTag = patrolList.getCompound(i);
                patrolPoints.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }
        if (tag.contains("CurrentPatrolIndex")) {
            currentPatrolIndex = tag.getInt("CurrentPatrolIndex");
        }
        if (tag.contains("PatrolArrivalTime")) {
            patrolArrivalTime = tag.getLong("PatrolArrivalTime");
        }
        if (tag.contains("StationArrivalTime")) {
            stationArrivalTime = tag.getLong("StationArrivalTime");
        }

        // Schedule Times
        if (tag.contains("WorkStartTime")) {
            workStartTime = tag.getLong("WorkStartTime");
        }
        if (tag.contains("WorkEndTime")) {
            workEndTime = tag.getLong("WorkEndTime");
        }
        if (tag.contains("HomeTime")) {
            homeTime = tag.getLong("HomeTime");
        }

        // Inventar und Geldbörse (nur für BEWOHNER und VERKAEUFER)
        if (npcType != NPCType.POLIZEI) {
            inventory = NonNullList.withSize(9, ItemStack.EMPTY);
            if (tag.contains("Inventory")) {
                ListTag inventoryList = tag.getList("Inventory", Tag.TAG_COMPOUND);
                for (int i = 0; i < inventoryList.size(); i++) {
                    CompoundTag itemTag = inventoryList.getCompound(i);
                    int slot = itemTag.getByte("Slot") & 255;
                    if (slot >= 0 && slot < inventory.size()) {
                        inventory.set(slot, ItemStack.of(itemTag));
                    }
                }
            }
            if (tag.contains("Wallet")) {
                wallet = tag.getInt("Wallet");
            }
            if (tag.contains("LastDailyIncome")) {
                lastDailyIncome = tag.getLong("LastDailyIncome");
            }
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

    public List<BlockPos> getLeisureLocations() {
        return leisureLocations;
    }

    public void addLeisureLocation(BlockPos location) {
        if (leisureLocations.size() < 3) {
            leisureLocations.add(location);
        }
    }

    public void removeLeisureLocation(int index) {
        if (index >= 0 && index < leisureLocations.size()) {
            leisureLocations.remove(index);
        }
    }

    public void clearLeisureLocations() {
        leisureLocations.clear();
    }

    public long getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(long workStartTime) {
        this.workStartTime = workStartTime;
    }

    public long getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(long workEndTime) {
        this.workEndTime = workEndTime;
    }

    public long getHomeTime() {
        return homeTime;
    }

    public void setHomeTime(long homeTime) {
        this.homeTime = homeTime;
    }

    // Inventar und Geldbörse (nur BEWOHNER und VERKAEUFER)
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    public ItemStack getInventoryItem(int slot) {
        if (slot >= 0 && slot < inventory.size()) {
            return inventory.get(slot);
        }
        return ItemStack.EMPTY;
    }

    public void setInventoryItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.size()) {
            inventory.set(slot, stack);
        }
    }

    public int getWallet() {
        return wallet;
    }

    public void setWallet(int amount) {
        this.wallet = Math.max(0, amount);
    }

    public void addMoney(int amount) {
        this.wallet += amount;
    }

    public boolean removeMoney(int amount) {
        if (wallet >= amount) {
            wallet -= amount;
            return true;
        }
        return false;
    }

    public long getLastDailyIncome() {
        return lastDailyIncome;
    }

    public void setLastDailyIncome(long day) {
        this.lastDailyIncome = day;
    }

    /**
     * Prüft ob dieser NPC ein Inventar und Geldbörse haben sollte
     */
    public boolean hasInventoryAndWallet() {
        return npcType == NPCType.BEWOHNER || npcType == NPCType.VERKAEUFER;
    }

    // Police Patrol System Getters/Setters
    @Nullable
    public BlockPos getPoliceStation() {
        return policeStation;
    }

    public void setPoliceStation(@Nullable BlockPos policeStation) {
        this.policeStation = policeStation;
    }

    public List<BlockPos> getPatrolPoints() {
        return patrolPoints;
    }

    public void addPatrolPoint(BlockPos point) {
        if (patrolPoints.size() < 16) {
            patrolPoints.add(point);
        }
    }

    public void removePatrolPoint(int index) {
        if (index >= 0 && index < patrolPoints.size()) {
            patrolPoints.remove(index);
        }
    }

    public void clearPatrolPoints() {
        patrolPoints.clear();
        currentPatrolIndex = 0;
    }

    public int getCurrentPatrolIndex() {
        return currentPatrolIndex;
    }

    public void setCurrentPatrolIndex(int index) {
        this.currentPatrolIndex = index;
    }

    public void incrementPatrolIndex() {
        if (!patrolPoints.isEmpty()) {
            currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.size();
        }
    }

    public long getPatrolArrivalTime() {
        return patrolArrivalTime;
    }

    public void setPatrolArrivalTime(long time) {
        this.patrolArrivalTime = time;
    }

    public long getStationArrivalTime() {
        return stationArrivalTime;
    }

    public void setStationArrivalTime(long time) {
        this.stationArrivalTime = time;
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
