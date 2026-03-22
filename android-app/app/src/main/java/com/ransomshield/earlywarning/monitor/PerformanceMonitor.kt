package com.ransomshield.earlywarning.monitor

import android.os.Process
import android.os.SystemClock
import com.ransomshield.earlywarning.domain.PerformanceMetrics

/**
 * Lightweight background performance monitoring.
 * Tracks app CPU, memory footprint, and battery drain estimate.
 */
class PerformanceMonitor {
    private var lastCpuTime = Process.getElapsedCpuTime()
    private var lastWallTime = SystemClock.elapsedRealtime()
    private val cpuSamples = ArrayDeque<Float>(10)

    fun capture(): PerformanceMetrics {
        val now = SystemClock.elapsedRealtime()
        val wallDelta = (now - lastWallTime).coerceAtLeast(100)
        lastWallTime = now

        val cpuTime = Process.getElapsedCpuTime()
        val cpuDelta = (cpuTime - lastCpuTime).coerceAtLeast(0)
        lastCpuTime = cpuTime
        val cpuPercent = (cpuDelta / 10_000f) / wallDelta
        cpuSamples.addLast(cpuPercent.coerceIn(0f, 100f))
        if (cpuSamples.size > 10) cpuSamples.removeFirst()
        val avgCpu = cpuSamples.average().toFloat()

        val rt = Runtime.getRuntime()
        val usedBytes = rt.totalMemory() - rt.freeMemory()
        val memoryMB = (usedBytes / (1024f * 1024f))

        // Battery: ~0.5%/hr per 1% sustained CPU + memory factor
        val batteryPercentPerHour = (avgCpu * 0.05f + memoryMB * 0.02f).coerceIn(0.1f, 5f)

        return PerformanceMetrics(
            cpuOverheadPercent = avgCpu,
            memoryFootprintMB = memoryMB,
            batteryDrainPercentPerHour = batteryPercentPerHour
        )
    }
}
