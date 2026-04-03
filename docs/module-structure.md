# Wonderland — Module Structure (Single Source of Truth)

Every other document that references the module structure must point here. Do not duplicate this tree elsewhere.

---

## Full Module Map

```
wonderland/
├── build-logic/            — Convention plugins (binary .kt, not precompiled scripts)
│
├── core/
│   ├── protocol/           — CBOR schemas, type/version bytes, capability flags, all QR types
│   ├── cryptography/       — IVaultCryptographyEngine + VaultCryptographyEngine
│   │                         EntropyPool, BouncyCastle wrappers, Double Ratchet, X3DH
│   ├── qr/                 — ZXing encode/decode wrapper
│   ├── common/             — base-37 encoding, padding, utilities
│
├── testfixtures/              — shared test fakes, data builders, canonical spec vectors
│
├── alice/                  — Air-gapped crypto vault (minSdk 33, GrapheneOS)
│   ├── app/
│   ├── core:surveillance_api/   — pure Kotlin domain types (IAirGapSurveillance, AirGapViolation, AirGapStatus)
│   ├── core:surveillance/       — Android implementation (BroadcastReceiver, NetworkCallback, WorkManager, Koin)
│   ├── core:ui/                 — Alice Compose theme
│   ├── feature:contacts/
│   ├── feature:keygen/
│   ├── feature:messaging/
│   ├── feature:pairing/
│   ├── feature:scanner/
│   ├── feature:settings/
│   └── feature:delivery/        — tentative
│
└── bob/                    — Online courier (minSdk 26)
    ├── app/
    ├── core:ui/            — Bob Compose theme
    ├── feature:contacts/
    ├── feature:delivery/
    ├── feature:pairing/
    ├── feature:receive/
    ├── feature:scanner/
    └── feature:settings/
```

---

## Dependency Rules

```
alice/* and bob/*  → may depend on core/*
core/*             → must not depend on alice or bob
feature:X (impl)   → must not be depended on by any other feature
:testfixtures      → testImplementation scope only
```

No circular dependencies. No upward dependencies from core to app.

Feature modules depend on `surveillance_api` only — never on `surveillance` directly.

---

## :api / :impl Split Rules

Apply only where there are cross-feature or cross-app consumers. Not blindly to every module.

**:api exposes:**
- Sealed interface State / Intent / Effect
- Navigation interface
- Public data models

**:impl contains:**
- Presenter / ViewModel
- Composables
- Room queries
- Koin module

Other features depend on `:api` only. Never on the impl module directly.

---

## Package Naming

Packages mirror the module hierarchy exactly:
- Alice root: `org.alice.rabbit.hole` + module path segments
  - e.g. `org.alice.rabbit.hole.core.cryptography`
  - e.g. `org.alice.rabbit.hole.feature.contacts`
- Bob root: `org.bob.cheshire.cat` + module path segments

---

## Build Order

1. `core:protocol` — schemas and type/capability bytes
2. `core:cryptography` — IVaultCryptographyEngine and implementations
3. `core:qr` — barcode scanner wrapper
4. `core:common` — utilities
5. `testfixtures` — test infrastructure
6. `alice` app modules
7. `bob` app modules
