# Wonderland — Claude Code Project Context

Read this file fully before doing anything. It is the single source of truth.
When resuming work, read design docs and recent files before asking the user to re-explain context.
Grok has contributed sections to design documents — treat those as authoritative.

---

## What This Project Is

**Wonderland** is the codebase for **Miss Charming** — the most secure messaging Android application in existence.
Two companion apps communicate exclusively via QR codes. Zero server involvement. Zero plaintext on any networked device.
Full cryptographic protocol: X3DH + Double Ratchet. Threat model: every layer of the stack is compromised. Target: 1 billion users.

| | Alice | Bob |
|---|---|---|
| **Package** | `org.alice.rabbit.hole` | `org.bob.cheshire.cat` |
| **minSdk** | 33 (GrapheneOS / Pixel 6+) | 26 (widest Android reach) |
| **Role** | Air-gapped crypto vault | Online courier / transport relay |
| **Rule** | Never touches the network | Never touches plaintext |

Full design: `docs/design.md`. **Read it before any architectural or protocol decision. Do not re-open closed decisions.**

---

## Repository & Paths

- **GitHub:** `tower-guidev2/wonderland`
- **Local:** `/Users/muttley/Miss_Charming/wonderland`
- **Design document:** `docs/design.md` (Draft 0.4)
- **Working directory:** `/Users/muttley/Miss_Charming`
- **Alice prototype reference:** `/Users/muttley/secret_squirrel/alice`

---

## Available Skills

| Command | Purpose |
|---|---|
| `/mc-crypto` | Cryptography — X25519, Ed25519, Double Ratchet, X3DH, BouncyCastle, Argon2id |
| `/mc-security` | Security review against the Miss Charming threat model |
| `/mc-android` | Android — Compose, MVI, Navigation 3, CameraX, Room, Koin |
| `/mc-protocol` | QR protocol and encoding |
| `/mc-threat` | STRIDE threat modelling |
| `/mc-test` | Test strategy — spec vectors, property tests, unit tests |
| `/mc-review` | Full holistic code review through all lenses simultaneously |
| `/mc-naming` | Naming decisions across all identifiers |
| `/mc-gradle` | Gradle build, modules, version catalog, ProGuard, NDK |
| `/mc-writing` | Technical writing and documentation standard |
| `/mc-design` | Compose theme, Material3, Alice/Bob design system — Rolls Royce standard |
| `/mc-quality` | Code quality & style — non-negotiable rules for every .kt file |

---

## Open Items — Required Before Coding

From `docs/design.md` Section 9:
1. **Capability flag registry** — bits 0–15 of the uint16 capability mask
2. **SAS wordlist** — selection and embedding of the 2,048-word EFF word list
3. **UI verification states** — visual treatment of Verified vs Unverified contacts

---

## Rules — Loaded On Demand

@docs/rules/quality.md
@docs/rules/naming.md
@docs/rules/architecture.md
@docs/rules/build.md
@docs/rules/dependencies.md
@docs/rules/testing.md
@docs/rules/security.md  
@docs/rules/cyber-security-air-gapped.md  
@docs/rules/cyber-security-networked.md
@docs/rules/crypto-protocol.md
@docs/rules/qr-protocol.md
@docs/rules/zkp.md
@docs/rules/workflow.md
@docs/rules/review.md
@docs/rules/static-analysis.md

---

## Control Documents

Read these before starting feature work:

| Document | Governs |
|---|---|
| `docs/prd.md` | Requirements and acceptance criteria |
| `docs/decisions.md` | Settled architectural choices — do not relitigate |
| `docs/api-contracts.md` | Cross-module interface definitions |
| `docs/compose-ui.md` | Visual standard and Compose conventions |
| `docs/ux-patterns.md` | Navigation, screen archetypes, interaction contracts |
| `docs/patterns.md` | Reusable implementation templates |
| `docs/release-checklist.md` | Pre-publish gate |
| `docs/build-variants.md` | Flavour/build type matrix |

---

## Engineering Principles

- Principles over rules — everything is challengeable with evidence.
- Progressive detail — start broad, refine iteratively.
- Five Year Old Test — if a design decision can't be explained simply, it's wrong.
- All killer, no filler — pure factual, unambiguous communication.
- UI standard is fine art: pixel-perfect, Rolls Royce quality, frictionless UX.

---

## Reviewer

Frank is the final authority on all code review. CC performs self-review and scoring. Frank approves, revises, or rejects. See `review.md` rules.

---

## Self-Archiving Rule

After every session:
- New rules, decisions, or patterns → written to the appropriate `docs/rules/*.md` file, never to root CLAUDE.md
- Root CLAUDE.md is append-prohibited except for new `@import` lines
- Module-specific session knowledge → nearest subdirectory CLAUDE.md
- If no rules file exists for a topic, create one and add the `@import` here, DONT CHANGE ANYTHING ELSE AT ALL!!!!!!!!!!!!!!