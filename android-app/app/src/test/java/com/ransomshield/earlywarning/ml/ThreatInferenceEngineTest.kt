package com.ransomshield.earlywarning.ml

import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.TelemetrySample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreatInferenceEngineTest {
    @Test
    fun highBurstSignalsShouldScoreHighRisk() {
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
        val result = engine.score(sample)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.score >= 75f)
    }
}
