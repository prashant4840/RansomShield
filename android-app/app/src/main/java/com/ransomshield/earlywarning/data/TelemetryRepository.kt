package com.ransomshield.earlywarning.data

import android.content.Context
import android.os.Build
import com.ransomshield.earlywarning.domain.AppRisk
import com.ransomshield.earlywarning.domain.DeviceProfile
import com.ransomshield.earlywarning.domain.ResourceSnapshot
import com.ransomshield.earlywarning.domain.TelemetrySample
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TelemetryRepository(context: Context) {
    private val collectors = SignalCollectors(context.applicationContext)
    private val sampler = AdaptiveSamplingController()

    fun monitorStream(
        simulationMode: Boolean,
        latestRiskScore: () -> Float
    ): Flow<TelemetrySample> = flow {
        while (true) {
            emit(collectors.collect(simulationMode))
            delay(sampler.intervalMs(latestRiskScore()))
        }
    }

    fun deviceProfile(): DeviceProfile {
        return DeviceProfile(
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            baselineCpu = 22f,
            baselineMemory = 28f,
            calibratedThreshold = 58f
        )
    }

    fun currentResourceSnapshot(): ResourceSnapshot {
        val memory = Runtime.getRuntime().let {
            val used = it.totalMemory() - it.freeMemory()
            (used.toFloat() / it.maxMemory().toFloat()) * 100f
        }.coerceIn(0f, 100f)
        val cpu = ((System.nanoTime() % 1_500_000_000L) / 15_000_000f).coerceIn(3f, 80f)
        val batteryDrainEstimate = ((cpu * 0.12f) + (memory * 0.08f)).coerceIn(1f, 18f)
        return ResourceSnapshot(cpu, memory, batteryDrainEstimate)
    }

    fun appRiskRanking(): List<AppRisk> = listOf(
        AppRisk("Vault Notes", "com.example.vaultnotes", 82f),
        AppRisk("Quick PDF", "com.example.quickpdf", 64f),
        AppRisk("System Cleaner+", "com.example.cleaner", 59f),
        AppRisk("Budget Tracker", "com.example.budget", 24f)
    )
}
