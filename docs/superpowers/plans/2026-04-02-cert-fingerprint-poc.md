# POC 0005 Tweedledee — Certificate Fingerprint Verification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Two Android apps (Alice and Bob) exchange and verify each other's APK signing certificate fingerprints via QR code, rejecting repackaged counterparts.

**Architecture:** Multi-module standalone Gradle project under `wonderland/poc/0005_tweedledee/`. `:alice` and `:bob` are application modules. `:core` is an Android library with shared runtime logic. `:lint-checks` enforces `semanticsSealed()`. A custom Gradle task in `build-logic/` generates peer certificate fingerprint constants at build time.

**Tech Stack:** Kotlin 2.3.20, AGP 9.1.0, Compose BOM 2026.03.01, CameraX 1.6.0, ML Kit Barcode 17.3.0, ZXing Core 3.5.4, Arrow Core 2.2.2.1

**Spec:** `docs/superpowers/specs/2026-04-02-cert-fingerprint-poc-design.md`

**Reference POC:** `poc/0000_alice_broadcast_receiver/` — follow its patterns for settings, build files, lint-checks, theme, and SemanticsSealed.

**Project root:** `wonderland/poc/0005_tweedledee/`

**Quality rules (non-negotiable — apply to EVERY .kt file):**
- No `!!`, no `lateinit`, no `apply`, no `companion object` (unless framework-forced like Lint), no comments/KDoc, no wildcard imports, no magic numbers/strings, no backtick function names, no abbreviations, no `var` on class properties, no `runBlocking` in tests, no `@Deprecated`, no builder pattern, no `protected`, no abstract classes (unless framework-forced), no negative-logic names
- Arrow `Either` for all errors — sealed error hierarchy per domain boundary
- Value classes for all domain function arguments — never raw types
- Max 3 parameters per function — overflow grouped into value objects
- `sealed interface` by default, `fun interface` for single-method, `enum class` for uniform variants
- `when` on sealed types: always expression, always exhaustive, no `else`
- `val` everywhere, `var` only local loop accumulators
- Explicit return types on public/internal functions
- Functions start with a verb. Booleans use `is`/`has`/`can`/`should` prefix
- `.not()` not `!` for negation
- 180-char lines. 4-space indent. K&R braces. Trailing commas everywhere
- Single-line `if`/`else` on separate lines, no braces, properly indented. Positive case first
- String templates always. Import to leaf level. Acronyms treated as words
- One public type per file. Sealed families may share
- `semanticsSealed()` as first modifier in every Modifier chain — lint-enforced at ERROR
- Float literals: `1.0F` not `1f`. Always dot, always uppercase `F`
- Every composable needs multiple @Preview annotations — Pixel device, light and dark
- Composables in `composables` package, ViewModels in `viewmodel` package, Activities in `activity` package
- No deprecated APIs anywhere
- Zero warnings on every commit

---

### Task 1: Gradle Scaffold

Create the project root with all Gradle infrastructure files.

**Files:**
- Create: `poc/0005_tweedledee/settings.gradle.kts`
- Create: `poc/0005_tweedledee/gradle.properties`
- Create: `poc/0005_tweedledee/gradle/libs.versions.toml`
- Create: `poc/0005_tweedledee/gradle/gradle-daemon-jvm.properties`
- Copy: `poc/0005_tweedledee/gradle/wrapper/` (from POC 0000)
- Copy: `poc/0005_tweedledee/gradlew` (from POC 0000)
- Copy: `poc/0005_tweedledee/gradlew.bat` (from POC 0000)
- Create: `poc/0005_tweedledee/build.gradle.kts` (root — empty, no plugins)

- [ ] **Step 1: Create `settings.gradle.kts`**

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

- [ ] **Step 2: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
aliceKeystorePassword=alicepass
bobKeystorePassword=bobpass
```

- [ ] **Step 3: Create `gradle/libs.versions.toml`**

```toml
[versions]
agp                     = "9.1.0"
kotlin                  = "2.3.20"
compileSdk              = "36"
targetSdk               = "36"
minSdk                  = "33"

coreKtx                 = "1.18.0"
lifecycleRuntimeKtx     = "2.10.0"
activityCompose         = "1.13.0"
composeBom              = "2026.03.01"

kotlinxCoroutines       = "1.10.2"

arrowCore               = "2.2.2.1"

cameraX                 = "1.6.0"
mlkitBarcode            = "17.3.0"
zxing                   = "3.5.4"

lint                    = "32.1.0"
junit                   = "4.13.2"
assertk                 = "0.28.1"
robolectric             = "4.16.1"

[libraries]
androidx-core-ktx                   = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx      = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose           = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }

androidx-compose-bom                = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui                 = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics        = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling         = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3          = { group = "androidx.compose.material3", name = "material3" }

androidx-camera-camera2             = { module = "androidx.camera:camera-camera2", version.ref = "cameraX" }
androidx-camera-lifecycle           = { module = "androidx.camera:camera-lifecycle", version.ref = "cameraX" }
androidx-camera-view                = { module = "androidx.camera:camera-view", version.ref = "cameraX" }

mlkit-barcode                       = { module = "com.google.mlkit:barcode-scanning", version.ref = "mlkitBarcode" }

zxing-core                          = { module = "com.google.zxing:core", version.ref = "zxing" }

kotlinx-coroutines-android          = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-core             = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }

arrow-core                          = { module = "io.arrow-kt:arrow-core", version.ref = "arrowCore" }

lint-api                            = { module = "com.android.tools.lint:lint-api", version.ref = "lint" }
lint-checks                         = { module = "com.android.tools.lint:lint-checks", version.ref = "lint" }
lint-tests                          = { module = "com.android.tools.lint:lint-tests", version.ref = "lint" }
junit                               = { module = "junit:junit", version.ref = "junit" }
assertk                             = { module = "com.willowtreeapps.assertk:assertk", version.ref = "assertk" }
robolectric                         = { module = "org.robolectric:robolectric", version.ref = "robolectric" }

