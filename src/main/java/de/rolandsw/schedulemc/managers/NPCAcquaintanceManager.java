package de.rolandsw.schedulemc.managers;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.network.KnownNPCAddedPacket;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet, welche NPCs ein Spieler bereits "kennt" (erstes Gespräch,
 * Handel oder Quest-Annahme). Unbekannte NPCs werden client-seitig ohne
 * Namen und Persönlichkeit gerendert — pro Spieler individuell.
 */
public class NPCAcquaintanceManager extends AbstractPersistenceManager<NPCAcquaintanceManager.Data> {

    private static volatile NPCAcquaintanceManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    /** PlayerId → Set von NPC-Data-UUIDs (stabil über Respawns). */
    private final Map<UUID, Set<UUID>> knownNpcsByPlayer = new ConcurrentHashMap<>();

    @Nullable
    public static NPCAcquaintanceManager getInstance() {
        return instance;
    }

    public static NPCAcquaintanceManager initialize(MinecraftServer server) {
        NPCAcquaintanceManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new NPCAcquaintanceManager(server);
                }
            }
        }
        return result;
    }

    private NPCAcquaintanceManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_acquaintances.json").toFile(),
            GsonHelper.get()
        );
        load();
    }

    public boolean knowsNPC(UUID playerId, UUID npcDataId) {
        Set<UUID> known = knownNpcsByPlayer.get(playerId);
        return known != null && known.contains(npcDataId);
    }

    public Set<UUID> getKnownNPCs(UUID playerId) {
        return Collections.unmodifiableSet(
            knownNpcsByPlayer.getOrDefault(playerId, Collections.emptySet()));
    }

    /**
     * Markiert einen NPC als bekannt (idempotent) und informiert den Client
     * sofort per Delta-Packet, damit Name + Persönlichkeit erscheinen.
     */
    public void markKnown(ServerPlayer player, CustomNPCEntity npc) {
        if (npc.getNpcData() == null) return;
        UUID npcDataId = npc.getNpcData().getNpcUUID();
        Set<UUID> known = knownNpcsByPlayer.computeIfAbsent(
            player.getUUID(), k -> ConcurrentHashMap.newKeySet());
        if (known.add(npcDataId)) {
            markDirty();
            NPCNetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new KnownNPCAddedPacket(npcDataId));
        }
    }

    // ═══════════════════════ Persistence ═══════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Data>() {}.getType();
    }

    @Override
    protected void onDataLoaded(Data data) {
        knownNpcsByPlayer.clear();
        if (data != null && data.acquaintances != null) {
            for (Map.Entry<String, List<String>> e : data.acquaintances.entrySet()) {
                Set<UUID> set = ConcurrentHashMap.newKeySet();
                for (String npcId : e.getValue()) {
                    set.add(UUID.fromString(npcId));
                }
                knownNpcsByPlayer.put(UUID.fromString(e.getKey()), set);
            }
        }
    }

    @Override
    protected Data getCurrentData() {
        Data data = new Data();
        data.acquaintances = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> e : knownNpcsByPlayer.entrySet()) {
            List<String> ids = new ArrayList<>();
            for (UUID id : e.getValue()) ids.add(id.toString());
            data.acquaintances.put(e.getKey().toString(), ids);
        }
        return data;
    }

    @Override
    protected String getComponentName() {
        return "NPCAcquaintanceManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d players, %d acquaintances", knownNpcsByPlayer.size(),
            knownNpcsByPlayer.values().stream().mapToInt(Set::size).sum());
    }

    @Override
    protected void onCriticalLoadFailure() {
        knownNpcsByPlayer.clear();
    }

    public static class Data {
        public Map<String, List<String>> acquaintances;
    }
}
