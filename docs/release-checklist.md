# RELEASE_CHECKLIST.md — Pre-Publish Gate

## Purpose

Every item must be verified before any release build. This is a gate, not a guideline. No item is skipped.

## Versioning

- [ ] `versionCode` incremented (integer, monotonically increasing, never reused).
- [ ] `versionName` updated (semantic versioning: MAJOR.MINOR.PATCH).
- [ ] Version bump committed with a dedicated commit message: `chore: bump version to X.Y.Z`.

## Build

- [ ] Release build completes without warnings: `./gradlew assembleRelease`.
- [ ] AAB generated for Play Store: `./gradlew bundleRelease`.
- [ ] No string literal Gradle plugin references (must be `alias(libs.plugins.*)`).
- [ ] All dependencies use version catalogue (`libs.*`), no hardcoded versions.
- [ ] ProGuard/R8 rules reviewed — no crypto classes stripped or obfuscated incorrectly.
- [ ] R8 full mode compatibility verified.

## Signing

- [ ] Release keystore is correct (not debug keystore).
- [ ] Keystore credentials are not committed to source control.
- [ ] Signed APK/AAB verified: `apksigner verify --print-certs`.

## Testing

- [ ] All unit tests pass: `./gradlew test`.
- [ ] All integration tests pass.
- [ ] UI tests pass on target device(s).
- [ ] Crypto round-trip tests pass.
- [ ] Key zeroing tests pass.
- [ ] Air-gap violation tests pass (Alice build only).
- [ ] No test is `@Ignore` without a documented reason.

## Security Review

- [ ] No key material logged at any level (grep for `Log.`, `println`, `Timber`).
- [ ] No `toString()` on crypto value classes.
- [ ] No debug-only backdoors or test endpoints in release build.
- [ ] StrongBox attestation constants match current GrapheneOS verified boot fingerprints (Alice only).
- [ ] ProGuard/R8 keep rules preserve crypto class structure.

## Play Store Metadata (Bob only — Alice is sideloaded)

- [ ] App title, short description, full description updated if changed.
- [ ] Screenshots current.
- [ ] Privacy policy URL valid and current.
- [ ] Data safety form accurate (no data collection, no data sharing).
- [ ] Content rating questionnaire completed.
- [ ] Target API level meets current Play Store minimum.

## Alice-Specific (Sideloaded)

- [ ] APK generated (not AAB — no Play Store).
- [ ] APK signed with Alice-specific keystore.
- [ ] Installation tested on target GrapheneOS Pixel device.
- [ ] Air-gap surveillance service starts on boot.
- [ ] Tamper screen triggers correctly on simulated violation.

## Final

- [ ] Git tag created: `vX.Y.Z`.
- [ ] Release branch merged to main.
- [ ] No uncommitted changes in working tree.
