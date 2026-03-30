# Air-Gap Surveillance System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the two-module air-gap surveillance system (`alice:core:surveillance_api` + `alice:core:surveillance`) that continuously verifies Alice's device is fully air-gapped.

**Architecture:** Pure Kotlin domain types in `surveillance_api`, Android implementation in `surveillance`. Reactive layer (BroadcastReceiver + NetworkCallback in callbackFlows) catches state transitions instantly. Periodic layer (3 WorkManager tiers) re-verifies on schedule. All Android system calls hidden behind provider interfaces so checks are pure functions testable with JUnit 4.

**Tech Stack:** Kotlin 2.3, WorkManager, App Startup, Koin 4.2, BouncyCastle (StrongBox attestation), kotlinx-coroutines, JUnit 4, AssertK, Turbine, Robolectric

**Spec:** `docs/superpowers/specs/2026-03-30-air-gap-surveillance-design.md`

---

## File Structure

### surveillance_api module
```
alice/core/surveillance_api/
├── build.gradle.kts
└── src/
    ├── main/kotlin/org/alice/rabbit/hole/core/surveillance/api/
    │   ├── IAirGapSurveillance.kt
    │   ├── AirGapStatus.kt
    │   └── AirGapViolation.kt
    └── test/kotlin/org/alice/rabbit/hole/core/surveillance/api/
        ├── AirGapViolationTest.kt
        └── FakeAirGapSurveillance.kt
```

### surveillance module
```
alice/core/surveillance/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
└── src/
    ├── main/kotlin/org/alice/rabbit/hole/core/surveillance/
    │   ├── AirGapSurveillance.kt          (BroadcastReceiver + IAirGapSurveillance impl)
    │   ├── AirGapNetworkMonitor.kt        (dual NetworkCallback flows)
    │   ├── BroadcastData.kt               (pure data class for intent extraction)
    │   ├── IntentToViolationMapper.kt     (pure function: BroadcastData -> AirGapViolation?)
    │   ├── worker/
    │   │   ├── BaseWorker.kt
    │   │   ├── ScheduleUniqueName.kt
    │   │   ├── ISurveillanceWorkDefinition.kt
    │   │   ├── FastTierDefinition.kt
    │   │   ├── StandardTierDefinition.kt
    │   │   ├── SlowTierDefinition.kt
    │   │   ├── IWorkScheduler.kt
    │   │   ├── WorkScheduler.kt
    │   │   ├── IViolationHandler.kt
    │   │   ├── FastTierChecks.kt          (pure function: providers -> violations)
    │   │   ├── StandardTierChecks.kt      (pure function: providers -> violations)
    │   │   ├── FastTierWorker.kt
    │   │   ├── StandardTierWorker.kt
    │   │   └── SlowTierWorker.kt
    │   ├── provider/
    │   │   ├── ISettingsProvider.kt
    │   │   ├── IAdapterStateProvider.kt
    │   │   ├── INetworkStateProvider.kt
    │   │   ├── IBuildPropertyProvider.kt
    │   │   ├── SettingsProvider.kt         (Android impl)
    │   │   ├── AdapterStateProvider.kt     (Android impl)
    │   │   ├── NetworkStateProvider.kt     (Android impl)
    │   │   └── BuildPropertyProvider.kt    (Android impl)
    │   ├── integrity/
    │   │   └── DeviceIntegrityVerifier.kt
    │   ├── initializer/
    │   │   └── AirGapInitializer.kt
    │   └── injection/
    │       ├── SurveillanceWorkerFactory.kt
    │       └── SurveillanceModule.kt
    └── test/kotlin/org/alice/rabbit/hole/core/surveillance/
        ├── IntentToViolationMapperTest.kt
        ├── worker/
        │   ├── ScheduleUniqueNameTest.kt
        │   ├── SurveillanceWorkDefinitionTest.kt
        │   ├── WorkSchedulerTest.kt        (Robolectric)
        │   ├── FastTierChecksTest.kt
        │   ├── StandardTierChecksTest.kt
        │   ├── FastTierWorkerTest.kt       (Robolectric)
        │   ├── FakeSettingsProvider.kt
        │   ├── FakeAdapterStateProvider.kt
        │   ├── FakeNetworkStateProvider.kt
        │   └── RecordingViolationHandler.kt
        └── integrity/
            └── DeviceIntegrityVerifierTest.kt
```

---

## Task 1: Add WorkManager and App Startup to version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

**IMPORTANT:** Fetch latest stable versions from Maven Central before writing. The versions below are placeholders — verify them.

- [ ] **Step 1: Fetch current stable versions**

Run:
```bash
# Check Maven Central for latest stable WorkManager and App Startup
```
Expected: Find latest `androidx.work:work-runtime-ktx` and `androidx.startup:startup-runtime` stable versions.

- [ ] **Step 2: Add versions and libraries to catalog**

In `[versions]` section add:
```toml
workManager                 = "<fetched-version>"
appStartup                  = "<fetched-version>"
```

In `[libraries]` section add:
```toml
androidx-work-runtime-ktx           = { module = "androidx.work:work-runtime-ktx",           version.ref = "workManager" }
androidx-work-testing               = { module = "androidx.work:work-testing",                version.ref = "workManager" }
androidx-startup-runtime            = { module = "androidx.startup:startup-runtime",          version.ref = "appStartup" }
```

- [ ] **Step 3: Verify catalog parses**

