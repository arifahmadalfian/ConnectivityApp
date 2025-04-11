package com.ariefahmadalfian.connectivityapp.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    @SerialName("messages")
    val messages: List<Message>,
    @SerialName("model")
    val model: String = "deepseek-chat"
)

@Serializable
data class Message(
    @SerialName("role")
    val role: String, // "user", "assistant", atau "system"
    @SerialName("content")
    val content: String
)

@Serializable
data class ChatResponse(
    @SerialName("id")
    val id: String,
    @SerialName("choices")
    val choices: List<Choice>,
    @SerialName("created")
    val created: Long,
    @SerialName("usage")
    val usage: Usage?
)

@Serializable
data class Choice(
    @SerialName("index")
    val index: Int,
    @SerialName("message")
    val message: Message, // Menggunakan data class Message yang sama
    @SerialName("finish_reason")
    val finishReason: String?
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)