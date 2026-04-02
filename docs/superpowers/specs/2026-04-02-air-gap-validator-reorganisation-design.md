# Air-Gap Validator Reorganisation Design

## Context

POC 0000 proves Alice's air-gap surveillance system. The complete spec (`alice-paranoid-air-gap-complete-spec.md`) defines 65+ attack surfaces across 4 validators and 13 defence layers. The current implementation has 23 checks in a single 443-line `SensorChecker` object. This design reorganises the detection layer to accommodate all spec surfaces without becoming overwhelming, and updates the UI to a tabbed layout matching the 4 validator groups.

This is a POC — prove what's verifiable, high quality, not production polish.

## Domain Model

Replace `Either<String, String>` with value classes:

```kotlin
@JvmInline
value class ViolationDetail(val message: String)

@JvmInline
value class SafeDetail(val message: String)

data class CheckResult(
    val surface: SurfaceName,
    val outcome: Either<ViolationDetail, SafeDetail>,
)
```

Rename `SensorName` → `SurfaceName`. Enum grows from 23 to ~55 entries (pure Kotlin surfaces) plus stubs. Each entry keeps `displayLabel` and `ViolationSeverity`.

`ViolationSeverity` stays HARD/SOFT for the POC.

Rename `SensorStatus` → `SurfaceStatus`, using `Either<ViolationDetail, SafeDetail>`.

## Package Structure

```
detection/
  AirGapBroadcastReceiver.kt         — shared, serves all validators
  airgap/
    AirGapStateValidator.kt           — orchestrator
    RadioChecks.kt                    — airplane, wifi, bluetooth, BLE, NFC, UWB, satellite, thread, SIM, eSIM, network interfaces
    LocationChecks.kt                 — master toggle, SUPL (stub), PSDS (stub)
    UsbChecks.kt                      — USB data (stub), USB power
    SoftwareServiceChecks.kt          — updater, private DNS, master sync, hotspot, print services, emergency SOS, emergency alerts
    AccountChecks.kt                  — accounts, autofill, auto sync
    LockScreenChecks.kt               — device admin, trust agents, encryption, lock screen notifications, sensitive content, media, app pinning
    DeveloperChecks.kt                — developer options, OEM unlock
    SystemStateChecks.kt              — health connect, sensor default (stub), crash notifications (stub), FLAG_SECURE, VPN
  accessibility/
    AccessibilityStateValidator.kt    — orchestrator
    ServiceChecks.kt                  — master toggle, raw enabled string, service list API
    FeatureChecks.kt                  — magnification, inversion, daltonizer, high contrast, captions, font scale, mono, touch exploration
    ShortcutChecks.kt                 — shortcut target service, button targets
  integrity/
    DeviceIntegrityVerifier.kt        — existing attestation code, moved here
    Asn1Parser.kt                     — existing, moved here
  exploit/
    ExploitProtectionValidator.kt     — stub only, returns "Phase 2 — JNI required"
```

### Sub-group shape

```kotlin
object RadioChecks {
    fun checkAll(context: Context): List<CheckResult> { ... }

    private fun checkAirplaneMode(context: Context): CheckResult { ... }
    private fun checkWifi(context: Context): CheckResult { ... }
    // etc
}
```

### Validator orchestrator shape

```kotlin
object AirGapStateValidator {
    fun validate(context: Context): List<CheckResult> =
        RadioChecks.checkAll(context) +
        LocationChecks.checkAll(context) +
        UsbChecks.checkAll(context) +
        SoftwareServiceChecks.checkAll(context) +
        AccountChecks.checkAll(context) +
        LockScreenChecks.checkAll(context) +
        DeveloperChecks.checkAll(context) +
        SystemStateChecks.checkAll(context)
}
```

## BroadcastReceiver and ContentObserver

Expand IntentFilter to include all spec-defined actions:

```
// Existing
ACTION_AIRPLANE_MODE_CHANGED
BluetoothAdapter.ACTION_STATE_CHANGED
NfcAdapter.ACTION_ADAPTER_STATE_CHANGED
SIM_STATE_CHANGED
WIFI_STATE_CHANGED_ACTION
WIFI_P2P_STATE_CHANGED_ACTION
TETHER_STATE_CHANGED
USB_STATE
ACTION_POWER_CONNECTED
WIFI_AWARE_STATE_CHANGED

// New
SUBSCRIPTION_CHANGED              — eSIM changes
ACTION_POWER_DISCONNECTED          — USB removal
```

Register `SubscriptionManager.OnSubscriptionsChangedListener` for eSIM profile changes.

Register `ContentObserver` on `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` URI for real-time accessibility service detection. Wired up by the ViewModel alongside the BroadcastReceiver.

All reactive triggers call the ViewModel's refresh. No per-validator routing.

## Tabbed UI

Replace the flat `LazyColumn` with `TabRow` + `HorizontalPager` (Material 3, swipeable):

| Tab | Validator |
|-----|-----------|
| Air Gap | AirGapStateValidator |
| Accessibility | AccessibilityStateValidator |
| Integrity | DeviceIntegrityVerifier |
| Exploit | ExploitProtectionValidator (stub) |

Each tab contains a `LazyColumn` of `SurfaceListItem` showing that validator's checks.

Tab badge: each tab shows violation count (e.g. "Air Gap (3)").

TopAppBar colour logic stays — turns red if ANY validator across ANY tab has a HARD violation.

## Stub Pattern for Undiscovered GOS Keys

GrapheneOS-specific checks where the settings key is unknown return a violation with a descriptive message:

```kotlin
private fun checkSuplEnabled(context: Context): CheckResult = CheckResult(
    surface = SurfaceName.SUPL,
    outcome = Either.Left(ViolationDetail("Key undiscovered — run ADB on device")),
)
```

Always shows as a violation in the UI, reminding that ADB discovery is needed. Replace stub body with real `Settings.Secure` lookup once the key is discovered on-device.

Affected checks (~8): SUPL, PSDS, eSIM toggle, default sensor permission, crash notifications, USB data mode, emergency SOS exact key, emergency alerts exact keys.

## Migration Path

Redistribute, not rewrite:

1. **`SensorChecker.kt`** — delete. 23 check functions move to appropriate sub-group files.
2. **`Asn1Parser.kt`** — move to `detection/integrity/`. Package declaration change only.
3. **`AirGapBroadcastReceiver.kt`** — stays at `detection/` level. Expand IntentFilter.
4. **`SensorName` → `SurfaceName`** — rename enum, add ~30 new entries.
5. **`SensorStatus` → `SurfaceStatus`** — rename, swap `Either<String, String>` for `Either<ViolationDetail, SafeDetail>`.
6. **`AirGapViewModel`** — calls all 4 validators, groups results by validator for tab UI. Registers ContentObserver.
7. **`AirGapScreen`** — replace `LazyColumn` with `TabRow` + `HorizontalPager`.
8. **`SensorListItem` → `SurfaceListItem`** — rename.

No logic changes to existing checks — they move to new homes.

## Verification

1. Build compiles with zero errors
2. Run on device/emulator — all 4 tabs visible and swipeable
3. Existing 23 checks still produce same results in their new locations
4. New checks appear in correct tabs with appropriate violation/safe states
5. GOS stub checks show "Key undiscovered" violation detail
6. BroadcastReceiver still triggers refresh on radio state changes
7. TopAppBar turns red on any HARD violation across any tab
8. Tab badges show correct violation counts
