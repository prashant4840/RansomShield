package com.ransomshield.earlywarning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ransomshield.earlywarning.ui.DashboardState
import com.ransomshield.earlywarning.ui.components.ThreatLineChart

@Composable
fun DashboardScreen(
    state: DashboardState,
    onToggleMonitoring: (Boolean) -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onKillProcess: () -> Unit,
    onLockFolders: () -> Unit,
    onRollback: () -> Unit,
    onSafeMode: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF050914), Color(0xFF0B1123), Color(0xFF111827))
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "RansomShield SOC",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF64FFDA),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AI-Powered Early Warning for Android",
                color = Color(0xFFB0BEC5)
            )
        }
        item {
            GlassCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Live Monitoring", color = Color.White)
                        Text(if (state.monitoringEnabled) "Active" else "Inactive", color = Color(0xFF90CAF9))
                    }
                    Switch(checked = state.monitoringEnabled, onCheckedChange = onToggleMonitoring)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Threat Simulation Mode", color = Color.White)
                    Switch(checked = state.simulationMode, onCheckedChange = onToggleSimulation)
                }
                Spacer(Modifier.height(8.dp))
                Text("Gamified Security Score: ${state.securityScore}/100", color = Color(0xFF1DE9B6))
                Text("Adaptive Threshold: ${state.calibratedThreshold.toInt()}", color = Color(0xFF80CBC4))
                Text(
                    "Device Profile: ${state.deviceProfile?.model ?: "Unknown"}",
                    color = Color(0xFFB3E5FC)
                )
            }
        }
        item {
            GlassCard {
                Text("Threat Timeline", color = Color.White, fontWeight = FontWeight.SemiBold)
                ThreatLineChart(state.timeline)
            }
        }
        item {
            GlassCard {
                val risk = state.assessment
                Text("Current Risk: ${risk?.riskLevel ?: "UNKNOWN"}", color = Color(0xFFFF8A80))
                Text("Risk Score: ${risk?.score?.toInt() ?: 0}", color = Color.White)
                Text("Explainable Alert:", color = Color(0xFF80D8FF), fontWeight = FontWeight.SemiBold)
                Text(risk?.reasons?.joinToString(" | ") ?: "No alerts yet", color = Color(0xFFE0E0E0))
            }
        }
        item {
            GlassCard {
                Text("AI Assistant", color = Color(0xFF64FFDA), fontWeight = FontWeight.SemiBold)
                Text(state.assistantText, color = Color.White)
            }
        }
        item {
            GlassCard {
                Text("Runtime Footprint", color = Color(0xFFE1BEE7), fontWeight = FontWeight.SemiBold)
                Text("App CPU: ${state.resourceSnapshot.appCpuPercent.toInt()}%", color = Color.White)
                Text("App RAM: ${state.resourceSnapshot.appMemoryPercent.toInt()}%", color = Color.White)
                Text(
                    "Battery Drain Estimate: ${"%.1f".format(state.resourceSnapshot.estimatedBatteryDrainMahPerHour)} mAh/h",
                    color = Color(0xFFB39DDB)
                )
                Text(
                    if (state.tamperRisk) "Tamper Warning: Debug/Root/Emulator signal detected"
                    else "Tamper Status: No high-risk tamper indicators",
                    color = if (state.tamperRisk) Color(0xFFFF8A80) else Color(0xFFA5D6A7)
                )
            }
        }
        item {
            GlassCard {
                Text("One-Tap Preventive Actions", color = Color(0xFFFFF59D), fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onKillProcess) { Text("Kill") }
                    TextButton(onClick = onLockFolders) { Text("Lock") }
                    TextButton(onClick = onRollback) { Text("Rollback") }
                    TextButton(onClick = onSafeMode) { Text("Safe Mode") }
                }
            }
        }
        item {
            Text("App Risk Ranking", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        items(state.appRanking) { app ->
            GlassCard {
                Text("${app.appName} (${app.packageName})", color = Color.White)
                Text("Risk Score: ${app.riskScore.toInt()}", color = Color(0xFFFF8A80))
            }
        }
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x2218FFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}
