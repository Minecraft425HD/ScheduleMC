package de.rolandsw.schedulemc.npc.events.speedcamera;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

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
 * Verwaltet alle im Overworld platzierten {@link SpeedCameraBlock}-Positionen und
 * rotiert periodisch, welche Teilmenge davon gerade aktiv ist - wie im echten Leben
 * ist nicht jeder markierte Blitzer-Standort dauerhaft besetzt
 * ({@code police.speed_camera_active_count}/{@code speed_camera_rotation_minutes}).
 */
public class SpeedCameraManager extends AbstractPersistenceManager<SpeedCameraManager.SpeedCameraData> {

    private static volatile SpeedCameraManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    /** Cooldown pro Spieler, damit ein Fahrzeug im Radius nicht mehrfach denselben Blitzer auslöst */
    private static final long VIOLATION_COOLDOWN_MS = 30_000L;

    private final Set<Long> allCameras = ConcurrentHashMap.newKeySet();
    private final Set<Long> activeCameras = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> violationCooldowns = new ConcurrentHashMap<>();
    private volatile long lastRotationGameTime = -1L;
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
    // KAMERA-REGISTRIERUNG
    // ═══════════════════════════════════════════════════════════

    public void registerCamera(BlockPos pos) {
        allCameras.add(pos.asLong());
        markDirty();
    }

    public void unregisterCamera(BlockPos pos) {
        long key = pos.asLong();
        allCameras.remove(key);
        activeCameras.remove(key);
        markDirty();
    }

    // ═══════════════════════════════════════════════════════════
    // AKTIV/INAKTIV-ROTATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Server-Tick aufgerufen; rollt intern selbst, wie oft tatsächlich
     * geprüft/rotiert wird (im konfigurierten Minuten-Takt).
     */
    public void tick(ServerLevel overworld) {
        long currentTick = overworld.getGameTime();

        // Erste Prüfung nach dem Serverstart sofort ausführen, danach im konfigurierten Takt
        long rotationIntervalTicks = ModConfigHandler.COMMON.POLICE_SPEED_CAMERA_ROTATION_MINUTES.get() * 60L * 20L;
        if (lastRotationGameTime >= 0 && currentTick - lastRotationGameTime < rotationIntervalTicks) {
            return;
        }
        lastRotationGameTime = currentTick;

        rotateActiveCameras(overworld);
        violationCooldowns.entrySet().removeIf(
            entry -> System.currentTimeMillis() - entry.getValue() > VIOLATION_COOLDOWN_MS * 2
        );
    }

    private void rotateActiveCameras(ServerLevel overworld) {
        int activeCount = ModConfigHandler.COMMON.POLICE_SPEED_CAMERA_ACTIVE_COUNT.get();

        List<Long> candidates = new ArrayList<>(allCameras);
        Collections.shuffle(candidates);

        Set<Long> newActive = new HashSet<>(candidates.subList(0, Math.min(activeCount, candidates.size())));

        for (Long key : allCameras) {
            boolean shouldBeActive = newActive.contains(key);
            BlockPos pos = BlockPos.of(key);
            if (overworld.getBlockEntity(pos) instanceof SpeedCameraBlockEntity camera) {
                camera.setActive(shouldBeActive);
            }
        }

        activeCameras.clear();
        activeCameras.addAll(newActive);
        markDirty();
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
        public Set<Long> allCameras = new HashSet<>();
        public Set<Long> activeCameras = new HashSet<>();
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<SpeedCameraData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(SpeedCameraData data) {
        allCameras.clear();
        activeCameras.clear();
        if (data == null) {
            LOGGER.warn("Null data loaded for SpeedCameraManager");
            return;
        }
        if (data.allCameras != null) {
            allCameras.addAll(data.allCameras);
        }
        if (data.activeCameras != null) {
            activeCameras.addAll(data.activeCameras);
        }
    }

    @Override
    protected SpeedCameraData getCurrentData() {
        SpeedCameraData data = new SpeedCameraData();
        data.allCameras = new HashSet<>(allCameras);
        data.activeCameras = new HashSet<>(activeCameras);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "SpeedCameraManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("Cameras: %d total, %d active", allCameras.size(), activeCameras.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        allCameras.clear();
        activeCameras.clear();
    }
}
