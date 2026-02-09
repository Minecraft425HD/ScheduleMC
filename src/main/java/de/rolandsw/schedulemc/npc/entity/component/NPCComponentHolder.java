package de.rolandsw.schedulemc.npc.entity.component;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet NPCComponents fuer eine NPC-Entity.
 *
 * Wird in CustomNPCEntity eingebettet und delegiert tick/save/load
 * an alle registrierten Komponenten.
 *
 * Features:
 * - Typsichere Komponentenabfrage via getComponent(Class)
 * - Automatisches Tick-Throttling pro Komponente
 * - NBT Serialisierung aller Komponenten
 * - Dynamisches Hinzufuegen/Entfernen zur Laufzeit
 */
public class NPCComponentHolder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, NPCComponent> components = new LinkedHashMap<>();
    private final Map<Class<? extends NPCComponent>, NPCComponent> byType = new ConcurrentHashMap<>();
    private final Map<String, Integer> tickCounters = new HashMap<>();

    // ═══════════════════════════════════════════════════════════
    // COMPONENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert eine Komponente.
     */
    public <T extends NPCComponent> void addComponent(T component, CustomNPCEntity entity) {
        String id = component.getComponentId();
        NPCComponent old = components.put(id, component);
        byType.put(component.getClass(), component);
        tickCounters.put(id, 0);

        if (old != null) {
            old.onRemoved(entity);
        }
        component.onAdded(entity);

        LOGGER.debug("NPC Component hinzugefuegt: {} fuer Entity {}", id, entity.getId());
    }

    /**
     * Entfernt eine Komponente.
     */
    public void removeComponent(String componentId, CustomNPCEntity entity) {
        NPCComponent removed = components.remove(componentId);
        if (removed != null) {
            byType.remove(removed.getClass());
            tickCounters.remove(componentId);
            removed.onRemoved(entity);
        }
    }

    /**
     * Holt eine Komponente nach Typ.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends NPCComponent> T getComponent(Class<T> type) {
        return (T) byType.get(type);
    }

    /**
     * Holt eine Komponente nach ID.
     */
    @Nullable
    public NPCComponent getComponent(String id) {
        return components.get(id);
    }

    /**
     * Prueft ob eine Komponente vorhanden ist.
     */
    public boolean hasComponent(Class<? extends NPCComponent> type) {
        return byType.containsKey(type);
    }

    /**
     * Gibt alle Komponenten zurueck.
     */
    public Collection<NPCComponent> getAllComponents() {
        return components.values();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK
    // ═══════════════════════════════════════════════════════════

    /**
     * Tickt alle Komponenten (mit individuellem Throttling).
     */
    public void tickAll(CustomNPCEntity entity) {
        for (Map.Entry<String, NPCComponent> entry : components.entrySet()) {
            String id = entry.getKey();
            NPCComponent component = entry.getValue();

            int counter = tickCounters.getOrDefault(id, 0) + 1;
            if (counter >= component.getUpdateInterval()) {
                try {
                    component.tick(entity);
                } catch (Exception e) {
                    LOGGER.error("Fehler in NPC Component '{}': {}", id, e.getMessage());
                }
                counter = 0;
            }
            tickCounters.put(id, counter);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    /**
     * Speichert alle Komponenten in einen NBT-Tag.
     */
    public CompoundTag saveAll() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, NPCComponent> entry : components.entrySet()) {
            try {
                CompoundTag componentTag = entry.getValue().save();
                if (!componentTag.isEmpty()) {
                    tag.put(entry.getKey(), componentTag);
                }
            } catch (Exception e) {
                LOGGER.error("Fehler beim Speichern von Component '{}': {}", entry.getKey(), e.getMessage());
            }
        }
        return tag;
    }

    /**
     * Laedt alle Komponenten aus einem NBT-Tag.
     */
    public void loadAll(CompoundTag tag) {
        for (Map.Entry<String, NPCComponent> entry : components.entrySet()) {
            String id = entry.getKey();
            if (tag.contains(id)) {
                try {
                    entry.getValue().load(tag.getCompound(id));
                } catch (Exception e) {
                    LOGGER.error("Fehler beim Laden von Component '{}': {}", id, e.getMessage());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DIAGNOSTICS
    // ═══════════════════════════════════════════════════════════

    public int getComponentCount() {
        return components.size();
    }

    public String getComponentList() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, NPCComponent> entry : components.entrySet()) {
            sb.append(entry.getKey())
              .append(" (interval: ").append(entry.getValue().getUpdateInterval()).append("t)")
              .append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "none";
    }
}
