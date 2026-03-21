package com.ransomshield.earlywarning.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ThreatLineChart(data: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        if (data.size < 2) return@Canvas
        val stepX = size.width / (data.size - 1)
        val path = Path()
        data.forEachIndexed { idx, value ->
            val y = size.height - (value / 100f * size.height)
            val x = idx * stepX
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(Color(0xFF56F2FF), radius = 4f, center = Offset(x, y))
        }
        drawPath(
            path = path,
            color = Color(0xFF1DE9B6),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
    }
}
