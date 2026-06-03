package com.rustcrawlercoach.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustcrawlercoach.data.database.AppDatabase
import com.rustcrawlercoach.data.entity.Chapter
import com.rustcrawlercoach.data.entity.CodeFile
import com.rustcrawlercoach.data.repository.AppRepository
import com.rustcrawlercoach.network.ApiResult
import com.rustcrawlercoach.network.DeepSeekHelper
import com.rustcrawlercoach.network.RetrofitClient
import com.rustcrawlercoach.util.CodeHighlighter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorUiState(
    val chapter: Chapter? = null,
    val codeFiles: List<CodeFile> = emptyList(),
    val currentFile: CodeFile? = null,
    val currentContent: String = "",
    val currentFileName: String = "main.py",
    val language: String = "python",
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val aiReview: String? = null,
    val isAiReviewing: Boolean = false,
    val showReviewDialog: Boolean = false,
    val showNewFileDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val projectRequirement: String? = null,
    val isProjectReviewPassed: Boolean = false // 项目审查是否通过
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

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

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun loadChapter(chapterId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val chapter = repository.getChapterById(chapterId)
            repository.getCodeFilesByChapter(chapterId).collect { files ->
                // 如果有项目需求但还没有项目文件，自动创建一个
                if (chapter?.requiredProject == true && 
                    chapter.projectRequirement != null && 
                    files.isEmpty()) {
                    val projectFileName = generateProjectFileName(chapter.title)
                    val content = CodeHighlighter.getDefaultTemplate(projectFileName)
                    repository.saveCodeFile(chapterId, projectFileName, content)
                    
                    // 重新获取文件列表
                    repository.getCodeFilesByChapter(chapterId).collect { updatedFiles ->
                        val currentFile = updatedFiles.firstOrNull()
                        _uiState.value = _uiState.value.copy(
                            chapter = chapter,
                            codeFiles = updatedFiles,
                            currentFile = currentFile,
                            currentContent = currentFile?.content ?: content,
                            currentFileName = currentFile?.fileName ?: projectFileName,
                            language = CodeHighlighter.getLanguage(currentFile?.fileName ?: projectFileName),
                            projectRequirement = chapter.projectRequirement,
                            isLoading = false
                        )
                        return@collect
                    }
                } else {
                    val currentFile = files.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        chapter = chapter,
                        codeFiles = files,
                        currentFile = currentFile,
                        currentContent = currentFile?.content ?: CodeHighlighter.getDefaultTemplate("main.py"),
                        currentFileName = currentFile?.fileName ?: "main.py",
                        language = CodeHighlighter.getLanguage(currentFile?.fileName ?: "main.py"),
                        projectRequirement = chapter?.projectRequirement,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun generateProjectFileName(chapterTitle: String): String {
        // 从章节标题生成文件名
        val sanitized = chapterTitle
            .replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5]"), "_")
            .take(20)
        return "${sanitized.lowercase()}_project.py"
    }

    fun selectFile(file: CodeFile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentFile = file,
                currentContent = file.content,
                currentFileName = file.fileName,
                language = CodeHighlighter.getLanguage(file.fileName)
            )
        }
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(currentContent = content)
    }

    fun updateFileName(name: String) {
        _uiState.value = _uiState.value.copy(
            currentFileName = name,
            language = CodeHighlighter.getLanguage(name)
        )
    }

    fun saveFile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val chapterId = _uiState.value.chapter?.id ?: return@launch
            val fileName = _uiState.value.currentFileName
            val content = _uiState.value.currentContent

            repository.saveCodeFile(chapterId, fileName, content)

            // 刷新文件列表
            repository.getCodeFilesByChapter(chapterId).collect { files ->
                val savedFile = files.find { it.fileName == fileName }
                _uiState.value = _uiState.value.copy(
                    codeFiles = files,
                    currentFile = savedFile,
                    isSaving = false,
                    snackbarMessage = "文件已保存"
                )
                return@collect
            }
        }
    }

    fun createNewFile(fileName: String) {
        viewModelScope.launch {
            val chapterId = _uiState.value.chapter?.id ?: return@launch
            val content = CodeHighlighter.getDefaultTemplate(fileName)

            repository.saveCodeFile(chapterId, fileName, content)

            repository.getCodeFilesByChapter(chapterId).collect { files ->
                val newFile = files.find { it.fileName == fileName }
                _uiState.value = _uiState.value.copy(
                    codeFiles = files,
                    currentFile = newFile,
                    currentContent = content,
                    currentFileName = fileName,
                    language = CodeHighlighter.getLanguage(fileName),
                    showNewFileDialog = false,
                    snackbarMessage = "文件已创建"
                )
                return@collect
            }
        }
    }

    fun deleteFile(fileId: Int) {
        viewModelScope.launch {
            repository.deleteCodeFile(fileId)

            val chapterId = _uiState.value.chapter?.id ?: return@launch
            repository.getCodeFilesByChapter(chapterId).collect { files ->
                val currentFile = files.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    codeFiles = files,
                    currentFile = currentFile,
                    currentContent = currentFile?.content ?: "",
                    currentFileName = currentFile?.fileName ?: "main.py",
                    language = CodeHighlighter.getLanguage(currentFile?.fileName ?: "main.py"),
                    snackbarMessage = "文件已删除"
                )
                return@collect
            }
        }
    }

    fun reviewCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiReviewing = true)

            val code = _uiState.value.currentContent
            
            // 构建审查上下文：知识点 + 项目需求
            val knowledgePoints = _uiState.value.chapter?.knowledgePoints ?: ""
            val projectRequirement = _uiState.value.projectRequirement ?: ""
            val context = buildString {
                append("知识点：$knowledgePoints")
                if (projectRequirement.isNotBlank()) {
                    append("\n\n项目需求：\n$projectRequirement")
                }
            }

            val result = deepSeekHelper.reviewCode(code, context)

            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        aiReview = result.data,
                        isAiReviewing = false,
                        showReviewDialog = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        aiReview = "抱歉，发生了错误：${result.message}",
                        isAiReviewing = false,
                        showReviewDialog = true
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun submitProject() {
        viewModelScope.launch {
            val chapterId = _uiState.value.chapter?.id ?: return@launch
            
            // 保存当前代码
            saveFile()
            
            // 更新项目进度为完成
            repository.updateProjectProgress(chapterId, true)
            
            _uiState.value = _uiState.value.copy(
                isProjectReviewPassed = true,
                snackbarMessage = "项目已提交，下一章节已解锁！"
            )
        }
    }

    fun markProjectAsCompleted() {
        viewModelScope.launch {
            val chapterId = _uiState.value.chapter?.id ?: return@launch
            
            // 更新项目进度为完成
            repository.updateProjectProgress(chapterId, true)
            
            _uiState.value = _uiState.value.copy(
                isProjectReviewPassed = true,
                snackbarMessage = "项目已完成，下一章节已解锁！"
            )
        }
    }

    fun dismissReviewDialog() {
        _uiState.value = _uiState.value.copy(
            showReviewDialog = false,
            aiReview = null
        )
    }

    fun dismissNewFileDialog() {
        _uiState.value = _uiState.value.copy(showNewFileDialog = false)
    }

    fun showNewFileDialog() {
        _uiState.value = _uiState.value.copy(showNewFileDialog = true)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
