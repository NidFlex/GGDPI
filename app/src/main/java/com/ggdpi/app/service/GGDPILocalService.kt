package com.ggdpi.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.ggdpi.app.MainActivity
import com.ggdpi.app.R
import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.dpibypass.DpiBypassEngine
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class GGDPILocalService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var bypassEngine: DpiBypassEngine? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRunning = false

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "ggdpi_local_bypass"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_LOCAL_BYPASS" -> startLocalBypass()
            "STOP_LOCAL_BYPASS" -> stopLocalBypass()
        }
        return START_STICKY
    }

    private fun startLocalBypass() {
        if (isRunning) return
        
        isRunning = true
        startForeground(NOTIFICATION_ID, buildNotification("Starting DPI bypass..."))

        serviceScope.launch {
            try {
                establishLocalVpn()
                startBypassEngine()
                updateNotification("DPI Bypass Active - ${getCurrentStrategyName()}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start local bypass")
                stopLocalBypass()
            }
        }
    }

    private fun establishLocalVpn() {
        val builder = Builder().apply {
            setSession("GGDPI-Local")
            addAddress("10.200.200.1", 24)
            addDnsServer("1.1.1.1")
            addDnsServer("8.8.8.8")
            addRoute("0.0.0.0", 0)
            addRoute("::", 0)
            setMtu(1400)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                allowBypass()
            }
        }

        vpnInterface = builder.establish()
            ?: throw IllegalStateException("Failed to establish VPN interface")
        
        Timber.i("Local VPN interface established")
    }

    private fun startBypassEngine() {
        vpnInterface?.let { iface ->
            bypassEngine = DpiBypassEngine(
                vpnInterface = iface,
                strategyManager = StrategyManager(applicationContext),
                scope = serviceScope,
                onPacketProcessed = { count ->
                    if (count % 1000 == 0L) {
                        updateNotification("Processed $count packets")
                    }
                }
            )
            bypassEngine?.start()
        }
    }

    private fun stopLocalBypass() {
        isRunning = false
        bypassEngine?.stop()
        bypassEngine = null
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Timber.i("Local DPI bypass stopped")
    }

    private fun getCurrentStrategyName(): String {
        return StrategyManager(applicationContext).getCurrentStrategy().displayName
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "GGDPI Local Bypass",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Local DPI bypass service"
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("GGDPI Local Bypass")
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_vpn)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setOngoing(true)
        .build()

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}