package de.rolandsw.schedulemc.npc.data;
nimport de.rolandsw.schedulemc.util.GameConstants;

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
 * Central data model for custom NPCs in ScheduleMC, managing all NPC properties and behaviors.
 *
 * <p>This class represents the complete state of an NPC entity, including identity, appearance,
 * behavior, inventory, schedule, and specialized systems like shops, patrols, and warehouses.
 *
 * <h2>Core Features:</h2>
 * <ul>
 *   <li><b>Identity & Appearance:</b> Name, UUID, custom skin file</li>
 *   <li><b>NPC Types:</b> BEWOHNER (resident), VERKAEUFER (merchant), POLIZEI (police), BANK</li>
 *   <li><b>Dialog System:</b> Configurable conversation entries with player responses</li>
 *   <li><b>Shop System:</b> Buy/Sell inventories with pricing and stock management</li>
 *   <li><b>Schedule System:</b> Work hours, home time, and daily routines</li>
 *   <li><b>Location Management:</b> Home, workplace, leisure locations (up to 10)</li>
 *   <li><b>Police Patrol:</b> Station location and patrol points (up to 16)</li>
 *   <li><b>Inventory & Wallet:</b> 9-slot inventory and cash wallet (BEWOHNER/VERKAEUFER only)</li>
 *   <li><b>Warehouse Integration:</b> Shop NPCs can sell from assigned warehouses</li>
 *   <li><b>Behavior Settings:</b> Movement, speed, player interaction</li>
 * </ul>
 *
 * <h2>NPC Types:</h2>
 * <table border="1">
 *   <tr><th>Type</th><th>Features</th><th>Use Case</th></tr>
 *   <tr><td>BEWOHNER</td><td>Inventory, Wallet, Schedule, Leisure</td><td>General town residents</td></tr>
 *   <tr><td>VERKAEUFER</td><td>Shop, Inventory, Wallet, Work Location</td><td>Merchants and shop owners</td></tr>
 *   <tr><td>POLIZEI</td><td>Patrol System, Police Station</td><td>Law enforcement NPCs</td></tr>
 *   <tr><td>BANK</td><td>Bank Category, Dialog</td><td>Banking services</td></tr>
 * </table>
 *
 * <h2>Schedule System (Minecraft Ticks):</h2>
 * <ul>
 *   <li><b>Work Start:</b> Default 0 ticks (6:00 AM)</li>
 *   <li><b>Work End:</b> Default 13000 ticks (7:00 PM)</li>
 *   <li><b>Home Time:</b> Default 23000 ticks (5:00 AM next day)</li>
 *   <li><b>Conversion:</b> {@link GameConstants#TICKS_PER_DAY} = 1 Minecraft day</li>
 * </ul>
 *
 * <h2>Shop System:</h2>
 * <p>NPCs can have two shop inventories:
 * <ul>
 *   <li><b>Buy Shop:</b> Items the NPC sells to players</li>
 *   <li><b>Sell Shop:</b> Items the NPC buys from players</li>
 * </ul>
 * Each shop entry supports:
 * <ul>
 *   <li>Item type and pricing</li>
 *   <li>Unlimited stock OR limited inventory</li>
 *   <li>Warehouse integration for automatic restocking</li>
 * </ul>
 *
 * <h2>Warehouse Integration:</h2>
 * <p>Shop NPCs can be assigned to a {@link de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity}:
 * <ul>
 *   <li>NPC sells items directly from warehouse stock</li>
 *   <li>Revenue is automatically credited to shop account</li>
 *   <li>Stock levels are synchronized with warehouse inventory</li>
 *   <li>Falls back to traditional stock system if warehouse unavailable</li>
 * </ul>
 *
 * <h2>Police Patrol System:</h2>
 * <p>For POLIZEI type NPCs:
 * <ul>
 *   <li>Define a police station location (base)</li>
 *   <li>Configure up to 16 patrol points</li>
 *   <li>NPC automatically patrols between points</li>
 *   <li>Tracks arrival times at each location</li>
 * </ul>
 *
 * <h2>NBT Serialization:</h2>
 * <p>All data is persistently stored using Minecraft's NBT format:
 * <ul>
 *   <li>{@link #save(CompoundTag)} - Serialize to NBT</li>
 *   <li>{@link #load(CompoundTag)} - Deserialize from NBT</li>
 *   <li>Subdivided into logical sections (Basic, Dialog, Shop, Location, etc.)</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p><b>Warning:</b> This class is NOT thread-safe. All modifications should occur
 * on the server thread. For concurrent access, external synchronization is required.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create a merchant NPC
 * NPCData merchant = new NPCData("Hans", "merchant_skin.png",
 *                                 NPCType.VERKAEUFER,
 *                                 MerchantCategory.BAUMARKT);
 *
 * // Configure shop
 * merchant.getBuyShop().addEntry(new ItemStack(Items.DIAMOND), 100);
 * merchant.setWorkLocation(new BlockPos(100, 64, 200));
 *
 * // Set schedule
 * merchant.setWorkStartTime(0);      // 6:00 AM
 * merchant.setWorkEndTime(13000);    // 7:00 PM
 *
 * // Assign warehouse
 * merchant.setAssignedWarehouse(warehousePos);
 * }</pre>
 *
 * <h2>Inner Classes:</h2>
 * <ul>
 *   <li>{@link DialogEntry} - Single dialog conversation entry</li>
 *   <li>{@link ShopInventory} - Collection of shop items (buy/sell)</li>
 *   <li>{@link ShopEntry} - Individual shop item with price and stock</li>
 *   <li>{@link NPCBehavior} - Behavioral settings (movement, interaction)</li>
 * </ul>
 *
 * @see NPCType
 * @see MerchantCategory
 * @see BankCategory
 * @see de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity
 * @see de.rolandsw.schedulemc.economy.ShopAccount
 * @author ScheduleMC Development Team
 * @since 1.0.0
 */
public class NPCData {
    // Time Constants (Minecraft Ticks: GameConstants.TICKS_PER_DAY ticks = 1 day)
    private static final long WORK_END_TIME_TICKS = 13000;  // 19:00 Uhr abends (work end time)
    private static final long HOME_TIME_TICKS = 23000;      // 5:00 Uhr morgens (time to go home)

    private String npcName;
    private String skinFileName; // Dateiname des Skins (z.B. "steve.png")
    private UUID npcUUID;

    // NPC Typ System
    private NPCType npcType;
    private MerchantCategory merchantCategory; // Nur relevant wenn npcType == VERKAEUFER
    private BankCategory bankCategory; // Nur relevant wenn npcType == BANK

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
    @Nullable
    private BlockPos policeStation;  // Polizeistation
    private List<BlockPos> patrolPoints; // Bis zu 16 Patrouillenpunkte
    private int currentPatrolIndex; // Aktueller Patrol-Index
    private long patrolArrivalTime; // Letzte Ankunftszeit am Patrol-Punkt
    private long stationArrivalTime; // Letzte Ankunftszeit an der Station

    // Schedule - Zeiteinstellungen (in Minecraft Ticks, GameConstants.TICKS_PER_DAY = 1 Tag)
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
        this.bankCategory = BankCategory.BANKER;
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
        this.workStartTime = 0;                 // 6:00 Uhr morgens
        this.workEndTime = WORK_END_TIME_TICKS; // 19:00 Uhr abends
        this.homeTime = HOME_TIME_TICKS;        // 5:00 Uhr morgens (Zeit zum Schlafen)
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
        tag.putInt("CurrentDialogIndex", currentDialogIndex);
        tag.put("CustomData", customData);
        tag.put("Behavior", behavior.save(new CompoundTag()));
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
    }

    private void saveScheduleData(CompoundTag tag) {
        tag.putLong("WorkStartTime", workStartTime);
        tag.putLong("WorkEndTime", workEndTime);
        tag.putLong("HomeTime", homeTime);
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
        currentDialogIndex = tag.getInt("CurrentDialogIndex");
        customData = tag.getCompound("CustomData");
        behavior = new NPCBehavior();
        behavior.load(tag.getCompound("Behavior"));
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
    }

    private void loadScheduleData(CompoundTag tag) {
        if (tag.contains("WorkStartTime")) {
            workStartTime = tag.getLong("WorkStartTime");
        }
        if (tag.contains("WorkEndTime")) {
            workEndTime = tag.getLong("WorkEndTime");
        }
        if (tag.contains("HomeTime")) {
            homeTime = tag.getLong("HomeTime");
        }
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
     * Represents a single dialog conversation entry between an NPC and player.
     *
     * <p>Each dialog entry contains:
     * <ul>
     *   <li><b>Text:</b> The NPC's dialog message</li>
     *   <li><b>Response:</b> Optional player response text</li>
     * </ul>
     *
     * <p>Multiple dialog entries can be cycled through using {@link NPCData#nextDialog()}.
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * DialogEntry greeting = new DialogEntry("Hello traveler!", "Greetings!");
     * npcData.addDialogEntry(greeting);
     * }</pre>
     *
     * @see NPCData#getDialogEntries()
     * @see NPCData#getCurrentDialog()
     * @see NPCData#nextDialog()
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
     * Manages a collection of shop entries for buying or selling items.
     *
     * <p>NPCs have two separate shop inventories:
     * <ul>
     *   <li><b>Buy Shop:</b> Items the NPC sells to players (player buys from NPC)</li>
     *   <li><b>Sell Shop:</b> Items the NPC buys from players (player sells to NPC)</li>
     * </ul>
     *
     * <p>Each inventory can contain multiple {@link ShopEntry} items with individual
     * pricing, stock levels, and warehouse integration.
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Dynamic entry management (add, clear)</li>
     *   <li>NBT serialization for persistence</li>
     *   <li>Supports unlimited stock or limited inventory</li>
     * </ul>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * ShopInventory buyShop = npcData.getBuyShop();
     * buyShop.addEntry(new ItemStack(Items.DIAMOND), 100);
     * buyShop.addEntry(new ItemStack(Items.IRON_INGOT), 10);
     * }</pre>
     *
     * @see ShopEntry
     * @see NPCData#getBuyShop()
     * @see NPCData#getSellShop()
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
     * Represents a single item in an NPC's shop inventory with pricing and stock management.
     *
     * <p>Each shop entry defines:
     * <ul>
     *   <li><b>Item:</b> The ItemStack being traded</li>
     *   <li><b>Price:</b> Cost in currency units</li>
     *   <li><b>Stock Mode:</b> Unlimited (infinite supply) OR Limited (tracked inventory)</li>
     *   <li><b>Stock Level:</b> Current inventory count (for limited mode only)</li>
     * </ul>
     *
     * <h3>Stock Management:</h3>
     * <p><b>Unlimited Mode (default):</b>
     * <ul>
     *   <li>Item is always available for purchase</li>
     *   <li>Stock level is ignored</li>
     *   <li>Suitable for basic merchant NPCs</li>
     * </ul>
     *
     * <p><b>Limited Mode:</b>
     * <ul>
     *   <li>Item availability depends on stock level</li>
     *   <li>Stock decreases with each purchase via {@link #reduceStock(int)}</li>
     *   <li>Can integrate with warehouse for automatic restocking</li>
     *   <li>Suitable for realistic economy simulation</li>
     * </ul>
     *
     * <h3>Warehouse Integration:</h3>
     * <p>When an NPC has an assigned warehouse via {@link NPCData#setAssignedWarehouse(BlockPos)},
     * stock checks and reductions can be routed through the warehouse system instead of
     * the local stock counter. See {@link NPCData#canSellItemFromWarehouse} for details.
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * // Create unlimited stock item
     * ShopEntry diamond = new ShopEntry(new ItemStack(Items.DIAMOND), 100);
     *
     * // Create limited stock item
     * ShopEntry rare = new ShopEntry(new ItemStack(Items.NETHER_STAR), 500, false, 10);
     *
     * // Check and reduce stock
     * if (rare.hasStock(5)) {
     *     rare.reduceStock(5);
     * }
     * }</pre>
     *
     * @see ShopInventory
     * @see NPCData#canSellItemFromWarehouse
     * @see NPCData#onItemSoldFromWarehouse
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
     * Configurable behavioral settings for NPC entities.
     *
     * <p>Controls how NPCs interact with the world and players:
     * <ul>
     *   <li><b>Movement:</b> Whether NPC can walk around ({@link #canMove})</li>
     *   <li><b>Player Interaction:</b> Whether NPC looks at nearby players ({@link #lookAtPlayer})</li>
     *   <li><b>Movement Speed:</b> Walk speed multiplier (default: 0.3f)</li>
     * </ul>
     *
     * <h3>Default Behavior:</h3>
     * <ul>
     *   <li>Movement: <b>Disabled</b> (NPCs are stationary by default)</li>
     *   <li>Look at Player: <b>Enabled</b></li>
     *   <li>Movement Speed: <b>0.3f</b> (30% of normal player speed)</li>
     * </ul>
     *
     * <h3>Use Cases:</h3>
     * <ul>
     *   <li><b>Stationary Merchant:</b> canMove=false, lookAtPlayer=true</li>
     *   <li><b>Patrol Officer:</b> canMove=true, lookAtPlayer=true, speed=0.3f</li>
     *   <li><b>Background Resident:</b> canMove=true, lookAtPlayer=false, speed=0.25f</li>
     * </ul>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * NPCBehavior behavior = npcData.getBehavior();
     * behavior.setCanMove(true);
     * behavior.setMovementSpeed(0.4f);
     * behavior.setLookAtPlayer(true);
     * }</pre>
     *
     * @see NPCData#getBehavior()
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
        return assignedWarehouse != null;
    }

    /**
     * Gibt Warehouse BlockEntity zurück (wenn vorhanden)
     */
    @Nullable
    public de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity getWarehouseEntity(net.minecraft.world.level.Level level) {
        if (assignedWarehouse == null) return null;
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(assignedWarehouse);
        if (be instanceof de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity) {
            return (de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity) be;
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
