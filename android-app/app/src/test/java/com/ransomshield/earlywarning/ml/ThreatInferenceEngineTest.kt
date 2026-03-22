package com.ransomshield.earlywarning.ml

import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.TelemetrySample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreatInferenceEngineTest {

    @Test
    fun highBurstSignalsWithHighRealRiskShouldScoreHigh() {
        val engine = ThreatInferenceEngine()
        val sample = TelemetrySample(
            timestampMs = 1L,
            cpuUsage = 88f,
            memoryUsage = 82f,
            ioOpsPerSec = 90f,
            fileMutationBurst = 97f,
            accessibilityAbuseSignal = 90f,
            suspiciousPermissionScore = 86f
        )
        val result = engine.computeHybridScore(85f, sample)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.score >= 70f)
    }

    @Test
    fun lowSignalsWithLowRealRiskShouldScoreLow() {
        val engine = ThreatInferenceEngine()
        val sample = TelemetrySample(
            timestampMs = 1L,
            cpuUsage = 15f,
            memoryUsage = 20f,
            ioOpsPerSec = 25f,
            fileMutationBurst = 10f,
            accessibilityAbuseSignal = 5f,
            suspiciousPermissionScore = 12f
        )
        val result = engine.computeHybridScore(15f, sample)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertTrue(result.score < 60f)
    }

    @Test
    fun scoreShouldIncludeExplainableReasons() {
        val engine = ThreatInferenceEngine()
        val sample = TelemetrySample(
            timestampMs = 1L,
            cpuUsage = 88f,
            memoryUsage = 85f,
            ioOpsPerSec = 90f,
            fileMutationBurst = 95f,
            accessibilityAbuseSignal = 92f,
            suspiciousPermissionScore = 88f
        )
        val result = engine.computeHybridScore(80f, sample)
        assertTrue(result.reasons.isNotEmpty())
    }
}
