# Dependencies & Documentation Registry

---

## Technology Stack

### All modules
- Kotlin 2.3+ (pure — no Java)
- `kotlinx-coroutines-android` — async
- `kotlinx-serialization-cbor` — CBOR encoding (not `co.nstant.in:cbor`)
- `koin-android` — DI (not Hilt, not Dagger)

### core:cryptography
- `org.bouncycastle:bcprov-jdk18on` — ALL crypto primitives. No other library does crypto.
- Primitives: X25519, Ed25519, HKDF-SHA256, ChaCha20-Poly1305, SHA-256, Argon2id
- Protocol: X3DH + Double Ratchet — pure Kotlin, no library
- Compression: `com.github.luben:zstd-jni` — Phase 1 only

### core:qr
- `com.google.zxing:core` — encode/decode (no Android dependency)

### Alice
- `androidx.room` — storage with BouncyCastle TypeConverters for column-level encryption at rest
- `androidx.camera:camera-camera2/lifecycle/view` — CameraX
- `androidx.navigation3:navigation3-runtime/ui:1.0.1` — Navigation 3 stable
- Jetpack Compose BOM
- `com.google.mlkit:barcode-scanning` — bundled only. Never `play-services-mlkit-barcode-scanning`.
- Custom `InputMethodService` keyboard — a-z, 0-9, space only. `FLAG_SECURE` set.

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

## Version Verification Rule

**Never write a version number from training memory.** Before declaring any dependency version:
1. Fetch from the appropriate source below
2. State the version found, release date, and source URL
3. Use latest stable unless there is explicit documented reason to do otherwise

| Library | Source |
|---|---|
| Kotlin, KSP, Compose, ML Kit | https://maven.google.com/web/index.html |
| Room, Koin, Arrow, Bouncy Castle, ZXing, Turbine, Molecule | https://central.sonatype.com/search |
| Jetpack releases | https://developer.android.com/jetpack/androidx/releases |
| Rust crates | https://crates.io |

---

## Documentation Registry

**Fetch docs before writing code. No exceptions — not for "simple" cases, not under time pressure.**

Workflow: identify libraries and language features the task uses → fetch the relevant URL(s) → read current API surface → write code. Never from training memory.

### Language & Compiler
- Kotlin reference: https://kotlinlang.org/docs/home.html
- Kotlin for Android: https://developer.android.com/kotlin
- KSP2 internals: https://github.com/google/ksp/blob/main/docs/ksp2.md
- Migrate kapt to KSP: https://developer.android.com/build/migrate-to-ksp

### UI
- Compose guide: https://developer.android.com/develop/ui/compose/documentation
- Compose BOM: https://developer.android.com/jetpack/androidx/releases/compose

### Persistence
- Room guide: https://developer.android.com/training/data-storage/room

### Dependency Injection
- Koin Android: https://insert-koin.io/docs/quickstart/android/
- Koin definitions: https://insert-koin.io/docs/reference/koin-core/definitions/

### Cryptography
- Bouncy Castle: https://www.bouncycastle.org/documentation/documentation-java/

### QR & Camera
- ML Kit barcode (bundled): https://developers.google.com/ml-kit/vision/barcode-scanning/android
- CameraX + ML Kit: https://developer.android.com/media/camera/camerax/mlkitanalyzer
- ZXing core: https://github.com/zxing/zxing/tree/master/core

### Build System
- AGP 9.0 release notes: https://developer.android.com/build/releases/agp-9-0-0-release-notes
- AGP 9.1 release notes: https://developer.android.com/build/releases/agp-9-1-0-release-notes
- Version catalogs: https://docs.gradle.org/current/userguide/version_catalogs.html
- Convention plugins: https://docs.gradle.org/current/userguide/implementing_gradle_plugins_convention.html
- Composite builds: https://docs.gradle.org/current/userguide/composite_builds.html

### Architecture
- App architecture: https://developer.android.com/topic/architecture
- Modularisation patterns: https://developer.android.com/topic/modularization/patterns
- Now in Android (gold standard): https://github.com/android/nowinandroid
- Molecule: https://github.com/cashapp/molecule
- Orbit MVI (Phase 1b reference): https://github.com/orbit-mvi/orbit-mvi

### Testing
- Testing fundamentals: https://developer.android.com/training/testing/fundamentals
- Coroutines testing: https://developer.android.com/kotlin/coroutines/test
- Turbine: https://github.com/cashapp/turbine
- Compose testing: https://developer.android.com/develop/ui/compose/testing

### Security
- R8 shrinking: https://developer.android.com/build/shrink-code

### ZKP & Rust
- Mopro: https://github.com/zkmopro/mopro
- Halo2 book: https://zcash.github.io/halo2/
- cargo-ndk (Phase 2 only): https://github.com/bbqsrc/cargo-ndk
- Android NDK guide (Phase 2 only): https://developer.android.com/ndk/guides
