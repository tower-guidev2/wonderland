# Rust ZKP VM Track

---

## Current Phase — Pure Rust Only

No Android. No NDK. No JNI. No `.so` compilation. Build and test with standard Rust tooling only (`cargo test`, `cargo bench`, `cargo build`). If a task appears to require NDK, it is premature — raise it.

---

## What This Library Proves

Zero-knowledge proofs for the Alice/Bob protocol.

**Priority 1:** chain membership, identity, encryption correctness.
**Priority 2:** key exchange correctness, freshness, deniability.
**Priority 3 (if proof size and performance allow):** padding correctness, bundle validity.

---

## Hard Constraints

- All proofs for a single message must fit within QR capacity after zstd compression. **Target: under 2KB.** If a proof system cannot meet this, it is disqualified.
- No network calls. Fully offline.
- Proving must run on Alice's ARM64 device. **Target: under 10s.** Under 30s acceptable for Phase 1 research.
- No per-circuit trusted setup — Alice cannot participate in a setup ceremony while air-gapped. Universal or transparent setup only.

---

## Library Selection

**Primary: Mopro + Halo2** — purpose-built for client-side mobile ZKP, no trusted setup, compact proofs, ~0.5s proving on Pixel 6 Pro, fully offline.

**Fallback: dalek zkp / bulletproofs** — use only if Merkle membership and verifiable encryption are not required.

**Avoid for on-device proving:** winterfell (memory hungry), risc0-zkp (server/offload), bellman Groth16 (trusted setup required).

---

## Phase 0 Deliverable — Research Document (BLOCKER: not yet delivered)

Before writing any ZKP circuit code, produce a structured research document covering:
1. Exact ZKP statements for each Priority 1 property with constraint count and proof size estimates
2. Minimal Halo2 proof built on dev machine with measured proof size
3. QR compatibility test — serialise + zstd, confirm under 2KB
4. Performance baseline with ARM64 scaling projection
5. Recommendation confirming Mopro + Halo2 or recommending dalek with justification from measured data

---

## Integration Path

| Phase | Scope |
|---|---|
| 1 | Pure Rust library, tested on dev machine |
| 2 | Add `ffi.rs` wrapping core library (thin wrapper only) |
| 3 | Compile to `.so` with cargo-ndk, commit to `rust-zkp-vm/prebuilt/` |
| 4 | Wire Kotlin FFI declarations in app modules |
| 5 | Migrate to Gradle-integrated build |

ABI targets: `arm64-v8a` (production), `x86_64` (emulator only).
Pre-built location: `rust-zkp-vm/prebuilt/` — Alice and Bob reference via `jniLibs` source set.
