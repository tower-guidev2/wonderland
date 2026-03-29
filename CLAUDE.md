# Wonderland — Claude Code Project Context

This file is read at the start of every session. It is the single source of truth for project context, decisions, and rules. Read it fully before doing anything.

---

## Session Continuity

When resuming work, read the project's design docs and recent files before asking the user to re-explain context. Key design docs are in the project root. Grok has contributed sections to design documents — treat those as authoritative.

---

## Project Context

This is a multi-module Android project (Miss Charming) using Kotlin 2.3, Jetpack Compose with M3 theming, and Koin for dependency injection. Always reference the latest Kotlin 2.3 syntax — do not guess at newer language features like explicit backing fields; fetch the actual docs if unsure.

---

## Project Overview

**Miss Charming** is the most secure messaging Android application in existence. Two companion apps — Alice (air-gapped crypto vault) and Bob (online courier) — communicate exclusively via QR codes, with zero server involvement, zero plaintext on any networked device, and a full cryptographic protocol built on X3DH and Double Ratchet. The threat model assumes every layer of the stack is compromised. The target is 1 billion users. The complete design is in `docs/design.md`.

**Before making any suggestion** — architectural, naming, protocol, or library — read `docs/design.md` first. Many decisions are already made and documented. Do not re-open closed decisions. Do not propose alternatives to things already decided.

---

## What This Project Is

**Wonderland** is the codebase for **Alice & Bob** — a secure anonymous messaging system built as two companion Android applications. The design goal is 1 billion worldwide users. The full design document is at `docs/design.md`. Read it before making any architectural or protocol decisions.

### The Two Apps

| | Alice | Bob |
|---|---|---|
| **Package** | `org.alice.rabbit.hole` | `org.bob.cheshire.cat` |
| **minSdk** | 33 (GrapheneOS / Pixel 6+) | 26 (widest Android reach) |
| **Role** | Air-gapped crypto vault | Online courier / transport relay |
| **Rule** | Never touches the network | Never touches plaintext |

Alice and Bob communicate **exclusively via QR codes**. No other channel exists or is permitted.

---

## Module Structure

```
wonderland/
├── core/
│   ├── protocol/       — CBOR schemas, type/version bytes, all QR types
│   ├── cryptography/   — VaultCryptoEngine interface + KotlinVaultCryptoEngine
│   │                     entropy pool, BouncyCastle wrappers, Double Ratchet, X3DH
│   ├── qr/             — ZXing encode/decode wrapper
│   ├── common/         — base-37, padding, utilities
│   └── testfixtures/   — shared test fakes, data builders, test utilities
│
├── alice/              — D1 Vault app (minSdk 33)
│   ├── core:surveillance/ — pure Kotlin domain types (IAirGapSurveillance, AirGapViolation, AirGapStatus)
│   ├── core:ui/        — Alice Compose theme (feminine, Rolls Royce quality)
│   ├── app/
│   ├── feature:contacts/
│   ├── feature:keygen/
│   ├── feature:messaging/
│   ├── feature:pairing/
│   ├── feature:scanner/
│   ├── feature:settings/
│   └── feature:delivery/  — tentative
│
└── bob/                — D2 Courier app (minSdk 26)
    ├── core:ui/        — Bob Compose theme (masculine, same font family as Alice)
    ├── app/
    ├── feature:scanner/
    ├── feature:contacts/
    ├── feature:delivery/
    ├── feature:receive/
    ├── feature:pairing/
    └── feature:settings/
```

---

## Naming Rules — Non-Negotiable

These are locked decisions. Do not revisit, suggest alternatives, or silently revert to any external standard.

