package com.ransomshield.earlywarning.ml

import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.TelemetrySample
import com.ransomshield.earlywarning.domain.ThreatAssessment
import kotlin.math.max

class ThreatInferenceEngine(private val tfliteScorer: TFLiteScorer? = null) {
    private val trailing = ArrayDeque<Float>()

    fun calibrateThreshold(
        baselineCpu: Float,
        baselineMemory: Float
    ): Float = (45f + (baselineCpu * 0.35f) + (baselineMemory * 0.25f)).coerceIn(50f, 75f)

    fun score(sample: TelemetrySample): ThreatAssessment {
        val fallback = (
            sample.cpuUsage * 0.12f +
                sample.memoryUsage * 0.10f +
                sample.ioOpsPerSec * 0.15f +
                sample.fileMutationBurst * 0.25f +
                sample.accessibilityAbuseSignal * 0.23f +
                sample.suspiciousPermissionScore * 0.15f
            ).coerceIn(0f, 100f)
        val weighted = tfliteScorer?.scoreOrNull(sample) ?: fallback
        trailing.addLast(weighted)
        if (trailing.size > 6) trailing.removeFirst()
        val smoothed = trailing.average().toFloat()

        val reasons = buildList {
            if (sample.fileMutationBurst > 70f) add("Rapid file mutation resembles pre-encryption burst")
            if (sample.accessibilityAbuseSignal > 60f) add("Accessibility abuse pattern indicates lockscreen hijack risk")
            if (sample.suspiciousPermissionScore > 65f) add("Permission profile deviates from benign baselines")
            if (sample.ioOpsPerSec > 75f) add("I/O spike exceeds normal app behavior")
            if (smoothed - weighted > 8f) add("Temporal model indicates persistent escalation trend")
            if (isEmpty()) add("Mild anomaly detected in behavior profile")
        }

        val risk = when {
            smoothed >= 80f -> RiskLevel.HIGH
            smoothed >= 50f -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return ThreatAssessment(score = max(smoothed, 5f), riskLevel = risk, reasons = reasons)
    }
}
