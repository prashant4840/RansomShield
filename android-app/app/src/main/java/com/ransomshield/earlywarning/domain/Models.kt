package com.ransomshield.earlywarning.domain

enum class RiskLevel { LOW, MEDIUM, HIGH }

data class TelemetrySample(
    val timestampMs: Long,
    val cpuUsage: Float,
    val memoryUsage: Float,
    val ioOpsPerSec: Float,
    val fileMutationBurst: Float,
    val accessibilityAbuseSignal: Float,
    val suspiciousPermissionScore: Float
)

data class ThreatAssessment(
    val score: Float,
    val riskLevel: RiskLevel,
    val reasons: List<String>,
    val confidence: Float = 0.8f
)

data class AppRisk(
    val appName: String,
    val packageName: String,
    val riskScore: Float
)

data class ResourceSnapshot(
    val appCpuPercent: Float,
    val appMemoryPercent: Float,
    val estimatedBatteryDrainMahPerHour: Float
)

data class DeviceProfile(
    val model: String,
    val baselineCpu: Float,
    val baselineMemory: Float,
    val calibratedThreshold: Float
)

data class RiskSignalBreakdown(
    val cpu: Float,
    val memory: Float,
    val processCount: Float,
    val permissionAbuse: Float,
    val fileActivity: Float,
    val networkSpike: Float
)

data class ThreatEvent(
    val timestampMs: Long,
    val riskScore: Float,
    val riskLevel: RiskLevel,
    val summary: String
)

data class TelemetryWithRisk(
    val sample: TelemetrySample,
    val realRiskScore: Float,
    val breakdown: RiskSignalBreakdown,
    val confidence: Float = 0.8f
)

/** App performance metrics for System Impact Panel */
data class PerformanceMetrics(
    val cpuOverheadPercent: Float,
    val memoryFootprintMB: Float,
    val batteryDrainPercentPerHour: Float,
    val timestampMs: Long = System.currentTimeMillis()
)

/** Full threat log entry for export */
data class ThreatLogEntry(
    val timestampMs: Long,
    val riskScore: Float,
    val confidence: Float,
    val riskLevel: RiskLevel,
    val reasons: List<String>,
    val signalSnapshot: RiskSignalBreakdown,
    val isAlert: Boolean
)