### Identifiers
- **No abbreviations. Ever.** Full words always. `cryptography` not `crypto`. `identifier` not `id`.
- **No technology names in identifiers.** Never embed library, language, or platform names — `Kotlin`, `Rust`, `ZXing`, `Room`, `Koin`, `Bouncy`, `CBOR` — in any class, function, or variable name. Name what the thing *does*, not what runs it. Exceptions require explicit documented justification.
- **Interface prefix `I`.** Interfaces are named `IFoo`. The primary production implementation drops the `I` and takes the bare name `Foo`. Test fakes are `FakeFoo`. Example: `IVaultCryptographyEngine` (interface) → `VaultCryptographyEngine` (impl) → `FakeVaultCryptographyEngine` (test fake).
- **No `lateinit`. Ever.**
- **No backtick function names. Ever.** Including in tests.
- **No magic numbers or strings.** Extract to named constants.
- **Short, sharp names.** Single words where possible. Max ~30 chars. Uncle Bob self-documenting.
- **No plurals** unless the thing genuinely is a collection.
- **Matching pairs and word families** — names in a set must be from the same vocabulary.
- **No negative-logic names.** Name what something *is*, not what it isn't.
- **Prefer `==` over `!=`.** Structure `if/else` with the positive case first.
- **Never use `!` for negation.** Use `.not()` instead.
- **Single-expression `if` branches with no `{}` braces** — only when the branch is one line.
- **Line width: 180 characters.** This overrides every external standard.

### Modules
- **No hyphens in module names.** Use `:` hierarchy — `core:ui`, not `core-ui`.
- **Exception: `build-logic`.** The composite build directory is named `build-logic` with a hyphen. This is intentional — it must stand apart from app modules and be immediately obvious in the project structure. This is the only hyphen permitted anywhere in the project.
- **Module name segments are single lowercase words.** Never abbreviated.
- **Package names mirror module hierarchy exactly.**
  - Root app package + module path segments.
  - Alice: `org.alice.rabbit.hole` + module `core:cryptography` → `org.alice.rabbit.hole.core.cryptography`
  - Bob: `org.bob.cheshire.cat` + module path

---

## Code Standards — Non-Negotiable

These rules override every Google guide, Now in Android reference, and linter default. They are not up for debate.

- User's style wins. Always. Never suggest "the standard way is..." for anything already decided.
- The rules above are locked. Do not re-open them.
- **Kotlin 2.0+ syntax — fetch before writing.** Before writing any Kotlin code that uses features from Kotlin 2.0+, fetch the relevant page from `kotlinlang.org/docs` and verify the exact syntax. Do not guess. Do not rely on training data for newer language features. Fetch first, write second.

## Language & Framework

This is an Android project using Kotlin 2.3+. Always check actual Kotlin documentation before suggesting syntax for newer language features (e.g., explicit backing fields). Do not assume syntax from older versions.

---

## Code Style & Conventions

For module and package naming decisions, always present options and let the user choose. Do not default to generic names like 'compose'. Follow existing project naming conventions visible in settings.gradle.kts.

---

## Conventions — Undecided Naming

The naming rules above are locked for decisions already made. For any naming decision not yet covered:

- Present options with explicit trade-offs. Do not make a single recommendation and present it as obvious.
- Check `docs/design.md` first — the decision may already be made.
- If the decision is not in the design doc, surface it as a genuine choice for the user to make.
- Once the user decides, that decision is locked. Record it and follow it.

---

## Technology Stack

### All modules
- Kotlin (pure — no Java)
- `kotlinx-coroutines-android` — async
- `kotlinx-serialization-cbor` — CBOR encoding (not `co.nstant.in:cbor`)
- `koin-android` — DI (not Hilt, not Dagger)

### shared:cryptography
- `org.bouncycastle:bcprov-jdk18on` — ALL crypto primitives. No other library does crypto.
- Primitives: X25519, Ed25519, HKDF-SHA256, ChaCha20-Poly1305, SHA-256, Argon2id
- Protocol: X3DH + Double Ratchet — pure Kotlin, no library
- Compression: `com.github.luben:zstd-jni` — Phase 1 only

### shared:qr
- `com.google.zxing:core` — encode/decode (no Android dependency)

### Alice
- `androidx.room` — storage with BouncyCastle TypeConverters for column-level encryption at rest
- `androidx.camera:camera-camera2/lifecycle/view` — CameraX
- `androidx.navigation3:navigation3-runtime/ui:1.0.1` — Navigation 3 stable
- Jetpack Compose BOM
- Custom `InputMethodService` keyboard — covers a-z, 0-9, space only. `FLAG_SECURE` set.

### Bob
- `androidx.room` — encrypted contact directory via BouncyCastle TypeConverters
- CameraX, Navigation 3, Compose BOM (same as Alice)
- Deep link delivery: SMS (`smsto:`), WhatsApp (`whatsapp://send`), Telegram (`tg://resolve`)

