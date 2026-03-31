# Air-Gap Surveillance — Implementation Progress

**Branch:** `feature/air-gap-surveillance`
**Date:** 2026-03-30
**Commits:** 20 on branch (from `32894c7` to `33bcc33`)
**Tests:** 68 passing (10 surveillance_api, 58 surveillance), BUILD SUCCESSFUL

## Completed

All 19 plan tasks done. Code review done. Review fixes applied.

### surveillance_api module (pure Kotlin domain)
- AirGapViolation — 23-type sealed class with HARD/SOFT severity
- AirGapStatus — Secure / Compromised sealed interface
- IAirGapSurveillance — reactive interface (StateFlow + Flow)
- FakeAirGapSurveillance — test double for consumer modules

### surveillance module (Android implementation)
- Pure functions: FastTierChecks, StandardTierChecks, IntentToViolationMapper, DeviceIntegrityVerifier
- Provider interfaces + Android impls: Settings, Adapters, Network, Build properties
- Workers: FastTierWorker (15min), StandardTierWorker (1hr), SlowTierWorker (6hr)
- Reactive layer: AirGapSurveillance (BroadcastReceiver + callbackFlow), AirGapNetworkMonitor (dual NetworkCallback)
- Koin: SurveillanceModule, SurveillanceWorkerFactory
- App Startup: AirGapInitializer with synchronous fast-tier gate
- Manifest: 4 surveillance permissions + InitializationProvider

## Code Review Issues — Resolved

- C1: IViolationHandler Koin binding added (was missing — runtime crash)
- C2: ACTION_POWER_CONNECTED broadcast added for USB power detection
- C3: Wi-Fi Aware broadcast added to reactive layer
- I5: AirGapNetworkMonitor flows now collected in startCollecting()
- I3: Tethering detection uses sticky broadcast (was using wrong Settings.Global approach)
- I2: All naming rule violations fixed (!=, !in → .not(), positive-first)
- Magic strings extracted to named constants, SDK constants used for intent actions

## Still Outstanding

### Must do before merge
- **StrongBox attestation (Layer 2 of SlowTierWorker):** Ephemeral EC key in Titan M2, ASN.1 parsing of KeyDescription, bootloader lock check, GrapheneOS verified boot key verification. The AttestationFailed violation type exists but no code path produces it. This is the strongest verification in the system.
- **Full sweep for remaining CLAUDE.md rule violations:** The subagents missed rules. A manual pass through every file is warranted.
- **CLAUDE.md update:** Section 12 of the spec lists CLAUDE.md items needing update (some done, verify all)

### Should do before merge
- `uses-feature` declarations (belong in `alice:app` manifest, not surveillance module)
- StandardTierChecks spec gap: USB devices, storage encryption, UWB checks have no violation types — needs spec update or new violation types
- WorkScheduler Robolectric test was planned but not written (Task 7 in plan)

### Not needed for merge
- Physical hardware testing on GrapheneOS Pixel device (manual acceptance)
- Instrumented tests (manual on device)