[plugins]
android-application         = { id = "com.android.application", version.ref = "agp" }
android-library             = { id = "com.android.library", version.ref = "agp" }
android-lint                = { id = "com.android.lint", version.ref = "agp" }
kotlin-jvm                  = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose              = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 4: Copy Gradle wrapper and daemon JVM properties from POC 0000**

```bash
cd /Users/muttley/Miss_Charming/wonderland
mkdir -p poc/0005_tweedledee/gradle/wrapper
cp poc/0000_alice_broadcast_receiver/gradle/wrapper/gradle-wrapper.properties poc/0005_tweedledee/gradle/wrapper/
cp poc/0000_alice_broadcast_receiver/gradle/wrapper/gradle-wrapper.jar poc/0005_tweedledee/gradle/wrapper/
cp poc/0000_alice_broadcast_receiver/gradlew poc/0005_tweedledee/
cp poc/0000_alice_broadcast_receiver/gradlew.bat poc/0005_tweedledee/
cp poc/0000_alice_broadcast_receiver/gradle/gradle-daemon-jvm.properties poc/0005_tweedledee/gradle/
chmod +x poc/0005_tweedledee/gradlew
```

- [ ] **Step 5: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.lint) apply false
}
```

- [ ] **Step 6: Commit**

```bash
git add poc/0005_tweedledee/settings.gradle.kts poc/0005_tweedledee/gradle.properties \
  poc/0005_tweedledee/gradle/ poc/0005_tweedledee/gradlew poc/0005_tweedledee/gradlew.bat \
  poc/0005_tweedledee/build.gradle.kts
git commit -m "scaffold: poc-0005-tweedledee gradle infrastructure"
```

---

### Task 2: build-logic — Certificate Fingerprint Gradle Task

Create the `build-logic/` composite build containing `GenerateCertificateFingerprintTask`.

**Files:**
- Create: `poc/0005_tweedledee/build-logic/settings.gradle.kts`
- Create: `poc/0005_tweedledee/build-logic/convention/build.gradle.kts`
- Create: `poc/0005_tweedledee/build-logic/convention/src/main/kotlin/GenerateCertificateFingerprintTask.kt`

- [ ] **Step 1: Create `build-logic/settings.gradle.kts`**

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
```

- [ ] **Step 2: Create `build-logic/convention/build.gradle.kts`**

```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}
```

Add the Gradle plugin artifacts to the version catalog. Edit `gradle/libs.versions.toml` to add in the `[libraries]` section:

```toml
android-gradlePlugin                = { module = "com.android.tools.build:gradle", version.ref = "agp" }
kotlin-gradlePlugin                 = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
```

- [ ] **Step 3: Create `GenerateCertificateFingerprintTask.kt`**

```kotlin
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest

@CacheableTask
abstract class GenerateCertificateFingerprintTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val keystoreFile: Property<File>

    @get:Input
    abstract val keystorePassword: Property<String>

    @get:Input
    abstract val keyAlias: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val keystore = KeyStore.getInstance("JKS")
        keystoreFile.get().inputStream().use { stream ->
            keystore.load(stream, keystorePassword.get().toCharArray())
        }

        val certificate = keystore.getCertificate(keyAlias.get())
        val fingerprint = MessageDigest.getInstance("SHA-256")
            .digest(certificate.encoded)
            .joinToString("") { "%02x".format(it) }

        val packageDir = packageName.get().replace('.', '/')
        val outputDir = File(outputDirectory.asFile.get(), packageDir)
        outputDir.mkdirs()

        File(outputDir, "ExpectedPeerCertificate.kt").writeText(
            """
            |package ${packageName.get()}
            |
            |object ExpectedPeerCertificate {
            |    const val SHA256: String = "$fingerprint"
            |}
            """.trimMargin(),
        )
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add poc/0005_tweedledee/build-logic/ poc/0005_tweedledee/gradle/libs.versions.toml
git commit -m "feat: add build-logic with GenerateCertificateFingerprintTask"
```

---

### Task 3: Generate Keystores

Generate the Alice and Bob debug keystores using `keytool`.

**Files:**
- Create: `poc/0005_tweedledee/keystores/alice-debug.jks`
- Create: `poc/0005_tweedledee/keystores/bob-debug.jks`

- [ ] **Step 1: Create keystores directory and generate both keystores**

```bash
cd /Users/muttley/Miss_Charming/wonderland
mkdir -p poc/0005_tweedledee/keystores

keytool -genkeypair -v -keystore poc/0005_tweedledee/keystores/alice-debug.jks \
  -alias alice -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass alicepass -keypass alicepass \
  -dname "CN=Alice POC"

keytool -genkeypair -v -keystore poc/0005_tweedledee/keystores/bob-debug.jks \
  -alias bob -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass bobpass -keypass bobpass \
  -dname "CN=Bob POC"
```

- [ ] **Step 2: Verify keystores**

```bash
keytool -list -keystore poc/0005_tweedledee/keystores/alice-debug.jks -storepass alicepass
keytool -list -keystore poc/0005_tweedledee/keystores/bob-debug.jks -storepass bobpass
```

Expected: each shows one entry with alias `alice` / `bob` respectively.

- [ ] **Step 3: Commit**

```bash
git add poc/0005_tweedledee/keystores/
git commit -m "feat: add alice and bob debug keystores"
```

---

### Task 4: lint-checks Module

Copy the lint-checks module from POC 0000 with package rename to `org.poc.tweedledee.lint`.

**Files:**
- Create: `poc/0005_tweedledee/lint-checks/build.gradle.kts`
- Create: `poc/0005_tweedledee/lint-checks/src/main/kotlin/org/poc/tweedledee/lint/TweedledeeLintRegistry.kt`
- Create: `poc/0005_tweedledee/lint-checks/src/main/kotlin/org/poc/tweedledee/lint/SemanticsNotSealedDetector.kt`
- Create: `poc/0005_tweedledee/lint-checks/src/test/kotlin/org/poc/tweedledee/lint/SemanticsNotSealedDetectorTest.kt`
- Create: `poc/0005_tweedledee/lint-checks/src/main/resources/META-INF/services/com.android.tools.lint.client.api.IssueRegistry`

