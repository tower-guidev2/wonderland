# Build System Rules — Non-Negotiable

---

## Stack

- Kotlin 2.3+ (pure — no Java anywhere)
- AGP 9+, Gradle 9+
- KSP2 only — KSP1 is incompatible with Kotlin ≥ 2.3 and AGP ≥ 9.0. Never configure KSP1.
- build-logic composite build following NowInAndroid exactly

---

## build-logic

Binary convention plugins (`.kt` implementing `Plugin<Project>`). Not precompiled script plugins (`.gradle.kts`).

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

---

## Version Catalog

All versions pinned exactly in `libs.versions.toml`. No dynamic versions anywhere.

---

## AGP 9 — Critical Rules

- Do **not** apply `org.jetbrains.kotlin.android` — AGP 9 includes it. Applying it manually breaks the build.
- Do **not** import `com.android.build.gradle.LibraryExtension` — use `com.android.build.api.dsl.LibraryExtension` (public DSL). Internal type removed in AGP 10.0.
- Library modules do not generate BuildConfig by default. Add `android { buildFeatures { buildConfig = true } }` if needed.

---

## JVM vs Android Library Modules

When a module uses `wonderland.jvm.library` (`org.jetbrains.kotlin.jvm`) AND is consumed by an Android module, experimental compiler flags (`-Xcontext-parameters`, `-Xcontext-sensitive-resolution`) mark the JAR as pre-release and break the Android consumer's compilation.

**Rule:** use `wonderland.android.library` for any module in the alice or bob subtrees — even if the source is pure Kotlin with no Android imports. Only truly standalone, non-Android-consumed modules (e.g. `core:common`, `core:protocol`) may use `wonderland.jvm.library`.

---

## Compiler Flag Note

`-XXLanguage:+ExplicitBackingFields` marks compiled `.class` files as pre-release Kotlin, blocking Android module consumers. `ExplicitBackingFields` is stable in Kotlin 2.2+ — use `-Xexplicit-backing-fields` only.

---

## Merged Manifest Auditing

After every dependency update, verify Alice's merged manifest at `build/intermediates/merged_manifest/` contains no prohibited permissions. Libraries inject permissions through their own manifests. Source manifest alone is insufficient.

---

## R8 / ProGuard

Explicit keep rules must be authored for both Alice and Bob. Bouncy Castle requires specific keeps — R8 silently strips cryptographic classes. Verify release build after every dependency update. Debug build passing is not sufficient.

---

## Android XML Stub Theme

For Compose-only apps use `android:Theme.Material.Light.NoActionBar`.
- `android:Theme.Material.NoTitleBar` — does not exist in AOSP
- `android:Theme.Material.Light.NoTitleBar` — does not exist in AOSP
- `Theme.Material3.DayNight.NoActionBar` — requires `com.google.android.material`, not present in Alice

---

## Other Known Traps

- `FontVariation.weight()` takes `Int` not `Float` — requires `@OptIn(ExperimentalTextApi::class)` on the `FontFamily` declaration.
- `TelephonyManager.ACTION_SIM_CARD_STATE_CHANGED` is `@SystemApi`/`@hide`. Use string literal `"android.intent.action.SIM_STATE_CHANGED"`. SIM state extra key is `"ss"` with string values — no public SDK constants exist for either.
