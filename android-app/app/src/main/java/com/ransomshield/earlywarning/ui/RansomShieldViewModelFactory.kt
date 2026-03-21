package com.ransomshield.earlywarning.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ransomshield.earlywarning.data.EncryptedThreatLogStore
import com.ransomshield.earlywarning.data.TelemetryRepository
import com.ransomshield.earlywarning.ml.TFLiteScorer
import com.ransomshield.earlywarning.ml.ThreatInferenceEngine
import com.ransomshield.earlywarning.security.AntiTamperGuard

class RansomShieldViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RansomShieldViewModel::class.java)) {
            val repo = TelemetryRepository(context.applicationContext)
            val scorer = TFLiteScorer(context.applicationContext)
            val engine = ThreatInferenceEngine(scorer)
            val logs = EncryptedThreatLogStore(context.applicationContext)
            val antiTamperGuard = AntiTamperGuard(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return RansomShieldViewModel(repo, engine, logStore = logs, antiTamperGuard = antiTamperGuard) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
