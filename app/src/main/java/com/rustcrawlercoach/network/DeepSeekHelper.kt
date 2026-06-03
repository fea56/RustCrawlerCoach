package com.rustcrawlercoach.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * DeepSeek API 调用结果
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: String? = null) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

@Serializable
data class GeneratedQuestion(
    val question: String,
    val type: String,
    val options: List<String>? = null,
    val answer: String
)

/**
 * DeepSeek 助手类
 */
class DeepSeekHelper(
    private val apiService: DeepSeekApiService
) {
    companion object {
        const val SYSTEM_PROMPT = "你是全栈爬虫课程的智能教练助手，帮助用户解答编程问题，评审代码，提供学习指导。请用简洁专业的语言回答。"

        // 项目需求生成提示模板
        fun generateProjectPrompt(knowledgePoints: String): String {
            return """你是全栈爬虫教练。根据以下知识点生成一个适合初学者的编程项目需求。

知识点：$knowledgePoints

要求：
1. 包含项目背景和目标
2. 列出具体功能要求
3. 提供验收标准
4. 给出难度评估和建议

请用 Markdown 格式输出。"""
        }

        // 代码审查提示模板
        fun generateCodeReviewPrompt(code: String, context: String): String {
            return """你是代码审查专家。请审查以下代码并给出建议。

当前学习内容上下文：$context

代码：
```
$code
```

请从以下角度审查：
1. 代码正确性
2. 潜在错误
3. 性能优化
4. 代码风格
5. 改进建议

请用 Markdown 格式输出。"""
        }

        // 费曼测验评分提示模板
        fun generateFeynmanPrompt(explanation: String, topic: String): String {
            return """你是计算机教育专家。请评价以下解释是否正确、清晰、深入程度。给出0-100分和具体反馈。
知识点是$topic。
解释：$explanation"""
        }

        // 学习建议生成提示模板
        fun generateLearningAdvicePrompt(completedChapters: List<String>, accuracy: Int, streakDays: Int): String {
            val chaptersList = completedChapters.joinToString(", ")
            return """用户正在学习全栈爬虫课程，已完成章节：$chaptersList，正确率约 $accuracy%，连续打卡 $streakDays 天。请给出下一步学习重点和建议（不超过150字）。"""
        }

        // 题目生成提示模板
        fun generateQuestionPrompt(knowledgePoints: String, exampleQuestion: String, exampleType: String, exampleOptions: String? = null, exampleAnswer: String): String {
            val exampleSection = buildString {
                appendLine("示例题目：")
                appendLine(exampleQuestion)
                appendLine("类型：$exampleType")
                if (exampleOptions != null && exampleOptions.isNotEmpty()) {
                    appendLine("选项：$exampleOptions")
                }
                appendLine("答案：$exampleAnswer")
            }
            
            return """请根据以下知识点，生成一道新的选择题或填空题。
知识点：$knowledgePoints
$exampleSection
请返回 JSON 格式，包含以下字段：
- question: 题目文本
- type: 题目类型，只能是 "choice"（选择题）或 "fill"（填空题）
- options: 选择题的选项数组（填空题时为 null）
- answer: 正确答案
请只返回 JSON，不要有其他文字。"""
        }

        // 通用问答提示
        fun generateQAPrompt(question: String, context: String? = null): String {
            return if (context != null) {
                """当前学习内容：$context

用户问题：$question

请用简洁专业的方式回答，如果涉及编程问题请给出代码示例。"""
            } else {
                question
            }
        }
    }

    suspend fun chat(messages: List<Message>): ApiResult<String> {
        return try {
            val response = apiService.chat(
                authorization = "Bearer ${DeepSeekConfig.apiKey}",
                request = DeepSeekRequest(messages = messages)
            )

            if (response.error != null) {
                ApiResult.Error(
                    message = response.error.message ?: "Unknown error",
                    code = response.error.code
                )
            } else {
                val content = response.choices?.firstOrNull()?.message?.content ?: ""
                ApiResult.Success(content)
            }
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Network error")
        }
    }

    suspend fun generateProject(knowledgePoints: String): ApiResult<String> {
        val messages = listOf(
            Message("system", SYSTEM_PROMPT),
            Message("user", generateProjectPrompt(knowledgePoints))
        )
        return chat(messages)
    }

    suspend fun reviewCode(code: String, context: String): ApiResult<String> {
        val messages = listOf(
            Message("system", SYSTEM_PROMPT),
            Message("user", generateCodeReviewPrompt(code, context))
        )
        return chat(messages)
    }

    suspend fun evaluateFeynman(explanation: String, topic: String): ApiResult<String> {
        val messages = listOf(
            Message("system", "你是费曼学习法评审专家，用心给出评分和反馈，帮助用户提升理解能力。"),
            Message("user", generateFeynmanPrompt(explanation, topic))
        )
        return chat(messages)
    }

    suspend fun answerQuestion(question: String, context: String? = null): ApiResult<String> {
        val messages = listOf(
            Message("system", SYSTEM_PROMPT),
            Message("user", generateQAPrompt(question, context))
        )
        return chat(messages)
    }

    suspend fun generateLearningAdvice(
        completedChapters: List<String>,
        accuracy: Int,
        streakDays: Int
    ): ApiResult<String> {
        val messages = listOf(
            Message("system", "你是专业的学习规划师，根据用户学习进度给出针对性的建议。"),
            Message("user", generateLearningAdvicePrompt(completedChapters, accuracy, streakDays))
        )
        return chat(messages)
    }

    suspend fun generateQuestion(
        knowledgePoints: String,
        exampleQuestion: String,
        exampleType: String,
        exampleOptions: String? = null,
        exampleAnswer: String
    ): ApiResult<GeneratedQuestion> {
        val messages = listOf(
            Message("system", "你是专业的题目生成专家，根据知识点和示例生成新题目。"),
            Message("user", generateQuestionPrompt(knowledgePoints, exampleQuestion, exampleType, exampleOptions, exampleAnswer))
        )
        
        val chatResult = chat(messages)
        return when (chatResult) {
            is ApiResult.Success -> {
                try {
                    val json = Json { ignoreUnknownKeys = true }
                    val generatedQuestion = json.decodeFromString<GeneratedQuestion>(chatResult.data.trim())
                    ApiResult.Success(generatedQuestion)
                } catch (e: Exception) {
                    ApiResult.Error("JSON 解析失败：${e.message}")
                }
            }
            is ApiResult.Error -> chatResult
            is ApiResult.Loading -> ApiResult.Loading
        }
    }
}

/**
 * DeepSeek 配置
 */
object DeepSeekConfig {
    var apiKey: String = ""
        private set

    fun setApiKey(key: String) {
        apiKey = key.trim()
    }

    fun isConfigured(): Boolean = apiKey.isNotBlank()
}
