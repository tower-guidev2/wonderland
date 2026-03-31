# Alice Security Constraints — Non-Negotiable

---

## Compose Semantics — Complete Prohibition

Every `@Composable` in Alice that emits UI must apply `clearAndSetSemantics {}` as the **first** modifier in its chain.

```kotlin
// Correct
modifier = modifier.clearAndSetSemantics { }

// Wrong — missing suppression
modifier = modifier

// Wrong — wrong position
modifier = modifier.padding(16.dp).clearAndSetSemantics { }
```

Never add to any Alice composable:
- `Modifier.semantics {}`
- `Modifier.testTag()`
- `contentDescription` parameters
- `isTraversalGroup`, `heading`, `role`, or any semantics property

A lint rule in build-logic must fail the Alice build if any composable applies a `Modifier` without `clearAndSetSemantics` first.

An instrumented test must traverse the full unmerged semantics tree of every Alice screen and assert every node's `SemanticsConfiguration` is empty. Runs on every CI build — failing after a Compose BOM update is a blocker.

`ComposeTestRule` finders return nothing by design on Alice. Alice UI behaviour is verified at presenter and state layer only.

---

## Manifest — Prohibited Permissions

Must never appear in Alice's merged manifest:
- `android.permission.INTERNET`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_COARSE_LOCATION`
- `android.permission.BLUETOOTH`
- `android.permission.READ_CONTACTS`
- `android.permission.WRITE_CONTACTS`

**Surveillance exceptions** — permitted exclusively in `alice:core:surveillance`:
- `android.permission.ACCESS_NETWORK_STATE` — detect network interface activation
- `android.permission.ACCESS_WIFI_STATE` — detect Wi-Fi state changes
- `android.permission.BLUETOOTH_CONNECT` — query Bluetooth adapter state (runtime, dangerous)
- `android.permission.READ_BASIC_PHONE_STATE` — detect SIM state (normal, install-time, API 33+)

These permissions enable detection only. They do not enable network access or data transmission.

Permitted beyond surveillance: `android.permission.CAMERA` only.

A lint rule must fail the Alice build if any prohibited permission appears in the merged manifest.

---

## Manifest — Feature Declarations

```xml
<uses-feature android:name="android.hardware.camera" android:required="true"/>
<uses-feature android:name="android.hardware.wifi" android:required="false"/>
<uses-feature android:name="android.hardware.bluetooth" android:required="false"/>
<uses-feature android:name="android.hardware.bluetooth_le" android:required="false"/>
<uses-feature android:name="android.hardware.nfc" android:required="false"/>
<uses-feature android:name="android.hardware.telephony" android:required="false"/>
<uses-feature android:name="android.hardware.location" android:required="false"/>
<uses-feature android:name="android.hardware.location.gps" android:required="false"/>
```

---

## Cryptographic Key Material

Never written to any persistent storage of any kind — including `SharedPreferences`, `DataStore`, `Room`, files, `ViewModel`, companion objects, or object singletons.

Keys live only in local variables or function parameters for the duration of a single operation.

---

## Production Build Only

Alice is always installed as a release build on the production device. Debug builds are never installed on the Alice device.

---

## ML Kit

Use bundled only: `com.google.mlkit:barcode-scanning`
Never: `play-services-mlkit-barcode-scanning`

---

## Air-Gap Surveillance System

Full spec: `docs/superpowers/specs/2026-03-30-air-gap-surveillance-design.md`. This section is a summary — the spec is authoritative.

Alice implements continuous multi-channel air-gap surveillance. Mental model: inverse of a connectivity listener — Alice reacts to connectivity *gain* or radio *activation*, not loss.

### Module Structure

- `alice:core:surveillance_api` — pure Kotlin domain types, zero Android imports
- `alice:core:surveillance` — Android implementation. Feature modules depend on `surveillance_api` only.

### Domain Interface

```kotlin
interface IAirGapSurveillance {
    val status: StateFlow<AirGapStatus>
    val violations: Flow<AirGapViolation>
}

sealed interface AirGapStatus {
    data object Secure : AirGapStatus
    data class Compromised(val violation: AirGapViolation) : AirGapStatus
}
```

Purely observational. No `start()` / `stop()`. Lifecycle is structural — always on from app process creation. Workers enqueued via App Startup. BroadcastReceiver and NetworkCallback live in callbackFlows scoped to the application CoroutineScope.

23 violation types in a sealed class with HARD/SOFT severity. Covers: radio surfaces (airplane mode, Bluetooth, BLE, NFC, SIM, Wi-Fi, Wi-Fi Direct, Wi-Fi Aware), network surfaces (active interface, VPN, tethering), background scans (Wi-Fi, BLE), location, device security (developer options, ADB, wireless ADB, accessibility services, display mirroring, OEM unlock), device integrity (build properties, StrongBox attestation), and USB power (soft).

### Architecture — Two Layers

**Reactive layer** (instant detection): Single consolidated BroadcastReceiver with compound `IntentFilter` covering all radio and device state broadcasts. Two `ConnectivityManager.NetworkCallback` instances — one for all networks, one VPN-specific.

**Periodic layer** (WorkManager): Three `PeriodicWorkRequest` tiers:
- FastTierWorker — 15 min, cheap Settings.Global reads
- StandardTierWorker — 1 hour, network enumeration
- SlowTierWorker — 6 hours, StrongBox hardware attestation

No network constraint — Alice is air-gapped.

**SIM monitoring:** Uses `"android.intent.action.SIM_STATE_CHANGED"` with `"ss"` string extra. `TelephonyManager.ACTION_SIM_CARD_STATE_CHANGED` is `@SystemApi` — not available to normal apps.

**Tethering monitoring:** Uses `"android.net.conn.TETHER_STATE_CHANGED"`. `TetheringManager` is `@SystemApi`.

### Violation Response

**Hard violations** (all radios, networks, security surfaces, integrity failures):
1. Zeroise all in-memory cryptographic material
2. Cancel all active coroutines holding key material
3. Emit to `status: StateFlow<AirGapStatus>` — non-dismissable tamper warning
4. No dismiss path. No continue-anyway. No developer override.
5. Requires full app restart AND clean synchronous check to resume

**Soft violation** (USB power only — release builds):
1. Emit suspension signal — informational suspension screen
2. No cryptographic zeroing
3. No auto-resume on disconnect — requires explicit app restart

**Development mode** (debug builds only): USB connection ignored entirely. No suspension, no violation emitted. Supports APK installation and debugging via USB.

Camera activity is not a violation. Do not monitor camera state.

---

## Global Never-Do Rules

- Never add network permissions to Alice
- Never add network code to Alice
- Never introduce a second communication channel between Alice and Bob
- If a feature requires a second channel between Alice and Bob, the design is wrong — raise it before implementing
- Never store cryptographic key material in any persistent storage
- Never produce a debug APK for the Alice production device
- Never use unbundled ML Kit on Alice
- Never add semantics to Alice composables
