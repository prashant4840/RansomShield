package com.ransomshield.earlywarning.ml

import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.TelemetrySample
import com.ransomshield.earlywarning.domain.ThreatAssessment

/**
 * Hybrid scoring: 0.7 * realRisk + 0.3 * mlScore.
 * Confidence from signal agreement. ML optional (rolling-window TFLite).
 */
class ThreatInferenceEngine(private val tfliteScorer: TFLiteScorer? = null) {

    fun computeHybridScore(
        realRiskScore: Float,
        sample: TelemetrySample,
        confidence: Float
    ): ThreatAssessment {
        val mlScore = tfliteScorer?.scoreOrNull(sample) ?: realRiskScore
        val finalScore = (0.7f * realRiskScore + 0.3f * mlScore).coerceIn(0f, 100f)

        val reasons = buildReasons(sample, finalScore)
        val risk = when {
            finalScore >= 85f -> RiskLevel.HIGH
            finalScore >= 60f -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return ThreatAssessment(
            score = finalScore,
            riskLevel = risk,
            reasons = reasons,
            confidence = confidence
        )
    }

    private fun buildReasons(sample: TelemetrySample, score: Float): List<String> = buildList {
        if (sample.cpuUsage > 75f) add("High CPU usage")
        if (sample.memoryUsage > 80f) add("Elevated memory")
        if (sample.fileMutationBurst > 70f) add("Rapid file activity — pre-encryption pattern")
        if (sample.accessibilityAbuseSignal > 60f) add("Accessibility abuse — lockscreen hijack risk")
        if (sample.suspiciousPermissionScore > 65f) add("Permission profile deviates from benign")
        if (sample.ioOpsPerSec > 75f) add("I/O spike exceeds normal behaviour")
        if (sample.cpuUsage > 80f && sample.memoryUsage > 70f)
            add("Sustained high CPU + memory — encryption workload")
        if (isEmpty()) add("Baseline monitoring — no critical anomalies")
    }
}
