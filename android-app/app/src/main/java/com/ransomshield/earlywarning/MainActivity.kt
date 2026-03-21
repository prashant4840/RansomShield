package com.ransomshield.earlywarning

import android.os.Bundle
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
                onToggleSimulation = viewModel::toggleSimulation,
                onKillProcess = actionManager::killSuspiciousProcesses,
                onLockFolders = actionManager::lockSensitiveFolders,
                onRollback = actionManager::triggerBackupRollback,
                onSafeMode = actionManager::suggestSafeMode
            )
        }
    }
}
