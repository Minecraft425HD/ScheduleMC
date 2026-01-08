package de.rolandsw.schedulemc.mushroom.blocks;

import de.rolandsw.schedulemc.mushroom.blockentity.ClimateLampBlockEntity;
import de.rolandsw.schedulemc.mushroom.blockentity.MushroomBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * ClimateLamp-Block mit 3 Modi: Kalt, Warm, Aus
 */
public class ClimateLampBlock extends Block implements EntityBlock {

    public static final EnumProperty<TemperatureMode> MODE = EnumProperty.create("mode", TemperatureMode.class);

    private final ClimateLampTier tier;

    public ClimateLampBlock(ClimateLampTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(MODE, TemperatureMode.OFF));
    }

    public ClimateLampTier getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClimateLampBlockEntity(pos, state, tier);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof ClimateLampBlockEntity klimalampe) {
                klimalampe.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // Bei automatischen Lampen: Nur Info anzeigen
        if (tier.isAutomatic()) {
            TemperatureMode currentMode = state.getValue(MODE);
            player.displayClientMessage(Component.literal(
                    "§7" + tier.getColoredName() + "\n" +
                            "§7Modus: " + currentMode.getColoredName() + "\n" +
                            "§8(Automatische Regulierung)"
            ), true);
            return InteractionResult.SUCCESS;
        }

        // Manuell: Modus wechseln
        TemperatureMode currentMode = state.getValue(MODE);
        TemperatureMode nextMode = currentMode.next();
        level.setBlock(pos, state.setValue(MODE, nextMode), 3);

        player.displayClientMessage(Component.literal(
                "§7Modus gewechselt: " + nextMode.getColoredName()
        ), true);

        player.playSound(net.minecraft.sounds.SoundEvents.LEVER_CLICK, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        TemperatureMode mode = state.getValue(MODE);
        return switch (mode) {
            case COLD -> 4;   // Leichtes blaues Licht
            case WARM -> 8;   // Wärmeres Licht
            case OFF -> 0;
        };
    }
}