### Deliberately excluded
- No Retrofit / OkHttp in Alice — air-gapped
- No Firebase / Analytics anywhere
- No Hilt / Dagger — Koin is sufficient
- No `androidx.biometric` — bypassable, PIN only
- No JUnit 5 — JUnit 4 preferred
- No `co.nstant.in:cbor` — replaced by `kotlinx-serialization-cbor`

---

## Architecture Decisions

Encryption approach: Use BouncyCastle with Room TypeConverters for field-level encryption. Do NOT suggest SQLCipher. DI framework: Koin (not Hilt/Dagger).

---

## Testing Rules — Non-Negotiable

Follow this hierarchy in order. Never skip levels.

1. **JUnit 4 JVM test** — the default. Always try this first.
2. **+ Robolectric** — only when Android framework is genuinely needed.
3. **+ MockK** — only when a real or fake cannot be used. Never mock what can be faked.
4. **Android instrumented test** — last resort. Must be explicitly justified with a comment.

**Additional rules:**
- **AssertK** for all assertions — not JUnit assert methods, not Truth
- **Turbine** for any test involving Flow or StateFlow
- **AAA structure** (Arrange / Act / Assert) in every test
- All test data from `:shared:testfixtures` — no inline data in test classes
- `kotlin-faker 1.16.1` for dynamic data — always seeded, always in testfixtures
- testfixtures has two categories: **canonical** (fixed spec vectors) and **dynamic** (faker builders)
- **Coverage target:** 90%+ on `shared:cryptography` and `shared:protocol`
- **Spec vectors first:** every crypto function is tested against official specification test vectors before any other test is written

---

## Cryptographic Protocol — Key Decisions

The full spec is in `docs/design.md` Section 7. Key points:

- **All cryptography on Alice only.** Bob never sees plaintext or session keys.
- **Pipeline (immutable):** Composition → Padding → Compression → Encryption → CBOR → QR
- **Identity Key is two separate key pairs:** IK_Ed (Ed25519, signing only) + IK_X (X25519, DH only). Never mixed.
- **HKDF info strings** (namespace reserved `AliceBob_v1_`):
  - X3DH: `AliceBob_v1_X3DH`
  - Double Ratchet root: `AliceBob_v1_DR_RK`
  - MAC: `AliceBob_v1_MAC`
  - SAS: `AliceBob_v1_SAS`
- **Argon2id params:** 64 MiB, 3 iterations, parallelism 4, 32-byte output
- **`IVaultCryptographyEngine`** is the single interface boundary for all cryptography. Phase 1 = `VaultCryptographyEngine`. Phase 2 = `[name TBD — see Open Items]` via UniFFI. Swap is one Koin line.

---

## QR Protocol — Key Decisions

Full field registries in `docs/design.md` Section 6.

- **Wire format:** `[type: 1 byte][capabilities: 2 bytes][CBOR payload]`
- **11 QR types** defined. Types 8 and 9 deferred to V2.
- **CBOR integer keys only.** No string keys.
- **Strictly additive schema evolution.** Field numbers are permanent. Never reused. Never redefined.
- **Unknown fields silently ignored** — baked in from day one.
- **Dual-QR message transmission:** every message produces Type 3 + Type 10 simultaneously. Bob scans both in one camera frame via ML Kit bundled model.

---

## Phase 1 vs Phase 2

- **Phase 1** — pure Kotlin PoC. Proves the protocol, UX, and pipeline end-to-end. Uses `VaultCryptographyEngine`. Distributes via GitHub Releases APK.
- **Phase 2** — native security layer on Alice via Android NDK + UniFFI. Replaces cryptography engine only. Phase 1 code is not evolved — Phase 2 starts clean.
- The `IVaultCryptographyEngine` interface is the boundary. Define it right in Phase 1. Nothing else changes in Phase 2.

---

## Presenter Architecture — Molecule First

Phase 1a uses **Molecule-style Compose-runtime presenters**. `@Composable` presenter functions return state — they do not emit UI. Wrapped as `StateFlow` via Molecule's `launchMolecule`.

Reference: https://github.com/cashapp/molecule

```kotlin
@Composable
fun messagingPresenter(events: Flow<MessagingEvent>): MessagingState {
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is MessagingEvent.Send -> { /* update messages */ }
            }
        }
    }
    return MessagingState(messages)
}
```

