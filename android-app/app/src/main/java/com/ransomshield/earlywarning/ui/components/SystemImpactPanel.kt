package com.ransomshield.earlywarning.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ransomshield.earlywarning.domain.PerformanceMetrics

@Composable
fun SystemImpactPanel(metrics: PerformanceMetrics?, modifier: Modifier = Modifier) {
    if (metrics == null) return
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "System Impact",
                color = SocTheme.accentCyan,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "CPU overhead: ~${"%.1f".format(metrics.cpuOverheadPercent)}%",
                color = Color.White
            )
            Text(
                "Memory: ~${"%.1f".format(metrics.memoryFootprintMB)} MB",
                color = Color.White
            )
            Text(
                "Battery drain: ~${"%.2f".format(metrics.batteryDrainPercentPerHour)}%/hr",
                color = SocTheme.textMuted
            )
        }
    }
}
