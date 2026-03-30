# Alice Air-Gap Surveillance System — Design Specification

**Date:** 2026-03-30
**Status:** Approved
**Module:** `alice:core:surveillance_api` + `alice:core:surveillance`

---

## 1. Purpose

Alice must continuously verify that the device it runs on is fully air-gapped. This system detects all sensors, all remote access paths, and all network surfaces — then responds immediately when any surface is compromised. It also verifies that the device is a genuine Google Pixel running genuine GrapheneOS with a locked bootloader.

The mental model is the inverse of a connectivity listener: where a normal app reacts to connectivity loss, Alice reacts to connectivity gain or radio activation on any interface.

---

## 2. Module Structure

```
alice/core/surveillance_api/     wonderland.android.library
  └── Pure Kotlin domain types — zero Android imports
      IAirGapSurveillance, AirGapViolation, AirGapStatus

alice/core/surveillance/         wonderland.android.library
  └── Android implementation
      Workers, BroadcastReceiver, NetworkCallback,
      App Startup Initializer, device integrity verifier,
      Koin wiring
```

`surveillance` depends on `surveillance_api`. Feature modules and `alice:app` depend on `surveillance_api` only. Koin wires the implementation at runtime.

---

## 3. Domain Interface (surveillance_api)

### IAirGapSurveillance

```kotlin
interface IAirGapSurveillance {
    val status: StateFlow<AirGapStatus>
    val violations: Flow<AirGapViolation>
}
```

Purely observational. No `start()` / `stop()`. Lifecycle is structural — the system is always on from the moment the app process exists. Workers are enqueued via App Startup. BroadcastReceiver and NetworkCallback live in callbackFlows scoped to the application CoroutineScope. Nothing for the caller to manage.

### AirGapStatus

```kotlin
sealed interface AirGapStatus {
    data object Secure : AirGapStatus
    data class Compromised(val violation: AirGapViolation) : AirGapStatus
}
```

### AirGapViolation

23 granular violation types. Each distinct attack surface gets its own type so the tamper screen reports exactly what tripped.

```kotlin
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

---

## 4. Architecture — Two Layers

### 4.1 Reactive Layer (instant detection)

Lives in `callbackFlow` blocks collected at application `CoroutineScope` via Koin. Catches state transitions the moment they happen.

#### AirGapSurveillance — single consolidated BroadcastReceiver

Registered with `RECEIVER_NOT_EXPORTED`. One `IntentFilter`, one `onReceive`, one violation channel.

| Signal | Intent Action | Extra | Violation States |
|---|---|---|---|
| Airplane mode | `Intent.ACTION_AIRPLANE_MODE_CHANGED` | `"state"` (boolean) | `false` = off |
| Bluetooth | `BluetoothAdapter.ACTION_STATE_CHANGED` | `EXTRA_STATE` (int) | `STATE_ON` (12), `STATE_TURNING_ON` (11), `STATE_BLE_ON` (15), `STATE_BLE_TURNING_ON` (14) |
| NFC | `NfcAdapter.ACTION_ADAPTER_STATE_CHANGED` | `EXTRA_ADAPTER_STATE` (int) | `STATE_ON` (3), `STATE_TURNING_ON` (2) |
| SIM | `"android.intent.action.SIM_STATE_CHANGED"` | `"ss"` (String) | Any value other than `"ABSENT"` |
| Wi-Fi | `WifiManager.WIFI_STATE_CHANGED_ACTION` | `EXTRA_WIFI_STATE` (int) | `WIFI_STATE_ENABLED` (3), `WIFI_STATE_ENABLING` (2) |
| Wi-Fi Direct | `WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION` | `EXTRA_WIFI_STATE` (int) | `WIFI_P2P_STATE_ENABLED` (2) |
| Wi-Fi Aware | `WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED` | N/A — query `isAvailable()` | `true` |
| Tethering | `"android.net.conn.TETHER_STATE_CHANGED"` | `"tetherArray"` (String[]) | Non-empty array |
| USB state | `"android.hardware.usb.action.USB_STATE"` | `"connected"` (boolean) | `true` |
| USB power | `Intent.ACTION_POWER_CONNECTED` / `_DISCONNECTED` | Sticky `ACTION_BATTERY_CHANGED` → `EXTRA_PLUGGED` | `BATTERY_PLUGGED_USB` (2) |

**SIM monitoring note:** `TelephonyManager.ACTION_SIM_CARD_STATE_CHANGED` is `@SystemApi` / `@hide` and requires `READ_PRIVILEGED_PHONE_STATE` (signature-level). Not available to normal apps. The correct broadcast is the legacy `"android.intent.action.SIM_STATE_CHANGED"` with string extra `"ss"`.

**Tethering note:** `TetheringManager` is entirely `@SystemApi`. The broadcast `"android.net.conn.TETHER_STATE_CHANGED"` is receivable by normal apps. Check extra `"tetherArray"` for non-empty String array.

#### AirGapNetworkMonitor — ConnectivityManager callbacks

Two callbacks registered on `ConnectivityManager`:

**Callback A — all networks:**
```kotlin
val request = NetworkRequest.Builder().build()
connectivityManager.registerNetworkCallback(request, callback)
```
Any `onAvailable()` triggers `NetworkInterfaceActive`. Check all transports 0-9 via `hasTransport()`:
`TRANSPORT_CELLULAR` (0), `TRANSPORT_WIFI` (1), `TRANSPORT_BLUETOOTH` (2), `TRANSPORT_ETHERNET` (3), `TRANSPORT_VPN` (4), `TRANSPORT_WIFI_AWARE` (5), `TRANSPORT_LOWPAN` (6), `TRANSPORT_TEST` (7), `TRANSPORT_USB` (8), `TRANSPORT_THREAD` (9).

**Callback B — VPN-specific:**
```kotlin
val vpnRequest = NetworkRequest.Builder()
    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
    .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
    .build()
