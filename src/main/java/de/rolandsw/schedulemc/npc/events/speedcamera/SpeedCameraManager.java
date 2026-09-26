package de.rolandsw.schedulemc.npc.events.speedcamera;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet alle admin-markierten Blitzer-Standorte und rotiert alle 7 Minecraft-Tage
 * (fester Takt, siehe Begründung unten), welche Teilmenge davon gerade tatsächlich einen
 * physischen {@link SpeedCameraBlock} trägt - wie im echten Leben taucht der Blitzer nur
 * temporär an einem von mehreren möglichen Standorten auf, statt dauerhaft überall zu
 * stehen. Markierung erfolgt über {@link SpeedCameraMarkerItem}, nicht durch manuelles
 * Block-Platzieren.
 */
public class SpeedCameraManager extends AbstractPersistenceManager<SpeedCameraManager.SpeedCameraData> {

    private static volatile SpeedCameraManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    /** Cooldown pro Spieler, damit ein Fahrzeug im Radius nicht mehrfach denselben Blitzer auslöst */
    private static final long VIOLATION_COOLDOWN_MS = 30_000L;

    /**
     * Rotations-Takt in Minecraft-Tagen. Bewusst fest verdrahtet statt als Config-Wert,
     * analog zur Preisglättung (Teil 3): der Nutzer hat "alle 7 Tage" konkret benannt.
     */
    private static final long ROTATION_INTERVAL_DAYS = 7L;

    private final Set<Long> markedPositions = ConcurrentHashMap.newKeySet();
    private final Set<Long> activeCameraPositions = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> violationCooldowns = new ConcurrentHashMap<>();
    private volatile long lastRotationDay = -1L;
    private volatile MinecraftServer server;

