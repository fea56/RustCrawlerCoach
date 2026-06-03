package com.rustcrawlercoach.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustcrawlercoach.data.database.AppDatabase
import com.rustcrawlercoach.data.entity.Chapter
import com.rustcrawlercoach.data.entity.FeynmanAnswer
import com.rustcrawlercoach.data.repository.AppRepository
import com.rustcrawlercoach.network.ApiResult
import com.rustcrawlercoach.network.DeepSeekHelper
import com.rustcrawlercoach.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FeynmanUiState(
    val chapter: Chapter? = null,
    val answers: List<FeynmanAnswer> = emptyList(),
    val currentAnswer: String = "",
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = true,
    val aiFeedback: String? = null,
    val aiScore: Int? = null,
    val showResultDialog: Boolean = false,
    val isMicRecording: Boolean = false,
    val chapterCompleted: Boolean = false
)

class FeynmanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        phaseDao = database.phaseDao(),
        chapterDao = database.chapterDao(),
        chapterProgressDao = database.chapterProgressDao(),
        questionBankDao = database.questionBankDao(),
        userProgressDao = database.userProgressDao(),
        codeFileDao = database.codeFileDao(),
        feynmanAnswerDao = database.feynmanAnswerDao()
    )

    private val deepSeekHelper = DeepSeekHelper(RetrofitClient.deepSeekApi)

    private val _uiState = MutableStateFlow(FeynmanUiState())
    val uiState: StateFlow<FeynmanUiState> = _uiState.asStateFlow()

    fun loadChapter(chapterId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val chapter = repository.getChapterById(chapterId)

            repository.getFeynmanAnswersByChapter(chapterId).collect { answers ->
                _uiState.value = _uiState.value.copy(
                    chapter = chapter,
                    answers = answers,
                    isLoading = false
                )
            }
        }
    }

    fun updateAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(currentAnswer = answer)
    }

    fun appendToAnswer(text: String) {
        _uiState.value = _uiState.value.copy(
            currentAnswer = _uiState.value.currentAnswer + text
        )
    }

    fun setMicRecording(isRecording: Boolean) {
        _uiState.value = _uiState.value.copy(isMicRecording = isRecording)
    }

    fun submitAnswer() {
        viewModelScope.launch {
            val answerText = _uiState.value.currentAnswer.trim()
            if (answerText.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(isSubmitting = true)

            // 保存答案
            val chapterId = _uiState.value.chapter?.id ?: return@launch
            val answerId = repository.saveFeynmanAnswer(chapterId, answerText)

            // 获取 AI 评分
            val topic = _uiState.value.chapter?.knowledgePoints ?: ""
            val result = deepSeekHelper.evaluateFeynman(answerText, topic)

            when (result) {
                is ApiResult.Success -> {
                    // 解析评分和反馈
                    val response = result.data
                    val score = parseScore(response)
                    val feedback = parseFeedback(response)

                    // 更新答案记录
                    repository.updateFeynmanAnswer(answerId.toInt(), score, feedback)

                    // 更新测验进度（费曼测验也使用 updateQuizProgress）
                    repository.updateQuizProgress(chapterId, score)

                    // 对于阶段 2 的章节，完成后标记项目进度为已完成（不需要项目）
                    repository.updateProjectProgress(chapterId, true)

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        aiScore = score,
                        aiFeedback = feedback,
                        showResultDialog = true,
                        currentAnswer = "",
                        chapterCompleted = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        aiFeedback = "抱歉，发生了错误：${result.message}",
                        showResultDialog = true
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    private fun parseScore(response: String): Int {
        // 简单解析：尝试从响应中提取数字评分
        val regex = Regex("(\\d{1,3})\\s*分|评分[:：]?\\s*(\\d{1,3})")
        val match = regex.find(response)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: match?.groupValues?.getOrNull(2)?.toIntOrNull()
            ?: 75 // 默认分数
    }

    private fun parseFeedback(response: String): String {
        // 简单处理：返回整个响应
        return response
    }

    fun dismissResultDialog() {
        _uiState.value = _uiState.value.copy(
            showResultDialog = false,
            aiScore = null,
            aiFeedback = null
        )
    }

    fun resetAnswer() {
        _uiState.value = _uiState.value.copy(
            currentAnswer = "",
            aiScore = null,
            aiFeedback = null
        )
    }
}
