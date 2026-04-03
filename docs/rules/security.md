# Alice Security Constraints — Non-Negotiable

---

## Compose Semantics — Complete Prohibition

**Applies to: Alice, Bob, and all POCs.**

Every `Modifier` chain in every `@Composable` must begin with `.semanticsSealed()` — no exceptions. This applies to every composable that receives or constructs a `Modifier`, including inner layout elements (`Column`, `Box`, `Spacer`, etc.).

```kotlin
// Correct
modifier = modifier.semanticsSealed().fillMaxSize()
modifier = Modifier.semanticsSealed().fillMaxWidth().padding(ITEM_PADDING)

// Wrong — missing semanticsSealed
modifier = modifier
modifier = Modifier.fillMaxSize()

// Wrong — not first
modifier = modifier.padding(16.dp).semanticsSealed()
```

The `semanticsSealed()` extension wraps `clearAndSetSemantics {}`. It lives in a `modifier` package within the composables directory. Every project must include it.

Never add to any composable:
- `Modifier.semantics {}`
- `Modifier.testTag()`
- `contentDescription` parameters
- `isTraversalGroup`, `heading`, `role`, or any semantics property

**Why this rule exists:** Accessibility services (TalkBack, Switch Access, third-party screen readers) can read all semantic nodes in real time. On an air-gapped security device, this is a data exfiltration vector — a malicious or compromised accessibility service could silently scrape screen content including cryptographic ceremony state, contact identities, and verification phrases. This is a deliberate security trade-off, not an accessibility oversight.

**UI testing consequence:** With all semantics cleared, `ComposeTestRule` finders return nothing by design. UI is verified at the presenter/state layer only. Visual correctness is validated via screenshot tests and manual review on the target GrapheneOS device.

### SemanticsNotSealed Lint Rule — Mandatory

Every POC and application (Alice and Bob) must include a `:lint-checks` module containing the `SemanticsNotSealedDetector`. This lint rule:
- Severity: **ERROR** (build-breaking)
- Scans every `Modifier` chain and verifies the first chained call is `semanticsSealed()` or `clearAndSetSemantics()`
- Wired via `lintChecks(project(":lint-checks"))` in every module containing Compose UI

Reference implementation: `wonderland/poc/0000_alice_broadcast_receiver/lint-checks/`

An instrumented test must traverse the full unmerged semantics tree of every screen and assert every node's `SemanticsConfiguration` is empty. Runs on every CI build — failing after a Compose BOM update is a blocker.

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

- Zero after use: `bytes.fill(0)` in `finally` blocks — mandatory even on failure paths.
- No `toString()` overrides on crypto value classes — prevent accidental logging.
- Room: no tables store private keys, session keys, or ephemeral keys. Ever.

---

## Production Build Only

Alice is always installed as a release build on the production device. Debug builds are never installed on the Alice device.

---

## ML Kit

Use bundled only: `com.google.mlkit:barcode-scanning`
Never: `play-services-mlkit-barcode-scanning`

---

## Air-Gap Surveillance System

Full spec: `docs/superpowers/specs/2026-03-30-air-gap-surveillance-design.md`. The spec is authoritative — do not duplicate here.

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
