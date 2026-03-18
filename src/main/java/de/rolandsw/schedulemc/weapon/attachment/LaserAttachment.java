package de.rolandsw.schedulemc.weapon.attachment;

import de.rolandsw.schedulemc.weapon.gun.GunItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LaserAttachment extends BaseAttachmentItem {
    public LaserAttachment() {
        super(WeaponAttachments.LASER);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack attachmentStack = player.getItemInHand(hand);
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof GunItem gun) {
            if (!level.isClientSide) {
                if (gun.hasAttachmentType(mainHand, Attachment.Type.LASER)) {
                    player.displayClientMessage(Component.literal("§cLaser bereits montiert!"), true);
                } else if (!gun.addAttachment(mainHand, WeaponAttachments.LASER)) {
                    player.displayClientMessage(Component.literal("§cMaximal 2 Zubehörteile pro Waffe!"), true);
                } else {
                    attachmentStack.shrink(1);
                    player.displayClientMessage(Component.literal("§aLaser angebracht!"), true);
                }
            }
            return InteractionResultHolder.success(attachmentStack);
        }
        return InteractionResultHolder.pass(attachmentStack);
    }
}
