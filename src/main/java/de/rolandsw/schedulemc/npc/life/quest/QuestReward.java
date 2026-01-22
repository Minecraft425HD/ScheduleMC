package de.rolandsw.schedulemc.npc.life.quest;

import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * QuestReward - Belohnungen für abgeschlossene Quests
 *
 * Unterstützt verschiedene Belohnungstypen wie Items, Geld, Reputation.
 */
public class QuestReward {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Geldbelohnung */
    private int money;

    /** Item-Belohnungen */
    private final List<ItemStack> items = new ArrayList<>();

    /** Fraktions-Reputation */
    private final List<FactionRepReward> factionRewards = new ArrayList<>();

    /** Erfahrungspunkte (optional) */
    private int experience;

    /** Spezielle Belohnung (z.B. Zugang zu neuen Dialogen) */
    @Nullable
    private String specialReward;

    // ═══════════════════════════════════════════════════════════
    // BUILDER PATTERN
    // ═══════════════════════════════════════════════════════════

    public QuestReward() {
        this.money = 0;
        this.experience = 0;
    }

    public static QuestReward create() {
        return new QuestReward();
    }

    public QuestReward money(int amount) {
        this.money = amount;
        return this;
    }

    public QuestReward addItem(Item item, int count) {
        this.items.add(new ItemStack(item, count));
        return this;
    }

    public QuestReward addItem(ItemStack stack) {
        this.items.add(stack.copy());
        return this;
    }

    public QuestReward factionRep(Faction faction, int amount) {
        this.factionRewards.add(new FactionRepReward(faction, amount));
        return this;
    }

    public QuestReward experience(int xp) {
        this.experience = xp;
        return this;
    }

    public QuestReward special(String reward) {
        this.specialReward = reward;
        return this;
    }

    // ═══════════════════════════════════════════════════════════
    // GRANT REWARDS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gewährt alle Belohnungen an den Spieler
     */
    public void grant(ServerPlayer player, ServerLevel level) {
        // Geld (via Economy-System oder direkt)
        if (money > 0) {
            grantMoney(player, money);
        }

        // Items
        for (ItemStack stack : items) {
            grantItem(player, stack.copy());
        }

        // Fraktions-Reputation
        FactionManager factionManager = FactionManager.getManager(level);
        for (FactionRepReward rep : factionRewards) {
            factionManager.modifyReputation(player.getUUID(), rep.faction, rep.amount);
        }

        // Erfahrung
        if (experience > 0) {
            player.giveExperiencePoints(experience);
        }

        // Spezielle Belohnung
        if (specialReward != null) {
            handleSpecialReward(player, specialReward);
        }
    }

    /**
     * Gewährt Geld an den Spieler
     */
    private void grantMoney(ServerPlayer player, int amount) {
        // Hier Integration mit dem Währungssystem
        // Für jetzt: Speichere in Player's persistent data
        CompoundTag playerData = player.getPersistentData();
        CompoundTag walletTag = playerData.getCompound("ScheduleMC");
        long currentMoney = walletTag.getLong("wallet");
        walletTag.putLong("wallet", currentMoney + amount);
        playerData.put("ScheduleMC", walletTag);
    }

