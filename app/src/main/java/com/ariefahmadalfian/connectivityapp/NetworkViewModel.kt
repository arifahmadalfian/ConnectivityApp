package com.ariefahmadalfian.connectivityapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.net.InetAddress

class ConnectivityViewModel(
    private val connectivityObserver: IConnectivityObserver
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
}


