package com.rustcrawlercoach.data.repository

import com.rustcrawlercoach.data.dao.ChapterDao
import com.rustcrawlercoach.data.dao.ChapterProgressDao
import com.rustcrawlercoach.data.dao.CodeFileDao
import com.rustcrawlercoach.data.dao.FeynmanAnswerDao
import com.rustcrawlercoach.data.dao.PhaseDao
import com.rustcrawlercoach.data.dao.QuestionBankDao
import com.rustcrawlercoach.data.dao.UserProgressDao
import com.rustcrawlercoach.data.entity.Chapter
import com.rustcrawlercoach.data.entity.ChapterProgress
import com.rustcrawlercoach.data.entity.CodeFile
import com.rustcrawlercoach.data.entity.FeynmanAnswer
import com.rustcrawlercoach.data.entity.Phase
import com.rustcrawlercoach.data.entity.QuestionBank
import com.rustcrawlercoach.data.entity.UserProgress
import com.rustcrawlercoach.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppRepository(
    private val phaseDao: PhaseDao,
    private val chapterDao: ChapterDao,
    private val chapterProgressDao: ChapterProgressDao,
    private val questionBankDao: QuestionBankDao,
    private val userProgressDao: UserProgressDao,
    private val codeFileDao: CodeFileDao,
    private val feynmanAnswerDao: FeynmanAnswerDao
) {
    // ==================== Phase ====================
    fun getAllPhases(): Flow<List<Phase>> = phaseDao.getAllPhases()

    suspend fun getPhaseById(phaseId: Int): Phase? = phaseDao.getPhaseById(phaseId)

    suspend fun getPhaseByOrder(orderIndex: Int): Phase? = phaseDao.getPhaseByOrder(orderIndex)

    // ==================== Chapter ====================
    fun getChaptersByPhase(phaseId: Int): Flow<List<Chapter>> = chapterDao.getChaptersByPhase(phaseId)

    suspend fun getChapterById(chapterId: Int): Chapter? = chapterDao.getChapterById(chapterId)

    suspend fun getFirstChapterOfPhase(phaseId: Int): Chapter? = chapterDao.getFirstChapterOfPhase(phaseId)

    fun getAllChapters(): Flow<List<Chapter>> = chapterDao.getAllChapters()

    suspend fun getNextChapter(currentChapterId: Int): Chapter? {
        val current = chapterDao.getChapterById(currentChapterId) ?: return null
        return chapterDao.getChapterByPhaseAndOrder(current.phaseId, current.orderIndex + 1)
            ?: chapterDao.getFirstChapterOfPhase(current.phaseId + 1)
    }

    suspend fun getTotalChapterCount(): Int = chapterDao.getTotalChapterCount()

    suspend fun updateProjectRequirement(chapterId: Int, requirement: String) {
        chapterDao.updateProjectRequirement(chapterId, requirement)
    }

    // ==================== Question ====================
    fun getQuestionsByChapter(chapterId: Int): Flow<List<QuestionBank>> =
        questionBankDao.getQuestionsByChapter(chapterId)

    suspend fun getRandomQuestions(chapterId: Int, limit: Int = 5): List<QuestionBank> =
        questionBankDao.getRandomQuestionsByChapter(chapterId, limit)

    suspend fun getQuestionCountByChapter(chapterId: Int): Int =
        questionBankDao.getQuestionCountByChapter(chapterId)

    // ==================== User Progress ====================
    fun getUserProgress(): Flow<UserProgress?> = userProgressDao.getUserProgress()

    suspend fun getUserProgressSync(): UserProgress? = userProgressDao.getUserProgressSync()

    suspend fun initializeUserProgress() {
        if (userProgressDao.getUserProgressSync() == null) {
            userProgressDao.insert(
                UserProgress(
                    userId = 1,
                    currentPhaseId = 1,
                    currentChapterId = 1,
                    completedChapterIds = "[]",
                    streakDays = 0,
                    totalXp = 0,
                    lastActiveDate = "",
                    streakFreeze = 0
                )
            )
        }
    }

    suspend fun completeChapter(chapterId: Int, xpEarned: Int) {
        val progress = userProgressDao.getUserProgressSync() ?: return
        val completedIds = parseCompletedIds(progress.completedChapterIds).toMutableSet()

        if (!completedIds.contains(chapterId)) {
            completedIds.add(chapterId)
            userProgressDao.updateCompletedChapters(completedIds.toJson())
            userProgressDao.addXp(xpEarned)
        }

        // 检查是否应该奖励补签卡（每7天）
        if ((progress.streakDays + 1) % 7 == 0) {
            userProgressDao.addStreakFreeze()
        }
    }

    suspend fun moveToNextChapter() {
        val progress = userProgressDao.getUserProgressSync() ?: return
        val nextChapter = getNextChapter(progress.currentChapterId)

        if (nextChapter != null) {
            userProgressDao.updateCurrentProgress(nextChapter.phaseId, nextChapter.id)
        }
    }

    suspend fun checkIn(): Boolean {
        val progress = userProgressDao.getUserProgressSync() ?: return false
        val today = DateUtils.getCurrentDateString()

        if (progress.lastActiveDate == today) {
            // 今天已经打卡
            return false
        }

        val yesterday = DateUtils.getYesterdayDateString()
        val newStreak = if (progress.lastActiveDate == yesterday) {
            progress.streakDays + 1
        } else {
            1
        }

        userProgressDao.updateStreak(newStreak, today)
        userProgressDao.addXp(20) // 打卡奖励 20 XP
        return true
    }

    suspend fun checkAndResetStreak() {
        val progress = userProgressDao.getUserProgressSync() ?: return
        val today = DateUtils.getCurrentDateString()
        val yesterday = DateUtils.getYesterdayDateString()

        // 如果昨天没打卡且没使用补签卡，重置连续天数
        if (progress.lastActiveDate != today &&
            progress.lastActiveDate != yesterday &&
            progress.streakDays > 0
        ) {
            if (progress.streakFreeze > 0) {
                // 使用补签卡
                userProgressDao.useStreakFreeze()
                userProgressDao.updateStreak(progress.streakDays, today)
            } else {
                userProgressDao.updateStreak(0, today)
            }
        }
    }

    suspend fun useStreakFreeze(): Boolean {
        val progress = userProgressDao.getUserProgressSync() ?: return false
        if (progress.streakFreeze > 0) {
            userProgressDao.useStreakFreeze()
            val today = DateUtils.getCurrentDateString()
            userProgressDao.updateStreak(progress.streakDays, today)
            return true
        }
        return false
    }

    private fun parseCompletedIds(json: String): Set<Int> {
        return try {
            json.removeSurrounding("[", "]")
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun Set<Int>.toJson(): String = "[${joinToString(",")}]"

    // ==================== Code Files ====================
    fun getCodeFilesByChapter(chapterId: Int): Flow<List<CodeFile>> =
        codeFileDao.getCodeFilesByChapter(chapterId)

    suspend fun saveCodeFile(chapterId: Int, fileName: String, content: String): Long {
        val existing = codeFileDao.getCodeFileByName(chapterId, fileName)
        return if (existing != null) {
            codeFileDao.update(existing.copy(content = content))
            existing.id.toLong()
        } else {
            codeFileDao.insert(CodeFile(chapterId = chapterId, fileName = fileName, content = content))
        }
    }

    suspend fun deleteCodeFile(fileId: Int) = codeFileDao.deleteById(fileId)

    suspend fun getCodeFileById(fileId: Int): CodeFile? = codeFileDao.getCodeFileById(fileId)

    // ==================== Feynman Answers ====================
    fun getFeynmanAnswersByChapter(chapterId: Int): Flow<List<FeynmanAnswer>> =
        feynmanAnswerDao.getAnswersByChapter(chapterId)

    suspend fun saveFeynmanAnswer(chapterId: Int, answerText: String): Long {
        return feynmanAnswerDao.insert(
            FeynmanAnswer(chapterId = chapterId, answerText = answerText)
        )
    }

    suspend fun updateFeynmanAnswer(answerId: Int, score: Int, feedback: String) {
        val answer = feynmanAnswerDao.getAnswerById(answerId) ?: return
        feynmanAnswerDao.update(answer.copy(aiScore = score, aiFeedback = feedback))
    }

    // ==================== Chapter Progress ====================
    fun getChapterProgress(chapterId: Int): Flow<ChapterProgress?> =
        chapterProgressDao.getChapterProgress(chapterId)

    fun getAllChapterProgress(): Flow<List<ChapterProgress>> =
        chapterProgressDao.getAllChapterProgress()

    suspend fun updateQuizProgress(chapterId: Int, score: Int) {
        val isCompleted = score >= 80
        chapterProgressDao.updateQuizProgress(chapterId, isCompleted, score)
        checkAndCompleteChapter(chapterId)
    }

    suspend fun updateProjectProgress(chapterId: Int, isCompleted: Boolean) {
        chapterProgressDao.updateProjectProgress(chapterId, isCompleted)
        checkAndCompleteChapter(chapterId)
    }

    private suspend fun checkAndCompleteChapter(chapterId: Int) {
        val chapter = chapterDao.getChapterById(chapterId) ?: return
        val progress = chapterProgressDao.getChapterProgressSync(chapterId) ?: return

        // 检查是否满足完成条件
        val canComplete = if (chapter.requiredProject) {
            progress.isQuizCompleted && progress.isProjectCompleted
        } else {
            progress.isQuizCompleted
        }

        if (canComplete && !progress.isCompleted) {
            chapterProgressDao.markChapterCompleted(chapterId, true, System.currentTimeMillis())
            
            // 更新用户进度中的完成章节列表
            val userProgress = userProgressDao.getUserProgressSync() ?: return
            val completedIds = parseCompletedIds(userProgress.completedChapterIds).toMutableSet()
            if (!completedIds.contains(chapterId)) {
                completedIds.add(chapterId)
                userProgressDao.updateCompletedChapters(completedIds.toJson())
                userProgressDao.addXp(50) // 完成章节奖励 50 XP
            }
        }
    }

    suspend fun isChapterUnlocked(chapterId: Int): Boolean {
        if (chapterId == 1) return true // 第一章总是解锁的
        
        val chapter = chapterDao.getChapterById(chapterId) ?: return false
        
        // 获取前一个章节
        val previousChapter = if (chapter.orderIndex > 1) {
            chapterDao.getChapterByPhaseAndOrder(chapter.phaseId, chapter.orderIndex - 1)
        } else {
            // 是某个阶段的第一章，需要找到上一个阶段的最后一章
            val previousPhaseId = chapter.phaseId - 1
            if (previousPhaseId < 1) return true
            val chaptersInPreviousPhase = chapterDao.getChaptersByPhase(previousPhaseId).first()
            chaptersInPreviousPhase.lastOrNull()
        } ?: return true
        
        val previousProgress = chapterProgressDao.getChapterProgressSync(previousChapter.id)
        return previousProgress?.isCompleted == true
    }

    fun getUnlockedChapters(): Flow<List<Chapter>> {
        return getAllChapters().map { chapters ->
            chapters.filter { chapter ->
                // 这里需要同步检查解锁状态，实际项目中可能需要优化
                // 为了简单起见，暂时返回所有章节，稍后在ViewModel中处理
                true
            }
        }
    }

    suspend fun getNextLockedChapterHint(chapterId: Int): String? {
        val chapter = chapterDao.getChapterById(chapterId) ?: return null
        val progress = chapterProgressDao.getChapterProgressSync(chapterId) ?: return null
        
        if (progress.isCompleted) return null
        
        val hints = mutableListOf<String>()
        if (!progress.isQuizCompleted) {
            hints.add("完成测验（正确率≥80%）")
        }
        if (chapter.requiredProject && !progress.isProjectCompleted) {
            hints.add("完成项目并通过代码审查")
        }
        
        return if (hints.isNotEmpty()) {
            "完成当前章节的${hints.joinToString("和")}才能解锁"
        } else {
            null
        }
    }

    suspend fun getCompletedChapterIds(): List<Int> =
        chapterProgressDao.getCompletedChapterIds()

    suspend fun getCompletedChapterNames(): List<String> {
        val completedIds = chapterProgressDao.getCompletedChapterIds()
        return completedIds.mapNotNull { id ->
            chapterDao.getChapterById(id)?.title
        }
    }

    suspend fun getAverageAccuracy(): Int {
        val allProgress = chapterProgressDao.getAllChapterProgressSync()
        val completedWithScores = allProgress.filter { it.isQuizCompleted && it.quizScore != null }
        if (completedWithScores.isEmpty()) return 0
        val average = completedWithScores.map { it.quizScore!! }.average()
        return average.toInt()
    }
}
