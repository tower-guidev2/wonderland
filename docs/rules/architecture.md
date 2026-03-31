# Architecture Rules

---

## Module Map

```
wonderland/
├── core/
│   ├── protocol/       — CBOR schemas, type/version bytes, all QR types
│   ├── cryptography/   — IVaultCryptographyEngine + VaultCryptographyEngine
│   │                     entropy pool, BouncyCastle wrappers, Double Ratchet, X3DH
│   ├── qr/             — ZXing encode/decode wrapper
│   ├── common/         — base-37, padding, utilities
│   └── testfixtures/   — shared test fakes, data builders, test utilities
│
├── alice/              — Air-gapped crypto vault (minSdk 33)
│   ├── core:surveillance_api/ — pure Kotlin domain types (IAirGapSurveillance, AirGapViolation, AirGapStatus)
│   ├── core:surveillance/    — Android implementation (BroadcastReceiver, NetworkCallback, WorkManager, Koin)
│   ├── core:ui/              — Alice Compose theme (feminine, Rolls Royce quality)
│   ├── app/
│   ├── feature:contacts/
│   ├── feature:keygen/
│   ├── feature:messaging/
│   ├── feature:pairing/
│   ├── feature:scanner/
│   ├── feature:settings/
│   └── feature:delivery/     — tentative
│
└── bob/                — Online courier (minSdk 26)
    ├── core:ui/        — Bob Compose theme (masculine, same font family as Alice)
    ├── app/
    ├── feature:scanner/
    ├── feature:contacts/
    ├── feature:delivery/
    ├── feature:receive/
    ├── feature:pairing/
    └── feature:settings/
```

---

## Clean Layer Rules

- Zero technology leakage between layers.
- No Android framework types in domain interfaces or use-case layers.
- Feature modules depend on `surveillance_api` only — never on `surveillance` directly.

---

## Presenter Architecture — Molecule First

Phase 1a: **Molecule-style Compose-runtime presenters**. `@Composable` presenter functions return state — they do not emit UI. Wrapped as `StateFlow` via `launchMolecule`.

Reference: https://github.com/cashapp/molecule

```kotlin
@Composable
fun messagingPresenter(events: Flow<MessagingEvent>): MessagingState {
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is MessagingEvent.Send -> { /* update messages */ }
            }
        }
    }
    return MessagingState(messages)
}
```

Phase 1b fallback: ViewModel-based MVI with `StateFlow` + `dispatch()`. Switch is per-feature — never mix patterns within a single feature.

---

## Phase 1 vs Phase 2

- **Phase 1** — pure Kotlin PoC. Proves protocol, UX, and pipeline end-to-end. Uses `VaultCryptographyEngine`. Distributes via GitHub Releases APK.
- **Phase 2** — native security layer via Android NDK + UniFFI. Replaces cryptography engine only. Phase 1 code is not evolved — Phase 2 starts clean.
- `IVaultCryptographyEngine` is the boundary. Define it right in Phase 1. Nothing else changes in Phase 2.