Phase 1b (ViewModel-based MVI with `StateFlow` + `dispatch()`) is the named fallback if Molecule proves insufficient. Switch is per-feature — never mix patterns within a single feature.

---

## Code Generation Rules

**FETCH DOCS FIRST. ALWAYS. NO EXCEPTIONS.**

Before writing a single line of code — regardless of how simple or obvious it seems — fetch and read the authoritative documentation for every library and language feature the task touches. The Documentation Registry below contains the URLs. Use them. Every time.

This means:
- Identify what libraries and language features the task uses
- Fetch the relevant URL(s) from the Documentation Registry
- Read the actual current API surface and syntax from the fetched page
- Only then write code — based on what was fetched, never from training memory

This applies to Kotlin, AGP, Room, Koin, Arrow, Compose, Navigation, BouncyCastle, ZXing, Turbine — everything. There are no exceptions for "simple" cases. There are no exceptions under time pressure. If a URL is not in the registry, find the official source and fetch it anyway, and state what was fetched before writing anything.

When the user provides reference source code or points to specific documentation URLs, always read and follow them before generating code. Do not hallucinate APIs or syntax.

---

## Pre-Session Verification Protocol

Before writing any code, generating any file, or making any architectural decision:

1. State in one sentence what is about to be built
2. List every technology, library, and API the task touches
3. Fetch the authoritative documentation URL for each item from the Documentation Registry below
4. Confirm the current API surface matches the intended approach
5. Explicitly call out any discrepancy between training knowledge and current documentation
6. Only then begin implementation

If step 3 reveals the intended approach is outdated, revise the plan before writing a single line of code.

### Dependency Version Verification

Before declaring any dependency version in `libs.versions.toml`, `build.gradle.kts`, or `Cargo.toml`:

- Never write a version number from training memory — always fetch, always verify, always use latest stable
- If a release candidate or alpha is newer than latest stable, use latest stable unless there is an explicit documented reason
- After fetching: state the version found, the date it was released, and the source URL before writing it into any file

| Library | Source |
|---|---|
| Kotlin, KSP, Compose, ML Kit | Google Maven: https://maven.google.com/web/index.html |
| Room, Koin, Arrow, Bouncy Castle, ZXing, Turbine, Molecule | Maven Central: https://central.sonatype.com/search |
| Jetpack releases | https://developer.android.com/jetpack/androidx/releases |
| Rust crates | https://crates.io |

---

## Documentation Registry

### Language & Compiler
- Kotlin reference: https://kotlinlang.org/docs/home.html
- Kotlin for Android: https://developer.android.com/kotlin
- KSP overview: https://kotlinlang.org/docs/ksp-overview.html
- KSP2 internals: https://github.com/google/ksp/blob/main/docs/ksp2.md
- Migrate kapt to KSP: https://developer.android.com/build/migrate-to-ksp

### UI
- Compose guide: https://developer.android.com/develop/ui/compose/documentation
- Compose BOM: https://developer.android.com/jetpack/androidx/releases/compose

### Persistence
- Room guide: https://developer.android.com/training/data-storage/room
- Room API: https://developer.android.com/reference/androidx/room/package-summary

### Dependency Injection
- Koin Android: https://insert-koin.io/docs/quickstart/android/
- Koin definitions: https://insert-koin.io/docs/reference/koin-core/definitions/

### Functional Programming
- Arrow setup: https://arrow-kt.io/learn/quickstart/setup/
- Arrow Either: https://arrow-kt.io/learn/typed-errors/either/

### Cryptography
- Bouncy Castle: https://www.bouncycastle.org/documentation/documentation-java/

### QR & Camera
- ML Kit barcode (bundled): https://developers.google.com/ml-kit/vision/barcode-scanning/android
- CameraX + ML Kit: https://developer.android.com/media/camera/camerax/mlkitanalyzer
- CameraX overview: https://developer.android.com/media/camera/camerax
- ZXing core: https://github.com/zxing/zxing/tree/master/core

### Build System — AGP 9+
- AGP 9.0 release notes: https://developer.android.com/build/releases/agp-9-0-0-release-notes
- AGP 9.1 release notes: https://developer.android.com/build/releases/agp-9-1-0-release-notes
- AGP compatibility matrix: https://developer.android.com/build/releases/about-agp
- AGP DSL migration: https://developer.android.com/build/releases/gradle-plugin-roadmap

