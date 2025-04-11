package com.ariefahmadalfian.connectivityapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

class ConnectivityViewModel(
    private val connectivityObserver: IConnectivityObserver
) : ViewModel() {

    val isConnected = connectivityObserver
        .isConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    private val _latency = MutableStateFlow<Long>(-1)
    val latency: StateFlow<Long> = _latency.asStateFlow()

    init {
        viewModelScope.launch {
            isConnected.collectLatest { connected ->
                if (connected) {
                    while (isActive && isConnected.value) {
                        val latencyMs = getInternetLatency()
                        _latency.value = latencyMs
                        delay(5000)
                    }
                } else {
                    _latency.value = -1
                }
            }
        }
    }

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
}


