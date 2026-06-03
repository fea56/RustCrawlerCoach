package com.rustcrawlercoach.network

import com.google.gson.annotations.SerializedName

/**
 * DeepSeek API 请求体
 */
data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048
)

data class Message(
    val role: String,
    val content: String
)

/**
 * DeepSeek API 响应体
 */
data class DeepSeekResponse(
    val id: String?,
    val choices: List<Choice>?,
    val error: ErrorDetail?
)

data class Choice(
    val message: Message?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class ErrorDetail(
    val message: String?,
    val type: String?,
    val code: String?
)
