# PRD.md — Product Requirements Document

## Product Vision

A secure messaging system enabling private communication between two parties without any network dependency on the sending device. Alice (air-gapped) and Bob (networked) communicate exclusively via QR codes. The system must be trustworthy enough to scale to one billion users.

## Target Users

- Privacy-conscious individuals requiring provably secure communication.
- Users in high-threat environments where network-based messaging is compromised or surveilled.
- Anyone who needs cryptographic guarantees that their messaging device has not been tampered with.

## Core User Stories

### Engagement Ceremony
- **As Bob**, I want to initiate a secure pairing with Alice by generating an Invitation QR code, so that a trust relationship can be established on first use.
- **As Alice**, I want to scan Bob's Invitation and respond with a Key Bundle, so that Bob has pre-keyed material for future messages.
- **As both**, I want the Contact Record to be established after a successful ceremony, so that subsequent messaging requires no further setup.

### Messaging
- **As a sender**, I want to compose a message and present it as a QR code (QR1 = encrypted CBOR payload), so that the recipient can scan and decrypt it without any network.
- **As a sender**, I want a second QR code (QR2 = lightweight blockchain authentication) to accompany every message, so that authenticity is independently verifiable.
- **As a recipient**, I want to scan both QR codes and see the decrypted message, so that I can read and respond.

### Key Management
- **As Alice**, I want to generate Key Bundles (batches of one-time public keys) and present them to Bob via QR, so that forward secrecy is maintained.
- **As Bob**, I want to be notified when my one-time key slots are running low (Bundle Refresh), so that I can request a new Key Bundle from Alice.
- **As both**, I want no private or session keys to ever be persisted to storage, so that key compromise from device seizure is impossible.

### Air-Gap Integrity (Alice only)
- **As Alice**, I want continuous positive attestation that my device is air-gapped (Wi-Fi, BLE, UWB, GPS, USB data, ADB, NFC, SIM all verified off), so that I have cryptographic proof of isolation.
- **As Alice**, I want hard violations (network, Bluetooth, NFC, SIM, airplane mode disabled) to trigger immediate cryptographic zeroing and a tamper screen, so that compromise is irrecoverable.
- **As Alice**, I want device integrity verified via hardware StrongBox attestation (Titan M2, bootloader lock, GrapheneOS verified boot), so that the platform itself is trustworthy.

## Non-Functional Requirements

- **Security**: Zero-trust. Ephemeral DH keys + ChaCha20-Poly1305. No key persistence. Hardware attestation.
- **Privacy**: No analytics, no telemetry, no network calls from Alice ever.
- **Performance**: QR code generation and scanning must feel instantaneous (<200ms).
- **Accessibility**: UI must be usable but accessibility services are a security surface on Alice — document the tradeoff.
- **Offline**: Alice is permanently offline by design. Bob must function fully offline for message composition and decryption.
- **Scale target**: Architecture must support one billion users without fundamental redesign.

## Acceptance Criteria Format

Each feature implementation must include:
1. The user story it satisfies (reference by section).
2. A demonstration that the acceptance criteria are met.
3. A test covering the happy path and at least one failure/edge case.

## Out of Scope (Phase 1)

- Rust VM / NDK integration.
- ZKP (Mopro + Halo2).
- Group messaging.
- Media attachments beyond text.
