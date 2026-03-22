package com.ransomshield.earlywarning.engine

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import com.ransomshield.earlywarning.domain.RiskSignalBreakdown

/**
 * Real-time risk scoring using device signals.
 * NO random values. Supports demo mode via AttackSimulator.
 */
class RiskEngine(private val context: Context) {
    private val movingAverage = ArrayDeque<Float>(8)
    private var lastCpuTime = Process.getElapsedCpuTime()
    private var lastCpuWall = SystemClock.elapsedRealtime()
    private var lastRxBytes = TrafficStats.getTotalRxBytes().takeIf { it >= 0 } ?: 0L
    private var lastTxBytes = TrafficStats.getTotalTxBytes().takeIf { it >= 0 } ?: 0L
    private var lastNetworkTime = SystemClock.elapsedRealtime()
    private val attackSimulator = AttackSimulator(context)
    private val thresholdTuner = ThresholdTuner()

    fun computeRisk(demoMode: Boolean): Triple<Float, RiskSignalBreakdown, Float> {
        var breakdown = RiskSignalBreakdown(
            cpu = normalizeCpu() * 100f,
            memory = normalizeMemory() * 100f,
            processCount = normalizeProcessCount() * 100f,
            permissionAbuse = normalizePermissionAbuse() * 100f,
            fileActivity = normalizeFileActivity() * 100f,
            networkSpike = normalizeNetworkSpike() * 100f
        )

        if (demoMode) {
            breakdown = attackSimulator.simulateSignals(breakdown)
        }

        val rawRisk = (
            breakdown.cpu * 0.2f +
            breakdown.memory * 0.2f +
            breakdown.processCount * 0.15f +
            breakdown.permissionAbuse * 0.15f +
            breakdown.fileActivity * 0.15f +
            breakdown.networkSpike * 0.15f
        ) / 100f

        movingAverage.addLast(rawRisk.coerceIn(0f, 1f))
        if (movingAverage.size > 8) movingAverage.removeFirst()
        val smoothed = movingAverage.average().toFloat()
        val rawScore = (smoothed * 100f).coerceIn(0f, 100f)

        val isIdle = breakdown.cpu < 30f && breakdown.fileActivity < 40f
        val (adjustedScore, confidence) = thresholdTuner.computeAdjustedScore(rawScore, breakdown, isIdle)

        return Triple(adjustedScore, breakdown, confidence)
    }

    private fun normalizeCpu(): Float {
        val now = SystemClock.elapsedRealtime()
        val delta = (now - lastCpuWall).coerceAtLeast(100)
        lastCpuWall = now
        val cpuTime = Process.getElapsedCpuTime()
        val cpuDelta = (cpuTime - lastCpuTime).coerceAtLeast(0)
        lastCpuTime = cpuTime
        return ((cpuDelta / 10_000f) / delta / 100f).coerceIn(0f, 1f)
    }

    private fun normalizeMemory(): Float {
        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        return (used.toFloat() / rt.maxMemory()).coerceIn(0f, 1f)
    }

    private fun normalizeProcessCount(): Float {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 0.1f
        return ((am.runningAppProcesses?.size ?: 0) / 150f).coerceIn(0f, 1f)
    }

    private fun normalizePermissionAbuse(): Float {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val serviceCount = enabled.split(':').filter { it.isNotBlank() }.size
        val suspicious = listOf("overlay", "autoclick", "keyguard", "lockscreen", "inject")
            .any { enabled.contains(it, ignoreCase = true) }
        return (serviceCount * 0.08f + if (suspicious) 0.5f else 0.05f).coerceIn(0f, 1f)
    }

    private fun normalizeFileActivity(): Float {
        val dirs = listOfNotNull(context.cacheDir, context.filesDir, context.externalCacheDir)
        val fileCount = dirs.sumOf { it.listFiles()?.size ?: 0 }
        return (fileCount / 200f).coerceIn(0f, 1f)
    }

    private fun normalizeNetworkSpike(): Float {
        val now = SystemClock.elapsedRealtime()
        val delta = (now - lastNetworkTime).coerceAtLeast(500)
        lastNetworkTime = now
        val rx = TrafficStats.getTotalRxBytes().takeIf { it >= 0 } ?: 0L
        val tx = TrafficStats.getTotalTxBytes().takeIf { it >= 0 } ?: 0L
        val rxDelta = (rx - lastRxBytes).coerceAtLeast(0)
        val txDelta = (tx - lastTxBytes).coerceAtLeast(0)
        lastRxBytes = rx
        lastTxBytes = tx
        val bytesPerSec = (rxDelta + txDelta).toFloat() / (delta / 1000f).coerceAtLeast(0.1f)
        return (bytesPerSec / 5_000_000f).coerceIn(0f, 1f)
    }
}
