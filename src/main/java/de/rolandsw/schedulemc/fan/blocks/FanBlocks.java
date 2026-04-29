package de.rolandsw.schedulemc.fan.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.fan.FanTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Registrierung der drei Ventilator-Blöcke.
 *
 * In {@code ScheduleMC} eintragen:
 * <pre>
 *   FanBlocks.BLOCKS.register(modEventBus);
 *   FanBlocks.ITEMS.register(modEventBus);
 * </pre>
 */
public class FanBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ─── Tier-1: Kleiner Ventilator (Eisen, +50 % Geschwindigkeit) ──────────
    public static final RegistryObject<Block> FAN_TIER_1 = register("fan_tier1",
            () -> new FanBlock(FanTier.TIER_1,
                    BlockBehaviour.Properties.of()
                            .strength(2.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    // ─── Tier-2: Großer Ventilator (2 Blöcke hoch, +150 % Geschwindigkeit) ────
    public static final RegistryObject<Block> FAN_TIER_2 = register("fan_tier2",
            () -> new TallFanBlock(FanTier.TIER_2,
                    BlockBehaviour.Properties.of()
                            .strength(2.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    // ─── Tier-3: Industrieventilator (2 Blöcke hoch, +300 % Geschwindigkeit) ─
    public static final RegistryObject<Block> FAN_TIER_3 = register("fan_tier3",
            () -> new TallFanBlock(FanTier.TIER_3,
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    // ─── Hilfsmethode: Block + BlockItem gleichzeitig registrieren ────────────
    private static RegistryObject<Block> register(String name, Supplier<FanBlock> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, factory);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
