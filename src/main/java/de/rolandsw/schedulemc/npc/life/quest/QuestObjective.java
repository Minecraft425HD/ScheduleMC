package de.rolandsw.schedulemc.npc.life.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * QuestObjective - Repräsentiert ein einzelnes Questziel
 *
 * Unterstützt verschiedene Zieltypen wie Item-Sammlung, NPC-Besuch, etc.
 */
public class QuestObjective {

    // ═══════════════════════════════════════════════════════════
    // OBJECTIVE TYPES
    // ═══════════════════════════════════════════════════════════

    public enum ObjectiveType {
        /** Sammle bestimmte Items */
        COLLECT_ITEMS,
        /** Liefere Items an NPC */
        DELIVER_TO_NPC,
        /** Besuche einen bestimmten Ort */
        VISIT_LOCATION,
        /** Sprich mit einem NPC */
        TALK_TO_NPC,
        /** Töte bestimmte Entities */
        KILL_ENTITIES,
        /** Begleite NPC zu einem Ort */
        ESCORT_NPC,
        /** Finde Information (Dialog-basiert) */
        FIND_INFORMATION,
        /** Verhandle erfolgreich */
        NEGOTIATE_DEAL,
        /** Bestehe eine Zeit lang */
        SURVIVE_TIME
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String id;
    private final ObjectiveType type;
    private final String description;

    // Fortschritt
    private int currentProgress;
    private int requiredProgress;
    private boolean completed;
    private boolean failed;

    // Typ-spezifische Daten
    @Nullable private Item targetItem;
    @Nullable private UUID targetNPC;
    @Nullable private BlockPos targetLocation;
    @Nullable private String targetEntityType;
    private int locationRadius = 5;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════

    public QuestObjective(String id, ObjectiveType type, String description, int requiredProgress) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.requiredProgress = requiredProgress;
        this.currentProgress = 0;
        this.completed = false;
        this.failed = false;
    }

