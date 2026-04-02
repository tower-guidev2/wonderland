# POC 0005 Tweedledee — APK Certificate Fingerprint Verification

## Context

Miss Charming's security model requires Alice and Bob to verify each other's APK signing certificate at runtime before trusting QR payloads. A repackaged app with a different signing key must be rejected. This POC proves the mechanism: two apps exchange SHA-256 certificate fingerprints via QR code and verify them against build-time-pinned expected values.

This is one layer of the four-layer defence (cert fingerprint, StrongBox attestation, air-gap surveillance, ephemeral key exchange). No single layer is sufficient — combined, they require an attacker to defeat all four simultaneously.

## Architecture

Two minimal single-Activity Compose apps (`:alice`, `:bob`) sharing a common library (`:core`). A custom Gradle task in `build-logic/` extracts the peer's certificate fingerprint at build time and generates a Kotlin constant. At runtime, each app extracts its own fingerprint, displays it as a QR code, scans the peer's QR, and compares.

```
0005_tweedledee/
├── build-logic/
│   └── convention/
│       ├── build.gradle.kts
│       ├── settings.gradle.kts
│       └── src/main/kotlin/
│           └── GenerateCertificateFingerprintTask.kt
├── keystores/
│   ├── alice-debug.jks
│   └── bob-debug.jks
├── alice/                                        // :alice application module
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/org/alice/poc/tweedledee/
│           ├── activity/
│           │   └── MainActivity.kt
│           ├── composables/
│           │   ├── CertificateVerificationScreen.kt
│           │   ├── modifier/
│           │   │   └── SemanticsSealed.kt
│           │   └── theme/
│           │       ├── Color.kt
│           │       ├── Theme.kt
│           │       └── Type.kt
│           └── viewmodel/
│               └── CertificateVerificationViewModel.kt
├── bob/                                          // :bob application module
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/org/bob/poc/tweedledee/
│           ├── activity/
│           │   └── MainActivity.kt
│           ├── composables/
│           │   ├── CertificateVerificationScreen.kt
│           │   ├── modifier/
│           │   │   └── SemanticsSealed.kt
│           │   └── theme/
│           │       ├── Color.kt
│           │       ├── Theme.kt
│           │       └── Type.kt
│           └── viewmodel/
│               └── CertificateVerificationViewModel.kt
├── core/                                         // :core Android library module
│   ├── build.gradle.kts
│   └── src/main/kotlin/org/poc/tweedledee/core/
│       ├── CertificateExtractor.kt
│       ├── QrGenerator.kt
│       └── QrScanner.kt
├── lint-checks/                                  // :lint-checks JVM module
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/org/poc/tweedledee/lint/
│       │   ├── TweedledeeLintRegistry.kt
│       │   └── SemanticsNotSealedDetector.kt
│       └── test/kotlin/org/poc/tweedledee/lint/
│           └── SemanticsNotSealedDetectorTest.kt
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── gradle.properties
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

## Modules

### `:core` — Android library

Shared runtime logic. No UI. No generated code.

**`CertificateExtractor.kt`** — extracts the running app's signing certificate SHA-256 fingerprint via `PackageManager.GET_SIGNING_CERTIFICATES`. Returns `Either<CertificateError, Fingerprint>` where `Fingerprint` is a value class wrapping a hex string.

**`QrGenerator.kt`** — takes a `Fingerprint`, returns a `Bitmap` QR code via ZXing core `MultiFormatWriter` + `BarcodeFormat.QR_CODE`. Pure function, no Android UI dependency.

**`QrScanner.kt`** — wraps ML Kit bundled `BarcodeScanning` + CameraX `ImageAnalysis`. Exposes a `StateFlow<String?>` of the last scanned payload. Lifecycle-aware via CameraX lifecycle binding.

### `:alice` and `:bob` — Application modules

Near-identical structure. Differences:

| | Alice | Bob |
|---|---|---|
| Package | `org.alice.poc.tweedledee` | `org.bob.poc.tweedledee` |
| Application identifier | `org.alice.poc.tweedledee` | `org.bob.poc.tweedledee` |
| Signing keystore | `keystores/alice-debug.jks` | `keystores/bob-debug.jks` |
| Generated expected peer cert | Bob's fingerprint | Alice's fingerprint |

Each module contains:

- **`MainActivity.kt`** — single Activity, sets Compose content with theme. Creates ViewModel, requests camera permission.
- **`CertificateVerificationViewModel.kt`** — holds `StateFlow<VerificationScreenState>`. On init, extracts own fingerprint via `CertificateExtractor`. Exposes `onScanResult(fingerprint: String)` which compares against `ExpectedPeerCertificate.SHA256`.
- **`CertificateVerificationScreen.kt`** — single-screen Compose UI:
  - Top: QR code image of own fingerprint + truncated hex display
  - Middle: CameraX preview with ML Kit scan overlay
  - Bottom: verification status (WAITING / PASS / FAIL)
- **Theme** — minimal Material3 theme. Light/dark. Colours distinguish Alice (blue tones) from Bob (green tones) so operator knows which app is which at a glance.
- **`SemanticsSealed.kt`** — copied from POC 0000. Identical implementation.

### `:lint-checks` — JVM module

Copied from POC 0000 with package rename. `SemanticsNotSealedDetector` at ERROR severity. Wired to both `:alice` and `:bob` via `lintChecks(project(":lint-checks"))`.

## Build-Time Fingerprint Generation

### `GenerateCertificateFingerprintTask`

A Gradle task registered in `build-logic/convention/`. Not a convention plugin — a standalone task class registered directly in each app module's `build.gradle.kts`.

**Inputs:** keystore file path, keystore password, key alias.
**Output:** generated Kotlin file at `build/generated/peerCertificate/<package>/ExpectedPeerCertificate.kt`.

```kotlin
// Generated output shape
package org.alice.poc.tweedledee.generated

