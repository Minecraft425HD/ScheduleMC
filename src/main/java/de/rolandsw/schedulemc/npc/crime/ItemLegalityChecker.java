package de.rolandsw.schedulemc.npc.crime;

import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Zentrale Definition, welche Items als illegal gelten.
 *
 * Ersetzt die zuvor an mehreren Stellen verstreuten Listen
 * (PoliceCombatHandler, IllegalActivityScanner). Erfasst alle Drogen,
 * verarbeiteten Tabak, sämtliche Verarbeitungsstufen der illegalen
 * Produktionsketten sowie deren Pflanzen-/Samen-Blöcke.
 */
public final class ItemLegalityChecker {

    /** Registry-Pfad-Fragmente, die ein Item als illegal kennzeichnen. */
    private static final List<String> ILLEGAL_FRAGMENTS = List.of(
        // Enddrogen
        "cannabis", "cocaine", "heroin", "meth", "mdma", "ecstasy",
        "lsd", "crack", "hash", "opium", "morphine",
        // Verarbeiteter Tabak
        "moist_tobacco", "dried_tobacco", "fermented_tobacco", "packaged_tobacco",
        // Verarbeitungsstufen / Zwischenprodukte der illegalen Ketten
        "_bud", "coca_paste", "coca_leaf", "poppy_pod", "poppy_seeds",
        "raw_opium", "lysergic_acid", "ergot", "meth_paste", "mdma_crystal",
        "crystal_meth", "lsd_solution", "blotter",
        // Pflanzen/Samen der illegalen Linien (Blöcke als BlockItem)
        "cannabis_seed", "coca_seeds", "poppy_plant", "coca_plant", "cannabis_plant"
    );

    /** Pauschaler Schätzwert pro Item, falls keine Preisquelle greift. */
    private static final double FALLBACK_VALUE = 25.0;

    private ItemLegalityChecker() {
    }

    public static boolean isIllegal(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof PackagedDrugItem) return true;

        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null || !"schedulemc".equals(key.getNamespace())) return false;
        String path = key.getPath();
        for (String fragment : ILLEGAL_FRAGMENTS) {
            if (path.contains(fragment)) return true;
        }
        return false;
    }

    /** Schätzwert eines illegalen Stacks (für die Staatskassen-Gutschrift). */
    public static double estimateValue(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0;
        return FALLBACK_VALUE * stack.getCount();
    }
}
