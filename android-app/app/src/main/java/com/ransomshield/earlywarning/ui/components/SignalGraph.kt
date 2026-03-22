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
fun SignalGraph(
    data: List<Float>,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOrNull()?.coerceAtLeast(1f) ?: 100f
    val animatedMax by animateFloatAsState(targetValue = maxVal, animationSpec = tween(300), label = "max")

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        if (data.size < 2) return@Canvas
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        data.forEachIndexed { idx, value ->
            val y = size.height - (value / animatedMax * size.height * 0.9f) - 4
            val x = idx * stepX
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
}
