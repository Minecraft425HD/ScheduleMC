# ScheduleMC Testing Guide

**Version:** 3.7.0-beta | **Minecraft:** 1.20.1 | **Forge:** 47.4.0 | **Java:** 17

Complete guide to testing the ScheduleMC mod — running tests, understanding coverage, writing new tests, and using the CI/CD pipeline.

---

## Table of Contents

1. [Testing Overview](#testing-overview)
2. [Test Infrastructure](#test-infrastructure)
3. [Running Tests](#running-tests)
4. [Test Categories](#test-categories)
   - [Economy Tests](#economy-tests)
   - [NPC Tests](#npc-tests)
   - [Production Tests](#production-tests)
   - [Gang Tests](#gang-tests)
   - [Vehicle Tests](#vehicle-tests)
   - [Utility Tests](#utility-tests)
   - [Integration Tests](#integration-tests)
   - [Command Tests](#command-tests)
5. [Code Coverage](#code-coverage)
6. [Writing New Tests](#writing-new-tests)
7. [Mocking Forge and Minecraft Classes](#mocking-forge-and-minecraft-classes)
8. [CI/CD Pipeline](#cicd-pipeline)
9. [Static Analysis](#static-analysis)
10. [Troubleshooting Test Failures](#troubleshooting-test-failures)

---

## Testing Overview

ScheduleMC has a comprehensive test suite built with **JUnit 5**, **Mockito**, and **AssertJ**. Because the mod runs within the Forge framework (which requires a live Minecraft environment), tests use mocking and abstract utilities to test business logic independently of Minecraft internals.

### Test Statistics

| Metric | Value |
|--------|-------|
| Total test files | 38 |
| Unit test files | 35 |
| Integration test files | 3 |
| Minimum overall coverage | 60% |
| Minimum utility class coverage | 80% |
| Test framework | JUnit Jupiter 5.10.1 |
| Mocking library | Mockito 5.8.0 |
| Assertion library | AssertJ 3.24.2 |
| Coverage tool | JaCoCo 0.8.11 |

---

## Test Infrastructure

### Dependencies (build.gradle)

```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.10.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.10.1'
    testImplementation 'org.mockito:mockito-core:5.8.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.8.0'
    testImplementation 'org.assertj:assertj-core:3.24.2'
}

test {
    useJUnitPlatform()
    jvmArgs '-Xmx2G'
}
```

### Mockito Extension

Mockito's JUnit 5 extension is configured via:

**File:** `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

This enables `@MockitoSettings`, `@ExtendWith(MockitoExtension.class)`, and `@Mock` annotations.

### Test Directory Structure

```
src/test/java/de/rolandsw/schedulemc/
├── economy/
│   ├── WalletManagerTest.java
│   ├── LoanManagerTest.java
│   ├── OverdraftManagerTest.java
│   ├── TaxManagerTest.java
│   ├── TransactionHistoryTest.java
│   ├── CreditScoreManagerTest.java
│   ├── SavingsAccountManagerTest.java
│   └── InterestManagerTest.java
├── npc/
│   ├── NPCCrimeTest.java
│   ├── NPCEmotionTest.java
│   └── NPCNeedsTest.java
├── production/
│   ├── PlantGrowthTest.java
│   ├── ProductionSerializationTest.java
│   └── ProductionSizesTest.java
├── gang/
│   ├── GangMissionsTest.java
│   ├── GangReputationTest.java
│   └── GangLevelTest.java
├── vehicle/
│   ├── VehicleFuelTest.java
│   ├── VehicleTireTest.java
│   └── VehicleConstantsTest.java
├── util/
│   ├── ValidationUtilTest.java
│   ├── EventBusTest.java
│   ├── PacketHandlerTest.java
│   ├── ConfigCacheTest.java
│   └── ThreadPoolManagerTest.java
├── integration/
│   ├── EconomyIntegrationTest.java
│   ├── NPCIntegrationTest.java
│   └── ProductionChainIntegrationTest.java
└── commands/
    └── CommandExecutorTest.java
```

---

## Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run All Tests with explicit Java 17 (recommended in mixed environments)

If your default JVM is newer than Java 17 (for example Java 25), Gradle script compilation can fail before tests start.
Use the helper script below, which searches for a Java 17 installation and sets `JAVA_HOME` for the test run:

```bash
bash scripts/run_gradle_tests.sh
```

The helper searches common Java-17 locations (including SDKMAN, ASDF, Linux package paths, and Homebrew/macOS paths) before failing.
If no Java 17 is found, it also triggers `scripts/toolchain_check.sh` output for diagnostics.

Quick preflight check:

```bash
bash scripts/toolchain_check.sh
```

Optional (if Java 17 is not auto-detected):

```bash
JAVA17_HOME=/path/to/jdk17 bash scripts/run_gradle_tests.sh
```

Check-only preflight (detect Java 17 without launching Gradle):

```bash
bash scripts/run_gradle_tests.sh --check-only
```

`--check-only` is mutually exclusive with Gradle task arguments.

You can also pass arbitrary Gradle args/tasks through the helper:

```bash
bash scripts/run_gradle_tests.sh test --tests "de.rolandsw.schedulemc.economy.*"
```

Optional all-in-one local preflight:

```bash
bash scripts/local_quality_pipeline.sh
```

By default this pipeline continues with static checks if Java 17 is missing.
Set `STRICT_TOOLCHAIN=1` to fail fast instead:

```bash
STRICT_TOOLCHAIN=1 bash scripts/local_quality_pipeline.sh
```

Equivalent flag form:

```bash
bash scripts/local_quality_pipeline.sh --strict-toolchain
```

Unknown flags are treated as errors to avoid silent typo-misconfiguration.

### Run Specific Test Class

```bash
./gradlew test --tests "de.rolandsw.schedulemc.economy.WalletManagerTest"
```

### Run a Specific Test Method

```bash
./gradlew test --tests "de.rolandsw.schedulemc.economy.WalletManagerTest.testDeposit"
```

### Run Tests by Category Tag

JUnit 5 `@Tag` annotations organize tests by category:

```bash
# Run all economy tests
./gradlew test --tests "de.rolandsw.schedulemc.economy.*"

# Run all integration tests
./gradlew test --tests "de.rolandsw.schedulemc.integration.*"
```

### Run Tests with Verbose Output

```bash
./gradlew test --info
```

### Generate Coverage Report

```bash
./gradlew test jacocoTestReport
```

Report output: `build/reports/jacoco/test/html/index.html`

### Run Tests and Enforce Coverage Minimum

```bash
./gradlew test jacocoTestCoverageVerification
```

This fails the build if coverage drops below the configured minimums (60% overall, 80% for utilities).

---

## Test Categories

### Economy Tests

**Package:** `de.rolandsw.schedulemc.economy`

**8 test files** covering the financial simulation:

| Test Class | Coverage Area |
|-----------|---------------|
| `WalletManagerTest` | Deposit, withdraw, transfer, balance queries, insufficient funds |
| `LoanManagerTest` | Loan issuance, repayment, interest calculation, default handling |
| `OverdraftManagerTest` | Overdraft limit enforcement, interest accrual, auto-settlement |
| `TaxManagerTest` | Property tax calculation, sales tax, income tax brackets |
| `TransactionHistoryTest` | Audit trail persistence, history retrieval, 1000-entry limit |
| `CreditScoreManagerTest` | Score calculation, impact on loan eligibility |
| `SavingsAccountManagerTest` | Savings deposits, weekly interest, early withdrawal penalties |
| `InterestManagerTest` | Interest calculation formulas, periodic accrual |

**Example test:**

```java
@ExtendWith(MockitoExtension.class)
class WalletManagerTest {

    @Mock
    private EconomyManager economyManager;

    @InjectMocks
    private WalletManager walletManager;

    @Test
    void testDeposit_ValidAmount_IncreasesBalance() {
        // Given
        UUID playerUUID = UUID.randomUUID();
        double initialBalance = 100.0;
        double depositAmount = 50.0;

        when(economyManager.getBalance(playerUUID)).thenReturn(initialBalance);

        // When
        boolean result = walletManager.deposit(playerUUID, depositAmount);

        // Then
        assertThat(result).isTrue();
        verify(economyManager).setBalance(playerUUID, initialBalance + depositAmount);
    }

    @Test
    void testWithdraw_InsufficientFunds_ReturnsFalse() {
        UUID playerUUID = UUID.randomUUID();
        when(economyManager.getBalance(playerUUID)).thenReturn(10.0);

        boolean result = walletManager.withdraw(playerUUID, 100.0);

        assertThat(result).isFalse();
    }
}
```

---

### NPC Tests

**Package:** `de.rolandsw.schedulemc.npc`

**3 test files** covering NPC simulation:

| Test Class | Coverage Area |
|-----------|---------------|
| `NPCCrimeTest` | Crime detection, evidence creation, witness reporting |
| `NPCEmotionTest` | Emotion state transitions, mood calculation |
| `NPCNeedsTest` | Hunger, fatigue, need decay over time |

---

### Production Tests

**Package:** `de.rolandsw.schedulemc.production`

**3 test files** covering the production chain framework:

| Test Class | Coverage Area |
|-----------|---------------|
| `PlantGrowthTest` | Growth state machine transitions, stage progression |
| `ProductionSerializationTest` | NBT serialization/deserialization of plant state |
| `ProductionSizesTest` | Small/medium/large machine capacity calculations |

---

### Gang Tests

**Package:** `de.rolandsw.schedulemc.gang`

**3 test files** covering gang mechanics:

| Test Class | Coverage Area |
|-----------|---------------|
| `GangMissionsTest` | Mission generation, completion, reward calculation |
| `GangReputationTest` | Reputation tier transitions, XP thresholds |
| `GangLevelTest` | Level-up formulas, perk point calculations, territory limits |

---

### Vehicle Tests

**Package:** `de.rolandsw.schedulemc.vehicle`

**3 test files** covering vehicle mechanics:

| Test Class | Coverage Area |
|-----------|---------------|
| `VehicleFuelTest` | Fuel consumption rates, refueling, tank capacity |
| `VehicleTireTest` | Tire wear, pressure effects, replacement logic |
| `VehicleConstantsTest` | Engine specs, chassis properties, speed limits |

---

### Utility Tests

**Package:** `de.rolandsw.schedulemc.util`

**5 test files** covering shared utility classes:

| Test Class | Coverage Area |
|-----------|---------------|
| `ValidationUtilTest` | Input validation, UUID validation, string constraints |
| `EventBusTest` | Event dispatching, subscriber management |
| `PacketHandlerTest` | Packet encoding/decoding, size limits |
| `ConfigCacheTest` | Cache invalidation, config value retrieval |
| `ThreadPoolManagerTest` | Thread pool creation, task submission, shutdown |

Utility tests must achieve **80% coverage minimum** (enforced by JaCoCo verification).

---

### Integration Tests

**Package:** `de.rolandsw.schedulemc.integration`

**3 integration test files** test multi-system interactions:

| Test Class | Coverage Area |
|-----------|---------------|
| `EconomyIntegrationTest` | Full transaction flow: deposit → tax → loan → repayment |
| `NPCIntegrationTest` | NPC lifecycle: spawn → schedule → interact → despawn |
| `ProductionChainIntegrationTest` | Full production chain: plant → grow → harvest → sell → XP |

Integration tests use more complex mocking and test system boundaries.

---

### Command Tests

**Package:** `de.rolandsw.schedulemc.commands`

| Test Class | Coverage Area |
|-----------|---------------|
| `CommandExecutorTest` | Command parsing, permission checking, error handling |

---

## Code Coverage

### JaCoCo Configuration

```groovy
jacocoTestReport {
    reports {
        xml.required = true     // For CI/CD
        html.required = true    // For human review
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.60  // 60% overall
            }
        }
        rule {
            element = 'PACKAGE'
            includes = ['de.rolandsw.schedulemc.util.*']
            limit {
                minimum = 0.80  // 80% for utility classes
            }
        }
    }
}
```

### Coverage Reports

After running `./gradlew jacocoTestReport`:

| Report | Location |
|--------|----------|
| HTML report | `build/reports/jacoco/test/html/index.html` |
| XML report | `build/reports/jacoco/test/jacocoTestReport.xml` |
| CSV report | `build/reports/jacoco/test/jacocoTestReport.csv` |

### Coverage Exclusions

Classes excluded from coverage requirements:
- All `*Packet.java` files (network packets) — minimal logic
- All `*BlockEntity.java` files — Forge lifecycle boilerplate
- Data generation classes (`src/main/java/.../data/`)
- `ScheduleMC.java` (main class) — Forge registration only

---

## Writing New Tests

### Test Naming Convention

```
MethodName_StateUnderTest_ExpectedBehavior
```

Examples:
- `testDeposit_NegativeAmount_ThrowsIllegalArgument`
- `testGetBalance_NonExistentPlayer_ReturnsZero`
- `testWithdraw_ExactBalance_SucceedsAndLeavesZero`

### Test Structure (AAA Pattern)

```java
@Test
void testMethodName_Condition_ExpectedResult() {
    // Arrange (Given)
    UUID playerUUID = UUID.randomUUID();
    // ... set up state

    // Act (When)
    ResultType result = systemUnderTest.methodUnderTest(args);

    // Assert (Then)
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(expectedValue);
}
```

### Test Class Template

```java
package de.rolandsw.schedulemc.mypackage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyManagerTest {

    @Mock
    private DependencyClass dependency;

    @InjectMocks
    private MyManager systemUnderTest;

    @BeforeEach
    void setUp() {
        // Optional: additional setup
    }

    @Test
    void testHappyPath_ValidInput_ReturnsExpected() {
        // Arrange
        when(dependency.getSomeValue()).thenReturn("mocked");

        // Act
        String result = systemUnderTest.doSomething("input");

        // Assert
        assertThat(result).isEqualTo("expected");
        verify(dependency).getSomeValue();
    }

    @Test
    void testNullInput_ThrowsIllegalArgument() {
        assertThatThrownBy(() -> systemUnderTest.doSomething(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("null");
    }
}
```

---

## Mocking Forge and Minecraft Classes

Because ScheduleMC runs in Forge, many classes have Minecraft dependencies. Use these patterns:

### Mocking MinecraftServer

```java
@Mock
private MinecraftServer server;

@Mock
private PlayerList playerList;

@BeforeEach
void setUp() {
    when(server.getPlayerList()).thenReturn(playerList);
}
```

### Mocking ServerPlayer (Player)

```java
@Mock
private ServerPlayer player;

@Mock
private PlayerData playerData;

@BeforeEach
void setUp() {
    UUID testUUID = UUID.randomUUID();
    when(player.getUUID()).thenReturn(testUUID);
    when(player.getDisplayName()).thenReturn(Component.literal("TestPlayer"));
}
```

### Testing Without Forge Context

Many manager classes are designed to be Forge-independent in their core logic. Extract business logic into pure Java methods and test those directly.

```java
// Good: pure logic testable without Forge
public double calculateLoanPayment(double principal, double rate, int days) {
    return (principal * (1 + rate)) / days;
}

// Bad: Forge-dependent code mixed into business logic
public void processLoan(ServerPlayer player) {
    double payment = (getLoanAmount(player) * 1.1) / 14; // can't mock this easily
    EconomyManager.getInstance().withdraw(player.getUUID(), payment); // static call
}
```

---

## CI/CD Pipeline

Tests run automatically on every push via GitHub Actions.

**File:** `.github/workflows/ci.yml`

### Pipeline Steps

```yaml
steps:
  - name: Checkout
    uses: actions/checkout@v4

  - name: Setup Java 17
    uses: actions/setup-java@v3
    with:
      java-version: '17'
      distribution: 'temurin'

  - name: Cache Gradle
    uses: actions/cache@v3

  - name: Run Tests
    run: ./gradlew test

  - name: Generate Coverage Report
    run: ./gradlew jacocoTestReport

  - name: Check Coverage Thresholds
    run: ./gradlew jacocoTestCoverageVerification

  - name: Run Static Analysis
    run: ./gradlew spotbugsMain pmdMain

  - name: Upload Coverage Report
    uses: actions/upload-artifact@v3
    with:
      name: coverage-report
      path: build/reports/jacoco/
```

### Build Status

| Check | Threshold | Fails Build? |
|-------|-----------|-------------|
| Unit tests | All must pass | Yes |
| Coverage (overall) | ≥ 60% | Yes |
| Coverage (util) | ≥ 80% | Yes |
| SpotBugs | MEDIUM+ bugs | Yes |
| PMD | Violations | Warning only |

---

## Static Analysis

### SpotBugs

SpotBugs runs on every CI build and locally via:

```bash
./gradlew spotbugsMain
```

**Report:** `build/reports/spotbugs/main.html`

Exclusion config: `spotbugs-exclude.xml` — excludes common false positives in Forge event handlers.

### PMD

PMD checks code style and common issues:

```bash
./gradlew pmdMain
```

**Report:** `build/reports/pmd/main.html`

Rules configured in: `pmd-ruleset.xml`

### Checkstyle

```bash
./gradlew checkstyleMain
```

### Repository Quality Guard (custom)

For lightweight preflight checks before full Gradle runs:

```bash
bash scripts/quality_guard.sh
```

This guard currently tracks:
- empty catch blocks in `src/main/java`
- `catch (... ignored)` bindings in `src/main/java` (regression baseline)
- comment-only catch handlers in `src/main/java` (regression baseline)
- `Thread.sleep(...)` usage in main sources
- direct `new Thread(...)` usage in main sources

Current baseline target is strict zero for all five indicators (see `scripts/quality_baseline.env`).

---

## Troubleshooting Test Failures

### Gradle fails with `Unsupported class file major version 69`

**Cause:** Gradle is being launched with a newer host JVM (e.g. Java 25), while this project/tooling path expects Java 17 runtime compatibility.

**Fix:**

1. Install Java 17 locally.
2. Run tests via the helper script:

```bash
bash scripts/run_gradle_tests.sh
```

3. If auto-detection fails, set one of the env vars explicitly:

```bash
JAVA17_HOME=/path/to/jdk17 bash scripts/run_gradle_tests.sh
```

### Tests fail with `ClassNotFoundException` for Minecraft classes

**Cause:** Test is trying to instantiate a Minecraft class without Forge context.

**Fix:** Mock the class instead of instantiating it directly.

```java
// Wrong
new ServerPlayer(...) // requires full Minecraft bootstrap

// Right
@Mock ServerPlayer player;
```

### Tests fail with `NullPointerException` in static manager

**Cause:** A static singleton (like `ScheduleMCAPI.getInstance()`) is not initialized in test context.

**Fix:** Use `@Mock` for the API and inject it, or use Mockito's static mocking:

```java
try (MockedStatic<ScheduleMCAPI> mocked = mockStatic(ScheduleMCAPI.class)) {
    ScheduleMCAPI mockAPI = mock(ScheduleMCAPI.class);
    mocked.when(ScheduleMCAPI::getInstance).thenReturn(mockAPI);

    // ... test code that uses ScheduleMCAPI.getInstance()
}
```

### Coverage verification fails

**Cause:** New code added without corresponding tests.

**Fix:** Add tests for the new code. Use `./gradlew jacocoTestReport` and open the HTML report to see which lines are uncovered.

### Mockito strict stubbing violations

**Cause:** `@ExtendWith(MockitoExtension.class)` uses strict stubbing by default. Any unused `when(...)` call fails.

**Fix:** Either remove the unused stub, or use `lenient()`:

```java
lenient().when(mock.method()).thenReturn(value);
```

### Forge event tests

**Cause:** Forge events require the event bus to be active.

**Fix:** Test event handlers by calling them directly rather than going through the event bus:

```java
// Direct invocation (preferred for unit tests)
handler.onPlayerJoin(mockEvent);

// rather than going through MinecraftForge.EVENT_BUS
```

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

