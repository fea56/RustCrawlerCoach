package com.rustcrawlercoach.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustcrawlercoach.data.database.AppDatabase
import com.rustcrawlercoach.data.entity.Chapter
import com.rustcrawlercoach.data.entity.QuestionBank
import com.rustcrawlercoach.data.repository.AppRepository
import com.rustcrawlercoach.network.ApiResult
import com.rustcrawlercoach.network.DeepSeekHelper
import com.rustcrawlercoach.network.GeneratedQuestion
import com.rustcrawlercoach.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionState(
    val question: QuestionBank,
    val selectedAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val showResult: Boolean = false
)

data class LearningUiState(
    val chapter: Chapter? = null,
    val questions: List<QuestionBank> = emptyList(),
    val questionStates: List<QuestionState> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val correctCount: Int = 0,
    val isQuizComplete: Boolean = false,
    val isLoading: Boolean = true,
    val aiResponse: String? = null,
    val isAiLoading: Boolean = false,
    val showAiDialog: Boolean = false,
    val showProjectDialog: Boolean = false,
    val projectDescription: String? = null,
    val projectRequirement: String? = null, // 已保存的项目需求
    val isGeneratingQuestion: Boolean = false,
    val showGenerateError: Boolean = false,
    val generateErrorMessage: String? = null
)

class LearningViewModel(application: Application) : AndroidViewModel(application) {

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

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    fun loadChapter(chapterId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val chapter = repository.getChapterById(chapterId)
            val questions = repository.getRandomQuestions(chapterId, 5)

            _uiState.value = _uiState.value.copy(
                chapter = chapter,
                questions = questions,
                questionStates = questions.map { QuestionState(it) },
                projectRequirement = chapter?.projectRequirement,
                isLoading = false
            )
        }
    }

    fun selectAnswer(answer: String) {
        val currentIndex = _uiState.value.currentQuestionIndex
        val questionState = _uiState.value.questionStates.getOrNull(currentIndex) ?: return
        val question = questionState.question

        val isCorrect = answer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)

        val updatedStates = _uiState.value.questionStates.toMutableList()
        updatedStates[currentIndex] = questionState.copy(
            selectedAnswer = answer,
            isCorrect = isCorrect,
            showResult = true
        )

        _uiState.value = _uiState.value.copy(
            questionStates = updatedStates,
            correctCount = if (isCorrect) _uiState.value.correctCount + 1 else _uiState.value.correctCount
        )
    }

    fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val totalQuestions = _uiState.value.questions.size

        if (currentIndex < totalQuestions - 1) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = currentIndex + 1
            )
        } else {
            // 测验完成
            _uiState.value = _uiState.value.copy(isQuizComplete = true)

            // 更新测验进度
            viewModelScope.launch {
                val chapter = _uiState.value.chapter ?: return@launch
                val score = if (totalQuestions > 0) {
                    (_uiState.value.correctCount * 100) / totalQuestions
                } else {
                    0
                }
                repository.updateQuizProgress(chapter.id, score)
            }
        }
    }

    fun askAi(question: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiLoading = true)

            val result = deepSeekHelper.answerQuestion(
                question = question,
                context = _uiState.value.chapter?.knowledgePoints
            )

            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        aiResponse = result.data,
                        isAiLoading = false,
                        showAiDialog = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        aiResponse = "抱歉，发生了错误：${result.message}",
                        isAiLoading = false,
                        showAiDialog = true
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun generateProject() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiLoading = true)

            val chapter = _uiState.value.chapter ?: return@launch
            val result = deepSeekHelper.generateProject(chapter.knowledgePoints)

            when (result) {
                is ApiResult.Success -> {
                    val requirement = result.data
                    
                    // 保存项目需求到数据库
                    repository.updateProjectRequirement(chapter.id, requirement)
                    
                    _uiState.value = _uiState.value.copy(
                        projectDescription = requirement,
                        projectRequirement = requirement,
                        isAiLoading = false,
                        showProjectDialog = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        projectDescription = "抱歉，发生了错误：${result.message}",
                        isAiLoading = false,
                        showProjectDialog = true
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun completeChapterAndNext() {
        viewModelScope.launch {
            val chapter = _uiState.value.chapter ?: return@launch
            repository.updateProjectProgress(chapter.id, true)
            repository.moveToNextChapter()
        }
    }

    fun dismissAiDialog() {
        _uiState.value = _uiState.value.copy(
            showAiDialog = false,
            aiResponse = null
        )
    }

    fun dismissProjectDialog() {
        _uiState.value = _uiState.value.copy(
            showProjectDialog = false,
            projectDescription = null
        )
    }

    fun resetQuiz() {
        _uiState.value = _uiState.value.copy(
            questionStates = _uiState.value.questions.map { QuestionState(it) },
            currentQuestionIndex = 0,
            correctCount = 0,
            isQuizComplete = false
        )
    }

    fun generateNewQuestion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingQuestion = true,
                showGenerateError = false,
                generateErrorMessage = null
            )

            val chapter = _uiState.value.chapter ?: run {
                _uiState.value = _uiState.value.copy(
                    isGeneratingQuestion = false,
                    showGenerateError = true,
                    generateErrorMessage = "无法获取章节信息"
                )
                return@launch
            }

            val exampleQuestion = _uiState.value.questions.firstOrNull() ?: run {
                _uiState.value = _uiState.value.copy(
                    isGeneratingQuestion = false,
                    showGenerateError = true,
                    generateErrorMessage = "没有找到示例题目"
                )
                return@launch
            }

            val result = deepSeekHelper.generateQuestion(
                knowledgePoints = chapter.knowledgePoints,
                exampleQuestion = exampleQuestion.questionText,
                exampleType = exampleQuestion.questionType,
                exampleOptions = exampleQuestion.options,
                exampleAnswer = exampleQuestion.correctAnswer
            )

            when (result) {
                is ApiResult.Success -> {
                    addGeneratedQuestion(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingQuestion = false,
                        showGenerateError = true,
                        generateErrorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun retryGenerateQuestion() {
        _uiState.value = _uiState.value.copy(
            showGenerateError = false,
            generateErrorMessage = null
        )
        generateNewQuestion()
    }

    fun dismissGenerateError() {
        _uiState.value = _uiState.value.copy(
            showGenerateError = false,
            generateErrorMessage = null
        )
    }

    private fun addGeneratedQuestion(generatedQuestion: GeneratedQuestion) {
        val newId = 999999 + _uiState.value.questions.size // 临时 ID，不保存到数据库
        val optionsJson = if (generatedQuestion.type == "choice" && generatedQuestion.options != null) {
            // 转换为 JSON 数组格式
            "[${generatedQuestion.options.joinToString(",") { "\"$it\"" }}]"
        } else {
            null
        }
        
        val questionBank = QuestionBank(
            id = newId,
            chapterId = _uiState.value.chapter?.id ?: 0,
            questionText = generatedQuestion.question,
            questionType = generatedQuestion.type,
            options = optionsJson,
            correctAnswer = generatedQuestion.answer,
            xpReward = 20 // 额外经验奖励
        )

        val updatedQuestions = _uiState.value.questions + questionBank
        val updatedStates = _uiState.value.questionStates + QuestionState(questionBank)

        _uiState.value = _uiState.value.copy(
            questions = updatedQuestions,
            questionStates = updatedStates,
            isGeneratingQuestion = false
        )
    }
}