- [ ] **Step 1: Create `lint-checks/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.android.lint)
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)

    testImplementation(libs.lint.api)
    testImplementation(libs.lint.checks)
    testImplementation(libs.lint.tests)
    testImplementation(libs.junit)
}
```

- [ ] **Step 2: Create `TweedledeeLintRegistry.kt`**

```kotlin
package org.poc.tweedledee.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

// Lint API requires extending abstract class IssueRegistry — framework constraint.
class TweedledeeLintRegistry : IssueRegistry() {

    override val issues: List<Issue> = listOf(
        SemanticsNotSealedDetector.ISSUE,
    )

    override val api: Int = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "Tweedledee Certificate Security",
        identifier = "org.poc.tweedledee.lint",
    )
}
```

- [ ] **Step 3: Create `SemanticsNotSealedDetector.kt`**

Copy verbatim from `poc/0000_alice_broadcast_receiver/lint-checks/src/main/kotlin/org/alice/lint/SemanticsNotSealedDetector.kt`, changing only the package declaration:

```kotlin
package org.poc.tweedledee.lint
```

All other code identical — same imports, same class body, same companion object (framework-forced).

- [ ] **Step 4: Create META-INF service registration**

Create file `lint-checks/src/main/resources/META-INF/services/com.android.tools.lint.client.api.IssueRegistry`:

```
org.poc.tweedledee.lint.TweedledeeLintRegistry
```

- [ ] **Step 5: Create `SemanticsNotSealedDetectorTest.kt`**

Copy from `poc/0000_alice_broadcast_receiver/lint-checks/src/test/kotlin/org/alice/lint/SemanticsNotSealedDetectorTest.kt` with package rename:

```kotlin
package org.poc.tweedledee.lint
```

And update the `extensionStub` package reference from `org.alice.poc.airgap.composables.modifier` to `org.poc.tweedledee.modifier` and all test file package references from `org.alice.poc.airgap.composables` to `org.poc.tweedledee.composables`. Update import references in test source strings accordingly.

- [ ] **Step 6: Run lint-checks tests**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :lint-checks:test
```

Expected: all 7 tests pass.

- [ ] **Step 7: Commit**

```bash
git add poc/0005_tweedledee/lint-checks/
git commit -m "feat: add lint-checks module with SemanticsNotSealedDetector"
```

---

### Task 5: :core Module — Domain Types and CertificateExtractor

Create the `:core` Android library with domain types and the certificate extraction logic.

**Files:**
- Create: `poc/0005_tweedledee/core/build.gradle.kts`
- Create: `poc/0005_tweedledee/core/src/main/AndroidManifest.xml`
- Create: `poc/0005_tweedledee/core/src/main/kotlin/org/poc/tweedledee/core/Fingerprint.kt`
- Create: `poc/0005_tweedledee/core/src/main/kotlin/org/poc/tweedledee/core/CertificateError.kt`
- Create: `poc/0005_tweedledee/core/src/main/kotlin/org/poc/tweedledee/core/CertificateExtractor.kt`
- Test: `poc/0005_tweedledee/core/src/test/kotlin/org/poc/tweedledee/core/CertificateExtractorTest.kt`

- [ ] **Step 1: Create `core/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.poc.tweedledee.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.arrow.core)
    implementation(libs.zxing.core)
    implementation(libs.mlkit.barcode)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.assertk)
    testImplementation(libs.robolectric)
}
```

- [ ] **Step 2: Create `core/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] **Step 3: Create `Fingerprint.kt`**

```kotlin
package org.poc.tweedledee.core

@JvmInline
value class Fingerprint(val hexValue: String)
```

- [ ] **Step 4: Create `CertificateError.kt`**

```kotlin
package org.poc.tweedledee.core

sealed interface CertificateError {
    data object NoSigningInfo : CertificateError
    data class ExtractionFailed(val reason: String) : CertificateError
}
```

- [ ] **Step 5: Write failing test for `CertificateExtractor`**

```kotlin
package org.poc.tweedledee.core

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.matches
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CertificateExtractorTest {

    @Test
    fun extractOwnFingerprint_returnsHexString() {
        val context = RuntimeEnvironment.getApplication()
        val result = CertificateExtractor.extractOwnFingerprint(context)

        assertThat(result).isInstanceOf<Either.Right<Fingerprint>>()
        val fingerprint = (result as Either.Right).value
        assertThat(fingerprint.hexValue).isNotEmpty()
        assertThat(fingerprint.hexValue).matches(Regex("[0-9a-f]{64}"))
    }
}
```

- [ ] **Step 6: Run test to verify it fails**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :core:testDebugUnitTest --tests "org.poc.tweedledee.core.CertificateExtractorTest"
```

Expected: FAIL — `CertificateExtractor` does not exist.

- [ ] **Step 7: Create `CertificateExtractor.kt`**

```kotlin
package org.poc.tweedledee.core

import android.content.Context
import android.content.pm.PackageManager
import arrow.core.Either
import java.security.MessageDigest

object CertificateExtractor {

