// app/src/main/java/com/ggdpi/app/service/GGDPILocalService.kt
package com.ggdpi.app.service

import android.net.VpnService.Builder  // ← ДЛЯ addRoute()!
import com.ggdpi.app.data.LogEntry  // ← ДЛЯ LogEntry.LogType
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope  // ← ДОБАВИТЬ
import kotlinx.coroutines.Dispatchers  // ← ДОБАВИТЬ
import kotlinx.coroutines.SupervisorJob  // ← ДОБАВИТЬ
import kotlinx.coroutines.cancel
import com.ggdpi.app.R
import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.data.LogRepository
import com.ggdpi.app.dpibypass.DpiBypassEngine  // ← Без .kt!
import com.ggdpi.app.ui.MainActivity  // ← Убедитесь, что файл существует
import timber.log.Timber

class GGDPILocalService : VpnService() {

    private val binder = LocalBinder()
    private var bypassEngine: DpiBypassEngine? = null  // ← Без .kt!
    private val logRepository = LogRepository()
    private val strategyManager = StrategyManager()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)  // ← Исправлено

    inner class LocalBinder : Binder() {
        fun getService(): GGDPILocalService = this@GGDPILocalService
        fun getLogRepository(): LogRepository = logRepository
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val strategy = strategyManager.loadCurrentStrategy()
                startVpn(strategy)
            }
            ACTION_STOP -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn(strategy: StrategyManager.DpiStrategy) {
        val builder = Builder()
            .setSession("GGDPI")
            .addAddress(`Constants.kt`.VPN_ADDRESS, `Constants.kt`.VPN_SUBNET_PREFIX)
            .addRoute("0.0.0.0", 0)  // Перехватывать весь трафик
            .addDnsServer(`Constants.kt`.DEFAULT_DNS)
            .setMtu(`Constants.kt`.DEFAULT_MTU)
            .setBlocking(false)

        // Android 14+ требует explicit disallowed apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Можно добавить excludeApps если нужно
        }

        try {
            val vpnInterface = builder.establish() ?: throw IllegalStateException("Failed to establish VPN")

            // Запускаем notification
            startForeground(NOTIFICATION_ID, createNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )

            // Инициализируем и запускаем engine
            bypassEngine = DpiBypassEngine(this, strategyManager, logRepository)
            bypassEngine?.start(vpnInterface, strategy)

            logRepository.addLog("VPN started with strategy: ${strategy.name}", LogEntry.LogType.INFO)

        } catch (e: Exception) {
            Timber.e(e, "Failed to start VPN")
            logRepository.addLog("VPN start failed: ${e.message}", LogEntry.LogType.ERROR)
            stopSelf()
        }
    }

    private fun stopVpn() {
        bypassEngine?.stop()
        bypassEngine = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        logRepository.addLog("VPN stopped", LogEntry.LogType.INFO)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GGDPI Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DPI Bypass Service"
                lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),  // ← MainActivity должна существовать
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GGDPI Active")
            .setContentText("DPI bypass is running")
            .setSmallIcon(R.drawable.ic_vpn)  // Убедитесь, что иконка существует
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        serviceScope.cancel()
        stopVpn()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "ggdpi_service_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.ggdpi.app.service.START"
        const val ACTION_STOP = "com.ggdpi.app.service.STOP"
    }
}