connectivityManager.registerNetworkCallback(vpnRequest, vpnCallback)
```
The default `NetworkRequest` includes `NET_CAPABILITY_NOT_VPN` which filters out VPN networks. Must explicitly remove it.

### 4.2 Periodic Layer (WorkManager)

Three `PeriodicWorkRequest` Workers, enqueued once via App Startup with `ExistingPeriodicWorkPolicy.KEEP`.

No network constraint. Alice is air-gapped — `Constraints.Builder().setRequiredNetworkType(CONNECTED)` would prevent workers from ever firing.

#### FastTierWorker — every 15 minutes

Cheap reads. Settings.Global, adapter state queries.

| Check | API | Permission |
|---|---|---|
| Airplane mode | `Settings.Global.getInt(contentResolver, "airplane_mode_on", 0)` | None |
| Wi-Fi adapter | `Settings.Global.getInt(contentResolver, "wifi_on", 0)` | None |
| Bluetooth adapter | `Settings.Global.getInt(contentResolver, "bluetooth_on", 0)` | None |
| NFC | `Settings.Global.getInt(contentResolver, "nfc_on", 0)` | None |
| Bluetooth state (inc. BLE) | `BluetoothAdapter.state` — catch `STATE_BLE_ON` (15) | `BLUETOOTH_CONNECT` |
| NFC adapter | `NfcAdapter.isEnabled` | None |
| Wi-Fi background scan | `WifiManager.isScanAlwaysAvailable()` | None |
| BLE background scan | `Settings.Global.getInt(contentResolver, "ble_scan_always_enabled", 0)` | None |
| SIM state | `TelephonyManager.simState` | `READ_BASIC_PHONE_STATE` |
| Wi-Fi Aware | `WifiAwareManager.isAvailable()` | None |
| Location enabled | `LocationManager.isLocationEnabled()` | None |
| ADB enabled | `Settings.Global.getInt(contentResolver, "adb_enabled", 0)` | None |
| Wireless ADB | `Settings.Global.getInt(contentResolver, "adb_wifi_enabled", 0)` | None |
| Developer options | `Settings.Global.getInt(contentResolver, "development_settings_enabled", 0)` | None |
| Accessibility services | `AccessibilityManager.isEnabled()` | None |

#### StandardTierWorker — every 1 hour

Network enumeration and less-frequent surface checks.

| Check | API | Permission |
|---|---|---|
| Active network | `ConnectivityManager.activeNetwork` | `ACCESS_NETWORK_STATE` |
| All networks + VPN | `ConnectivityManager.allNetworks` → `hasTransport(TRANSPORT_VPN)` | `ACCESS_NETWORK_STATE` |
| Tethering | Sticky broadcast query for `TETHER_STATE_CHANGED` | `ACCESS_NETWORK_STATE` |
| Display mirroring | `DisplayManager.getDisplays()` — flag non-default displays | None |
| USB devices | `UsbManager.getDeviceList()` | None |
| OEM unlock toggle | `Settings.Global.getInt(contentResolver, "oem_unlock_allowed", 0)` | None |
| Storage encryption | `DevicePolicyManager.getStorageEncryptionStatus()` | None |
| UWB hardware | `PackageManager.hasSystemFeature("android.hardware.uwb")` + airplane mode proxy | None |

#### SlowTierWorker — every 6 hours

Device integrity verification. Aligned with GrapheneOS Auditor checks.

**Layer 1 — Build properties (fast, soft check):**

| Check | API |
|---|---|
| Manufacturer | `Build.MANUFACTURER == "Google"` |
| Brand | `Build.BRAND == "google"` |
| Device codename | `Build.DEVICE` in known Pixel codename set |
| Minimum SDK | `Build.VERSION.SDK_INT >= 33` |

**Layer 2 — StrongBox hardware attestation (strong, async):**

Generates an ephemeral EC key in Titan M2 StrongBox with `setAttestationChallenge()`. Parses `KeyDescription` ASN.1 extension (OID `1.3.6.1.4.1.11129.2.1.17`) from certificate chain index 1. Uses BouncyCastle (already in Alice's stack) for ASN.1 parsing. Fully offline — zero network calls.

| Assertion | Field | Expected Value |
|---|---|---|
| Hardware-backed | `attestationSecurityLevel` | `StrongBox` (2) |
| Anti-replay | `attestationChallenge` | Matches generated challenge |
| Bootloader locked | `rootOfTrust.deviceLocked` | `true` |
| GrapheneOS | `rootOfTrust.verifiedBootState` | `SelfSigned` (1) |
| Known OS signing key | `rootOfTrust.verifiedBootKey` | SHA-256 fingerprint in published GrapheneOS key set |

Source authority for verified boot keys: https://grapheneos.org/articles/attestation-compatibility-guide

**Maintenance:** When GrapheneOS adds new Pixel device support, add the new `verifiedBootKey` fingerprint and device codename.

---

## 5. Worker Pattern

Adapted from the scientific-research project's `core/worker` module.

### Retained

- `BaseWorker` — `CoroutineWorker` wrapper with `doActualWork()` abstract method, cancellation handling
- `ScheduleUniqueName` — inline value class for type-safe work names
- `IWorkScheduler` / `WorkScheduler` — scheduler abstraction wrapping `WorkManager`

### Adapted

| Aspect | scientific-research | Alice surveillance |
|---|---|---|
| Work type | `OneTimeWorkRequest` chained in stages | `PeriodicWorkRequest` (15 min / 1 hour / 6 hours) |
| Network constraint | `NetworkType.CONNECTED` | None — Alice is air-gapped |
| DI | Hilt `@AssistedInject` | Koin `WorkerFactory` |
| Stage chaining | Prologue → Ping → Feature → Epilogue | Each tier is self-contained, no chaining |
| Work definition | `IFeatureWorkDefinition` with `buildStages()` | `ISurveillanceWorkDefinition` with `repeatInterval: Duration` |

### Work Definitions

```kotlin
interface ISurveillanceWorkDefinition {
    val uniqueName: ScheduleUniqueName
    val repeatInterval: Duration
    val existingWorkPolicy: ExistingPeriodicWorkPolicy
        get() = ExistingPeriodicWorkPolicy.KEEP
}
```

Three concrete definitions:

| Definition | Unique Name | Interval |
|---|---|---|
| `FastTierDefinition` | `"surveillance_fast"` | 15 minutes |
| `StandardTierDefinition` | `"surveillance_standard"` | 1 hour |
| `SlowTierDefinition` | `"surveillance_slow"` | 6 hours |

---

## 6. App Startup Integration

```kotlin
class AirGapInitializer : Initializer<IAirGapSurveillance> {
    // 1. Run fast-tier checks synchronously on main thread — gate UI
    //    All checks are cheap Settings.Global reads, safe on main thread
    // 2. Enqueue 3 PeriodicWorkRequests (KEEP policy)
    // 3. Start reactive callbackFlow collection at app scope
    // 4. Return IAirGapSurveillance instance (Koin-provided)

