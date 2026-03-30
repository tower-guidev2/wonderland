package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.Test

class ScheduleUniqueNameTest {

    @Test
    fun valuePreservesSuppliedString() {
        assertThat(ScheduleUniqueName("surveillance_fast").value).isEqualTo("surveillance_fast")
    }

    @Test
    fun equalityBasedOnValue() {
        assertThat(ScheduleUniqueName("surveillance_fast")).isEqualTo(ScheduleUniqueName("surveillance_fast"))
    }

    @Test
    fun inequalityForDifferentValues() {
        assertThat(ScheduleUniqueName("surveillance_fast")).isNotEqualTo(ScheduleUniqueName("surveillance_slow"))
    }
}
