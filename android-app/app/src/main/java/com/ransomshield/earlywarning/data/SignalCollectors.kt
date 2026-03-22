package com.ransomshield.earlywarning.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import com.ransomshield.earlywarning.domain.TelemetrySample

/**
 * Collects real device signals. NO random values.
 * Demo mode amplifies real signals (additive) to simulate threat behaviour.
 */
class SignalCollectors(private val context: Context) {
    private var lastCpuTick = SystemClock.elapsedRealtime()
    private var lastForegroundMs: Long = 0L
    private val cpuHistory = ArrayDeque<Float>(5)
    private val ioHistory = ArrayDeque<Float>(5)
    private val permissionCache = mutableMapOf<String, Float>()

    fun collect(demoMode: Boolean): TelemetrySample {
        val cpu = collectCpuPercent(demoMode)
        val memory = collectMemoryPercent()
        val io = collectIoSignal(demoMode)
        val fileBurst = collectFileMutationSignal()
        val accessibility = collectAccessibilityAbuseSignal()
        val permission = collectPermissionMisuseSignal()

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

    private fun collectCpuPercent(demoMode: Boolean): Float {
        val now = SystemClock.elapsedRealtime()
        val delta = (now - lastCpuTick).coerceAtLeast(1)
        lastCpuTick = now
        val processCpu = Process.getElapsedCpuTime()
        val cpuPercent = (processCpu / 10_000f) / delta
        val base = cpuPercent.coerceIn(2f, 95f)
        cpuHistory.addLast(base)
        if (cpuHistory.size > 5) cpuHistory.removeFirst()
        val smoothed = cpuHistory.average().toFloat()
        return if (demoMode) (smoothed + 35f).coerceIn(0f, 100f) else smoothed
    }

    private fun collectMemoryPercent(): Float {
        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        return (used.toFloat() / rt.maxMemory() * 100f).coerceIn(0f, 100f)
    }

    private fun collectIoSignal(demoMode: Boolean): Float {
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
        val activityRate = (burstDelta / 500f).coerceIn(0f, 100f)
        val base = (((total % 12_000L) / 120f) + activityRate * 0.3f).coerceIn(5f, 85f)
        ioHistory.addLast(base)
        if (ioHistory.size > 5) ioHistory.removeFirst()
        val smoothed = ioHistory.average().toFloat()
        return if (demoMode) (smoothed + 30f).coerceIn(0f, 100f) else smoothed
    }

    private fun collectFileMutationSignal(): Float {
        val dirs = listOfNotNull(context.cacheDir, context.filesDir, context.externalCacheDir)
        val fileCount = dirs.sumOf { it.listFiles()?.size ?: 0 }
        val entropy = (fileCount * 2 + (SystemClock.elapsedRealtime() % 300) / 10).toFloat()
        return entropy.coerceIn(3f, 85f)
    }

    private fun collectAccessibilityAbuseSignal(): Float {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val serviceCount = enabled.split(':').filter { it.isNotBlank() }.size
        val suspicious = listOf("overlay", "autoclick", "keyguard", "lockscreen", "inject")
            .any { enabled.contains(it, ignoreCase = true) }
        return (serviceCount * 10f + if (suspicious) 35f else 3f).coerceIn(5f, 95f)
    }

    private fun collectPermissionMisuseSignal(): Float {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val riskyPermissions = setOf(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "android.permission.BIND_DEVICE_ADMIN",
            "android.permission.DISABLE_KEYGUARD",
            "android.permission.PACKAGE_USAGE_STATS"
        )
        var maxRisk = 15f

        for (app in packages.take(50)) {
            val pkg = app.packageName

            val cached = permissionCache[pkg]
            if (cached != null) {
                maxRisk = maxOf(maxRisk, cached)
                continue
            }

            val score = try {
                val info = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
                val requested = info.requestedPermissions?.toSet() ?: emptySet()
                val riskyCount = requested.intersect(riskyPermissions).size

                (riskyCount * 12f +
                        if (requested.any { it.contains("device_admin", ignoreCase = true) }) 20f else 0f
                        ).coerceIn(0f, 90f)

            } catch (_: Exception) { 15f }

            permissionCache[pkg] = score

            if (permissionCache.size > 100) permissionCache.clear()

            maxRisk = maxOf(maxRisk, score)
        }
        return maxRisk.coerceIn(10f, 95f)
    }
}