Run: `./gradlew --no-daemon :alice:core:surveillance:dependencies`
Expected: No errors.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "Add WorkManager and App Startup to version catalog"
```

---

## Task 2: Create surveillance_api module

**Files:**
- Create: `alice/core/surveillance_api/build.gradle.kts`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Create build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.wonderland.android.library)
}

android {
    namespace = "org.alice.rabbit.hole.core.surveillance.api"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.assertk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

Uses `wonderland.android.library` per CLAUDE.md rule — modules in the alice subtree must use this even if pure Kotlin, to avoid pre-release bytecode breaking Android consumers.

- [ ] **Step 2: Register in settings.gradle.kts**

Add `include(":alice:core:surveillance_api")` immediately before `include(":alice:core:surveillance")`.

- [ ] **Step 3: Verify**

Run: `./gradlew --no-daemon :alice:core:surveillance_api:tasks`
Expected: Task list prints without errors.

- [ ] **Step 4: Commit**

```bash
git add alice/core/surveillance_api/build.gradle.kts settings.gradle.kts
git commit -m "Create alice:core:surveillance_api module"
```

---

## Task 3: Update surveillance module build.gradle.kts

**Files:**
- Modify: `alice/core/surveillance/build.gradle.kts`

- [ ] **Step 1: Replace build.gradle.kts content**

```kotlin
plugins {
    alias(libs.plugins.wonderland.android.library)
}

