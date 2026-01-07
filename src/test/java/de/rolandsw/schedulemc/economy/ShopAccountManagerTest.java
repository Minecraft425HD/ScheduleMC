package de.rolandsw.schedulemc.economy;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link ShopAccountManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Shop account creation and retrieval</li>
 *   <li>Account existence checks</li>
 *   <li>Account removal</li>
 *   <li>Collection methods (getAllAccounts, getAllShopIds)</li>
 *   <li>Tick coordination for all shops</li>
 *   <li>Read-only view enforcement</li>
 * </ul>
 *
 * <p><b>Note:</b> This test clears the static ShopAccountManager state
 * between tests to ensure isolation.
 */
@DisplayName("ShopAccountManager Tests")
class ShopAccountManagerTest {

    private static final String SHOP_ID_1 = "shop-001";
    private static final String SHOP_ID_2 = "shop-002";
    private static final String SHOP_ID_3 = "shop-003";

    @Mock
    private Level mockLevel;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        clearAllAccounts();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearAllAccounts();
        mocks.close();
    }

    /**
     * Clears all accounts from static manager
     */
    private void clearAllAccounts() {
        List<String> allIds = ShopAccountManager.getAllShopIds();
        for (String id : allIds) {
            ShopAccountManager.removeAccount(id);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ACCOUNT CREATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Account Creation")
    class AccountCreationTests {

        @Test
        @DisplayName("getOrCreateAccount() should create new account if not exists")
        void getOrCreateAccountShouldCreateNew() {
            ShopAccount account = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            assertThat(account).isNotNull();
            assertThat(account.getShopId()).isEqualTo(SHOP_ID_1);
        }

        @Test
        @DisplayName("getOrCreateAccount() should return existing account if exists")
        void getOrCreateAccountShouldReturnExisting() {
            ShopAccount account1 = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccount account2 = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            assertThat(account1).isSameAs(account2);
        }

        @Test
        @DisplayName("getOrCreateAccount() should increment shop count")
        void getOrCreateAccountShouldIncrementCount() {
            assertThat(ShopAccountManager.getShopCount()).isZero();

            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            assertThat(ShopAccountManager.getShopCount()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ACCOUNT RETRIEVAL TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Account Retrieval")
    class AccountRetrievalTests {

        @Test
        @DisplayName("getAccount() should return null for non-existent account")
        void getAccountShouldReturnNullForNonExistent() {
            ShopAccount account = ShopAccountManager.getAccount("nonexistent-shop");

            assertThat(account).isNull();
        }

        @Test
        @DisplayName("getAccount() should return existing account")
        void getAccountShouldReturnExisting() {
            ShopAccount created = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            ShopAccount retrieved = ShopAccountManager.getAccount(SHOP_ID_1);

            assertThat(retrieved).isSameAs(created);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // EXISTENCE CHECKS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Existence Checks")
    class ExistenceTests {

        @Test
        @DisplayName("exists() should return false for non-existent account")
        void existsShouldReturnFalseForNonExistent() {
            boolean exists = ShopAccountManager.exists("nonexistent-shop");

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("exists() should return true for existing account")
        void existsShouldReturnTrueForExisting() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            boolean exists = ShopAccountManager.exists(SHOP_ID_1);

            assertThat(exists).isTrue();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ACCOUNT REMOVAL
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Account Removal")
    class RemovalTests {

        @Test
        @DisplayName("removeAccount() should remove existing account")
        void removeAccountShouldRemove() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            ShopAccountManager.removeAccount(SHOP_ID_1);

            assertThat(ShopAccountManager.exists(SHOP_ID_1)).isFalse();
            assertThat(ShopAccountManager.getShopCount()).isZero();
        }

        @Test
        @DisplayName("removeAccount() should handle non-existent account gracefully")
        void removeAccountShouldHandleNonExistent() {
            assertThatCode(() -> ShopAccountManager.removeAccount("nonexistent-shop"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("removeAccount() should decrement shop count")
        void removeAccountShouldDecrementCount() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_2);
            assertThat(ShopAccountManager.getShopCount()).isEqualTo(2);

            ShopAccountManager.removeAccount(SHOP_ID_1);

            assertThat(ShopAccountManager.getShopCount()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // COLLECTION METHODS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Collection Methods")
    class CollectionTests {

        @Test
        @DisplayName("getAllAccounts() should return all accounts")
        void getAllAccountsShouldReturnAll() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_2);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_3);

            Collection<ShopAccount> accounts = ShopAccountManager.getAllAccounts();

            assertThat(accounts).hasSize(3);
        }

        @Test
        @DisplayName("getAllAccounts() should return empty collection when no accounts")
        void getAllAccountsShouldReturnEmptyWhenNoAccounts() {
            Collection<ShopAccount> accounts = ShopAccountManager.getAllAccounts();

            assertThat(accounts).isEmpty();
        }

        @Test
        @DisplayName("getAllAccounts() should return unmodifiable collection")
        void getAllAccountsShouldReturnUnmodifiable() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            Collection<ShopAccount> accounts = ShopAccountManager.getAllAccounts();

            assertThatThrownBy(() -> accounts.clear())
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getAllShopIds() should return all shop IDs")
        void getAllShopIdsShouldReturnAll() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_2);

            List<String> ids = ShopAccountManager.getAllShopIds();

            assertThat(ids).containsExactlyInAnyOrder(SHOP_ID_1, SHOP_ID_2);
        }

        @Test
        @DisplayName("getAllShopIds() should return empty list when no accounts")
        void getAllShopIdsShouldReturnEmptyWhenNoAccounts() {
            List<String> ids = ShopAccountManager.getAllShopIds();

            assertThat(ids).isEmpty();
        }

        @Test
        @DisplayName("getAllShopIds() should return unmodifiable list")
        void getAllShopIdsShouldReturnUnmodifiable() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            List<String> ids = ShopAccountManager.getAllShopIds();

            assertThatThrownBy(() -> ids.clear())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // SHOP COUNT TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Shop Count")
    class ShopCountTests {

        @Test
        @DisplayName("getShopCount() should return 0 initially")
        void getShopCountShouldReturnZeroInitially() {
            assertThat(ShopAccountManager.getShopCount()).isZero();
        }

        @Test
        @DisplayName("getShopCount() should return correct count")
        void getShopCountShouldReturnCorrectCount() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_2);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_3);

            assertThat(ShopAccountManager.getShopCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getShopCount() should update after removal")
        void getShopCountShouldUpdateAfterRemoval() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_2);

            ShopAccountManager.removeAccount(SHOP_ID_1);

            assertThat(ShopAccountManager.getShopCount()).isEqualTo(1);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // TICK COORDINATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Tick Coordination")
    class TickCoordinationTests {

        @Test
        @DisplayName("tickAll() should not throw when no accounts")
        void tickAllShouldNotThrowWhenNoAccounts() {
            assertThatCode(() -> ShopAccountManager.tickAll(mockLevel))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("tickAll() should handle null level gracefully")
        void tickAllShouldHandleNullLevel() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            // This might throw NPE depending on ShopAccount.tick() implementation
            // Leaving as-is to test actual behavior
            assertThatCode(() -> ShopAccountManager.tickAll(null))
                .doesNotThrowAnyException();
        }
    }

    // ════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("full lifecycle: create → retrieve → remove")
        void fullLifecycle() {
            // Create
            ShopAccount account = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            assertThat(account).isNotNull();
            assertThat(ShopAccountManager.getShopCount()).isEqualTo(1);

            // Retrieve
            ShopAccount retrieved = ShopAccountManager.getAccount(SHOP_ID_1);
            assertThat(retrieved).isSameAs(account);

            // Exists check
            assertThat(ShopAccountManager.exists(SHOP_ID_1)).isTrue();

            // Remove
            ShopAccountManager.removeAccount(SHOP_ID_1);
            assertThat(ShopAccountManager.exists(SHOP_ID_1)).isFalse();
            assertThat(ShopAccountManager.getShopCount()).isZero();
        }

        @Test
        @DisplayName("multiple shops should coexist independently")
        void multipleShopsShouldCoexist() {
            ShopAccount account1 = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccount account2 = ShopAccountManager.getOrCreateAccount(SHOP_ID_2);
            ShopAccount account3 = ShopAccountManager.getOrCreateAccount(SHOP_ID_3);

            assertThat(account1).isNotSameAs(account2);
            assertThat(account2).isNotSameAs(account3);

            assertThat(ShopAccountManager.getShopCount()).isEqualTo(3);
            assertThat(ShopAccountManager.getAllAccounts()).containsExactlyInAnyOrder(account1, account2, account3);
        }

        @Test
        @DisplayName("removing one shop should not affect others")
        void removingOneShopShouldNotAffectOthers() {
            ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_2);
            ShopAccountManager.getOrCreateAccount(SHOP_ID_3);

            ShopAccountManager.removeAccount(SHOP_ID_2);

            assertThat(ShopAccountManager.exists(SHOP_ID_1)).isTrue();
            assertThat(ShopAccountManager.exists(SHOP_ID_2)).isFalse();
            assertThat(ShopAccountManager.exists(SHOP_ID_3)).isTrue();
            assertThat(ShopAccountManager.getShopCount()).isEqualTo(2);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle special characters in shop ID")
        void shouldHandleSpecialCharactersInShopId() {
            String specialId = "shop-§äö-123_ABC";

            ShopAccount account = ShopAccountManager.getOrCreateAccount(specialId);

            assertThat(account).isNotNull();
            assertThat(account.getShopId()).isEqualTo(specialId);
        }

        @Test
        @DisplayName("should handle very long shop IDs")
        void shouldHandleVeryLongShopIds() {
            String longId = "shop-" + "x".repeat(1000);

            ShopAccount account = ShopAccountManager.getOrCreateAccount(longId);

            assertThat(account).isNotNull();
            assertThat(account.getShopId()).isEqualTo(longId);
        }

        @Test
        @DisplayName("should handle creating same shop ID multiple times")
        void shouldHandleCreatingSameIdMultipleTimes() {
            ShopAccount account1 = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccount account2 = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);
            ShopAccount account3 = ShopAccountManager.getOrCreateAccount(SHOP_ID_1);

            assertThat(account1).isSameAs(account2);
            assertThat(account2).isSameAs(account3);
            assertThat(ShopAccountManager.getShopCount()).isEqualTo(1);
        }
    }
}
