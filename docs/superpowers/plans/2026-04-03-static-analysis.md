# Static Analysis Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add detekt + ktlint wrapper + compose-rules as a convention plugin with a pre-commit hook, so every Kotlin change is validated before it enters the repo.

**Architecture:** A single `DetektConventionPlugin` in `build-logic/convention/` applies detekt with ktlint wrapper to all modules. Compose-rules are added conditionally only when the Compose compiler plugin is present. A shell pre-commit hook runs `./gradlew detekt` as a hard gate.

**Tech Stack:** detekt 1.23.8, detekt-rules-ktlint-wrapper 1.23.8, io.nlopez.compose.rules:detekt 0.4.27

**Design spec:** `docs/superpowers/specs/2026-04-03-static-analysis-design.md`

---

### Task 1: Add detekt to the version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add detekt version and compose-rules version**

In the `[versions]` block, add after the existing test versions:

```toml
# Static analysis
detekt                  = "1.23.8"
detektComposeRules      = "0.4.27"
```

- [ ] **Step 2: Add detekt plugin entry**

In the `[plugins]` block, add after the `ksp` entry:

```toml
detekt                  = { id = "io.gitlab.arturbosch.detekt",                  version.ref = "detekt" }
```

- [ ] **Step 3: Add detekt library entries**

In the `[libraries]` block, add after the existing Gradle plugin artifacts section:

```toml
# Static analysis — detekt plugins
detekt-gradlePlugin                 = { module = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin",         version.ref = "detekt" }
detekt-ktlint-wrapper               = { module = "io.gitlab.arturbosch.detekt:detekt-rules-ktlint-wrapper", version.ref = "detekt" }
detekt-compose-rules                = { module = "io.nlopez.compose.rules:detekt",                          version.ref = "detektComposeRules" }
```

- [ ] **Step 4: Add convention plugin entry**

In the `[plugins]` block, add with the other wonderland convention plugins:

```toml
wonderland-detekt                   = { id = "wonderland.detekt" }
```

- [ ] **Step 5: Verify catalog parses**

Run: `./gradlew --dry-run help`
Expected: BUILD SUCCESSFUL (no catalog parse errors)

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build: add detekt version catalog entries"
```

---

### Task 2: Add detekt Gradle plugin dependency to build-logic

**Files:**
- Modify: `build-logic/convention/build.gradle.kts`

- [ ] **Step 1: Add detekt-gradlePlugin as compileOnly dependency**

Add to the `dependencies` block:

```kotlin
compileOnly(libs.detekt.gradlePlugin)
```

- [ ] **Step 2: Verify build-logic compiles**

Run: `./gradlew -p build-logic assemble`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add build-logic/convention/build.gradle.kts
git commit -m "build: add detekt gradle plugin to build-logic dependencies"
```

---

### Task 3: Create the DetektConventionPlugin

**Files:**
- Create: `build-logic/convention/src/main/kotlin/DetektConventionPlugin.kt`

- [ ] **Step 1: Write the convention plugin**

```kotlin
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import wonderland.libs

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                parallel = true
                autoCorrect = false
                config.setFrom("${rootProject.projectDir}/config/detekt/detekt.yml")
            }

            dependencies {
                "detektPlugins"(libs.findLibrary("detekt-ktlint-wrapper").get())
            }

            pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
                dependencies {
                    "detektPlugins"(libs.findLibrary("detekt-compose-rules").get())
                }
            }

            tasks.withType<Detekt>().configureEach {
                jvmTarget = "21"
                autoCorrect = false
                reports {
                    html.required.set(true)
                    sarif.required.set(true)
                    md.required.set(true)
                }
            }

            tasks.withType<DetektCreateBaselineTask>().configureEach {
                jvmTarget = "21"
            }
        }
    }
}
```

Note: `pluginManager.withPlugin` fires immediately if the Compose plugin is already applied, or defers until it is applied later. This handles both plugin orderings cleanly. ktlint wrapper is always added; compose-rules only when the module uses Compose.

- [ ] **Step 2: Register the plugin in build-logic/convention/build.gradle.kts**

Add to the `gradlePlugin.plugins` block:

```kotlin
register("detekt") {
    id = libs.plugins.wonderland.detekt.get().pluginId
    implementationClass = "DetektConventionPlugin"
}
```

- [ ] **Step 3: Verify build-logic compiles**

