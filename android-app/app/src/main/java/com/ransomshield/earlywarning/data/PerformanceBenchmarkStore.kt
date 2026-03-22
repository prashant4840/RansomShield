package com.ransomshield.earlywarning.data

import android.content.Context
import com.ransomshield.earlywarning.domain.PerformanceMetrics
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Lightweight file-based metric logging for benchmarking.
 * Stores rolling window of performance snapshots.
 */
class PerformanceBenchmarkStore(context: Context) {
    private val file = File(context.filesDir, "perf_benchmark.json")
    private val maxEntries = 100

    fun append(metrics: PerformanceMetrics) {
        val existing = runCatching { file.readText() }.getOrElse { "{\"samples\":[]}" }
        val obj = JSONObject(existing)
        val arr = obj.optJSONArray("samples") ?: JSONArray()
        arr.put(JSONObject().apply {
            put("ts", metrics.timestampMs)
            put("cpu", metrics.cpuOverheadPercent.toDouble())
            put("mem", metrics.memoryFootprintMB.toDouble())
            put("battery", metrics.batteryDrainPercentPerHour.toDouble())
        })
        while (arr.length() > maxEntries) arr.remove(0)
        obj.put("samples", arr)
        file.writeText(obj.toString(2))
    }

    fun getLatestSummary(): PerformanceMetrics {
        val existing = runCatching { file.readText() }.getOrElse { "{\"samples\":[]}" }
        val arr = JSONObject(existing).optJSONArray("samples") ?: return PerformanceMetrics(0f, 0f, 0f)
        if (arr.length() == 0) return PerformanceMetrics(0f, 0f, 0f)
        var cpuSum = 0.0
        var memSum = 0.0
        var batSum = 0.0
        val count = minOf(arr.length(), 20)
        for (i in arr.length() - count until arr.length()) {
            val o = arr.getJSONObject(i)
            cpuSum += o.optDouble("cpu", 0.0)
            memSum += o.optDouble("mem", 0.0)
            batSum += o.optDouble("battery", 0.0)
        }
        return PerformanceMetrics(
            cpuOverheadPercent = (cpuSum / count).toFloat(),
            memoryFootprintMB = (memSum / count).toFloat(),
            batteryDrainPercentPerHour = (batSum / count).toFloat()
        )
    }
}
