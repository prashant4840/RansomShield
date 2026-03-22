package com.ransomshield.earlywarning.engine

import com.ransomshield.earlywarning.domain.RiskSignalBreakdown

/**
 * Dynamic threshold tuning and signal calibration.
 * Reduces false positives via percentile-based thresholds and context-aware scoring.
 */
class ThresholdTuner(
    private val highPercentile: Float = 92f,
    private val mediumPercentile: Float = 75f
) {
    private val scoreHistory = ArrayDeque<Float>(30)

    fun computeAdjustedScore(
        rawScore: Float,
        breakdown: RiskSignalBreakdown,
        isIdle: Boolean
    ): Pair<Float, Float> {
        scoreHistory.addLast(rawScore)
        if (scoreHistory.size > 30) scoreHistory.removeFirst()

        val baseline = scoreHistory.average().toFloat()
        val variance = scoreHistory.maxOrNull()?.minus(scoreHistory.minOrNull() ?: 0f) ?: 0f

        // Idle: raise bar (fewer FPs). Active: slightly lower bar
        val contextFactor = if (isIdle) 1.15f else 0.95f
        val highThreshold = (85f * contextFactor).coerceIn(75f, 95f)
        val mediumThreshold = (60f * contextFactor).coerceIn(50f, 75f)

        val adjusted = when {
            rawScore >= highThreshold -> rawScore
            rawScore >= mediumThreshold -> rawScore * 0.98f
            else -> rawScore
        }

        // Confidence: high when multiple signals agree, low when single spike
        val contributingSignals = listOf(
            breakdown.cpu > 70f,
            breakdown.memory > 75f,
            breakdown.fileActivity > 65f,
            breakdown.permissionAbuse > 60f,
            breakdown.networkSpike > 55f
        ).count { it }
        val confidence = (0.5f + contributingSignals * 0.1f).coerceIn(0.5f, 0.95f)

        return (adjusted.coerceIn(0f, 100f)) to confidence
    }
}
