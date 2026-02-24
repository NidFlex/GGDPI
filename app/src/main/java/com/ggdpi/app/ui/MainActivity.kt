// app/src/main/java/com/ggdpi/app/ui/MainActivity.kt
package com.ggdpi.app.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ggdpi.app.service.GGDPILocalService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var service: GGDPILocalService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as GGDPILocalService.LocalBinder).getService()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(
                    onStartClick = { startVpn() },
                    onStopClick = { stopVpn() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, GGDPILocalService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    private fun startVpn() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            startActivityForResult(prepareIntent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
        }
    }

    private fun stopVpn() {
        service?.let {
            val intent = Intent(it, GGDPILocalService::class.java).apply {
                action = GGDPILocalService.ACTION_STOP
            }
            it.startService(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            service?.let {
                val intent = Intent(it, GGDPILocalService::class.java).apply {
                    action = GGDPILocalService.ACTION_START
                }
                it.startService(intent)
            }
        }
    }

    companion object {
        private const val VPN_REQUEST_CODE = 1001
    }
}

@Composable
fun MainScreen(onStartClick: () -> Unit, onStopClick: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("GGDPI") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRunning) "● Running" else "○ Stopped",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        isRunning = true
                        onStartClick()
                    },
                    enabled = !isRunning
                ) {
                    Text("Start")
                }
                Button(
                    onClick = {
                        isRunning = false
                        onStopClick()
                    },
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Stop")
                }
            }
        }
    }
}