android {
    namespace = "org.alice.rabbit.hole.core.surveillance"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(projects.alice.core.surveillanceApi)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.bouncycastle.bcprov)

    testImplementation(libs.assertk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.koin.test)
}
```

`buildConfig = true` required for `BuildConfig.DEBUG` check in USB dev mode carve-out.

- [ ] **Step 2: Verify**

Run: `./gradlew --no-daemon :alice:core:surveillance:dependencies`
Expected: Dependency tree resolves.

- [ ] **Step 3: Commit**

```bash
git add alice/core/surveillance/build.gradle.kts
git commit -m "Configure surveillance module dependencies"
```

---

## Task 4: AirGapViolation — test then implement

**Files:**
- Create: `alice/core/surveillance_api/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/api/AirGapViolationTest.kt`
- Create: `alice/core/surveillance_api/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/api/AirGapViolation.kt`
- Create: `alice/core/surveillance_api/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/api/AirGapStatus.kt`

- [ ] **Step 1: Write failing test**

```kotlin
package org.alice.rabbit.hole.core.surveillance.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class AirGapViolationTest {

    @Test
    fun airplaneModeDisabledIsHardSeverity() {
        // Arrange
        val violation = AirGapViolation.AirplaneModeDisabled

        // Act
        val severity = violation.severity

        // Assert
        assertThat(severity).isEqualTo(AirGapViolation.Severity.HARD)
    }

    @Test
    fun usbPowerConnectedIsSoftSeverity() {
        // Arrange
        val violation = AirGapViolation.UsbPowerConnected

        // Act / Assert
        assertThat(violation.severity).isEqualTo(AirGapViolation.Severity.SOFT)
    }

    @Test
    fun deviceIntegrityFailedCarriesReason() {
        // Arrange / Act
        val violation = AirGapViolation.DeviceIntegrityFailed(reason = "manufacturer mismatch")

        // Assert
        assertThat(violation.reason).isEqualTo("manufacturer mismatch")
    }

    @Test
    fun attestationFailedCarriesFailedChecks() {
        // Arrange
        val checks = listOf("bootloader unlocked", "wrong boot key")

        // Act
        val violation = AirGapViolation.AttestationFailed(failedChecks = checks)

        // Assert
        assertThat(violation.failedChecks).isEqualTo(checks)
    }

    @Test
    fun allHardViolationsCountTwentyTwo() {
        // Arrange
        val hardViolations = listOf(
            AirGapViolation.AirplaneModeDisabled,
            AirGapViolation.BluetoothEnabled,
            AirGapViolation.BluetoothLowEnergyEnabled,
            AirGapViolation.NfcEnabled,
            AirGapViolation.SimPresent,
            AirGapViolation.WifiEnabled,
            AirGapViolation.WifiDirectEnabled,
            AirGapViolation.WifiAwareEnabled,
            AirGapViolation.NetworkInterfaceActive,
            AirGapViolation.VpnActive,
            AirGapViolation.TetheringActive,
            AirGapViolation.WifiBackgroundScanEnabled,
            AirGapViolation.BluetoothBackgroundScanEnabled,
            AirGapViolation.LocationEnabled,
            AirGapViolation.DeveloperOptionsEnabled,
            AirGapViolation.AdbEnabled,
            AirGapViolation.AdbWirelessEnabled,
            AirGapViolation.AccessibilityServiceActive,
            AirGapViolation.DisplayMirroringActive,
            AirGapViolation.OemUnlockEnabled,
            AirGapViolation.DeviceIntegrityFailed(reason = "test"),
            AirGapViolation.AttestationFailed(failedChecks = listOf("test")),
        )

        // Act / Assert
        assertThat(hardViolations.size).isEqualTo(22)
        hardViolations.forEach { violation ->
            assertThat(violation.severity).isEqualTo(AirGapViolation.Severity.HARD)
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :alice:core:surveillance_api:test`
Expected: FAIL — `AirGapViolation` does not exist.

- [ ] **Step 3: Implement AirGapViolation.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.api

sealed class AirGapViolation(val severity: Severity) {

    enum class Severity { HARD, SOFT }

    // Radio surfaces
    data object AirplaneModeDisabled : AirGapViolation(Severity.HARD)
    data object BluetoothEnabled : AirGapViolation(Severity.HARD)
    data object BluetoothLowEnergyEnabled : AirGapViolation(Severity.HARD)
    data object NfcEnabled : AirGapViolation(Severity.HARD)
    data object SimPresent : AirGapViolation(Severity.HARD)
    data object WifiEnabled : AirGapViolation(Severity.HARD)
    data object WifiDirectEnabled : AirGapViolation(Severity.HARD)
    data object WifiAwareEnabled : AirGapViolation(Severity.HARD)

    // Network surfaces
    data object NetworkInterfaceActive : AirGapViolation(Severity.HARD)
    data object VpnActive : AirGapViolation(Severity.HARD)
    data object TetheringActive : AirGapViolation(Severity.HARD)

    // Background scan surfaces
    data object WifiBackgroundScanEnabled : AirGapViolation(Severity.HARD)
    data object BluetoothBackgroundScanEnabled : AirGapViolation(Severity.HARD)

    // Location
    data object LocationEnabled : AirGapViolation(Severity.HARD)

    // Device security
    data object DeveloperOptionsEnabled : AirGapViolation(Severity.HARD)
    data object AdbEnabled : AirGapViolation(Severity.HARD)
    data object AdbWirelessEnabled : AirGapViolation(Severity.HARD)
    data object AccessibilityServiceActive : AirGapViolation(Severity.HARD)
    data object DisplayMirroringActive : AirGapViolation(Severity.HARD)
    data object OemUnlockEnabled : AirGapViolation(Severity.HARD)

    // Device integrity
    data class DeviceIntegrityFailed(val reason: String) : AirGapViolation(Severity.HARD)
    data class AttestationFailed(val failedChecks: List<String>) : AirGapViolation(Severity.HARD)

    // Soft
    data object UsbPowerConnected : AirGapViolation(Severity.SOFT)
}
```

- [ ] **Step 4: Implement AirGapStatus.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.api

sealed interface AirGapStatus {
    data object Secure : AirGapStatus
    data class Compromised(val violation: AirGapViolation) : AirGapStatus
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :alice:core:surveillance_api:test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add alice/core/surveillance_api/
git commit -m "Add AirGapViolation sealed class (23 types) and AirGapStatus"
```

---

## Task 5: IAirGapSurveillance + FakeAirGapSurveillance — test then implement

**Files:**
- Create: `alice/core/surveillance_api/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/api/FakeAirGapSurveillanceTest.kt`
- Create: `alice/core/surveillance_api/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/api/IAirGapSurveillance.kt`
- Create: `alice/core/surveillance_api/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/api/FakeAirGapSurveillance.kt`

- [ ] **Step 1: Write failing test**

```kotlin
package org.alice.rabbit.hole.core.surveillance.api

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeAirGapSurveillanceTest {

    @Test
    fun initialStatusIsSecure() = runTest {
        // Arrange
        val surveillance = FakeAirGapSurveillance()

        // Act / Assert
        assertThat(surveillance.status.value).isEqualTo(AirGapStatus.Secure)
    }

    @Test
    fun emitViolationUpdatesStatusToCompromised() = runTest {
        // Arrange
        val surveillance = FakeAirGapSurveillance()

        // Act
        surveillance.emitViolation(AirGapViolation.WifiEnabled)

        // Assert
        assertThat(surveillance.status.value).isInstanceOf<AirGapStatus.Compromised>()
        val compromised = surveillance.status.value as AirGapStatus.Compromised
        assertThat(compromised.violation).isEqualTo(AirGapViolation.WifiEnabled)
    }

    @Test
    fun violationsFlowEmitsViolation() = runTest {
        // Arrange
        val surveillance = FakeAirGapSurveillance()

        surveillance.violations.test {
            // Act
            surveillance.emitViolation(AirGapViolation.BluetoothEnabled)

            // Assert
            assertThat(awaitItem()).isEqualTo(AirGapViolation.BluetoothEnabled)
        }
    }

    @Test
    fun resetRestoresSecureStatus() = runTest {
        // Arrange
        val surveillance = FakeAirGapSurveillance()
        surveillance.emitViolation(AirGapViolation.NfcEnabled)

        // Act
        surveillance.reset()

        // Assert
        assertThat(surveillance.status.value).isEqualTo(AirGapStatus.Secure)
    }

    @Test
    fun statusReflectsLatestViolation() = runTest {
        // Arrange
        val surveillance = FakeAirGapSurveillance()

        // Act
        surveillance.emitViolation(AirGapViolation.SimPresent)
        surveillance.emitViolation(AirGapViolation.VpnActive)

        // Assert
        val compromised = surveillance.status.value as AirGapStatus.Compromised
        assertThat(compromised.violation).isEqualTo(AirGapViolation.VpnActive)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :alice:core:surveillance_api:test`
Expected: FAIL — `IAirGapSurveillance` and `FakeAirGapSurveillance` do not exist.

- [ ] **Step 3: Implement IAirGapSurveillance.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IAirGapSurveillance {
    val status: StateFlow<AirGapStatus>
    val violations: Flow<AirGapViolation>
}
```

- [ ] **Step 4: Implement FakeAirGapSurveillance.kt** (in test source set)

```kotlin
package org.alice.rabbit.hole.core.surveillance.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAirGapSurveillance : IAirGapSurveillance {

    private val mutableStatus: MutableStateFlow<AirGapStatus> = MutableStateFlow(AirGapStatus.Secure)
    override val status: StateFlow<AirGapStatus> = mutableStatus.asStateFlow()

    private val mutableViolations: MutableSharedFlow<AirGapViolation> = MutableSharedFlow()
    override val violations: Flow<AirGapViolation> = mutableViolations.asSharedFlow()

    suspend fun emitViolation(violation: AirGapViolation) {
        mutableStatus.value = AirGapStatus.Compromised(violation)
        mutableViolations.emit(violation)
    }

    fun reset() {
        mutableStatus.value = AirGapStatus.Secure
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :alice:core:surveillance_api:test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add alice/core/surveillance_api/
git commit -m "Add IAirGapSurveillance interface and FakeAirGapSurveillance test double"
```

---

## Task 6: Worker infrastructure — ScheduleUniqueName + work definitions

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/ScheduleUniqueName.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/ISurveillanceWorkDefinition.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/FastTierDefinition.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/StandardTierDefinition.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/SlowTierDefinition.kt`
- Create: `alice/core/surveillance/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/worker/ScheduleUniqueNameTest.kt`
- Create: `alice/core/surveillance/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/worker/SurveillanceWorkDefinitionTest.kt`

- [ ] **Step 1: Write ScheduleUniqueName test**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.Test

class ScheduleUniqueNameTest {

    @Test
    fun valuePreservesSuppliedString() {
        val name = ScheduleUniqueName("surveillance_fast")
        assertThat(name.value).isEqualTo("surveillance_fast")
    }

    @Test
    fun equalityBasedOnValue() {
        assertThat(ScheduleUniqueName("surveillance_fast")).isEqualTo(ScheduleUniqueName("surveillance_fast"))
    }

    @Test
    fun inequalityForDifferentValues() {
        assertThat(ScheduleUniqueName("surveillance_fast")).isNotEqualTo(ScheduleUniqueName("surveillance_slow"))
    }
}
```

- [ ] **Step 2: Write work definition test**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.isEqualTo
import androidx.work.ExistingPeriodicWorkPolicy
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class SurveillanceWorkDefinitionTest {

    @Test
    fun fastTierHasFifteenMinuteInterval() {
        assertThat(FastTierDefinition.repeatInterval).isEqualTo(15.minutes)
    }

    @Test
    fun fastTierHasCorrectUniqueName() {
        assertThat(FastTierDefinition.uniqueName).isEqualTo(ScheduleUniqueName("surveillance_fast"))
    }

    @Test
    fun standardTierHasOneHourInterval() {
        assertThat(StandardTierDefinition.repeatInterval).isEqualTo(1.hours)
    }

    @Test
    fun slowTierHasSixHourInterval() {
        assertThat(SlowTierDefinition.repeatInterval).isEqualTo(6.hours)
    }

    @Test
    fun allDefinitionsUseKeepPolicy() {
        listOf(FastTierDefinition, StandardTierDefinition, SlowTierDefinition).forEach { definition ->
            assertThat(definition.existingWorkPolicy).isEqualTo(ExistingPeriodicWorkPolicy.KEEP)
        }
    }
}
```

- [ ] **Step 3: Run tests — expect FAIL**

Run: `./gradlew :alice:core:surveillance:testDebugUnitTest`
Expected: FAIL — classes do not exist.

- [ ] **Step 4: Implement ScheduleUniqueName.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

@JvmInline
value class ScheduleUniqueName(val value: String)
```

- [ ] **Step 5: Implement ISurveillanceWorkDefinition.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import androidx.work.ExistingPeriodicWorkPolicy
import kotlin.time.Duration

interface ISurveillanceWorkDefinition {
    val uniqueName: ScheduleUniqueName
    val repeatInterval: Duration
    val existingWorkPolicy: ExistingPeriodicWorkPolicy
        get() = ExistingPeriodicWorkPolicy.KEEP
}
```

- [ ] **Step 6: Implement FastTierDefinition.kt, StandardTierDefinition.kt, SlowTierDefinition.kt**

```kotlin
// FastTierDefinition.kt
package org.alice.rabbit.hole.core.surveillance.worker

import kotlin.time.Duration.Companion.minutes

data object FastTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_fast")
    override val repeatInterval = 15.minutes
}
```

```kotlin
// StandardTierDefinition.kt
package org.alice.rabbit.hole.core.surveillance.worker

import kotlin.time.Duration.Companion.hours

data object StandardTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_standard")
    override val repeatInterval = 1.hours
}
```

```kotlin
// SlowTierDefinition.kt
package org.alice.rabbit.hole.core.surveillance.worker

import kotlin.time.Duration.Companion.hours

data object SlowTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_slow")
    override val repeatInterval = 6.hours
}
```

- [ ] **Step 7: Run tests — expect PASS**

Run: `./gradlew :alice:core:surveillance:testDebugUnitTest --tests "*.ScheduleUniqueNameTest" --tests "*.SurveillanceWorkDefinitionTest"`
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add worker infrastructure: ScheduleUniqueName, ISurveillanceWorkDefinition, 3 tier definitions"
```

---

## Task 7: BaseWorker + IWorkScheduler + WorkScheduler

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/BaseWorker.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/IWorkScheduler.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/WorkScheduler.kt`
- Create: `alice/core/surveillance/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/worker/WorkSchedulerTest.kt`

- [ ] **Step 1: Write WorkScheduler Robolectric test**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WorkSchedulerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    @Test
    fun scheduleEnqueuesPeriodicWork() {
        // Arrange
        val workManager = WorkManager.getInstance(context)
        val scheduler: IWorkScheduler = WorkScheduler(workManager)

        // Act
        scheduler.schedule(FastTierDefinition, FastTierWorker::class.java)

        // Assert
        val workInfos = workManager.getWorkInfosForUniqueWork(FastTierDefinition.uniqueName.value).get()
        assertThat(workInfos).hasSize(1)
        assertThat(workInfos.first().state).isEqualTo(WorkInfo.State.ENQUEUED)
    }

    @Test
    fun scheduleWithKeepPolicyDoesNotDuplicate() {
        // Arrange
        val workManager = WorkManager.getInstance(context)
        val scheduler: IWorkScheduler = WorkScheduler(workManager)

        // Act
        scheduler.schedule(FastTierDefinition, FastTierWorker::class.java)
        scheduler.schedule(FastTierDefinition, FastTierWorker::class.java)

        // Assert
        val workInfos = workManager.getWorkInfosForUniqueWork(FastTierDefinition.uniqueName.value).get()
        assertThat(workInfos).hasSize(1)
    }
}
```

Note: This test references `FastTierWorker` which doesn't exist yet. Create a minimal stub or run this test after Task 9. The test is included here because the scheduler and workers are tightly coupled in the test setup.

- [ ] **Step 2: Implement BaseWorker.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

abstract class BaseWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    abstract suspend fun doActualWork(): Result

    override suspend fun doWork(): Result {
        return if (isStopped) {
            Result.retry()
        } else {
            doActualWork()
        }
    }
}
```

- [ ] **Step 3: Implement IWorkScheduler.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import androidx.work.ListenableWorker

interface IWorkScheduler {
    fun schedule(definition: ISurveillanceWorkDefinition, workerClass: Class<out ListenableWorker>)
}
```

- [ ] **Step 4: Implement WorkScheduler.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WorkScheduler(private val workManager: WorkManager) : IWorkScheduler {

    override fun schedule(definition: ISurveillanceWorkDefinition, workerClass: Class<out ListenableWorker>) {
        val request = PeriodicWorkRequest.Builder(
            workerClass,
            definition.repeatInterval.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
        ).build()
        workManager.enqueueUniquePeriodicWork(
            definition.uniqueName.value,
            definition.existingWorkPolicy,
            request,
        )
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add BaseWorker, IWorkScheduler, and WorkScheduler"
```

---

## Task 8: Provider interfaces + fake test doubles

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/ISettingsProvider.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/IAdapterStateProvider.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/INetworkStateProvider.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/IBuildPropertyProvider.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/IViolationHandler.kt`
- Create test fakes: `FakeSettingsProvider.kt`, `FakeAdapterStateProvider.kt`, `FakeNetworkStateProvider.kt`, `RecordingViolationHandler.kt`

- [ ] **Step 1: Create provider interfaces**

```kotlin
// ISettingsProvider.kt
package org.alice.rabbit.hole.core.surveillance.provider

interface ISettingsProvider {
    fun readGlobalInt(name: String, defaultValue: Int = 0): Int
}
```

```kotlin
// IAdapterStateProvider.kt
package org.alice.rabbit.hole.core.surveillance.provider

interface IAdapterStateProvider {
    fun isBluetoothEnabled(): Boolean
    fun isBluetoothLowEnergyEnabled(): Boolean
    fun isNfcEnabled(): Boolean
    fun isWifiBackgroundScanEnabled(): Boolean
    fun isWifiAwareAvailable(): Boolean
    fun isLocationEnabled(): Boolean
    fun isAccessibilityEnabled(): Boolean
    fun simState(): Int
}
```

```kotlin
// INetworkStateProvider.kt
package org.alice.rabbit.hole.core.surveillance.provider

interface INetworkStateProvider {
    fun hasActiveNetwork(): Boolean
    fun hasVpnNetwork(): Boolean
    fun hasTetheredInterfaces(): Boolean
    fun displayCount(): Int
    fun usbDeviceCount(): Int
    fun isOemUnlockEnabled(): Boolean
}
```

```kotlin
// IBuildPropertyProvider.kt
package org.alice.rabbit.hole.core.surveillance.provider

interface IBuildPropertyProvider {
    fun manufacturer(): String
    fun brand(): String
    fun device(): String
    fun sdkVersion(): Int
}
```

```kotlin
// IViolationHandler.kt
package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation

interface IViolationHandler {
    suspend fun handle(violation: AirGapViolation)
}
```

- [ ] **Step 2: Create test fakes**

```kotlin
// FakeSettingsProvider.kt
package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider

class FakeSettingsProvider(
    private val globalInts: Map<String, Int> = emptyMap(),
) : ISettingsProvider {
    override fun readGlobalInt(name: String, defaultValue: Int): Int =
        globalInts.getOrDefault(name, defaultValue)
}
```

```kotlin
// FakeAdapterStateProvider.kt
package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider

class FakeAdapterStateProvider(
    private val bluetoothEnabled: Boolean = false,
    private val bluetoothLowEnergyEnabled: Boolean = false,
    private val nfcEnabled: Boolean = false,
    private val wifiBackgroundScanEnabled: Boolean = false,
    private val wifiAwareAvailable: Boolean = false,
    private val locationEnabled: Boolean = false,
    private val accessibilityEnabled: Boolean = false,
    private val simState: Int = 1,
) : IAdapterStateProvider {
    override fun isBluetoothEnabled(): Boolean = bluetoothEnabled
    override fun isBluetoothLowEnergyEnabled(): Boolean = bluetoothLowEnergyEnabled
    override fun isNfcEnabled(): Boolean = nfcEnabled
    override fun isWifiBackgroundScanEnabled(): Boolean = wifiBackgroundScanEnabled
    override fun isWifiAwareAvailable(): Boolean = wifiAwareAvailable
    override fun isLocationEnabled(): Boolean = locationEnabled
    override fun isAccessibilityEnabled(): Boolean = accessibilityEnabled
    override fun simState(): Int = simState
}
```

```kotlin
// FakeNetworkStateProvider.kt
package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider

class FakeNetworkStateProvider(
    private val activeNetwork: Boolean = false,
    private val vpnNetwork: Boolean = false,
    private val tetheredInterfaces: Boolean = false,
    private val displayCount: Int = 1,
    private val usbDeviceCount: Int = 0,
    private val oemUnlockEnabled: Boolean = false,
) : INetworkStateProvider {
    override fun hasActiveNetwork(): Boolean = activeNetwork
    override fun hasVpnNetwork(): Boolean = vpnNetwork
    override fun hasTetheredInterfaces(): Boolean = tetheredInterfaces
    override fun displayCount(): Int = displayCount
    override fun usbDeviceCount(): Int = usbDeviceCount
    override fun isOemUnlockEnabled(): Boolean = oemUnlockEnabled
}
```

```kotlin
// RecordingViolationHandler.kt
package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation

class RecordingViolationHandler : IViolationHandler {
    val violations: MutableList<AirGapViolation> = mutableListOf()
    override suspend fun handle(violation: AirGapViolation) {
        violations.add(violation)
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add provider interfaces, IViolationHandler, and test fakes"
```

---

## Task 9: FastTierChecks — test then implement

**Files:**
- Create: `alice/core/surveillance/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/worker/FastTierChecksTest.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/FastTierChecks.kt`

- [ ] **Step 1: Write failing test**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.junit.Test

class FastTierChecksTest {

    private val secureSettings = mapOf(
        "airplane_mode_on" to 1, "wifi_on" to 0, "bluetooth_on" to 0,
        "nfc_on" to 0, "ble_scan_always_enabled" to 0, "adb_enabled" to 0,
        "adb_wifi_enabled" to 0, "development_settings_enabled" to 0,
    )

    @Test
    fun secureDeviceProducesNoViolations() {
        val violations = FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider())
        assertThat(violations).isEmpty()
    }

    @Test
    fun airplaneModeOffProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("airplane_mode_on" to 0))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.AirplaneModeDisabled)
    }

    @Test
    fun wifiOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("wifi_on" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.WifiEnabled)
    }

    @Test
    fun bluetoothOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("bluetooth_on" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.BluetoothEnabled)
    }

    @Test
    fun nfcOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("nfc_on" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.NfcEnabled)
    }

    @Test
    fun bleBackgroundScanProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("ble_scan_always_enabled" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.BluetoothBackgroundScanEnabled)
    }

    @Test
    fun adbEnabledProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("adb_enabled" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.AdbEnabled)
    }

    @Test
    fun developerOptionsProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("development_settings_enabled" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.DeveloperOptionsEnabled)
    }

    @Test
    fun locationEnabledViaAdapterProducesViolation() {
        val violations = FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider(locationEnabled = true))
        assertThat(violations).contains(AirGapViolation.LocationEnabled)
    }

    @Test
    fun simReadyProducesViolation() {
        val violations = FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider(simState = 5))
        assertThat(violations).contains(AirGapViolation.SimPresent)
    }

    @Test
    fun simAbsentProducesNoViolation() {
        val violations = FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider(simState = 1))
        assertThat(violations.filterIsInstance<AirGapViolation.SimPresent>()).isEmpty()
    }

    @Test
    fun multipleViolationsReturnedTogether() {
        val settings = FakeSettingsProvider(secureSettings + ("airplane_mode_on" to 0) + ("wifi_on" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.AirplaneModeDisabled)
        assertThat(violations).contains(AirGapViolation.WifiEnabled)
    }
}
```

- [ ] **Step 2: Run — expect FAIL**

- [ ] **Step 3: Implement FastTierChecks.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider

object FastTierChecks {

    private const val SIM_STATE_ABSENT = 1

    fun execute(settingsProvider: ISettingsProvider, adapterStateProvider: IAdapterStateProvider): List<AirGapViolation> {
        val violations = mutableListOf<AirGapViolation>()

        if (settingsProvider.readGlobalInt("airplane_mode_on") == 0) violations.add(AirGapViolation.AirplaneModeDisabled)
        if (settingsProvider.readGlobalInt("wifi_on") == 1) violations.add(AirGapViolation.WifiEnabled)
        if (settingsProvider.readGlobalInt("bluetooth_on") == 1) violations.add(AirGapViolation.BluetoothEnabled)
        if (settingsProvider.readGlobalInt("nfc_on") == 1) violations.add(AirGapViolation.NfcEnabled)
        if (settingsProvider.readGlobalInt("ble_scan_always_enabled") == 1) violations.add(AirGapViolation.BluetoothBackgroundScanEnabled)
        if (settingsProvider.readGlobalInt("adb_enabled") == 1) violations.add(AirGapViolation.AdbEnabled)
        if (settingsProvider.readGlobalInt("adb_wifi_enabled") == 1) violations.add(AirGapViolation.AdbWirelessEnabled)
        if (settingsProvider.readGlobalInt("development_settings_enabled") == 1) violations.add(AirGapViolation.DeveloperOptionsEnabled)

        if (adapterStateProvider.isBluetoothLowEnergyEnabled()) violations.add(AirGapViolation.BluetoothLowEnergyEnabled)
        if (adapterStateProvider.isNfcEnabled()) violations.add(AirGapViolation.NfcEnabled)
        if (adapterStateProvider.isWifiBackgroundScanEnabled()) violations.add(AirGapViolation.WifiBackgroundScanEnabled)
        if (adapterStateProvider.isWifiAwareAvailable()) violations.add(AirGapViolation.WifiAwareEnabled)
        if (adapterStateProvider.isLocationEnabled()) violations.add(AirGapViolation.LocationEnabled)
        if (adapterStateProvider.isAccessibilityEnabled()) violations.add(AirGapViolation.AccessibilityServiceActive)
        if (adapterStateProvider.simState() != SIM_STATE_ABSENT) violations.add(AirGapViolation.SimPresent)

        return violations
    }
}
```

