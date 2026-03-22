package com.ransomshield.earlywarning.engine

import android.content.Context
import com.ransomshield.earlywarning.domain.RiskSignalBreakdown
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.min
import kotlin.math.sin

/**
 * Simulates ransomware-like behaviour for demo mode.
 * Generates: rapid file access, CPU spike, permission abuse trigger.
 * All deterministic (no random) for reproducible demos.
 */
class AttackSimulator(private val context: Context) {
    private var phase = 0f

    fun simulateSignals(baseBreakdown: RiskSignalBreakdown): RiskSignalBreakdown {
        phase += 0.15f
        val t = (phase % 6.28f) // cycle
        val spike = (sin(t) * 0.5f + 0.5f) * 40f

        return RiskSignalBreakdown(
            cpu = (baseBreakdown.cpu + spike).coerceIn(0f, 100f),
            memory = (baseBreakdown.memory + spike * 0.6f).coerceIn(0f, 100f),
            processCount = (baseBreakdown.processCount + 15f).coerceIn(0f, 100f),
            permissionAbuse = (baseBreakdown.permissionAbuse + 35f).coerceIn(0f, 100f),
            fileActivity = (baseBreakdown.fileActivity + spike * 1.2f).coerceIn(0f, 100f),
            networkSpike = (baseBreakdown.networkSpike + spike * 0.5f).coerceIn(0f, 100f)
        )
    }

    suspend fun triggerFileAccessSimulation() {
        repeat(20) {
            context.cacheDir.listFiles()
            context.filesDir.listFiles()
            context.externalCacheDir?.listFiles()
            delay(50)
        }
    }
}
