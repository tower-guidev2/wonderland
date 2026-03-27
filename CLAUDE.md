# Wonderland — Claude Code Project Context

This file is read at the start of every session. It is the single source of truth for project context, decisions, and rules. Read it fully before doing anything.

---

## What This Project Is

**Wonderland** is the codebase for **Alice & Bob** — a secure anonymous messaging system built as two companion Android applications. The design goal is 1 billion worldwide users. The full design document is at `docs/design.md`. Read it before making any architectural or protocol decisions.

### The Two Apps

| | Alice | Bob |
|---|---|---|
| **Package** | `dev.misscharming.alice` | `dev.misscharming.bob` |
| **minSdk** | 33 (GrapheneOS / Pixel 6+) | 26 (widest Android reach) |
| **Role** | Air-gapped crypto vault | Online courier / transport relay |
| **Rule** | Never touches the network | Never touches plaintext |

Alice and Bob communicate **exclusively via QR codes**. No other channel exists or is permitted.

---

## Module Structure

```
wonderland/
├── shared/
│   ├── protocol/       — CBOR schemas, type/version bytes, all QR types
│   ├── cryptography/   — VaultCryptoEngine interface + KotlinVaultCryptoEngine
│   │                     entropy pool, BouncyCastle wrappers, Double Ratchet, X3DH
│   ├── qr/             — ZXing encode/decode wrapper
│   ├── common/         — base-37, padding, utilities
│   └── testfixtures/   — shared test fakes, data builders, test utilities
│
├── alice/              — D1 Vault app (minSdk 33)
│   ├── app/
│   ├── feature:messaging/
│   ├── feature:contacts/
│   ├── feature:pairing/
│   ├── feature:keygen/
│   └── feature:settings/
│
└── bob/                — D2 Courier app (minSdk 26)
    ├── app/
    ├── feature:scanner/
    ├── feature:contacts/
    ├── feature:delivery/
    ├── feature:receive/
    ├── feature:pairing/
    └── feature:settings/
```

---

## Naming Rules — Non-Negotiable

These are locked decisions. Do not revisit, suggest alternatives, or silently revert to any external standard.

### Identifiers
- **No abbreviations. Ever.** Full words always. `cryptography` not `crypto`. `identifier` not `id`.
- **No `lateinit`. Ever.**
- **No backtick function names. Ever.** Including in tests.
- **No magic numbers or strings.** Extract to named constants.
- **Short, sharp names.** Single words where possible. Max ~30 chars. Uncle Bob self-documenting.
- **No plurals** unless the thing genuinely is a collection.
- **Matching pairs and word families** — names in a set must be from the same vocabulary.
- **No negative-logic names.** Name what something *is*, not what it isn't.
- **Prefer `==` over `!=`.** Structure `if/else` with the positive case first.
- **Never use `!` for negation.** Use `.not()` instead.
- **Single-expression `if` branches with no `{}` braces** — only when the branch is one line.
- **Line width: 180 characters.** This overrides every external standard.

### Modules
- **No hyphens in module names.** Use `:` hierarchy — `core:ui`, not `core-ui`.
- **Module name segments are single lowercase words.** Never abbreviated.
- **Package names mirror module hierarchy exactly.**
  - Root app package + module path segments.
  - Alice: `dev.misscharming.alice` + module `core:cryptography` → `dev.misscharming.alice.core.cryptography`
  - Bob: `dev.misscharming.bob` + module path

---

## Coding Style — Non-Negotiable

These rules override every Google guide, Now in Android reference, and linter default. They are not up for debate.

- User's style wins. Always. Never suggest "the standard way is..." for anything already decided.
- The rules above are locked. Do not re-open them.

---

## Technology Stack

### All modules
- Kotlin (pure — no Java)
- `kotlinx-coroutines-android` — async
- `kotlinx-serialization-cbor` — CBOR encoding (not `co.nstant.in:cbor`)
- `koin-android` — DI (not Hilt, not Dagger)

### shared:cryptography
- `org.bouncycastle:bcprov-jdk18on` — ALL crypto primitives. No other library does crypto.
- Primitives: X25519, Ed25519, HKDF-SHA256, ChaCha20-Poly1305, SHA-256, Argon2id
- Protocol: X3DH + Double Ratchet — pure Kotlin, no library
- Compression: `com.github.luben:zstd-jni` — Phase 1 only

### shared:qr
- `com.google.zxing:core` — encode/decode (no Android dependency)

### Alice
- `androidx.room` + `net.zetetic:android-database-sqlcipher` — encrypted storage
- `androidx.camera:camera-camera2/lifecycle/view` — CameraX
- `androidx.navigation3:navigation3-runtime/ui:1.0.1` — Navigation 3 stable
- Jetpack Compose BOM
- Custom `InputMethodService` keyboard — covers a-z, 0-9, space only. `FLAG_SECURE` set.

