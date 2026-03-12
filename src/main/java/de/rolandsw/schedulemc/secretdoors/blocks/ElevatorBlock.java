package de.rolandsw.schedulemc.secretdoors.blocks;

import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.ElevatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Aufzug-Block: Solide Plattform, tarnbar, Redstone-steuerbar.
 * Breitet sich beim Platzieren per BFS-Floodfill auf alle angrenzenden
 * Luftblöcke auf der gleichen Y-Ebene aus (max. 20×20).
 * Rechtsklick teleportiert den Spieler zur nächsten verknüpften Station oben.
 */
public class ElevatorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final int MAX_SPREAD = 20;

    public ElevatorBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false));
    }

    // ─────────────────────────────────────────────────────────────────
    // Block State
    // ─────────────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
            .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
            .setValue(POWERED, false);
    }

    // ─────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    // ─────────────────────────────────────────────────────────────────
    // Platzierung: BFS-Floodfill
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                             @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;

        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof ElevatorBlockEntity be) {
            be.setOwner(player);
            detectAndSpawnFillers(level, pos, be);
        }
    }

    /**
     * BFS-Floodfill: Füllt alle zusammenhängenden Luftblöcke
     * auf der gleichen Y-Ebene mit Filler-Blöcken (max. 20 Blocks Abstand in X/Z).
     */
    public void detectAndSpawnFillers(Level level, BlockPos controllerPos, ElevatorBlockEntity be) {
        if (level.isClientSide) return;

        // Alte Filler entfernen
        for (int[] offset : be.getFillerOffsets()) {
            BlockPos fp = controllerPos.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(fp).is(SecretDoors.DOOR_FILLER.get())) {
                level.setBlockAndUpdate(fp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
        be.clearFillerOffsets();

        int y = controllerPos.getY();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        // Starte BFS von den 4 direkten Nachbarn
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = controllerPos.relative(dir);
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            int dx = current.getX() - controllerPos.getX();
            int dz = current.getZ() - controllerPos.getZ();

            // Bounding-Box-Check: max. 20 Blöcke in jede Richtung
            if (Math.abs(dx) > MAX_SPREAD || Math.abs(dz) > MAX_SPREAD) continue;
            // Gleiche Y-Ebene
            if (current.getY() != y) continue;
            // Nur Luft ersetzen
            if (!level.getBlockState(current).isAir()) continue;

            // Filler platzieren
            level.setBlockAndUpdate(current, SecretDoors.DOOR_FILLER.get().defaultBlockState());
            if (level.getBlockEntity(current) instanceof DoorFillerBlockEntity fbe) {
                fbe.setControllerPos(controllerPos);
                fbe.setChanged();
            }
            be.addFillerOffset(dx, 0, dz);

            // Nachbarn in Queue
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(dir);
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        be.setChanged();
        level.sendBlockUpdated(controllerPos, level.getBlockState(controllerPos),
            level.getBlockState(controllerPos), 3);
    }

    // ─────────────────────────────────────────────────────────────────
    // Abbau: Filler entfernen
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof ElevatorBlockEntity be) {
                removeAllFillers(level, pos, be);
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    public static void removeAllFillers(Level level, BlockPos controllerPos, ElevatorBlockEntity be) {
        for (int[] offset : be.getFillerOffsets()) {
            BlockPos fp = controllerPos.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(fp).is(SecretDoors.DOOR_FILLER.get())) {
                level.setBlockAndUpdate(fp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
        be.clearFillerOffsets();
    }

    // ─────────────────────────────────────────────────────────────────
    // Interaktion (Rechtsklick)
    // ─────────────────────────────────────────────────────────────────

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ElevatorBlockEntity be)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        // Shift+Rechtsklick mit Block → Tarnung entfernen (wie bei Türen)
        if (player.isShiftKeyDown() && held.getItem() instanceof BlockItem) {
            be.clearCamoBlock();
            sendCamoUpdateToAll(level, pos, state, be);
            player.sendSystemMessage(Component.literal("§7[Aufzug] Tarnung entfernt."));
            return InteractionResult.SUCCESS;
        }

        // Shift+Rechtsklick mit leerem Hand → Verknüpfungs-Modus oder Camo-Clear
        if (player.isShiftKeyDown() && held.isEmpty()) {
            // Suche nach einem ANDEREN Aufzug im Linking-Modus (max. 16 H / 128 V Blöcke)
            BlockPos linkingSource = findLinkingElevator(level, pos, player);
            if (linkingSource != null) {
                return handleLinking(level, linkingSource, pos, state, player);
            }

            // Kein anderer im Linking-Modus → diesen Aufzug umschalten
            boolean newMode = !be.isLinkingMode();
            be.setLinkingMode(newMode);
            if (newMode) {
                player.sendSystemMessage(Component.literal(
                    "§a[Aufzug] Verknüpfungs-Modus AN §7– Shift+Klick auf eine weitere Station."));
            } else {
                player.sendSystemMessage(Component.literal("§7[Aufzug] Verknüpfungs-Modus AUS."));
            }
            return InteractionResult.SUCCESS;
        }

        // Normaler Rechtsklick mit Block → Tarnung setzen (wie bei Türen)
        if (!player.isShiftKeyDown() && held.getItem() instanceof BlockItem blockItem) {
            be.setCamoBlock(blockItem.getBlock());
            sendCamoUpdateToAll(level, pos, state, be);
            player.sendSystemMessage(Component.literal(
                "§a[Aufzug] Tarnung: §e" + held.getHoverName().getString()));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // ─────────────────────────────────────────────────────────────────
    // Redstone
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                 Block block, BlockPos fromPos, boolean moving) {
        if (level.isClientSide) return;
        boolean isPowered = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (isPowered && !wasPowered) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
            // Alle Spieler auf der Plattform teleportieren
            if (level.getBlockEntity(pos) instanceof ElevatorBlockEntity be) {
                if (!be.getLinkedStationsSortedByY().isEmpty()) {
                    AABB platformBox = buildPlatformAABB(pos, be);
                    List<Player> players = level.getEntitiesOfClass(Player.class, platformBox);
                    for (Player p : players) {
                        if (p instanceof ServerPlayer sp) {
                            be.teleportPlayerUp(sp);
                        }
                    }
                }
            }
        } else if (!isPowered && wasPowered) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    /** Berechnet die AABB der gesamten Plattform (Controller + Filler) für Spieler-Erkennung. */
    private AABB buildPlatformAABB(BlockPos controllerPos, ElevatorBlockEntity be) {
        int minX = controllerPos.getX(), maxX = controllerPos.getX();
        int minZ = controllerPos.getZ(), maxZ = controllerPos.getZ();
        for (int[] o : be.getFillerOffsets()) {
            int x = controllerPos.getX() + o[0];
            int z = controllerPos.getZ() + o[2];
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }
        return new AABB(minX, controllerPos.getY() + 1.0, minZ,
                        maxX + 1.0, controllerPos.getY() + 2.5, maxZ + 1.0);
    }

    // ─────────────────────────────────────────────────────────────────
    // Stations-Verknüpfung
    // ─────────────────────────────────────────────────────────────────

    /**
     * Sucht in 16 Blöcken horizontal und 128 vertikal nach einem anderen Aufzug
     * im Linking-Modus, der dem Spieler gehört. Schließt {@code excludePos} aus.
     */
    @Nullable
    private BlockPos findLinkingElevator(Level level, BlockPos excludePos, Player player) {
        BlockPos origin = player.blockPosition();
        for (int dx = -16; dx <= 16; dx++) {
            for (int dy = -ElevatorBlockEntity.MAX_DISTANCE; dy <= ElevatorBlockEntity.MAX_DISTANCE; dy++) {
                for (int dz = -16; dz <= 16; dz++) {
                    BlockPos check = origin.offset(dx, dy, dz);
                    if (check.equals(excludePos)) continue;
                    if (!(level.getBlockState(check).getBlock() instanceof ElevatorBlock)) continue;
                    if (level.getBlockEntity(check) instanceof ElevatorBlockEntity ebe
                            && ebe.isLinkingMode() && ebe.canUse(player)) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Stellt eine bidirektionale Verknüpfung zwischen {@code sourcePos} (im Linking-Modus)
     * und {@code targetPos} (angeklickter Aufzug) her.
     */
    private InteractionResult handleLinking(Level level, BlockPos sourcePos, BlockPos targetPos,
                                             BlockState targetState, Player player) {
        if (!(level.getBlockEntity(sourcePos) instanceof ElevatorBlockEntity sourceBE)) return InteractionResult.PASS;

        // Stationen müssen exakt über-/untereinander sein (gleicher X/Z)
        if (sourcePos.getX() != targetPos.getX() || sourcePos.getZ() != targetPos.getZ()) {
            player.sendSystemMessage(Component.literal(
                "§c[Aufzug] Stationen müssen exakt über-/untereinander sein (gleicher X/Z)."));
            sourceBE.setLinkingMode(false);
            sourceBE.setChanged();
            return InteractionResult.SUCCESS;
        }

        int yDist = Math.abs(targetPos.getY() - sourcePos.getY());
        if (yDist > ElevatorBlockEntity.MAX_DISTANCE) {
            player.sendSystemMessage(Component.literal(
                "§c[Aufzug] Stationen zu weit auseinander (max. "
                + ElevatorBlockEntity.MAX_DISTANCE + " Blöcke, aktuell: " + yDist + ")."));
            sourceBE.setLinkingMode(false);
            sourceBE.setChanged();
            return InteractionResult.SUCCESS;
        }

        boolean added = sourceBE.addLinkedStation(targetPos);
        sourceBE.setLinkingMode(false);
        sourceBE.setChanged();
        level.sendBlockUpdated(sourcePos, level.getBlockState(sourcePos), level.getBlockState(sourcePos), 3);

        if (level.getBlockEntity(targetPos) instanceof ElevatorBlockEntity targetBE) {
            targetBE.addLinkedStation(sourcePos);
            targetBE.setChanged();
            level.sendBlockUpdated(targetPos, targetState, targetState, 3);
        }

        if (added) {
            int total = sourceBE.getLinkedStationsSortedByY().size();
            player.sendSystemMessage(Component.literal(
                "§a[Aufzug] Station bei §e" + targetPos.toShortString()
                + "§a verknüpft. §7(" + total + " Station(en))"));
        } else {
            player.sendSystemMessage(Component.literal(
                "§7[Aufzug] Bereits verknüpft oder Stationslimit (32) erreicht."));
        }
        return InteractionResult.SUCCESS;
    }

    // ─────────────────────────────────────────────────────────────────
    // Hilfsmethoden
    // ─────────────────────────────────────────────────────────────────

    /**
     * Sendet ein Block-Update an den Controller und alle Filler-Blöcke,
     * damit die Tarnung in allen Chunks aktualisiert wird.
     */
    private static void sendCamoUpdateToAll(Level level, BlockPos controllerPos,
                                             BlockState controllerState, ElevatorBlockEntity be) {
        level.sendBlockUpdated(controllerPos, controllerState, controllerState, 3);
        for (int[] offset : be.getFillerOffsets()) {
            BlockPos fp = controllerPos.offset(offset[0], offset[1], offset[2]);
            BlockState fs = level.getBlockState(fp);
            level.sendBlockUpdated(fp, fs, fs, 3);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // BlockEntity
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElevatorBlockEntity(pos, state);
    }
}
