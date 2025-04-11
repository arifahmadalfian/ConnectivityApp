package com.ariefahmadalfian.connectivityapp

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ariefahmadalfian.connectivityapp.ui.theme.ConnectivityAppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = koinViewModel<ConnectivityViewModel>()

            val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
            val latency by viewModel.latency.collectAsStateWithLifecycle()

            LaunchedEffect(key1 = isConnected, key2 = latency) {
                println("isConnected: $isConnected latency: $latency")
            }

            ConnectivityAppTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .navigationBarsPadding(),
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
                        PermissionWrapper(viewModel)
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

@Composable
fun ChatScreen(viewModel: ConnectivityViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var recognizedText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Speech Recognizer
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    // Handle error
    if (!error.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error!!) },
            confirmButton = { Button(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    text = message.content,
                    role = message.role,
                    viewModel = viewModel
                )
            }
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // Recording UI
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tombol Rekam Suara
            Button(
                onClick = {
                    viewModel.toggleRecording()
                    if (isRecording) {
                        startListening(speechRecognizer) { text ->
                            recognizedText = text
                            viewModel.sendMessage(text)
                        }
                    } else {
                        stopListening(speechRecognizer)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isRecording) "Stop" else "Start Recording")
            }

            // Text hasil rekaman
            Text(
                text = recognizedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MessageBubble(text: String, role: String, viewModel: ConnectivityViewModel) {
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (role == "user") MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { viewModel.toggleSpeech(text) }
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Close
                    else Icons.Default.Check,
                    contentDescription = "Speak"
                )
            }
        }
    }
}

fun startListening(
    speechRecognizer: SpeechRecognizer,
    onResult: (String) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
        }

        override fun onBeginningOfSpeech() {
        }

        override fun onRmsChanged(rmsdB: Float) {
        }

        override fun onBufferReceived(buffer: ByteArray?) {
        }

        override fun onEndOfSpeech() {
        }

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> {
                    // Tidak ada hasil yang cocok
                }

                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    // Waktu habis untuk mendeteksi ucapan
                }

                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                    // Waktu habis untuk koneksi jaringan
                }

                SpeechRecognizer.ERROR_NETWORK -> {
                    // Kesalahan jaringan
                }

                else -> {
                    // Kesalahan lainnya
                }
            }
        }

        override fun onResults(results: Bundle?) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                onResult(it)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Handle partial results jika diperlukan
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
        }

        // Implementasi callback lainnya (onError, onReadyForSpeech, dll)
    })

    speechRecognizer.startListening(intent)
}

fun stopListening(speechRecognizer: SpeechRecognizer) {
    speechRecognizer.stopListening()
}

@Composable
fun PermissionWrapper(viewModel: ConnectivityViewModel) {
    val context = LocalContext.current
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
    }

    if (!hasRecordPermission) {
        AlertDialog(
            onDismissRequest = { /* Handle dismiss */ },
            title = { Text("Permission Required") },
            text = { Text("Please allow microphone access for voice chat") },
            confirmButton = {
                Button(onClick = { launcher.launch(android.Manifest.permission.RECORD_AUDIO) }) {
                    Text("Grant Permission")
                }
            }
        )
    } else {
        ChatScreen(viewModel)
    }
}



