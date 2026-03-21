package com.ransomshield.earlywarning.ml

import android.content.Context
import com.ransomshield.earlywarning.domain.TelemetrySample
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

class TFLiteScorer(context: Context) {
    private val window = ArrayDeque<FloatArray>()
    private val windowSize = 8

    private val interpreter: Interpreter? = runCatching {
        val asset = context.assets.open("ransom_model.tflite").readBytes()
        val model = ByteBuffer.allocateDirect(asset.size).order(ByteOrder.nativeOrder())
        model.put(asset)
        model.rewind()
        Interpreter(model)
    }.getOrNull()

    fun scoreOrNull(sample: TelemetrySample): Float? {
        val runner = interpreter ?: return null
        val point = floatArrayOf(
            sample.cpuUsage / 100f,
            sample.memoryUsage / 100f,
            sample.ioOpsPerSec / 100f,
            sample.fileMutationBurst / 100f,
            sample.accessibilityAbuseSignal / 100f,
            sample.suspiciousPermissionScore / 100f
        )
        window.addLast(point)
        while (window.size < windowSize) window.addFirst(point)
        if (window.size > windowSize) window.removeFirst()

        val input = Array(1) { Array(windowSize) { FloatArray(6) } }
        window.forEachIndexed { i, row -> input[0][i] = row }
        val output = Array(1) { Array(windowSize) { FloatArray(6) } }

        return runCatching {
            runner.run(input, output)
            var error = 0f
            for (i in 0 until windowSize) {
                for (j in 0 until 6) {
                    error += abs(input[0][i][j] - output[0][i][j])
                }
            }
            ((error / (windowSize * 6)) * 250f).coerceIn(0f, 100f)
        }.getOrNull()
    }
}
