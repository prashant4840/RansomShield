package com.ransomshield.earlywarning.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ransomshield.earlywarning.domain.RiskLevel
import com.ransomshield.earlywarning.ui.DashboardState
import com.ransomshield.earlywarning.ui.components.RiskGauge
import com.ransomshield.earlywarning.ui.components.SignalGraph
import com.ransomshield.earlywarning.ui.components.SocTheme
import com.ransomshield.earlywarning.ui.components.SystemImpactPanel
import com.ransomshield.earlywarning.ui.components.ThreatLineChart

@Composable
fun DashboardScreen(
    state: DashboardState,
    onToggleMonitoring: (Boolean) -> Unit,
    onToggleDemoMode: (Boolean) -> Unit,
    onKillProcess: (String?) -> Unit,
    onLockFolders: () -> Unit,
    onRollback: () -> Unit,
    onSafeMode: () -> Unit,
    onExportTelemetry: (format: String, asMalicious: Boolean) -> Unit,
    onExportLogs: () -> Unit,
    onShareReport: (format: String) -> Unit
) {
    val riskScore = state.assessment?.score ?: 0f
    val riskColor = when {
        riskScore < 30f -> SocTheme.riskLow
        riskScore < 60f -> SocTheme.riskMedium
        else -> SocTheme.riskHigh
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(SocTheme.backgroundDark, SocTheme.backgroundMid, SocTheme.backgroundLight)
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "RansomShield SOS",
                style = MaterialTheme.typography.headlineMedium,
                color = SocTheme.accentCyan,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Endpoint Detection & Response", color = SocTheme.textMuted)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Live Monitoring", color = Color.White)
                    Text(
                        if (state.monitoringEnabled) "Active" else "Inactive",
                        color = if (state.monitoringEnabled) SocTheme.riskLow else SocTheme.textMuted
                    )
                }
                Switch(checked = state.monitoringEnabled, onCheckedChange = onToggleMonitoring)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟢 Live", color = if (!state.demoMode) SocTheme.riskLow else SocTheme.textMuted)
                    Text("  ", color = Color.Transparent)
                    Text("🟡 Demo", color = if (state.demoMode) SocTheme.riskMedium else SocTheme.textMuted)
                }
                Switch(checked = state.demoMode, onCheckedChange = onToggleDemoMode)
            }
        }

        item {
            if (state.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = SocTheme.accentCyan,
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Analysing...", color = SocTheme.textMuted)
                }
            }
        }

        item {
            SocCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RiskGauge(score = riskScore)
                    Text("Risk: ${state.assessment?.riskLevel ?: "—"}", color = riskColor, fontWeight = FontWeight.SemiBold)
                    Text("Confidence: ${((state.assessment?.confidence ?: 0f) * 100).toInt()}%", color = SocTheme.textMuted)
                    Text("Security Score: ${state.securityScore}/100", color = SocTheme.accentCyan)
                }
            }
        }

        item {
            AnimatedVisibility(visible = riskScore > 70f, enter = fadeIn(), exit = fadeOut()) {
                SocCard(backgroundColor = Color(0x44F44336)) {
                    Text("⚠️ Potential ransomware behaviour detected", color = SocTheme.riskHigh, fontWeight = FontWeight.Bold)
                    if (riskScore > 85f) {
                        Spacer(Modifier.height(8.dp))
                        Text("Recommended: Kill suspicious app, enable safe mode, revoke permissions", color = Color.White)
                    }
                }
            }
        }

        item {
            SocCard {
                Text("Live Threat Graph", color = Color.White, fontWeight = FontWeight.SemiBold)
                ThreatLineChart(state.timeline.ifEmpty { listOf(0f, 0f) }, modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            SocCard {
                Text("Signal Graphs", color = SocTheme.accentCyan, fontWeight = FontWeight.SemiBold)
                if (state.cpuGraph.size >= 2) {
                    Text("CPU", color = SocTheme.textMuted)
                    SignalGraph(state.cpuGraph, SocTheme.riskMedium, "CPU")
                }
                if (state.fileActivityGraph.size >= 2) {
                    Spacer(Modifier.height(4.dp))
                    Text("File Activity", color = SocTheme.textMuted)
                    SignalGraph(state.fileActivityGraph, SocTheme.riskHigh, "File")
                }
                if (state.cpuGraph.size < 2 && state.fileActivityGraph.size < 2) {
                    Text("Collecting...", color = SocTheme.textMuted)
                }
            }
        }

        item {
            SocCard {
                Text("Explainable AI", color = SocTheme.accentCyan, fontWeight = FontWeight.SemiBold)
                Text(state.assessment?.reasons?.joinToString(" • ") ?: "No alerts yet", color = SocTheme.textMuted)
                state.signalBreakdown?.let { b ->
                    Spacer(Modifier.height(8.dp))
                    Text("CPU ${b.cpu.toInt()}% | Mem ${b.memory.toInt()}% | Perms ${b.permissionAbuse.toInt()}% | I/O ${b.fileActivity.toInt()}% | Net ${b.networkSpike.toInt()}%", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        item {
            SocCard {
                Text("AI Assistant", color = SocTheme.accentCyan, fontWeight = FontWeight.SemiBold)
                Text(state.assistantText, color = Color.White)
            }
        }

        item {
            SystemImpactPanel(state.performanceMetrics)
        }

        item {
            SocCard {
                Text("System Health", color = Color(0xFFE1BEE7), fontWeight = FontWeight.SemiBold)
                Text("CPU: ${state.resourceSnapshot.appCpuPercent.toInt()}% | RAM: ${state.resourceSnapshot.appMemoryPercent.toInt()}%", color = Color.White)
                Text("Battery: ${"%.1f".format(state.resourceSnapshot.estimatedBatteryDrainMahPerHour)} mAh/h", color = SocTheme.textMuted)
                Text(if (state.tamperRisk) "⚠️ Tamper detected" else "✓ No tamper", color = if (state.tamperRisk) SocTheme.riskHigh else SocTheme.riskLow)
            }
        }

        item {
            SocCard {
                Text("Event Timeline", color = Color.White, fontWeight = FontWeight.SemiBold)
                if (state.eventLog.isEmpty()) Text("No events", color = SocTheme.textMuted)
                else state.eventLog.takeLast(5).reversed().forEach { e ->
                    Text("[${e.riskLevel}] ${e.summary.take(40)} (${e.riskScore.toInt()})",
                        color = when (e.riskLevel) {
                            RiskLevel.HIGH -> SocTheme.riskHigh
                            RiskLevel.MEDIUM -> SocTheme.riskMedium
                            RiskLevel.LOW -> SocTheme.riskLow
                        })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onExportLogs) { Text("Export JSON") }
                    TextButton(onClick = { onShareReport("json") }) { Text("Share Report") }
                }
            }
        }

        item {
            SocCard {
                Text("Preventive Actions", color = Color(0xFFFFF59D), fontWeight = FontWeight.SemiBold)
                if (state.suggestedKillTarget != null) {
                    Text("Suggested: ${state.suggestedKillTarget.appName} (${state.suggestedKillTarget.riskScore.toInt()}%)", color = SocTheme.riskHigh)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { onKillProcess(state.suggestedKillTarget?.packageName ?: state.appRanking.firstOrNull()?.packageName) }) { Text("Kill") }
                    TextButton(onClick = onLockFolders) { Text("Lock") }
                    TextButton(onClick = onRollback) { Text("Rollback") }
                    TextButton(onClick = onSafeMode) { Text("Safe Mode") }
                }
            }
        }

        item {
            SocCard {
                Text("Telemetry & Reports", color = SocTheme.accentBlue, fontWeight = FontWeight.SemiBold)
                Text("Buffer: ${state.exportableSampleCount} samples", color = SocTheme.textMuted)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onExportTelemetry("json", state.demoMode) }) { Text("JSON") }
                    TextButton(onClick = { onExportTelemetry("csv", state.demoMode) }) { Text("CSV") }
                    TextButton(onClick = { onShareReport("csv") }) { Text("Share Report") }
                }
            }
        }

        item { Text("Top Risky Apps", color = Color.White, fontWeight = FontWeight.SemiBold) }
        items(state.appRanking) { app ->
            val isSuggested = app.packageName == state.suggestedKillTarget?.packageName
            SocCard(backgroundColor = if (isSuggested) Color(0x44F44336) else SocTheme.cardBg) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${app.appName}${if (isSuggested) " ⚠" else ""}", color = Color.White)
                    Text("${app.riskScore.toInt()}%", color = SocTheme.riskHigh)
                }
            }
        }
    }
}

@Composable
private fun SocCard(backgroundColor: Color = SocTheme.cardBg, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = backgroundColor), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}
