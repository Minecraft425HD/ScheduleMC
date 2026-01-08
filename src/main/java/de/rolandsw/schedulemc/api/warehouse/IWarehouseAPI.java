package de.rolandsw.schedulemc.api.warehouse;

import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
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
    boolean hasWarehouse(@Nonnull BlockPos position);

    /**
     * Fügt Items zu einem Warehouse hinzu.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @param amount Die Menge
     * @return true wenn erfolgreich hinzugefügt
     * @throws IllegalArgumentException wenn Parameter ungültig
     */
    boolean addItemToWarehouse(@Nonnull BlockPos position, @Nonnull Item item, int amount);

    /**
     * Entfernt Items aus einem Warehouse.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @param amount Die Menge
     * @return true wenn erfolgreich entfernt
     * @throws IllegalArgumentException wenn Parameter ungültig
     */
    boolean removeItemFromWarehouse(@Nonnull BlockPos position, @Nonnull Item item, int amount);

    /**
     * Gibt den Bestand eines Items zurück.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @return Aktuelle Menge
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    int getItemStock(@Nonnull BlockPos position, @Nonnull Item item);

    /**
     * Gibt die maximale Kapazität eines Items zurück.
     *
     * @param position Die Warehouse-Position
     * @param item Das Item
     * @return Maximale Kapazität
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    int getItemCapacity(@Nonnull BlockPos position, @Nonnull Item item);

    /**
     * Gibt alle Slots eines Warehouses zurück.
     *
     * @param position Die Warehouse-Position
     * @return Liste aller Slots
     * @throws IllegalArgumentException wenn position null ist
     */
    @Nonnull
    List<WarehouseSlot> getAllSlots(@Nonnull BlockPos position);

    /**
     * Fügt einen Verkäufer zum Warehouse hinzu.
     *
     * @param position Die Warehouse-Position
     * @param sellerUUID Die UUID des Verkäufers
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void addSeller(@Nonnull BlockPos position, @Nonnull UUID sellerUUID);

    /**
     * Entfernt einen Verkäufer vom Warehouse.
     *
     * @param position Die Warehouse-Position
     * @param sellerUUID Die UUID des Verkäufers
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void removeSeller(@Nonnull BlockPos position, @Nonnull UUID sellerUUID);

    /**
     * Prüft ob ein Spieler Verkäufer ist.
     *
     * @param position Die Warehouse-Position
     * @param sellerUUID Die UUID des Spielers
     * @return true wenn Verkäufer
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    boolean isSeller(@Nonnull BlockPos position, @Nonnull UUID sellerUUID);
}
