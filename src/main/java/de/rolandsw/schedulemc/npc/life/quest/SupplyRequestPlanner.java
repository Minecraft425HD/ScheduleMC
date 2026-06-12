package de.rolandsw.schedulemc.npc.life.quest;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.data.ShopEntry;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.NPCTraits;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Plant den Inhalt einer Warenanfrage: welches Item, wie viel, zu welchem Preis.
 * Pure Logik (bis auf Registry-Lookups) — gut testbar.
 */
public final class SupplyRequestPlanner {

    /** Alltagsbedarf für CITIZEN-NPCs (Item + Stückwert in €). */
    private static final List<CitizenNeed> CITIZEN_NEEDS = List.of(
        new CitizenNeed(Items.BREAD, 4),
        new CitizenNeed(Items.APPLE, 3),
        new CitizenNeed(Items.COOKED_BEEF, 6),
        new CitizenNeed(Items.EGG, 2),
        new CitizenNeed(Items.SUGAR, 3),
        new CitizenNeed(Items.PAPER, 2),
        new CitizenNeed(Items.COAL, 5),
        new CitizenNeed(Items.LEATHER, 7)
    );

    private record CitizenNeed(Item item, int unitValue) { }

    /** Geplante Anfrage: Item, Menge, Bezahlung. */
    public record PlannedRequest(Item item, int amount, int payment) { }

    private SupplyRequestPlanner() {
    }

    /**
     * Plant eine Anfrage für diesen NPC oder gibt null zurück, wenn der NPC
     * nichts Sinnvolles anfragen kann.
     */
    @Nullable
    public static PlannedRequest plan(CustomNPCEntity npc, Random random) {
        NPCTraits traits = npc.getLifeData() != null ? npc.getLifeData().getTraits() : new NPCTraits();

        Item item = null;
        int unitValue = 0;

        if (npc.getNpcType() == NPCType.MERCHANT && npc.getNpcData() != null) {
            List<ShopEntry> buyEntries = npc.getNpcData().getShopData().getBuyShop().getEntries();
            if (!buyEntries.isEmpty()) {
                ShopEntry entry = buyEntries.get(random.nextInt(buyEntries.size()));
                ItemStack stack = entry.getItem();
                if (!stack.isEmpty()) {
                    item = stack.getItem();
                    unitValue = Math.max(1, entry.getPrice());
                }
            }
        }
        if (item == null) {
            CitizenNeed need = CITIZEN_NEEDS.get(random.nextInt(CITIZEN_NEEDS.size()));
            item = need.item();
            unitValue = need.unitValue();
        }

        int amount = calculateAmount(traits, random);
        int payment = calculatePayment(unitValue, amount, traits);
        return new PlannedRequest(item, amount, payment);
    }

    /**
     * Menge 4-16: Gierige NPCs wollen mehr auf einmal.
     */
    public static int calculateAmount(NPCTraits traits, Random random) {
        int base = 4 + random.nextInt(9); // 4-12
        int greedBonus = Math.max(0, traits.getGreed()) / 25; // 0-4
        return Math.min(16, base + greedBonus);
    }

    /**
     * Bezahlung = Stückwert × Menge × 1.25, gierige NPCs zahlen etwas mehr
     * Aufschlag (sie brauchen die Ware dringend für ihren Profit).
     */
    public static int calculatePayment(int unitValue, int amount, NPCTraits traits) {
        float greedFactor = 1.0f + Math.max(0, traits.getGreed()) / 400f; // 1.0-1.25
        return Math.max(1, Math.round(unitValue * amount * 1.25f * greedFactor));
    }

    public static String itemRegistryId(Item item) {
        var key = ForgeRegistries.ITEMS.getKey(item);
        return key != null ? key.toString() : "minecraft:air";
    }

    @Nullable
    public static Item itemFromRegistryId(String id) {
        try {
            return ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse(id));
        } catch (Exception e) {
            return null;
        }
    }
}
