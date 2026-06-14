package de.rolandsw.schedulemc.npc;

import de.rolandsw.schedulemc.npc.crime.ItemLegalityChecker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-Tests für die zentrale Illegalitäts-Erkennung.
 * Vanilla-Items müssen legal sein; die Drogen-/Verarbeitungs-Fragmente illegal.
 */
public class ItemLegalityCheckerTest {

    @BeforeAll
    static void init() {
        de.rolandsw.schedulemc.test.TestEnvironment.init();
    }

    @Test
    @DisplayName("Empty and vanilla items are legal")
    void vanillaLegal() {
        assertThat(ItemLegalityChecker.isIllegal(ItemStack.EMPTY)).isFalse();
        assertThat(ItemLegalityChecker.isIllegal(new ItemStack(Items.BREAD))).isFalse();
        assertThat(ItemLegalityChecker.isIllegal(new ItemStack(Items.DIAMOND))).isFalse();
        assertThat(ItemLegalityChecker.isIllegal(new ItemStack(Blocks.STONE.asItem()))).isFalse();
    }

    @Test
    @DisplayName("estimateValue scales with stack count")
    void valueScales() {
        ItemStack one = new ItemStack(Items.BREAD, 1);
        ItemStack four = new ItemStack(Items.BREAD, 4);
        // Vanilla bread is legal -> value still computed by count for the helper
        assertThat(ItemLegalityChecker.estimateValue(four))
            .isEqualTo(ItemLegalityChecker.estimateValue(one) * 4);
        assertThat(ItemLegalityChecker.estimateValue(ItemStack.EMPTY)).isZero();
    }
}
