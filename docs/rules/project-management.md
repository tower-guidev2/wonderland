# Project Management Rules

---

## Prioritisation Tiers

Every task falls into one of three tiers:

- **Must Ship** — non-negotiable deliverables, blocking bugs, external commitments.
- **Should Ship** — valuable work without hard deadlines. Quality, debt reduction, user value.
- **Nice to Have** — improvements that can wait indefinitely.

Focus on Must Ship. Interleave Should Ship when Must Ship is clear. Nice to Have only enters the pipeline when the other tiers are under control.

---

## Branching Strategy

GitHub Flow. Simple, sufficient for solo + CC.

- **`main`** — always stable, always deployable. Every merge is a releasable state.
- **`feature/*`, `fix/*`, `refactor/*`** — short-lived branches for specific work. Name descriptively: `feature/air_gap_monitor`, `fix/leak_canary_callback`, `refactor/snackbar_routing`.
- **Safety branches** — before risky merges or large refactors, create a backup branch preserving current state. Cheap insurance.
- **Tag releases** — semantic versioning: `v1.2.3`. Tag the exact commit that ships.
- **Never force-push to main.**

---

## Technical Debt

### Track It

Maintain a debt log. Every shortcut, TODO, hack, or known problem gets an entry: what, where, why incurred, estimated impact, estimated effort.

### Prioritise by Impact

High: debt affecting security, performance, or causing recurring bugs. Target files that change most frequently — git activity heatmap reveals where debt causes the most friction. Medium: maintainability and velocity. Low: cosmetic issues with no current impact.

### The 20% Rule

~20% of development time goes to debt reduction. Weave into feature work (Boy Scout Rule) or batch into dedicated sessions. Consistency matters more than volume — regular small reductions prevent compounding.

### Safety

Never refactor without tests. Write characterisation tests first if coverage is missing. Refactor in small verifiable steps. Commit between steps.

---

## Weekly Cadence

- Review and update task priorities against the three tiers.
- Address at least one technical debt item.
- Update CLAUDE.md / skills / rules if patterns evolved during the week.
