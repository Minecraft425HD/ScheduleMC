package de.rolandsw.schedulemc.npc.crime;

import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Speichert, wer welchen Spieler angegriffen/getötet hat, damit das Opfer
 * den Täter später bei einem beliebigen Polizisten anzeigen kann.
 *
 * Einträge verfallen nach {@link #TTL_DAYS} Spieltagen.
 */
public class AttackerRecordManager extends AbstractPersistenceManager<AttackerRecordManager.Data> {

    private static volatile AttackerRecordManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    /** Anzeige-Frist in Spieltagen. */
    public static final long TTL_DAYS = 1;
    private static final int MAX_RECORDS_PER_VICTIM = 20;

    /** victimUUID -> Angriffe gegen diesen Spieler. */
    private final Map<UUID, List<AttackRecord>> recordsByVictim = new ConcurrentHashMap<>();

    @Nullable
    public static AttackerRecordManager getInstance() {
        return instance;
    }

    public static AttackerRecordManager initialize(MinecraftServer server) {
        AttackerRecordManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new AttackerRecordManager(server);
                }
            }
        }
        return result;
    }

    private AttackerRecordManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("attacker_records.json").toFile(),
            GsonHelper.get()
        );
        load();
    }

    /**
     * Hält einen Angriff fest. Mehrfache Angriffe desselben Täters werden
     * zu einem (noch nicht gemeldeten) Eintrag zusammengefasst und zum
     * jeweils schwersten Verbrechen eskaliert.
     */
    public void recordAttack(ServerPlayer victim, ServerPlayer attacker, CrimeType type,
                             boolean killed, BlockPos location, long currentDay) {
        List<AttackRecord> list = recordsByVictim.computeIfAbsent(victim.getUUID(), k -> new ArrayList<>());

        for (AttackRecord r : list) {
            if (!r.reported && r.attackerUUID.equals(attacker.getUUID())) {
                if (type.getSeverity() > r.crimeTypeOrAssault().getSeverity()) {
                    r.crimeType = type.name();
                }
                r.hitCount++;
                r.killed = r.killed || killed;
                r.gameDay = currentDay;
                r.x = location.getX();
                r.y = location.getY();
                r.z = location.getZ();
                markDirty();
                return;
            }
        }

        AttackRecord rec = new AttackRecord();
        rec.attackerUUID = attacker.getUUID();
        rec.attackerName = attacker.getName().getString();
        rec.crimeType = type.name();
        rec.hitCount = 1;
        rec.killed = killed;
        rec.gameDay = currentDay;
        rec.x = location.getX();
        rec.y = location.getY();
        rec.z = location.getZ();
        list.add(rec);

        while (list.size() > MAX_RECORDS_PER_VICTIM) {
            list.remove(0);
        }
        markDirty();
    }

    /** Noch meldbare (nicht gemeldete, nicht abgelaufene) Angriffe gegen den Spieler. */
    public List<AttackRecord> getReportableAttacks(UUID victim, long currentDay) {
        List<AttackRecord> list = recordsByVictim.get(victim);
        if (list == null) return List.of();
        List<AttackRecord> result = new ArrayList<>();
        for (AttackRecord r : list) {
            if (!r.reported && currentDay <= r.gameDay + TTL_DAYS) {
                result.add(r);
            }
        }
        return result;
    }

    /** Markiert alle Angriffe eines bestimmten Täters gegen das Opfer als gemeldet. */
    public void markReported(UUID victim, UUID attacker) {
        List<AttackRecord> list = recordsByVictim.get(victim);
        if (list == null) return;
        boolean changed = false;
        for (AttackRecord r : list) {
            if (!r.reported && r.attackerUUID.equals(attacker)) {
                r.reported = true;
                changed = true;
            }
        }
        if (changed) markDirty();
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return Data.class;
    }

    @Override
    protected Data getCurrentData() {
        Data data = new Data();
        data.recordsByVictim = new HashMap<>();
        recordsByVictim.forEach((k, v) -> data.recordsByVictim.put(k.toString(), new ArrayList<>(v)));
        return data;
    }

    @Override
    protected void onDataLoaded(Data data) {
        recordsByVictim.clear();
        if (data != null && data.recordsByVictim != null) {
            data.recordsByVictim.forEach((k, v) -> {
                try {
                    recordsByVictim.put(UUID.fromString(k), new ArrayList<>(v));
                } catch (IllegalArgumentException ignored) {
                    // korrupten Key überspringen
                }
            });
        }
    }

    @Override
    protected String getComponentName() {
        return "AttackerRecordManager";
    }

    @Override
    protected String getHealthDetails() {
        int total = recordsByVictim.values().stream().mapToInt(List::size).sum();
        return recordsByVictim.size() + " victims, " + total + " records";
    }

    @Override
    protected void onCriticalLoadFailure() {
        recordsByVictim.clear();
    }

    public static class Data {
        public Map<String, List<AttackRecord>> recordsByVictim;
    }

    /** Ein protokollierter Angriff. Gson-freundliches POJO. */
    public static class AttackRecord {
        public UUID attackerUUID;
        public String attackerName;
        public String crimeType;
        public int hitCount;
        public boolean killed;
        public long gameDay;
        public int x;
        public int y;
        public int z;
        public boolean reported;

        public CrimeType crimeTypeOrAssault() {
            try {
                return CrimeType.valueOf(crimeType);
            } catch (Exception e) {
                return CrimeType.ASSAULT;
            }
        }

        public BlockPos location() {
            return new BlockPos(x, y, z);
        }
    }
}
