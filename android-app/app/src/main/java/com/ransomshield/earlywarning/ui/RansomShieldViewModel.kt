package com.ransomshield.earlywarning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ransomshield.earlywarning.assistant.ThreatAssistant
import com.ransomshield.earlywarning.data.CloudSyncManager
import com.ransomshield.earlywarning.data.EncryptedThreatLogStore
import com.ransomshield.earlywarning.data.TelemetryRepository
import com.ransomshield.earlywarning.domain.AppRisk
import com.ransomshield.earlywarning.domain.DeviceProfile
import com.ransomshield.earlywarning.domain.ResourceSnapshot
import com.ransomshield.earlywarning.domain.TelemetrySample
import com.ransomshield.earlywarning.domain.ThreatAssessment
import com.ransomshield.earlywarning.ml.ThreatInferenceEngine
import com.ransomshield.earlywarning.security.AntiTamperGuard
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val monitoringEnabled: Boolean = false,
    val simulationMode: Boolean = false,
    val latestSample: TelemetrySample? = null,
    val assessment: ThreatAssessment? = null,
    val timeline: List<Float> = emptyList(),
    val appRanking: List<AppRisk> = emptyList(),
    val assistantText: String = "Start monitoring for AI-powered threat guidance.",
    val securityScore: Int = 92,
    val resourceSnapshot: ResourceSnapshot = ResourceSnapshot(0f, 0f, 0f),
    val calibratedThreshold: Float = 58f,
    val deviceProfile: DeviceProfile? = null,
    val tamperRisk: Boolean = false
)

class RansomShieldViewModel(
    private val telemetryRepository: TelemetryRepository,
    private val engine: ThreatInferenceEngine,
    private val cloudSyncManager: CloudSyncManager = CloudSyncManager(),
    private val logStore: EncryptedThreatLogStore,
    private val antiTamperGuard: AntiTamperGuard
) : ViewModel() {
    private val assistant = ThreatAssistant()

    private val profile = telemetryRepository.deviceProfile()
    private val _state = MutableStateFlow(
        DashboardState(
            appRanking = telemetryRepository.appRiskRanking(),
            calibratedThreshold = engine.calibrateThreshold(profile.baselineCpu, profile.baselineMemory),
            deviceProfile = profile,
            tamperRisk = antiTamperGuard.isCompromised()
        )
    )
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    private var monitorJob: Job? = null

    fun toggleMonitoring(enabled: Boolean) {
        _state.value = _state.value.copy(monitoringEnabled = enabled)
        if (enabled) startPipeline() else monitorJob?.cancel()
    }

    fun toggleSimulation(enabled: Boolean) {
        _state.value = _state.value.copy(simulationMode = enabled)
        if (_state.value.monitoringEnabled) startPipeline()
    }

    private fun startPipeline() {
        monitorJob?.cancel()
        monitorJob = viewModelScope.launch {
            telemetryRepository.monitorStream(
                simulationMode = _state.value.simulationMode,
                latestRiskScore = { _state.value.assessment?.score ?: 0f }
            ).collect { sample ->
                val assessment = engine.score(sample)
                cloudSyncManager.syncThreatEvent(assessment)
                logStore.append(assessment)
                val newTimeline = (_state.value.timeline + assessment.score).takeLast(30)
                val score = (100 - assessment.score.toInt()).coerceIn(10, 99)
                _state.value = _state.value.copy(
                    latestSample = sample,
                    assessment = assessment,
                    timeline = newTimeline,
                    assistantText = assistant.guidance(assessment),
                    securityScore = score,
                    resourceSnapshot = telemetryRepository.currentResourceSnapshot()
                )
            }
        }
    }
}
