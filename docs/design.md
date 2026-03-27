# Alice & Bob — Secure Messaging System
## Design Document — Draft 0.4
### Date: 27 March 2026

---

### Changes in Draft 0.4

- Section 2: Input architecture added. Custom keyboard requirement and security rationale documented.
- Section 6: Field registries complete. Full CBOR field specifications for all eleven QR types. Type 11 (X3DH Initial) added to the registry.
- Section 7: Cryptographic Protocol Design complete. Full specification for X3DH, Double Ratchet, Argon2id, SAS phrase derivation, and all HKDF info strings. Identity Key structure formalised. Initial DH ratchet step specified.
- Section 9: Resolved open items removed. Custom keyboard visual design added as a production open item.

### Changes in Draft 0.3

- Section 5: Engagement Ceremony revised. Frank's Key Bundle is now included in the Invitation, reducing the ceremony from 10 steps to 8 steps and the messaging infrastructure legs from 4 to 2. The embedded return-request mechanism is removed — it is no longer needed.
- Section 6: Message Ordering added. Monotonic sequence number is the primary ordering key. Timestamp is a display-only field. Rationale documented.
- Section 6: Dual-QR Message Transmission added. Each message produces two simultaneous Version 40 QR codes — one for the encrypted payload, one for authentication. ML Kit bundled barcode scanning specified. Type 10 added to QR type registry.

### Changes in Draft 0.2

- Section 3: Added Scenario 11 — Man in the Middle. Added MITM to residual risks and limitations.
- Section 5: Full rewrite. Bidirectional ceremony. Key type separation. Prime number lifetimes. SAS verification step. Lightweight refresh ceremony. Stake in the ground on key exchange resolved.
- Section 6: New section — Protocol Encoding. Wire format, capability flags, CBOR, field lifecycle. Replaces version byte approach entirely.
- Section 7: Cryptographic Protocol Design (renumbered from 6). Pipeline and properties updated. Still in progress for full X3DH and Double Ratchet specification.
- Section 9: Open Questions updated. Resolved items removed. New open items added.

---

# Part 1 — Engineering Charter

This charter governs how software is designed, built, and evolved. It applies to every project and every collaborator, human or AI. Every decision carries its reasoning. No decision is permanent. Every principle here must be challenged when following it would harm a project. Blind compliance is unacceptable.

---

## Principles over Rules

Principles carry reasoning. Rules demand compliance. When a principle conflicts with reality, challenge the principle, examine the reasoning, reach a better decision. Document every significant deviation and the reasoning behind it.

---

## Common Sense First

Apply common sense before anything else. Examine every problem plainly before reaching for a framework, pattern, or library. If the answer is obvious, use it. If it is not obvious, think before acting.

---

## Simplicity is Power

The simplest correct solution is always preferred. Simplicity is the goal, not a compromise. Simple systems are easier to secure, understand, test, and trust. Every layer of complexity must justify its existence. Unjustified complexity must be removed.

---

## Intentional Design

Every decision is deliberate. Architecture, naming, structure, library selection, and user experience result from deliberate thought. An arbitrary decision is a future maintenance burden and a potential vulnerability. If a decision feels arbitrary, it has not been thought through. Stop and think.

---

## Security is a Foundation

Where security is relevant it is the foundation, not a feature. Security and convenience in conflict — security wins. Security and elegance in conflict — examine the tension carefully. An elegant solution is frequently the more secure one.

---

## Craftsmanship

Code is read by humans first, executed by machines second. Names are chosen with precision. Structures are intentional. Standards do not relax because something is hidden. There are no unimportant parts.

---

## Progressive Detail

Every topic is first stated at its highest level. Detail is added progressively. This is a design methodology, not a documentation style. Descending from high level to fine detail is where design holes are found and assumptions are challenged. A topic that cannot be stated simply at the high level is not yet understood well enough to be detailed.

---

## Technology Serves the Problem

Libraries, frameworks, and tools serve the problem. They are not chosen to demonstrate sophistication or follow trends. Every dependency is a liability as well as an asset. The standard library is always considered first. Third party dependencies are adopted deliberately, justified clearly, and kept to a minimum.

---

## Security Through Transparency

Security that depends on keeping the existence or nature of a system secret is not security — it is obscurity. Obscurity fails the moment it is discovered. This system makes no attempt to conceal what it is or how it works. Its strength derives entirely from its cryptographic and architectural properties. It does what it says. Nothing more. Nothing less.

---

## The Zero Option

Before implementing any solution in software, explicitly consider not implementing it at all. A missing feature cannot contain bugs, security vulnerabilities, or complexity. The best code is frequently no code. A non-software solution — a process, a convention, a physical action, a user behaviour — is always a valid option and must always be on the table.

---

## UI and UX Standard

Every user interface produced under this charter is considered fine art. Pixel perfect and deliberately considered in every detail. Frictionless user experience is not a goal — it is a baseline requirement. Every screen, every interaction, every transition, every typographic choice, every colour decision, every animation is intentional. Nothing is accidental. Nothing is unfinished. If it does not look and feel excellent it is not done.

---

## Usability is Not Optional

A secure system that cannot be used is not secure — it is abandoned. Security and usability are not opposites. Where a design decision makes the system difficult or impractical to use, that decision must be challenged. A solution that requires extraordinary effort from users will be circumvented, abandoned, or misused. Usability is a security property.

---

## The Five Year Old Test

Any design decision, architectural choice, or technical mechanism that cannot be explained clearly to a non technical person is not yet fully understood. Complexity that cannot be explained is complexity that cannot be trusted. Before any significant decision is considered final, it must pass this test.

---

## Collaborator Expectations

Every collaborator operates by this charter. Blind compliance is a failure. Challenge where challenge is warranted. Propose alternatives with clear reasoning. Raising a concern is always correct. Silent compliance with something that feels wrong is never acceptable.

---

# Part 2 — Project Design Document

---

# 1. Project Overview

## What

A secure messaging system consisting of two companion Android applications, Alice and Bob, operating together on two physically separate devices owned by a single user.

## Why

