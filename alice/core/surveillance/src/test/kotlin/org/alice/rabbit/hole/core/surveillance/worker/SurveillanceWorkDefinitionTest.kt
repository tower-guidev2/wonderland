package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.isEqualTo
import androidx.work.ExistingPeriodicWorkPolicy
import org.alice.rabbit.hole.core.surveillance.worker.fast.FastTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.slow.SlowTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.standard.StandardTierDefinition
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class SurveillanceWorkDefinitionTest {

    @Test
    fun fastTierHasFifteenMinuteInterval() {
        assertThat(FastTierDefinition.repeatInterval).isEqualTo(15.minutes)
    }

    @Test
    fun fastTierHasCorrectUniqueName() {
        assertThat(FastTierDefinition.uniqueName).isEqualTo(ScheduleUniqueName("surveillance_fast"))
    }

    @Test
    fun standardTierHasOneHourInterval() {
        assertThat(StandardTierDefinition.repeatInterval).isEqualTo(1.hours)
    }

    @Test
    fun slowTierHasSixHourInterval() {
        assertThat(SlowTierDefinition.repeatInterval).isEqualTo(6.hours)
    }

    @Test
    fun allDefinitionsUseKeepPolicy() {
        listOf(FastTierDefinition, StandardTierDefinition, SlowTierDefinition).forEach { definition ->
            assertThat(definition.existingWorkPolicy).isEqualTo(ExistingPeriodicWorkPolicy.KEEP)
        }
    }
}
