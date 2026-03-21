package com.ransomshield.earlywarning.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveSamplingControllerTest {
    @Test
    fun intervalShouldBeFastForHighRisk() {
        val controller = AdaptiveSamplingController()
        assertEquals(500L, controller.intervalMs(85f))
        assertEquals(1800L, controller.intervalMs(12f))
    }
}
