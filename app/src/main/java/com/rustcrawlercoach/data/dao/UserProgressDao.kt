package com.rustcrawlercoach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rustcrawlercoach.data.entity.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE userId = 1")
    suspend fun getUserProgressSync(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgress)

    @Update
    suspend fun update(progress: UserProgress)

    @Query("UPDATE user_progress SET streakDays = :streakDays, lastActiveDate = :date WHERE userId = 1")
    suspend fun updateStreak(streakDays: Int, date: String)

    @Query("UPDATE user_progress SET totalXp = totalXp + :xp WHERE userId = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_progress SET currentPhaseId = :phaseId, currentChapterId = :chapterId WHERE userId = 1")
    suspend fun updateCurrentProgress(phaseId: Int, chapterId: Int)

    @Query("UPDATE user_progress SET completedChapterIds = :completedIds WHERE userId = 1")
    suspend fun updateCompletedChapters(completedIds: String)

    @Query("UPDATE user_progress SET streakFreeze = streakFreeze + 1 WHERE userId = 1")
    suspend fun addStreakFreeze()

    @Query("UPDATE user_progress SET streakFreeze = streakFreeze - 1 WHERE userId = 1")
    suspend fun useStreakFreeze()
}
