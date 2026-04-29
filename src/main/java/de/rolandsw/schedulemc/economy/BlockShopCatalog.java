package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton-Katalog aller kaufbaren Produktionsblöcke.
 *
 * Liest die Config-Liste {@code BLOCK_PRICES} (Format: "schedulemc:blockname=Preis:Level")
 * und stellt Lookup für NPC-Shops und den Kauf-Validierungs-Packet bereit.
 */
public class BlockShopCatalog {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ── Singleton ────────────────────────────────────────────────────────────
    private static volatile BlockShopCatalog instance;

    public static BlockShopCatalog getInstance() {
        if (instance == null) {
            synchronized (BlockShopCatalog.class) {
                if (instance == null) instance = new BlockShopCatalog();
            }
        }
        return instance;
    }

    // ── Katalog ───────────────────────────────────────────────────────────────
    /** registryName → entry */
    private final ConcurrentHashMap<String, BlockCatalogEntry> catalog = new ConcurrentHashMap<>();

    private BlockShopCatalog() {}

    // ── Config laden ──────────────────────────────────────────────────────────

    /**
     * Liest {@code ModConfigHandler.COMMON.BLOCK_PRICES} und füllt den Katalog.
     * Darf mehrfach aufgerufen werden (z.B. nach Config-Reload).
     */
    public void applyConfig() {
        catalog.clear();
        int count = 0;
        try {
            for (String entry : ModConfigHandler.COMMON.BLOCK_PRICES.get()) {
                // Format:  schedulemc:blockname=Preis:Level
                String[] eqSplit = entry.split("=", 2);
                if (eqSplit.length != 2) continue;

                String blockId = eqSplit[0].trim();
                String[] valSplit = eqSplit[1].split(":", 2);
                if (valSplit.length != 2) continue;

                double price;
                int level;
                try {
                    price = Double.parseDouble(valSplit[0].trim());
                    level = Integer.parseInt(valSplit[1].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                // Namensschlüssel: "block.schedulemc.<name>"  (letzter Pfad-Teil)
                String localName = blockId.contains(":") ? blockId.substring(blockId.indexOf(':') + 1) : blockId;
                String nameKey = "block.schedulemc." + localName;

                catalog.put(blockId, new BlockCatalogEntry(blockId, nameKey, price, level));
                count++;
            }
        } catch (Exception e) {
            LOGGER.warn("BlockShopCatalog.applyConfig fehlgeschlagen: {}", e.getMessage());
        }
        LOGGER.info("BlockShopCatalog: {} Einträge geladen", count);
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    /**
     * Gibt den Katalog-Eintrag für einen ItemStack zurück (null wenn unbekannt).
     */
    @Nullable
    public BlockCatalogEntry getEntry(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return null;
        return catalog.get(key.toString());
    }

    /**
     * Gibt alle Einträge (unveränderliche Kopie) zurück.
     */
    public List<BlockCatalogEntry> getAllEntries() {
        return Collections.unmodifiableList(new ArrayList<>(catalog.values()));
    }

    // ── Record ────────────────────────────────────────────────────────────────

    /**
     * Daten-Record eines Produktionsblocks im Shop-Katalog.
     */
    public record BlockCatalogEntry(
        String blockId,        // "schedulemc:trim_station"
        String nameKey,        // "block.schedulemc.trim_station"
        double price,          // Kaufpreis in €
        int    requiredLevel   // Mindest-ProducerLevel (0 = kein Limit)
    ) {}
}
