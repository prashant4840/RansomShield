package com.ransomshield.earlywarning.engine

import com.ransomshield.earlywarning.domain.AppRisk
import com.ransomshield.earlywarning.domain.RiskLevel

/**
 * Smart response: auto-suggest kill, highlight suspicious app, rank by risk contribution.
 */
class SmartResponseEngine {
    fun getSuggestedKillTarget(
        appRanking: List<AppRisk>,
        currentRisk: Float
    ): AppRisk? {
        if (currentRisk < 85f) return null
        return appRanking.maxByOrNull { it.riskScore }?.takeIf { it.riskScore >= 60f }
    }

    fun rankByRiskContribution(
        apps: List<AppRisk>,
        currentScore: Float
    ): List<AppRisk> = apps.sortedByDescending { it.riskScore }

    fun shouldShowAutoSuggest(riskLevel: RiskLevel, confidence: Float): Boolean =
        riskLevel == RiskLevel.HIGH && confidence >= 0.7f
}
