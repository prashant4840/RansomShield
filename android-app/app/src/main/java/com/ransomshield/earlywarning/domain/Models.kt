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
    val reasons: List<String>
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