    /**
     * Gibt ein Item an den Spieler
     */
    private void grantItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            // Inventar voll - droppe das Item
            player.drop(stack, false);
        }
    }

    /**
     * Verarbeitet spezielle Belohnungen
     */
    private void handleSpecialReward(ServerPlayer player, String reward) {
        // Spezielle Belohnungen basierend auf String
        switch (reward) {
            case "unlock_trader_discount" -> {
                CompoundTag data = player.getPersistentData().getCompound("ScheduleMC");
                data.putBoolean("traderDiscount", true);
                player.getPersistentData().put("ScheduleMC", data);
            }
            case "unlock_black_market" -> {
                CompoundTag data = player.getPersistentData().getCompound("ScheduleMC");
                data.putBoolean("blackMarketAccess", true);
                player.getPersistentData().put("ScheduleMC", data);
            }
            case "become_informant" -> {
                // Spieler wird zum Informanten
                CompoundTag data = player.getPersistentData().getCompound("ScheduleMC");
                data.putBoolean("isInformant", true);
                player.getPersistentData().put("ScheduleMC", data);
            }
            // Weitere spezielle Belohnungen können hier hinzugefügt werden
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public int getMoney() { return money; }
    public List<ItemStack> getItems() { return new ArrayList<>(items); }
    public List<FactionRepReward> getFactionRewards() { return new ArrayList<>(factionRewards); }
    public int getExperience() { return experience; }
    @Nullable public String getSpecialReward() { return specialReward; }

    /**
     * Prüft ob die Belohnung leer ist
     */
    public boolean isEmpty() {
        return money <= 0 && items.isEmpty() && factionRewards.isEmpty() &&
               experience <= 0 && specialReward == null;
    }

    /**
     * Berechnet den geschätzten Gesamtwert
     */
    public int getEstimatedValue() {
        int value = money;
        value += experience / 10;

        // Item-Werte schätzen (vereinfacht)
        for (ItemStack stack : items) {
            value += switch (stack.getRarity()) {
                case COMMON -> 10 * stack.getCount();
                case UNCOMMON -> 50 * stack.getCount();
                case RARE -> 200 * stack.getCount();
                case EPIC -> 500 * stack.getCount();
            };
        }

        return value;
    }

    /**
     * Formatierte Beschreibung der Belohnung
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        if (money > 0) {
            sb.append(money).append(" Münzen");
        }

        for (ItemStack stack : items) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(stack.getCount()).append("x ").append(stack.getHoverName().getString());
        }

        for (FactionRepReward rep : factionRewards) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(rep.amount > 0 ? "+" : "").append(rep.amount)
              .append(" ").append(rep.faction.getDisplayName()).append("-Ruf");
        }

        if (experience > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(experience).append(" XP");
        }

        if (specialReward != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Spezial: ").append(specialReward);
        }

        return sb.length() > 0 ? sb.toString() : "Keine Belohnung";
    }

    // ═══════════════════════════════════════════════════════════
    // SCALING
    // ═══════════════════════════════════════════════════════════

    /**
     * Skaliert die Belohnung mit einem Multiplikator
     */
    public QuestReward scale(float multiplier) {
        QuestReward scaled = new QuestReward();
        scaled.money = Math.round(this.money * multiplier);
        scaled.experience = Math.round(this.experience * multiplier);
        scaled.specialReward = this.specialReward;

        // Items können nicht skaliert werden (oder Anzahl erhöhen?)
        scaled.items.addAll(this.items);

        // Factions auch skalieren
        for (FactionRepReward rep : this.factionRewards) {
            scaled.factionRewards.add(new FactionRepReward(rep.faction, Math.round(rep.amount * multiplier)));
        }

        return scaled;
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("money", money);
        tag.putInt("experience", experience);

        if (specialReward != null) {
            tag.putString("specialReward", specialReward);
        }

        // Items
        ListTag itemsTag = new ListTag();
        for (ItemStack stack : items) {
            itemsTag.add(stack.save(new CompoundTag()));
        }
        tag.put("items", itemsTag);

        // Faction Rewards
        ListTag factionTag = new ListTag();
        for (FactionRepReward rep : factionRewards) {
            CompoundTag repTag = new CompoundTag();
            repTag.putString("faction", rep.faction.name());
            repTag.putInt("amount", rep.amount);
            factionTag.add(repTag);
        }
        tag.put("factionRewards", factionTag);

        return tag;
    }

    public static QuestReward load(CompoundTag tag) {
        QuestReward reward = new QuestReward();

        reward.money = tag.getInt("money");
        reward.experience = tag.getInt("experience");

        if (tag.contains("specialReward")) {
            reward.specialReward = tag.getString("specialReward");
        }

        // Items
        ListTag itemsTag = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemsTag.size(); i++) {
            ItemStack stack = ItemStack.of(itemsTag.getCompound(i));
            if (!stack.isEmpty()) {
                reward.items.add(stack);
            }
        }

        // Faction Rewards
        ListTag factionTag = tag.getList("factionRewards", Tag.TAG_COMPOUND);
        for (int i = 0; i < factionTag.size(); i++) {
            CompoundTag repTag = factionTag.getCompound(i);
            Faction faction = Faction.valueOf(repTag.getString("faction"));
            int amount = repTag.getInt("amount");
            reward.factionRewards.add(new FactionRepReward(faction, amount));
        }

        return reward;
    }

    // ═══════════════════════════════════════════════════════════
    // INNER CLASS
    // ═══════════════════════════════════════════════════════════

    /**
     * Fraktions-Reputations-Belohnung
     */
    public static class FactionRepReward {
        public final Faction faction;
        public final int amount;

        public FactionRepReward(Faction faction, int amount) {
            this.faction = faction;
            this.amount = amount;
        }
    }

    @Override
    public String toString() {
        return "QuestReward{" + getDescription() + "}";
    }
}
