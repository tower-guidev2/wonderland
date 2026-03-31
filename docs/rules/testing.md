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
- `kotlin-faker 1.16.1` for dynamic data — always seeded, always in testfixtures
- testfixtures categories:
  - **canonical** — fixed spec vectors (immutable, referenced by name)
  - **dynamic** — faker builders (seeded, reproducible)

---

## Coverage & Spec Vectors

- **Coverage target:** 90%+ on `core:cryptography` and `core:protocol`
- **Spec vectors first:** every crypto function is tested against official specification test vectors before any other test is written
- Broadcast intent-to-violation mapping is a pure function — tested exhaustively with pure unit tests (every action string, every state value, every null/unknown case)

---

## Air-Gap Surveillance Testing

- All Android system calls hidden behind provider interfaces injected via Koin
- Tests use `FakeAirGapSurveillance`, never real system adapters
- Broadcast injection tests use Robolectric
- Physical hardware events verified manually during security acceptance testing on target GrapheneOS device
- Never mock the `BroadcastReceiver` itself — test `intentToViolation` and provider interfaces directly
