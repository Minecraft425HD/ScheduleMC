package de.rolandsw.schedulemc.npc;

import de.rolandsw.schedulemc.npc.life.core.NPCTraits;
import de.rolandsw.schedulemc.npc.life.quest.SupplyRequestPlanner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-Tests für die Warenanfragen-Planung (reine Logik).
 */
public class SupplyRequestPlannerTest {

    @BeforeAll
    static void initTestEnvironment() {
        de.rolandsw.schedulemc.test.TestEnvironment.init();
    }

    @Test
    @DisplayName("Amount stays within 4-16 for all greed values")
    void testAmountBounds() {
        Random random = new Random(42);
        for (int greed = -100; greed <= 100; greed += 25) {
            NPCTraits traits = new NPCTraits(0, 0, greed);
            for (int i = 0; i < 50; i++) {
                int amount = SupplyRequestPlanner.calculateAmount(traits, random);
                assertThat(amount).isBetween(4, 16);
            }
        }
    }

    @Test
    @DisplayName("Greedy NPCs ask for more on average")
    void testGreedIncreasesAmount() {
        long greedySum = 0;
        long generousSum = 0;
        NPCTraits greedy = new NPCTraits(0, 0, 100);
        NPCTraits generous = new NPCTraits(0, 0, -100);
        Random r1 = new Random(7);
        Random r2 = new Random(7);
        for (int i = 0; i < 500; i++) {
            greedySum += SupplyRequestPlanner.calculateAmount(greedy, r1);
            generousSum += SupplyRequestPlanner.calculateAmount(generous, r2);
        }
        assertThat(greedySum).isGreaterThan(generousSum);
    }

    @Test
    @DisplayName("Payment scales with unit value, amount and greed")
    void testPaymentFormula() {
        NPCTraits neutral = new NPCTraits(0, 0, 0);
        NPCTraits greedy = new NPCTraits(0, 0, 100);

        int neutralPay = SupplyRequestPlanner.calculatePayment(4, 10, neutral);
        assertThat(neutralPay).isEqualTo(Math.round(4 * 10 * 1.25f));

        int greedyPay = SupplyRequestPlanner.calculatePayment(4, 10, greedy);
        assertThat(greedyPay).isGreaterThan(neutralPay);

        // Mindestens 1€, auch bei Kleinstwerten
        assertThat(SupplyRequestPlanner.calculatePayment(0, 1, neutral)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Negative greed does not reduce payment below base")
    void testGenerousPaymentNotBelowBase() {
        NPCTraits generous = new NPCTraits(0, 0, -100);
        int base = Math.round(5 * 8 * 1.25f);
        assertThat(SupplyRequestPlanner.calculatePayment(5, 8, generous)).isEqualTo(base);
    }
}
