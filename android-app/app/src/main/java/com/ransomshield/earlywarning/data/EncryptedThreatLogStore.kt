package com.ransomshield.earlywarning.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ransomshield.earlywarning.domain.ThreatAssessment
import org.json.JSONArray
import org.json.JSONObject

class EncryptedThreatLogStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "threat_log.enc",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun append(assessment: ThreatAssessment) {
        val existing = prefs.getString("events", "[]") ?: "[]"
        val arr = JSONArray(existing)
        val obj = JSONObject()
            .put("ts", System.currentTimeMillis())
            .put("score", assessment.score)
            .put("risk", assessment.riskLevel.name)
            .put("reasons", JSONArray(assessment.reasons))
        arr.put(obj)
        if (arr.length() > 200) {
            val trimmed = JSONArray()
            for (i in arr.length() - 200 until arr.length()) trimmed.put(arr.get(i))
            prefs.edit().putString("events", trimmed.toString()).apply()
        } else {
            prefs.edit().putString("events", arr.toString()).apply()
        }
    }
}
