package com.ransomshield.earlywarning.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

class PreventiveActionManager(private val context: Context) {

    /**
     * Attempts to kill background processes of the highest-risk app.
     * Uses ActivityManager.killBackgroundProcesses() — system may not kill foreground apps.
     * Falls back to opening App Info so user can force-stop manually.
     */
    fun killSuspiciousProcesses(packageName: String?) {
        if (packageName.isNullOrBlank()) {
            Toast.makeText(context, "No suspicious app identified. Check App Risk Ranking.", Toast.LENGTH_SHORT).show()
            return
        }
        if (packageName == context.packageName) {
            Toast.makeText(context, "Cannot terminate RansomShield. Target another app from the ranking.", Toast.LENGTH_LONG).show()
            return
        }
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (am != null) {
            am.killBackgroundProcesses(packageName)
            Toast.makeText(context, "Background processes killed for $packageName. If app is in foreground, force-stop from App Info.", Toast.LENGTH_LONG).show()
            openAppInfo(packageName)
        } else {
            openAppInfo(packageName)
        }
    }

    /**
     * Opens the system App Info screen so user can force-stop or revoke permissions.
     */
    private fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Open Settings > Apps to manage $packageName", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens storage/file-access settings. User can revoke broad storage access from suspicious apps.
     */
    fun lockSensitiveFolders() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Settings.ACTION_PRIVACY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            }
            context.startActivity(intent)
            Toast.makeText(context, "Review which apps have file access. Revoke from suspicious apps.", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Go to Settings > Apps > Permissions to restrict file access.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens backup settings so user can restore from backup if available.
     */
    fun triggerBackupRollback() {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_PRIVACY_SETTINGS
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Toast.makeText(context, "Check Settings > Google > Backup for restore options. Restore from a pre-incident backup if available.", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Open Settings > Backup to restore from backup.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Guides user to boot in safe mode (requires manual reboot).
     */
    fun suggestSafeMode() {
        Toast.makeText(
            context,
            "To boot in Safe Mode: Hold power button → tap & hold 'Power off' → tap 'Restart in Safe Mode'",
            Toast.LENGTH_LONG
        ).show()
    }
}
