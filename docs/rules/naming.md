# Naming Rules — Non-Negotiable

Locked decisions. Do not revisit, suggest alternatives, or silently revert to any external standard.

---

## Identifiers

- **No abbreviations. Ever.** Full words always. `cryptography` not `crypto`. `identifier` not `id`.
- **No technology names in identifiers.** Never embed library, language, or platform names — `Kotlin`, `Rust`, `ZXing`, `Room`, `Koin`, `Bouncy`, `CBOR` — in any identifier. Name what the thing *does*, not what runs it. Exceptions require explicit documented justification.
- **Interface prefix `I`.** Interfaces: `IFoo`. Primary implementation: `Foo`. Test fake: `FakeFoo`.
  - Example: `IVaultCryptographyEngine` → `VaultCryptographyEngine` → `FakeVaultCryptographyEngine`
- **No backtick function names. Ever.** Including in tests.
- **No magic numbers or strings.** Extract to named constants.
- **Short, sharp names.** Single words where possible. Max ~30 chars. Uncle Bob self-documenting.
- **No plurals** unless the thing genuinely is a collection.
- **Matching pairs and word families** — names in a set must share vocabulary.
- **No negative-logic names.** Name what something *is*, not what it isn't.
- **Max ~30 chars** per identifier. Over 30 means the design is wrong, not the name.

---

## Modules

- **No hyphens in module names.** Use `:` hierarchy — `core:ui`, not `core-ui`.
- **Exception: `build-logic`.** The only hyphen permitted anywhere in the project. It must stand apart from app modules and be immediately obvious in the project structure.
- **Module name segments are single lowercase words.** Never abbreviated.
- **Package names mirror module hierarchy exactly.**
  - Alice root: `org.alice.rabbit.hole` + module path segments → `org.alice.rabbit.hole.core.cryptography`
  - Bob root: `org.bob.cheshire.cat` + module path segments

---

## Undecided Naming

For any naming decision not yet covered by the above:
- Check `docs/design.md` first — the decision may already be made.
- If not there, present options with explicit trade-offs. Do not make a single recommendation and present it as obvious.
- Once the user decides, that decision is locked. Record it and follow it.
