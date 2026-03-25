package de.rolandsw.schedulemc.npc.data;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kerndaten eines Custom NPCs.
 * Felder wurden aufgeteilt in:
 *  - NPCLocationData  (home, work, leisure, warehouse)
 *  - NPCScheduleData  (workStart, workEnd, homeTime)
 *  - NPCPoliceData    (station, patrolPoints, timing)
 *  - NPCShopData      (buyShop, sellShop)
 */
public class NPCData {
    private String npcName;
    private String skinFileName;
    private UUID npcUUID;

    // NPC Typ System
    private NPCType npcType;
    private MerchantCategory merchantCategory;
    private BankCategory bankCategory;
    private ServiceCategory serviceCategory;

    // Dialog System
    private List<DialogEntry> dialogEntries;
    private int currentDialogIndex;

    // Erweiterbare Features
    private CompoundTag customData;

    // Verhalten
    private NPCBehavior behavior;

    // Sub-Objekte
    private NPCLocationData locationData;
    private NPCScheduleData scheduleData;
    private NPCPoliceData   policeData;
    private NPCShopData     shopData;

    // Inventar und Geldbörse (nur BEWOHNER und VERKAEUFER)
    private NonNullList<ItemStack> inventory;
    private int wallet;
    private long lastDailyIncome;

