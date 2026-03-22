package com.ransomshield.earlywarning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ransomshield.earlywarning.assistant.ThreatAssistant
import com.ransomshield.earlywarning.data.CloudSyncManager
import com.ransomshield.earlywarning.data.EncryptedThreatLogStore
import com.ransomshield.earlywarning.data.PerformanceBenchmarkStore
import com.ransomshield.earlywarning.data.TelemetryExporter
import com.ransomshield.earlywarning.data.TelemetryRepository
import com.ransomshield.earlywarning.data.ThreatLogRepository
import com.ransomshield.earlywarning.domain.AppRisk
import com.ransomshield.earlywarning.domain.DeviceProfile
import com.ransomshield.earlywarning.domain.PerformanceMetrics
import com.ransomshield.earlywarning.domain.ResourceSnapshot
import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.domain.RiskSignalBreakdown
import com.ransomshield.earlywarning.domain.TelemetrySample
import com.ransomshield.earlywarning.domain.ThreatAssessment
import com.ransomshield.earlywarning.domain.ThreatEvent
import com.ransomshield.earlywarning.engine.SmartResponseEngine
import com.ransomshield.earlywarning.ml.ThreatInferenceEngine
import com.ransomshield.earlywarning.monitor.PerformanceMonitor
import com.ransomshield.earlywarning.security.AntiTamperGuard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val monitoringEnabled: Boolean = false,
    val demoMode: Boolean = false,
    val latestSample: TelemetrySample? = null,
    val assessment: ThreatAssessment? = null,
    val signalBreakdown: RiskSignalBreakdown? = null,
    val timeline: List<Float> = emptyList(),
    val cpuGraph: List<Float> = emptyList(),
    val fileActivityGraph: List<Float> = emptyList(),
    val eventLog: List<ThreatEvent> = emptyList(),
    val appRanking: List<AppRisk> = emptyList(),
    val suggestedKillTarget: AppRisk? = null,
    val assistantText: String = "Enable monitoring for real-time threat detection.",
    val securityScore: Int = 100,
    val resourceSnapshot: ResourceSnapshot = ResourceSnapshot(0f, 0f, 0f),
    val performanceMetrics: PerformanceMetrics? = null,
    val deviceProfile: DeviceProfile? = null,
    val tamperRisk: Boolean = false,
    val exportableSampleCount: Int = 0,
    val isLoading: Boolean = false
)

