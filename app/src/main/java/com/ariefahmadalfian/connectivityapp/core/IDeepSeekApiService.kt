package com.ariefahmadalfian.connectivityapp.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url

interface IDeepSeekApiService {
    suspend fun sendMessage(request: ChatRequest): ChatResponse
}

class DeepSeekApiService(private val client: HttpClient) : IDeepSeekApiService {
    override suspend fun sendMessage(request: ChatRequest): ChatResponse {
        return client.post {
            url("/chat/completions")
            setBody(request)
        }.body<ChatResponse>()
    }
}