### Build System — Gradle
- Build environment: https://docs.gradle.org/current/userguide/build_environment.html
- Configuration cache: https://docs.gradle.org/current/userguide/configuration_cache_enabling.html
- Android build speed: https://developer.android.com/build/optimize-your-build
- Composite builds: https://docs.gradle.org/current/userguide/composite_builds.html
- Sharing build logic: https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html
- Convention plugins: https://docs.gradle.org/current/userguide/implementing_gradle_plugins_convention.html
- Version catalogs Gradle: https://docs.gradle.org/current/userguide/version_catalogs.html
- Version catalogs Android: https://developer.android.com/build/migrate-to-catalogs
- Gradle best practices: https://docs.gradle.org/current/userguide/best_practices.html

### Architecture
- App architecture: https://developer.android.com/topic/architecture
- UI layer: https://developer.android.com/topic/architecture/ui-layer
- Data layer: https://developer.android.com/topic/architecture/data-layer
- Domain layer: https://developer.android.com/topic/architecture/domain-layer
- Modularisation: https://developer.android.com/topic/modularization
- Modularisation patterns: https://developer.android.com/topic/modularization/patterns

### Reference Codebases
- Now in Android (gold standard): https://github.com/android/nowinandroid
- NowInAndroid architecture: https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
- NowInAndroid modularisation: https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md
- NowInAndroid build-logic: https://github.com/android/nowinandroid/tree/main/build-logic
- Molecule: https://github.com/cashapp/molecule
- Slack Circuit (MVI reference): https://slackhq.github.io/circuit/
- Orbit MVI (Phase 1b reference): https://github.com/orbit-mvi/orbit-mvi

### Testing
- Testing fundamentals: https://developer.android.com/training/testing/fundamentals
- What to test: https://developer.android.com/training/testing/fundamentals/what-to-test
- Test doubles: https://developer.android.com/training/testing/fundamentals/test-doubles
- Local unit tests: https://developer.android.com/training/testing/local-tests
- Coroutines testing: https://developer.android.com/kotlin/coroutines/test
- kotlinx-coroutines-test: https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test
- Turbine: https://github.com/cashapp/turbine
- Compose testing: https://developer.android.com/develop/ui/compose/testing
- Compose testing cheatsheet: https://developer.android.com/develop/ui/compose/testing/cheatsheet

### Security
- Android security best practices: https://developer.android.com/topic/security/best-practices
- R8 shrinking: https://developer.android.com/build/shrink-code

### ZKP & Rust (Rust track only)
- Rust book: https://doc.rust-lang.org/book/
- Mopro: https://github.com/zkmopro/mopro
- Halo2 book: https://zcash.github.io/halo2/
- Halo2 repo: https://github.com/zcash/halo2
- dalek zkp: https://github.com/dalek-cryptography/zkp
- bulletproofs: https://github.com/dalek-cryptography/bulletproofs
- cargo-ndk (Phase 2 only): https://github.com/bbqsrc/cargo-ndk
- Android JNI tips (Phase 2 only): https://developer.android.com/training/articles/perf-jni
- Android NDK guide (Phase 2 only): https://developer.android.com/ndk/guides

---

## Build System

This is a multi-module Android project. Library modules do NOT generate BuildConfig by default — use `android { buildFeatures { buildConfig = true } }` in library module build files if BuildConfig is needed. Avoid adding unnecessary desugaring or deprecated APIs.

---

## Android Build Notes

Library modules in Android do not generate BuildConfig by default. When referencing BuildConfig in a library module, add `android { buildFeatures { buildConfig = true } }` to that module's build.gradle.kts.

---

## Build System Constraints

### build-logic
Follows NowInAndroid exactly. Binary convention plugins (`.kt` implementing `Plugin<Project>`). Not precompiled script plugins (`.gradle.kts`).

