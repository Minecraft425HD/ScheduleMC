package de.rolandsw.schedulemc.npc.life.companion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * CompanionData - Daten eines Begleiters
 *
 * Speichert alle Informationen über einen Begleiter:
 * - Typ und Name
 * - Besitzer
 * - Inventar
 * - Zustand und Befehle
 */
public class CompanionData {

    // ═══════════════════════════════════════════════════════════
    // COMPANION STATES
    // ═══════════════════════════════════════════════════════════

    public enum CompanionState {
        /** Folgt dem Spieler */
        FOLLOWING,
        /** Wartet an einem Ort */
        WAITING,
        /** Erkundet die Umgebung */
        EXPLORING,
        /** Kämpft */
        FIGHTING,
        /** Ruht sich aus */
        RESTING,
        /** Handelt */
        TRADING,
        /** Tot / Bewusstlos */
        INCAPACITATED
    }

    public enum CompanionCommand {
        /** Folge mir */
        FOLLOW,
        /** Warte hier */
        STAY,
        /** Erkunde die Umgebung */
        SCOUT,
        /** Greife an */
        ATTACK,
        /** Verteidige */
        DEFEND,
        /** Heile */
        HEAL,
        /** Komm zurück */
        RETURN,
        /** Frei (eigene Entscheidungen) */
        FREE
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final UUID companionUUID;
    private final CompanionType type;
    private String name;

    /** Besitzer des Begleiters */
    @Nullable
    private UUID ownerUUID;

    /** Aktueller Zustand */
    private CompanionState state = CompanionState.FOLLOWING;

    /** Aktueller Befehl */
    private CompanionCommand currentCommand = CompanionCommand.FOLLOW;

    /** Warte-Position (wenn state = WAITING) */
    @Nullable
    private BlockPos waitPosition;

    /** Inventar */
    private final NonNullList<ItemStack> inventory;

    /** Erfahrung und Level */
    private int experience = 0;
    private int level = 1;

    /** Loyalität (0-100) */
    private int loyalty = 50;

    /** Gesundheit */
    private float health;
    private float maxHealth;

    /** Hunger/Zufriedenheit */
    private float satisfaction = 100.0f;

    /** Zeit seit letzter Interaktion mit Besitzer (in Ticks) */
    private int ticksSinceOwnerInteraction = 0;

    /** Ist der Begleiter beschwörbar (z.B. nach Tod)? */
    private boolean summonable = true;
    private int respawnCooldown = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public CompanionData(UUID companionUUID, CompanionType type, String name) {
        this.companionUUID = companionUUID;
        this.type = type;
        this.name = name;
        this.inventory = NonNullList.withSize(type.getInventorySize(), ItemStack.EMPTY);
        this.maxHealth = type.getBaseHealth();
        this.health = maxHealth;
    }

    // ═══════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt den Begleiter auf "Folgen"
     */
    public void setFollowing() {
        this.state = CompanionState.FOLLOWING;
        this.currentCommand = CompanionCommand.FOLLOW;
        this.waitPosition = null;
    }

    /**
     * Setzt den Begleiter auf "Warten"
     */
    public void setWaiting(BlockPos position) {
        this.state = CompanionState.WAITING;
        this.currentCommand = CompanionCommand.STAY;
        this.waitPosition = position;
    }

    /**
     * Gibt einen Befehl
     */
    public boolean giveCommand(CompanionCommand command) {
        // Prüfe ob Befehl erlaubt ist
        if (!isCommandAllowed(command)) {
            return false;
        }

        this.currentCommand = command;

        // Zustand basierend auf Befehl ändern
        switch (command) {
            case FOLLOW, RETURN -> state = CompanionState.FOLLOWING;
            case STAY -> state = CompanionState.WAITING;
            case SCOUT -> {
                if (type.canScout()) state = CompanionState.EXPLORING;
            }
            case ATTACK, DEFEND -> state = CompanionState.FIGHTING;
            case HEAL -> state = CompanionState.RESTING;
            case FREE -> {} // Begleiter entscheidet selbst
        }

        return true;
    }

