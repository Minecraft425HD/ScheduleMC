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
    private List<BlockPos> freetimeLocations; // Freizeitbereiche (Park, Café, etc.)

    // Schedule System
    private List<ScheduleEntry> schedule; // Zeitplan-Einträge

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
        this.freetimeLocations = new ArrayList<>();
        this.schedule = new ArrayList<>();
        createDefaultSchedule();
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

        // Freetime Locations
        ListTag freetimeList = new ListTag();
        for (BlockPos pos : freetimeLocations) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            freetimeList.add(posTag);
        }
        tag.put("FreetimeLocations", freetimeList);

        // Schedule
        ListTag scheduleList = new ListTag();
        for (ScheduleEntry entry : schedule) {
            scheduleList.add(entry.save(new CompoundTag()));
        }
        tag.put("Schedule", scheduleList);

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

        // Freetime Locations
        freetimeLocations.clear();
        if (tag.contains("FreetimeLocations")) {
            ListTag freetimeList = tag.getList("FreetimeLocations", Tag.TAG_COMPOUND);
            for (int i = 0; i < freetimeList.size(); i++) {
                CompoundTag posTag = freetimeList.getCompound(i);
                freetimeLocations.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }

        // Schedule
        schedule.clear();
        if (tag.contains("Schedule")) {
            ListTag scheduleList = tag.getList("Schedule", Tag.TAG_COMPOUND);
            for (int i = 0; i < scheduleList.size(); i++) {
                ScheduleEntry entry = new ScheduleEntry();
                entry.load(scheduleList.getCompound(i));
                schedule.add(entry);
            }
        }
        // Falls kein Schedule geladen wurde, erstelle Default
        if (schedule.isEmpty()) {
            createDefaultSchedule();
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

    public List<BlockPos> getFreetimeLocations() {
        return freetimeLocations;
    }

    public void addFreetimeLocation(BlockPos location) {
        this.freetimeLocations.add(location);
    }

    public void removeFreetimeLocation(int index) {
        if (index >= 0 && index < freetimeLocations.size()) {
            this.freetimeLocations.remove(index);
        }
    }

    public void clearFreetimeLocations() {
        this.freetimeLocations.clear();
    }

    public List<ScheduleEntry> getSchedule() {
        return schedule;
    }

    public void addScheduleEntry(ScheduleEntry entry) {
        this.schedule.add(entry);
    }

    public void removeScheduleEntry(int index) {
        if (index >= 0 && index < schedule.size()) {
            this.schedule.remove(index);
        }
    }

    public void clearSchedule() {
        this.schedule.clear();
    }

    /**
     * Erstellt einen Standard-Zeitplan:
     * - 06:00-18:00 (0-12000): Arbeit
     * - 18:00-22:00 (12000-16000): Freizeit
     * - 22:00-06:00 (16000-24000/0): Zuhause
     */
    public void createDefaultSchedule() {
        schedule.clear();
        // Arbeit: 06:00-18:00 (0-12000 Ticks)
        schedule.add(new ScheduleEntry(0, 12000, ActivityType.WORK));
        // Freizeit: 18:00-22:00 (12000-16000 Ticks)
        schedule.add(new ScheduleEntry(12000, 16000, ActivityType.FREETIME));
        // Zuhause: 22:00-06:00 (16000-24000 Ticks, dann 0)
        schedule.add(new ScheduleEntry(16000, 24000, ActivityType.HOME));
    }

    /**
     * Gibt den aktuellen Schedule-Eintrag basierend auf der Tageszeit zurück
     */
    @Nullable
    public ScheduleEntry getCurrentScheduleEntry(long dayTime) {
        for (ScheduleEntry entry : schedule) {
            if (entry.isActive(dayTime)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gibt die Ziel-Location für einen Schedule-Eintrag zurück
     * Berücksichtigt ActivityType und targetLocation/locationIndex
     */
    @Nullable
    public BlockPos getTargetLocationForEntry(ScheduleEntry entry) {
        // Wenn eine spezifische Location gesetzt ist, diese verwenden
        if (entry.getTargetLocation() != null) {
            return entry.getTargetLocation();
        }

        // Sonst basierend auf ActivityType
        switch (entry.getActivityType()) {
            case HOME:
                return homeLocation;
            case WORK:
                return workLocation;
            case FREETIME:
                // Verwende locationIndex wenn gesetzt
                if (entry.getLocationIndex() >= 0 && entry.getLocationIndex() < freetimeLocations.size()) {
                    return freetimeLocations.get(entry.getLocationIndex());
                }
                // Sonst zufällige Freetime-Location
                if (!freetimeLocations.isEmpty()) {
                    int randomIndex = (int) (Math.random() * freetimeLocations.size());
                    return freetimeLocations.get(randomIndex);
                }
                return null;
            default:
                return null;
        }
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
