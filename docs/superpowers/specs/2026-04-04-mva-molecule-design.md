# MVA with Molecule v2 — Design Spec

**Date:** 2026-04-04
**Status:** Draft — pending review and audit
**Scope:** Establish MVA architecture pattern, create `core/mva` module, migrate AirGap POC ViewModel

---

## 1. Overview

Alice & Bob adopt MVA (Model-View-Action) as the mandatory state management architecture for all ViewModels. MVA is a unidirectional data flow pattern implemented using Cash App's Molecule v2 library inside standard Android ViewModels.

MVA replaces raw `MutableStateFlow` + ad-hoc ViewModel methods with sealed Action/State contracts processed by the Compose runtime.

## 2. Vocabulary

| Term | Definition |
|---|---|
| Action | Sealed interface. Every possible user or system trigger that can change screen state. |
| State | Sealed interface. Every possible screen state at a point in time. Single source of truth. |
| Effect | One-time side effect (navigation, snackbar). Delivered via Channel, consumed exactly once. |
| `reaction()` | The `@Composable` method on the ViewModel that transforms a `Flow<Action>` into `State`. |
| `onAction()` | The single public entry point for the UI to send an Action to the ViewModel. |
| MVA | Model-View-Action. The pattern name. Replaces MVI to avoid collision with Android's `Intent`. |

## 3. Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Pattern name | MVA | Avoids `Intent` collision with `android.content.Intent` |
| Base classes | `MoleculeViewModel<Action, State>` + `MoleculeViewModelWithEffect<Action, State, Effect>` | Simple screens stay simple. Effect-capable screens opt in. |
| Module | `core/mva` | Dedicated module, shared across alice and bob |
| Abstract method | `reaction(action: Flow<Action>): State` | Action → Reaction metaphor. No collision with `state` property. |
| Public API | `state: StateFlow<State>` + `onAction(action: Action)` | Standard Android naming |
| Contract style | Top-level sealed interfaces: `<Name>Action` + `<Name>State` | Zero nesting, clean imports |
| No presenters | Logic lives inline in ViewModel's `reaction()` override | No extra abstraction layer |
| Explicit backing fields | Used where `MutableStateFlow` → `StateFlow` smart-cast applies. Not used for Channel-based Effect. | Pragmatic — use where they shine, traditional pattern where they don't. Requires `-Xexplicit-backing-fields` compiler flag. |
| Molecule version | 2.2.0 stable | Latest stable. No custom Gradle plugin — uses `org.jetbrains.kotlin.plugin.compose`. |

## 4. Architecture

```
User taps button
    → UI calls viewModel.onAction(SomeAction.OnTapped)
        → MutableSharedFlow<Action> buffers the action
            → Compose runtime recomposes reaction()
                → reaction() reads domain Flows via collectAsState()
                → reaction() processes actions via LaunchedEffect
                → reaction() returns new State
                    → StateFlow<State> emits
                        → UI recomposes via collectAsStateWithLifecycle()
```

Molecule sits at exactly one layer: it converts a `@Composable` function into a `StateFlow`. Everything above (UI) and below (domain, repositories, crypto) is the project's architecture, not Molecule's.

## 5. Base Classes

### 5.1 MoleculeViewModel

Lives in `core/mva/src/main/kotlin/org/alice/rabbit/hole/core/mva/`.

```kotlin
// Abstract class justified — framework-forced by ViewModel inheritance and Compose runtime contract.
abstract class MoleculeViewModel<Action, State> : ViewModel() {

    private val scope = CoroutineScope(
        viewModelScope.coroutineContext + AndroidUiDispatcher.Main
    )

    private val action = MutableSharedFlow<Action>(extraBufferCapacity = 20)

    val state: StateFlow<State> by lazy(LazyThreadSafetyMode.NONE) {
        scope.launchMolecule(mode = ContextClock) {
            reaction(action)
        }
    }

    fun onAction(action: Action) {
        if (!this.action.tryEmit(action)) {
            error("Action buffer overflow.")
        }
    }

    @Composable
    protected abstract fun reaction(action: Flow<Action>): State
}
```