`build-logic/settings.gradle.kts` must contain:
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```
This wiring is the most common build-logic mistake. Verify it exists before touching any convention plugin.

### KSP
KSP1 is incompatible with Kotlin ≥ 2.3 and AGP ≥ 9.0. Use KSP2 only. Never configure KSP1.

### AGP 9
Do not apply `org.jetbrains.kotlin.android` — AGP 9 includes it. Applying it manually breaks the build.

### Merged Manifest Auditing
After every dependency update, verify Alice's merged manifest at `build/intermediates/merged_manifest/` contains no prohibited permissions. Libraries inject permissions through their own manifests. Source manifest alone is insufficient.

### R8 / ProGuard
Explicit keep rules must be authored for both Alice and Bob. Bouncy Castle requires specific keeps or R8 silently strips cryptographic classes. Verify release build after every dependency update — debug build passing is not sufficient.

### Dependency Versions
All versions pinned exactly in `libs.versions.toml`. No dynamic versions anywhere.

---

## Critical Never-Do Rules

- Never apply `org.jetbrains.kotlin.android` with AGP 9
- Never import `com.android.build.gradle.LibraryExtension` — use `com.android.build.api.dsl.LibraryExtension` (public DSL). The internal type is removed in AGP 10.0
- `FontVariation.weight()` takes `Int` not `Float` — and requires `@OptIn(ExperimentalTextApi::class)` on the `FontFamily` declaration
- `TelephonyManager.ACTION_SIM_CARD_STATE_CHANGED` is `@SystemApi`/`@hide` — not available to normal apps. Use the string literal `"android.intent.action.SIM_STATE_CHANGED"` (no SDK constant exists for it). SIM state extra key is `"ss"` with string values ("ABSENT", "READY", "LOADED" etc.) — no public constant for the key either.
- `-XXLanguage:+ExplicitBackingFields` marks all compiled `.class` files as pre-release Kotlin, blocking android module consumers from loading them. `ExplicitBackingFields` is stable in Kotlin 2.2+ — use `-Xexplicit-backing-fields` only.
- **JVM library modules consumed by Android modules produce pre-release bytecode.** When a module uses `wonderland.jvm.library` (`org.jetbrains.kotlin.jvm`) AND is consumed by an Android module, the experimental compiler flags (`-Xcontext-parameters`, `-Xcontext-sensitive-resolution`) mark the JAR as pre-release and break the Android consumer's compilation. Fix: use `wonderland.android.library` instead of `wonderland.jvm.library` for any module in the alice or bob subtrees — even if the source is pure Kotlin with no Android imports. Only truly standalone, non-Android-consumed modules (e.g. `core:common`, `core:protocol`) should use `wonderland.jvm.library`.
- Android XML stub theme for Compose-only apps: use `android:Theme.Material.Light.NoActionBar`. `android:Theme.Material.NoTitleBar` and `android:Theme.Material.Light.NoTitleBar` do **not exist** in AOSP. `Theme.Material3.DayNight.NoActionBar` requires `com.google.android.material` — not present in Alice.
- Never use KSP1
- Never use dynamic dependency versions
- Never add network permissions to Alice
- Never add network code to Alice
- Never introduce a second communication channel between Alice and Bob
- Never use `runBlocking` in tests
- Never mock types you do not own
- Never collect a Flow in tests without Turbine
- Never add semantics to Alice composables (see Alice Security Constraints)
- Never add Android framework types to unit tests
- Never store cryptographic key material in any persistent storage
- Never produce a debug APK for the Alice production device
- Never use unbundled ML Kit on Alice (`play-services-mlkit-barcode-scanning` is banned)
- Never use `lateinit` — redesign for construction-time initialisation
- Never use abbreviations in identifiers
- Never use backtick function names anywhere including tests
- If a feature requires a second channel between Alice and Bob, the design is wrong — raise it before implementing

---

## Alice Security Constraints

### Compose Semantics — Complete Prohibition

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

An instrumented test must traverse the full unmerged semantics tree of every Alice screen and assert every node's `SemanticsConfiguration` is empty. This test runs on every CI build — a failing test after a Compose BOM update is a blocker.

`ComposeTestRule` finders return nothing by design on Alice. Alice UI behaviour is verified at presenter and state layer only.

### Alice Manifest — Prohibited Permissions

Must never appear in Alice's merged manifest:
- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.ACCESS_WIFI_STATE`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_COARSE_LOCATION`
- `android.permission.BLUETOOTH`
- `android.permission.BLUETOOTH_CONNECT`
- `android.permission.READ_CONTACTS`
- `android.permission.WRITE_CONTACTS`

Permitted: `android.permission.CAMERA` only. A lint rule must fail the Alice build if any prohibited permission appears in the merged manifest.

### Alice Manifest — Feature Declarations

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

### Alice — Cryptographic Key Material

Never written to any persistent storage of any kind including `SharedPreferences`, `DataStore`, `Room`, files, `ViewModel`, companion objects, or object singletons. Keys live only in local variables or function parameters for the duration of a single operation.

### Alice — Production Build Only

Always installed as a release build on the production device. Debug builds are never installed on the Alice device.

### Alice — ML Kit

Use bundled only: `com.google.mlkit:barcode-scanning`
Never: `play-services-mlkit-barcode-scanning`

---

## Alice Air-Gap Surveillance System

Alice implements a continuous multi-channel air-gap surveillance system. The mental model is the inverse of a connectivity listener: where a normal app reacts to connectivity loss, Alice reacts to connectivity gain or radio activation on any interface.

Surveillance starts in `Application.onCreate()` before any other initialisation. A synchronous check runs on every `Activity.onResume()` before any UI is displayed.

### Domain Interface

```kotlin
interface IAirGapSurveillance {
    val violations: Flow<AirGapViolation>
    fun start()
    fun stop()
    fun currentStatus(): AirGapStatus
}

