# Android Cybersecurity Protocol — Air-Gapped GrapheneOS Device (No Google Services)

**Full spec:** Authoritative. Cross-ref `crypto-protocol.md`.  
**Threat model:** 100% air-gapped GrapheneOS (no network, no Google Play/Services).  
**Version:** 1.0 (Mar 2026).

## Authoritative Sources (Enforce Every Decision)
- https://grapheneos.org/features (hardened malloc, exec-spawning, USB-C control, verified boot)
- https://developer.android.com/privacy-and-security/security-best-practices
- https://developer.android.com/privacy-and-security/security-tips
- https://mas.owasp.org/MASVS/ (MASVS v2)
- https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-163r1.pdf

## Fundamental Rules (Non-Negotiable)
- Zero-trust + defense-in-depth. Assume physical tampering/root.
- All ops via `IVaultCryptographyEngine`. Fail closed.
- No network = no remote/MITM threats; focus on local/physical.
- Minimum permissions; disable USB data (charging-only via GrapheneOS).
- Use `crypto-protocol.md` only. No Google APIs.
- GrapheneOS exploit mitigations mandatory (hardened malloc/libc, exec-spawning, SELinux/seccomp).

## Secure Architecture
- Pipeline: Input Validation → GrapheneOS Verified Boot + Integrity → Auth → Crypto (`crypto-protocol.md`) → Storage.
- Sandbox sacred: `exported=false`, no external storage for secrets.
- Enable all GrapheneOS hardening: MTE, sensor controls, PIN scrambling.

## Controls (MASVS-Aligned)
- **Storage:** Internal + hardware Keystore + EncryptedFile/Room (per crypto-protocol.md).
- **Network:** None (air-gapped). Airplane mode + no SIM.
- **Auth:** Credential Manager + strong PIN (scrambling enabled). No `androidx.biometric` — bypassable.
- **Permissions/IPC:** Explicit intents + signature protection only.
- **Integrity:** GrapheneOS verified boot + Auditor (local) + no dynamic loading.
- **Code:** Validate all input; use GitHub Releases APK (sideloaded) only; keep GrapheneOS updated.

## PR Checklist (Block Merge if Any Fail)
1. Threat model updated? 2. USB charging-only? 3. All GrapheneOS hardening enabled?
4. Crypto per `crypto-protocol.md`? 5. Exported=false? 6. No network code? 7. Deps vetted? 8. MASVS/NIST passed?

## Threat Coverage
Physical access/USB/side-channel/local malware/supply-chain all mitigated by GrapheneOS + protocol.

**Claude instruction:** Act as Android cyber expert under this exact air-gapped GrapheneOS protocol. Reference only these URLs. Never suggest Google services or network features.