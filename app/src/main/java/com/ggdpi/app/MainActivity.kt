package com.ggdpi.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ggdpi.app.service.GGDPILocalService
import com.ggdpi.app.ui.navigation.NavGraph
import com.ggdpi.app.ui.theme.GGDPITheme

class MainActivity : ComponentActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startBypass()
        } else {
            Toast.makeText(this, "VPN permission required for DPI bypass", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            GGDPITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        onRequestPermission = ::requestVpnPermission,
                        onStartBypass = ::startBypass,
                        onStopBypass = ::stopBypass
                    )
                }
            }
        }
    }

    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startBypass()
        }
    }

    private fun startBypass() {
        val intent = Intent(this, GGDPILocalService::class.java).apply {
            action = "START_LOCAL_BYPASS"
        }
        startService(intent)
    }

    private fun stopBypass() {
        val intent = Intent(this, GGDPILocalService::class.java).apply {
            action = "STOP_LOCAL_BYPASS"
        }
        startService(intent)
    }
}