# UX_PATTERNS.md — Navigation and Interaction Patterns

## Navigation Model

- Single Activity architecture.
- All navigation is Compose-driven. Navigation library: Jetpack Navigation 3 (1.0.1).
- No fragment-based navigation.
- Deep linking is not supported (Alice is air-gapped, Bob has no reason for deep links).

## Screen Archetypes

### Conversation Screen
- Shows message history with a single contact.
- Compose area at bottom.
- Action to generate QR codes for sending.
- Action to scan incoming QR codes.

### QR Display Screen
- Full-screen QR code display (QR1 and QR2 shown sequentially or side by side depending on screen real estate).
- Clear instruction text: what the recipient needs to do.
- Tap or swipe to alternate between QR1 and QR2 if shown sequentially.
- No timeout — user dismisses manually.

### QR Scan Screen
- Full-screen camera viewfinder.
- ML Kit bundled scanner running continuously.
- Visual feedback on successful scan (not just a toast — a clear state change).
- Scans QR1 and QR2 in sequence with progress indication.

### Engagement Ceremony Screen
- Step-by-step wizard flow.
- Clear indication of which party acts next (Alice or Bob).
- Progress indicator showing ceremony stage.
- Confirmation screen at completion showing Contact Record summary.

### Key Bundle Management Screen (Alice)
- Shows remaining one-time key slots.
- Action to generate new Key Bundle.
- Displays Key Bundle as QR for Bob to scan.
- Warning state when slots are low (Bundle Refresh trigger).

### Air-Gap Status Screen (Alice only)
- Dashboard showing all 20+ attestation surfaces and their current state.
- Green/red per surface — no ambiguity.
- Last attestation timestamp.
- Device integrity summary (StrongBox, bootloader, verified boot).

### Tamper Screen (Alice only)
- Non-dismissable full-screen overlay.
- Triggered by hard violations.
- Confirms cryptographic zeroing has occurred.
- No navigation away — device must be re-provisioned.

## Interaction Contracts

- All destructive actions require explicit confirmation (e.g., deleting a contact, zeroing keys).
- Loading states are shown for any operation exceeding 100ms.
- Error states are shown inline, not as dialogs, unless the error is blocking.
- No snackbars or toasts — state-driven UI feedback only.
- No pull-to-refresh — there is nothing to refresh from a server.

## Empty States

Every screen that can be empty must have a purposeful empty state:
- Conversation list: "No contacts yet. Start an Engagement Ceremony to begin."
- Message history: "No messages yet. Compose your first message."
- Key slots: "No keys generated. Create a Key Bundle."
