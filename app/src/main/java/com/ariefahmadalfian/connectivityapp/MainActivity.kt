package com.ariefahmadalfian.connectivityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ariefahmadalfian.connectivityapp.ui.theme.ConnectivityAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel<ConnectivityViewModel> {
                ConnectivityViewModel(
                    connectivityObserver = AndroidConnectivityObserver(applicationContext)
                )
            }

            val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
            val latency by viewModel.latency.collectAsStateWithLifecycle()

            LaunchedEffect(key1 = isConnected, key2 = latency) {
                println("isConnected: $isConnected latency: $latency")
            }

            ConnectivityAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ConnectionStatusWithLatency(isConnected, latency)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusWithLatency(isConnected: Boolean, latency: Long) {
    val (color, text) = when {
        !isConnected -> Color.Red to "Tidak Ada Koneksi"
        latency < 0 -> Color.Red to "Tidak Dapat Mengukur Latency"
        latency < 100 -> Color.Green to "Koneksi Bagus (${latency}ms)"
        latency in 100..500 -> Color.Yellow to "Koneksi Lemah (${latency}ms)"
        else -> Color.Red to "Koneksi Buruk (${latency}ms)"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

