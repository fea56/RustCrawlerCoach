package com.rustcrawlercoach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rustcrawlercoach.data.entity.ChapterProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterProgressDao {
    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId")
    fun getChapterProgress(chapterId: Int): Flow<ChapterProgress?>

    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId")
    suspend fun getChapterProgressSync(chapterId: Int): ChapterProgress?

    @Query("SELECT * FROM chapter_progress")
    fun getAllChapterProgress(): Flow<List<ChapterProgress>>

    @Query("SELECT * FROM chapter_progress")
    suspend fun getAllChapterProgressSync(): List<ChapterProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: ChapterProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<ChapterProgress>)

    @Update
    suspend fun update(progress: ChapterProgress)

    @Query("UPDATE chapter_progress SET isQuizCompleted = :completed, quizScore = :score WHERE chapterId = :chapterId")
    suspend fun updateQuizProgress(chapterId: Int, completed: Boolean, score: Int)

    @Query("UPDATE chapter_progress SET isProjectCompleted = :completed WHERE chapterId = :chapterId")
    suspend fun updateProjectProgress(chapterId: Int, completed: Boolean)

    @Query("UPDATE chapter_progress SET isCompleted = :completed, completedAt = :timestamp WHERE chapterId = :chapterId")
    suspend fun markChapterCompleted(chapterId: Int, completed: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT chapterId FROM chapter_progress WHERE isCompleted = 1")
    suspend fun getCompletedChapterIds(): List<Int>

    @Query("SELECT COUNT(*) FROM chapter_progress WHERE isCompleted = 1")
    suspend fun getCompletedChapterCount(): Int
}