- [ ] **Step 4: Run — expect PASS**

- [ ] **Step 5: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add FastTierChecks pure function with exhaustive tests"
```

---

## Task 10: StandardTierChecks — test then implement

**Files:**
- Create: `alice/core/surveillance/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/worker/StandardTierChecksTest.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/StandardTierChecks.kt`

- [ ] **Step 1: Write failing test**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.junit.Test

class StandardTierChecksTest {

    @Test
    fun secureDeviceProducesNoViolations() {
        val violations = StandardTierChecks.execute(FakeNetworkStateProvider())
        assertThat(violations).isEmpty()
    }

    @Test
    fun activeNetworkProducesViolation() {
        val violations = StandardTierChecks.execute(FakeNetworkStateProvider(activeNetwork = true))
        assertThat(violations).contains(AirGapViolation.NetworkInterfaceActive)
    }

    @Test
    fun vpnProducesViolation() {
        val violations = StandardTierChecks.execute(FakeNetworkStateProvider(vpnNetwork = true))
        assertThat(violations).contains(AirGapViolation.VpnActive)
    }

    @Test
    fun tetheringProducesViolation() {
        val violations = StandardTierChecks.execute(FakeNetworkStateProvider(tetheredInterfaces = true))
        assertThat(violations).contains(AirGapViolation.TetheringActive)
    }

    @Test
    fun multipleDisplaysProducesViolation() {
        val violations = StandardTierChecks.execute(FakeNetworkStateProvider(displayCount = 2))
        assertThat(violations).contains(AirGapViolation.DisplayMirroringActive)
    }

    @Test
    fun singleDisplayProducesNoViolation() {
        val violations = StandardTierChecks.execute(FakeNetworkStateProvider(displayCount = 1))
        assertThat(violations.filterIsInstance<AirGapViolation.DisplayMirroringActive>()).isEmpty()
    }

    @Test
    fun oemUnlockProducesViolation() {
        val violations = StandardTierChecks.execute(FakeNetworkStateProvider(oemUnlockEnabled = true))
        assertThat(violations).contains(AirGapViolation.OemUnlockEnabled)
    }
}
```

