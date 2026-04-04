# Cryptographic Protocol — Key Decisions

Full spec: `docs/design.md` Section 7. This is a summary — the spec is authoritative.

---

## Fundamental Rules

- **All cryptography on Alice only.** Bob never sees plaintext or session keys.
- **Pipeline (immutable):** Message Assembly → Padding → Compression → Encryption → CBOR → QR
  - "Message Assembly" = user plaintext + metadata assembled into the cleartext payload structure
- **`IVaultCryptographyEngine`** is the single interface boundary for all cryptography.
  - Phase 1 = `VaultCryptographyEngine` (pure Kotlin + BouncyCastle)
  - Phase 2 = Rust VM via UniFFI. Swap is one Koin line.

---

## Key Architecture

- **Identity Key is two separate key pairs:** IK_Ed (Ed25519, signing only) + IK_X (X25519, DH only). Never mixed.
- **Protocol:** X3DH key agreement + Double Ratchet message encryption — pure Kotlin, no external library for the protocol itself
- **Primitives:** X25519, Ed25519, HKDF-SHA256, ChaCha20-Poly1305, SHA-256, Argon2id — all via BouncyCastle

---

## HKDF Info Strings

Namespace reserved: `AliceBob_v1_`

| Use | Info string |
|---|---|
| X3DH | `AliceBob_v1_X3DH` |
| Double Ratchet root | `AliceBob_v1_DR_RK` |
| Per-message session key | `AliceBob_v1_MSG` |
| MAC | `AliceBob_v1_MAC` |
| SAS | `AliceBob_v1_SAS` |

---

## Argon2id Parameters

64 MiB memory, 3 iterations, parallelism 4, 32-byte output.

---

## Encryption at Rest

BouncyCastle with Room TypeConverters for field-level encryption. Do not suggest SQLCipher.