    // Missions-System
    private List<String> missionIds;

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
        this.customData = new CompoundTag();
        this.behavior = new NPCBehavior();
        this.locationData = new NPCLocationData();
        this.scheduleData = new NPCScheduleData();
        this.policeData   = new NPCPoliceData();
        this.shopData     = new NPCShopData();
        this.inventory = NonNullList.withSize(9, ItemStack.EMPTY);
        this.wallet = 0;
        this.lastDailyIncome = -1;
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
    // NBT Serialization
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save(CompoundTag tag) {
        saveBasicData(tag);
        saveDialogData(tag);
        shopData.save(tag);
        locationData.save(tag);
        policeData.save(tag);
        scheduleData.save(tag);
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
        shopData.load(tag);
        locationData.load(tag);
        policeData.load(tag);
        scheduleData.load(tag);
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
            if (tag.contains("Wallet")) wallet = tag.getInt("Wallet");
            if (tag.contains("LastDailyIncome")) lastDailyIncome = tag.getLong("LastDailyIncome");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Sub-Objekt Accessoren
    // ═══════════════════════════════════════════════════════════

    public NPCLocationData getLocationData() { return locationData; }
    public NPCScheduleData getScheduleData() { return scheduleData; }
    public NPCPoliceData   getPoliceData()   { return policeData; }
    public NPCShopData     getShopData()     { return shopData; }

    // ═══════════════════════════════════════════════════════════
    // Kern-Getter & Setter
    // ═══════════════════════════════════════════════════════════

    public String getNpcName() { return npcName; }
    public void setNpcName(String npcName) { this.npcName = npcName; }

    public String getSkinFileName() { return skinFileName; }
    public void setSkinFileName(String skinFileName) { this.skinFileName = skinFileName; }

    public NPCType getNpcType() { return npcType; }
    public void setNpcType(NPCType npcType) { this.npcType = npcType; }

    public MerchantCategory getMerchantCategory() { return merchantCategory; }
    public void setMerchantCategory(MerchantCategory merchantCategory) { this.merchantCategory = merchantCategory; }

    public BankCategory getBankCategory() { return bankCategory; }
    public void setBankCategory(BankCategory bankCategory) { this.bankCategory = bankCategory; }

    public ServiceCategory getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(ServiceCategory serviceCategory) { this.serviceCategory = serviceCategory; }

    public UUID getNpcUUID() { return npcUUID; }

    public List<String> getMissionIds() { return missionIds; }
    public void setMissionIds(List<String> missionIds) {
        this.missionIds = missionIds != null ? missionIds : new ArrayList<>();
    }

    public List<DialogEntry> getDialogEntries() { return dialogEntries; }
    public void addDialogEntry(DialogEntry entry) { dialogEntries.add(entry); }

    public DialogEntry getCurrentDialog() {
        if (dialogEntries.isEmpty()) {
            return new DialogEntry(Component.translatable("npc.dialog.fallback").getString(), "");
        }
        if (currentDialogIndex >= dialogEntries.size()) currentDialogIndex = 0;
        return dialogEntries.get(currentDialogIndex);
    }

    public void nextDialog() {
        currentDialogIndex = (currentDialogIndex + 1) % Math.max(1, dialogEntries.size());
    }

    public CompoundTag getCustomData() { return customData; }
    public NPCBehavior getBehavior() { return behavior; }

    // Inventar und Geldbörse
    public NonNullList<ItemStack> getInventory() { return inventory; }

    public ItemStack getInventoryItem(int slot) {
        if (slot >= 0 && slot < inventory.size()) return inventory.get(slot);
        return ItemStack.EMPTY;
    }

    public void setInventoryItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.size()) inventory.set(slot, stack);
    }

    public int getWallet() { return wallet; }
    public void setWallet(int amount) { this.wallet = Math.max(0, amount); }

    public void addMoney(int amount) {
        this.wallet = (int) Math.max(0L, Math.min((long) this.wallet + amount, Integer.MAX_VALUE));
    }

    public boolean removeMoney(int amount) {
        if (wallet >= amount) { wallet -= amount; return true; }
        return false;
    }

    public long getLastDailyIncome() { return lastDailyIncome; }
    public void setLastDailyIncome(long day) { this.lastDailyIncome = day; }

    public boolean hasInventoryAndWallet() {
        return npcType == NPCType.BEWOHNER || npcType == NPCType.VERKAEUFER;
    }

    // ═══════════════════════════════════════════════════════════
    // Warehouse-Methoden (bleiben in NPCData wegen npcType-Kontext)
    // ═══════════════════════════════════════════════════════════

    public boolean hasWarehouse() {
        if (locationData.getAssignedWarehouse() != null) return true;
        if (npcType == NPCType.ABSCHLEPPER && locationData.getWorkLocation() != null) {
            de.rolandsw.schedulemc.region.PlotRegion plot =
                de.rolandsw.schedulemc.region.PlotManager.getPlotAt(locationData.getWorkLocation());
            return plot != null && plot.getType().isTowingYard() && plot.getWarehouseLocation() != null;
        }
        return false;
    }

    public de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity getWarehouseEntity(
            net.minecraft.world.level.Level level) {
        net.minecraft.core.BlockPos warehousePos = null;
        if (locationData.getAssignedWarehouse() != null) {
            warehousePos = locationData.getAssignedWarehouse();
        } else if (npcType == NPCType.ABSCHLEPPER && locationData.getWorkLocation() != null) {
            de.rolandsw.schedulemc.region.PlotRegion plot =
                de.rolandsw.schedulemc.region.PlotManager.getPlotAt(locationData.getWorkLocation());
            if (plot != null && plot.getType().isTowingYard()) {
                warehousePos = plot.getWarehouseLocation();
            }
        }
        if (warehousePos != null) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(warehousePos);
            if (be instanceof de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity w) return w;
        }
        return null;
    }

    public boolean canSellItemFromWarehouse(net.minecraft.world.level.Level level, ShopEntry entry, int amount) {
        if (entry.isUnlimited()) return true;
        if (hasWarehouse()) {
            de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity wh = getWarehouseEntity(level);
            if (wh != null) return wh.hasStock(entry.getItem().getItem(), amount);
        }
        return entry.hasStock(amount);
    }

    public void onItemSoldFromWarehouse(net.minecraft.world.level.Level level, ShopEntry entry,
                                        int amount, int totalPrice) {
        if (hasWarehouse()) {
            de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity wh = getWarehouseEntity(level);
            if (wh != null) {
                wh.removeItem(entry.getItem().getItem(), amount);
                String shopId = wh.getShopId();
                if (shopId != null) {
                    de.rolandsw.schedulemc.economy.ShopAccount account =
                        de.rolandsw.schedulemc.economy.ShopAccountManager.getAccount(shopId);
                    if (account != null) account.addRevenue(level, totalPrice, "Verkauf");
                }
                return;
            }
        }
        entry.reduceStock(amount);
    }

    // ═══════════════════════════════════════════════════════════
    // Inner Classes (Dialog + Behavior — bleiben in NPCData)
    // ═══════════════════════════════════════════════════════════

    public static class DialogEntry {
        private String text;
        private String response;

        public DialogEntry() { this.text = ""; this.response = ""; }
        public DialogEntry(String text, String response) { this.text = text; this.response = response; }

        public CompoundTag save(CompoundTag tag) {
            tag.putString("Text", text);
            tag.putString("Response", response);
            return tag;
        }
        public void load(CompoundTag tag) {
            text = tag.getString("Text");
            response = tag.getString("Response");
        }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
    }

    public static class NPCBehavior {
        private boolean canMove;
        private boolean lookAtPlayer;
        private float movementSpeed;

        public NPCBehavior() {
            this.canMove = false;
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
        public boolean canMove() { return canMove; }
        public void setCanMove(boolean canMove) { this.canMove = canMove; }
        public boolean shouldLookAtPlayer() { return lookAtPlayer; }
        public void setLookAtPlayer(boolean lookAtPlayer) { this.lookAtPlayer = lookAtPlayer; }
        public float getMovementSpeed() { return movementSpeed; }
        public void setMovementSpeed(float movementSpeed) { this.movementSpeed = movementSpeed; }
    }
}