- [ ] **Step 2: Implement StandardTierChecks.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider

object StandardTierChecks {

    private const val SINGLE_DISPLAY_COUNT = 1

    fun execute(networkStateProvider: INetworkStateProvider): List<AirGapViolation> {
        val violations = mutableListOf<AirGapViolation>()

        if (networkStateProvider.hasActiveNetwork()) violations.add(AirGapViolation.NetworkInterfaceActive)
        if (networkStateProvider.hasVpnNetwork()) violations.add(AirGapViolation.VpnActive)
        if (networkStateProvider.hasTetheredInterfaces()) violations.add(AirGapViolation.TetheringActive)
        if (networkStateProvider.displayCount() > SINGLE_DISPLAY_COUNT) violations.add(AirGapViolation.DisplayMirroringActive)
        if (networkStateProvider.isOemUnlockEnabled()) violations.add(AirGapViolation.OemUnlockEnabled)

        return violations
    }
}
```

- [ ] **Step 3: Run — expect PASS**

- [ ] **Step 4: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add StandardTierChecks pure function with tests"
```

---

## Task 11: IntentToViolationMapper — test then implement

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/BroadcastData.kt`
- Create: `alice/core/surveillance/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/IntentToViolationMapperTest.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/IntentToViolationMapper.kt`

This is the critical pure function mapping broadcast data to violations. No Android types — fully testable with JUnit 4.

- [ ] **Step 1: Create BroadcastData.kt**

```kotlin
package org.alice.rabbit.hole.core.surveillance