Run: `./gradlew -p build-logic assemble`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add build-logic/convention/src/main/kotlin/DetektConventionPlugin.kt build-logic/convention/build.gradle.kts
git commit -m "build: create wonderland.detekt convention plugin"
```

---

### Task 4: Declare detekt in the root build file

**Files:**
- Modify: `build.gradle.kts` (root)

- [ ] **Step 1: Add detekt plugin with apply false**

Add to the `plugins` block:

```kotlin
alias(libs.plugins.detekt) apply false
```

- [ ] **Step 2: Commit**

```bash
git add build.gradle.kts
git commit -m "build: declare detekt plugin in root build file"
```

---

### Task 5: Apply wonderland.detekt to existing modules

**Files:**
- Modify: `app/build.gradle.kts`

Apply `wonderland.detekt` to the `:app` module first as a smoke test. Other modules get it as they are built.

- [ ] **Step 1: Add the plugin to app/build.gradle.kts**

Add to the `plugins` block:

```kotlin
alias(libs.plugins.wonderland.detekt)
```

- [ ] **Step 2: Generate the default detekt config**

Run: `./gradlew detektGenerateConfig`
Expected: Creates `config/detekt/detekt.yml`

- [ ] **Step 3: Verify detekt runs**

Run: `./gradlew :app:detekt`
Expected: BUILD SUCCESSFUL (may report violations — that's fine, we configure next)

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts config/detekt/detekt.yml
git commit -m "build: apply wonderland.detekt to app module and generate default config"
```

---

### Task 6: Customise detekt.yml

**Files:**
- Modify: `config/detekt/detekt.yml`

- [ ] **Step 1: Add Compose compatibility overrides**

Find and modify these sections in the generated config:

Under `naming:` → `FunctionNaming:`, add:
```yaml
    ignoreAnnotated:
      - 'Composable'
```

Under `naming:` → `TopLevelPropertyNaming:`, set:
```yaml
    constantPattern: '[A-Z][A-Za-z0-9]*'
```

Under `style:` → `MagicNumber:`, set:
```yaml
    ignorePropertyDeclaration: true
    ignoreCompanionObjectPropertyDeclaration: true
    ignoreAnnotation: true
```

Under `style:` → `MaxLineLength:`, set:
```yaml
    maxLineLength: 180
```

Under `complexity:` → `LongParameterList:`, set:
```yaml
    ignoreAnnotated:
      - 'Composable'
    functionThreshold: 10
    constructorThreshold: 10
```

- [ ] **Step 2: Add compose-rules plugin configuration**

Append to the end of `detekt.yml`:

```yaml
# === nlopez compose-rules ===
Compose:
  ComposableAnnotationNaming:
    active: true
  ComposableNaming:
    active: true
  ComposableParamOrder:
    active: true
  ContentEmitterReturningValues:
    active: true
  ModifierComposable:
    active: true
  ModifierMissing:
    active: true
  ModifierReused:
    active: true
  ModifierWithoutDefault:
    active: true
  MultipleEmitters:
    active: true
  MutableParams:
    active: true
  PreviewPublic:
    active: true
  RememberMissing:
    active: true
  UnstableCollections:
    active: true
  ViewModelForwarding:
    active: true
  ViewModelInjection:
    active: true
```

- [ ] **Step 3: Add ktlint wrapper configuration**

Append to the end of `detekt.yml`:

```yaml
# === ktlint via detekt wrapper ===
formatting:
  autoCorrect: false
```

- [ ] **Step 4: Enable rules that reinforce mc-quality hard bans**

Find and activate these rules in the generated config:

Under `potential-bugs:` → `UnnecessaryNotNullOperator:`:
```yaml
    active: true
```

Under `style:` → `ForbiddenMethodCall:`:
```yaml
    active: true
    methods:
      - 'kotlin.apply'
      - 'kotlin.runCatching'
```

- [ ] **Step 5: Run detekt to verify config is valid**

Run: `./gradlew :app:detekt`
Expected: BUILD SUCCESSFUL (violations are expected — fix any config parse errors)

- [ ] **Step 6: Commit**

```bash
git add config/detekt/detekt.yml
git commit -m "build: customise detekt config for Compose, 180-char lines, mc-quality bans"
```

---

### Task 7: Fix any detekt violations in the app module

**Files:**
- Modify: whatever files have violations

- [ ] **Step 1: Run detekt and capture output**

Run: `./gradlew :app:detekt`
Read every violation reported.

- [ ] **Step 2: Fix all violations**

For each violation, fix the code. Do not suppress unless it is a genuine false positive with a justifying comment.

- [ ] **Step 3: Run detekt again to confirm zero violations**

Run: `./gradlew :app:detekt`
Expected: BUILD SUCCESSFUL with no violations

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "fix: resolve detekt violations in app module"
```

---

### Task 8: Apply wonderland.detekt to all existing modules

**Files:**
- Modify: every module's `build.gradle.kts` that exists in the project

- [ ] **Step 1: Add the plugin to every module**

For each module listed in `settings.gradle.kts`, add to its `plugins` block:

```kotlin
alias(libs.plugins.wonderland.detekt)
```

- [ ] **Step 2: Run detekt across all modules**

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL (fix any violations found)

- [ ] **Step 3: Fix all violations across all modules**

Same process as Task 7.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "build: apply wonderland.detekt to all modules and fix violations"
```

