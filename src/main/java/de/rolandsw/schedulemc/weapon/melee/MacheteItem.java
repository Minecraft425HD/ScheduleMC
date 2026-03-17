package de.rolandsw.schedulemc.weapon.melee;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MacheteItem extends MeleeWeaponItem {
    public MacheteItem() {
        super(Tiers.DIAMOND, 6, -2.4F, new Properties().durability(800));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.LEAVES) || state.is(Blocks.COBWEB) || state.is(Blocks.VINE) ||
                state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)) {
            return 15.0F;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.is(BlockTags.LEAVES) && !level.isClientSide) {
            level.destroyBlock(pos, false);
            stack.hurtAndBreak(1, entity, (e) -> e.broadcastBreakEvent(e.getUsedItemHand()));
            return true;
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getRandom().nextFloat() < 0.2f) {
            target.hurt(target.damageSources().mobAttack(attacker), 4.0f);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