sealed interface AirGapViolation {
    object AirplaneModeDisabled : AirGapViolation
    data class NetworkInterface(val description: String) : AirGapViolation
    object Bluetooth : AirGapViolation
    object Nfc : AirGapViolation
    object SimCard : AirGapViolation
    object UsbConnected : AirGapViolation
}

sealed interface AirGapStatus {
    object Secure : AirGapStatus
    data class Compromised(val violation: AirGapViolation) : AirGapStatus
}
```

Production impl: `AirGapSurveillance`. Test fake: `FakeAirGapSurveillance`.
No Android framework types in the domain interface. Android implementation lives in the data layer, injected via Koin.

### Airplane Mode — Primary Control Layer

Airplane mode is the master radio kill switch. Must be enabled at all times while Alice is in use. Airplane mode disabling is a hard violation — same response as network appearing.

Important: Airplane mode can be overridden per-radio after enabling. A user can re-enable Wi-Fi or Bluetooth while airplane mode is on. Individual radio monitoring is therefore still required alongside airplane mode monitoring. Both layers are necessary. Neither replaces the other.

### Single Consolidated BroadcastReceiver

All interface state changes handled by one receiver with a compound `IntentFilter`. One registration, one `onReceive`, one violation channel. Monitors: `ACTION_AIRPLANE_MODE_CHANGED`, `BluetoothAdapter.ACTION_STATE_CHANGED`, `NfcAdapter.ACTION_ADAPTER_STATE_CHANGED`, `TelephonyManager.ACTION_SIM_STATE_CHANGED`, `Intent.ACTION_POWER_CONNECTED`.

Network interfaces use `ConnectivityManager.NetworkCallback` separately — `onAvailable` triggers `AirGapViolation.NetworkInterface`.

### Synchronous Check — Every Startup and onResume

Runs before any UI is displayed. Checks in order: (1) Airplane mode, (2) Active network, (3) Bluetooth, (4) NFC, (5) SIM state, (6) USB power (release builds only).

### Violation Response

**Hard violations** (airplane mode disabled, network, Bluetooth, NFC, SIM):
1. Immediately zeroise all in-memory cryptographic material
2. Cancel all active coroutines via application root scope
3. Navigate unconditionally to full-screen tamper warning
4. No dismiss path. No continue-anyway. No developer override.
5. Requires full app restart AND clean synchronous check to resume

**Soft violation** (USB connected):
1. Zeroise all in-memory cryptographic material
2. Display informational suspension screen with reason
3. Do not auto-resume on disconnection — requires explicit restart

Camera activity is not a violation. Do not monitor camera state.

### Testing the Air-Gap Surveillance

All Android system calls hidden behind provider interfaces injected via Koin. Tests use `FakeAirGapSurveillance`, never real system adapters.

The intent-to-violation mapping is a pure function tested exhaustively with pure unit tests — every action string, every state value, every null/unknown case. No Android framework types.

Broadcast injection tests use Robolectric. Physical hardware events verified manually during security acceptance testing on the target GrapheneOS device.

Never mock the `BroadcastReceiver` itself — test `intentToViolation` and provider interfaces directly.

---

## Rust ZKP VM Track

### Current Phase — Pure Rust Only

No Android. No NDK. No JNI. No `.so` compilation. Build and test with standard Rust tooling only (`cargo test`, `cargo bench`, `cargo build`). If a task appears to require NDK, it is premature — raise it.

### What This Library Proves

Zero-knowledge proofs for the Alice/Bob protocol.

**Priority 1:** chain membership (valid next hash-chain link without revealing contents), identity (Alice holds private key without revealing it), encryption correctness (ciphertext is valid encryption without revealing key or plaintext).

**Priority 2:** key exchange correctness, freshness (not a replay), deniability (sender is one of {Alice, Bob} without specifying which).

**Priority 3 (if proof size and performance allow):** padding correctness, bundle validity.

### Hard Constraints

- Proof size: all proofs for a single message must fit within QR capacity after zstd compression. Target: under 2KB. If a proof system cannot meet this it is disqualified.
- No network calls of any kind. Fully offline.
- Proving must run on Alice's ARM64 device. Target: under 10s. Under 30s acceptable for Phase 1 research.
- No per-circuit trusted setup — Alice cannot participate in a setup ceremony while air-gapped. Universal or transparent setup only.

### Recommended Library

Primary: **Mopro + Halo2** — purpose-built for client-side mobile ZKP, no trusted setup, compact proofs, ~0.5s proving on Pixel 6 Pro, fully offline.

Fallback: **dalek zkp / bulletproofs** — use only if Merkle membership and verifiable encryption are not required.

Avoid for on-device proving: winterfell (memory hungry), risc0-zkp (server/offload), bellman Groth16 (trusted setup required).

### Phase 0 Deliverable — Research Document

Before writing any ZKP circuit code, produce a structured research document covering: (1) exact ZKP statements for each Priority 1 property with constraint count and proof size estimates, (2) minimal Halo2 proof built on dev machine with measured proof size, (3) QR compatibility test — serialise + zstd, confirm under 2KB, (4) performance baseline with ARM64 scaling projection, (5) recommendation confirming Mopro + Halo2 or recommending dalek with justification from measured data.

### Integration Path

Phase 1: Pure Rust library, tested on dev machine.
Phase 2: Add `ffi.rs` wrapping core library (thin wrapper only).
Phase 3: Compile to `.so` with cargo-ndk, commit to `rust-zkp-vm/prebuilt/`.
Phase 4: Wire Kotlin FFI declarations in app modules.
Phase 5: Migrate to Gradle-integrated build.

ABI targets: `arm64-v8a` (production), `x86_64` (emulator only).

Pre-built location: `rust-zkp-vm/prebuilt/` — Alice and Bob reference via `jniLibs` source set.

---

## Open Items — Required Before Coding

From `docs/design.md` Section 9:

1. **Capability flag registry** — bits 0–15 of the uint16 capability mask
2. **SAS wordlist** — selection and embedding of the 2,048-word EFF word list
3. **UI verification states** — visual treatment of Verified vs Unverified contacts

---

## Available Skills

These custom skills are available in this project. Use them — they are the correct expert lenses for this codebase.

| Command | Purpose |
|---|---|
| `/mc-crypto` | Cryptography — X25519, Ed25519, Double Ratchet, X3DH, BouncyCastle, Argon2id |
| `/mc-security` | Security review against the Miss Charming threat model |
| `/mc-android` | Android — Compose, MVI, Navigation 3, CameraX, Room, Koin |
| `/mc-protocol` | QR protocol and encoding |
| `/mc-threat` | STRIDE threat modelling |
| `/mc-test` | Test strategy — spec vectors, property tests, unit tests |
| `/mc-review` | Full holistic code review through all lenses simultaneously |
| `/mc-naming` | Naming decisions across all identifiers |
| `/mc-gradle` | Gradle build, modules, version catalog, ProGuard, NDK |
| `/mc-writing` | Technical writing and documentation standard |
| `/mc-design` | Compose theme, Material3, Alice/Bob design system — Rolls Royce standard |

---

## Repository

- **GitHub:** `tower-guidev2/wonderland`
- **Local:** `/Users/muttley/Miss_Charming/wonderland`
- **Design document:** `docs/design.md` (Draft 0.4)
- **Working directory for Miss Charming:** `/Users/muttley/Miss_Charming`
- **Test / reference project (Alice prototype):** `/Users/muttley/secret_squirrel/alice`