---

### Task 9: Create the pre-commit hook

**Files:**
- Create: `scripts/pre-commit`

- [ ] **Step 1: Write the pre-commit hook script**

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "Running detekt..."
./gradlew detekt --quiet

if [ $? -ne 0 ]; then
    echo ""
    echo "COMMIT BLOCKED: detekt violations found. Fix them before committing."
    exit 1
fi
```

- [ ] **Step 2: Make the script executable**

Run: `chmod +x scripts/pre-commit`

- [ ] **Step 3: Symlink into .git/hooks**

Run: `ln -sf ../../scripts/pre-commit .git/hooks/pre-commit`

- [ ] **Step 4: Verify the hook works**

Run: `git commit --allow-empty -m "test: pre-commit hook smoke test"`
Expected: detekt runs, commit succeeds if no violations. Then remove the empty commit:
Run: `git reset HEAD~1`

- [ ] **Step 5: Commit**

```bash
git add scripts/pre-commit
git commit -m "build: add pre-commit hook running detekt"
```

---

### Task 10: Create the rules file

**Files:**
- Create: `docs/rules/static-analysis.md`

- [ ] **Step 1: Write the rules file**

```markdown
# Static Analysis Rules — Non-Negotiable

---

## Toolchain

detekt 1.23.8 + ktlint wrapper + nlopez compose-rules. Android Lint for platform-specific checks.

---

## When To Run

- `./gradlew detekt` — after any Kotlin change.
- `./gradlew lint` — after manifest, resource, or layout changes.
- `./gradlew detekt lint` — before commits and milestones.
- Pre-commit hook enforces `./gradlew detekt` as a hard gate. Commits with violations are blocked.

---

## Type Resolution

`./gradlew detektMain` — manual only. For pre-PR or periodic deep checks. Not on the commit path.

---

## autoCorrect

Off everywhere. Read violations, fix deliberately. Understanding the problem produces better code than silent patching.

---

## Baseline

None. Greenfield project = zero violations from day one. Add baseline support only if legacy code is acquired.

---

## Fixing Violations

1. Read every violation.
2. Fix the code — refactor to satisfy the rule.
3. `@Suppress("RuleName")` only for genuine false positives. Narrowest scope. Justifying comment required. Never at file level unless absolutely necessary.
4. Never disable a rule globally. If a rule produces consistent false positives project-wide, raise it for human review.

---

## semanticsSealed()

Enforced by the project's custom lint-checks module (`SemanticsNotSealed` rule at ERROR severity), not by detekt or compose-rules. If compose-rules produces false positives related to `semanticsSealed()`, suppress individually with justification.

---

## Android Lint

Defaults. No custom `lint.xml` until real noise warrants tuning.

---

## Compose Rules

Mandatory for all modules that use Compose. The `wonderland.detekt` convention plugin adds them automatically when the Compose compiler plugin is present. Non-Compose modules get detekt + ktlint wrapper only.
```

- [ ] **Step 2: Commit**

```bash
git add docs/rules/static-analysis.md
git commit -m "docs: add static analysis rules file"
```

---

### Task 11: Update CLAUDE.md files

**Files:**
- Modify: `CLAUDE.md` (wonderland root, i.e. `clauding/CLAUDE.md` when referenced from the working directory — actual path is the wonderland repo root)

- [ ] **Step 1: Add the import to the Rules section**

Add to the Rules section in CLAUDE.md, after the last `@` import:

```
@docs/rules/static-analysis.md
```

- [ ] **Step 2: Add detekt to the Tech Stack**

The wonderland CLAUDE.md does not have a Tech Stack section (that's in `current/CLAUDE.md`). Skip this — the rules file import is sufficient.

- [ ] **Step 3: Update current/CLAUDE.md Tech Stack**

In `/Users/muttley/Miss_Charming/current/CLAUDE.md`, add to the Tech Stack list:

```
- detekt + ktlint wrapper + compose-rules (static analysis)
```

- [ ] **Step 4: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: import static analysis rules in CLAUDE.md"
```

Note: `current/CLAUDE.md` is outside the wonderland repo — commit it separately or skip if it's not version-controlled.

---

### Task 12: Verify end-to-end

- [ ] **Step 1: Run full detekt across all modules**

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL, zero violations

- [ ] **Step 2: Run Android Lint**

Run: `./gradlew lint`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Test pre-commit hook with a real commit**

Make a trivial change (e.g., add a blank line to a file), stage it, and commit. Verify detekt runs and the commit succeeds.

- [ ] **Step 4: Test pre-commit hook catches violations**

Introduce a deliberate violation (e.g., add `val x = 42` as a magic number in a non-exempt location), stage it, and commit. Verify the hook blocks the commit. Then revert the change.
