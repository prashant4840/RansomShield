package com.ransomshield.earlywarning.service

import android.content.Context
import android.widget.Toast

class PreventiveActionManager(private val context: Context) {
    fun killSuspiciousProcesses() {
        Toast.makeText(context, "Isolation action: suspicious process terminated (simulated)", Toast.LENGTH_SHORT).show()
    }

    fun lockSensitiveFolders() {
        Toast.makeText(context, "Sensitive folders moved to protected mode (simulated)", Toast.LENGTH_SHORT).show()
    }

    fun triggerBackupRollback() {
        Toast.makeText(context, "Secure snapshot rollback initiated (simulated)", Toast.LENGTH_SHORT).show()
    }

    fun suggestSafeMode() {
        Toast.makeText(context, "Recommendation: reboot in safe mode", Toast.LENGTH_SHORT).show()
    }
}
