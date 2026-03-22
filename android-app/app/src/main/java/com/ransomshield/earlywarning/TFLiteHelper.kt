package com.ransomshield.earlywarning

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteHelper(context: Context) {

    private var interpreter: Interpreter

    init {
        val assetFile = context.assets.open("ransom_model.tflite")
        val modelBytes = assetFile.readBytes()

        val buffer = ByteBuffer.allocateDirect(modelBytes.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(modelBytes)

        interpreter = Interpreter(buffer)
    }

    fun predict(input: Array<FloatArray>): FloatArray {
        val output = Array(1) { FloatArray(1) }
        interpreter.run(input, output)
        return output[0]
    }
}