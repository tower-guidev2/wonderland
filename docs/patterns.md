# PATTERNS.md — Reusable Implementation Templates

## Purpose

When a problem matches a pattern documented here, use this pattern. Do not invent an alternative. If you believe a pattern should change, propose the change explicitly — do not silently deviate.

---

## Pattern: Ephemeral Key Lifecycle

**Problem**: Crypto key material must never outlive its operation.

**Template**:
```kotlin
fun performCryptoOperation(input: PlainText, recipientKey: PublicKey): Either<CryptoError, CipherText> {
    val ephemeralKeyPair = generateEphemeralKeyPair()
    return try {
        val sharedSecret = deriveSharedSecret(ephemeralKeyPair.private, recipientKey)
        try {
            encrypt(input, sharedSecret)
        } finally {
            sharedSecret.bytes.fill(0)
        }
    } finally {
        ephemeralKeyPair.private.bytes.fill(0)
    }
}
```

**Rules**:
- `finally` block zeroing is mandatory — even if the operation fails.
- Never assign key material to a class property. Local variables only.
- Never log key material at any level.

---

## Pattern: Molecule Presenter Integration

**Problem**: MVI presenters need to produce state from intents using Compose runtime.

**Template**:
```kotlin
class FeatureViewModel(
    private val useCase: FeatureUseCase
) : ViewModel() {

    private val intents = Channel<FeatureIntent>(Channel.UNLIMITED)

    val state: StateFlow<FeatureState> = viewModelScope.launchMolecule(RecompositionMode.Immediate) {
        featurePresenter(intents = intents.receiveAsFlow(), useCase = useCase)
    }

    fun onIntent(intent: FeatureIntent) {
        intents.trySend(intent)
    }
}

@Composable
fun featurePresenter(
    intents: Flow<FeatureIntent>,
    useCase: FeatureUseCase
): FeatureState {
    var state by remember { mutableStateOf(FeatureState.Initial) }

    LaunchedEffect(Unit) {
        intents.collect { intent ->
            state = when (intent) {
                is FeatureIntent.Load -> { /* ... */ }
            }
        }
    }

    return state
}
```

**Rules**:
- Presenter is a `@Composable` function, not a class.
- No standalone Presenter abstraction layer.
- ViewModel owns the `Channel` and exposes `StateFlow`.
- One presenter per feature screen.

---

## Pattern: Either-Based Use Case

**Problem**: Use cases must return typed errors, not throw exceptions.

**Template**:
```kotlin
class EncryptMessageUseCase(
    private val cryptoService: CryptoService,
    private val keyManager: KeyManagementApi
) {
    suspend operator fun invoke(
        contactId: ContactId,
        plainText: PlainText
    ): Either<MessagingError, EncryptedPayload> = either {
        val oneTimeKey = keyManager.consumeOneTimeKey(contactId).bind()
        val cipherText = cryptoService.encrypt(plainText, oneTimeKey).bind()
        cipherText
    }
}
```

**Rules**:
- Use `either { }` block with `.bind()` for composition.
- Return type is always `Either<SpecificError, SuccessType>`.
- Never catch and wrap exceptions inside `either` — let them propagate as unexpected failures.

---

## Pattern: Koin Module Declaration

**Problem**: DI modules must be co-located and single-responsibility.

**Template**:
```kotlin
// In :feature_engagement module
@Module
@ComponentScan("org.alice.rabbit.hole.feature.engagement")
class EngagementModule

@Single
class EngagementRepository(
    private val cryptoService: CryptoService,
    private val contactStore: ContactStore
) : IEngagementApi

@KoinViewModel
class EngagementViewModel(
    private val useCase: EngagementUseCase
) : ViewModel()
```

**Rules**:
- One `@Module` class per feature module.
- `@ComponentScan` scopes to feature package.
- `@Single` for repositories/services, `@Factory` for stateless, `@KoinViewModel` for ViewModels.
- Module class registered in `:app` startup.

---

## Pattern: QR Payload Encoding

**Problem**: Messages must be serialised to CBOR for QR code capacity.

**Template**:
```kotlin
@Serializable
data class QrMessagePayload(
    @SerialName("c") val cipherText: ByteArray,
    @SerialName("n") val nonce: ByteArray,
    @SerialName("t") val authTag: ByteArray,
    @SerialName("k") val ephemeralPublicKey: ByteArray
)

fun encodePayload(payload: QrMessagePayload): QrPayload {
    val bytes = Cbor.encodeToByteArray(payload)
    return QrPayload(bytes)
}

fun decodePayload(qrPayload: QrPayload): Either<SerializationError, QrMessagePayload> =
    Either.catch { Cbor.decodeFromByteArray<QrMessagePayload>(qrPayload.bytes) }
        .mapLeft { SerializationError.DecodeFailed(it.message) }
```

**Rules**:
- Field names are single-character `@SerialName` to minimise CBOR size.
- Encoding/decoding functions return `Either` — never throw.
- Payload classes are `@Serializable` data classes with no business logic.

---

## Pattern: Air-Gap Surface Check (Alice only)

**Problem**: Each attestation surface must be positively verified as disabled.

**Template**:
```kotlin
fun checkSurface(surface: AirGapSurface): AttestationResult {
    val isSecure = when (surface) {
        AirGapSurface.WiFi -> wifiManager.isWifiEnabled.not()
        AirGapSurface.Bluetooth -> bluetoothAdapter.isEnabled.not()
        // ... all 20+ surfaces
    }
    return if (isSecure) {
        AttestationResult.Verified(surface, Instant.now())
    } else {
        AttestationResult.Violation(surface, Instant.now())
    }
}
```

**Rules**:
- Positive attestation: check that the surface IS disabled, not that it was never enabled.
- Hard violations (network, BLE, NFC, SIM, airplane mode disabled) → immediate cryptographic zeroing.
- USB → graceful suspension only, not zeroing.
- Check runs every 30 seconds.

---

## Adding New Patterns

When you solve a problem that will recur, add it here using this template:

```
## Pattern: [Name]

**Problem**: [What class of problem this solves]

**Template**:
[Code example]

**Rules**:
[Non-negotiable constraints when applying this pattern]
```
