# DECISIONS.md — Architecture Decision Records

## Purpose

Settled decisions that must not be relitigated without new evidence. If you disagree with a decision here, raise it explicitly — do not silently deviate.

---

## ADR-001: ZXing Rejected for QR Scanning

**Context**: Needed a QR scanning library for both Alice and Bob.
**Decision**: Use ML Kit bundled variant. ZXing is rejected.
**Rationale**: ML Kit bundled runs fully offline (critical for Alice), provides superior scan performance, and has active Google maintenance.
**Consequence**: Larger APK due to bundled models. Acceptable tradeoff.

---

## ADR-002: Arrow Either for Error Handling

**Context**: Needed a consistent error handling strategy across all layers.
**Decision**: Use Arrow Either. No exceptions for expected failure paths.
**Rationale**: Makes error paths explicit in type signatures. Composes cleanly across use cases. Eliminates silent failure.
**Consequence**: All use cases return `Either<DomainError, T>`. UI layer maps Left to user-visible error state.

---

## ADR-003: Molecule over Standalone Presenter

**Context**: Evaluated Molecule by Jake Wharton for MVI state management.
**Decision**: Use `launchMolecule` inside `viewModelScope`. Reject standalone Presenter pattern.
**Rationale**: Direct ViewModel integration avoids an unnecessary abstraction layer. Presenter pattern adds indirection with no benefit for this project's scale.
**Consequence**: Presenters are Composable functions invoked inside the ViewModel. Testing uses Molecule's test utilities.

---

## ADR-004: Core Module Naming

**Context**: Needed a shared utilities/foundation module.
**Decision**: Name it `:core`, not `:shared` or `:common`.
**Rationale**: Project convention. Clear, unambiguous, single word.
**Consequence**: All cross-cutting foundation code lives in `:core`.

---

## ADR-005: Koin Annotations for Dependency Injection

**Context**: Needed a DI framework.
**Decision**: Use Koin Annotations. Reject Hilt/Dagger.
**Rationale**: Annotation-first DI with compile-time safety via KSP. Pure Kotlin, simpler for a project that spans two distinct app targets (Alice and Bob) with different module graphs.
**Consequence**: `@Module`, `@Single`, `@Factory`, `@KoinViewModel` annotations. DSL used only where annotations cannot express the binding.

---

## ADR-006: Value Classes for All Function Arguments

**Context**: Needed to prevent primitive obsession and argument transposition bugs, especially in crypto operations where passing the wrong key is catastrophic.
**Decision**: All function arguments use Kotlin value classes.
**Rationale**: Compile-time type safety with zero runtime overhead. A `RecipientPublicKey` cannot be accidentally passed where a `SenderPrivateKey` is expected.
**Consequence**: Every domain type that wraps a primitive gets a value class. Slightly more boilerplate, dramatically safer.

---

## ADR-007: kotlinx CBOR for Serialisation

**Context**: Needed a compact binary serialisation format for QR payloads.
**Decision**: Use kotlinx.serialization with CBOR format.
**Rationale**: CBOR is compact (critical for QR capacity limits), well-specified (RFC 8949), and kotlinx.serialization provides compile-time safety.
**Consequence**: All QR payloads are CBOR-encoded. JSON is not used for wire format.

---

## ADR-008: No Key Persistence

**Context**: Needed to decide whether private keys or session keys are stored.
**Decision**: No private or session keys are ever persisted to Room or any storage.
**Rationale**: If the device is seized, there is nothing to extract. Ephemeral keys are generated per message and zeroed after use.
**Consequence**: Key material exists only in memory during active operations. Bundle management must account for this constraint.

---

## ADR-009: Navigation 3

**Context**: Needed a navigation framework for single-activity Compose architecture.
**Decision**: Use Jetpack Navigation 3 (version 1.0.1).
**Rationale**: Compose-native, type-safe, replaces fragment-based navigation.
**Consequence**: Single Activity, all navigation Compose-driven.

---

## ADR-010: Mandatory First Modifier — semanticsSealed()

**Context**: Accessibility services are a data exfiltration vector on air-gapped devices.
**Decision**: `semanticsSealed()` is the mandatory first modifier on every Composable.
**Rationale**: Custom extension wraps `clearAndSetSemantics {}` with a lint rule (`SemanticsNotSealedDetector`) enforcing at ERROR severity.
**Consequence**: No Composable emits UI without `semanticsSealed()` first in the modifier chain.

---

## ADR-011: Testing Framework — JUnit 4

**Context**: Needed a standard testing framework.
**Decision**: JUnit 4. JUnit 5 explicitly excluded.
**Rationale**: Project standard.
**Consequence**: `@Test` from `org.junit`, `@Ignore` for skipped tests. Use AssertK (not Truth or JUnit assert), Turbine for Flow testing, `runTest` for coroutines.

---

## Template for New Decisions

```
## ADR-NNN: [Title]

**Context**: [What problem or choice prompted this decision]
**Decision**: [What was decided]
**Rationale**: [Why this option was chosen over alternatives]
**Consequence**: [What follows from this decision — tradeoffs, constraints, implications]
```