    override fun dependencies() =
        listOf(WorkManagerInitializer::class.java)
}
```

Runs on main thread during content provider initialisation, before `Application.onCreate()`. All fast-tier checks are cheap `Settings.Global` reads and adapter state queries — safe on main thread. If any check fails, the app enters compromised state before any UI renders. The tamper screen is the only thing the user sees.

---

## 7. Violation Response

### Hard violations

1. Zeroise all in-memory cryptographic material (delegates to Alice's crypto zeroing mechanism)
2. Cancel all active coroutines holding key material
3. Emit to `status: StateFlow<AirGapStatus>` — UI layer shows non-dismissable tamper warning
4. No dismiss path. No continue-anyway. No developer override.
5. Requires full app restart AND clean synchronous check to resume

### Soft violation (USB power only — release builds)

1. Emit suspension signal to `status: StateFlow<AirGapStatus>`
2. Display informational suspension screen with reason
3. No cryptographic zeroing
4. No auto-resume on disconnect — requires explicit app restart

### Development mode (debug builds only)

USB connection is ignored entirely when `BuildConfig.DEBUG == true`. No suspension, no screen, no violation emitted. This supports the development workflow where USB is required for APK installation and debugging. Release builds on the production device retain the soft violation behaviour above.

---

## 8. Permissions

### Alice manifest additions

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_BASIC_PHONE_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

**`ACCESS_NETWORK_STATE` exception:** This permission appears on the prohibited list in CLAUDE.md. It is required by surveillance to detect network interface activation — the very thing it guards against. It does not enable network access. Added with documented exception. The CLAUDE.md prohibited list needs updating to reflect this.

**`ACCESS_WIFI_STATE`** — normal, install-time. Required for Wi-Fi state broadcasts.

**`READ_BASIC_PHONE_STATE`** — normal, install-time. API 33 split from `READ_PHONE_STATE`. Covers `getSimState()` and SIM broadcasts. Does not gate sensitive identifiers (IMEI etc.).

**`BLUETOOTH_CONNECT`** — dangerous, runtime. One-time prompt on first launch. Required for `BluetoothAdapter.state` query. If denied: non-dismissable block explaining security monitoring rationale. If "don't ask again": direct user to Settings.

---

## 9. Testing Strategy

| Layer | Test Type | What |
|---|---|---|
| `surveillance_api` domain types | Pure JUnit 4 | Violation severity mapping, status sealed class |
| Fast/Standard/Slow tier checks | Pure JUnit 4 | Each check is a pure function: system state in → pass/fail out. Provider interfaces hide Android system calls |
| Intent-to-violation mapping | Pure JUnit 4 | Every action string, every state value, every null/unknown case. Pure function, no Android types |
| BroadcastReceiver dispatch | Robolectric | Intent action → violation type mapping with real receiver |
| NetworkCallback dispatch | Robolectric | Transport type → violation type mapping |
| Worker execution | Robolectric + `TestListenableWorkerBuilder` | Worker runs checks, emits correct violations |
| App Startup gate | Robolectric | Synchronous check blocks correctly on violation |
| Integration (feature modules) | `FakeAirGapSurveillance` | Feature modules test against fake, never real system |
| Hardware attestation | Manual on device | StrongBox + GrapheneOS boot key — cannot fake Titan M2 |
| Physical hardware events | Manual security acceptance | GrapheneOS Pixel device, all radios toggled |

---

## 10. Surfaces Not Monitorable from App Tier

Documented as accepted residual risk.

| Surface | Why | Mitigation |
|---|---|---|
| Baseband / modem firmware | No SDK API exists | GrapheneOS IOMMU isolation + absent SIM |
| OS zero-day / zero-click | App cannot detect kernel exploits | All radios off = no network attack surface |
| UWB state (runtime) | `UwbManager` is `@SystemApi` | Feature detection via PackageManager + airplane mode proxy |
| GrapheneOS per-app sensor toggle | Not queryable via public API | Check Alice's own permission grants |
| IR blaster | No Pixel device has one | Non-issue on target hardware |
| `TRANSPORT_LOWPAN` | No Pixel has LoWPAN hardware | Non-issue on target hardware |
| `TRANSPORT_THREAD` | Not exposed to Android apps on Pixel | Non-issue on target hardware |

---

## 11. Maintenance

One ongoing responsibility:

When GrapheneOS adds new Pixel device support:
1. Check https://grapheneos.org/articles/attestation-compatibility-guide
2. Add the new `verifiedBootKey` SHA-256 fingerprint
3. Add the device codename to the known Pixel set

---

## 12. CLAUDE.md Updates Required

This design identifies the following CLAUDE.md items needing update:

1. **`ACCESS_NETWORK_STATE` prohibited permission** — add documented exception for surveillance
2. **`IAirGapSurveillance` interface** — remove `start()` / `stop()` / `currentStatus()`, replace with `status: StateFlow<AirGapStatus>` + `violations: Flow<AirGapViolation>`
3. **`AirGapViolation` sealed interface** — expand from 6 to 23 types (sealed class with severity)
4. **`AirGapStatus`** — `Compromised` carries `AirGapViolation` not a description string
5. **SIM monitoring** — correct `ACTION_SIM_CARD_STATE_CHANGED` reference to `"android.intent.action.SIM_STATE_CHANGED"` with `"ss"` string extra
6. **Full CLAUDE.md audit** — pre-release bytecode rule and other items need review (separate task)