    private SpeedCameraManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("schedulemc_speed_cameras.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    public static SpeedCameraManager initialize(MinecraftServer server) {
        SpeedCameraManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new SpeedCameraManager(server);
                }
            }
        }
        result.server = server;
        return result;
    }

    @Nullable
    public static SpeedCameraManager getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // MARKIERUNG (über SpeedCameraMarkerItem, kein physischer Block)
    // ═══════════════════════════════════════════════════════════

    public boolean isMarked(BlockPos pos) {
        return markedPositions.contains(pos.asLong());
    }

    public void registerMarker(BlockPos pos) {
        markedPositions.add(pos.asLong());
        markDirty();
    }

    /**
     * Entfernt eine Markierung und räumt einen dort ggf. aktiven Blitzer sofort ab
     * (nur wenn an dieser Stelle tatsächlich noch ein {@link SpeedCameraBlock} steht -
     * ein Spieler könnte den Platz inzwischen anders bebaut haben).
     */
    public void unregisterMarker(ServerLevel overworld, BlockPos pos) {
        long key = pos.asLong();
        markedPositions.remove(key);
        if (activeCameraPositions.remove(key)) {
            removeCameraIfPresent(overworld, pos);
        }
        markDirty();
    }

    public int getMarkedCount() {
        return markedPositions.size();
    }

    /**
     * @return verbleibende Minecraft-Tage bis zur nächsten Rotation (für die
     *         Rechtsklick-Statusanzeige an einem aktiven {@link SpeedCameraBlock})
     */
    public long getDaysUntilNextRotation(ServerLevel overworld) {
        if (lastRotationDay < 0) return 0L;
        long currentDay = overworld.getDayTime() / 24000L;
        long elapsed = currentDay - lastRotationDay;
        return Math.max(0L, ROTATION_INTERVAL_DAYS - elapsed);
    }

    // ═══════════════════════════════════════════════════════════
    // AKTIV-ROTATION (alle 7 Minecraft-Tage)
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Server-Tick aufgerufen; rotiert intern nur alle {@link #ROTATION_INTERVAL_DAYS}
     * Minecraft-Tage tatsächlich (Erstlauf nach Serverstart sofort, damit markierte Punkte
     * nicht erst 7 Tage auf ihren ersten Blitzer warten).
     */
    public void tick(ServerLevel overworld) {
        long currentDay = overworld.getDayTime() / 24000L;

        if (lastRotationDay >= 0 && currentDay - lastRotationDay < ROTATION_INTERVAL_DAYS) {
            violationCooldowns.entrySet().removeIf(
                entry -> System.currentTimeMillis() - entry.getValue() > VIOLATION_COOLDOWN_MS * 2
            );
            return;
        }
        lastRotationDay = currentDay;

        rotateActiveCameras(overworld);
    }

    private void rotateActiveCameras(ServerLevel overworld) {
        int activeCount = ModConfigHandler.COMMON.POLICE_SPEED_CAMERA_ACTIVE_COUNT.get();

        List<Long> candidates = new ArrayList<>(markedPositions);
        Collections.shuffle(candidates);
        Set<Long> newActive = new HashSet<>(candidates.subList(0, Math.min(activeCount, candidates.size())));

        // Alte aktive Standorte, die nicht erneut gezogen wurden, wieder abräumen
        for (Long key : activeCameraPositions) {
            if (!newActive.contains(key)) {
                removeCameraIfPresent(overworld, BlockPos.of(key));
            }
        }

        // Neu gezogene Standorte physisch bebauen
        for (Long key : newActive) {
            if (!activeCameraPositions.contains(key)) {
                placeCameraIfEmpty(overworld, BlockPos.of(key));
            }
        }

        activeCameraPositions.clear();
        activeCameraPositions.addAll(newActive);
        markDirty();
    }

    private void placeCameraIfEmpty(ServerLevel overworld, BlockPos pos) {
        BlockState current = overworld.getBlockState(pos);
        if (!current.isAir() && !current.canBeReplaced()) {
            // Markierter Punkt ist inzwischen zugebaut - überspringen statt etwas zu zerstören
            return;
        }
        overworld.setBlock(pos, SpeedCameraRegistry.SPEED_CAMERA_BLOCK.get().defaultBlockState(), 3);
    }

    private void removeCameraIfPresent(ServerLevel overworld, BlockPos pos) {
        if (overworld.getBlockState(pos).getBlock() == SpeedCameraRegistry.SPEED_CAMERA_BLOCK.get()) {
            overworld.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VERSTOSS-COOLDOWN
    // ═══════════════════════════════════════════════════════════

    /**
     * @return true wenn für diesen Spieler jetzt ein Verstoß gemeldet werden darf
     *         (und merkt sich das sofort selbst, um Doppel-Meldungen zu verhindern)
     */
    public boolean tryRecordViolation(UUID playerUUID) {
        Long last = violationCooldowns.get(playerUUID);
        if (last != null && System.currentTimeMillis() - last < VIOLATION_COOLDOWN_MS) {
            return false;
        }
        violationCooldowns.put(playerUUID, System.currentTimeMillis());
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    public static class SpeedCameraData {
        public Set<Long> markedPositions = new HashSet<>();
        public Set<Long> activeCameraPositions = new HashSet<>();
        public long lastRotationDay = -1L;
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<SpeedCameraData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(SpeedCameraData data) {
        markedPositions.clear();
        activeCameraPositions.clear();
        if (data == null) {
            LOGGER.warn("Null data loaded for SpeedCameraManager");
            return;
        }
        if (data.markedPositions != null) {
            markedPositions.addAll(data.markedPositions);
        }
        if (data.activeCameraPositions != null) {
            activeCameraPositions.addAll(data.activeCameraPositions);
        }
        lastRotationDay = data.lastRotationDay;
    }

    @Override
    protected SpeedCameraData getCurrentData() {
        SpeedCameraData data = new SpeedCameraData();
        data.markedPositions = new HashSet<>(markedPositions);
        data.activeCameraPositions = new HashSet<>(activeCameraPositions);
        data.lastRotationDay = lastRotationDay;
        return data;
    }

    @Override
    protected String getComponentName() {
        return "SpeedCameraManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("Marked: %d, active: %d", markedPositions.size(), activeCameraPositions.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        markedPositions.clear();
        activeCameraPositions.clear();
    }
}
