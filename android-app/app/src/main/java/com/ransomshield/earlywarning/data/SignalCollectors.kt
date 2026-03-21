package com.ransomshield.earlywarning.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.provider.Settings
import android.os.Process
import android.os.SystemClock
import com.ransomshield.earlywarning.domain.TelemetrySample
import kotlin.math.absoluteValue
import kotlin.random.Random

class SignalCollectors(private val context: Context) {
    private var lastCpuTick = SystemClock.elapsedRealtime()
    private var lastForegroundMs: Long = 0L

    fun collect(simulationMode: Boolean): TelemetrySample {
        val cpu = collectCpuPercent(simulationMode)
        val memory = collectMemoryPercent(simulationMode)
        val io = collectIoSignal(simulationMode)
        val fileBurst = collectFileMutationSignal(simulationMode)
        val accessibility = collectAccessibilityAbuseSignal(simulationMode)
        val permission = collectPermissionMisuseSignal(simulationMode)

        return TelemetrySample(
            timestampMs = System.currentTimeMillis(),
            cpuUsage = cpu,
            memoryUsage = memory,
            ioOpsPerSec = io,
            fileMutationBurst = fileBurst,
            accessibilityAbuseSignal = accessibility,
            suspiciousPermissionScore = permission
        )
    }

    private fun collectCpuPercent(simulationMode: Boolean): Float {
        val now = SystemClock.elapsedRealtime()
        val delta = (now - lastCpuTick).coerceAtLeast(1)
        lastCpuTick = now
        val pseudoCpu = (Process.getElapsedCpuTime() % 10_000L).toFloat() / 100f
        val base = (pseudoCpu + (delta % 17)).coerceIn(5f, 65f)
        return if (simulationMode) (base + 30f + Random.nextFloat() * 20f).coerceAtMost(100f) else base
    }

    private fun collectMemoryPercent(simulationMode: Boolean): Float {
        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        val ratio = (used.toFloat() / rt.maxMemory().toFloat()) * 100f
        return if (simulationMode) (ratio + 20f).coerceAtMost(100f) else ratio.coerceIn(0f, 100f)
    }

    private fun collectIoSignal(simulationMode: Boolean): Float {
        val usageStats = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        val now = System.currentTimeMillis()
        val usage = usageStats?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 60_000L,
            now
        ).orEmpty()
        val total = usage.sumOf { it.totalTimeInForeground }
        val burstDelta = (total - lastForegroundMs).coerceAtLeast(0L)
        lastForegroundMs = total
        val base = (((total % 10_000L) / 100f) + (burstDelta / 2000f)).coerceIn(8f, 75f)
        return if (simulationMode) (base + 25f + Random.nextFloat() * 30f).coerceAtMost(100f) else base
    }

    private fun collectFileMutationSignal(simulationMode: Boolean): Float {
        val entropy = (SystemClock.elapsedRealtime() % 1000).toFloat() / 10f
        val base = (entropy + Random.nextFloat() * 8f).coerceIn(5f, 55f)
        return if (simulationMode) (base + 35f + Random.nextFloat() * 25f).coerceAtMost(100f) else base
    }

    private fun collectAccessibilityAbuseSignal(simulationMode: Boolean): Float {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val serviceCount = enabled.split(':').filter { it.isNotBlank() }.size
        val suspicious = enabled.contains("overlay", true) || enabled.contains("autoclick", true)
        val base = (serviceCount * 12f + if (suspicious) 25f else 5f).coerceIn(5f, 80f)
        return if (simulationMode) (base + 30f + Random.nextFloat() * 25f).coerceAtMost(100f) else base
    }

    private fun collectPermissionMisuseSignal(simulationMode: Boolean): Float {
        val signal = context.packageName.hashCode().absoluteValue % 35
        val base = (signal + 12).toFloat()
        return if (simulationMode) (base + 20f + Random.nextFloat() * 25f).coerceAtMost(100f) else base
    }
}
