package de.rolandsw.schedulemc.npc.items;

import de.rolandsw.schedulemc.npc.menu.NPCSpawnerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Tool zum Spawnen von Custom NPCs
 * - Rechtsklick auf Block: Öffnet GUI zum Auswählen des NPC-Typs
 */
public class NPCSpawnerTool extends Item {

    public NPCSpawnerTool() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Speichere die Position für das Spawnen
            // Öffne GUI zum Konfigurieren des NPCs
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                (id, playerInventory, p) -> new NPCSpawnerMenu(id, playerInventory, context.getClickedPos()),
                Component.translatable("gui.npc.spawner_title")
            ), buf -> {
                buf.writeBlockPos(context.getClickedPos());
            });

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Öffne GUI auch bei Rechtsklick in die Luft
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                (id, playerInventory, p) -> new NPCSpawnerMenu(id, playerInventory, player.blockPosition()),
                Component.translatable("gui.npc.spawner_title")
            ), buf -> {
                buf.writeBlockPos(player.blockPosition());
            });

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }
}
