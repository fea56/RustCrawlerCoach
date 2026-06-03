package com.rustcrawlercoach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rustcrawlercoach.data.entity.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapter WHERE phaseId = :phaseId ORDER BY orderIndex ASC")
    fun getChaptersByPhase(phaseId: Int): Flow<List<Chapter>>

    @Query("SELECT * FROM chapter WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: Int): Chapter?

    @Query("SELECT * FROM chapter WHERE phaseId = :phaseId AND orderIndex = :orderIndex")
    suspend fun getChapterByPhaseAndOrder(phaseId: Int, orderIndex: Int): Chapter?

    @Query("SELECT * FROM chapter WHERE phaseId = :phaseId ORDER BY orderIndex ASC LIMIT 1")
    suspend fun getFirstChapterOfPhase(phaseId: Int): Chapter?

    @Query("SELECT * FROM chapter ORDER BY orderIndex ASC")
    fun getAllChapters(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapter ORDER BY orderIndex ASC")
    suspend fun getAllChaptersSync(): List<Chapter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<Chapter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: Chapter)

    @Query("UPDATE chapter SET projectRequirement = :requirement WHERE id = :chapterId")
    suspend fun updateProjectRequirement(chapterId: Int, requirement: String)

    @Query("SELECT COUNT(*) FROM chapter WHERE phaseId = :phaseId")
    suspend fun getChapterCountByPhase(phaseId: Int): Int

    @Query("SELECT COUNT(*) FROM chapter")
    suspend fun getTotalChapterCount(): Int
}