class RansomShieldViewModel(
    private val telemetryRepository: TelemetryRepository,
    private val engine: ThreatInferenceEngine,
    private val cloudSyncManager: CloudSyncManager = CloudSyncManager(),
    private val logStore: EncryptedThreatLogStore,
    private val threatLogRepo: ThreatLogRepository,
    private val perfStore: PerformanceBenchmarkStore,
    private val antiTamperGuard: AntiTamperGuard
) : ViewModel() {
    private val assistant = ThreatAssistant()
    private val perfMonitor = PerformanceMonitor()
    private val smartResponse = SmartResponseEngine()

    private val profile = telemetryRepository.deviceProfile()
    private val _state = MutableStateFlow(
        DashboardState(
            appRanking = telemetryRepository.appRiskRanking(),
            deviceProfile = profile,
            tamperRisk = antiTamperGuard.isCompromised()
        )
    )
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    private var monitorJob: Job? = null
    private var perfJob: Job? = null
    private val telemetryBuffer = ArrayDeque<TelemetrySample>(TelemetryExporter.MAX_EXPORT_SAMPLES)

    fun toggleMonitoring(enabled: Boolean) {
        _state.value = _state.value.copy(
            monitoringEnabled = enabled,
            isLoading = enabled,
            assistantText = if (enabled) "Monitoring active. Analysing device behaviour..." else "Enable monitoring for real-time threat detection."
        )
        if (enabled) {
            startPipeline()
            startPerformanceMonitoring()
        } else {
            monitorJob?.cancel()
            perfJob?.cancel()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        _state.value = _state.value.copy(demoMode = enabled)
        if (_state.value.monitoringEnabled) startPipeline()
    }

    private fun startPerformanceMonitoring() {
        perfJob?.cancel()
        perfJob = viewModelScope.launch {
            while (true) {
                delay(5000)
                val metrics = perfMonitor.capture()
                perfStore.append(metrics)
                _state.value = _state.value.copy(
                    performanceMetrics = perfStore.getLatestSummary()
                )
            }
        }
    }

    fun exportTelemetryJson(asMalicious: Boolean): String {
        val label = if (asMalicious) 1 else 0
        return TelemetryExporter.toJson(telemetryBuffer.toList(), label)
    }

    fun exportTelemetryCsv(asMalicious: Boolean): String {
        val label = if (asMalicious) 1 else 0
        return TelemetryExporter.toCsv(telemetryBuffer.toList(), label)
    }

    fun getBufferedSampleCount(): Int = telemetryBuffer.size

    fun exportEventLogJson(): String = threatLogRepo.exportJson()

    fun exportEventLogCsv(): String = threatLogRepo.exportCsv()

    private fun startPipeline() {
        monitorJob?.cancel()
        monitorJob = viewModelScope.launch {
            telemetryRepository.monitorStream(
                demoMode = _state.value.demoMode,
                latestRiskScore = { _state.value.assessment?.score ?: 0f }
            ).collect { data ->
                _state.value = _state.value.copy(isLoading = false)
                if (telemetryBuffer.size >= TelemetryExporter.MAX_EXPORT_SAMPLES) telemetryBuffer.removeFirst()
                telemetryBuffer.addLast(data.sample)

                val assessment = engine.computeHybridScore(
                    data.realRiskScore,
                    data.sample,
                    data.confidence
                )
                val guidance = assistant.guidance(assessment, data.breakdown)
                cloudSyncManager.syncThreatEvent(assessment)
                logStore.append(assessment)

                threatLogRepo.append(
                    riskScore = assessment.score,
                    confidence = assessment.confidence,
                    riskLevel = assessment.riskLevel,
                    reasons = assessment.reasons,
                    breakdown = data.breakdown,
                    isAlert = assessment.riskLevel == RiskLevel.HIGH || assessment.score > 70f
                )

                val newTimeline = (_state.value.timeline + assessment.score).takeLast(50)
                val newCpuGraph = (_state.value.cpuGraph + data.breakdown.cpu).takeLast(30)
                val newFileGraph = (_state.value.fileActivityGraph + data.breakdown.fileActivity).takeLast(30)
                val securityScore = (100 - assessment.score.toInt()).coerceIn(0, 100)
                val event = ThreatEvent(
                    timestampMs = data.sample.timestampMs,
                    riskScore = assessment.score,
                    riskLevel = assessment.riskLevel,
                    summary = assessment.reasons.firstOrNull() ?: "Monitor"
                )
                val newEvents = (_state.value.eventLog + event).takeLast(25)
                val rankedApps = smartResponse.rankByRiskContribution(_state.value.appRanking, assessment.score)
                val suggestedKill = smartResponse.getSuggestedKillTarget(rankedApps, assessment.score)

                _state.value = _state.value.copy(
                    latestSample = data.sample,
                    assessment = assessment,
                    signalBreakdown = data.breakdown,
                    timeline = newTimeline,
                    cpuGraph = newCpuGraph,
                    fileActivityGraph = newFileGraph,
                    eventLog = newEvents,
                    assistantText = guidance,
                    securityScore = securityScore,
                    appRanking = rankedApps,
                    suggestedKillTarget = suggestedKill,
                    resourceSnapshot = telemetryRepository.currentResourceSnapshot(),
                    exportableSampleCount = telemetryBuffer.size
                )
            }
        }
    }
}
