package de.rolandsw.schedulemc.npc.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
    private BankCategory bankCategory; // Nur relevant wenn npcType == BANK
    private ServiceCategory serviceCategory; // Nur relevant wenn npcType == ABSCHLEPPER

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
    private BlockPos workLocation;  // Arbeitsstätte (nur für VERKAEUFER)
    @Nullable
    private BlockPos assignedWarehouse; // Warehouse für Shop-Verkäufer (NEU)
    private List<BlockPos> leisureLocations; // Bis zu 10 Freizeitorte in der Stadt

    // Police Patrol System (nur für POLIZEI NPCs)
    private NPCPoliceData policeData;

    // Schedule - Zeiteinstellungen (in Minecraft Ticks, 24000 = 1 Tag)
    private NPCScheduleData scheduleData;

    // Inventar und Geldbörse (nur für BEWOHNER und VERKAEUFER, nicht für POLIZEI)
    private NonNullList<ItemStack> inventory; // 9 Slots wie Hotbar
    private int wallet; // Geldbörse in Bargeld
    private long lastDailyIncome; // Letzter Tag, an dem Einkommen ausgezahlt wurde

    // Missions-System: IDs der Missionen, die dieser NPC vergeben kann
    private List<String> missionIds = new ArrayList<>();

    public NPCData() {
        this.npcName = "NPC";
        this.skinFileName = "default.png";
        this.npcUUID = UUID.randomUUID();
        this.npcType = NPCType.BEWOHNER;
        this.merchantCategory = MerchantCategory.BAUMARKT;
        this.bankCategory = BankCategory.BANKER;
        this.serviceCategory = ServiceCategory.ABSCHLEPPDIENST;
        this.dialogEntries = new ArrayList<>();
        this.currentDialogIndex = 0;
        this.buyShop = new ShopInventory();
        this.sellShop = new ShopInventory();
        this.customData = new CompoundTag();
        this.behavior = new NPCBehavior();
        this.leisureLocations = new ArrayList<>();
        this.policeData = new NPCPoliceData();
        this.scheduleData = new NPCScheduleData();
        // Inventar und Geldbörse
        this.inventory = NonNullList.withSize(9, ItemStack.EMPTY); // 9 Slots wie Hotbar
        this.wallet = 0;
        this.lastDailyIncome = -1; // Noch kein Einkommen erhalten
        this.missionIds = new ArrayList<>();
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

    // ═══════════════════════════════════════════════════════════
    // NBT Serialization - OPTIMIERT: Aufgeteilt in kleinere Methoden
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save(CompoundTag tag) {
        saveBasicData(tag);
        saveDialogData(tag);
        saveShopData(tag);
        saveLocationData(tag);
        savePolicePatrolData(tag);
        saveScheduleData(tag);
        saveInventoryData(tag);
        return tag;
    }

    private void saveBasicData(CompoundTag tag) {
        tag.putString("NPCName", npcName);
        tag.putString("SkinFileName", skinFileName);
        tag.putUUID("NPCUUID", npcUUID);
        tag.putInt("NPCType", npcType.ordinal());
        tag.putInt("MerchantCategory", merchantCategory.ordinal());
        tag.putInt("BankCategory", bankCategory.ordinal());
        tag.putInt("ServiceCategory", serviceCategory.ordinal());
        tag.putInt("CurrentDialogIndex", currentDialogIndex);
        tag.put("CustomData", customData);
        tag.put("Behavior", behavior.save(new CompoundTag()));
        ListTag missionIdList = new ListTag();
        for (String id : missionIds) {
            missionIdList.add(net.minecraft.nbt.StringTag.valueOf(id));
        }
        tag.put("MissionIds", missionIdList);
    }

    private void saveDialogData(CompoundTag tag) {
        ListTag dialogList = new ListTag();
        for (DialogEntry entry : dialogEntries) {
            dialogList.add(entry.save(new CompoundTag()));
        }
        tag.put("DialogEntries", dialogList);
    }

    private void saveShopData(CompoundTag tag) {
        tag.put("BuyShop", buyShop.save(new CompoundTag()));
        tag.put("SellShop", sellShop.save(new CompoundTag()));
    }

    private void saveLocationData(CompoundTag tag) {
        if (homeLocation != null) {
            tag.putLong("HomeLocation", homeLocation.asLong());
        }
        if (workLocation != null) {
            tag.putLong("WorkLocation", workLocation.asLong());
        }
        if (assignedWarehouse != null) {
            tag.putLong("AssignedWarehouse", assignedWarehouse.asLong());
        }

        // Leisure Locations
        ListTag leisureList = new ListTag();
        for (BlockPos pos : leisureLocations) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            leisureList.add(posTag);
        }
        tag.put("LeisureLocations", leisureList);
    }

    private void savePolicePatrolData(CompoundTag tag) {
        policeData.save(tag);
    }

    private void saveScheduleData(CompoundTag tag) {
        scheduleData.save(tag);
    }

    private void saveInventoryData(CompoundTag tag) {
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
    }

    public void load(CompoundTag tag) {
        loadBasicData(tag);
        loadDialogData(tag);
        loadShopData(tag);
        loadLocationData(tag);
        loadPolicePatrolData(tag);
        loadScheduleData(tag);
        loadInventoryData(tag);
    }

    private void loadBasicData(CompoundTag tag) {
        npcName = tag.getString("NPCName");
        skinFileName = tag.getString("SkinFileName");
        npcUUID = tag.getUUID("NPCUUID");
        npcType = NPCType.fromOrdinal(tag.getInt("NPCType"));
        merchantCategory = MerchantCategory.fromOrdinal(tag.getInt("MerchantCategory"));
        bankCategory = BankCategory.fromOrdinal(tag.getInt("BankCategory"));
        serviceCategory = ServiceCategory.fromOrdinal(tag.getInt("ServiceCategory"));
        currentDialogIndex = tag.getInt("CurrentDialogIndex");
        customData = tag.getCompound("CustomData");
        behavior = new NPCBehavior();
        behavior.load(tag.getCompound("Behavior"));
        missionIds = new ArrayList<>();
        if (tag.contains("MissionIds")) {
            ListTag missionIdList = tag.getList("MissionIds", Tag.TAG_STRING);
            for (int i = 0; i < missionIdList.size(); i++) {
                missionIds.add(missionIdList.getString(i));
            }
        }
    }

    private void loadDialogData(CompoundTag tag) {
        dialogEntries.clear();
        ListTag dialogList = tag.getList("DialogEntries", Tag.TAG_COMPOUND);
        for (int i = 0; i < dialogList.size(); i++) {
            DialogEntry entry = new DialogEntry();
            entry.load(dialogList.getCompound(i));
            dialogEntries.add(entry);
        }
    }

    private void loadShopData(CompoundTag tag) {
        buyShop = new ShopInventory();
        buyShop.load(tag.getCompound("BuyShop"));
        sellShop = new ShopInventory();
        sellShop.load(tag.getCompound("SellShop"));
    }

    private void loadLocationData(CompoundTag tag) {
        if (tag.contains("HomeLocation")) {
            homeLocation = BlockPos.of(tag.getLong("HomeLocation"));
        }
        if (tag.contains("WorkLocation")) {
            workLocation = BlockPos.of(tag.getLong("WorkLocation"));
        }
        if (tag.contains("AssignedWarehouse")) {
            assignedWarehouse = BlockPos.of(tag.getLong("AssignedWarehouse"));
        }

        leisureLocations.clear();
        if (tag.contains("LeisureLocations")) {
            ListTag leisureList = tag.getList("LeisureLocations", Tag.TAG_COMPOUND);
            for (int i = 0; i < leisureList.size(); i++) {
                CompoundTag posTag = leisureList.getCompound(i);
                leisureLocations.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }
    }

    private void loadPolicePatrolData(CompoundTag tag) {
        policeData.load(tag);
    }

    private void loadScheduleData(CompoundTag tag) {
        scheduleData.load(tag);
    }

    private void loadInventoryData(CompoundTag tag) {
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

    public BankCategory getBankCategory() {
        return bankCategory;
    }

    public void setBankCategory(BankCategory bankCategory) {
        this.bankCategory = bankCategory;
    }

    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public UUID getNpcUUID() {
        return npcUUID;
    }

    public List<String> getMissionIds() {
        return missionIds;
    }

    public void setMissionIds(List<String> missionIds) {
        this.missionIds = missionIds != null ? missionIds : new ArrayList<>();
    }

    public List<DialogEntry> getDialogEntries() {
        return dialogEntries;
    }

    public void addDialogEntry(DialogEntry entry) {
        dialogEntries.add(entry);
    }

    public DialogEntry getCurrentDialog() {
        if (dialogEntries.isEmpty()) {
            return new DialogEntry(Component.translatable("npc.dialog.fallback").getString(), "");
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
        if (leisureLocations.size() < 10) {
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

    public NPCScheduleData getScheduleData() {
        return scheduleData;
    }

    public long getWorkStartTime() {
        return scheduleData.getWorkStartTime();
    }

    public void setWorkStartTime(long workStartTime) {
        scheduleData.setWorkStartTime(workStartTime);
    }

    public long getWorkEndTime() {
        return scheduleData.getWorkEndTime();
    }

    public void setWorkEndTime(long workEndTime) {
        scheduleData.setWorkEndTime(workEndTime);
    }

    public long getHomeTime() {
        return scheduleData.getHomeTime();
    }

    public void setHomeTime(long homeTime) {
        scheduleData.setHomeTime(homeTime);
    }

    /**
     * Prüft ob der NPC aktuell während seiner Arbeitszeit ist.
     * @param level Die Welt (für die aktuelle Zeit)
     * @return true wenn innerhalb der Arbeitszeit, sonst false
     */
    public boolean isWithinWorkingHours(net.minecraft.world.level.Level level) {
        return scheduleData.isWithinWorkingHours(level);
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
        this.wallet = (int) Math.max(0L, Math.min((long) this.wallet + amount, Integer.MAX_VALUE));
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

    // Police Patrol System — Getters/Setters (delegieren an NPCPoliceData)

    public NPCPoliceData getPoliceData() {
        return policeData;
    }

    @Nullable
    public BlockPos getPoliceStation() {
        return policeData.getPoliceStation();
    }

    public void setPoliceStation(@Nullable BlockPos policeStation) {
        policeData.setPoliceStation(policeStation);
    }

    public List<BlockPos> getPatrolPoints() {
        return policeData.getPatrolPoints();
    }

    public void addPatrolPoint(BlockPos point) {
        policeData.addPatrolPoint(point);
    }

    public void removePatrolPoint(int index) {
        policeData.removePatrolPoint(index);
    }

    public void clearPatrolPoints() {
        policeData.clearPatrolPoints();
    }

    public int getCurrentPatrolIndex() {
        return policeData.getCurrentPatrolIndex();
    }

    public void setCurrentPatrolIndex(int index) {
        policeData.setCurrentPatrolIndex(index);
    }

    public void incrementPatrolIndex() {
        policeData.incrementPatrolIndex();
    }

    public long getPatrolArrivalTime() {
        return policeData.getPatrolArrivalTime();
    }

    public void setPatrolArrivalTime(long time) {
        policeData.setPatrolArrivalTime(time);
    }

    public long getStationArrivalTime() {
        return policeData.getStationArrivalTime();
    }

    public void setStationArrivalTime(long time) {
        policeData.setStationArrivalTime(time);
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
        final private List<ShopEntry> entries;

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

    // ═══════════════════════════════════════════════════════════
    // WAREHOUSE INTEGRATION (NEU)
    // ═══════════════════════════════════════════════════════════

    @Nullable
    public BlockPos getAssignedWarehouse() {
        return assignedWarehouse;
    }

    public void setAssignedWarehouse(@Nullable BlockPos pos) {
        this.assignedWarehouse = pos;
    }

    public boolean hasWarehouse() {
        // Direktes Warehouse (für VERKAEUFER)
        if (assignedWarehouse != null) {
            return true;
        }

        // Plot-basiertes Warehouse (für ABSCHLEPPER)
        if (npcType == NPCType.ABSCHLEPPER && workLocation != null) {
            de.rolandsw.schedulemc.region.PlotRegion plot =
                de.rolandsw.schedulemc.region.PlotManager.getPlotAt(workLocation);
            return plot != null && plot.getType().isTowingYard() && plot.getWarehouseLocation() != null;
        }

        return false;
    }

    /**
     * Gibt Warehouse BlockEntity zurück (wenn vorhanden)
     * Für VERKAEUFER: Nutzt assignedWarehouse
     * Für ABSCHLEPPER: Sucht Warehouse über Towing Yard Plot
     */
    @Nullable
    public de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity getWarehouseEntity(net.minecraft.world.level.Level level) {
        BlockPos warehousePos = null;

        // Methode 1: Direkt zugewiesenes Warehouse (für VERKAEUFER)
        if (assignedWarehouse != null) {
            warehousePos = assignedWarehouse;
        }
        // Methode 2: Warehouse über Plot finden (für ABSCHLEPPER)
        else if (npcType == NPCType.ABSCHLEPPER && workLocation != null) {
            de.rolandsw.schedulemc.region.PlotRegion plot =
                de.rolandsw.schedulemc.region.PlotManager.getPlotAt(workLocation);
            if (plot != null && plot.getType().isTowingYard()) {
                warehousePos = plot.getWarehouseLocation();
            }
        }

        // Warehouse BlockEntity holen
        if (warehousePos != null) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(warehousePos);
            if (be instanceof de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity) {
                return (de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity) be;
            }
        }

        return null;
    }

    /**
     * Prüft ob Item im Warehouse verfügbar ist (für Shop-System)
     */
    public boolean canSellItemFromWarehouse(net.minecraft.world.level.Level level, ShopEntry entry, int amount) {
        // Falls unlimited: wie bisher
        if (entry.isUnlimited()) {
            return true;
        }

        // Falls Warehouse vorhanden, prüfe dort
        if (hasWarehouse()) {
            de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity warehouse = getWarehouseEntity(level);
            if (warehouse != null) {
                return warehouse.hasStock(entry.getItem().getItem(), amount);
            }
        }

        // Fallback: Nutze altes Stock-System
        return entry.hasStock(amount);
    }

    /**
     * Wird aufgerufen wenn Item verkauft wird (reduziert Warehouse-Stock)
     */
    public void onItemSoldFromWarehouse(net.minecraft.world.level.Level level, ShopEntry entry, int amount, int totalPrice) {
        // Warehouse-Integration
        if (hasWarehouse()) {
            de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity warehouse = getWarehouseEntity(level);
            if (warehouse != null) {
                // Stock aus Warehouse entfernen
                warehouse.removeItem(entry.getItem().getItem(), amount);

                // Erlös ans Shop-Konto senden
                String shopId = warehouse.getShopId();
                if (shopId != null) {
                    de.rolandsw.schedulemc.economy.ShopAccount account =
                        de.rolandsw.schedulemc.economy.ShopAccountManager.getAccount(shopId);
                    if (account != null) {
                        account.addRevenue(level, totalPrice, "Verkauf");
                    }
                }
                return;
            }
        }

        // Fallback: Nutze altes System
        entry.reduceStock(amount);
    }
}
