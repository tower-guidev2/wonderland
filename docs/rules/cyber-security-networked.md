# Android Cybersecurity Protocol — Network-Exposed Device with Google Services

**Full spec:** Authoritative. Cross-ref `crypto.md`.  
**Threat model:** Network-exposed Android with full Google services.  
**Version:** 1.0 (Mar 2026).

## Authoritative Sources (Enforce Every Decision)
- https://developer.android.com/privacy-and-security/security-best-practices
- https://developer.android.com/privacy-and-security/security-tips
- https://developer.android.com/security
- https://mas.owasp.org/MASVS/ (MASVS v2)
- https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-163r1.pdf
- https://source.android.com/docs/security/best-practices

## Fundamental Rules (Non-Negotiable)
- Zero-trust + defense-in-depth. Assume hostile network/rooted device.
- All ops via `ISecurityEngine`. Fail closed.
- Minimum permissions. Play Integrity on every critical call.
- No custom crypto — use `crypto.md` only.
- Assume device tampered; use Google Play Protect.

## Secure Architecture
- Pipeline: Input Validation → Play Integrity → Auth → Crypto (`crypto.md`) → Storage/Transmit.
- Sandbox sacred: `exported=false`, no `WORLD_READABLE`.
- Obfuscation (R8 full) + anti-debug mandatory.

## Controls (MASVS-Aligned)
- **Storage:** Internal + Keystore + EncryptedFile/Room (per crypto.md).
- **Network:** HTTPS + pinning + Network Security Config. No cleartext.
- **Auth:** Credential Manager + Biometric + short-lived tokens.
- **Permissions/IPC:** Explicit intents + signature protection.
- **Integrity:** Play Integrity API + dependency scanning + Play Protect.
- **Code:** Validate all input; keep Google Play Services/dependencies current.

## PR Checklist (Block Merge if Any Fail)
1. Threat model updated? 2. Permissions minimized? 3. HTTPS/pinning? 4. Play Integrity called?
5. Crypto per `crypto.md`? 6. Exported=false? 7. Deps vetted? 8. MASVS/NIST passed?

## Threat Coverage
MITM/remote/network/IPC/reverse-eng/supply-chain/side-channel all mitigated by Play Integrity + protocol.

**Claude instruction:** Act as Android cyber expert under this exact network-exposed protocol. Reference only these URLs. Never suggest air-gapped or GrapheneOS-specific features.