Existing messaging systems derive their security from trusted servers, controlled infrastructure, and in many cases secrecy of implementation. This system trusts none of those things. Its security derives entirely from cryptographic properties and physical air gap. It does not attempt to hide what it is. A user running Alice is running Alice. The system's strength is in its design, not its concealment.

## What it Does

Alice composes, encrypts, and encodes messages on a permanently air gapped device. Bob receives encoded messages from Alice via QR code, then dispatches them over existing messaging infrastructure. No plaintext ever exists on a networked device. No server is involved at any point.

## What it Does Not Do

- Provide its own messaging network or transport
- Store any private key or plaintext message beyond immediate operational need
- Trust any hardware, firmware, operating system, or third party library
- Require its users to trust each other's infrastructure
- Guarantee anonymity — it provides confidentiality only
- Require account creation of any kind
- Request, collect, store, or transmit any personal or private user information
- Present advertisements of any kind
- Offer paid features or in-app purchases
- Collect any user data whatsoever

## Product Principles

This system is free. It contains no advertisements, no paid features, and no in-app purchases. It requires no account creation. It requests no personal information. It collects no user data of any kind. These are not technical constraints — they are ethical commitments.

---

# 2. Actors, Devices and Roles

## Actors

- **The User** — a single human operating both Alice and Bob devices
- **Alice** — an Android application running on a permanently air gapped device
- **Bob** — an Android application running on a networked device
- **The Recipient** — another user operating their own Alice and Bob configuration

## Devices

- **The Alice Device** — an Android device, permanently air gapped, no SIM, WiFi disabled, Bluetooth disabled, NFC disabled, airplane mode permanently enabled. Optimal configuration is a Google Pixel running GrapheneOS. Any deviation from optimal configuration is continuously detected and prominently communicated to the user.
- **The Bob Device** — any Android device, networked, with one or more messaging applications installed.

## Relationships

- One Alice pairs with exactly one Bob. This relationship is permanent.
- Every participant operates their own Alice and Bob configuration.
- There are no partial participants. Every recipient must be capable of encrypting and decrypting messages using their own Alice and Bob.

## Communication

- Alice and Bob both generate and scan QR codes.
- Alice communicates with Bob exclusively by generating QR codes for Bob to scan.
- Bob communicates with Alice exclusively by generating QR codes for Alice to scan.
- Bob dispatches outbound encrypted messages via standard Android intents to installed messaging applications.
- Bob receives inbound encrypted messages via those same messaging applications.
- All data exchange between Alice and Bob occurs exclusively via QR code. This includes messages, contact information, key exchange, and authentication. There are no exceptions.
- No other communication channel exists or is permitted.

## Security Posture

Alice continuously monitors the security configuration of the device she runs on. When the device does not meet optimal security requirements, Alice permanently and prominently displays the specific deficiency with a clear human readable explanation. Alice never misrepresents the user's security posture. The user is always informed. The decision is always theirs.

## Input — Alice

Alice uses a custom Android `InputMethodService` as its exclusive keyboard for all message composition. The system keyboard is never used.

**Why a custom keyboard is required.** The system keyboard is a third-party process. It receives every keystroke the user types. System keyboards routinely autocorrect, log input, sync to cloud services, and share data with their developers. On Alice, every character typed is plaintext. Any system keyboard with network access, background sync, or telemetry is a live exfiltration channel. A custom keyboard eliminates this attack surface entirely.

**Character set.** The keyboard exposes exactly 37 symbols: lowercase a–z, digits 0–9, and space. These are the only characters Alice accepts. No capitalisation. No punctuation. No emoji. The keyboard physically cannot produce characters outside this set.

**Security properties of the implementation.**
- `FLAG_SECURE` is set on the keyboard window. Screenshots and screen recorders cannot capture the keyboard surface.
- Accessibility services are blocked from reading key events. This prevents accessibility-based keystroke logging.
- Every character is validated against the permitted set before being committed to the input connection. Characters outside the set are silently discarded.

**Status.** A working Phase 1 implementation exists. The architecture and security properties are complete and correct. The visual design is an open item — the current look and feel does not meet the UI standard and requires dedicated design work before the app ships.

## Trust Establishment

Users never meet in person. All trust establishment, identity verification, and key exchange is performed entirely through the Alice and Bob system itself. This is a fundamental usability commitment and a hard design requirement. Any cryptographic or architectural approach that cannot satisfy this requirement is not a valid solution for this system. The mechanism by which MITM attacks are mitigated within this constraint is specified in Section 5.

---

# 3. Threat Model

## The Fundamental Assumption

Every layer of the stack is considered compromised and hostile. This includes hardware, firmware, operating system, third party libraries, messaging infrastructure, and network. No component outside of the application code itself is trusted. Security derives entirely from cryptographic properties and physical air gap.

## Assets Being Protected

- Message plaintext
- Recipient identity
- Sender identity
- The relationship between sender and recipient
- Private keys
- The fact that a specific message was sent at a specific time

## Adversary Capabilities

We assume the adversary can:

- Read all network traffic
- Compromise the operating system on the Bob device
- Compromise any third party messaging application on the Bob device
- Access all data stored on either device
- Observe QR codes being displayed and scanned
- Intercept messages in transit between Bob and recipient
- Perform traffic analysis on the Bob device
- Physically access the Bob device
- Physically access the Alice device when not in active use
- Intercept messages on the engagement channel during the key exchange ceremony

We assume the adversary cannot:

- Read the screen of the Alice device in real time during active use
- Intercept a QR code at the physical moment of scanning between Alice and Bob
- Break ChaCha20 encryption with a correctly generated ephemeral key
- Forge a valid zero knowledge proof — Phase 2 only
- Be physically present during every QR exchange simultaneously
- Simultaneously intercept the engagement channel and an independent voice call between the two users

## Threat Scenarios

**Scenario 1 — Compromised Bob Device**
Bob operates on a networked device running a potentially hostile OS. Bob never receives plaintext. Bob only ever handles encrypted CBOR payloads. A fully compromised Bob device yields only ciphertext, recipient contact IDs, and metadata about when messages were dispatched. No message content is recoverable.

