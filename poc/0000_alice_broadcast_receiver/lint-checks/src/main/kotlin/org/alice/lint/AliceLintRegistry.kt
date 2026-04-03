package org.alice.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

// Lint API requires extending abstract class IssueRegistry — framework constraint.
class AliceLintRegistry : IssueRegistry() {

    override val issues: List<Issue> = listOf(
        SemanticsNotSealedDetector.ISSUE,
    )

    override val api: Int = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "Alice Air-Gap Security",
        identifier = "org.alice.lint",
    )
}
