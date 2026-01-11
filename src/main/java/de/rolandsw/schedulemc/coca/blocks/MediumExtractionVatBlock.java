package de.rolandsw.schedulemc.coca.blocks;

import de.rolandsw.schedulemc.coca.blockentity.MediumExtractionVatBlockEntity;
import de.rolandsw.schedulemc.vehicle.items.ItemBioDieselCanister;
import de.rolandsw.schedulemc.coca.items.FreshCocaLeafItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Mittlere Extraktionswanne - verarbeitet Koka-Blätter + Diesel zu Koka-Paste
 */
public class MediumExtractionVatBlock extends Block implements EntityBlock {

    public MediumExtractionVatBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MediumExtractionVatBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof MediumExtractionVatBlockEntity vatBE) {
                vatBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MediumExtractionVatBlockEntity vatBE)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);

        // 1. Diesel einfüllen
        if (handStack.getItem() instanceof ItemBioDieselCanister) {
            int dieselInCanister = ItemBioDieselCanister.getFuel(handStack);
            if (dieselInCanister <= 0) {
                player.displayClientMessage(Component.translatable("message.medium_extraction_vat.canister_empty"), true);
                return InteractionResult.FAIL;
            }

            int maxDiesel = vatBE.getMaxDiesel();
            int currentDiesel = vatBE.getDieselLevel();
            int space = maxDiesel - currentDiesel;

            if (space <= 0) {
                player.displayClientMessage(Component.translatable("message.medium_extraction_vat.diesel_tank_full"), true);
                return InteractionResult.FAIL;
            }

            int toAdd = Math.min(dieselInCanister, space);
            vatBE.addDiesel(toAdd);
            ItemBioDieselCanister.consumeFuel(handStack, toAdd);

            player.displayClientMessage(Component.translatable("message.medium_extraction_vat.diesel_added", vatBE.getDieselLevel(), maxDiesel), true);

            player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, 1.0f, 0.8f);
            return InteractionResult.SUCCESS;
        }

        // 2. Koka-Blätter hinzufügen
        if (handStack.getItem() instanceof FreshCocaLeafItem) {
            if (vatBE.isFull()) {
                player.displayClientMessage(Component.translatable("message.medium_extraction_vat.vat_full"), true);
                return InteractionResult.FAIL;
            }

            if (vatBE.addFreshLeaves(handStack)) {
                handStack.shrink(1);
                player.displayClientMessage(Component.translatable("message.medium_extraction_vat.leaves_added", vatBE.getInputCount(), vatBE.getCapacity()), true);
                player.playSound(net.minecraft.sounds.SoundEvents.CROP_PLANTED, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Paste entnehmen
        if (handStack.isEmpty() && player.isShiftKeyDown()) {
            if (vatBE.hasOutput()) {
                ItemStack paste = vatBE.extractAllPaste();
                if (!paste.isEmpty()) {
                    player.getInventory().add(paste);
                    player.displayClientMessage(Component.translatable("message.medium_extraction_vat.paste_extracted", paste.getCount()), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // 4. Status anzeigen
        if (handStack.isEmpty() && !player.isShiftKeyDown()) {
            float progress = vatBE.getAverageExtractionPercentage() * 100;
            player.displayClientMessage(Component.translatable("message.medium_extraction_vat.status", vatBE.getDieselLevel(), vatBE.getMaxDiesel(), vatBE.getInputCount(), vatBE.getCapacity(), vatBE.getOutputCount(), String.format("%.1f", progress)), false);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
