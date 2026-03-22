package com.ransomshield.earlywarning.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ThreatLineChart(data: List<Float>, modifier: Modifier = Modifier) {
    val maxVal = data.maxOrNull() ?: 100f
    val animatedMax by animateFloatAsState(
        targetValue = maxVal.coerceAtLeast(1f),
        animationSpec = tween(400),
        label = "chart"
    )
    val color = when {
        (data.lastOrNull() ?: 0f) >= 60f -> Color(0xFFF44336)
        (data.lastOrNull() ?: 0f) >= 30f -> Color(0xFFFFC107)
        else -> Color(0xFF4CAF50)
    }

    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        if (data.size < 2) return@Canvas
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        data.forEachIndexed { idx, value ->
            val y = size.height - (value / animatedMax.coerceAtLeast(1f) * size.height * 0.9f) - 8
            val x = idx * stepX
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
        data.forEachIndexed { idx, value ->
            val y = size.height - (value / animatedMax.coerceAtLeast(1f) * size.height * 0.9f) - 8
            val x = idx * stepX
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 3f, center = Offset(x, y))
        }
    }
}