    /**
     * Prüft ob ein Befehl erlaubt ist
     */
    public boolean isCommandAllowed(CompanionCommand command) {
        if (state == CompanionState.INCAPACITATED) {
            return false;
        }

        return switch (command) {
            case ATTACK, DEFEND -> type.canFight();
            case SCOUT -> type.canScout();
            case HEAL -> type.canHeal();
            default -> true;
        };
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Tick aufgerufen
     */
    public void tick() {
        ticksSinceOwnerInteraction++;

        // Zufriedenheit sinkt über Zeit
        if (ticksSinceOwnerInteraction > 6000) { // 5 Minuten
            satisfaction = Math.max(0, satisfaction - 0.01f);
        }

        // Loyalität sinkt bei niedriger Zufriedenheit
        if (satisfaction < 20 && ticksSinceOwnerInteraction % 1200 == 0) {
            loyalty = Math.max(0, loyalty - 1);
        }

        // Respawn-Cooldown
        if (respawnCooldown > 0) {
            respawnCooldown--;
            if (respawnCooldown <= 0) {
                summonable = true;
            }
        }
    }

    /**
     * Interaktion mit Besitzer
     */
    public void onOwnerInteraction() {
        ticksSinceOwnerInteraction = 0;
        satisfaction = Math.min(100, satisfaction + 5);
    }

    /**
     * Begleiter wurde besiegt
     */
    public void onIncapacitated() {
        state = CompanionState.INCAPACITATED;
        summonable = false;
        respawnCooldown = 6000; // 5 Minuten
    }

    /**
     * Begleiter wiederbelebt
     */
    public void onRevive() {
        state = CompanionState.FOLLOWING;
        health = maxHealth * 0.5f;
        summonable = true;
        respawnCooldown = 0;
    }

    // ═══════════════════════════════════════════════════════════
    // EXPERIENCE / LEVELING
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt Erfahrung hinzu
     */
    public void addExperience(int amount) {
        experience += amount;

        // Level-Up prüfen
        int requiredXP = getRequiredExperience();
        while (experience >= requiredXP && level < 10) {
            experience -= requiredXP;
            levelUp();
            requiredXP = getRequiredExperience();
        }
    }

    /**
     * Berechnet benötigte XP für nächstes Level
     */
    public int getRequiredExperience() {
        return 100 * level * level;
    }

    /**
     * Level-Up
     */
    private void levelUp() {
        level++;
        maxHealth += 2;
        health = maxHealth;
        loyalty = Math.min(100, loyalty + 5);
    }

    // ═══════════════════════════════════════════════════════════
    // INVENTORY
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt das Inventar
     */
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    /**
     * Fügt ein Item zum Inventar hinzu
     */
    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack existing = inventory.get(i);
            if (existing.isEmpty()) {
                inventory.set(i, stack.copy());
                return true;
            }
            if (ItemStack.isSameItemSameTags(existing, stack) &&
                existing.getCount() < existing.getMaxStackSize()) {
                int space = existing.getMaxStackSize() - existing.getCount();
                int toAdd = Math.min(space, stack.getCount());
                existing.grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return true;
            }
        }
        return false;
    }

    /**
     * Prüft ob das Inventar voll ist
     */
    public boolean isInventoryFull() {
        for (ItemStack stack : inventory) {
            if (stack.isEmpty()) return false;
        }
        return true;
    }

