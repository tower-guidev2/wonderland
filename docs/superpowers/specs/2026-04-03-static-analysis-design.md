# Static Analysis — Design Spec

**Date:** 2026-04-03
**Status:** Approved
**Source:** `/Users/muttley/Miss_Charming/STATIC_ANALYSIS.md` (original research doc)

---

## Decisions

| # | Decision | Detail |
|---|----------|--------|
| 1 | Convention plugin | Single `wonderland.detekt` convention plugin. No per-module duplication. |
| 2 | Detekt 1.23.8 stable | No alpha/beta libraries, ever. Migrate to 2.0 when stable. |
| 3 | autoCorrect = false everywhere | ktlint wrapper in detekt.yml AND Gradle task config. Convention plugin enforces both. |
| 4 | No baseline | Greenfield = zero violations from day one. Add only if legacy code acquired. |
| 5 | semanticsSealed() clarity | Enforced by custom lint-checks module, not detekt. Verify for false positives during setup. |
| 6 | Git pre-commit hook | Hard gate. No CC hooks for detekt. Blocks commits with violations. |
| 7 | Type resolution is manual | Pre-commit runs `./gradlew detekt` (fast). `detektMain` manual for pre-PR/periodic deep checks. |
| 8 | Android Lint defaults | No custom `lint.xml` yet. Deferred until real UI noise warrants tuning. |
| 9 | Split ingestion | Rules → `docs/rules/static-analysis.md`. Config → code in wonderland. |
| 10 | Compose-rules conditional | detekt core + ktlint wrapper on all modules. compose-rules only on modules that use Compose. |
| 11 | JVM modules default | Android feature modules only when absolutely required. JVM is the default. |

---

## Artifacts

| # | Artifact | Location | Type |
|---|----------|----------|------|
| 1 | Rules file | `docs/rules/static-analysis.md` | Documentation — CC behavioural rules |
| 2 | Convention plugin | `build-logic/` — `wonderland.detekt` | Code |
| 3 | detekt config | `config/detekt/detekt.yml` | Code |
| 4 | Pre-commit hook | `scripts/pre-commit` (symlinked into `.git/hooks/`) | Code |
| 5 | CLAUDE.md updates | `clauding/CLAUDE.md` + `current/CLAUDE.md` | Documentation |

---

## Artifact 1 — Rules File (`docs/rules/static-analysis.md`)

Imported by CLAUDE.md. Contains only behavioural rules CC must follow. No installation, no config snippets.

**Content:**

- **Toolchain:** detekt 1.23.8 + ktlint wrapper + nlopez compose-rules. Android Lint for platform-specific checks.
- **When to run:** `./gradlew detekt` after any Kotlin change. `./gradlew lint` after manifest/resource/layout changes. Both before commits.
- **Pre-commit hook:** Hard gate. Commits with violations blocked.
- **Type resolution:** `./gradlew detektMain` is manual — pre-PR or periodic deep checks only. Not on the commit path.
- **autoCorrect off everywhere.** Read violations, fix deliberately.
- **No baseline.** Greenfield = zero violations. Baseline added only if legacy code acquired.
- **Fix, don't suppress.** `@Suppress("RuleName")` only for genuine false positives, narrowest scope, with justifying comment. Never disable a rule globally.
- **`semanticsSealed()` enforcement** from custom lint-checks module, not detekt. Verify for false positives with compose-rules during setup.
- **Android Lint defaults** — no custom `lint.xml` until noise warrants it.

---

## Artifact 2 — Convention Plugin (`wonderland.detekt`)

Binary convention plugin in `build-logic/`, `.kt` implementing `Plugin<Project>`. Matches NowInAndroid pattern.

**Configures:**

- `buildUponDefaultConfig = true`, `allRules = false`, `parallel = true`
- `autoCorrect = false` at the task level
- Config path: `$rootDir/config/detekt/detekt.yml`
- No baseline
- `detektPlugins` dependencies: ktlint wrapper + compose-rules (compose-rules conditional on module using Compose)
- Reports: HTML + SARIF + MD
- `jvmTarget = "17"` on all detekt tasks

**Does not:**

- Reference any baseline file
- Set `autoCorrect = true` anywhere
- Enable type resolution by default

**Version catalog entries (`libs.versions.toml`):**

- `detekt = "1.23.8"`, `detekt-compose-rules = "0.4.27"`
- Plugin alias: `detekt`
- Library aliases: `detekt-ktlint-wrapper`, `detekt-compose-rules`

---

## Artifact 3 — detekt.yml (`config/detekt/detekt.yml`)

Generated via `./gradlew detektGenerateConfig`, then customised.

**Compose compatibility overrides:**

- `FunctionNaming`: ignores `@Composable`
- `TopLevelPropertyNaming`: `constantPattern: '[A-Z][A-Za-z0-9]*'`
- `MagicNumber`: ignores property/companion/annotation declarations
- `LongParameterList`: ignores `@Composable`, threshold 10

**nlopez compose-rules:** All rules active — `ComposableAnnotationNaming`, `ComposableNaming`, `ComposableParamOrder`, `ContentEmitterReturningValues`, `ModifierComposable`, `ModifierMissing`, `ModifierReused`, `ModifierWithoutDefault`, `MultipleEmitters`, `MutableParams`, `PreviewPublic`, `RememberMissing`, `UnstableCollections`, `ViewModelForwarding`, `ViewModelInjection`.

**ktlint wrapper:** `code_style: intellij_idea`, `autoCorrect: false`.

**Project-specific:**

- `MaxLineLength`: 180 (not default 120)
- Enable detekt rules that reinforce mc-quality hard bans (e.g. `UnnecessaryNotNullOperator` for `!!` ban). Exact rule mapping determined during implementation.

---

## Artifact 4 — Pre-commit Hook (`scripts/pre-commit`)

Plain shell script. Symlinked to `.git/hooks/pre-commit` during project setup.

**Behaviour:**

- Runs `./gradlew detekt`
- Non-zero exit code blocks the commit, prints violation summary
- Does NOT run `lint` (too slow)
- Does NOT run `detektMain` (type resolution is manual)

---

## Artifact 5 — CLAUDE.md Updates

**`clauding/CLAUDE.md`:**

- Add `@docs/rules/static-analysis.md` to Rules section

**`current/CLAUDE.md`:**

- Add `detekt + ktlint wrapper + compose-rules (static analysis)` to Tech Stack

**No changes to:** mc-quality skill, control documents table.