Key details:
- `AndroidUiDispatcher.Main` provides `MonotonicFrameClock` for `ContextClock` mode. Never use `Dispatchers.Main` directly.
- `extraBufferCapacity = 20` handles rapid simultaneous UI actions. Overflow crashes immediately to surface bugs.
- `by lazy(LazyThreadSafetyMode.NONE)` defers Compose runtime creation until first UI access. `NONE` is safe — only accessed from main thread.
- First composition is synchronous — `state.value` is available immediately after first access.

### 5.2 MoleculeViewModelWithEffect

```kotlin
abstract class MoleculeViewModelWithEffect<Action, State, Effect> : MoleculeViewModel<Action, State>() {

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    protected fun sendEffect(effect: Effect) {
        _effect.trySend(effect)
    }
}
```

Key details:
- `Channel.BUFFERED` with `receiveAsFlow()` delivers each effect exactly once. No replay. No duplication on config change.
- Traditional `_backing` / `public` pattern used here because `Channel` does not implement `Flow` — explicit backing fields don't apply.
- Not every ViewModel needs effects. Simple screens extend `MoleculeViewModel` directly.

## 6. Contract Pattern

Every feature defines top-level sealed interfaces in the `api/` package:

```kotlin
// AirGapAction.kt
sealed interface AirGapAction {
    data object OnRefreshRequested : AirGapAction
}

// AirGapState.kt
sealed interface AirGapState {
    data object Loading : AirGapState
    data class Content(
        val airGapStatus: AirGapStatus,
        val violation: ImmutableList<AirGapViolation>,
    ) : AirGapState
}
```

Error states are feature-specific. The AirGap surveillance API is a passive observer — it emits status and violation via Flow. Features with fallible operations (network, crypto) define their own `Error` variant with a domain-specific reason type.

Rules:
- `Action` is always a sealed interface. Exhaustive `when` in `reaction()`.
- `State` is always a sealed interface with a finite set of screen states.
- Inner data classes use value classes for all primitive arguments.
- `ImmutableList` from kotlinx-collections-immutable, never raw `List`.
- No technology leakage. No Room entities. No Android types.

## 7. ViewModel Implementation

```kotlin
class AirGapViewModel(
    private val surveillance: IAirGapSurveillance,
) : MoleculeViewModel<AirGapAction, AirGapState>() {

    @Composable
    override fun reaction(action: Flow<AirGapAction>): AirGapState {
        val status by surveillance.status().collectAsState(initial = null)
        val violation by surveillance.violation().collectAsState(initial = persistentListOf())

        LaunchedEffect(Unit) {
            action.collect { receivedAction ->
                when (receivedAction) {
                    is AirGapAction.OnRefreshRequested -> surveillance.refresh()
                }
            }
        }

        if (status == null) return AirGapState.Loading

        return AirGapState.Content(
            airGapStatus = status ?: AirGapStatus.Secure,
            violation = violation,
        )
    }
}
```

## 8. UI Consumption

```kotlin
@Composable
fun AirGapScreen(viewModel: AirGapViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is AirGapState.Loading -> LoadingIndicator()
        is AirGapState.Content -> {
            val content = state as AirGapState.Content
            AirGapContent(
                status = content.airGapStatus,
                violation = content.violation,
                onRefresh = { viewModel.onAction(AirGapAction.OnRefreshRequested) },
            )
        }
    }
}
```

Key points:
- `collectAsStateWithLifecycle()` from `androidx.lifecycle:lifecycle-runtime-compose`. Lifecycle-aware — stops collection when UI is not visible.
- UI only calls `viewModel.onAction(...)`. Never mutates state directly.
- UI only reads `viewModel.state`. Single source of truth.

## 9. Effect Consumption (When Needed)

For ViewModels extending `MoleculeViewModelWithEffect`:

```kotlin
@Composable
fun SomeScreen(viewModel: SomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SomeEffect.NavigateTo -> navController.navigate(effect.route)
                is SomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // ... render state ...
}
```

## 10. Testing

Tests go through the ViewModel directly. No separate presenter to test.

