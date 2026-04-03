# QR Protocol — Key Decisions

Full field registries: `docs/design.md` Section 6. This is a summary — the spec is authoritative.

---

## Wire Format

`[type: 1 byte][capabilities: 2 bytes][CBOR payload]`

- **11 QR types** defined. Types 8 and 9 deferred to V2.
- **CBOR integer keys only.** No string keys.
- **Strictly additive schema evolution.** Field numbers are permanent. Never reused. Never redefined.
- **Unknown fields silently ignored** — baked in from day one.

---

## QR Display & Capacity

- **Error correction:** Level H (30%) — tolerates partial obstruction during scanning.
- **Max payload:** QR Version 40, error correction H = 1,273 bytes binary.
- **Display:** max practical size, high contrast (black on white), no transparency, no decorative overlays.
- **Quiet zone:** minimum 4 modules wide (white border preserved).

---

## Dual-QR Message Transmission

Every message produces **Type 3 + Type 10** simultaneously. Bob scans both in one camera frame via ML Kit bundled model.
