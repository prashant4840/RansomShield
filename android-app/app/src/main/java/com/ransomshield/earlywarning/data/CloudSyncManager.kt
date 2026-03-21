package com.ransomshield.earlywarning.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ransomshield.earlywarning.domain.ThreatAssessment

class CloudSyncManager {
    fun syncThreatEvent(assessment: ThreatAssessment): Boolean {
        return runCatching {
            val db = FirebaseFirestore.getInstance()
            db.collection("threat_events")
                .add(
                    mapOf(
                        "timestamp" to FieldValue.serverTimestamp(),
                        "score" to assessment.score,
                        "risk" to assessment.riskLevel.name,
                        "reasons" to assessment.reasons
                    )
                )
            true
        }.getOrElse { false }
    }
}
