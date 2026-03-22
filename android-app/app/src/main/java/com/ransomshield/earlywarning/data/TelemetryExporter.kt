package com.ransomshield.earlywarning.data

import com.ransomshield.earlywarning.domain.TelemetrySample
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports telemetry samples to JSON/CSV for validation and model training.
 * Format is compatible with ml/load_telemetry.py and validate_model.py.
 */
object TelemetryExporter {

    const val MAX_EXPORT_SAMPLES = 5000
    private val FEATURE_NAMES = listOf(
        "timestampMs", "cpuUsage", "memoryUsage", "ioOpsPerSec",
        "fileMutationBurst", "accessibilityAbuseSignal", "suspiciousPermissionScore", "label"
    )

    /**
     * Export samples to JSON array. Each object has all 6 features plus optional label.
     * @param samples Telemetry samples
     * @param label 0=benign, 1=malicious. Set based on capture context (simulation mode = 1)
     */
    fun toJson(samples: List<TelemetrySample>, label: Int = 0): String {
        val arr = JSONArray()
        for (s in samples) {
            arr.put(
                JSONObject().apply {
                    put("timestampMs", s.timestampMs)
                    put("cpuUsage", s.cpuUsage.toDouble())
                    put("memoryUsage", s.memoryUsage.toDouble())
                    put("ioOpsPerSec", s.ioOpsPerSec.toDouble())
                    put("fileMutationBurst", s.fileMutationBurst.toDouble())
                    put("accessibilityAbuseSignal", s.accessibilityAbuseSignal.toDouble())
                    put("suspiciousPermissionScore", s.suspiciousPermissionScore.toDouble())
                    put("label", label)
                }
            )
        }
        val meta = JSONObject().apply {
            put("schema_version", 1)
            put("feature_names", JSONArray(FEATURE_NAMES))
            put("sample_count", samples.size)
            put("label", label)
            put("exported_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))
        }
        return JSONObject().apply {
            put("metadata", meta)
            put("samples", arr)
        }.toString(2)
    }

    /**
     * Export to CSV for easy inspection and pandas loading.
     */
    fun toCsv(samples: List<TelemetrySample>, label: Int = 0): String {
        val header = "timestampMs,cpuUsage,memoryUsage,ioOpsPerSec,fileMutationBurst,accessibilityAbuseSignal,suspiciousPermissionScore,label"
        val rows = samples.map { s ->
            "${s.timestampMs},${s.cpuUsage},${s.memoryUsage},${s.ioOpsPerSec}," +
                "${s.fileMutationBurst},${s.accessibilityAbuseSignal},${s.suspiciousPermissionScore},$label"
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}