**Scenario 2 — Compromised Messaging Infrastructure**
Messages travel over SMS, WhatsApp, or similar. These channels are considered fully compromised. Messages are encrypted before reaching Bob. The messaging infrastructure sees only Base64 encoded ciphertext. No message content is recoverable.

**Scenario 3 — Compromised Alice Device at Rest**
Alice persists no plaintext and no private or session keys. A seized Alice device at rest yields only public keys and contact identifiers. These are public information by definition. No message content is recoverable.

**Scenario 4 — Compromised Alice Device During Active Session**
During active use, plaintext exists briefly in memory on Alice. An adversary with real time access to Alice's memory during composition could theoretically recover plaintext. This is an accepted and documented residual risk. The physical air gap is the primary mitigation — an adversary cannot exfiltrate memory contents from an air gapped device without physical presence.

**Scenario 5 — Hostile Third Party Library**
All third party libraries are considered potentially hostile. Libraries are kept to an absolute minimum. Bouncy Castle is used exclusively for cryptographic operations. No library receives plaintext except during the encryption operation itself. Library interactions are isolated behind domain interfaces — no library type crosses an architectural boundary.

**Scenario 6 — Air Gap Violation**
Alice continuously monitors her air gap status. Any detected violation immediately suspends all operations and alerts the user. However, a sufficiently sophisticated compromise of the OS could theoretically suppress these alerts. This is a documented residual risk. GrapheneOS on a Google Pixel is the recommended mitigation.

**Scenario 7 — QR Code Interception**
QR codes contain only encrypted ciphertext. An adversary who photographs a QR code in transit obtains only ciphertext encrypted with an ephemeral key. Without the private key material, which exists only momentarily in Alice's memory, the ciphertext is not recoverable.

**Scenario 8 — Traffic Analysis**
An adversary observing Bob's network traffic can determine that Bob is communicating, with whom, and approximately when. This system does not protect against traffic analysis. Message content is protected. Communication patterns are not. This is a documented and accepted limitation.

**Scenario 9 — Physical Coercion**
This system provides no protection against a user who is physically coerced into operating Alice and Bob under duress. This is a documented and accepted limitation. It is out of scope for a software solution.

**Scenario 10 — Key Compromise**
Keys are ephemeral. A compromised key exposes only the single message it was used to encrypt. There is no key material that exposes historical messages. Forward secrecy is a fundamental property of the system.

**Scenario 11 — Man in the Middle During Engagement**
An adversary positioned between two users during the engagement ceremony could substitute their own key bundle, causing both parties to encrypt messages to the attacker rather than to each other. This attack requires the adversary to simultaneously compromise both legs of the key exchange — the outbound invitation channel and the inbound bundle return channel.

Primary mitigation: SAS Verification. After the engagement ceremony completes, both Alices independently derive a short verification phrase from the shared key material. The two users compare this phrase over any independent channel — a voice call is sufficient. A matching phrase proves no substitution occurred. A mismatch requires the engagement to be aborted and restarted.

Secondary mitigation: TOFU and key change alerts. Once keys are established, any subsequent key change triggers a prominent unavoidable alert. The user must explicitly re-verify before communication resumes.

The UI distinguishes Verified contacts from Unverified contacts at all times. Alice never misrepresents the verification state of a contact.

Residual risk: an adversary capable of simultaneously compromising the engagement channel and an independent voice verification call could still succeed. This requires capabilities significantly beyond the assumed adversary model and is accepted and documented.

## Message Ephemerality

The system maintains no message history of any kind. A message exists in three states only — being composed on Alice, encoded in a QR code, and decrypted on the recipient's Alice. Once read, it is gone. There is no inbox, no sent folder, no conversation thread, no log. This is not a missing feature. It is a deliberate and fundamental security property.

## Residual Risks

These risks are known, accepted, and documented:

- Plaintext exists briefly in Alice's memory during composition and decryption
- A hostile OS on Alice could theoretically suppress air gap violation alerts
- Communication patterns between users are observable via traffic analysis
- Physical coercion of the user is out of scope
- Physical seizure of Alice during an active session exposes in-memory plaintext
- A man in the middle attack during engagement is possible if the user does not complete SAS verification

## What This System Does Not Claim

- Anonymity — it provides confidentiality, not anonymity
- Protection against traffic analysis
- Protection against physical coercion
- Perfect security on non-recommended hardware and OS configurations
- Any protection once plaintext is displayed to the user on Alice's screen
- MITM resistance without SAS verification

---

# 4. Development Phases

## Phase 1 — Proof of Concept

Phase 1 proves the core thesis of the system — that QR code based key exchange, message encryption, and inter-application communication is viable, usable, and correct. It is implemented entirely in Kotlin. Phase 1 code is not production code. It is not architected to survive into Phase 2. Its sole purpose is to validate the concept and build confidence before entering unknown technical territory.

### Phase 1 — Definition of Done

A user can add a brand new contact to Alice by scanning a Contact QR generated by Bob. Bob derives contact information from the Android contacts provider, exposing only the internal contact ID and display name. Alice stores this in her local database.

The user composes a message on Alice, selects that new contact as recipient, and generates a message QR. Bob scans the message QR and dispatches the encrypted message to the recipient via a messaging application. The recipient scans the received message with their Bob, which generates a QR that their Alice scans and decrypts, presenting the original plaintext message to the user.

The full round trip completes successfully on real devices. No plaintext exists on any networked device at any point.

## Phase 2 — Production

Phase 2 begins with a clean slate. The Kotlin messaging pipeline from Phase 1 is replaced by a Rust based domain specific virtual machine, embedded in Android via the NDK as a native library. The VM executes a reduced instruction set designed exclusively for secure messaging operations.

Zero knowledge proofs are a first class citizen of the VM. Every stage of the messaging pipeline — padding, compression, encryption, QR generation — produces a cryptographic proof of correct execution. These proofs are embedded in QR codes as verification signatures. They are entirely internal to the system. Users are never exposed to them directly.

Phase 2 does not evolve Phase 1. It replaces it. This is a deliberate and accepted design decision.

### NDK Requirement