### Bob
- `androidx.room` + SQLCipher — encrypted contact directory
- CameraX, Navigation 3, Compose BOM (same as Alice)
- Deep link delivery: SMS (`smsto:`), WhatsApp (`whatsapp://send`), Telegram (`tg://resolve`)

### Deliberately excluded
- No Retrofit / OkHttp in Alice — air-gapped
- No Firebase / Analytics anywhere
- No Hilt / Dagger — Koin is sufficient
- No `androidx.biometric` — bypassable, PIN only
- No JUnit 5 — JUnit 4 preferred
- No `co.nstant.in:cbor` — replaced by `kotlinx-serialization-cbor`

---

## Testing Rules — Non-Negotiable

Follow this hierarchy in order. Never skip levels.

1. **JUnit 4 JVM test** — the default. Always try this first.
2. **+ Robolectric** — only when Android framework is genuinely needed.
3. **+ MockK** — only when a real or fake cannot be used. Never mock what can be faked.
4. **Android instrumented test** — last resort. Must be explicitly justified with a comment.

**Additional rules:**
- **AssertK** for all assertions — not JUnit assert methods, not Truth
- **Turbine** for any test involving Flow or StateFlow
- **AAA structure** (Arrange / Act / Assert) in every test
- All test data from `:shared:testfixtures` — no inline data in test classes
- `kotlin-faker 1.16.1` for dynamic data — always seeded, always in testfixtures
- testfixtures has two categories: **canonical** (fixed spec vectors) and **dynamic** (faker builders)
- **Coverage target:** 90%+ on `shared:cryptography` and `shared:protocol`
- **Spec vectors first:** every crypto function is tested against official specification test vectors before any other test is written

---

## Cryptographic Protocol — Key Decisions

The full spec is in `docs/design.md` Section 7. Key points:

- **All crypto on Alice only.** Bob never sees plaintext or session keys.
- **Pipeline (immutable):** Composition → Padding → Compression → Encryption → CBOR → QR
- **Identity Key is two separate key pairs:** IK_Ed (Ed25519, signing only) + IK_X (X25519, DH only). Never mixed.
- **HKDF info strings** (namespace reserved `AliceBob_v1_`):
  - X3DH: `AliceBob_v1_X3DH`
  - Double Ratchet root: `AliceBob_v1_DR_RK`
  - MAC: `AliceBob_v1_MAC`
  - SAS: `AliceBob_v1_SAS`
- **Argon2id params:** 64 MiB, 3 iterations, parallelism 4, 32-byte output
- **VaultCryptoEngine** is the single interface boundary for all crypto. Phase 1 = `KotlinVaultCryptoEngine`. Phase 2 = `RustVaultCryptoEngine` via UniFFI. Swap is one Koin line.

---

## QR Protocol — Key Decisions

Full field registries in `docs/design.md` Section 6.

- **Wire format:** `[type: 1 byte][capabilities: 2 bytes][CBOR payload]`
- **11 QR types** defined. Types 8 and 9 deferred to V2.
- **CBOR integer keys only.** No string keys.
- **Strictly additive schema evolution.** Field numbers are permanent. Never reused. Never redefined.
- **Unknown fields silently ignored** — baked in from day one.
- **Dual-QR message transmission:** every message produces Type 3 + Type 10 simultaneously. Bob scans both in one camera frame via ML Kit bundled model.

---

## Phase 1 vs Phase 2

- **Phase 1** — pure Kotlin PoC. Proves the protocol, UX, and pipeline end-to-end. Uses `KotlinVaultCryptoEngine`. Distributes via GitHub Releases APK.
- **Phase 2** — Rust security layer on Alice via Android NDK + UniFFI. Replaces crypto engine only. Phase 1 code is not evolved — Phase 2 starts clean.
- The `VaultCryptoEngine` interface is the boundary. Define it right in Phase 1. Nothing else changes in Phase 2.

---

## Open Items — Required Before Coding

From `docs/design.md` Section 9:

1. **Capability flag registry** — bits 0–15 of the uint16 capability mask
2. **SAS wordlist** — selection and embedding of the 2,048-word EFF word list
3. **UI verification states** — visual treatment of Verified vs Unverified contacts

---

## Available Skills

These custom skills are available in this project. Use them — they are the correct expert lenses for this codebase.

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
| `/mc-design` | UI/UX design review |

---

## Repository

- **GitHub:** `tower-guidev2/wonderland`
- **Local:** `/Users/muttley/Miss_Charming/wonderland`
- **Design document:** `docs/design.md` (Draft 0.4)
- **Working directory for Miss Charming:** `/Users/muttley/Miss_Charming`
- **Test / reference project (Alice prototype):** `/Users/muttley/secret_squirrel/alice`
