package de.rolandsw.schedulemc.secretdoors.blocks;

import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Basis-Block für alle geheimen Türtypen.
 * Steuert: Platzierung mit Füller-Blöcken, Öffnen/Schließen, Redstone-Auslösung.
 */
public abstract class AbstractSecretDoorBlock extends BaseEntityBlock {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected final DoorType doorType;

    protected AbstractSecretDoorBlock(BlockBehaviour.Properties props, DoorType doorType) {
        super(props);
        this.doorType = doorType;
        registerDefaultState(stateDefinition.any()
            .setValue(OPEN, false)
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false));
    }

    // ─────────────────────────────────────────────────────────────────
    // Block State
    // ─────────────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
            .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
            .setValue(OPEN, false)
            .setValue(POWERED, false);
    }

    // ─────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Immer ENTITYBLOCK_ANIMATED: Renderer übernimmt Darstellung (inkl. Tarnung)
        return state.getValue(OPEN) ? RenderShape.INVISIBLE : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // Wenn offen: kein Kollisions-Shape
        if (state.getValue(OPEN)) return Shapes.empty();
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(OPEN)) return Shapes.empty();
        return Shapes.block();
    }

    // ─────────────────────────────────────────────────────────────────
    // Platzierung: Füller-Blöcke aufspannen
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                             @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;

        // Besitzer setzen
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be) {
            be.setOwner(player);
            // Standard-Größe: 1×2
            spawnFillers(level, pos, be, 1, 2, state.getValue(FACING));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Füller-Blöcke aufspannen
    // ─────────────────────────────────────────────────────────────────

    /**
     * Spawnt Füller-Blöcke für eine gegebene Größe.
     * @param controllerPos Position des Controller-Blocks (unten-links)
     * @param be BlockEntity des Controllers
     * @param width Breite (1-20)
     * @param height Höhe (1-20)
     * @param facing Ausrichtung des Türrahmens
     */
    public void spawnFillers(Level level, BlockPos controllerPos, SecretDoorBlockEntity be,
                              int width, int height, Direction facing) {
        if (level.isClientSide) return;

        // Alte Füller löschen
        for (int[] offset : be.getFillerOffsets()) {
            BlockPos fp = controllerPos.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(fp).is(SecretDoors.DOOR_FILLER.get())) {
                level.setBlockAndUpdate(fp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
        be.clearFillerOffsets();
        be.setOpen(false);

        // Richtungsvektoren berechnen (seitwärts = rechts vom FACING)
        Direction right = facing.getClockWise();

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                if (w == 0 && h == 0) continue; // Controller selbst überspringen

                int dx, dy, dz;
                if (doorType == DoorType.HATCH) {
                    // Luke: breitet sich auf dem Boden aus
                    dx = w * right.getStepX();
                    dy = 0;
                    dz = w * right.getStepZ() + h * facing.getStepZ();
                } else {
                    // Wand-Türen: breitet sich seitwärts und nach oben aus
                    dx = w * right.getStepX();
                    dy = h;
                    dz = w * right.getStepZ();
                }

                BlockPos fillerPos = controllerPos.offset(dx, dy, dz);
                // Nur spawnen wenn Platz frei
                if (level.getBlockState(fillerPos).isAir() ||
                    level.getBlockState(fillerPos).is(SecretDoors.DOOR_FILLER.get())) {

                    level.setBlockAndUpdate(fillerPos, SecretDoors.DOOR_FILLER.get().defaultBlockState());
                    if (level.getBlockEntity(fillerPos) instanceof DoorFillerBlockEntity fbe) {
                        fbe.setControllerPos(controllerPos);
                        fbe.setChanged();
                    }
                    be.addFillerOffset(dx, dy, dz);
                }
            }
        }

        be.setChanged();
        level.sendBlockUpdated(controllerPos, level.getBlockState(controllerPos),
            level.getBlockState(controllerPos), 3);
    }

    // ─────────────────────────────────────────────────────────────────
    // Abbau: Alle Füller entfernen
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be) {
                removeAllFillers(level, pos, be);
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    /**
     * Alle Füller-Blöcke eines Controllers entfernen.
     */
    public static void removeAllFillers(Level level, BlockPos controllerPos, SecretDoorBlockEntity be) {
        for (int[] offset : be.getFillerOffsets()) {
            BlockPos fillerPos = controllerPos.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(fillerPos).is(SecretDoors.DOOR_FILLER.get())) {
                level.setBlockAndUpdate(fillerPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
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

        if (!(level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // Shift+Rechtsklick mit leerem Hand → Größen-Konfiguration
        if (player.isShiftKeyDown() && heldItem.isEmpty()) {
            openSizeConfig(level, pos, be, player, state.getValue(FACING));
            return InteractionResult.SUCCESS;
        }

        // Shift+Rechtsklick mit Block → Tarnung entfernen
        if (player.isShiftKeyDown() && heldItem.getItem() instanceof BlockItem) {
            be.clearCamoBlock();
            level.sendBlockUpdated(pos, state, state, 3);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7Tarnung entfernt."));
            return InteractionResult.SUCCESS;
        }

        // Rechtsklick mit Block → Tarnung setzen
        if (!player.isShiftKeyDown() && heldItem.getItem() instanceof BlockItem blockItem) {
            be.setCamoBlock(blockItem.getBlock());
            level.sendBlockUpdated(pos, state, state, 3);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§aTarnung gesetzt auf: §e" + heldItem.getHoverName().getString()));
            return InteractionResult.SUCCESS;
        }

        // Normaler Rechtsklick → Tür toggeln
        be.toggle(level, player);
        return InteractionResult.SUCCESS;
    }

    /**
     * Öffnet die Größen-Konfiguration (Chat-Nachricht mit Befehlen als Alternative zur GUI).
     * Da wir kein Screen implementieren, nutzen wir Chat-Nachrichten mit Hinweisen.
     */
    protected void openSizeConfig(Level level, BlockPos pos, SecretDoorBlockEntity be,
                                   Player player, Direction facing) {
        if (level.isClientSide) return;
        int w = be.getDoorWidth();
        int h = be.getDoorHeight();
        player.sendSystemMessage(Component.literal(
            "§6=== Türgröße konfigurieren ===\n" +
            "§7Aktuelle Größe: §e" + w + "×" + h + "\n" +
            "§7Tipp: §aSchneide mit §eFernbedienung§a oder nutze\n" +
            "§7/secretdoor size " + pos.getX() + " " + pos.getY() + " " + pos.getZ() +
            " <breite 1-20> <höhe 1-20>\n" +
            "§7Besitzer: §b" + (be.getOwnerName().isEmpty() ? "Niemand" : be.getOwnerName())
        ));
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

        if (isPowered != wasPowered) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, isPowered));
            if (level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be) {
                if (isPowered && !be.isOpen()) {
                    be.open(level);
                } else if (!isPowered && be.isOpen()) {
                    be.close(level);
                }
            }
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    // ─────────────────────────────────────────────────────────────────
    // BlockEntity
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SecretDoorBlockEntity(pos, state);
    }

    // ─────────────────────────────────────────────────────────────────
    // Getter
    // ─────────────────────────────────────────────────────────────────

    public DoorType getDoorType() { return doorType; }

    // ─────────────────────────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────────────────────────

    public enum DoorType {
        PIVOT,   // Geheimtür (horizontal, Wand)
        HATCH    // Bodenluke (vertikal, Boden/Decke)
    }
}
