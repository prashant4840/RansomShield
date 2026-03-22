package com.ransomshield.earlywarning.assistant

import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.ThreatAssessment
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreatAssistantTest {

    @Test
    fun highRiskReturnsActionableGuidance() {
        val assistant = ThreatAssistant()
        val assessment = ThreatAssessment(85f, RiskLevel.HIGH, listOf("Rapid file mutation resembles pre-encryption burst"))
        val guidance = assistant.guidance(assessment, null)
        assertTrue(guidance.contains("isolate") || guidance.contains("disable") || guidance.contains("rollback") || guidance.contains("safe mode"))
    }

    @Test
    fun mediumRiskReturnsModerateGuidance() {
        val assistant = ThreatAssistant()
        val assessment = ThreatAssessment(60f, RiskLevel.MEDIUM, listOf("Elevated file activity detected"))
        val guidance = assistant.guidance(assessment, null)
        assertTrue(guidance.contains("review") || guidance.contains("revoke") || guidance.contains("monitor"))
    }

    @Test
    fun lowRiskReturnsBaselineGuidance() {
        val assistant = ThreatAssistant()
        val assessment = ThreatAssessment(25f, RiskLevel.LOW, listOf("Mild anomaly detected"))
        val guidance = assistant.guidance(assessment, null)
        assertTrue(guidance.contains("No critical") || guidance.contains("normal") || guidance.contains("Keep monitor"))
    }
}
