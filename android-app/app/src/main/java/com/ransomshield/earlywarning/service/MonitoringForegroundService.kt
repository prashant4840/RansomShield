package com.ransomshield.earlywarning.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class MonitoringForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(NotificationManager::class.java)
        val channelId = "ransomshield_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "RansomShield Monitor", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("RansomShield Active")
            .setContentText("Real-time ransomware early warning is running")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
        startForeground(11, notification)
        return START_STICKY
    }
}