    /**
     * Leert das Inventar
     */
    public void clearInventory() {
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getCompanionUUID() { return companionUUID; }
    public CompanionType getType() { return type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public boolean hasOwner() { return ownerUUID != null; }

    public CompanionState getState() { return state; }
    public CompanionCommand getCurrentCommand() { return currentCommand; }
    @Nullable public BlockPos getWaitPosition() { return waitPosition; }

    public int getExperience() { return experience; }
    public int getLevel() { return level; }
    public int getLoyalty() { return loyalty; }
    public void setLoyalty(int loyalty) { this.loyalty = Math.max(0, Math.min(100, loyalty)); }
    public void addLoyalty(int amount) { setLoyalty(loyalty + amount); }

    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public void setHealth(float health) { this.health = Math.max(0, Math.min(maxHealth, health)); }
    public void heal(float amount) { setHealth(health + amount); }
    public void damage(float amount) { setHealth(health - amount); }
    public boolean isDead() { return health <= 0; }

    public float getSatisfaction() { return satisfaction; }
    public void setSatisfaction(float satisfaction) { this.satisfaction = Math.max(0, Math.min(100, satisfaction)); }

    public boolean isSummonable() { return summonable; }
    public int getRespawnCooldown() { return respawnCooldown; }

    /**
     * Berechnet die aktuelle Angriffsstärke
     */
    public float getAttackDamage() {
        return type.getBaseAttack() * (1 + (level - 1) * 0.1f) * type.getCombatMultiplier();
    }

    /**
     * Berechnet die Bewegungsgeschwindigkeit
     */
    public float getMoveSpeed() {
        return 0.25f * type.getSpeedMultiplier() * (1 + (level - 1) * 0.05f);
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("companionUUID", companionUUID);
        tag.putString("type", type.name());
        tag.putString("name", name);

        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
        }

        tag.putString("state", state.name());
        tag.putString("command", currentCommand.name());

        if (waitPosition != null) {
            tag.putInt("waitX", waitPosition.getX());
            tag.putInt("waitY", waitPosition.getY());
            tag.putInt("waitZ", waitPosition.getZ());
        }

        tag.putInt("experience", experience);
        tag.putInt("level", level);
        tag.putInt("loyalty", loyalty);
        tag.putFloat("health", health);
        tag.putFloat("maxHealth", maxHealth);
        tag.putFloat("satisfaction", satisfaction);
        tag.putInt("ticksSinceOwner", ticksSinceOwnerInteraction);
        tag.putBoolean("summonable", summonable);
        tag.putInt("respawnCooldown", respawnCooldown);

        // Inventar
        ListTag inventoryTag = new ListTag();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt("slot", i);
                stack.save(slotTag);
                inventoryTag.add(slotTag);
            }
        }
        tag.put("inventory", inventoryTag);

        return tag;
    }

    public static CompanionData load(CompoundTag tag) {
        UUID companionUUID = tag.getUUID("companionUUID");
        CompanionType type = CompanionType.valueOf(tag.getString("type"));
        String name = tag.getString("name");

        CompanionData data = new CompanionData(companionUUID, type, name);

        if (tag.hasUUID("ownerUUID")) {
            data.ownerUUID = tag.getUUID("ownerUUID");
        }

        data.state = CompanionState.valueOf(tag.getString("state"));
        data.currentCommand = CompanionCommand.valueOf(tag.getString("command"));

        if (tag.contains("waitX")) {
            data.waitPosition = new BlockPos(
                tag.getInt("waitX"),
                tag.getInt("waitY"),
                tag.getInt("waitZ")
            );
        }

        data.experience = tag.getInt("experience");
        data.level = tag.getInt("level");
        data.loyalty = tag.getInt("loyalty");
        data.health = tag.getFloat("health");
        data.maxHealth = tag.getFloat("maxHealth");
        data.satisfaction = tag.getFloat("satisfaction");
        data.ticksSinceOwnerInteraction = tag.getInt("ticksSinceOwner");
        data.summonable = tag.getBoolean("summonable");
        data.respawnCooldown = tag.getInt("respawnCooldown");

        // Inventar
        ListTag inventoryTag = tag.getList("inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < inventoryTag.size(); i++) {
            CompoundTag slotTag = inventoryTag.getCompound(i);
            int slot = slotTag.getInt("slot");
            ItemStack stack = ItemStack.of(slotTag);
            if (slot >= 0 && slot < data.inventory.size()) {
                data.inventory.set(slot, stack);
            }
        }

        return data;
    }

    @Override
    public String toString() {
        return String.format("CompanionData{name='%s', type=%s, level=%d, state=%s}",
            name, type, level, state);
    }
}
