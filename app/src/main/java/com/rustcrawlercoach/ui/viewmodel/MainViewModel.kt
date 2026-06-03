package com.rustcrawlercoach.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustcrawlercoach.data.database.AppDatabase
import com.rustcrawlercoach.data.entity.Chapter
import com.rustcrawlercoach.data.entity.ChapterProgress
import com.rustcrawlercoach.data.entity.Phase
import com.rustcrawlercoach.data.entity.UserProgress
import com.rustcrawlercoach.data.repository.AppRepository
import com.rustcrawlercoach.network.ApiResult
import com.rustcrawlercoach.network.DeepSeekHelper
import com.rustcrawlercoach.network.RetrofitClient
import com.rustcrawlercoach.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChapterWithProgress(
    val chapter: Chapter,
    val progress: ChapterProgress?,
    val isUnlocked: Boolean
)

data class DashboardUiState(
    val currentPhase: Phase? = null,
    val currentChapter: Chapter? = null,
    val userProgress: UserProgress? = null,
    val totalChapters: Int = 0,
    val completedChapters: Int = 0,
    val allPhases: List<Phase> = emptyList(),
    val chaptersWithProgress: List<ChapterWithProgress> = emptyList(),
    val lockedChapterHint: String? = null,
    val isLoading: Boolean = true,
    val learningAdvice: String = "",
    val adviceIsLoading: Boolean = false,
    val canRefreshAdvice: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

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
    private val preferencesManager = PreferencesManager(application)
    private val deepSeekHelper = DeepSeekHelper(RetrofitClient.deepSeekApi)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeUserProgress()
            loadDashboardData()
            loadLearningAdvice()
            checkCanRefreshAdvice()
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                repository.getAllPhases(),
                repository.getUserProgress(),
                repository.getAllChapters(),
                repository.getAllChapterProgress()
            ) { phases, progress, chapters, chapterProgressList ->
                Quadruple(phases, progress, chapters, chapterProgressList)
            }.collect { (phases, progress, chapters, chapterProgressList) ->
                val currentPhase = phases.find { it.id == progress?.currentPhaseId }
                val currentChapter = chapters.find { it.id == progress?.currentChapterId }
                val completedCount = chapterProgressList.count { it.isCompleted }

                // 构建带进度和解锁状态的章节列表
                val chaptersWithProgress = chapters.map { chapter ->
                    val chapterProgress = chapterProgressList.find { it.chapterId == chapter.id }
                    val isUnlocked = repository.isChapterUnlocked(chapter.id)
                    ChapterWithProgress(chapter, chapterProgress, isUnlocked)
                }

                // 获取当前章节的锁定提示
                val lockedHint = currentChapter?.let {
                    repository.getNextLockedChapterHint(it.id)
                }

                _uiState.value = DashboardUiState(
                    currentPhase = currentPhase,
                    currentChapter = currentChapter,
                    userProgress = progress,
                    totalChapters = chapters.size,
                    completedChapters = completedCount,
                    allPhases = phases,
                    chaptersWithProgress = chaptersWithProgress,
                    lockedChapterHint = lockedHint,
                    isLoading = false
                )
            }
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun checkIn() {
        viewModelScope.launch {
            repository.checkIn()
        }
    }

    fun checkAndResetStreak() {
        viewModelScope.launch {
            repository.checkAndResetStreak()
        }
    }

    fun useStreakFreeze() {
        viewModelScope.launch {
            repository.useStreakFreeze()
        }
    }

    fun refreshData() {
        loadDashboardData()
    }

    private suspend fun loadLearningAdvice() {
        val cachedAdvice = preferencesManager.learningAdvice.first()
        if (cachedAdvice.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(learningAdvice = cachedAdvice)
        } else {
            // 如果没有缓存，自动获取一次
            generateAndSaveAdvice()
        }
    }

    private suspend fun checkCanRefreshAdvice() {
        val canRefresh = preferencesManager.canRefreshAdvice()
        _uiState.value = _uiState.value.copy(canRefreshAdvice = canRefresh)
    }

    fun refreshLearningAdvice() {
        viewModelScope.launch {
            generateAndSaveAdvice()
        }
    }

    private suspend fun generateAndSaveAdvice() {
        _uiState.value = _uiState.value.copy(adviceIsLoading = true)
        
        try {
            val completedChapters = repository.getCompletedChapterNames()
            val accuracy = repository.getAverageAccuracy()
            val userProgress = repository.getUserProgressSync()
            val streakDays = userProgress?.streakDays ?: 0

            val result = deepSeekHelper.generateLearningAdvice(
                completedChapters = completedChapters,
                accuracy = accuracy,
                streakDays = streakDays
            )

            when (result) {
                is ApiResult.Success -> {
                    val advice = result.data
                    preferencesManager.saveLearningAdvice(advice)
                    preferencesManager.saveAdviceLastRefresh(System.currentTimeMillis().toString())
                    _uiState.value = _uiState.value.copy(
                        learningAdvice = advice,
                        canRefreshAdvice = false
                    )
                }
                is ApiResult.Error -> {
                    // 保持原样或显示错误
                }
                is ApiResult.Loading -> {}
            }
        } finally {
            _uiState.value = _uiState.value.copy(adviceIsLoading = false)
        }
    }
}