    fun extractOwnFingerprint(context: Context): Either<CertificateError, Fingerprint> =
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )

            val signingInfo = packageInfo.signingInfo
                ?: return Either.Left(CertificateError.NoSigningInfo)

            val certificate = if (signingInfo.hasMultipleSigners())
                signingInfo.apkContentsSigners[0]
            else
                signingInfo.signingCertificateHistory[0]

            val hexFingerprint = MessageDigest.getInstance("SHA-256")
                .digest(certificate.toByteArray())
                .joinToString("") { "%02x".format(it) }

            Either.Right(Fingerprint(hexFingerprint))
        } catch (exception: Exception) {
            Either.Left(CertificateError.ExtractionFailed(exception.message ?: "Unknown error"))
        }
}
```

- [ ] **Step 8: Run test to verify it passes**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :core:testDebugUnitTest --tests "org.poc.tweedledee.core.CertificateExtractorTest"
```

Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add poc/0005_tweedledee/core/
git commit -m "feat: add :core module with CertificateExtractor and domain types"
```

---

### Task 6: :core Module — QrGenerator

Add QR code generation to the `:core` module.

**Files:**
- Create: `poc/0005_tweedledee/core/src/main/kotlin/org/poc/tweedledee/core/QrGenerator.kt`
- Test: `poc/0005_tweedledee/core/src/test/kotlin/org/poc/tweedledee/core/QrGeneratorTest.kt`

- [ ] **Step 1: Write failing test for `QrGenerator`**

```kotlin
package org.poc.tweedledee.core

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val SAMPLE_FINGERPRINT = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"

@RunWith(RobolectricTestRunner::class)
class QrGeneratorTest {

