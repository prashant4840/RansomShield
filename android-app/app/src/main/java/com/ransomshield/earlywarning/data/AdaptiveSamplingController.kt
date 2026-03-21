package com.ransomshield.earlywarning.data

class AdaptiveSamplingController {
    fun intervalMs(lastRiskScore: Float): Long {
        return when {
            lastRiskScore >= 80f -> 500L
            lastRiskScore >= 60f -> 800L
            lastRiskScore >= 40f -> 1200L
            else -> 1800L
        }
    }
}
