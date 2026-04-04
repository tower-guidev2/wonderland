# Workflow — Stage Sequencing

This file is the tiebreaker when it's unclear which document governs a decision.

---

## Feature Workflow

### 1. Understand Requirements
- Read `docs/prd.md` for the relevant feature/user story.
- Confirm acceptance criteria before proceeding.
- If requirements are ambiguous, ask — do not assume.

### 2. Validate Architecture Fit
- Check `architecture.md` for module placement and layer responsibility.
- Check `docs/decisions.md` to confirm no settled choices are being relitigated.
- New module or changed boundary → document in `docs/decisions.md` before writing code.

### 3. Define Interfaces
- Check `docs/api-contracts.md` for existing contracts the feature must honour.
- New cross-module interfaces → define in `docs/api-contracts.md` first.

### 4. Implement
- Apply `quality.md` and `naming.md` for all code style and convention.
- Match `docs/patterns.md` for any problem class with an established template.
- Apply `docs/compose-ui.md` for all UI work.
- Apply `docs/ux-patterns.md` for navigation and interaction behaviour.
- Do not invent new patterns when an existing one applies.

### 5. Test
- Follow `testing.md` for coverage expectations and test type selection.
- Use `/mc-test` skill for reusable test scaffolding.
- Tests must pass before any PR or merge.

### 6. Review and Score
- Complete self-review per `review.md` Stage 1 checklist.
- Score using `review.md` scoring rubric.
- Score below 72/85 → rework before presenting.
- Any hard fail → fix. No exceptions.
- Present to Frank with completed score card.

### 7. Pre-Release Validation
- Walk `docs/release-checklist.md` item by item.
- Confirm `docs/build-variants.md` for variant-specific behaviour.

---

## Bug Fix Workflow

1. Reproduce and confirm the bug.
2. Check `docs/patterns.md` — does an existing pattern address this class of problem?
3. Fix, applying `quality.md` and `naming.md`.
4. Add a regression test per `testing.md`.
5. Update `docs/patterns.md` if the fix establishes a new reusable template.

---

## Refactor Workflow

1. Confirm the refactor does not change external behaviour.
2. Ensure existing tests pass before starting.
3. Apply in small, independently verifiable steps.
4. Confirm all tests pass after each step.
5. Update `docs/patterns.md` or `docs/decisions.md` if the refactor changes an established convention.

---

## Design Spec Workflow

Every new design spec must be cross-referenced against locked protocol rules before presenting for review:

- `docs/rules/qr-protocol.md` — wire format, CBOR integer key convention, QR type registry
- `docs/rules/crypto-protocol.md` — HKDF info string registry, primitive selection, key architecture
- `docs/design.md` — field registries, settled decisions, type assignments

Wire format, CBOR field keys, HKDF info strings, and QR type assignments must match locked decisions. Catch conflicts at spec time, not at review time.

---

## Document Maintenance

After completing any significant work:
- Update the relevant control documents if the work changes conventions, patterns, or decisions.
- Do not let documents drift from reality.