```kotlin
@RunWith(JUnit4::class)
class AirGapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialStateIsContentWithSecureStatus() {
        val fakeSurveillance = FakeAirGapSurveillance()
        val viewModel = AirGapViewModel(surveillance = fakeSurveillance)

        val state = viewModel.state.value
        assertIs<AirGapState.Content>(state)
        assertEquals(AirGapStatus.Secure, state.airGapStatus)
    }

    @Test
    fun violationEmittedUpdatesState() = runTest {
        val fakeSurveillance = FakeAirGapSurveillance()
        val viewModel = AirGapViewModel(surveillance = fakeSurveillance)

        viewModel.state.test {
            awaitItem() // initial

            fakeSurveillance.emitViolation(AirGapViolation.BluetoothEnabled)

            val updated = awaitItem()
            assertIs<AirGapState.Content>(updated)
            assertEquals(1, updated.violation.size)
            cancel()
        }
    }
}
```

Test dependencies: JUnit 4, Turbine, coroutines-test, molecule-runtime. No Robolectric. JVM tests.

## 11. Pure Reducer — Crypto-Critical Flows Only

Not needed for AirGap POC. Established as the pattern for engagement ceremony and other FSM flows.

For crypto-critical features, extract a pure reducer function: `(State, Action) → ReducerResult(state, effect?)`. The `reaction()` composable orchestrates the reducer and handles effects. The reducer is testable with zero framework — pure `assertEquals`.

Reserve pure reducers for flows where formal verification, audit trails, and crypto safety demand them. Standard MVA screens do not need this.

## 12. File Structure Per Feature

```
feature_<name>/
├── api/
│   ├── <Name>Action.kt          // Sealed interface — all possible actions
│   └── <Name>State.kt           // Sealed interface — all possible screen states
├── impl/
│   ├── <Name>ViewModel.kt       // Extends MoleculeViewModel, reaction() inline
│   └── di/
│       └── <Name>Module.kt      // Koin module
└── test/
    └── <Name>ViewModelTest.kt   // JVM tests via ViewModel with Turbine
```

For crypto-critical features, add:
```
├── api/
│   └── <Name>Reducer.kt         // Pure (State, Action) → ReducerResult
└── test/
    └── <Name>ReducerTest.kt     // Pure unit tests — no Compose, no coroutines
```

## 13. Spike Scope

Work on a spike branch against AirGap POC (poc/0000):

1. Create `core/mva` module with `MoleculeViewModel` and `MoleculeViewModelWithEffect`
2. Add `molecule-runtime:2.2.0` to version catalog
3. Add `-Xexplicit-backing-fields` compiler flag to convention plugin
4. Define `AirGapAction` and `AirGapState` sealed interfaces
5. Migrate `AirGapViewModel` to extend `MoleculeViewModel`
6. Update Compose UI to `collectAsStateWithLifecycle()` + `onAction()`
7. Write ViewModel tests with Turbine
8. Full review/audit with rubric scoring

## 14. Anti-Patterns — Must Reject

- Multiple mutable `StateFlow` properties exposed from ViewModel
- Ad-hoc ViewModel methods instead of sealed Action interface
- Reducer doing IO (network/crypto calls inside state transition logic)
- Direct state mutation from UI
- `MutableSharedFlow` created inside the `@Composable`
- `ByteArray` in data class Action/State (breaks equals/hashCode)
- `SecretKey`, `PrivateKey`, or any key material in Action/State — keep in domain layer only
- Heavy computation or non-`remember`ed object allocation inside `reaction()` — expensive work belongs in domain layer or `LaunchedEffect`
- Using the word "presenter" anywhere
- Using the word "presentation" anywhere
- Plural names unless referring to actual collections

## 15. Dependencies

```toml
# libs.versions.toml
[versions]
molecule = "2.2.0"

[libraries]
molecule-runtime = { group = "app.cash.molecule", name = "molecule-runtime", version.ref = "molecule" }
```

Gradle plugin: `org.jetbrains.kotlin.plugin.compose` (already applied — ships with Kotlin).
No `app.cash.molecule` Gradle plugin — removed in v2.

## 16. Subsequent Work

After spike validation:
- Create `mc-mva` skill for CC guidance
- Update `architecture.md` — Phase 1a status from "deferred" to "active"
- Update `patterns.md` — replace any presenter-based templates with MVA ViewModel template
- Migrate remaining ViewModels feature-by-feature
- Ingest reference doc into `docs/references/`
