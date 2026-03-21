package com.ransomshield.earlywarning.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import java.io.File

class AntiTamperGuard(private val context: Context) {
    fun isCompromised(): Boolean = isDebuggableBuild() || isLikelyRooted() || isLikelyEmulator()

    private fun isDebuggableBuild(): Boolean {
        val flags = context.applicationInfo.flags
        return flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    private fun isLikelyRooted(): Boolean {
        val indicators = listOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su"
        )
        return indicators.any { File(it).exists() }
    }

    private fun isLikelyEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
            Build.MODEL.contains("emulator", ignoreCase = true) ||
            Build.MANUFACTURER.contains("genymotion", ignoreCase = true)
    }
}