data class BroadcastData(
    val action: String?,
    val booleanExtras: Map<String, Boolean> = emptyMap(),
    val intExtras: Map<String, Int> = emptyMap(),
    val stringExtras: Map<String, String?> = emptyMap(),
    val stringArrayExtras: Map<String, Array<String>> = emptyMap(),
)
```

- [ ] **Step 2: Write exhaustive test**

Test every action string, every state value, every null/unknown case. See spec section 4.1 for complete signal reference table. Full test code in plan agent output — covers: airplane mode (on/off), Bluetooth (STATE_ON/TURNING_ON/BLE_ON/BLE_TURNING_ON/OFF), NFC (ON/TURNING_ON/OFF), SIM (READY/LOADED/ABSENT/null), Wi-Fi (ENABLED/ENABLING/DISABLED), Wi-Fi Direct (ENABLED/DISABLED), tethering (active/empty), USB (connected/disconnected), null action, unknown action.

- [ ] **Step 3: Implement IntentToViolationMapper.kt**

Pure `when` expression over action strings. Constants for all state values defined as private companion members. See plan agent output for complete implementation.

- [ ] **Step 4: Run — expect PASS**

- [ ] **Step 5: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add IntentToViolationMapper pure function with exhaustive tests"
```

---

## Task 12: DeviceIntegrityVerifier — test then implement

