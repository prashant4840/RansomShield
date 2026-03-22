package com.ransomshield.earlywarning

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ransomshield.earlywarning.service.PreventiveActionManager
import com.ransomshield.earlywarning.ui.RansomShieldViewModel
import com.ransomshield.earlywarning.ui.RansomShieldViewModelFactory
import com.ransomshield.earlywarning.ui.screens.DashboardScreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: RansomShieldViewModel by viewModels {
        RansomShieldViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val actionManager = PreventiveActionManager(this)
        setContent {
            val state by viewModel.state.collectAsState()
            DashboardScreen(
                state = state,
                onToggleMonitoring = viewModel::toggleMonitoring,
                onToggleDemoMode = viewModel::toggleDemoMode,
                onKillProcess = { pkg -> actionManager.killSuspiciousProcesses(pkg) },
                onLockFolders = actionManager::lockSensitiveFolders,
                onRollback = actionManager::triggerBackupRollback,
                onSafeMode = actionManager::suggestSafeMode,
                onExportTelemetry = { format, asMalicious ->
                    exportAndShareTelemetry(format, asMalicious)
                },
                onExportLogs = { exportAndShareLogs() },
                onShareReport = { format -> shareReport(format) }
            )
        }
    }

    private fun exportAndShareTelemetry(format: String, asMalicious: Boolean) {
        val content = if (format == "json") {
            viewModel.exportTelemetryJson(asMalicious)
        } else {
            viewModel.exportTelemetryCsv(asMalicious)
        }
        val count = viewModel.getBufferedSampleCount()
        if (count == 0) {
            Toast.makeText(this, "Enable monitoring first to capture telemetry.", Toast.LENGTH_SHORT).show()
            return
        }
        val ext = if (format == "json") "json" else "csv"
        val label = if (asMalicious) "malicious" else "benign"
        val filename = "ransomshield_telemetry_${label}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.$ext"
        shareFile(filename, content, if (format == "json") "application/json" else "text/csv")
        Toast.makeText(this, "Exported $count samples", Toast.LENGTH_SHORT).show()
    }

    private fun exportAndShareLogs() {
        val content = viewModel.exportEventLogJson()
        val filename = "ransomshield_events_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
        shareFile(filename, content, "application/json")
        Toast.makeText(this, "Event log exported", Toast.LENGTH_SHORT).show()
    }

    private fun shareReport(format: String) {
        val content = if (format == "json") viewModel.exportEventLogJson() else viewModel.exportEventLogCsv()
        val ext = if (format == "json") "json" else "csv"
        val mime = if (format == "json") "application/json" else "text/csv"
        val filename = "ransomshield_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.$ext"
        shareFile(filename, content, mime)
        Toast.makeText(this, "Report shared", Toast.LENGTH_SHORT).show()
    }

    private fun shareFile(filename: String, content: String, mimeType: String) {
        val file = File(cacheDir, filename)
        file.writeText(content)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }
}
