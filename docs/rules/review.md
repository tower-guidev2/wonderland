# Review Process & Scoring Rubric

CC performs first-pass review. Frank has final authority to approve, reject, or send back.

---

## Stage 1: Self-Review (CC, before presenting)

Before presenting any code to Frank, verify against every applicable control document.

- [ ] **Architecture** — module boundaries and dependency rules per `architecture.md`
- [ ] **Decisions** — all settled choices in `docs/decisions.md` honoured, nothing relitigated
- [ ] **Contracts** — cross-module interfaces match `docs/api-contracts.md`, all parameter types are value classes
- [ ] **Code quality** — conforms to `quality.md` and `naming.md`
- [ ] **Patterns** — established patterns from `docs/patterns.md` used where applicable
- [ ] **Compose** — `semanticsSealed()` is the first modifier on every Composable, UI meets `docs/compose-ui.md`
- [ ] **UX** — interaction behaviour matches `docs/ux-patterns.md`, empty states handled, destructive actions confirmed
- [ ] **Tests** — included per `testing.md`, follow `/mc-test` scaffolding
- [ ] **Security** — no key material logged, keys zeroed after use, no persistence of private/session keys, value classes on all crypto boundaries
- [ ] **Build variants** — correct behaviour for both Alice and Bob per `docs/build-variants.md`

If any item fails, fix it before presenting. Do not present work with known violations.

---

## Stage 2: Scoring (CC, after self-review passes)

Score the work using the rubric below. If below pass threshold, rework before presenting. Present the score alongside the work.

---

## Stage 3: Frank Review

Possible outcomes:
- **Approved** — merged, no changes needed.
- **Approved with notes** — merged. CC records notes and updates relevant control documents if they reflect a new convention.
- **Revise** — specific issues identified. CC addresses them, returns to Stage 1.
- **Reject** — fundamental approach wrong. CC re-reads control documents, starts from scratch.

---

## Stage 4: Post-Merge Maintenance

- [ ] Update `docs/patterns.md` if new reusable pattern established
- [ ] Update `docs/decisions.md` if new architectural choice settled
- [ ] Update `docs/api-contracts.md` if new cross-module interfaces created
- [ ] Update any control document the work has made stale

---

## Review Scope by Change Type

| Change Type | Self-Review Depth | Scoring | Frank Review |
|---|---|---|---|
| New feature | Full checklist | Yes | Always |
| Bug fix | Relevant items + regression test | Yes | Always |
| Refactor | Architecture + patterns + tests pass | Yes | Always |
| Documentation only | Accuracy and consistency | No | Lightweight |
| Dependency update | Build succeeds + tests pass | No | Always |

---

## Scoring Rubric

### Scale

| Score | Meaning |
|---|---|
| 0 | Not addressed |
| 1 | Attempted but fundamentally broken |
| 2 | Partially complete with significant issues |
| 3 | Functional with notable gaps or deviations |
| 4 | Solid — meets expectations, minor polish needed |
| 5 | Exemplary — full standard, no caveats |

### Categories and Weights

| # | Category | Weight | Max | Evaluates |
|---|---|---|---|---|
| 1 | Correctness | x3 | 15 | Works per spec, edge cases handled |
| 2 | Security | x3 | 15 | Key zeroing, no key persistence/logging, value classes on crypto boundaries, air-gap integrity (Alice) |
| 3 | Architecture Compliance | x2 | 10 | Module boundaries, layer separation, zero technology leakage per `architecture.md` |
| 4 | Pattern Compliance | x2 | 10 | Established patterns from `docs/patterns.md` used, no reinvented alternatives |
| 5 | Test Coverage | x2 | 10 | Tests per `testing.md`, happy + error paths covered |
| 6 | Code Quality | x1 | 5 | `quality.md` + `naming.md` compliance, comments explain why not what |
| 7 | UI Quality | x1 | 5 | `semanticsSealed()` first, pixel-perfect, fine-art bar per `docs/compose-ui.md` |
| 8 | Contract Compliance | x1 | 5 | Interfaces match `docs/api-contracts.md`, value classes on all public signatures, Either return types |
| 9 | Decision Compliance | x1 | 5 | No settled decisions from `docs/decisions.md` relitigated without approval |
| 10 | Documentation | x1 | 5 | KDoc on public APIs, control documents updated if conventions changed |

**Maximum: 85**

### Thresholds

| Threshold | Score | Action |
|---|---|---|
| Pass | 72+ (85%) | Present to Frank |
| Conditional | 60–71 (70–84%) | Rework weakest categories, re-score, present with explanation |
| Fail | Below 60 (<70%) | Mandatory rework. Do not present. Re-read control documents first. |

### Hard Fails (automatic fail regardless of score)

Any single occurrence:
- Private or session key persisted to storage
- Key material logged at any level
- `semanticsSealed()` missing or not the first modifier on any Composable
- Gradle plugin referenced as string literal instead of `alias(libs.plugins.*)`
- Technology leakage across architecture layers (e.g., Android import in domain)
- Settled decision from `docs/decisions.md` silently overridden
- Work presented with known violations flagged as "known issues" instead of fixed
- Tests missing for crypto operations

### Score Card Template

```
## Score Card: [Feature/Change Name]

| # | Category | Score (/5) | Weighted | Notes |
|---|---|---|---|---|
| 1 | Correctness | /5 | /15 | |
| 2 | Security | /5 | /15 | |
| 3 | Architecture Compliance | /5 | /10 | |
| 4 | Pattern Compliance | /5 | /10 | |
| 5 | Test Coverage | /5 | /10 | |
| 6 | Code Quality | /5 | /5 | |
| 7 | UI Quality | /5 | /5 | |
| 8 | Contract Compliance | /5 | /5 | |
| 9 | Decision Compliance | /5 | /5 | |
| 10 | Documentation | /5 | /5 | |

**Total: /85**
**Threshold: [Pass / Conditional / Fail]**
**Hard Fails: [None / List any]**
```

### Calibration

Frank may adjust weights, thresholds, or hard fail criteria at any time. If Frank consistently scores differently from CC's self-assessment, CC recalibrates and notes the adjustment.

### Scoring Integrity

- Score honestly. Inflating to clear threshold defeats the purpose.
- Uncertain → err lower and explain why.
- 5 means "Frank would find nothing to improve." Do not award 5 unless genuinely true.
- The rubric measures against this project's standards, not general industry practice.

---

## Escalation Rules

- Uncertain whether a change violates a control document → ask Frank, don't guess.
- Believe a control document is wrong or incomplete → raise explicitly, never silently deviate.
- Self-review reveals a gap in a control document → flag it and propose an addition.

---

## What CC Must Never Do

- Present work with violations listed as "known issues"
- Skip self-review because the change is "small" or "obvious"
- Approve its own work without scoring
- Merge without Frank's explicit approval