object ExpectedPeerCertificate {
    const val SHA256: String = "a1b2c3d4..."
}
```

The task uses `java.security.KeyStore` to load the peer's keystore, extract the certificate, and compute SHA-256. No BouncyCastle needed — JDK `MessageDigest` is sufficient for SHA-256 of a certificate.

The `build-logic/convention/settings.gradle.kts` must wire the version catalog:

```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}
```

**Wiring in app build.gradle.kts:**

```kotlin
val generatePeerCertificate by tasks.registering(GenerateCertificateFingerprintTask::class) {
    keystoreFile.set(rootProject.file("keystores/bob-debug.jks"))
    keystorePassword.set(providers.gradleProperty("bobKeystorePassword"))
    keyAlias.set("bob")
    outputDirectory.set(layout.buildDirectory.dir("generated/peerCertificate"))
    packageName.set("org.alice.poc.tweedledee.generated")
}

android.sourceSets["main"].kotlin.srcDir(generatePeerCertificate.map { it.outputDirectory })
```

### Keystores

Generated once via `keytool` before first build. Committed to the repo (debug-only keystores with known passwords — not secrets).

```bash
keytool -genkeypair -v -keystore keystores/alice-debug.jks \
  -alias alice -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass alicepass -keypass alicepass -dname "CN=Alice POC"

keytool -genkeypair -v -keystore keystores/bob-debug.jks \
  -alias bob -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass bobpass -keypass bobpass -dname "CN=Bob POC"
```

Passwords stored in `gradle.properties` (committed — these are throwaway debug keys):

```properties
aliceKeystorePassword=alicepass
bobKeystorePassword=bobpass
```

## Runtime Flow

1. App launches → `CertificateExtractor.extractOwnFingerprint(context)` returns `Either.Right(Fingerprint("a1b2c3..."))`
2. ViewModel generates QR bitmap via `QrGenerator.generate(fingerprint)`
3. UI displays QR code + hex string
4. User grants camera permission → CameraX preview starts → `QrScanner` begins image analysis
5. ML Kit detects QR → extracts hex string → `QrScanner.scannedPayload` emits value
6. ViewModel compares `scannedPayload == ExpectedPeerCertificate.SHA256`
7. UI shows PASS (green) or FAIL (red)

## State Model

```kotlin
sealed interface VerificationScreenState {
    data class Loading(val message: String) : VerificationScreenState
    data class Ready(
        val ownFingerprint: Fingerprint,
        val qrBitmap: Bitmap,
        val verificationResult: VerificationResult,
    ) : VerificationScreenState
    data class Error(val error: CertificateError) : VerificationScreenState
}

sealed interface VerificationResult {
    data object Waiting : VerificationResult
    data object Pass : VerificationResult
    data class Fail(val scannedFingerprint: String) : VerificationResult
}

@JvmInline
value class Fingerprint(val hexValue: String)

sealed interface CertificateError {
    data object NoSigningInfo : CertificateError
    data class ExtractionFailed(val reason: String) : CertificateError
}
```

## Version Catalog (`gradle/libs.versions.toml`)

Standalone catalog for this POC. Versions match wonderland's catalog exactly.

New entries beyond POC 0000's catalog:

| Entry | Purpose |
|---|---|
| `cameraX = "1.6.0"` | CameraX camera2, lifecycle, view |
| `mlkitBarcode = "17.3.0"` | ML Kit bundled barcode scanning |
| `zxing = "3.5.4"` | ZXing core for QR generation |

Plus `android-library` plugin entry for the `:core` module.

## settings.gradle.kts

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "poc-0005-tweedledee"
include(":alice")
include(":bob")
include(":core")
include(":lint-checks")
```

## Permissions

### Alice and Bob modules (identical)

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

Neither app requests `INTERNET` or any other network permission.

## Negative Test Procedure

Re-sign Alice's APK with a rogue key:

```bash
keytool -genkeypair -v -keystore keystores/evil.jks \
  -alias evil -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass evilpass -keypass evilpass -dname "CN=Evil Alice"
```

Rebuild `:alice` using `evil.jks`. Bob scans → fingerprint mismatch → FAIL.

## Constraints

- Kotlin only. No Java.
- Jetpack Compose only. No XML layouts.
- minSdk 33 (project standard).
- `semanticsSealed()` on all Modifier chains. Lint-enforced at ERROR.
- No `!!`, no `lateinit`, no `apply`, no `companion object` — all quality rules from `docs/rules/quality.md`.
- Arrow `Either` for all error returns.
- Value classes for domain types (`Fingerprint`).
- No network permissions.
- No convention plugin IDs from wonderland — this POC has its own build-logic with just the task class.

## Verification Plan

1. Generate keystores → build both apps → install on two devices
2. Both apps display QR codes → point cameras at each other → both show PASS
3. Rebuild Alice with `evil.jks` → Bob scans → FAIL
4. Lint check: remove `semanticsSealed()` from a Modifier chain → build fails
5. Unit test: `CertificateExtractor` returns `Either.Left` for missing signing info (Robolectric)
6. Unit test: `QrGenerator` produces a decodable QR bitmap (JVM + ZXing decoder)
7. Unit test: verification logic — matching fingerprints → PASS, mismatched → FAIL