    // ═══════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt ein Item-Sammel-Ziel
     */
    public static QuestObjective collectItems(String id, Item item, int amount, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.COLLECT_ITEMS, description, amount);
        obj.targetItem = item;
        return obj;
    }

    /**
     * Erstellt ein Liefer-Ziel
     */
    public static QuestObjective deliverToNPC(String id, Item item, int amount, UUID npcUUID, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.DELIVER_TO_NPC, description, amount);
        obj.targetItem = item;
        obj.targetNPC = npcUUID;
        return obj;
    }

    /**
     * Erstellt ein Ort-Besuch-Ziel
     */
    public static QuestObjective visitLocation(String id, BlockPos location, int radius, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.VISIT_LOCATION, description, 1);
        obj.targetLocation = location;
        obj.locationRadius = radius;
        return obj;
    }

    /**
     * Erstellt ein NPC-Gespräch-Ziel
     */
    public static QuestObjective talkToNPC(String id, UUID npcUUID, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.TALK_TO_NPC, description, 1);
        obj.targetNPC = npcUUID;
        return obj;
    }

    /**
     * Erstellt ein Kill-Ziel
     */
    public static QuestObjective killEntities(String id, String entityType, int amount, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.KILL_ENTITIES, description, amount);
        obj.targetEntityType = entityType;
        return obj;
    }

    /**
     * Erstellt ein Eskorte-Ziel
     */
    public static QuestObjective escortNPC(String id, UUID npcUUID, BlockPos destination, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.ESCORT_NPC, description, 1);
        obj.targetNPC = npcUUID;
        obj.targetLocation = destination;
        return obj;
    }

    /**
     * Erstellt ein Informations-Ziel
     */
    public static QuestObjective findInformation(String id, String infoKey, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.FIND_INFORMATION, description, 1);
        obj.targetEntityType = infoKey; // Missbrauch des Feldes für Info-Key
        return obj;
    }

    /**
     * Erstellt ein Verhandlungs-Ziel
     */
    public static QuestObjective negotiateDeal(String id, UUID npcUUID, String description) {
        QuestObjective obj = new QuestObjective(id, ObjectiveType.NEGOTIATE_DEAL, description, 1);
        obj.targetNPC = npcUUID;
        return obj;
    }

    // ═══════════════════════════════════════════════════════════
    // PROGRESS TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt Fortschritt hinzu
     */
    public void addProgress(int amount) {
        if (completed || failed) return;

        currentProgress = Math.min(currentProgress + amount, requiredProgress);
        if (currentProgress >= requiredProgress) {
            completed = true;
        }
    }

    /**
     * Setzt den Fortschritt
     */
    public void setProgress(int progress) {
        if (completed || failed) return;

        currentProgress = Math.min(progress, requiredProgress);
        if (currentProgress >= requiredProgress) {
            completed = true;
        }
    }

    /**
     * Markiert als fehlgeschlagen
     */
    public void fail() {
        failed = true;
        completed = false;
    }

    /**
     * Prüft ob Items das Ziel erfüllen
     */
    public boolean checkItemProgress(ItemStack stack) {
        if (type != ObjectiveType.COLLECT_ITEMS && type != ObjectiveType.DELIVER_TO_NPC) {
            return false;
        }

        if (targetItem == null || stack.getItem() != targetItem) {
            return false;
        }

        addProgress(stack.getCount());
        return true;
    }

    /**
     * Prüft ob eine Position das Ziel erfüllt
     */
    public boolean checkLocationProgress(BlockPos pos) {
        if (type != ObjectiveType.VISIT_LOCATION && type != ObjectiveType.ESCORT_NPC) {
            return false;
        }

        if (targetLocation == null) return false;

        double distance = Math.sqrt(pos.distSqr(targetLocation));
        if (distance <= locationRadius) {
            addProgress(1);
            return true;
        }
        return false;
    }

    /**
     * Prüft NPC-Interaktion
     */
    public boolean checkNPCInteraction(UUID npcUUID) {
        if (type != ObjectiveType.TALK_TO_NPC && type != ObjectiveType.NEGOTIATE_DEAL) {
            return false;
        }

        if (targetNPC != null && targetNPC.equals(npcUUID)) {
            addProgress(1);
            return true;
        }
        return false;
    }

    /**
     * Prüft Kill-Fortschritt
     */
    public boolean checkKillProgress(String entityType) {
        if (type != ObjectiveType.KILL_ENTITIES) return false;

        if (targetEntityType != null && targetEntityType.equalsIgnoreCase(entityType)) {
            addProgress(1);
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() { return id; }
    public ObjectiveType getType() { return type; }
    public String getDescription() { return description; }
    public int getCurrentProgress() { return currentProgress; }
    public int getRequiredProgress() { return requiredProgress; }
    public boolean isCompleted() { return completed; }
    public boolean isFailed() { return failed; }

    @Nullable public Item getTargetItem() { return targetItem; }
    @Nullable public UUID getTargetNPC() { return targetNPC; }
    @Nullable public BlockPos getTargetLocation() { return targetLocation; }
    @Nullable public String getTargetEntityType() { return targetEntityType; }
    public int getLocationRadius() { return locationRadius; }

    /**
     * Fortschritt als Prozent
     */
    public float getProgressPercent() {
        if (requiredProgress <= 0) return completed ? 1.0f : 0.0f;
        return (float) currentProgress / requiredProgress;
    }

    /**
     * Formatierte Fortschrittsanzeige
     */
    public String getProgressString() {
        return String.format("%d/%d", currentProgress, requiredProgress);
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", id);
        tag.putString("type", type.name());
        tag.putString("description", description);
        tag.putInt("currentProgress", currentProgress);
        tag.putInt("requiredProgress", requiredProgress);
        tag.putBoolean("completed", completed);
        tag.putBoolean("failed", failed);
        tag.putInt("locationRadius", locationRadius);

        if (targetItem != null) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(targetItem);
            if (itemId != null) {
                tag.putString("targetItem", itemId.toString());
            }
        }

        if (targetNPC != null) {
            tag.putUUID("targetNPC", targetNPC);
        }

        if (targetLocation != null) {
            tag.putInt("locX", targetLocation.getX());
            tag.putInt("locY", targetLocation.getY());
            tag.putInt("locZ", targetLocation.getZ());
        }

        if (targetEntityType != null) {
            tag.putString("targetEntityType", targetEntityType);
        }

        return tag;
    }

    public static QuestObjective load(CompoundTag tag) {
        String id = tag.getString("id");
        ObjectiveType type;
        try { type = ObjectiveType.valueOf(tag.getString("type")); }
        catch (IllegalArgumentException e) { type = ObjectiveType.values()[0]; }
        String description = tag.getString("description");
        int requiredProgress = tag.getInt("requiredProgress");

        QuestObjective obj = new QuestObjective(id, type, description, requiredProgress);
        obj.currentProgress = tag.getInt("currentProgress");
        obj.completed = tag.getBoolean("completed");
        obj.failed = tag.getBoolean("failed");
        obj.locationRadius = tag.getInt("locationRadius");

        if (tag.contains("targetItem")) {
            ResourceLocation itemId = new ResourceLocation(tag.getString("targetItem"));
            obj.targetItem = ForgeRegistries.ITEMS.getValue(itemId);
        }

        if (tag.hasUUID("targetNPC")) {
            obj.targetNPC = tag.getUUID("targetNPC");
        }

        if (tag.contains("locX")) {
            obj.targetLocation = new BlockPos(
                tag.getInt("locX"),
                tag.getInt("locY"),
                tag.getInt("locZ")
            );
        }

        if (tag.contains("targetEntityType")) {
            obj.targetEntityType = tag.getString("targetEntityType");
        }

        return obj;
    }

    @Override
    public String toString() {
        return String.format("QuestObjective{id='%s', type=%s, progress=%s, completed=%s}",
            id, type, getProgressString(), completed);
    }
}
