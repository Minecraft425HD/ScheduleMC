package de.rolandsw.schedulemc.weapon.grenade;

import de.rolandsw.schedulemc.weapon.entity.ThrownWeaponGrenade;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class GrenadeItem extends Item {
    private final GrenadeType type;

    public GrenadeItem(GrenadeType type) {
        super(new Properties().stacksTo(16));
        this.type = type;
    }

    public GrenadeType getType() { return type; }

    /** Cooldown zwischen zwei Würfen in Ticks (3 Sekunden = 60 Ticks). */
    private static final int THROW_COOLDOWN_TICKS = 60;

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Cooldown-Check — gilt pro Granaten-Typ (jeder Typ hat eigenen Cooldown-Eintrag)
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            ThrownWeaponGrenade grenade = new ThrownWeaponGrenade(level, player, type);
            grenade.setItem(stack);
            grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(grenade);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        // Cooldown starten — graut den Slot im Hotbar visuell aus
        player.getCooldowns().addCooldown(this, THROW_COOLDOWN_TICKS);

        return InteractionResultHolder.success(stack);
    }
}
