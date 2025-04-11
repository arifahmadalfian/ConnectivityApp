package com.ariefahmadalfian.connectivityapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariefahmadalfian.connectivityapp.core.ChatRequest
import com.ariefahmadalfian.connectivityapp.core.DeepSeekApiService
import com.ariefahmadalfian.connectivityapp.core.IConnectivityObserver
import com.ariefahmadalfian.connectivityapp.core.IDeepSeekApiService
import com.ariefahmadalfian.connectivityapp.core.Message
import com.ariefahmadalfian.connectivityapp.core.TextToSpeechHelper
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

class ConnectivityViewModel(
    private val connectivityObserver: IConnectivityObserver,
    private val ttsHelper: TextToSpeechHelper,
    private val apiService: IDeepSeekApiService
) : ViewModel() {

    val isConnected = connectivityObserver
        .isConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val latency: StateFlow<Long> = isConnected.flatMapLatest { connected ->
        if (connected) {
            flow {
                while (true) {
                    emit(getInternetLatency())
                    delay(5000)
                }
            }
        } else {
            flowOf(-1L)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        -1
    )

    private suspend fun getInternetLatency(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()
                val address = InetAddress.getByName("google.com")
                if (address.isReachable(1000)) {
                    System.currentTimeMillis() - start
                } else {
                    -1
                }
            } catch (e: Exception) {
                -1
            }
        }
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Fungsi untuk memulai/menghentikan recording
    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
    }

    // Fungsi untuk mengirim pesan ke AI
    fun sendMessage(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Tambahkan pesan user ke UI
                _messages.value += MessageState(
                    text = text,
                    isUser = true
                ).toDto()
                println("API Response Messages: ${_messages.value}")

                // 2. Konversi ke format DTO untuk API
                val apiRequest = ChatRequest(
                    messages = listOf(
                        Message(  // <-- Gunakan MessageDTO untuk API
                            role = "user",
                            content = text
                        )
                    )
                )
                println("API Response Request: $apiRequest")

                // 3. Kirim request ke API
                val apiResponse = apiService.sendMessage(apiRequest)
                println("API Response: $apiResponse")

                // 4. Konversi response ke model UI
                val aiMessage = MessageState(
                    text = apiResponse.choices.first().message.content,
                    isUser = false
                )

                // 5. Update UI
                _messages.update { currentList ->
                    currentList + aiMessage.toDto()
                }

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                println("API Response Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    // Fungsi untuk memulai/menghentikan suara
    fun toggleSpeech(text: String) {
        if (_isSpeaking.value) {
            ttsHelper.stop()
            _isSpeaking.value = false
        } else {
            ttsHelper.speak(text)
            _isSpeaking.value = true
        }
    }

    override fun onCleared() {
        ttsHelper.shutdown()
        super.onCleared()
    }
}

data class MessageState(
    val text: String,
    val isUser: Boolean
)

fun Message.toUiModel() = MessageState(
    text = content,
    isUser = role == "user"
)

fun MessageState.toDto() = Message(
    role = if (isUser) "user" else "assistant",
    content = text
)



