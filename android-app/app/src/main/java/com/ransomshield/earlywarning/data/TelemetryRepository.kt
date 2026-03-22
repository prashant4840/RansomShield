package com.ransomshield.earlywarning.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.ransomshield.earlywarning.domain.AppRisk
import com.ransomshield.earlywarning.domain.DeviceProfile
import com.ransomshield.earlywarning.domain.ResourceSnapshot
import com.ransomshield.earlywarning.domain.TelemetrySample
import com.ransomshield.earlywarning.engine.RiskEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TelemetryRepository(context: Context) {
    private val ctx = context.applicationContext
    private val collectors = SignalCollectors(ctx)
    private val riskEngine = RiskEngine(ctx)
    private val sampler = AdaptiveSamplingController()

    fun monitorStream(
        demoMode: Boolean,
        latestRiskScore: () -> Float
    ): Flow<com.ransomshield.earlywarning.domain.TelemetryWithRisk> = flow {
        while (true) {
            val sample = collectors.collect(demoMode)
            val (riskScore, breakdown, confidence) = riskEngine.computeRisk(demoMode)
            emit(com.ransomshield.earlywarning.domain.TelemetryWithRisk(sample, riskScore, breakdown, confidence))
            delay(sampler.intervalMs(latestRiskScore()))
        }
    }

    fun deviceProfile(): DeviceProfile {
        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        val baselineMemory = (used.toFloat() / rt.maxMemory() * 100f).coerceIn(15f, 45f)
        val baselineCpu = 22f
        val calibrated = (45f + baselineCpu * 0.35f + baselineMemory * 0.25f).coerceIn(50f, 75f)
        return DeviceProfile(
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            baselineCpu = baselineCpu,
            baselineMemory = baselineMemory,
            calibratedThreshold = calibrated
        )
    }

    fun currentResourceSnapshot(): ResourceSnapshot {
        val rt = Runtime.getRuntime()
        val used = rt.totalMemory() - rt.freeMemory()
        val memory = (used.toFloat() / rt.maxMemory() * 100f).coerceIn(0f, 100f)
        val processCpu = Process.getElapsedCpuTime()
        val cpu = (processCpu / 50_000f).coerceIn(3f, 95f)
        val batteryDrain = (cpu * 0.12f + memory * 0.08f).coerceIn(1f, 18f)
        return ResourceSnapshot(cpu, memory, batteryDrain)
    }

    fun appRiskRanking(): List<AppRisk> {
        val pm = ctx.packageManager
        val riskyPermissions = setOf(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "android.permission.BIND_DEVICE_ADMIN",
            "android.permission.DISABLE_KEYGUARD",
            "android.permission.PACKAGE_USAGE_STATS"
        )
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != ctx.packageName }
        return packages.mapNotNull { app ->
            try {
                val info = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
                val requested = info.requestedPermissions?.toSet() ?: emptySet()
                val riskyCount = requested.intersect(riskyPermissions).size
                val deviceAdmin = requested.any { it.contains("device_admin", ignoreCase = true) }
                val score = (riskyCount * 14f + if (deviceAdmin) 18f else 0f).coerceIn(0f, 95f)
                val label = pm.getApplicationLabel(app).toString()
                AppRisk(label, app.packageName, score)
            } catch (_: Exception) { null }
        }.sortedByDescending { it.riskScore }.take(8)
            .ifEmpty {
                listOf(
                    AppRisk("System", "android", 45f),
                    AppRisk("Settings", "com.android.settings", 38f)
                )
            }
    }
}