Phase 2 requires the Android NDK to be installed and configured. This is a known and accepted build dependency introduced in Phase 2.

---

# 5. Key Exchange

## Key Types

Three distinct key types exist in this system. They serve different purposes and have different lifetimes. They must not be conflated.

**Identity Key (IK)**
The long-term anchor key for a user's Alice installation. It is generated once at installation and signs all other keys produced by that Alice. It is the key whose authenticity is proved by SAS Verification during engagement. It persists for the lifetime of the Alice installation. It rotates only on device migration, explicit user-initiated rotation, or key compromise declaration. Any rotation of the Identity Key requires full re-verification by all existing contacts.

**Signed Prekey (SPK)**
A medium-term key signed by the Identity Key. It is the key bundle that contacts use to establish new messaging sessions. It has a defined lifetime of 89 days. The system alerts the user at 30 days and 7 days remaining. An expired Signed Prekey blocks outbound messages to the affected contact until a Refresh Ceremony completes.

**One-Time Prekeys (OPKs)**
Single-use keys consumed one per new engagement. They are generated in a pool and held until consumed or expired. An unconsumed OPK expires after 127 days. Expired unconsumed OPKs are silently discarded and replaced. The pool is also refreshed when it falls below a defined threshold, regardless of expiry.

## Key Lifetimes

All lifetimes are expressed in prime numbers of days.

| Key | Lifetime | Rotation Trigger | SAS Re-verification Required |
|---|---|---|---|
| Identity Key | 727 days | Migration, revocation, explicit user action | Yes |
| Signed Prekey | 89 days | Expiry (alert at 30d and 7d) | No |
| One-Time Prekey | 127 days or consumed | Expiry or pool threshold | No |

## Vocabulary

- **Engagement** — the complete ceremony establishing a secure communication relationship between two users for the first time. Bidirectional. Both parties leave with each other's keys.
- **Invitation** — the initial message Bob sends on behalf of a user to propose an engagement. The Invitation is the Engagement Request. They are one and the same.
- **Key Bundle** — the set of keys one Alice prepares for a specific contact: one Signed Prekey and a pool of One-Time Prekeys, all signed by the Identity Key.
- **Key Slot** — a single One-Time Prekey within a Key Bundle.
- **Contact Record** — the persistent entry Alice stores for a contact: their Identity Key, their current Signed Prekey, their remaining One-Time Prekeys, their Bob ID, their display name, and their verification state.
- **Verification State** — one of two values: Unverified or Verified. Set to Verified only after successful SAS Verification. Never assumed. Never inferred.
- **SAS Verification** — Short Authentication String Verification. Both Alices independently derive a short phrase from the shared key material after engagement. The users compare this phrase over an independent channel. A match confirms no man in the middle occurred.
- **Refresh Ceremony** — a lightweight ceremony triggered when a contact's Signed Prekey approaches expiry. Much lighter than the full Engagement Ceremony because both parties already hold each other's Identity Keys.
- **Bundle Dispatch** — the act of Bob sending a Key Bundle to a contact via messaging infrastructure.
- **Bundle Presentation** — the act of Bob presenting a received Key Bundle to Alice via QR code.

## The Engagement Ceremony

The ceremony is bidirectional. One party initiates. Both parties leave with the other's keys. Frank's Key Bundle is included in the Invitation — the exchange completes in two messaging legs rather than four. The example uses Frank as initiator and Iris as responder.

```
Step 1   Frank's Alice generates Frank's Key Bundle
         (SPK + OPK pool, signed by Frank's IK).
         Frank's Alice presents the bundle to Frank's Bob via QR.

Step 2   Frank's Bob includes Frank's Key Bundle in the Engagement Invitation
         and sends both to Iris's Bob via messaging infrastructure.

Step 3   Iris's Bob presents the Invitation and Frank's Key Bundle
         to Iris's Alice via QR.

Step 4   Iris accepts. Iris's Alice stores Frank's Contact Record.
         Iris's Alice generates Iris's Key Bundle for Frank
         (SPK + OPK pool, signed by Iris's IK).
         Iris's Alice presents Iris's bundle to Iris's Bob via QR.

Step 5   Iris's Bob sends Iris's Key Bundle to Frank's Bob
         via messaging infrastructure.

Step 6   Frank's Bob presents Iris's Key Bundle to Frank's Alice via QR.

Step 7   Frank's Alice stores Iris's Contact Record.

Step 8   Both Alices display the SAS Verification phrase.
         Frank calls Iris. They read the phrase to each other.
         Phrases match — both mark the contact as Verified.
         Engagement complete.
```

If the phrases do not match at Step 8, the engagement is aborted and all stored contact data for this attempt is discarded. The ceremony restarts from Step 1.

The contact is usable but marked Unverified until Step 10 completes. Alice always displays the verification state of every contact. Alice never misrepresents an Unverified contact as Verified.

## The Refresh Ceremony

Triggered when a contact's Signed Prekey reaches the 30-day warning threshold. Lighter than the full Engagement Ceremony because both parties already hold each other's Identity Keys.

```
Step 1   The approaching expiry is detected.
         The requester's Bob sends a Key Refresh Request to the contact's Bob.

Step 2   The contact's Bob presents the request to the contact's Alice via QR.

Step 3   The contact's Alice generates a new Signed Prekey and a fresh
         One-Time Prekey pool, signed by the contact's Identity Key.
         The contact's Alice presents the new bundle to the contact's Bob via QR.

Step 4   The contact's Bob returns the new bundle to the requester's Bob
         via messaging infrastructure.

Step 5   The requester's Bob presents the new bundle to the requester's Alice via QR.

Step 6   The requester's Alice verifies the bundle signature
         against the contact's stored Identity Key.
         Verification passes. New keys stored. Contact remains active.
```

No new SAS Verification is required. The Identity Key has not changed. The existing trust is sufficient to authenticate the new bundle.

If the Signed Prekey expires before the Refresh Ceremony completes, outbound messages to that contact are blocked. Alice presents a clear explanation and prompts the user to complete the refresh.

---

# 6. Protocol Encoding

## Wire Format

Every QR payload begins with a fixed three-byte header followed by a CBOR encoded body.