**Files:**
- Create: `alice/core/surveillance/src/test/kotlin/org/alice/rabbit/hole/core/surveillance/integrity/DeviceIntegrityVerifierTest.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/integrity/DeviceIntegrityVerifier.kt`

- [ ] **Step 1: Write failing test**

Tests: valid Pixel passes, wrong manufacturer fails, wrong brand fails, unknown codename fails, SDK < 33 fails, known codenames (raven, caiman, etc.) pass.

- [ ] **Step 2: Implement DeviceIntegrityVerifier.kt**

`verifyBuildProperties(provider: IBuildPropertyProvider): AirGapViolation?` — checks manufacturer == "Google", brand == "google", device in known Pixel codenames set, SDK >= 33. Returns `DeviceIntegrityFailed(reason)` or null.

Known Pixel codenames: oriole, raven, bluejay, panther, cheetah, lynx, tangorpro, felix, shiba, husky, akita, tokay, caiman, komodo, comet.

Source: https://grapheneos.org/articles/attestation-compatibility-guide

- [ ] **Step 3: Run — expect PASS**

- [ ] **Step 4: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add DeviceIntegrityVerifier build property checks with known Pixel codenames"
```

---

## Task 13: Android provider implementations

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/SettingsProvider.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/AdapterStateProvider.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/NetworkStateProvider.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/provider/BuildPropertyProvider.kt`

Thin wrappers around Android system calls. Not unit tested — they delegate to the OS. Verified via integration on the real device.

- [ ] **Step 1: Implement all four providers**

