package com.ransomshield.earlywarning.assistant

import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.RiskSignalBreakdown
import com.ransomshield.earlywarning.domain.ThreatAssessment

class ThreatAssistant {
    fun guidance(assessment: ThreatAssessment, breakdown: RiskSignalBreakdown?): String {
        val topSignals = breakdown?.let { listOfNotNull(
            if (it.cpu > 70f) "High CPU (${it.cpu.toInt()}%)" else null,
            if (it.memory > 75f) "High memory (${it.memory.toInt()}%)" else null,
            if (it.permissionAbuse > 60f) "Permission abuse signal" else null,
            if (it.fileActivity > 65f) "Elevated file activity" else null,
            if (it.networkSpike > 55f) "Network spike" else null,
            if (it.processCount > 70f) "Unusual app count" else null
        ).filter { s -> s != null }.take(3) } ?: emptyList()

        val signalSummary = if (topSignals.isNotEmpty())
            "Drivers: ${topSignals.joinToString(" + ")}."
        else ""

        return when (assessment.riskLevel) {
            RiskLevel.HIGH -> "Potential ransomware behaviour detected. $signalSummary Isolate the target app, revoke permissions, and run backup rollback."
            RiskLevel.MEDIUM -> "Elevated risk. $signalSummary Review recently installed apps and revoke suspicious permissions."
            RiskLevel.LOW -> "Baseline behaviour within normal range. Keep monitoring active."
        }
    }
}