```
[ type: 1 byte ][ capabilities: 2 bytes ][ CBOR payload ]
```

There is no version byte. Versioning by sequential number creates migration chains. This system does not have migration chains.

## QR Type Byte

The type byte identifies what kind of QR this is. Each type is its own protocol with its own schema. The type byte is the breaking change mechanism. If a change is so fundamental that it is incompatible with an existing type, it becomes a new type. Existing types are never redefined. Type numbers are never reused.

| Type | Name | Error Correction |
|---|---|---|
| 1 | Device Pairing | ECL H |
| 2 | Contact Card | ECL M |
| 3 | Message | ECL H |
| 4 | Key Rotation | ECL M |
| 5 | Session Reset | ECL M |
| 6 | Key Revocation | ECL M |
| 7 | Device Migration | ECL M |
| 8 | Safety Number | ECL M — Deferred V2 |
| 9 | Group Invite | ECL M — Deferred V2 |
| 10 | Message Authentication | ECL H |
| 11 | X3DH Initial | ECL M |

## Capability Flags

The two capability bytes form a uint16 bitmask. Each bit represents a defined protocol feature. When Alice generates a QR that uses a feature, she sets that bit. When Alice receives a QR, she checks every set bit against the features she supports.

- All set bits understood: proceed.
- Any set bit not understood: reject. Display a clear human-readable message prompting the user to update Alice.

This mechanism replaces sequential version numbers entirely. The receiver does not need to know what version generated the message. It needs to know only whether it can process what is in front of it.

The capability flag registry is an open item. Individual bits are assigned as the protocol is specified. A bit, once assigned, is never reassigned to a different meaning. A retired bit is tombstoned — permanently reserved, never reused, never set in new messages.

## CBOR Payload

All QR payloads use CBOR encoding via `kotlinx-serialization-cbor`. CBOR is a binary serialisation format. It is structurally equivalent to JSON but significantly more compact. Raw byte arrays are native — no Base64 encoding overhead. Integer keys cost one byte each regardless of what the field represents.

All schemas use integer field keys, not string keys. Field key 1 is always field key 1. Its meaning never changes. Adding field key 17 later costs nothing and breaks nothing.

CBOR maps are sparse. An absent field costs zero bytes. There is no need to send null or placeholder values for unused fields.

## Strictly Additive Schema Evolution

These rules govern how schemas evolve over time.

- **Never reuse a field number.** A field number is permanently assigned to its original meaning. If a field is retired, its number is tombstoned.
- **Never redefine the meaning of an existing field.** If the semantics change, it is a new field with a new number.
- **Only add fields.** New fields are added with new numbers. Old receivers encounter them as unknown fields and ignore them silently.
- **Unknown fields are always silently ignored.** This rule is baked into every parser from day one without exception.

With these rules in place, adding fields never breaks old receivers and never requires a new capability bit. A capability bit is only needed when the receiver genuinely must understand a new feature to process the message correctly.

## Message Ordering

Every Message payload carries two ordering fields.

**Sequence number** — a monotonically increasing integer, scoped per sender-recipient pair. This is the authoritative ordering key. Bob sorts received messages by sequence number, not by arrival time. A gap in the sequence signals a missing message. The sequence number is never reset except on a full session reset.

**Timestamp** — a UTC Unix timestamp recorded by Alice at the moment of composition. This is a display-only field. It is shown to the user ("sent Tuesday 14:03") and is never used for ordering. It must not be used as an ordering key.

The reason for this separation: Alice is permanently air-gapped with no NTP correction. Her clock can drift, and two messages composed in quick succession may produce equal or inverted timestamps. The sequence number is immune to clock drift and is the only reliable basis for ordering.

## Dual-QR Message Transmission

Every outbound message from Alice produces two Version 40 QR codes displayed simultaneously, filling the screen. Bob scans both in a single camera frame.

**QR 1 — Type 3 (Message)** — the encrypted CBOR payload. ECL H. Contains the ciphertext, sequence number, and display timestamp.

**QR 2 — Type 10 (Message Authentication)** — the authentication CBOR payload. ECL H. Contains a cryptographic binding between the sender's Alice instance and the recipient's Bob instance, giving both sides verification that the counterpart is genuine.

The Type 10 field schema is specified in the field registry below.

### QR Code Scanning

Bob scans both codes simultaneously using Google ML Kit Barcode Scanning. The bundled model variant is mandatory — the unbundled variant downloads the model on first use and requires internet connectivity, which is incompatible with the air-gapped Alice device. Bob uses the bundled model exclusively.

```kotlin
// Gradle dependency — bundled model only
implementation("com.google.mlkit:barcode-scanning:17.3.0")
```

ML Kit returns a `List<Barcode>`. For a message transmission, Bob expects exactly two entries — one of each type. Receiving only one, or receiving two of the same type, is a protocol error and must be surfaced to the user.

Device validation has confirmed that two Version 40 QR codes displayed simultaneously on a Pixel device scan reliably within a single camera frame without issue.

## The Field Registry

Each QR type maintains its own field registry. Field numbers are permanent. A retired field number is tombstoned — reserved forever, never reused, never set in new messages. The registry grows monotonically. Messages do not.

### Type 1 — Device Pairing

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | aliceDeviceId | String | variable | Unique Alice installation identifier |
| 2 | bobDeviceId | String | variable | Unique Bob installation identifier |
| 3 | pairingSecret | ByteArray | 32 bytes | Ephemeral shared secret for the pairing channel |
| 4 | timestamp | Long | — | UTC Unix ms. Replay protection. |

### Type 2 — Contact Card

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | ikPubEd | ByteArray | 32 bytes | Ed25519 Identity Key public key. Used for signature verification only. |
| 2 | ikPubX | ByteArray | 32 bytes | X25519 Identity Key public key. Used for Diffie-Hellman only. |
| 3 | spkPub | ByteArray | 32 bytes | X25519 Signed Prekey public key |
| 4 | spkSig | ByteArray | 64 bytes | Ed25519 signature of `spkPub` by `ikPubEd` |
| 5 | opkList | List\<ByteArray\> | 47 × 32 bytes | One-Time Prekey public keys. One consumed per X3DH initiation. |
| 6 | bundleExpiry | Long | — | UTC Unix timestamp of bundle expiry |
| 7 | timestamp | Long | — | UTC Unix ms. Bundle creation time. |