See plan agent output for complete implementations. Key points:
- `SettingsProvider` wraps `Settings.Global.getInt(contentResolver, ...)`
- `AdapterStateProvider` queries `BluetoothManager`, `NfcAdapter`, `WifiManager`, `WifiAwareManager`, `LocationManager`, `AccessibilityManager`, `TelephonyManager`
- `NetworkStateProvider` queries `ConnectivityManager`, `DisplayManager`, `UsbManager`, `Settings.Global`
- `BuildPropertyProvider` reads `Build.MANUFACTURER`, `Build.BRAND`, `Build.DEVICE`, `Build.VERSION.SDK_INT`

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :alice:core:surveillance:compileDebugKotlin`
Expected: Compiles.

- [ ] **Step 3: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add Android provider implementations for settings, adapters, network, and build properties"
```

---

## Task 14: Worker implementations (FastTier, StandardTier, SlowTier)

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/FastTierWorker.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/StandardTierWorker.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/worker/SlowTierWorker.kt`

Each worker is a thin wrapper: calls the corresponding `*Checks.execute()` pure function, then delegates violations to `IViolationHandler`.

- [ ] **Step 1: Implement all three workers**

See plan agent output for complete code. Each takes its providers + `IViolationHandler` via constructor.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :alice:core:surveillance:compileDebugKotlin`

- [ ] **Step 3: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add FastTierWorker, StandardTierWorker, SlowTierWorker"
```

---

## Task 15: AirGapSurveillance BroadcastReceiver + AirGapNetworkMonitor

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/AirGapSurveillance.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/AirGapNetworkMonitor.kt`

- [ ] **Step 1: Implement AirGapSurveillance.kt**

Implements `IAirGapSurveillance`. Contains:
- `broadcastFlow(): Flow<AirGapViolation>` — `callbackFlow` registering consolidated `BroadcastReceiver`
- `startCollecting()` — launches collection at `applicationScope`
- `setCompromised(violation)` — for synchronous initial check from `AirGapInitializer`
- Debug build USB carve-out: `if (isDebugBuild && violation == AirGapViolation.UsbPowerConnected) return`

See plan agent output for complete implementation.

- [ ] **Step 2: Implement AirGapNetworkMonitor.kt**

Two `callbackFlow` functions:
- `allNetworkFlow()` — `NetworkRequest.Builder().build()` — any `onAvailable` emits `NetworkInterfaceActive`
- `vpnNetworkFlow()` — `addTransportType(TRANSPORT_VPN).removeCapability(NET_CAPABILITY_NOT_VPN)` — `onAvailable` emits `VpnActive`

- [ ] **Step 3: Verify compilation**

- [ ] **Step 4: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add AirGapSurveillance BroadcastReceiver and AirGapNetworkMonitor with callbackFlows"
```

---

## Task 16: Koin wiring + WorkerFactory

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/injection/SurveillanceWorkerFactory.kt`
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/injection/SurveillanceModule.kt`

- [ ] **Step 1: Implement SurveillanceWorkerFactory.kt**

Custom `WorkerFactory` that creates workers with injected dependencies. Maps `workerClassName` to constructor calls.

- [ ] **Step 2: Implement SurveillanceModule.kt**

Koin `module { }` providing all singletons: providers, monitors, `IAirGapSurveillance`, `IWorkScheduler`, `SurveillanceWorkerFactory`.

- [ ] **Step 3: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add Koin SurveillanceModule and SurveillanceWorkerFactory"
```

---

## Task 17: AirGapInitializer (App Startup)

**Files:**
- Create: `alice/core/surveillance/src/main/kotlin/org/alice/rabbit/hole/core/surveillance/initializer/AirGapInitializer.kt`

- [ ] **Step 1: Implement AirGapInitializer.kt**

`Initializer<IAirGapSurveillance>` that:
1. Runs `FastTierChecks.execute()` synchronously — gates UI
2. Enqueues 3 `PeriodicWorkRequests` via `IWorkScheduler`
3. Calls `surveillance.startCollecting()` for reactive layer
4. Returns `IAirGapSurveillance` instance

`dependencies()` returns `listOf(WorkManagerInitializer::class.java)`.

- [ ] **Step 2: Commit**

```bash
git add alice/core/surveillance/
git commit -m "Add AirGapInitializer with synchronous fast-tier gate"
```

---

## Task 18: AndroidManifest permissions + App Startup provider

**Files:**
- Create/Modify: `alice/core/surveillance/src/main/AndroidManifest.xml`

- [ ] **Step 1: Write manifest**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_BASIC_PHONE_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <application>
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="org.alice.rabbit.hole.core.surveillance.initializer.AirGapInitializer"
                android:value="androidx.startup" />
        </provider>
    </application>
</manifest>
```

- [ ] **Step 2: Commit**

```bash
git add alice/core/surveillance/src/main/AndroidManifest.xml
git commit -m "Add surveillance permissions and App Startup provider to manifest"
```

---

## Task 19: Final verification build

- [ ] **Step 1: Run all surveillance tests**

Run: `./gradlew :alice:core:surveillance_api:test :alice:core:surveillance:testDebugUnitTest`
Expected: All tests pass.

- [ ] **Step 2: Run full project build**

Run: `./gradlew :alice:app:assembleDebug`
Expected: Builds successfully.

- [ ] **Step 3: Verify merged manifest**

Check `alice/app/build/intermediates/merged_manifest/debug/AndroidManifest.xml` — confirm surveillance permissions appear and no unexpected permissions were added by dependencies.

---

## Verification

After all tasks:
1. All pure JUnit 4 tests pass for domain types, FastTierChecks, StandardTierChecks, IntentToViolationMapper, DeviceIntegrityVerifier
2. Robolectric tests pass for WorkScheduler
3. Full project compiles (`assembleDebug`)
4. Merged manifest contains exactly the 4 surveillance permissions and no other prohibited permissions
5. Manual testing on GrapheneOS Pixel device for: hardware attestation, physical radio toggles, USB behaviour in debug vs release
