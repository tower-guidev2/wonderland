# API_CONTRACTS.md — Cross-Module Interface Definitions

## Purpose

Defines the public interfaces between feature modules. All cross-module communication goes through `:feature_api`. No feature module may depend on another feature module's internals.

## Value Classes — Crypto Primitives

```kotlin
// :crypto module
@JvmInline value class PublicKey(val bytes: ByteArray)
@JvmInline value class PrivateKey(val bytes: ByteArray)
@JvmInline value class EphemeralPublicKey(val bytes: ByteArray)
@JvmInline value class EphemeralPrivateKey(val bytes: ByteArray)
@JvmInline value class SharedSecret(val bytes: ByteArray)
@JvmInline value class CipherText(val bytes: ByteArray)
@JvmInline value class Nonce(val bytes: ByteArray)
@JvmInline value class AuthTag(val bytes: ByteArray)
```

## Value Classes — Domain

```kotlin
// :core module
@JvmInline value class ContactId(val value: String)
@JvmInline value class MessageId(val value: String)
@JvmInline value class BundleId(val value: String)
@JvmInline value class PlainText(val value: String)
@JvmInline value class QrPayload(val bytes: ByteArray)
```

## Engagement API

```kotlin
// :feature_api
interface IEngagementApi {
    fun createInvitation(): Either<EngagementError, Invitation>
    fun acceptInvitation(invitation: Invitation): Either<EngagementError, KeyBundleResponse>
    fun completeEngagement(keyBundle: KeyBundleResponse): Either<EngagementError, ContactRecord>
}
```

## Messaging API

```kotlin
// :feature_api
interface IMessagingApi {
    fun encryptMessage(
        contactId: ContactId,
        plainText: PlainText
    ): Either<MessagingError, EncryptedPayload>

    fun decryptMessage(
        cipherText: CipherText,
        nonce: Nonce,
        authTag: AuthTag
    ): Either<MessagingError, PlainText>
}

data class EncryptedPayload(
    val qr1: QrPayload,  // Encrypted CBOR
    val qr2: QrPayload   // Blockchain auth token
)
```

## Key Management API

```kotlin
// :feature_api
interface IKeyManagementApi {
    fun generateKeyBundle(count: Int): Either<KeyError, KeyBundle>
    fun consumeOneTimeKey(contactId: ContactId): Either<KeyError, PublicKey>
    fun remainingKeySlots(contactId: ContactId): Int
    fun needsRefresh(contactId: ContactId): Boolean
}
```

## QR API

```kotlin
// :feature_api
interface IQrApi {
    fun generateQrBitmap(payload: QrPayload): Either<QrError, Bitmap> // NOTE: Bitmap is Android-specific. This interface lives in :feature_api which is Android-aware.
    fun startScanning(onResult: (Either<QrError, QrPayload>) -> Unit)
    fun stopScanning()
}
```

## Air-Gap API (Alice only)

```kotlin
// :feature_api
interface IAirGapApi {
    fun currentStatus(): AirGapStatus
    fun observeStatus(): Flow<AirGapStatus>
    fun isDeviceIntegrityValid(): Either<IntegrityError, AttestationResult>
}
```

## Error Hierarchies

```kotlin
sealed interface DomainError
sealed interface EngagementError : DomainError
sealed interface MessagingError : DomainError
sealed interface KeyError : DomainError
sealed interface QrError : DomainError
sealed interface IntegrityError : DomainError
```

## Contract Rules

- All return types use `Either<Error, Success>`. No thrown exceptions for expected failures.
- All parameter types are value classes. No raw `String`, `ByteArray`, or `Int` in public signatures.
- Interfaces live in `:feature_api`. Implementations live in the corresponding feature module.
- New interfaces require an entry in this document before implementation.