### Type 3 — Message

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | sequence | Long | — | Monotonic sender-recipient sequence number. Authoritative ordering key. |
| 2 | timestamp | Long | — | UTC Unix ms. Display only. Never used for ordering. |
| 3 | nonce | ByteArray | 12 bytes | ChaCha20-Poly1305 nonce |
| 4 | header | ByteArray | 40 bytes | `dhsPub` (32) ∥ `PN` (4) ∥ `Ns` (4) |
| 5 | ciphertext | ByteArray | variable | ChaCha20-Poly1305 ciphertext including 16-byte tag |

### Type 4 — Key Rotation

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | newIkPubEd | ByteArray | 32 bytes | New Ed25519 Identity Key. Absent if IK unchanged. |
| 2 | newIkPubX | ByteArray | 32 bytes | New X25519 Identity Key. Absent if IK unchanged. |
| 3 | newSpkPub | ByteArray | 32 bytes | New X25519 Signed Prekey |
| 4 | newSpkSig | ByteArray | 64 bytes | Signature of `newSpkPub` by the active IK |
| 5 | reason | Int | — | `1` = user-initiated, `2` = expiry, `3` = compromise |
| 6 | timestamp | Long | — | UTC Unix ms |

### Type 5 — Session Reset

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | contactIkPubEd | ByteArray | 32 bytes | Target contact's Ed25519 Identity Key |
| 2 | reason | Int | — | `1` = user request, `2` = suspected compromise |
| 3 | timestamp | Long | — | UTC Unix ms |

### Type 6 — Key Revocation

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | revokedIkPubEd | ByteArray | 32 bytes | Revoked Ed25519 Identity Key. Absent for SPK-only revocations. |
| 2 | revokedIkPubX | ByteArray | 32 bytes | Revoked X25519 Identity Key. Absent for SPK-only revocations. |
| 3 | revokedSpkPub | ByteArray | 32 bytes | Revoked Signed Prekey |
| 4 | timestamp | Long | — | UTC Unix ms |
| 5 | replacementBundle | Contact Card | variable | Immediate replacement bundle, if provided |

### Type 7 — Device Migration

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | encryptedState | ByteArray | variable | Argon2id-encrypted full Alice database |
| 2 | salt | ByteArray | 16 bytes | Argon2id salt |
| 3 | migrationId | String | variable | One-time migration token |
| 4 | timestamp | Long | — | UTC Unix ms |

### Type 8 — Safety Number (Deferred V2)

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | safetyNumber | String | 60 chars | Long-term safety number for manual out-of-band comparison |
| 2 | ikPubA | ByteArray | 32 bytes | Party A Ed25519 Identity Key |
| 3 | ikPubB | ByteArray | 32 bytes | Party B Ed25519 Identity Key |

### Type 9 — Group Invite (Deferred V2)

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | groupId | ByteArray | 32 bytes | Group identifier |
| 2 | inviterIkPubEd | ByteArray | 32 bytes | Inviter's Ed25519 Identity Key |
| 3 | groupBundle | Contact Card | variable | Group key bundle |

### Type 10 — Message Authentication

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | mac | ByteArray | 32 bytes | HKDF-derived MAC binding this message to both Identity Keys. See Section 7. |
| 2 | senderIkPubX | ByteArray | 32 bytes | Sender's X25519 Identity Key |
| 3 | recipientIkPubX | ByteArray | 32 bytes | Recipient's X25519 Identity Key |
| 4 | sequenceRef | Long | — | Must match the `sequence` field in the paired Type 3 payload |
| 5 | timestamp | Long | — | UTC Unix ms |

### Type 11 — X3DH Initial

Transmitted by the initiator alongside the first message to a new contact. Gives the responder everything needed to complete their side of X3DH and establish the shared session.

| Field | Name | Type | Size | Notes |
|---|---|---|---|---|
| 1 | ekPub | ByteArray | 32 bytes | Initiator's X25519 ephemeral public key |
| 2 | consumedOpkIndex | Int | — | Index of the One-Time Prekey consumed from the responder's bundle |
| 3 | initialPayload | ByteArray | variable | Optional: the first encrypted message payload, bundled for efficiency |

---

# 7. Cryptographic Protocol Design

## Overview

All cryptographic operations run exclusively on Alice. Bob never receives plaintext or session key material. Every message passes through a fixed, invariant pipeline. No step is skipped. No step is reordered.

## The Pipeline

**Composition → Padding → Compression → Encryption → CBOR Serialisation → QR Generation**

Padding before Compression prevents compression-oracle attacks. This order is fixed and non-negotiable.

## Cryptographic Primitives

All cryptographic operations in Phase 1 use BouncyCastle (`org.bouncycastle:bcprov-jdk18on`). No other library performs cryptographic operations. No primitive is implemented by hand. The BouncyCastle provider is registered once at application startup. Android's bundled BouncyCastle provider is removed before registration.

| Primitive | Algorithm | RFC | Purpose |
|---|---|---|---|
| Key agreement | X25519 | 7748 | DH for X3DH and Double Ratchet |
| Signatures | Ed25519 | 8032 | Identity Key signing, bundle authentication |
| Key derivation | HKDF-SHA256 | 5869 | Session key derivation throughout |
| Symmetric AEAD | ChaCha20-Poly1305 | 8439 | All message encryption |
| Hash | SHA-256 | — | Hash chain, entropy pool, HKDF |
| Password KDF | Argon2id | 9106 | Storage key derivation |
| Compression | zstd (zstd-jni) | — | Pre-encryption. Phase 1 only. Replaced by Rust in Phase 2. |

## Message Constraints

- Character set: lowercase a–z, digits 0–9, space. 37 symbols total.
- Maximum length: 499 characters.
- Encoded: base-37 → approximately 324 bytes.
- QR capacity ceiling: Type 40 ECL H, binary mode, 1,273 bytes.

