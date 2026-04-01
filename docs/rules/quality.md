# Code Quality Rules — Non-Negotiable

Full skill: `/mc-quality`. This file is a summary — the skill is authoritative.

Foundation: Google Kotlin Style Guide + JetBrains Kotlin Conventions + Uncle Bob Clean Code, with project overrides winning every conflict.

---

## Hard Bans

No `lateinit`. No `!!`. No `!` negation (use `.not()`). No `apply`. No `runCatching`. No companion objects. No comments or KDoc. No wildcard imports. No magic numbers/strings. No backtick function names. No abbreviations. No `@Deprecated`. No builder pattern. No `protected`. No abstract classes (unless framework-forced). No `var` on class properties. No mutable collection properties. No negative-logic names. No technology names in identifiers. No `runBlocking` in tests.

---

## Mandatory Patterns

- **Value classes** for all domain function arguments — never raw types
- **Max 3 parameters** per function — overflow grouped into value objects
- **Arrow `Either`** for all errors — sealed error hierarchy per domain boundary
- **Structured concurrency** — paging loops check `isActive`, dispatchers injected via constructor
- **Scope functions:** `let`, `also`, `run`, `with` only — no nesting
- **`sealed interface`** by default, `fun interface` for single-method, `enum class` for uniform variants
- **`when`** on sealed types: always expression, always exhaustive, no `else`
- **Immutability:** `val` everywhere, `var` only local loop accumulators
- **Visibility:** API modules expose interfaces only, implementations `internal`/`private`
- **Nullability:** map nulls at boundary, zero nulls in domain
- **Composition over inheritance:** interfaces + delegation, max 1 level deep
- **Extensions:** centralised per module, tangible benefit required
- **Explicit return types** on public/internal functions
- **Functions start with a verb.** Booleans use `is`/`has`/`can`/`should` prefix.
- **Sealed variants** name the specific case, never repeat the parent name
- **Extension functions** read as English at the call site

---

## Formatting

180-char lines. 4-space indent. K&R braces. Trailing commas everywhere. Single-line `if`/`else` on separate lines, no braces, properly indented. Positive case first. `.not()` not `!`. String templates always. Import to leaf level. Acronyms treated as words.

---

## File Organisation

One public type per file. Sealed families may share. JSON model families may share. Classes kept to single screen (~50-80 lines). Constants in top-level `object` containers named for domain purpose.
