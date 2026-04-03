# Build Quality Gates — Non-Negotiable

Six gates, ordered by speed. Fail fast, fix immediately.

---

## Gate Sequence

| # | Gate | Command | Trigger |
|---|------|---------|---------|
| 1 | Compile | `./gradlew compileDebugKotlin` | Every change |
| 2 | Static analysis | `./gradlew detekt` | Every Kotlin change |
| 3 | Unit tests | `./gradlew testDebugUnitTest` | Every logic change |
| 4 | Android Lint | `./gradlew lint` | Android resource/manifest changes |
| 5 | Dependency health | `./gradlew buildHealth` | Dependency changes, weekly |
| 6 | Vulnerability scan | `./gradlew dependencyCheckAnalyze` | Dependency changes, weekly |

Gate 1 is implicit in Gate 2 — detekt compiles the code. The separate compile command is useful for scoped runs (`./gradlew :module:compileDebugKotlin`) when you want faster feedback than a full detekt sweep.

---

## Fast Path — The Common Case

Most changes are Kotlin logic. One command covers Gates 1-3:

```bash
./gradlew compileDebugKotlin detekt testDebugUnitTest
```

---

## Gate Rules

### Gates 1-2: Mandatory on every change

No code is presented with compile errors or detekt violations. The pre-commit hook enforces Gate 2.

### Gate 3: Mandatory on every logic change

Pure formatting or comment changes are exempt. If tests exist for modified code, they must pass. If no tests exist, write one. A failing test means the change broke existing behaviour — fix the code, not the test. The test is only wrong if its assertion is provably incorrect, and that must be explained before changing it.

### Gate 4: Mandatory on Android-specific changes

Run after manifest, resource, or layout changes. See `static-analysis.md` for Android Lint details.

### Gates 5-6: Triggered by dependency or release milestones

Run when dependencies change or before releases. Do not chain on every change — they are slow and only relevant in specific circumstances.

---

## Scoped Runs

For multi-module projects, scope any gate to the affected module:

```bash
./gradlew :module:compileDebugKotlin
./gradlew :module:detekt
./gradlew :module:testDebugUnitTest
```

---

## Gate 5 — Dependency Health (not yet implemented)

Tool: Dependency Analysis Gradle Plugin (`com.autonomousapps.dependency-analysis`). Applied in `settings.gradle.kts` (not root build file — the plugin requires settings-level application for project-wide analysis).

Detects: unused dependencies, wrong configurations (`api` vs `implementation` vs `compileOnly`), used transitive dependencies that should be declared directly.

Severity: unused and incorrect configuration = fail. Used transitive = warn.

---

## Gate 6 — Vulnerability Scan (not yet implemented)

Tool: OWASP Dependency-Check (`org.owasp.dependencycheck`). Checks all dependencies against the NVD for known CVEs.

Requires: NVD API key in `~/.gradle/gradle.properties` (`nvdApiKey=YOUR_KEY`). Without it, updates are rate-limited to unusable. For CI, the key must come from a secret store.

Threshold: CVSS >= 7.0 fails the build. On failure: upgrade the dependency. If no patch exists, assess applicability — suppress with `<notes>` explanation only if the CVE does not apply. If it applies and no patch exists, flag for human review immediately.

Suppression file: `config/owasp/suppressions.xml` — confirmed false positives only.

---

## Failure Protocol

Failures are fixed, not skipped. The only exception is a confirmed false positive with a documented suppression. Never present work with known gate failures listed as "known issues."
