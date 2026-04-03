# BUILD_VARIANTS.md — Flavour and Build Type Matrix

## Product Flavours

| Flavour | Description | Distribution | Target Device |
|---|---|---|---|
| `alice` | Air-gapped device. Full surveillance module. No network permissions. | Sideloaded APK | GrapheneOS Pixel |
| `bob` | Networked device. Standard messaging. No air-gap module. | Play Store AAB | Any Android device |

## Build Types

| Build Type | Minification | Debuggable | Logging |
|---|---|---|---|
| `debug` | Off | Yes | Verbose (but NEVER key material) |
| `release` | R8 full mode | No | None |

## Variant Matrix

| Variant | Flavour | Build Type | Notes |
|---|---|---|---|
| `aliceDebug` | alice | debug | Development and testing on GrapheneOS device |
| `aliceRelease` | alice | release | Production sideload build |
| `bobDebug` | bob | debug | Development and testing on standard device |
| `bobRelease` | bob | release | Play Store submission build |

## Module Inclusion by Flavour

| Module | Alice | Bob |
|---|---|---|
| `:feature_airgap` | Yes | No |
| `:feature_engagement` | Yes | Yes |
| `:feature_messaging` | Yes | Yes |
| `:feature_keys` | Yes | Yes |
| `:feature_qr` | Yes | Yes |
| `:crypto` | Yes | Yes |
| `:serialization` | Yes | Yes |
| `:core` | Yes | Yes |

## Permissions by Flavour

### Alice
- `CAMERA` (QR scanning only)
- No `INTERNET`. No `ACCESS_NETWORK_STATE`. No `BLUETOOTH`. No network-related permissions whatsoever.

### Bob
- `CAMERA` (QR scanning only)
- `INTERNET` (future: blockchain auth verification only — Phase 2)

## Feature Flags

- `BuildConfig.IS_AIRGAPPED` — `true` for Alice, `false` for Bob.
- `BuildConfig.ENABLE_SURVEILLANCE` — `true` for Alice, `false` for Bob.
- `BuildConfig.DISTRIBUTION_MODE` — `SIDELOAD` for Alice, `PLAY_STORE` for Bob.

## R8/ProGuard Rules

### Keep Rules (both flavours)
- All `@Serializable` data classes (CBOR serialisation uses reflection-free generated serializers, but keep rules prevent stripping).
- Bouncy Castle provider classes.
- Value class constructors.

### Alice-Specific Keep Rules
- `AirGapSurveillanceService` and all `BroadcastReceiver` subclasses.
- StrongBox attestation parsing classes.
- All GrapheneOS verified boot fingerprint constants.

## Build Commands

```bash
# Alice debug
./gradlew assembleAliceDebug

# Alice release (sideload APK)
./gradlew assembleAliceRelease

# Bob debug
./gradlew assembleBobDebug

# Bob release (Play Store AAB)
./gradlew bundleBobRelease

# Run all tests for both flavours
./gradlew test

# Run Alice-specific tests only
./gradlew testAliceDebugUnitTest
```
