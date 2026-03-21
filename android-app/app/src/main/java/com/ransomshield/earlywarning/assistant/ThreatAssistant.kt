package com.ransomshield.earlywarning.assistant

import com.ransomshield.earlywarning.domain.ThreatAssessment

class ThreatAssistant {
    fun guidance(assessment: ThreatAssessment): String {
        return when {
            assessment.score >= 80f -> "High-risk anomaly: isolate target app, disable accessibility permissions, and run backup rollback."
            assessment.score >= 50f -> "Medium-risk behavior detected: review recently installed apps and revoke suspicious permissions."
            else -> "No critical threat pattern. Keep monitor active and run routine permission audit."
        }
    }
}
