# Testing Rules — Non-Negotiable

---

## Test Hierarchy

Follow in order. Never skip levels.

1. **JUnit 4 JVM test** — the default. Always try this first.
2. **+ Robolectric** — only when Android framework is genuinely needed.
3. **+ MockK** — only when a real or fake cannot be used. Never mock what can be faked.
4. **Android instrumented test** — last resort. Must be explicitly justified with a comment.

---

## Required Libraries & Patterns

- **AssertK** for all assertions — not JUnit assert methods, not Truth
- **Turbine** for any test involving Flow or StateFlow — never collect a Flow without Turbine
- **AAA structure** (Arrange / Act / Assert) in every test
- **`runBlocking` is banned in tests** — use `runTest` from `kotlinx-coroutines-test`
- **Never mock types you do not own**
- **Never add Android framework types to unit tests**

---

## Test Data

- All test data from `:core:testfixtures` — no inline data in test classes
- `kotlin-faker` (version from libs.versions.toml) for dynamic data — always seeded, always in testfixtures
- testfixtures categories:
  - **canonical** — fixed spec vectors (immutable, referenced by name)
  - **dynamic** — faker builders (seeded, reproducible)

---

## Coverage & Spec Vectors

- **Spec vectors first:** every crypto function is tested against official specification test vectors before any other test is written
- Broadcast intent-to-violation mapping is a pure function — tested exhaustively with pure unit tests (every action string, every state value, every null/unknown case)

---

## Coverage Expectations

- `:core:cryptography` module: 100% line coverage — no exceptions
- `:core:protocol` serialisation: 100% line coverage
- Use cases: 100% branch coverage (every Either path)
- Presenters: every state transition covered
- UI: every screen archetype has at least one state test
- Overall: 80% minimum — coverage is a trailing indicator, test quality matters more

---

## Security Tests

- Key material zeroed after use (verify byte arrays cleared)
- No key material in logs at any level
- Hard violations trigger cryptographic zeroing (Alice only)
- StrongBox rejects invalid device state (Alice only)
- Malformed payloads → `Either.Left`, never crash

---

## What Does NOT Get Tested

- Android framework internals (`Context`, `Activity`, etc.)
- Koin wiring (validated at app startup)
- Third-party library internals (ML Kit, Bouncy Castle)
- Private functions — test through public callers

---

## Air-Gap Surveillance Testing

- All Android system calls hidden behind provider interfaces injected via Koin
- Tests use `FakeAirGapSurveillance`, never real system adapters
- Broadcast injection tests use Robolectric
- Physical hardware events verified manually during security acceptance testing on target GrapheneOS device
- Never mock the `BroadcastReceiver` itself — test `intentToViolation` and provider interfaces directly
