package com.ransomshield.earlywarning.data

import android.content.Context
import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.RiskSignalBreakdown
import com.ransomshield.earlywarning.domain.ThreatLogEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stores risk history, alerts, signal snapshots.
 * Exports JSON/CSV for reports.
 */
class ThreatLogRepository(context: Context) {
    private val file = File(context.filesDir, "threat_log.json")
    private val maxEntries = 500

    private val entries = mutableListOf<ThreatLogEntry>()

    fun append(
        riskScore: Float,
        confidence: Float,
        riskLevel: RiskLevel,
        reasons: List<String>,
        breakdown: RiskSignalBreakdown,
        isAlert: Boolean
    ) {
        val entry = ThreatLogEntry(
            timestampMs = System.currentTimeMillis(),
            riskScore = riskScore,
            confidence = confidence,
            riskLevel = riskLevel,
            reasons = reasons,
            signalSnapshot = breakdown,
            isAlert = isAlert
        )
        synchronized(entries) {
            entries.add(entry)
            if (entries.size > maxEntries) entries.removeAt(0)
        }
        persist()
    }

    private fun persist() {
        val arr = JSONArray()
        synchronized(entries) {
            entries.takeLast(200).forEach { e ->
                arr.put(JSONObject().apply {
                    put("ts", e.timestampMs)
                    put("score", e.riskScore.toDouble())
                    put("confidence", e.confidence.toDouble())
                    put("level", e.riskLevel.name)
                    put("reasons", JSONArray(e.reasons))
                    put("alert", e.isAlert)
                    put("cpu", e.signalSnapshot.cpu.toDouble())
                    put("mem", e.signalSnapshot.memory.toDouble())
                    put("file", e.signalSnapshot.fileActivity.toDouble())
                    put("perm", e.signalSnapshot.permissionAbuse.toDouble())
                    put("net", e.signalSnapshot.networkSpike.toDouble())
                })
            }
        }
        val obj = JSONObject().put("entries", arr).put("schema", 1)
        file.writeText(obj.toString(2))
    }

    fun exportJson(): String {
        val arr = JSONArray()
        synchronized(entries) {
            entries.forEach { e ->
                arr.put(JSONObject().apply {
                    put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(e.timestampMs)))
                    put("riskScore", e.riskScore.toDouble())
                    put("confidence", e.confidence.toDouble())
                    put("riskLevel", e.riskLevel.name)
                    put("reasons", JSONArray(e.reasons))
                    put("isAlert", e.isAlert)
                    put("signals", JSONObject().apply {
                        put("cpu", e.signalSnapshot.cpu.toDouble())
                        put("memory", e.signalSnapshot.memory.toDouble())
                        put("fileActivity", e.signalSnapshot.fileActivity.toDouble())
                        put("permissionAbuse", e.signalSnapshot.permissionAbuse.toDouble())
                        put("networkSpike", e.signalSnapshot.networkSpike.toDouble())
                    })
                })
            }
        }
        return JSONObject()
            .put("report", "RansomShield SOS")
            .put("exported", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))
            .put("entries", arr)
            .toString(2)
    }

    fun exportCsv(): String {
        val header = "timestamp,riskScore,confidence,riskLevel,isAlert,cpu,memory,fileActivity,permissionAbuse,networkSpike,reasons"
        val rows = synchronized(entries) {
            entries.map { e ->
                val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(e.timestampMs))
                """$ts,${e.riskScore},${e.confidence},${e.riskLevel},${e.isAlert},${e.signalSnapshot.cpu},${e.signalSnapshot.memory},${e.signalSnapshot.fileActivity},${e.signalSnapshot.permissionAbuse},${e.signalSnapshot.networkSpike},"${e.reasons.joinToString(";").replace("\"", "'")}""""
            }
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun getRecentAlerts(limit: Int = 20): List<ThreatLogEntry> =
        synchronized(entries) { entries.filter { it.isAlert }.takeLast(limit).reversed() }

    fun getRiskHistory(limit: Int = 50): List<Float> =
        synchronized(entries) { entries.takeLast(limit).map { it.riskScore } }
}
