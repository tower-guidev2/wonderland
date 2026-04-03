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
