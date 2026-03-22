package com.ransomshield.earlywarning.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ransomshield.earlywarning.data.EncryptedThreatLogStore
import com.ransomshield.earlywarning.data.PerformanceBenchmarkStore
import com.ransomshield.earlywarning.data.TelemetryRepository
import com.ransomshield.earlywarning.data.ThreatLogRepository
import com.ransomshield.earlywarning.ml.TFLiteScorer
import com.ransomshield.earlywarning.ml.ThreatInferenceEngine
import com.ransomshield.earlywarning.security.AntiTamperGuard

class RansomShieldViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RansomShieldViewModel::class.java)) {
            val ctx = context.applicationContext
            val repo = TelemetryRepository(ctx)
            val scorer = TFLiteScorer(ctx)
            val engine = ThreatInferenceEngine(scorer)
            val logStore = EncryptedThreatLogStore(ctx)
            val threatLogRepo = ThreatLogRepository(ctx)
            val perfStore = PerformanceBenchmarkStore(ctx)
            val antiTamperGuard = AntiTamperGuard(ctx)
            @Suppress("UNCHECKED_CAST")
            return RansomShieldViewModel(
                telemetryRepository = repo,
                engine = engine,
                cloudSyncManager = com.ransomshield.earlywarning.data.CloudSyncManager(),
                logStore = logStore,
                threatLogRepo = threatLogRepo,
                perfStore = perfStore,
                antiTamperGuard = antiTamperGuard
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
