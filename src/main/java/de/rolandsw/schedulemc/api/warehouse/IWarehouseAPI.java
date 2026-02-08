package de.rolandsw.schedulemc.api.warehouse;

import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Public Warehouse API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Warehouse-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Warehouse-Inventory-Verwaltung</li>
 *   <li>Item-Slots und Kapazitäten</li>
 *   <li>Verkäufer-Verwaltung</li>
 *   <li>Lieferungen und Kosten</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch synchronized Operations.
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IWarehouseAPI warehouseAPI = ScheduleMCAPI.getWarehouseAPI();
 *
 * // Warehouse an Position finden
 * boolean exists = warehouseAPI.hasWarehouse(pos);
 *
 * // Item hinzufügen
 * warehouseAPI.addItemToWarehouse(pos, item, amount);
 *
 * // Bestand abfragen
 * int stock = warehouseAPI.getItemStock(pos, item);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IWarehouseAPI {

    /**
     * Prüft ob an der Position ein Warehouse existiert.
     *
     * @param position Die Position
     * @return true wenn Warehouse existiert
     * @throws IllegalArgumentException wenn position null ist
     */
    boolean hasWarehouse(BlockPos position);

    /**
     * Fügt Items zu einem Warehouse hinzu.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @param amount Die Menge
     * @return true wenn erfolgreich hinzugefügt
     * @throws IllegalArgumentException wenn Parameter ungültig
     */
    boolean addItemToWarehouse(BlockPos position, Item item, int amount);

    /**
     * Entfernt Items aus einem Warehouse.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @param amount Die Menge
     * @return true wenn erfolgreich entfernt
     * @throws IllegalArgumentException wenn Parameter ungültig
     */
    boolean removeItemFromWarehouse(BlockPos position, Item item, int amount);

    /**
     * Gibt den Bestand eines Items zurück.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @return Aktuelle Menge
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    int getItemStock(BlockPos position, Item item);

    /**
     * Gibt die maximale Kapazität eines Items zurück.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @return Maximale Kapazität
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    int getItemCapacity(BlockPos position, Item item);

    /**
     * Gibt alle Slots eines Warehouses zurück.
     *
     * @param position Die Warehouse-Position
     * @return Liste aller Slots
     * @throws IllegalArgumentException wenn position null ist
     */
    List<WarehouseSlot> getAllSlots(BlockPos position);

    /**
     * Fügt einen Verkäufer zum Warehouse hinzu.
     *
     * @param position Die Warehouse-Position
     * @param sellerUUID Die UUID des Verkäufers
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void addSeller(BlockPos position, UUID sellerUUID);

    /**
     * Entfernt einen Verkäufer vom Warehouse.
     *
     * @param position Die Warehouse-Position
     * @param sellerUUID Die UUID des Verkäufers
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void removeSeller(BlockPos position, UUID sellerUUID);

    /**
     * Prüft ob ein Spieler Verkäufer ist.
     *
     * @param position Die Warehouse-Position
     * @param sellerUUID Die UUID des Spielers
     * @return true wenn Verkäufer
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    boolean isSeller(BlockPos position, UUID sellerUUID);

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns all warehouse positions.
     *
     * @return Set of BlockPos for all warehouses
     * @since 3.2.0
     */
    java.util.Set<BlockPos> getAllWarehousePositions();

    /**
     * Returns the total item count in a warehouse.
     *
     * @param position The warehouse position
     * @return Total items stored
     * @throws IllegalArgumentException if position is null
     * @since 3.2.0
     */
    int getTotalItemCount(BlockPos position);

    /**
     * Returns the percentage of capacity used.
     *
     * @param position The warehouse position
     * @return Usage percentage 0.0 - 100.0
     * @throws IllegalArgumentException if position is null
     * @since 3.2.0
     */
    double getUsagePercentage(BlockPos position);

    /**
     * Returns all sellers for a warehouse.
     *
     * @param position The warehouse position
     * @return Set of seller UUIDs
     * @throws IllegalArgumentException if position is null
     * @since 3.2.0
     */
    java.util.Set<UUID> getAllSellers(BlockPos position);

    /**
     * Links a warehouse to a shop plot.
     *
     * @param position The warehouse position
     * @param shopPlotId The shop plot ID
     * @return true if linked successfully
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    boolean linkToShop(BlockPos position, String shopPlotId);

    /**
     * Triggers an immediate delivery to a warehouse.
     *
     * @param position The warehouse position
     * @return true if delivery triggered
     * @throws IllegalArgumentException if position is null
     * @since 3.2.0
     */
    boolean triggerDelivery(BlockPos position);

    /**
     * Clears all items from a warehouse.
     *
     * @param position The warehouse position
     * @return true if cleared
     * @throws IllegalArgumentException if position is null
     * @since 3.2.0
     */
    boolean clearWarehouse(BlockPos position);
}