## Entropy — Alice Only

Alice does not trust the hardware RNG. Entropy is gathered continuously from multiple independent sources and SHA-256 HMAC chained into a running pool:

- Accelerometer and gyroscope noise
- Microphone sensor noise
- Camera sensor noise
- Timing jitter
- User interaction events

The pool is the sole source of randomness for all key generation on Alice. Hardware RNG output is not used.

## HKDF Info Strings

All HKDF derivations use domain-separated info strings. No two derivation contexts share an info string. The namespace prefix `AliceBob_v1_` is reserved.

| Context | Info string |
|---|---|
| X3DH shared secret | `AliceBob_v1_X3DH` |
| Double Ratchet root ratchet | `AliceBob_v1_DR_RK` |
| Type 10 message authentication MAC | `AliceBob_v1_MAC` |
| SAS phrase derivation | `AliceBob_v1_SAS` |

## Identity Key Structure

Each Alice installation generates two distinct long-term key pairs at first launch.

- **IK_Ed** — Ed25519 key pair. Used exclusively for signing.
- **IK_X** — X25519 key pair. Used exclusively for Diffie-Hellman.

These key pairs are generated independently. They are never mixed. Both public components appear in the Contact Card (Type 2 QR).

## Key Agreement — X3DH

X3DH establishes a shared secret between two parties for the first time. It runs once per contact during the Engagement Ceremony.

**Parameters**

| Parameter | Value |
|---|---|
| Domain separator `F` | 32 bytes of `0xFF` |
| Salt | 32 zero bytes |
| HKDF info | `AliceBob_v1_X3DH` |
| Output length | 32 bytes — becomes the initial Root Key |

**Computation — initiator**

```
DH1 = X25519(IK_A_priv_X,  SPK_B_pub)
DH2 = X25519(EK_A_priv,    IK_B_pub_X)
DH3 = X25519(EK_A_priv,    SPK_B_pub)
DH4 = X25519(EK_A_priv,    OPK_B_pub[consumed])

IKM = F || DH1 || DH2 || DH3 || DH4
SK  = HKDF(IKM, salt = ZERO, info = "AliceBob_v1_X3DH", len = 32)
```

This order is fixed. The responder executes identical steps in identical order and arrives at the same SK.

Before computing X3DH, the initiator verifies the SPK signature against the responder's `IK_B_pub_Ed`. A failed verification aborts the ceremony immediately.

The ephemeral key `EK_A` and the consumed One-Time Prekey are zeroed immediately after use. The index of the consumed OPK is transmitted to the responder in the Type 11 payload.

**Initial DH Ratchet Step**

Immediately after X3DH, the initiator bootstraps the sending chain:

1. Generate a fresh ratchet key pair `(DHS_priv, DHS_pub)`.
2. Compute `dhOut = X25519(DHS_priv, SPK_B_pub)`.
3. Apply KDF_RK: `(rk, cks) = KDF_RK(SK, dhOut)`.
4. Zero `DHS_priv`.
5. Set session state: `rk`, `cks`, `ckr = null`, `dhr = SPK_B_pub`, `dhsPub = DHS_pub`.

The responder performs the identical step using its own SPK private key to arrive at the same `rk` and `cks`.

## Session Management — Double Ratchet

The Double Ratchet manages the ongoing session after X3DH completes. It combines a DH ratchet (forward secrecy and break-in recovery) with a symmetric ratchet (per-message key derivation). The Double Ratchet is implemented in pure Kotlin. It is not sourced from a library. It is tested against the official specification test vectors before any other test is written.

**Session State**

| Field | Type | Description |
|---|---|---|
| `rk` | 32 bytes | Root Key |
| `cks` | 32 bytes | Sending Chain Key |
| `ckr` | 32 bytes or null | Receiving Chain Key. Null until first receive. |
| `dhsPriv` | 32 bytes | Current sending DH private key |
| `dhsPub` | 32 bytes | Current sending DH public key |
| `dhr` | 32 bytes or null | Last received DH public key |
| `ns` | UInt32 | Sending counter |
| `nr` | UInt32 | Receiving counter |
| `pn` | UInt32 | Previous sending chain length |
| `mkSkipped` | Map | Skipped message keys, keyed by `(pn, ns)`. Hard limit: 1,000 entries. |

**KDF_RK** — applied when the DH ratchet advances:

```
HKDF(dhOut, salt = currentRK, info = "AliceBob_v1_DR_RK", len = 64)
  first 32 bytes → newRK
  last 32 bytes  → newCK
```

**KDF_CK** — applied for every message to derive the message key and advance the chain:

```
HMAC-SHA256(key = CK, data = 0x01) → MK      (message key — used once, zeroed immediately)
HMAC-SHA256(key = CK, data = 0x02) → nextCK  (next chain key)
```

Both HMAC operations key on the same `CK`. They use separate instances with distinct single-byte data inputs.

**RatchetEncrypt — exact sequence**

1. `(MK, newCK) = KDF_CK(cks)` → `cks = newCK`
2. `header = dhsPub (32 bytes) || pn.to4BytesBigEndian() || ns.to4BytesBigEndian()` → 40 bytes total
3. `nonce = EntropyPool.nextBytes(12)`
4. `fullAD = IK_A_pub_X (32) || IK_B_pub_X (32) || header`
5. `ciphertext = ChaCha20-Poly1305(key = MK, nonce, plaintext, ad = fullAD)` — 128-bit tag appended
6. Zero `MK` immediately
7. `ns++`
8. Produce Type 3 payload: sequence, timestamp, nonce, header, ciphertext
9. Produce Type 10 MAC:
   `macData = CBOR(Type3Payload) || fullAD`
   `mac = HKDF(macData, salt = currentRK, info = "AliceBob_v1_MAC", len = 32)`
10. Produce Type 10 payload: mac, senderIkPubX, recipientIkPubX, sequenceRef, timestamp

**RatchetDecrypt — algorithm**

