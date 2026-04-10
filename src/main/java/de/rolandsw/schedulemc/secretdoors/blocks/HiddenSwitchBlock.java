package de.rolandsw.schedulemc.secretdoors.blocks;

import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Versteckter Schalter (Hidden Switch).
 * Sieht aus wie ein normaler Stein- oder Holzblock.
 * - Normaler Rechtsklick: Toggle alle verknüpften Türen
 * - Shift+Rechtsklick: Verknüpfungs-Modus aktivieren
 * - Im Verknüpfungs-Modus: Nächster Rechtsklick auf Tür verknüpft/trennt
 * - Redstone-Eingang: Toggle alle verknüpften Türen
 */
public class HiddenSwitchBlock extends BaseEntityBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public HiddenSwitchBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    // ─────────────────────────────────────────────────────────────────
    // Rendering: sieht aus wie normaler Vollblock
    // ─────────────────────────────────────────────────────────────────

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // INVISIBLE verhindert doppeltes Rendern (Blockmodell + BER) und vermeidet
        // die dunkle/schwarze Overlay-Schicht im Welt-Rendering.
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    // ─────────────────────────────────────────────────────────────────
    // Interaktion
    // ─────────────────────────────────────────────────────────────────

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof HiddenSwitchBlockEntity be)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // Shift+Rechtsklick mit Block → Tarnung entfernen
        if (player.isShiftKeyDown() && heldItem.getItem() instanceof BlockItem) {
            be.clearCamoBlock();
            level.sendBlockUpdated(pos, state, state, 3);
            player.sendSystemMessage(Component.literal("§7[Schalter] Tarnung entfernt."));
            return InteractionResult.SUCCESS;
        }

        // Rechtsklick mit Block → Tarnung setzen
        if (!player.isShiftKeyDown() && heldItem.getItem() instanceof BlockItem blockItem) {
            be.setCamoBlock(blockItem.getBlock());
            level.sendBlockUpdated(pos, state, state, 3);
            player.sendSystemMessage(Component.literal(
                "§a[Schalter] Tarnung gesetzt auf: §e" + heldItem.getHoverName().getString()));
            return InteractionResult.SUCCESS;
        }

        // Shift+Rechtsklick ohne Block: Verknüpfungs-Modus
        if (player.isShiftKeyDown()) {
            if (!be.canEdit(player)) {
                player.sendSystemMessage(Component.literal("§cKeine Berechtigung!"));
                return InteractionResult.SUCCESS;
            }
            boolean linking = !be.isLinkingMode();
            be.setLinkingMode(linking);
            be.setChanged();
            if (linking) {
                player.sendSystemMessage(Component.literal(
                    "§a[Schalter] Verknüpfungs-Modus §eAN§a. Klicke auf eine Geheimtür, um sie zu verknüpfen/trennen."));
            } else {
                player.sendSystemMessage(Component.literal(
                    "§7[Schalter] Verknüpfungs-Modus §cAUS§7."));
            }
            return InteractionResult.SUCCESS;
        }

        // Verknüpfungs-Modus aktiv? → nächste angeklickte Tür verknüpfen
        if (be.isLinkingMode()) {
            // Der Schalter selbst wurde angeklickt, nicht eine Tür → Modus beenden
            be.setLinkingMode(false);
            be.setChanged();
            player.sendSystemMessage(Component.literal("§7[Schalter] Verknüpfungs-Modus beendet."));
            return InteractionResult.SUCCESS;
        }

        // Normaler Rechtsklick: alle verknüpften Türen toggeln
        if (be.getLinkedDoorCount() == 0) {
            player.sendSystemMessage(Component.literal(
                "§7[Schalter] Keine Türen verknüpft. §eShift+Rechtsklick §7zum Verknüpfen."));
        } else {
            be.toggleLinkedDoors(level, player);
            player.sendSystemMessage(Component.literal(
                "§a[Schalter] §e" + be.getLinkedDoorCount() + " §aTür(en) geschaltet."));
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Wird aufgerufen wenn ein Spieler mit der Fernbedienung auf eine Tür klickt
     * und dieser Schalter im Verknüpfungs-Modus ist.
     */
    public void tryLinkDoor(Level level, BlockPos switchPos, BlockPos doorPos, Player player) {
        if (!(level.getBlockEntity(switchPos) instanceof HiddenSwitchBlockEntity be)) return;
        if (!be.isLinkingMode()) return;

        BlockState doorState = level.getBlockState(doorPos);
        if (!(doorState.getBlock() instanceof AbstractSecretDoorBlock)) {
            player.sendSystemMessage(Component.literal("§cDas ist keine Geheimtür!"));
            return;
        }

        boolean linked = be.linkDoor(doorPos, player);
        if (linked) {
            player.sendSystemMessage(Component.literal(
                "§a[Schalter] Tür bei §e" + doorPos.toShortString() + "§a verknüpft. " +
                "Gesamt: §e" + be.getLinkedDoorCount()));
            // Schalter auch im Türen-BE registrieren
            if (level.getBlockEntity(doorPos) instanceof SecretDoorBlockEntity doorBe) {
                doorBe.addLinkedSwitch(switchPos);
                doorBe.setChanged();
            }
        } else {
            player.sendSystemMessage(Component.literal(
                "§7[Schalter] Tür bei §e" + doorPos.toShortString() + "§7 getrennt."));
            if (level.getBlockEntity(doorPos) instanceof SecretDoorBlockEntity doorBe) {
                doorBe.removeLinkedSwitch(switchPos);
                doorBe.setChanged();
            }
        }
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
            if (isPowered && level.getBlockEntity(pos) instanceof HiddenSwitchBlockEntity be) {
                // Alle verknüpften Türen aktivieren (als "server-seitige Aktion")
                for (BlockPos doorPos : be.getLinkedDoors()) {
                    BlockState doorState = level.getBlockState(doorPos);
                    if (doorState.getBlock() instanceof AbstractSecretDoorBlock) {
                        if (level.getBlockEntity(doorPos) instanceof SecretDoorBlockEntity doorBe) {
                            if (!doorBe.isOpen()) {
                                doorBe.open(level);
                            }
                        }
                    }
                }
            } else if (!isPowered && level.getBlockEntity(pos) instanceof HiddenSwitchBlockEntity be) {
                for (BlockPos doorPos : be.getLinkedDoors()) {
                    BlockState doorState = level.getBlockState(doorPos);
                    if (doorState.getBlock() instanceof AbstractSecretDoorBlock) {
                        if (level.getBlockEntity(doorPos) instanceof SecretDoorBlockEntity doorBe) {
                            if (doorBe.isOpen()) {
                                doorBe.close(level);
                            }
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Platzierung: Besitzer setzen
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
                             @Nullable net.minecraft.world.entity.LivingEntity placer,
                             net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player) {
            if (level.getBlockEntity(pos) instanceof HiddenSwitchBlockEntity be) {
                be.setOwner(player);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // BlockEntity
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HiddenSwitchBlockEntity(pos, state);
    }
}
