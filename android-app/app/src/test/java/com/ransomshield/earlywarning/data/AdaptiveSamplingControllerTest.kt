package com.ransomshield.earlywarning.data

import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveSamplingControllerTest {

    @Test
    fun highRiskReducesInterval() {
        val controller = AdaptiveSamplingController()
        val interval = controller.intervalMs(85f)
        assertTrue(interval <= 600L)
    }

    @Test
    fun lowRiskIncreasesInterval() {
        val controller = AdaptiveSamplingController()
        val interval = controller.intervalMs(20f)
        assertTrue(interval >= 1500L)
    }

    @Test
    fun mediumRiskUsesMiddleInterval() {
        val controller = AdaptiveSamplingController()
        val interval = controller.intervalMs(55f)
        assertTrue(interval in 800L..1300L)
    }
}