1. Parse `header` → `dhPub` (32 bytes), `PN` (4 bytes), `Ns` (4 bytes).
2. Reconstruct `fullAD = IK_A_pub_X || IK_B_pub_X || header`.
3. If `(PN, Ns)` is in `mkSkipped`: decrypt with the stored key, delete the entry, zero the key, return plaintext.
4. If `dhPub == dhr`: advance the symmetric ratchet on `ckr`. Store skipped keys between `nr` and `Ns`. Decrypt at position `Ns`. Set `nr = Ns + 1`. Zero `MK`.
5. If `dhPub != dhr`: store remaining skipped keys from the current receiving chain. Compute `dhOut = X25519(dhsPriv, dhPub)`. Apply KDF_RK. Update `rk`, `ckr`, `dhr`. Reset `pn = ns`, `ns = 0`, `nr = 0`. Advance `ckr` through any skipped positions in the new chain. Decrypt at position `Ns`. Set `nr = Ns + 1`. Zero `MK`.
6. If `mkSkipped` exceeds 1,000 entries at any point: abort. This is a protocol violation.

## Key Derivation

| Derived key | Method | Parameters |
|---|---|---|
| Alice Room DB key | Argon2id | 64 MiB, 3 iterations, parallelism 4, 32-byte output, device-unique 16-byte salt |
| Bob SQLCipher key | Argon2id | 64 MiB, 3 iterations, parallelism 4, 32-byte output, user app-PIN |
| X3DH shared secret | HKDF-SHA256 | See X3DH section above |
| Session chain keys | KDF_RK, KDF_CK | See Double Ratchet section above |

Alice uses a device-unique salt stored alongside the encrypted database. No Android Keystore is used on Alice. Hardware is not trusted on Alice.

Bob uses the user's app-PIN as the Argon2id input. No biometric authentication. PIN is the sole factor.

## SAS Verification Phrase

After X3DH completes, both parties independently derive a verification phrase from the shared Root Key.

```
sasBytes = HKDF(rootKey, salt = ZERO, info = "AliceBob_v1_SAS", len = 8)
```

Five non-overlapping 11-bit windows are extracted from the 64-bit `sasBytes` value:

| Word | Bits |
|---|---|
| 0 | 63–53 |
| 1 | 52–42 |
| 2 | 41–31 |
| 3 | 30–20 |
| 4 | 19–9 |

Each 11-bit value (range 0–2047) indexes into a 2,048-word EFF word list. The five words are joined with spaces to form the phrase. Both parties read this phrase aloud during Step 8 of the Engagement Ceremony. A match confirms no substitution occurred during the ceremony.

## Cryptographic Properties

- Every message uses a unique key derived fresh by the symmetric ratchet.
- Message keys are used once and zeroed immediately after use.
- The Identity Key is the long-term trust anchor. It is encrypted at rest by the Argon2id-derived storage key. It never leaves Alice.
- No session key material is persisted.
- Contact Records persist public keys only. All private key material exists in memory only during active use.
- Compromise of one message key exposes only that message.
- Forward secrecy covers all past messages.
- Break-in recovery is provided by the DH ratchet.
- All private and ephemeral keys are zeroed in memory immediately after use.

## The VaultCryptoEngine Interface

All cryptographic operations on Alice pass through a single interface. This boundary isolates cryptographic code from all application code. It enables Phase 1 (Kotlin) and Phase 2 (Rust via UniFFI) to be swapped with a single dependency injection change. No code outside this interface touches cryptographic primitives directly.

```kotlin
interface VaultCryptoEngine {
    fun generateIdentityKeypair(entropy: EntropyPool): IdentityKeypair
    fun generateKeyBundle(identity: IdentityKeypair, entropy: EntropyPool): KeyBundle
    fun initSession(bundle: KeyBundle, myIdentity: IdentityKeypair, entropy: EntropyPool): Pair<SessionState, X3dhInitialPayload>
    fun ratchetEncrypt(session: SessionState, plaintext: ByteArray, ad: ByteArray, senderIkPubX: ByteArray, recipientIkPubX: ByteArray, entropy: EntropyPool): Pair<MessagePayload, MessageAuthPayload>
    fun ratchetDecrypt(session: SessionState, message: MessagePayload, auth: MessageAuthPayload, ad: ByteArray): ByteArray
    fun deriveSasPhrase(rootKey: ByteArray): String
    fun deriveStorageKey(passphrase: CharArray, salt: ByteArray): ByteArray
}

// Phase 1:  KotlinVaultCryptoEngine : VaultCryptoEngine
// Phase 2:  RustVaultCryptoEngine   : VaultCryptoEngine  (via UniFFI)
```

Swapping Phase 1 to Phase 2 is one line in the Koin dependency injection module. Nothing else changes.

---

# 8. CC Skills Backlog

The following Claude Code skills are required and will be developed:

- **Naming Skill** — governs naming of packages, modules, classes, functions, variables, and all identifiers across both applications
- **Common Sense Skill** — governs how CC thinks before acting. Prevents over-engineering, unnecessary dependencies, and silent compliance
- **Technical Writing Skill** — governs documentation style. Pure factual, no filler, all killer, unambiguous

---

# 9. Open Questions

## Active — Required Before Coding Begins

- **Capability flag registry** — assignment of bits 0–15 of the uint16 capability mask
- **SAS wordlist** — selection and embedding of the 2,048-word EFF word list
- **UI verification states** — visual treatment of Verified vs Unverified contacts across Alice and Bob

## Active — Required Before Production

- **Air gap surveillance specification** — what Alice monitors, monitoring frequency, violation response behaviour, suppression detection
- **QR visual differentiation** — each type has a distinct visual identity so users know what they are scanning before they scan it
- **Custom keyboard visual design** — working implementation exists. Architecture and security properties are complete. Look and feel does not meet the UI standard. Full visual design required before shipping.
- **Vocabulary review** — full naming audit applied across the entire document when the Naming Skill is complete

## Deferred

- **Phase 2 VM design** — domain-specific Rust VM, reduced instruction set, ZK proof integration. Not required until Phase 1 is proven.
- **Fading message input** — reduces plaintext exposure window during composition. Phase 2 candidate.

---

*Document status: Active draft. Every decision is subject to challenge. No decision is final.*
