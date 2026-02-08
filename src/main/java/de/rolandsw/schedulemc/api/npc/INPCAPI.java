package de.rolandsw.schedulemc.api.npc;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

/**
 * Public NPC API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das NPC-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>NPC-Erstellung und -Verwaltung</li>
 *   <li>NPC-Daten und Konfiguration</li>
 *   <li>Shop- und Bank-NPCs</li>
 *   <li>Zeitplan und Behavior-Steuerung</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap-basierte Registry.
 *
 * <h2>Performance:</h2>
 * NPC-Lookups nutzen O(1) UUID-Index für schnelle Suche.
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * INPCAPI npcAPI = ScheduleMCAPI.getNPCAPI();
 *
 * // NPC finden
 * CustomNPCEntity npc = npcAPI.getNPCByUUID(uuid, level);
 *
 * // Alle NPCs in einer Welt
 * Collection<CustomNPCEntity> npcs = npcAPI.getAllNPCs(level);
 *
 * // NPC-Daten abrufen
 * NPCData data = npc.getNpcData();
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface INPCAPI {

    /**
     * Findet einen NPC anhand seiner UUID in einem ServerLevel.
     * <p>
     * Performance: O(1) durch HashMap-Lookup
     *
     * @param npcUUID Die UUID des NPCs
     * @param level Das ServerLevel
     * @return Der gefundene NPC oder null
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    @Nullable
    CustomNPCEntity getNPCByUUID(UUID npcUUID, ServerLevel level);

    /**
     * Findet einen NPC anhand seiner UUID in allen geladenen Welten.
     * <p>
     * Performance: O(w) wobei w = Anzahl Welten (typisch 3)
     *
     * @param npcUUID Die UUID des NPCs
     * @return Der gefundene NPC oder null
     * @throws IllegalArgumentException wenn npcUUID null ist
     */
    @Nullable
    CustomNPCEntity getNPCByUUID(UUID npcUUID);

    /**
     * Gibt alle NPCs in einem ServerLevel zurück.
     *
     * @param level Das ServerLevel
     * @return Unmodifiable Collection aller NPCs
     * @throws IllegalArgumentException wenn level null ist
     */
    Collection<CustomNPCEntity> getAllNPCs(ServerLevel level);

    /**
     * Gibt alle NPCs in allen Welten zurück.
     *
     * @return Unmodifiable Collection aller NPCs
     */
    Collection<CustomNPCEntity> getAllNPCs();

    /**
     * Gibt die Anzahl NPCs in einem ServerLevel zurück.
     *
     * @param level Das ServerLevel
     * @return Anzahl NPCs
     * @throws IllegalArgumentException wenn level null ist
     */
    int getNPCCount(ServerLevel level);

    /**
     * Gibt die Gesamtanzahl aller NPCs zurück.
     *
     * @return Gesamtanzahl NPCs
     */
    int getTotalNPCCount();

    /**
     * Gibt die NPC-Daten eines NPCs zurück.
     *
     * @param npc Der NPC
     * @return NPCData-Objekt mit allen Daten
     * @throws IllegalArgumentException wenn npc null ist
     */
    NPCData getNPCData(CustomNPCEntity npc);

    /**
     * Setzt die Home-Position eines NPCs.
     *
     * @param npc Der NPC
     * @param homePos Die neue Home-Position
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void setNPCHome(CustomNPCEntity npc, BlockPos homePos);

    /**
     * Setzt die Work-Position eines NPCs.
     *
     * @param npc Der NPC
     * @param workPos Die neue Work-Position
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void setNPCWork(CustomNPCEntity npc, BlockPos workPos);

    /**
     * Setzt den NPC-Typ.
     *
     * @param npc Der NPC
     * @param type Der neue Typ
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    void setNPCType(CustomNPCEntity npc, NPCType type);

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns all NPCs of a specific type.
     *
     * @param type The NPC type to filter by
     * @return Collection of NPCs matching the type
     * @throws IllegalArgumentException if type is null
     * @since 3.2.0
     */
    Collection<CustomNPCEntity> getNPCsByType(NPCType type);

    /**
     * Returns all NPCs within a radius of a position.
     *
     * @param level The ServerLevel
     * @param center Center position
     * @param radius Search radius in blocks
     * @return Collection of NPCs within range
     * @throws IllegalArgumentException if parameters are null or radius negative
     * @since 3.2.0
     */
    Collection<CustomNPCEntity> getNPCsInRadius(ServerLevel level, BlockPos center, double radius);

    /**
     * Sets the NPC's display name.
     *
     * @param npc The NPC
     * @param name The new name
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    void setNPCName(CustomNPCEntity npc, String name);

    /**
     * Sets a leisure location for an NPC.
     *
     * @param npc The NPC
     * @param leisurePos The leisure location
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    void addNPCLeisureLocation(CustomNPCEntity npc, BlockPos leisurePos);

    /**
     * Removes an NPC from the world.
     *
     * @param npc The NPC to remove
     * @throws IllegalArgumentException if npc is null
     * @since 3.2.0
     */
    void removeNPC(CustomNPCEntity npc);

    /**
     * Sets the NPC's schedule for a specific activity.
     *
     * @param npc The NPC
     * @param activity Activity name (e.g. "workstart", "workend", "sleep")
     * @param time Time in HHMM format (e.g. 700 for 07:00)
     * @throws IllegalArgumentException if parameters are null
     * @since 3.2.0
     */
    void setNPCSchedule(CustomNPCEntity npc, String activity, int time);

    /**
     * Returns the NPC's current balance/wallet amount.
     *
     * @param npc The NPC
     * @return Balance in Euro
     * @throws IllegalArgumentException if npc is null
     * @since 3.2.0
     */
    double getNPCBalance(CustomNPCEntity npc);

    /**
     * Sets the NPC's wallet balance.
     *
     * @param npc The NPC
     * @param amount New balance
     * @throws IllegalArgumentException if npc is null
     * @since 3.2.0
     */
    void setNPCBalance(CustomNPCEntity npc, double amount);
}