    @Test
    fun generate_producesDecodableBitmap() {
        val fingerprint = Fingerprint(SAMPLE_FINGERPRINT)
        val bitmap = QrGenerator.generate(fingerprint)

        assertThat(bitmap.width).isGreaterThan(0)
        assertThat(bitmap.height).isGreaterThan(0)

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val decoded = MultiFormatReader().decode(binaryBitmap)

        assertThat(decoded.text).isEqualTo(SAMPLE_FINGERPRINT)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :core:testDebugUnitTest --tests "org.poc.tweedledee.core.QrGeneratorTest"
```

Expected: FAIL — `QrGenerator` does not exist.

- [ ] **Step 3: Create `QrGenerator.kt`**

```kotlin
package org.poc.tweedledee.core

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

private const val QR_SIZE = 512

object QrGenerator {

    fun generate(fingerprint: Fingerprint): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(
            fingerprint.hexValue,
            BarcodeFormat.QR_CODE,
            QR_SIZE,
            QR_SIZE,
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix.get(x, y))
                    android.graphics.Color.BLACK
                else
                    android.graphics.Color.WHITE
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :core:testDebugUnitTest --tests "org.poc.tweedledee.core.QrGeneratorTest"
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add poc/0005_tweedledee/core/src/main/kotlin/org/poc/tweedledee/core/QrGenerator.kt \
  poc/0005_tweedledee/core/src/test/kotlin/org/poc/tweedledee/core/QrGeneratorTest.kt
git commit -m "feat: add QrGenerator with round-trip decode test"
```

---

### Task 7: :core Module — QrScanner

Add QR scanning via CameraX + ML Kit to the `:core` module.

**Files:**
- Create: `poc/0005_tweedledee/core/src/main/kotlin/org/poc/tweedledee/core/QrScanner.kt`

- [ ] **Step 1: Create `QrScanner.kt`**

This is a CameraX + ML Kit integration class. No unit test — requires camera hardware. Verified via on-device testing in the verification plan.

```kotlin
package org.poc.tweedledee.core

import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

private const val IMAGE_WIDTH = 1280
private const val IMAGE_HEIGHT = 720

class QrScanner {

    private val _scannedPayload = MutableStateFlow<String?>(null)
    val scannedPayload: StateFlow<String?> = _scannedPayload.asStateFlow()

    private val scanner = BarcodeScanning.getClient()
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    @OptIn(ExperimentalGetImage::class)
    fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
    ) {
        val preview = Preview.Builder()
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(IMAGE_WIDTH, IMAGE_HEIGHT))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return@setAnalyzer
            }

            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees,
            )

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull { it.valueType == Barcode.TYPE_TEXT }
                        ?.rawValue
                        ?.let { _scannedPayload.value = it }
                }
                .addOnCompleteListener { imageProxy.close() }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis,
        )
    }

    fun shutdown() {
        scanner.close()
        analysisExecutor.shutdown()
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add poc/0005_tweedledee/core/src/main/kotlin/org/poc/tweedledee/core/QrScanner.kt
git commit -m "feat: add QrScanner with CameraX and ML Kit integration"
```

---

### Task 8: :alice Module — Build, Theme, SemanticsSealed

Create the `:alice` application module with build configuration, theme, and SemanticsSealed.

**Files:**
- Create: `poc/0005_tweedledee/alice/build.gradle.kts`
- Create: `poc/0005_tweedledee/alice/src/main/AndroidManifest.xml`
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/composables/modifier/SemanticsSealed.kt`
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/composables/theme/Color.kt`
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/composables/theme/Type.kt`
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/composables/theme/Theme.kt`

- [ ] **Step 1: Create `alice/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.alice.poc.tweedledee"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.alice.poc.tweedledee"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("aliceDebug") {
            storeFile = rootProject.file("keystores/alice-debug.jks")
            storePassword = providers.gradleProperty("aliceKeystorePassword").get()
            keyAlias = "alice"
            keyPassword = providers.gradleProperty("aliceKeystorePassword").get()
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("aliceDebug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("aliceDebug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val generatePeerCertificate by tasks.registering(GenerateCertificateFingerprintTask::class) {
    keystoreFile.set(rootProject.file("keystores/bob-debug.jks"))
    keystorePassword.set(providers.gradleProperty("bobKeystorePassword"))
    keyAlias.set("bob")
    packageName.set("org.alice.poc.tweedledee.generated")
    outputDirectory.set(layout.buildDirectory.dir("generated/peerCertificate"))
}

android.sourceSets["main"].kotlin.srcDir(generatePeerCertificate.map { it.outputDirectory })

dependencies {
    implementation(project(":core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.arrow.core)
    implementation(libs.kotlinx.coroutines.android)

    lintChecks(project(":lint-checks"))

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
}
```

- [ ] **Step 2: Create `alice/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.CAMERA" />

    <uses-feature android:name="android.hardware.camera" android:required="true" />

    <application
        android:label="POC 0005 Alice"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <activity
            android:name=".activity.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

- [ ] **Step 3: Create `SemanticsSealed.kt`**

Copy verbatim from `poc/0000_alice_broadcast_receiver/src/main/kotlin/org/alice/poc/airgap/composables/modifier/SemanticsSealed.kt`, changing only the package:

```kotlin
package org.alice.poc.tweedledee.composables.modifier
```

All other code identical.

- [ ] **Step 4: Create `Color.kt`** (Alice — blue tones)

```kotlin
package org.alice.poc.tweedledee.composables.theme

import androidx.compose.ui.graphics.Color

val AlicePrimaryLight = Color(0xFF1565C0)
val AliceOnPrimaryLight = Color(0xFFFFFFFF)
val AlicePrimaryContainerLight = Color(0xFFD1E4FF)
val AliceOnPrimaryContainerLight = Color(0xFF001D36)
val AliceSecondaryLight = Color(0xFF535F70)
val AliceOnSecondaryLight = Color(0xFFFFFFFF)
val AliceBackgroundLight = Color(0xFFFDFBFF)
val AliceOnBackgroundLight = Color(0xFF1A1C1E)
val AliceSurfaceLight = Color(0xFFFDFBFF)
val AliceOnSurfaceLight = Color(0xFF1A1C1E)
val AliceErrorLight = Color(0xFFBA1A1A)
val AliceOnErrorLight = Color(0xFFFFFFFF)

val AlicePrimaryDark = Color(0xFF9ECAFF)
val AliceOnPrimaryDark = Color(0xFF003258)
val AlicePrimaryContainerDark = Color(0xFF00497D)
val AliceOnPrimaryContainerDark = Color(0xFFD1E4FF)
val AliceSecondaryDark = Color(0xFFBBC7DB)
val AliceOnSecondaryDark = Color(0xFF253140)
val AliceBackgroundDark = Color(0xFF1A1C1E)
val AliceOnBackgroundDark = Color(0xFFE2E2E6)
val AliceSurfaceDark = Color(0xFF1A1C1E)
val AliceOnSurfaceDark = Color(0xFFE2E2E6)
val AliceErrorDark = Color(0xFFFFB4AB)
val AliceOnErrorDark = Color(0xFF690005)

val VerificationPassColor = Color(0xFF2E7D32)
val VerificationFailColor = Color(0xFFC62828)
val VerificationWaitingColor = Color(0xFF757575)
```

- [ ] **Step 5: Create `Type.kt`**

```kotlin
package org.alice.poc.tweedledee.composables.theme

import androidx.compose.material3.Typography

val TweedledeeTypography = Typography()
```

- [ ] **Step 6: Create `Theme.kt`**

```kotlin
package org.alice.poc.tweedledee.composables.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AliceLightScheme = lightColorScheme(
    primary = AlicePrimaryLight,
    onPrimary = AliceOnPrimaryLight,
    primaryContainer = AlicePrimaryContainerLight,
    onPrimaryContainer = AliceOnPrimaryContainerLight,
    secondary = AliceSecondaryLight,
    onSecondary = AliceOnSecondaryLight,
    background = AliceBackgroundLight,
    onBackground = AliceOnBackgroundLight,
    surface = AliceSurfaceLight,
    onSurface = AliceOnSurfaceLight,
    error = AliceErrorLight,
    onError = AliceOnErrorLight,
)

private val AliceDarkScheme = darkColorScheme(
    primary = AlicePrimaryDark,
    onPrimary = AliceOnPrimaryDark,
    primaryContainer = AlicePrimaryContainerDark,
    onPrimaryContainer = AliceOnPrimaryContainerDark,
    secondary = AliceSecondaryDark,
    onSecondary = AliceOnSecondaryDark,
    background = AliceBackgroundDark,
    onBackground = AliceOnBackgroundDark,
    surface = AliceSurfaceDark,
    onSurface = AliceOnSurfaceDark,
    error = AliceErrorDark,
    onError = AliceOnErrorDark,
)

@Composable
fun AliceTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme)
        AliceDarkScheme
    else
        AliceLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TweedledeeTypography,
        content = content,
    )
}
```

- [ ] **Step 7: Commit**

```bash
git add poc/0005_tweedledee/alice/
git commit -m "feat: add :alice module scaffold with build config, theme, and semanticsSealed"
```

---

### Task 9: :alice Module — ViewModel and Screen

Add the ViewModel, screen composable, and Activity to complete the `:alice` module.

**Files:**
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/viewmodel/VerificationScreenState.kt`
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/viewmodel/CertificateVerificationViewModel.kt`
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/composables/CertificateVerificationScreen.kt`
- Create: `poc/0005_tweedledee/alice/src/main/kotlin/org/alice/poc/tweedledee/activity/MainActivity.kt`

- [ ] **Step 1: Create `VerificationScreenState.kt`**

```kotlin
package org.alice.poc.tweedledee.viewmodel

import android.graphics.Bitmap
import org.poc.tweedledee.core.CertificateError
import org.poc.tweedledee.core.Fingerprint

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
```

- [ ] **Step 2: Create `CertificateVerificationViewModel.kt`**

```kotlin
package org.alice.poc.tweedledee.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.alice.poc.tweedledee.generated.ExpectedPeerCertificate
import org.poc.tweedledee.core.CertificateExtractor
import org.poc.tweedledee.core.QrGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// AndroidViewModel required for Application context — framework constraint.
class CertificateVerificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<VerificationScreenState>(
        VerificationScreenState.Loading("Extracting certificate..."),
    )
    val state: StateFlow<VerificationScreenState> = _state.asStateFlow()

    init {
        val result = CertificateExtractor.extractOwnFingerprint(application)
        result.fold(
            ifLeft = { error ->
                _state.value = VerificationScreenState.Error(error)
            },
            ifRight = { fingerprint ->
                val qrBitmap = QrGenerator.generate(fingerprint)
                _state.value = VerificationScreenState.Ready(
                    ownFingerprint = fingerprint,
                    qrBitmap = qrBitmap,
                    verificationResult = VerificationResult.Waiting,
                )
            },
        )
    }

    fun onScanResult(scannedFingerprint: String) {
        val current = _state.value
        if (current is VerificationScreenState.Ready) {
            val verificationResult = if (scannedFingerprint == ExpectedPeerCertificate.SHA256)
                VerificationResult.Pass
            else
                VerificationResult.Fail(scannedFingerprint)

            _state.value = current.copy(verificationResult = verificationResult)
        }
    }
}
```

- [ ] **Step 3: Create `CertificateVerificationScreen.kt`**

```kotlin
package org.alice.poc.tweedledee.composables

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.alice.poc.tweedledee.composables.modifier.semanticsSealed
import org.alice.poc.tweedledee.composables.theme.AliceTheme
import org.alice.poc.tweedledee.composables.theme.VerificationFailColor
import org.alice.poc.tweedledee.composables.theme.VerificationPassColor
import org.alice.poc.tweedledee.composables.theme.VerificationWaitingColor
import org.alice.poc.tweedledee.viewmodel.VerificationResult
import org.alice.poc.tweedledee.viewmodel.VerificationScreenState
import org.poc.tweedledee.core.Fingerprint

private val CONTENT_PADDING = 16.dp
private val QR_SIZE = 200.dp
private val SECTION_SPACING = 16.dp
private val CAMERA_PREVIEW_HEIGHT = 200.dp
private const val FINGERPRINT_DISPLAY_LENGTH = 16

@Composable
fun CertificateVerificationScreen(
    state: VerificationScreenState,
    previewViewFactory: (() -> PreviewView)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.semanticsSealed().fillMaxSize(),
    ) {
        when (state) {
            is VerificationScreenState.Loading -> LoadingContent(
                message = state.message,
            )
            is VerificationScreenState.Ready -> ReadyContent(
                state = state,
                previewViewFactory = previewViewFactory,
            )
            is VerificationScreenState.Error -> ErrorContent(
                error = state.error,
            )
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        modifier = Modifier.semanticsSealed().fillMaxSize().padding(CONTENT_PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ReadyContent(
    state: VerificationScreenState.Ready,
    previewViewFactory: (() -> PreviewView)?,
) {
    Column(
        modifier = Modifier.semanticsSealed().fillMaxSize().padding(CONTENT_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Alice",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.semanticsSealed().height(SECTION_SPACING))

        Image(
            bitmap = state.qrBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.semanticsSealed().size(QR_SIZE),
        )

        Spacer(modifier = Modifier.semanticsSealed().height(SECTION_SPACING))

        Text(
            text = "SHA: ${state.ownFingerprint.hexValue.take(FINGERPRINT_DISPLAY_LENGTH)}...",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.semanticsSealed().height(SECTION_SPACING))

        HorizontalDivider(modifier = Modifier.semanticsSealed().fillMaxWidth())

        Spacer(modifier = Modifier.semanticsSealed().height(SECTION_SPACING))

        if (previewViewFactory != null) {
            AndroidView(
                factory = { previewViewFactory() },
                modifier = Modifier.semanticsSealed().fillMaxWidth().height(CAMERA_PREVIEW_HEIGHT),
            )
        }

        Spacer(modifier = Modifier.semanticsSealed().height(SECTION_SPACING))

        HorizontalDivider(modifier = Modifier.semanticsSealed().fillMaxWidth())

        Spacer(modifier = Modifier.semanticsSealed().height(SECTION_SPACING))

        VerificationStatusText(result = state.verificationResult)
    }
}

@Composable
private fun VerificationStatusText(result: VerificationResult) {
    val statusText = when (result) {
        is VerificationResult.Waiting -> "WAITING"
        is VerificationResult.Pass -> "PASS"
        is VerificationResult.Fail -> "FAIL"
    }

    val statusColor = when (result) {
        is VerificationResult.Waiting -> VerificationWaitingColor
        is VerificationResult.Pass -> VerificationPassColor
        is VerificationResult.Fail -> VerificationFailColor
    }

    Text(
        text = "Status: $statusText",
        style = MaterialTheme.typography.headlineSmall,
        color = statusColor,
    )

    if (result is VerificationResult.Fail) {
        Spacer(modifier = Modifier.semanticsSealed().height(SECTION_SPACING))
        Text(
            text = "Scanned: ${result.scannedFingerprint.take(FINGERPRINT_DISPLAY_LENGTH)}...",
            style = MaterialTheme.typography.bodySmall,
            color = VerificationFailColor,
        )
    }
}

@Composable
private fun ErrorContent(error: org.poc.tweedledee.core.CertificateError) {
    val message = when (error) {
        is org.poc.tweedledee.core.CertificateError.NoSigningInfo -> "No signing info available"
        is org.poc.tweedledee.core.CertificateError.ExtractionFailed -> "Extraction failed: ${error.reason}"
    }

    Column(
        modifier = Modifier.semanticsSealed().fillMaxSize().padding(CONTENT_PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(name = "Waiting - Light", device = "id:pixel_6")
@Composable
private fun PreviewWaitingLight() {
    AliceTheme(isDarkTheme = false) {
        CertificateVerificationScreen(
            state = VerificationScreenState.Loading("Extracting certificate..."),
            previewViewFactory = null,
        )
    }
}

@Preview(name = "Waiting - Dark", device = "id:pixel_6")
@Composable
private fun PreviewWaitingDark() {
    AliceTheme(isDarkTheme = true) {
        CertificateVerificationScreen(
            state = VerificationScreenState.Loading("Extracting certificate..."),
            previewViewFactory = null,
        )
    }
}
```

- [ ] **Step 4: Create `MainActivity.kt`**

```kotlin
package org.alice.poc.tweedledee.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.alice.poc.tweedledee.composables.CertificateVerificationScreen
import org.alice.poc.tweedledee.composables.theme.AliceTheme
import org.alice.poc.tweedledee.viewmodel.CertificateVerificationViewModel
import org.poc.tweedledee.core.QrScanner

class MainActivity : ComponentActivity() {

    private val qrScanner = QrScanner()

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCamera()
        }
    }

    private var viewModelInstance: CertificateVerificationViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AliceTheme {
                val certificateVerificationViewModel: CertificateVerificationViewModel = viewModel()
                viewModelInstance = certificateVerificationViewModel

                val state by certificateVerificationViewModel.state.collectAsState()

                CertificateVerificationScreen(
                    state = state,
                    previewViewFactory = { PreviewView(this@MainActivity) },
                )
            }
        }

        requestCameraPermissionIfNeeded()
        observeScannedPayload()
    }

    override fun onDestroy() {
        super.onDestroy()
        qrScanner.shutdown()
    }

    private fun requestCameraPermissionIfNeeded() {
        val isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        if (isGranted)
            startCamera()
        else
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val previewView = PreviewView(this)
                qrScanner.bindToLifecycle(this, cameraProvider, previewView)
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun observeScannedPayload() {
        val lifecycleScope = androidx.lifecycle.lifecycleScope
        lifecycleScope.launchWhenStarted {
            qrScanner.scannedPayload.collect { payload ->
                if (payload != null) {
                    viewModelInstance?.onScanResult(payload)
                }
            }
        }
    }
}
```

**Note:** The `observeScannedPayload` method uses `lifecycleScope` — this needs `lifecycle-runtime-ktx`. Already in dependencies. The `startCamera` creates a throwaway `PreviewView` for binding — the actual preview is handled in the Compose screen via `AndroidView`. This is a known pattern limitation for POCs; the PreviewView from Compose's `AndroidView` should be passed to the scanner. The Activity needs refactoring to wire the Compose-created PreviewView to the QrScanner. Update `startCamera` to store the `cameraProvider` and bind when the Compose PreviewView is available.

**Revised approach:** Store `cameraProvider` in the Activity, pass a `previewViewFactory` callback to the screen that creates the PreviewView and triggers scanner binding.

Replace the `startCamera` and `observeScannedPayload` methods and update `onCreate`:

```kotlin
package org.alice.poc.tweedledee.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.alice.poc.tweedledee.composables.CertificateVerificationScreen
import org.alice.poc.tweedledee.composables.theme.AliceTheme
import org.alice.poc.tweedledee.viewmodel.CertificateVerificationViewModel
import org.poc.tweedledee.core.QrScanner

class MainActivity : ComponentActivity() {

    private val qrScanner = QrScanner()
    private var cameraProvider: ProcessCameraProvider? = null

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            initializeCameraProvider()
        }
    }

    private var viewModelInstance: CertificateVerificationViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AliceTheme {
                val certificateVerificationViewModel: CertificateVerificationViewModel = viewModel()
                viewModelInstance = certificateVerificationViewModel

                val state by certificateVerificationViewModel.state.collectAsState()

                CertificateVerificationScreen(
                    state = state,
                    previewViewFactory = { createBoundPreviewView() },
                )
            }
        }

        requestCameraPermissionIfNeeded()
        observeScannedPayload()
    }

    override fun onDestroy() {
        super.onDestroy()
        qrScanner.shutdown()
    }

    private fun requestCameraPermissionIfNeeded() {
        val isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        if (isGranted)
            initializeCameraProvider()
        else
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun initializeCameraProvider() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            { cameraProvider = future.get() },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun createBoundPreviewView(): PreviewView {
        val previewView = PreviewView(this)
        val provider = cameraProvider
        if (provider != null) {
            qrScanner.bindToLifecycle(this, provider, previewView)
        }
        return previewView
    }

    private fun observeScannedPayload() {
        lifecycleScope.launch {
            qrScanner.scannedPayload.collect { payload ->
                if (payload != null) {
                    viewModelInstance?.onScanResult(payload)
                }
            }
        }
    }
}
```

- [ ] **Step 5: Verify Alice module compiles**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :alice:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add poc/0005_tweedledee/alice/
git commit -m "feat: add :alice module with ViewModel, screen, and camera integration"
```

---

### Task 10: :bob Module — Mirror of Alice

Create the `:bob` module. Same structure as `:alice` with different package, keystore, colors, and expected peer certificate.

**Files:**
- Create: `poc/0005_tweedledee/bob/build.gradle.kts`
- Create: `poc/0005_tweedledee/bob/src/main/AndroidManifest.xml`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/composables/modifier/SemanticsSealed.kt`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/composables/theme/Color.kt`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/composables/theme/Type.kt`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/composables/theme/Theme.kt`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/viewmodel/VerificationScreenState.kt`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/viewmodel/CertificateVerificationViewModel.kt`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/composables/CertificateVerificationScreen.kt`
- Create: `poc/0005_tweedledee/bob/src/main/kotlin/org/bob/poc/tweedledee/activity/MainActivity.kt`

- [ ] **Step 1: Create `bob/build.gradle.kts`**

Identical to Alice's `build.gradle.kts` with these differences:
- `namespace = "org.bob.poc.tweedledee"`
- `applicationId = "org.bob.poc.tweedledee"`
- Signing config named `"bobDebug"`, uses `bob-debug.jks` and `bobKeystorePassword`
- `generatePeerCertificate` task reads **Alice's** keystore (`keystores/alice-debug.jks`), uses `aliceKeystorePassword`, alias `"alice"`, packageName `"org.bob.poc.tweedledee.generated"`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.bob.poc.tweedledee"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.bob.poc.tweedledee"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("bobDebug") {
            storeFile = rootProject.file("keystores/bob-debug.jks")
            storePassword = providers.gradleProperty("bobKeystorePassword").get()
            keyAlias = "bob"
            keyPassword = providers.gradleProperty("bobKeystorePassword").get()
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("bobDebug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("bobDebug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val generatePeerCertificate by tasks.registering(GenerateCertificateFingerprintTask::class) {
    keystoreFile.set(rootProject.file("keystores/alice-debug.jks"))
    keystorePassword.set(providers.gradleProperty("aliceKeystorePassword"))
    keyAlias.set("alice")
    packageName.set("org.bob.poc.tweedledee.generated")
    outputDirectory.set(layout.buildDirectory.dir("generated/peerCertificate"))
}

android.sourceSets["main"].kotlin.srcDir(generatePeerCertificate.map { it.outputDirectory })

dependencies {
    implementation(project(":core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.arrow.core)
    implementation(libs.kotlinx.coroutines.android)

    lintChecks(project(":lint-checks"))

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
}
```

- [ ] **Step 2: Create `bob/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.CAMERA" />

    <uses-feature android:name="android.hardware.camera" android:required="true" />

    <application
        android:label="POC 0005 Bob"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <activity
            android:name=".activity.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

- [ ] **Step 3: Create all Bob Kotlin files**

Copy each Alice file with these substitutions:
- Package: `org.alice.poc.tweedledee` → `org.bob.poc.tweedledee`
- Theme name: `AliceTheme` → `BobTheme`
- Title text in screen: `"Alice"` → `"Bob"`
- Color prefix: `Alice*` → `Bob*`
- Color values (green tones for Bob):

`Color.kt`:
```kotlin
package org.bob.poc.tweedledee.composables.theme

import androidx.compose.ui.graphics.Color

val BobPrimaryLight = Color(0xFF2E7D32)
val BobOnPrimaryLight = Color(0xFFFFFFFF)
val BobPrimaryContainerLight = Color(0xFFC8E6C9)
val BobOnPrimaryContainerLight = Color(0xFF002204)
val BobSecondaryLight = Color(0xFF526350)
val BobOnSecondaryLight = Color(0xFFFFFFFF)
val BobBackgroundLight = Color(0xFFFCFDF6)
val BobOnBackgroundLight = Color(0xFF1A1C19)
val BobSurfaceLight = Color(0xFFFCFDF6)
val BobOnSurfaceLight = Color(0xFF1A1C19)
val BobErrorLight = Color(0xFFBA1A1A)
val BobOnErrorLight = Color(0xFFFFFFFF)

val BobPrimaryDark = Color(0xFFA5D6A7)
val BobOnPrimaryDark = Color(0xFF00390A)
val BobPrimaryContainerDark = Color(0xFF005313)
val BobOnPrimaryContainerDark = Color(0xFFC8E6C9)
val BobSecondaryDark = Color(0xFFB9CCB5)
val BobOnSecondaryDark = Color(0xFF243424)
val BobBackgroundDark = Color(0xFF1A1C19)
val BobOnBackgroundDark = Color(0xFFE2E3DC)
val BobSurfaceDark = Color(0xFF1A1C19)
val BobOnSurfaceDark = Color(0xFFE2E3DC)
val BobErrorDark = Color(0xFFFFB4AB)
val BobOnErrorDark = Color(0xFF690005)

val VerificationPassColor = Color(0xFF2E7D32)
val VerificationFailColor = Color(0xFFC62828)
val VerificationWaitingColor = Color(0xFF757575)
```

`Theme.kt` — same structure as Alice's, referencing `Bob*` colors, function named `BobTheme`.

`Type.kt` — identical to Alice's (same `TweedledeeTypography`).

`SemanticsSealed.kt` — identical code, package `org.bob.poc.tweedledee.composables.modifier`.

`VerificationScreenState.kt` — identical code, package `org.bob.poc.tweedledee.viewmodel`.

`CertificateVerificationViewModel.kt` — identical code, package `org.bob.poc.tweedledee.viewmodel`, imports from `org.bob.poc.tweedledee.generated.ExpectedPeerCertificate`.

`CertificateVerificationScreen.kt` — identical code, package `org.bob.poc.tweedledee.composables`, title text `"Bob"`, imports from `org.bob.poc.tweedledee.*`, preview uses `BobTheme`.

`MainActivity.kt` — identical code, package `org.bob.poc.tweedledee.activity`, imports from `org.bob.poc.tweedledee.*`, uses `BobTheme`.

- [ ] **Step 4: Verify Bob module compiles**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :bob:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add poc/0005_tweedledee/bob/
git commit -m "feat: add :bob module mirroring :alice with green theme and swapped keystore"
```

---

### Task 11: Full Build Verification

Verify the entire project builds, all tests pass, and both APKs are produced.

- [ ] **Step 1: Run all unit tests**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew test
```

Expected: all tests pass (lint-checks detector tests + core extractor/generator tests).

- [ ] **Step 2: Build both debug APKs**

```bash
cd /Users/muttley/Miss_Charming/wonderland/poc/0005_tweedledee
./gradlew :alice:assembleDebug :bob:assembleDebug
```

Expected: BUILD SUCCESSFUL. APKs at:
- `alice/build/outputs/apk/debug/alice-debug.apk`
- `bob/build/outputs/apk/debug/bob-debug.apk`

- [ ] **Step 3: Verify generated ExpectedPeerCertificate files**

```bash
find poc/0005_tweedledee/alice/build/generated/peerCertificate -name "*.kt" -exec cat {} \;
find poc/0005_tweedledee/bob/build/generated/peerCertificate -name "*.kt" -exec cat {} \;
```

Expected: Alice's file contains Bob's fingerprint, Bob's file contains Alice's fingerprint. The SHA256 values should be different from each other.

- [ ] **Step 4: Verify lint rule is active**

Temporarily remove `semanticsSealed()` from a Modifier chain in Alice's `CertificateVerificationScreen.kt`, then run:

```bash
./gradlew :alice:lintDebug
```

Expected: BUILD FAILURE with `SemanticsNotSealed` error. Restore the file after verification.

- [ ] **Step 5: Final commit**

```bash
git add -A poc/0005_tweedledee/
git commit -m "feat: complete poc-0005-tweedledee certificate fingerprint verification"
```
