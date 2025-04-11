package com.ariefahmadalfian.connectivityapp.core

import android.speech.tts.TextToSpeech
import com.ariefahmadalfian.connectivityapp.BuildConfig
import com.ariefahmadalfian.connectivityapp.ConnectivityViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@OptIn(ExperimentalSerializationApi::class)
val appModule = module {
    // Ktor Client
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                        explicitNulls = false
                    }
                )
            }
            install(Logging) {
                logger = Logger.Companion.ANDROID
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 100_000L
                requestTimeoutMillis = 100_000L
                socketTimeoutMillis = 100_000L
            }
            defaultRequest {
                url(BuildConfig.BASE_URL)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
            }
        }
    }

    single<IConnectivityObserver> { AndroidConnectivityObserver(get()) }

    // DeepSeek Service
    single<IDeepSeekApiService> { DeepSeekApiService(get()) }

    single{ TextToSpeechHelper(get()) }

    // ViewModel
    viewModel { ConnectivityViewModel(get(), get(), get()) }
}