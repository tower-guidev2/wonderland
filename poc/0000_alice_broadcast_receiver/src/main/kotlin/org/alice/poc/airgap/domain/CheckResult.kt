package org.alice.poc.airgap.domain

import arrow.core.Either

data class CheckResult(
    val surface: SurfaceName,
    val outcome: Either<ViolationDetail, SafeDetail>,
) {
    val isViolating: Boolean get() = outcome.isLeft()

    val detail: String get() = outcome.fold(
        ifLeft = { it.message },
        ifRight = { it.message },
    )
}
