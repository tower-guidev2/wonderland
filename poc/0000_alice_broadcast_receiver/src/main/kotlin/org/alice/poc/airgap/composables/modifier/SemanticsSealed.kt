package org.alice.poc.airgap.composables.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * Seals this composable's semantic tree against data exfiltration.
 *
 * On the air-gapped Alice device (GrapheneOS Pixel), accessibility
 * services are an exfiltration vector — a rogue or compromised service
 * could read semantic nodes and cache content for later relay when the
 * device is no longer air-gapped (e.g. after a factory reset and
 * reconnection).
 *
 * This modifier calls [clearAndSetSemantics] with an empty lambda,
 * rendering the composable and its entire subtree invisible to
 * TalkBack, autofill, content capture, and any current or future
 * accessibility binding.
 *
 * ## Contract
 * - MUST be the **first** modifier in every composable chain.
 * - Enforced at build time by the `SemanticsNotSealed` lint check
 *   (severity: ERROR).
 *
 * @see clearAndSetSemantics
 */
fun Modifier.semanticsSealed(): Modifier =
    this.clearAndSetSemantics { }
